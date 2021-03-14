package com.soapboxrace.core.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.dao.LobbyEntrantDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.LobbyEntity;
import com.soapboxrace.core.jpa.LobbyEntrantEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.xmpp.OpenFireRestApiCli;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.core.xmpp.XmppLobby;
import com.soapboxrace.jaxb.http.ArrayOfLobbyEntrantInfo;
import com.soapboxrace.jaxb.http.CustomCarTrans;
import com.soapboxrace.jaxb.http.LobbyCountdown;
import com.soapboxrace.jaxb.http.LobbyEntrantAdded;
import com.soapboxrace.jaxb.http.LobbyEntrantInfo;
import com.soapboxrace.jaxb.http.LobbyEntrantRemoved;
import com.soapboxrace.jaxb.http.LobbyInfo;
import com.soapboxrace.jaxb.xmpp.XMPP_LobbyInviteType;

@Stateless
public class LobbyBO {

	@EJB
	private EventDAO eventDao;

	@EJB
	private EventSessionDAO eventSessionDao;

	@EJB
	private PersonaDAO personaDao;

	@EJB
	private TokenSessionDAO tokenDAO;

	@EJB
	private LobbyDAO lobbyDao;

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private LobbyEntrantDAO lobbyEntrantDao;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private OpenFireRestApiCli openFireRestApiCli;
	
	@EJB
	private TokenSessionBO tokenSessionBO;

	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private TeamsDAO teamsDao;
	
	@EJB
	private LobbyCountdownBO lobbyCountdownBO;
	
	@EJB
	private TeamsBO teamsBO;
	
	@EJB
	private MatchmakingBO matchmakingBO;
	
	@EJB
	private LobbyEntrantDAO lobbyEntrantDAO;
	
	@EJB
	private LobbyKeepAliveBO lobbyKeepAliveBO;

	// Checking the division of selected car - Hypercycle
	public String carDivision(int carClassHash) {
		if (carClassHash == 872416321 || carClassHash == 415909161 || carClassHash == 1866825865) {
			return "edc";
		}
		if (carClassHash == 0 || carClassHash == 1337) {
			return "npc";
		}
		return "bas";
	}
		
	public void joinFastLobby(Long personaId, int carClassHash, int raceFilter, boolean isSClassFilterActive, int searchStage) {
		// List<LobbyEntity> lobbys = lobbyDao.findAllOpen(carClassHash);
        // System.out.println("MM START Time: " + LocalDateTime.now());
		System.out.println("joinFastLobby");
		PersonaEntity personaEntity = personaDao.findById(personaId);
		boolean redis = parameterBO.getBoolParam("REDIS_ENABLE");
		System.out.println("### joinFastLobby, searchStage: " + searchStage);
		List<LobbyEntity> lobbys = lobbyDao.findAllMPLobbies(carClassHash, raceFilter, searchStage);
		if (lobbys.isEmpty() && searchStage == 1) { // If class-restricted and class group search is not succeed, initiate Priority Class Group search
			searchStage = 2;
		}
		
		if (redis) { // Do two "for" loops to avoid ConcurrentModificationException
			List<Integer> eventIgnoredList = matchmakingBO.getEventIgnoredList(personaId);
			if (personaEntity.isIgnoreRaces() && !eventIgnoredList.isEmpty()) {
				List<Integer> idsToRemove = new ArrayList<Integer>();
				for (LobbyEntity lobby : lobbys) {
					if (eventIgnoredList.contains(lobby.getEvent().getId())) {
						idsToRemove.add(lobbys.indexOf(lobby)); // Hold that lobby ID to remove it
					}
				}
				if (!idsToRemove.isEmpty()) {
					for (int idToRemove : idsToRemove) {
						lobbys.remove(idToRemove); // Remove the lobby with ignored event ID
					}
				}
			}
		}
		
		if (redis && lobbys.isEmpty()) {
			matchmakingBO.addPlayerToQueue(personaId, carClassHash, raceFilter, 1, searchStage);
			System.out.println("### searchStage: " + searchStage);
			int priorityMMTimeout = personaEntity.getPriorityMMTimeout();
			if (searchStage == 2) {
				if (priorityMMTimeout != 0) {
					lobbyKeepAliveBO.searchPriorityTimer(personaId, carClassHash, raceFilter, isSClassFilterActive, priorityMMTimeout);
				}
				else {
					joinFastLobby(personaId, carClassHash, raceFilter, isSClassFilterActive, 3); // Skip the Priority timeout
				}
			}
		}
//		else {
//			if (lobbys.isEmpty() && parameterBO.getBoolParam("RACENOW_RANDOMRACES")) {
//				// System.out.println("MM RANDOM CREATE Time: " + LocalDateTime.now());
//				System.out.println("createRandomLobby");
//				createRandomLobby(securityToken, personaId, raceFilter, carClassHash);
//			}
//		}
		if (!lobbys.isEmpty()) {
			System.out.println("### addFakeToQueue");
			matchmakingBO.addPlayerToQueue(personaId, carClassHash, raceFilter, 0, 0); // Temporary entry for "Ignore Races" feature
			joinLobby(personaEntity, lobbys, true, carClassHash, isSClassFilterActive);
		}
	}

	public void joinQueueEvent(Long personaId, int eventId, int carClassHash) {
		System.out.println("joinQueueEvent");
		PersonaEntity personaEntity = personaDao.findById(personaId);
//		String carDivision = this.carDivision(carClassHash);
		List<LobbyEntity> lobbys = lobbyDao.findByEventStarted(eventId);
		if (lobbys.size() == 0) {
			createLobby(personaEntity, eventId, false, false, carClassHash);
		} else {
			joinLobby(personaEntity, lobbys, false, carClassHash, parameterBO.getBoolParam("RACENOW_SCLASS_SEPARATE"));
		}
	}

	// Send the invites to your current in-game group
	// TODO Manual invites for players?
	public void createPrivateLobby(Long personaId, int eventId, int carClassHash) {
		List<Long> listOfPersona = openFireRestApiCli.getAllPersonaByGroup(personaId);
		if (!listOfPersona.isEmpty()) {
			PersonaEntity personaEntity = personaDao.findById(personaId);
//			String carDivision = this.carDivision(carClassHash);
			createLobby(personaEntity, eventId, true, false, carClassHash);

			LobbyEntity lobbys = lobbyDao.findByEventAndPersona(eventId, personaId);
			if (lobbys != null) {
				XMPP_LobbyInviteType lobbyInviteType = new XMPP_LobbyInviteType();
				lobbyInviteType.setEventId(eventId);
				lobbyInviteType.setInvitedByPersonaId(personaId);
				lobbyInviteType.setInviteLifetimeInMilliseconds(60);
				lobbyInviteType.setPrivate(true);
				lobbyInviteType.setLobbyInviteId(lobbys.getId());

				for (Long idPersona : listOfPersona) {
					if (!idPersona.equals(personaId)) {
						XmppLobby xmppLobby = new XmppLobby(idPersona, openFireSoapBoxCli);
						xmppLobby.sendLobbyInvite(lobbyInviteType);
					}
				}
			}
		}
	}

	public Long createLobby(PersonaEntity personaEntity, int eventId, Boolean isPrivate, boolean tempCreated, int carClassHash) {
		EventEntity eventEntity = eventDao.findById(eventId);
		int eventClass = eventEntity.getCarClassHash();
		int eventMaxPlayers = eventEntity.getMaxPlayers();
		Long personaId = personaEntity.getPersonaId();
		tokenSessionBO.setSearchEventId(personaId, eventId);
		boolean isSClassFilterActive = parameterBO.getBoolParam("RACENOW_SCLASS_SEPARATE");
		
		LobbyEntity lobbyEntity = new LobbyEntity();
		lobbyEntity.setEvent(eventEntity);
		lobbyEntity.setIsPrivate(isPrivate);
		lobbyEntity.setPersonaId(personaEntity.getPersonaId());
		lobbyEntity.setIsReserved(false);
		lobbyEntity.setLobbyDateTimeStart(null); // Don't count the time until any player gets the invite
		lobbyEntity.setCarClassHash(carClassHash);
		lobbyEntity.setStarted(false);
//		lobbyEntity.setCarDivision(carDivision);
		lobbyDao.insert(lobbyEntity);

		if (parameterBO.getBoolParam("REDIS_ENABLE")) { // Queue Matchmaking
//			matchmakingBO.changePlayerCountInQueue(true); // Add the hoster to the queue player count, but not in the queue itself
			int playersFounded = 1;
			for (int i = 1; i <= eventMaxPlayers - 1; i++) { // Search a players for all of the event slots
	            if (lobbyEntrantDAO.getPlayerCount(lobbyEntity) >= eventMaxPlayers) break;

	            System.out.println("### Get the player...");
	            Long queuePersonaId = matchmakingBO.getPlayerFromQueue(eventClass, eventEntity.getEventModeId(), carClassHash, isSClassFilterActive);

	            if (!queuePersonaId.equals(-1L) && !matchmakingBO.isEventIgnored(queuePersonaId, eventId)) {
	            	 System.out.println("### Get the player THERE...");
	                if (lobbyEntrantDAO.getPlayerCount(lobbyEntity) < eventMaxPlayers) {
	                    sendJoinEvent(queuePersonaId, lobbyEntity, eventId);
	                    playersFounded++;
	                    System.out.println("### Get the player DONE");
	                }
	            }
	        }
			if (playersFounded > 1) {
				System.out.println("### Get the player ACTIVE");
//				matchmakingBO.changePlayerCountInQueue(false); // Remove the hoster from queue player count
            	sendJoinEvent(personaId, lobbyEntity, eventId);
            	setIsLobbyReserved(lobbyEntity, true);
            	if (!tempCreated) {
            		lobbyEntity.setLobbyDateTimeStart(new Date());
            		lobbyDao.update(lobbyEntity);
            		lobbyCountdownBO.scheduleLobbyStart(lobbyEntity);
            	}
            }
		}
		else {
			sendJoinEvent(personaId, lobbyEntity, eventId);
		}
		// This lobby has been created again, when player got a invite, but the lobby itself is not exists anymore (e.g other player has declined the invite)
		if (tempCreated) { 
			System.out.println("### tempCreated timer");
			lobbyEntity.setLobbyDateTimeStart(new Date());
    		lobbyDao.update(lobbyEntity);
			lobbyCountdownBO.scheduleLobbyStart(lobbyEntity); // On some cases we need to start the timer for 1-player lobby
		}
		return lobbyEntity.getId();
	}
	
	public void setIsLobbyReserved(LobbyEntity lobbyEntity, boolean reserved) {
		lobbyEntity.setIsReserved(reserved);
		lobbyDao.update(lobbyEntity);
	}
	
	private void createRandomLobby(String securityToken, Long personaId, int raceFilter, int carClassHash) {
		EventEntity eventEntity = new EventEntity();
		List<EventEntity> eventList = new ArrayList<EventEntity>();
		switch (raceFilter) {
		case 1: // Sprint & Circuit
			eventList = eventDao.findRacesSearchEnabled(carClassHash);
			break;
		case 2: // Drag
			eventList = eventDao.findDragSearchEnabled(carClassHash);
			break;
		case 3: // All Races
			eventList = eventDao.findAllRacesSearchEnabled(carClassHash);
			break;
		case 4: // Team Escape
			eventList = eventDao.findTESearchEnabled(carClassHash);
			break;
		default: // No filter
			eventList = eventDao.findAllSearchEnabled(carClassHash);
			break;
		}
		
		// System.out.println("MM RANDOM LIST Time: " + LocalDateTime.now());
		int listSize = eventList.size();
		eventEntity.setId(randomEventId(eventList, listSize)); 

		// System.out.println("MM RANDOM LOBBY Time: " + LocalDateTime.now());
		LobbyEntity lobbyEntity = new LobbyEntity();
		lobbyEntity.setEvent(eventEntity);
		lobbyEntity.setIsPrivate(false);
		lobbyEntity.setLobbyDateTimeStart(null);
		lobbyEntity.setPersonaId(personaId);
		lobbyDao.insert(lobbyEntity);

		// System.out.println("MM RANDOM FINAL Time: " + LocalDateTime.now());
		openFireSoapBoxCli.send(XmppChat.createSystemMessage("### New MP race is created."), personaId);
		sendJoinEvent(personaId, lobbyEntity, eventEntity.getId());
		setIsLobbyReserved(lobbyEntity, true);
		lobbyCountdownBO.scheduleLobbyStart(lobbyEntity);
		lobbyDao.update(lobbyEntity);
		// System.out.println("MM RANDOM END Time: " + LocalDateTime.now());
	}
	
	// Random generated eventId
	private int randomEventId(List<EventEntity> eventList, int listSize) {
		Random rand = new Random();
		// System.out.println("MM RANDOM GETCAR Time: " + LocalDateTime.now());
		int randomEntityId = rand.nextInt(listSize);
	    EventEntity randomEvent = eventList.get(randomEntityId);
		// System.out.println("MM RANDOM CHOOSE Time: " + LocalDateTime.now());
		return randomEvent.getId();
	}

	// FIXME I'm not sure how the server will react on lobby-list, where all lobbies is full...
	private void joinLobby(PersonaEntity personaEntity, List<LobbyEntity> lobbys, boolean checkIgnoredEvents, 
			int playerCarHash, boolean isSClassFilterActive) {
		Long personaId = personaEntity.getPersonaId();
		System.out.println("joinLobby for " + personaId);
		LobbyEntity lobbyEntity = null;
		LobbyEntity lobbyEntityEmpty = null;
		int eventId = 0;
		EventEntity eventEntity = null;
		for (LobbyEntity lobbyEntityTmp : lobbys) {
			eventEntity = lobbyEntityTmp.getEvent();
			eventId = lobbyEntityTmp.getEvent().getId();
			int hosterCarClass = lobbyEntityTmp.getCarClassHash();
			
			if ((lobbyEntityTmp.getPersonaId().equals(personaId) && !lobbyEntityTmp.isReserved()) || // Player cannot join the lobby, which is being hosted by player himself and not yet populated
				(checkIgnoredEvents && matchmakingBO.isEventIgnored(personaEntity.getPersonaId(), eventId)) || // This event is being ignored by player, look for others
				(isSClassFilterActive && !matchmakingBO.isSClassFilterAllowed(playerCarHash, hosterCarClass, eventEntity.getCarClassHash(), isSClassFilterActive))) { // S-Class filter check (if parameter is active)
				continue; 
			}
			
			int maxEntrants = eventEntity.getMaxPlayers();
			List<LobbyEntrantEntity> lobbyEntrants = lobbyEntityTmp.getEntrants();
			int entrantsSize = lobbyEntrants.size();
			if (entrantsSize < maxEntrants) {
				if (lobbyEntityEmpty == null) { // In case of empty lobby-list, player will got the first empty lobby
					lobbyEntityEmpty = lobbyEntityTmp;
				}
				if (entrantsSize > 0) {
					lobbyEntity = lobbyEntityTmp;
					if (!isPersonaInside(personaId, lobbyEntrants)) {
						LobbyEntrantEntity lobbyEntrantEntity = new LobbyEntrantEntity();
						lobbyEntrantEntity.setPersona(personaEntity);
						lobbyEntrantEntity.setLobby(lobbyEntity);
						lobbyEntrants.add(lobbyEntrantEntity);
					}
					break;
				}
			}
		}
		if (lobbyEntity != null) {
//			System.out.println("MM END Time: " + System.currentTimeMillis());
			sendJoinEvent(personaId, lobbyEntity, eventId);
		}
		if (lobbyEntity == null && lobbyEntityEmpty != null) { // If all lobbies on the search is empty, player will got the first created empty lobby
//			System.out.println("MM END Time: " + System.currentTimeMillis());
			
			System.out.println("second choice for " + lobbyEntityEmpty.getPersonaId());
			sendJoinEvent(personaId, lobbyEntityEmpty, eventId);
			Long hosterPersonaId = lobbyEntityEmpty.getPersonaId();
			if (!personaId.equals(hosterPersonaId)) {
				System.out.println("callbackRequest for " + hosterPersonaId);
				setIsLobbyReserved(lobbyEntityEmpty, true);
				sendJoinEvent(hosterPersonaId, lobbyEntityEmpty, eventId); // Send the join request for race hoster
//				matchmakingBO.changePlayerCountInQueue(false); // Remove the hoster from queue player count
				lobbyEntityEmpty.setLobbyDateTimeStart(new Date());
				lobbyDao.update(lobbyEntityEmpty);
				lobbyCountdownBO.scheduleLobbyStart(lobbyEntityEmpty);
			}
		}
	}

	private boolean isPersonaInside(Long personaId, List<LobbyEntrantEntity> lobbyEntrants) {
		for (LobbyEntrantEntity lobbyEntrantEntity : lobbyEntrants) {
			Long entrantPersonaId = lobbyEntrantEntity.getPersona().getPersonaId();
			if (Objects.equals(entrantPersonaId, personaId)) {
				return true;
			}
		}
		return false;
	}

	private void sendJoinEvent(Long personaId, LobbyEntity lobbyEntity, int eventId) {
		System.out.println("sendJoinEvent for " + personaId);
		Long lobbyId = lobbyEntity.getId();

		XMPP_LobbyInviteType xMPP_LobbyInviteType = new XMPP_LobbyInviteType();
		xMPP_LobbyInviteType.setEventId(eventId);
		xMPP_LobbyInviteType.setLobbyInviteId(lobbyId);

		tokenSessionBO.setSearchEventId(personaId, eventId);
		XmppLobby xmppLobby = new XmppLobby(personaId, openFireSoapBoxCli);
		xmppLobby.sendLobbyInvite(xMPP_LobbyInviteType);
	}

	public LobbyInfo acceptinvite(Long personaId, Long lobbyInviteId) {
		System.out.println("acceptinvite for " + personaId);
		LobbyEntity lobbyEntity = lobbyDao.findById(lobbyInviteId);
		EventEntity eventEntity = lobbyEntity.getEvent();
		
		int eventId = eventEntity.getId();

		LobbyCountdown lobbyCountdown = new LobbyCountdown();
		lobbyCountdown.setLobbyId(lobbyInviteId);
		lobbyCountdown.setEventId(eventId);
		Date lobbyStartTime = new Date();
		if (lobbyEntity.getLobbyDateTimeStart() == null) {
			lobbyEntity.setLobbyDateTimeStart(lobbyStartTime);
			lobbyCountdown.setLobbyCountdownInMilliseconds(getLobbyCountdownInMilliseconds(lobbyStartTime));
		}
		else {
			lobbyCountdown.setLobbyCountdownInMilliseconds(getLobbyCountdownInMilliseconds(lobbyEntity.getLobbyDateTimeStart()));
		}
		lobbyCountdown.setLobbyStuckDurationInMilliseconds(7500);

		ArrayOfLobbyEntrantInfo arrayOfLobbyEntrantInfo = new ArrayOfLobbyEntrantInfo();
		List<LobbyEntrantInfo> lobbyEntrantInfo = arrayOfLobbyEntrantInfo.getLobbyEntrantInfo();

		List<LobbyEntrantEntity> entrants = lobbyEntity.getEntrants();
		sendJoinMsg(personaId, entrants);
		boolean personaInside = false;
		
		matchmakingBO.removePlayerFromQueue(personaId);
		for (LobbyEntrantEntity lobbyEntrantEntity : entrants) {
			Long teamRacerPersona = lobbyEntrantEntity.getPersona().getPersonaId();
			LobbyEntrantInfo LobbyEntrantInfo = new LobbyEntrantInfo();
			LobbyEntrantInfo.setPersonaId(teamRacerPersona);
			LobbyEntrantInfo.setLevel(lobbyEntrantEntity.getPersona().getLevel());
			LobbyEntrantInfo.setGridIndex(lobbyEntrantEntity.getGridIndex());
			lobbyEntrantInfo.add(LobbyEntrantInfo);
			if (teamRacerPersona.equals(personaId)) {
				personaInside = true;
			}
		}
		if (!personaInside) {
			LobbyEntrantEntity lobbyEntrantEntity = new LobbyEntrantEntity();
			PersonaEntity personaEntity = personaDao.findById(personaId);
			lobbyEntrantEntity.setPersona(personaEntity);
			lobbyEntrantEntity.setLobby(lobbyEntity);
			lobbyEntrantEntity.setGridIndex(entrants.size());
			lobbyEntity.getEntrants().add(lobbyEntrantEntity);
			lobbyDao.update(lobbyEntity);
			LobbyEntrantInfo LobbyEntrantInfo = new LobbyEntrantInfo();
			LobbyEntrantInfo.setPersonaId(lobbyEntrantEntity.getPersona().getPersonaId());
			LobbyEntrantInfo.setLevel(lobbyEntrantEntity.getPersona().getLevel());
			LobbyEntrantInfo.setGridIndex(lobbyEntrantEntity.getGridIndex());
			lobbyEntrantInfo.add(LobbyEntrantInfo);
		}
		LobbyInfo lobbyInfoType = new LobbyInfo();
		lobbyInfoType.setCountdown(lobbyCountdown);
		lobbyInfoType.setEntrants(arrayOfLobbyEntrantInfo);
		lobbyInfoType.setEventId(eventId);
		lobbyInfoType.setLobbyInviteId(lobbyInviteId);
		lobbyInfoType.setLobbyId(lobbyInviteId);
	
		teamRacingInit(personaId, lobbyEntity, entrants); // Team Racing actions
		
		return lobbyInfoType;
	}

	public void sendJoinMsg(Long personaId, List<LobbyEntrantEntity> lobbyEntrants) {
		for (LobbyEntrantEntity lobbyEntrantEntity : lobbyEntrants) {
			LobbyEntrantAdded lobbyEntrantAdded = new LobbyEntrantAdded();
			if (!Objects.equals(personaId, lobbyEntrantEntity.getPersona().getPersonaId())) {
				lobbyEntrantAdded.setHeat(1);
				lobbyEntrantAdded.setLevel(lobbyEntrantEntity.getPersona().getLevel());
				lobbyEntrantAdded.setPersonaId(personaId);
				lobbyEntrantAdded.setLobbyId(lobbyEntrantEntity.getLobby().getId());
				XmppLobby xmppLobby = new XmppLobby(lobbyEntrantEntity.getPersona().getPersonaId(), openFireSoapBoxCli);
				xmppLobby.sendJoinMsg(lobbyEntrantAdded);
			}
		}
	}

	public void deleteLobbyEntrant(Long personaId, Long lobbyId) {
		if (lobbyId != 0L) {
			PersonaEntity personaEntity = personaDao.findById(personaId);
			lobbyEntrantDao.deleteByPersona(personaEntity);
			updateLobby(personaId, lobbyId);
		}
	}

	private void updateLobby(Long personaId, Long lobbyId) {
		LobbyEntity lobbyEntity = lobbyDao.findById(lobbyId);
		if (lobbyEntity == null) {
			return;
		}
		List<LobbyEntrantEntity> listLobbyEntrantEntity = lobbyEntity.getEntrants();
		for (LobbyEntrantEntity entity : listLobbyEntrantEntity) {
			LobbyEntrantRemoved lobbyEntrantRemoved = new LobbyEntrantRemoved();
			if (!Objects.equals(entity.getPersona().getPersonaId(), personaId)) {
				lobbyEntrantRemoved.setPersonaId(personaId);
				lobbyEntrantRemoved.setLobbyId(lobbyId);
				XmppLobby xmppLobby = new XmppLobby(entity.getPersona().getPersonaId(), openFireSoapBoxCli);
				xmppLobby.sendExitMsg(lobbyEntrantRemoved);
			}
		}
	}
	
	public void teamRacingInit(Long personaId, LobbyEntity lobbyEntity, List<LobbyEntrantEntity> entrants) {
		// 2 teams can be inside of one race - Hypercycle
		// FIXME team can't exit - no 'exit' event for team
		// Team Racing is for only Sprints & Circuits
		int eventMode = lobbyEntity.getEvent().getEventModeId();
		if (eventMode == 4 || eventMode == 9) {
			Long teamRacerPersona = personaId;
			PersonaEntity personaEntityRacer = personaDao.findById(teamRacerPersona);
			TeamsEntity racerTeamEntity = personaEntityRacer.getTeam();
			if (racerTeamEntity != null && racerTeamEntity.getActive() && parameterBO.getIntParam("TEAM_CURRENTSEASON") > 0
					&& !lobbyEntity.getIsPrivate()) { // "Season" 0 means there is no active team racing
				int serverCarClass = parameterBO.getIntParam("CLASSBONUS_CARCLASSHASH");
				CustomCarTrans customCar = personaBO.getDefaultCar(personaId).getCustomCar();
				int playerCarHash = customCar.getPhysicsProfileHash();
				int playerCarClass = customCar.getCarClassHash();
				if (serverCarClass == playerCarClass || serverCarClass == 0) { // carClass 0 = open races for all classes
					if (!teamsBO.isPlayerCarAllowed(playerCarHash)) { // Player car-model check
						openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Your car model is not suitable for the current Team Racing Season." +
								"\n## Check out the allowed car-list in our Game Guide."), teamRacerPersona);
					}
					else {
						teamsBO.teamRacingLobbyInit(lobbyEntity, racerTeamEntity, teamRacerPersona, entrants);
					}
				}
			}
		}
	}
	
	public int getLobbyCountdownInMilliseconds(Date lobbyDateTimeStart) {
		Long time = 0L;
		if (lobbyDateTimeStart != null) {
			Date now = new Date();
			time = now.getTime() - lobbyDateTimeStart.getTime();
			time = parameterBO.getIntParam("LOBBY_TIME").longValue() - time;
			return time.intValue();
		}
		return time.intValue();
	}
}
