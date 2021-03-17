/*
 * Taken from SBRW WorldUnited.gg, original code by HeyItsLeo
 */

package com.soapboxrace.core.bo;

import io.lettuce.core.KeyValue;
import io.lettuce.core.ScanIterator;
import io.lettuce.core.api.StatefulRedisConnection;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;

import com.soapboxrace.core.bo.util.CarClassType;
import com.soapboxrace.core.bo.util.EventModeType;
import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.LobbyEntity;
import com.soapboxrace.core.jpa.PersonaPresenceEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;

/**
 * Responsible for managing the multiplayer matchmaking system.
 * This deals with 2 classes of events: restricted and open.
 * When asked for a persona for a given car class, the matchmaker
 * will check if that class is open or restricted. Open events will receive
 * players of any class, while restricted events will only receive players of
 * the required class.
 *
 * @author heyitsleo, Hypercycle
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class MatchmakingBO {

    @EJB
    private RedisBO redisBO;

    @EJB
    private ParameterBO parameterBO;
    
    @EJB
    private LobbyDAO lobbyDAO;
    
    @EJB
    private EventResultBO eventResultBO;
    
    @EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

    private StatefulRedisConnection<String, String> redisConnection;

    @PostConstruct
    public void initialize() {
        if (parameterBO.getBoolParam("REDIS_ENABLE")) {
            this.redisConnection = this.redisBO.getConnection();
            this.redisConnection.sync().del("matchmaking_queue"); // Delete any of MM queue entrants, if any exists
            System.out.println("Initialized matchmaking system");
        } else {
        	System.out.println("Redis is not enabled! Matchmaking queue is disabled.");
        }
    }

    @PreDestroy
    public void shutdown() {
        if (this.redisConnection != null) {
            this.redisConnection.sync().del("matchmaking_queue");
        }
        System.out.println("Shutdown matchmaking system");
    }

    /**
     * Adds the given Persona ID to the queue under the given search priorities.
     * CarClass, RaceFilter and isAvailable is saved as "PlayerVehicleInfo" string (Value), since personaId is a Key.
     *
     * @param personaId The ID of the persona to add to the queue.
     * @param carClass The class of the persona's current car.
     * @param raceFilter Race Filter value
     * @param isAvailable Is that entrant is available to be invited to races (1 - true, 0 - false)
     * @param searchStage Player lobby search stage (1 - class-restricted races only, 2 - priority class groups search, 3 - search all open lobbies)
     */
    public void addPlayerToQueue(Long personaId, Integer carClass, Integer raceFilter, int isAvailable, int searchStage) {
    	String playerVehicleInfo = carClass.toString() + "," + raceFilter.toString() + "," + isAvailable + "," + searchStage;
        if (this.redisConnection != null) {
            this.redisConnection.sync().hset("matchmaking_queue", personaId.toString(), playerVehicleInfo);
            matchmakingWebStatus();
//          System.out.println("playerCount add (+1): " + curPlayerCount);
        }
    }

    /**
     * Removes the given persona ID from the queue.
     *
     * @param personaId The ID of the persona to remove from the queue.
     */
    public void removePlayerFromQueue(Long personaId) {
        if (this.redisConnection != null) {
            this.redisConnection.sync().hdel("matchmaking_queue", personaId.toString());
            matchmakingWebStatus();
//          System.out.println("playerCount remove (+1): " + curPlayerCount);
        }
    }

    /**
     * Gets the ID of a persona from the queue, as long as that persona is listed under the given car class.
     * Basically, we are "pulling" the racers from MM queue to our event, if they able to participate.
     * playerVehicleInfo array: slot 0 - Car Class, slot 1 - Race Filter value, slot 2 - isAvailable value
     *
     * @param carClass The car class hash to find a persona in.
     * @param eventModeId Event Mode ID
     * @param hosterCarClass Car class hash of first player's car
     * @return The ID of the persona, or {@literal -1} if no persona was found.
     */
    public Long getPlayerFromQueue(Integer carClass, int eventModeId, int hosterCarClass, boolean isSClassFilterActive) {
        if (this.redisConnection == null) {
        	System.out.println("### redisConnection FUCKED");
            return -1L;
        }

        ScanIterator<KeyValue<String, String>> searchIterator = ScanIterator.hscan(this.redisConnection.sync(), "matchmaking_queue");
        System.out.println("### getPlayerFromQueue");
        long personaId = -1L;

        while (searchIterator.hasNext()) {
            KeyValue<String, String> keyValue = searchIterator.next();
            String[] playerVehicleInfo = keyValue.getValue().split(",");
            int playerCarClass = Integer.parseInt(playerVehicleInfo[0]);
            int playerRaceFilter = Integer.parseInt(playerVehicleInfo[1]);
            int isAvailable = Integer.parseInt(playerVehicleInfo[2]);
            int searchStage = Integer.parseInt(playerVehicleInfo[3]);
            System.out.println("### getPlayerFromQueue 2");
            
            System.out.println("### playerCarClass: " + Integer.parseInt(playerVehicleInfo[0]) + ", playerRaceFilter: " + Integer.parseInt(playerVehicleInfo[1]) + ", searchStage: " + Integer.parseInt(playerVehicleInfo[3]) + ", isAvailable: " + Integer.parseInt(playerVehicleInfo[2]) + " arrayLength: " + playerVehicleInfo.length);
            if (checkPlayerQueueRequirements(playerCarClass, playerRaceFilter, searchStage, carClass, eventModeId, 
            		hosterCarClass, isSClassFilterActive, isAvailable)) {
                personaId = Long.parseLong(keyValue.getKey());
                String newPlayerVehicleInfo = playerCarClass + "," + playerRaceFilter + "," + 0 + "," + 0; // This entrant is not available for new invites
                this.redisConnection.sync().hset("matchmaking_queue", keyValue.getKey(), newPlayerVehicleInfo);
                // removePlayerFromQueue(personaId);
                System.out.println("### getPlayerFromQueue FINAL");
                break;
            }
        }
        return personaId;
    }
    
    /**
     * Various condition checks for player and the suggested event on Matchmaking. All requirements must be passed.
     *
     * @param playerCarClass player car class
     * @param eventModeId Game mode ID of the event
     * @param hosterCarClass Car class hash of first player's car
     * @param playerRaceFilter Race Filter value (mode)
     * @param searchStage Player lobby search stage (1 - class-restricted races only, 2 - priority class groups search, 3 - search all open lobbies)
     * @param carClass The car class hash to find a persona in.
     * @param isSClassFilterActive Custom filter for separate S-class matchmaking
     * @param isAvailable when 0, that player will be not taken as a player for queue.
     * @return Is that player is allowed to participate by Misc-Class Filter (true/false)
     */
    public boolean checkPlayerQueueRequirements(int playerCarClass, int playerRaceFilter, int searchStage, Integer carClass, int eventModeId, 
    		int hosterCarClass, boolean isSClassFilterActive, int isAvailable) {
    	boolean isPlayerPassed = true;
        if (isAvailable != 1 || !isRaceFilterAllowed(playerRaceFilter, eventModeId)) {
        	return false;
        }
        if (!isSClassFilterAllowed(playerCarClass, hosterCarClass, carClass, isSClassFilterActive) 
        		|| !isMiscClassFilterAllowed(playerCarClass, eventModeId)) {
        	return false;
        }
        if (!isPriorityClassFilterAllowed(playerCarClass, carClass, hosterCarClass, searchStage)) {
        	return false;
        }
        System.out.println("### checkPlayerQueueRequirements: " + isPlayerPassed);
        return isPlayerPassed;
    }
    
    // @Schedule(minute = "*/1", hour = "*", persistent = false)
    public void matchmakingWebStatus() {
        int SClassPlayers = 0;
        int AClassPlayers = 0;
        int BClassPlayers = 0;
        int CClassPlayers = 0;
        int DClassPlayers = 0;
        int EClassPlayers = 0;
        int playerCount = 0;
        System.out.println("### matchmakingWebStatusInit");
        ScanIterator<KeyValue<String, String>> searchIterator = ScanIterator.hscan(this.redisConnection.sync(), "matchmaking_queue");

        // Check the car class of all players in MM Queue
        while (searchIterator.hasNext()) {
        	System.out.println("### matchmakingWebStatus playerFetchStart");
        	KeyValue<String, String> keyValue = searchIterator.next();
            String[] playerVehicleInfo = keyValue.getValue().split(",");
            int playerCarClass = Integer.parseInt(playerVehicleInfo[0]);
            int isAvailable = Integer.parseInt(playerVehicleInfo[2]);
            if (isAvailable == 1) {
            	switch (CarClassType.valueOf(playerCarClass)) {
                case S_CLASS:
                	SClassPlayers++;
                	playerCount++;
                	break;
                case A_CLASS:
                	AClassPlayers++;
                	playerCount++;
                	break;
                case B_CLASS:
                	BClassPlayers++;
                	playerCount++;
                	break;
                case C_CLASS:
                	CClassPlayers++;
                	playerCount++;
                	break;
                case D_CLASS:
                	DClassPlayers++;
                	playerCount++;
                	break;
                case E_CLASS:
                	EClassPlayers++;
                	playerCount++;
                	break;
                default:
                	// Should we display this category too?
                	playerCount++;
                	break;
                }
            }
            System.out.println("### matchmakingWebStatus playerFetchEnd");
        }
        if (playerCount > 0) {
        	 System.out.println("### Players searching on Race Now, by classes: S[" + SClassPlayers + "], A[" + AClassPlayers + "], B[" + BClassPlayers + "],"
             		+ " C[" + CClassPlayers + "], D[" + DClassPlayers + "], E[" + EClassPlayers + "]");
        	 System.out.println("### Players searching on Race Now: " + playerCount);
        }
   
        List<LobbyEntity> lobbiesList = lobbyDAO.findAllOpen();
        if (lobbiesList.isEmpty()) {
        	System.out.println("### No public lobbies is available yet.");
        }
        else {
        	for (LobbyEntity lobby : lobbiesList) {
        		int lobbyHosterCarClass = lobby.getCarClassHash();
        		EventEntity lobbyEvent = lobby.getEvent();
        		String isTimerActive = "Search";
        		if (lobby.getLobbyDateTimeStart() != null) {
        			isTimerActive = "Waiting";
        		}
        		String eventName = lobbyEvent.getName();
        		int eventMode = lobbyEvent.getEventModeId();
        		int eventCarClass = lobbyEvent.getCarClassHash();
        		Long lobbyTeamPlayer = lobby.getTeam1Id();
        		
        		String lobbyHosterCarClassStr = eventResultBO.getCarClassLetter(lobbyHosterCarClass);
        		String eventCarClassStr = eventResultBO.getCarClassLetter(eventCarClass);
        		
        		StringBuilder lobbyInfoOutput = new StringBuilder();
        		lobbyInfoOutput.append("### Mode: " + EventModeType.valueOf(eventMode) + ", ");
        		lobbyInfoOutput.append("Class: " + eventCarClassStr + ", ");
        		lobbyInfoOutput.append("Event: " + eventName + ", ");
        		lobbyInfoOutput.append("Priority Class: " + lobbyHosterCarClassStr + ", ");
        		if (lobbyTeamPlayer != null) {
        			lobbyInfoOutput.append("[T], ");
        		}
        		lobbyInfoOutput.append("Status: " + isTimerActive + ", ");
        		System.out.println(lobbyInfoOutput.toString());
            }
        }
    }
    
    /**
     * Checks if player Race Filter is allowing him to participate on the suggested event.
     *
     * @param playerRaceFilter Race Filter value (mode)
     * @param eventModeId Game mode ID of the event
     * @return Is that event allowed by Race Filter (true/false)
     */
    public boolean isRaceFilterAllowed(int playerRaceFilter, int eventModeId) {
    	boolean isRaceFilterAllowed = false;
    	switch (playerRaceFilter) {
    	case 1: // Sprint & Circuit
			if (eventModeId == EventModeType.SPRINT.getId() || eventModeId == EventModeType.CIRCUIT.getId()) {isRaceFilterAllowed = true;}
			break;
		case 2: // Drag
			if (eventModeId == EventModeType.DRAG.getId()) {isRaceFilterAllowed = true;}
			break;	
		case 3: // All Races
			if (eventModeId == EventModeType.SPRINT.getId() || eventModeId == EventModeType.CIRCUIT.getId() || 
			eventModeId == EventModeType.DRAG.getId()) {isRaceFilterAllowed = true;}
			break;
		case 4: // Team Escape
			if (eventModeId == EventModeType.TEAM_ESCAPE.getId() || eventModeId == EventModeType.INTERCEPTOR.getId()) {isRaceFilterAllowed = true;}
			break;
		default: // No Filter
			isRaceFilterAllowed = true;
			break;
    	}
        System.out.println("### isRaceFilterAllowed: " + isRaceFilterAllowed);
        return isRaceFilterAllowed;
    }
    
    /**
     * Checks if player car class fits with his search stage (class group priority) and the event class-restriction.
     *
     * @param playerCarClass Car class hash of player car
     * @param eventCarClass Class-restriction of the event
     * @param hosterCarClass Car class hash of first player's car
     * @param searchStage Player lobby search stage (1 - class-restricted races only, 2 - priority class groups search, 3 - search all open lobbies)
     * @return Is that event allowed by Priority Class Filter (true/false)
     */
    public boolean isPriorityClassFilterAllowed(int playerCarClass, int eventCarClass, int hosterCarClass, int searchStage) {
    	boolean isPriorityClassFilterAllowed = false;
    	if (eventCarClass == CarClassType.OPEN_CLASS.getId()) {
    		if (searchStage == 2) { // If race search is on stage 2, player should get the lobbies which fits his class priority group
        		switch (CarClassType.valueOf(hosterCarClass)) {
        		case S_CLASS: // S Class group
        			if (playerCarClass == CarClassType.S_CLASS.getId()) {
        				isPriorityClassFilterAllowed = true;
        			}
        			break;
        		case A_CLASS:
    			case B_CLASS: // A-B Classes group
    				if (playerCarClass == CarClassType.A_CLASS.getId() || playerCarClass == CarClassType.B_CLASS.getId()) {
        				isPriorityClassFilterAllowed = true;
        			}
    				break;
    			case C_CLASS:
    			case D_CLASS:
    			case E_CLASS: // C-D-E Classes group
    				if (playerCarClass == CarClassType.C_CLASS.getId() || playerCarClass == CarClassType.D_CLASS.getId() 
    				|| playerCarClass == CarClassType.E_CLASS.getId()) {
        				isPriorityClassFilterAllowed = true;
        			}
    				break;
				default:
					isPriorityClassFilterAllowed = true; // Misc-Class is out of Priority Class Groups
					break;
        		}
        	}
        	if (searchStage == 3) { // Filter is inactive, search for any open lobby
        		isPriorityClassFilterAllowed = true;
        	}
    	}
    	else if (playerCarClass == eventCarClass) { // Class-restricted race and player fits in
        		isPriorityClassFilterAllowed = true;
        }
        System.out.println("### isPriorityClassFilterAllowed: " + isPriorityClassFilterAllowed + ", searchStage: " + searchStage);
        return isPriorityClassFilterAllowed;
    }
    
    /**
     * Checks if player is able to participate on event, where the hoster is on S-class car.
     * Needs RACENOW_SCLASS_SEPARATE parameter to be active, otherwise it's always returns true.
     *
     * @param playerCarClass player car class
     * @param hosterCarClass first player (hoster) car class
     * @param eventCarClass event car class restriction
     * @return Is that player is allowed to participate by S-Class Filter (true/false)
     */
    public boolean isSClassFilterAllowed(int playerCarClass, int hosterCarClass, int eventCarClass, boolean isSClassFilterActive) {
    	int SClassHash = CarClassType.S_CLASS.getId();
    	boolean isSClassFilterAllowed = true;
    	// We don't need to check S-class restricted races
    	if (eventCarClass != SClassHash && isSClassFilterActive && (hosterCarClass == SClassHash || SClassHash == playerCarClass) 
    			&& playerCarClass != hosterCarClass) {
    		isSClassFilterAllowed = false; // Only S-Class cars is able to participate on races, which is hosted by players on S-class cars 
    	}
        System.out.println("### isSClassFilterAllowed: " + isSClassFilterAllowed);
        return isSClassFilterAllowed;
    }
    
    /**
     * Checks if player on Misc-class car is able to participate on event. Drag-events is restricted.
     *
     * @param playerCarClass player car class
     * @param eventModeId Game mode ID of the event
     * @return Is that player is allowed to participate by Misc-Class Filter (true/false)
     */
    public boolean isMiscClassFilterAllowed(int playerCarClass, int eventModeId) {
    	boolean isMiscClassFilterAllowed = true;
    	if (playerCarClass == CarClassType.MISC.getId() && eventModeId == EventModeType.DRAG.getId()) {
    		isMiscClassFilterAllowed = false;
    	}
        System.out.println("### isMiscClassFilterAllowed: " + isMiscClassFilterAllowed);
        return isMiscClassFilterAllowed;
    }

    /**
     * Add the given event ID to the list of ignored events for the given persona ID.
     *
     * @param personaId the persona ID
     * @param eventId the event ID
     */
    public void ignoreEvent(long personaId, EventEntity EventEntity) {
    	// Event will be ignored only when player is on Race Now search
        if (this.redisConnection != null) {
        	System.out.println("### hexists: " + this.redisConnection.sync().hexists("matchmaking_queue", Long.toString(personaId)));
        	if (this.redisConnection.sync().hexists("matchmaking_queue", Long.toString(personaId))) {
        		this.redisConnection.sync().sadd("ignored_events." + personaId, Long.toString(EventEntity.getId()));
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### " + EventEntity.getName() + " will be ignored in the Race Now search for a while."), personaId);
        	}
        }
    }
    
    /**
     * Checks if the specified player is on Race Now search.
     *
     * @param personaId the persona ID
     */
    public boolean isPlayerOnMMSearch(long personaId) {
    	boolean isExists = false;
        if (this.redisConnection != null) {
        	isExists = this.redisConnection.sync().hexists("matchmaking_queue", Long.toString(personaId));
        }
        return isExists;
    }

    /**
     * Resets the list of ignored events for the given persona ID
     *
     * @param personaId the persona ID
     */
    public void resetIgnoredEvents(long personaId) {
        if (this.redisConnection != null) {
            this.redisConnection.sync().del("ignored_events." + personaId);
        }
    }

    /**
     * Checks if the given event ID is in the list of ignored events for the given persona ID
     *
     * @param personaId the persona ID
     * @param eventId the event ID
     * @return {@code true} if the given event ID is in the list of ignored events for the given persona ID
     */
    public boolean isEventIgnored(long personaId, long eventId) {
        if (this.redisConnection != null) {
            return this.redisConnection.sync().sismember("ignored_events." + personaId, Long.toString(eventId));
        }
        return false;
    }
    
    /**
     * Get the ignored by persona events list
     *
     * @param personaId the persona ID
     * @return int-list of ignored eventId
     */
    public List<Integer> getEventIgnoredList(long personaId) {
    	List<Integer> eventIgnoreList = new ArrayList<Integer>();
        if (this.redisConnection != null) {
            ScanIterator<String> ignoreIterator = ScanIterator.sscan(this.redisConnection.sync(), "ignored_events." + personaId);
            while (ignoreIterator.hasNext()) {
            	String eventId = ignoreIterator.next();
            	eventIgnoreList.add(Integer.parseInt(eventId));
            }
        }
        return eventIgnoreList;
    }

    @Asynchronous
    @Lock(LockType.READ)
    public void handlePersonaPresenceUpdated(PersonaPresenceEntity personaPresenceEntity) {
        removePlayerFromQueue(personaPresenceEntity.getActivePersonaId());
    }
}