package com.soapboxrace.core.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.*;

import com.soapboxrace.core.bo.util.CarClassType;
import com.soapboxrace.core.bo.util.EventModeType;
import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.LobbyEntity;
import com.soapboxrace.core.jpa.PersonaPresenceEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.ArrayOfMMLobbies;

/**
 * Matchmaking system with various filters for players & lobby hoster, including Ignored Events system.
 * Original base code by HeyItsLeo
 *
 * @author heyitsleo, Hypercycle
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class MatchmakingBO {

	// Stores the queue with players & their information
	private final Map<Long, String> mmQueuePlayers = new ConcurrentHashMap<>();
	// Stores the ignored events lists for players
    private final Map<Long, List<Long>> mmIgnoredEvents = new ConcurrentHashMap<>();
	
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
    	mmQueuePlayers.put(personaId, playerVehicleInfo);
    }

    /**
     * Removes the given persona ID from the queue.
     * @param personaId The ID of the persona to remove from the queue.
     */
    public void removePlayerFromQueue(Long personaId) {
    	mmQueuePlayers.remove(personaId);
    }

    /**
     * Gets the ID of a persona from the queue, as long as that persona is listed under the given car class.
     * Basically, we are "pulling" the racers from MM queue to our event, if they able to participate.
     * playerVehicleInfo array: slot 0 - Car Class, slot 1 - Race Filter value, slot 2 - isAvailable value
     *
     * @param carClass The car class hash to find a persona in.
     * @param eventModeId Event Mode ID
     * @param hosterCarClass Car class hash of first player's car
     * @param isSClassFilterActive Custom filter for separate S-class matchmaking
     * @param eventMaxPlayers How much players that event can have
     * @return List of personas (by ID) which is able to participate
     */
    public List<Long> getPlayersFromQueue(Integer carClass, int eventModeId, int hosterCarClass, boolean isSClassFilterActive, 
    		int eventMaxPlayers, int eventId) {
        // System.out.println("### getPlayerFromQueue");
        List<Long> readyPlayers = new ArrayList<Long>();
        long personaId = -1L;
        int playersFounded = 1; // We already have 1 of EventMaxPlayers entrant (the hoster)
        
        for (Map.Entry<Long, String> queueEntry : this.mmQueuePlayers.entrySet()) {
        	if (playersFounded >= eventMaxPlayers) break;
        	
            String[] playerVehicleInfo = queueEntry.getValue().split(",");
            int playerCarClass = Integer.parseInt(playerVehicleInfo[0]);
            int playerRaceFilter = Integer.parseInt(playerVehicleInfo[1]);
            int isAvailable = Integer.parseInt(playerVehicleInfo[2]);
            int searchStage = Integer.parseInt(playerVehicleInfo[3]);
            personaId = queueEntry.getKey();
            // System.out.println("### getPlayerFromQueue 2");
            
            // System.out.println("### playerCarClass: " + Integer.parseInt(playerVehicleInfo[0]) + ", playerRaceFilter: " + Integer.parseInt(playerVehicleInfo[1]) + ", searchStage: " + Integer.parseInt(playerVehicleInfo[3]) + ", isAvailable: " + Integer.parseInt(playerVehicleInfo[2]) + " arrayLength: " + playerVehicleInfo.length);
            if (checkPlayerQueueRequirements(playerCarClass, playerRaceFilter, searchStage, carClass, eventModeId, 
            		hosterCarClass, isSClassFilterActive, isAvailable) && !isEventIgnored(personaId, eventId)) {
                String newPlayerVehicleInfo = playerCarClass + "," + playerRaceFilter + "," + 0 + "," + 0; // This entrant is not available for new invites
                mmQueuePlayers.replace(personaId, newPlayerVehicleInfo);
                // System.out.println("### getPlayerFromQueue FINAL");
                readyPlayers.add(personaId);
                playersFounded++;
                break;
            }
        }
        return readyPlayers;
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
        // System.out.println("### checkPlayerQueueRequirements: " + isPlayerPassed);
        return isPlayerPassed;
    }
    
    /**
     * Information output for API, contains real-time Race Now status and available lobbies.
     * @return ArrayOfMMLobbies class with Matchmaking data
     */
    public ArrayOfMMLobbies matchmakingWebStatus() {
        // System.out.println("### matchmakingWebStatusInit");
        ArrayOfMMLobbies arrayOfMMLobbies = new ArrayOfMMLobbies();
        int playerCount = 0;

        // Check the car class of all players in MM Queue
        for (Map.Entry<Long, String> queueEntry : this.mmQueuePlayers.entrySet()) {
        	// System.out.println("### matchmakingWebStatus playerFetchStart");
            String[] playerVehicleInfo = queueEntry.getValue().split(",");
            int playerCarClass = Integer.parseInt(playerVehicleInfo[0]);
            int isAvailable = Integer.parseInt(playerVehicleInfo[2]);
            if (isAvailable == 1) {
            	switch (CarClassType.valueOf(playerCarClass)) {
                case S_CLASS:
                	arrayOfMMLobbies.setPlayerCountS(arrayOfMMLobbies.getPlayerCountS() + 1); break;
                case A_CLASS:
                	arrayOfMMLobbies.setPlayerCountA(arrayOfMMLobbies.getPlayerCountA() + 1); break;
                case B_CLASS:
                	arrayOfMMLobbies.setPlayerCountB(arrayOfMMLobbies.getPlayerCountB() + 1); break;
                case C_CLASS:
                	arrayOfMMLobbies.setPlayerCountC(arrayOfMMLobbies.getPlayerCountC() + 1); break;
                case D_CLASS:
                	arrayOfMMLobbies.setPlayerCountD(arrayOfMMLobbies.getPlayerCountD() + 1); break;
                case E_CLASS:
                	arrayOfMMLobbies.setPlayerCountE(arrayOfMMLobbies.getPlayerCountE() + 1); break;
                default:
                	// Should we display this category too?
                	arrayOfMMLobbies.setPlayerCountMisc(arrayOfMMLobbies.getPlayerCountMisc() + 1); break;
                }
            	playerCount++;
            }
            // System.out.println("### matchmakingWebStatus playerFetchEnd");
        }
        arrayOfMMLobbies.setPlayerCountAll(playerCount);
   
        // Let's get the lobbies information
        List<LobbyEntity> lobbiesList = lobbyDAO.findAllOpen();
        if (!lobbiesList.isEmpty()) {
        	// System.out.println("### matchmakingWebStatus lobbiesStart");
        	for (LobbyEntity lobby : lobbiesList) {
        		EventEntity lobbyEvent = lobby.getEvent();
        		String eventName = lobbyEvent.getName();
        		String eventMode = EventModeType.valueOf(lobbyEvent.getEventModeId()).toString();
        		String eventCarClassStr = eventResultBO.getCarClassLetter(lobbyEvent.getCarClassHash());
        		String lobbyHosterCarClassStr = eventResultBO.getCarClassLetter(lobby.getCarClassHash());
        		
        		boolean isPlayersInside = false;
        		if (lobby.getLobbyDateTimeStart() != null) {isPlayersInside = true;}
        		boolean isTeamRace = false;
        		if (lobby.getTeam1Id() != null) {isTeamRace = true;}
        		
        		arrayOfMMLobbies.add(eventMode, eventName, eventCarClassStr, lobbyHosterCarClassStr, isTeamRace, isPlayersInside);
        		// System.out.println("### matchmakingWebStatus lobbiesOne");
            }
        }
        // System.out.println("### matchmakingWebStatus finish");
        return arrayOfMMLobbies;
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
        // System.out.println("### isRaceFilterAllowed: " + isRaceFilterAllowed);
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
        // System.out.println("### isPriorityClassFilterAllowed: " + isPriorityClassFilterAllowed + ", searchStage: " + searchStage);
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
        // System.out.println("### isSClassFilterAllowed: " + isSClassFilterAllowed);
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
        // System.out.println("### isMiscClassFilterAllowed: " + isMiscClassFilterAllowed);
        return isMiscClassFilterAllowed;
    }

    /**
     * Add the given event ID to the list of ignored events for the given persona ID.
     *
     * @param personaId the persona ID
     * @param eventEntity Entity of the event
     */
    public void ignoreEvent(long personaId, EventEntity eventEntity) {
        getIgnoredEvents(personaId).add((long) eventEntity.getId());
        openFireSoapBoxCli.send(XmppChat.createSystemMessage("### " + eventEntity.getName() + " will be ignored in the Race Now search for a while."), personaId);
    }
    
    /**
     * Checks if the specified player is on Race Now search.
     * @param personaId the persona ID
     */
    public boolean isPlayerOnMMSearch(long personaId) {
        return mmQueuePlayers.containsKey(personaId);
    }

    /**
     * Resets the list of Ignored Events for the given persona ID.
     * @param personaId the persona ID
     */
    public void resetIgnoredEvents(long personaId) {
    	mmIgnoredEvents.remove(personaId);
    }

    /**
     * Checks if the given event ID is in the list of ignored events for the given persona ID.
     *
     * @param personaId the persona ID
     * @param eventId the event ID
     * @return {@code true} if the given event ID is in the list of Ignored Events for the given persona ID.
     */
    public boolean isEventIgnored(long personaId, long eventId) {
        return getIgnoredEvents(personaId).contains(eventId);
    }
    
    /**
     * Get the ignored events list by persona.
     *
     * @param personaId the persona ID
     * @return long-list of Ignored EventIds
     */
    public List<Long> getIgnoredEvents(long personaId) {
        return mmIgnoredEvents.computeIfAbsent(personaId, k -> new ArrayList<>());
    }

    @Asynchronous
    @Lock(LockType.READ) // TODO
    public void handlePersonaPresenceUpdated(PersonaPresenceEntity personaPresenceEntity) {
        removePlayerFromQueue(personaPresenceEntity.getActivePersonaId());
    }
}