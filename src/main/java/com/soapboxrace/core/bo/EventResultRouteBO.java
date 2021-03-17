package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.CarBrandsList;
import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.bo.util.EventModeType;
import com.soapboxrace.core.bo.util.StringListConverter;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.CustomCarDAO;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventMissionsDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventMissionsEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.Accolades;
import com.soapboxrace.jaxb.http.ArbitrationPacket;
import com.soapboxrace.jaxb.http.ArrayOfRouteEntrantResult;
import com.soapboxrace.jaxb.http.ExitPath;
import com.soapboxrace.jaxb.http.RouteArbitrationPacket;
import com.soapboxrace.jaxb.http.RouteEntrantResult;
import com.soapboxrace.jaxb.http.RouteEventResult;

@Stateless
public class EventResultRouteBO {

	@EJB
	private EventSessionDAO eventSessionDao;

	@EJB
	private EventDataDAO eventDataDao;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private RewardRouteBO rewardRouteBO;

	@EJB
	private CarDamageBO carDamageBO;

	@EJB
	private AchievementsBO achievementsBO;

	@EJB
	private PersonaDAO personaDAO;
	
	@EJB
	private TeamsDAO teamsDAO;
	
	@EJB
	private TeamsBO teamsBo;
	
	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private EventDAO eventDAO;
	
	@EJB
	private EventResultBO eventResultBO;
	
	@EJB
	private CustomCarDAO customCarDAO;
	
	@EJB
	private EventBO eventBO;
	
	@EJB
	private EventMissionsDAO eventMissionsDAO;
	
	@EJB
	private EventMissionsBO eventMissionsBO;
	
	@EJB
	private LegitRaceBO legitRaceBO;
	
	@EJB
	private CarBrandsList carBrandsList;
	
	@EJB
	private StringListConverter stringListConverter;
	
	@EJB
	private CarClassesDAO carClassesDAO;

	public RouteEventResult handleRaceEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, RouteArbitrationPacket routeArbitrationPacket, Long eventEnded) {
		Long eventSessionId = eventSessionEntity.getId();
		eventSessionEntity.setEnded(System.currentTimeMillis());
		if (eventSessionEntity.getEnded() == null) {
		    System.out.println("DEBUG some event ended with no ended time, id: " + eventSessionId);
		}
		EventDataEntity eventDataEntity = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
		EventEntity eventEntity = eventDataEntity.getEvent();
		int eventClass = eventEntity.getCarClassHash();
		PersonaEntity personaEntity = personaDAO.findById(activePersonaId);
		String playerName = personaEntity.getName();
		int playerRank = routeArbitrationPacket.getRank();
		
		EventMissionsEntity eventMissionsEntity = eventMissionsDAO.getEventMission(eventEntity);
		boolean isMission = eventMissionsEntity != null ? true : false;
		
		boolean isInterceptorEvent = EventModeType.INTERCEPTOR.getId() == eventEntity.getEventModeId() ? true : false;
		Long isWinnerPresented = eventSessionEntity.getPersonaWinner();
		boolean isCopsFailed = eventSessionEntity.isCopsFailed(); // Interceptor events only
		
		Long team1id = eventSessionEntity.getTeam1Id();
		Long team2id = eventSessionEntity.getTeam2Id();
		boolean preRegTeams = false;
		if (team1id != null && team2id != null) {
			preRegTeams = true;
		}
		// XKAYA's arbitration exploit fix
		boolean arbitStatus = eventDataEntity.getArbitration();
		if (arbitStatus) {
			System.out.println("WARINING - XKAYA's arbitration exploit attempt, driver: " + playerName);
			return null;
		}
		eventDataEntity.setArbitration(arbitStatus ? false : true);
		int finishReason = routeArbitrationPacket.getFinishReason();
		if (finishReason == 22) { // Proceed with achievements only when finish is proper
			achievementsBO.applyRaceAchievements(eventDataEntity, routeArbitrationPacket, personaEntity);
			achievementsBO.applyAirTimeAchievement(routeArbitrationPacket, personaEntity);
			achievementsBO.applyEventKmsAchievement(personaEntity, (long) eventEntity.getTrackLength());
		}
		
		eventDataEntity.setServerEventDuration(eventEnded - eventDataEntity.getServerEventDuration());
		updateEventDataEntity(eventDataEntity, routeArbitrationPacket);

		// RouteArbitrationPacket
		eventDataEntity.setBestLapDurationInMilliseconds(routeArbitrationPacket.getBestLapDurationInMilliseconds());
		eventDataEntity.setFractionCompleted(routeArbitrationPacket.getFractionCompleted());
		eventDataEntity.setLongestJumpDurationInMilliseconds(routeArbitrationPacket.getLongestJumpDurationInMilliseconds());
		eventDataEntity.setNumberOfCollisions(routeArbitrationPacket.getNumberOfCollisions());
		eventDataEntity.setPerfectStart(routeArbitrationPacket.getPerfectStart());
		eventDataEntity.setSumOfJumpsDurationInMilliseconds(routeArbitrationPacket.getSumOfJumpsDurationInMilliseconds());
		eventDataEntity.setTopSpeed(routeArbitrationPacket.getTopSpeed());
		eventDataEntity.setAvgSpeed(routeArbitrationPacket.getPhysicsMetrics().getSpeedAverage());

		eventDataEntity.setEventModeId(eventEntity.getEventModeId());
		eventDataEntity.setPersonaId(activePersonaId);
		boolean speedBugChance = eventResultBO.speedBugChance(personaEntity.getUser().getLastLogin());
		eventDataEntity.setSpeedBugChance(speedBugChance);
		int carVersion = eventResultBO.carVersionCheck(activePersonaId);
		eventDataEntity.setCarVersion(carVersion);
		eventDataDao.update(eventDataEntity);
		
		CustomCarEntity customCarEntity = customCarDAO.findById(eventDataEntity.getCarId());
		int carPhysicsHash = customCarEntity.getPhysicsProfileHash();
		if (carPhysicsHash == 202813212 || carPhysicsHash == -840317713 || carPhysicsHash == -845093474 || carPhysicsHash == -133221572 || carPhysicsHash == -409661256) {
			// Player on ModCar cannot finish any event (since he is restricted from), but if he somehow was finished it, we should know
			System.out.println("Player " + playerName + "has illegally finished the event on ModCar.");
			String message = ":heavy_minus_sign:"
	        		+ "\n:japanese_goblin: **|** Nгрок **" + playerName + "** участвовал в гонках на **моддерском слоте**, покончите с ним."
	        		+ "\n:japanese_goblin: **|** Player **" + playerName + "** was finished the event on **modder vehicle**, finish him.";
			discordBot.sendMessage(message);
		}
		Long eventDataId = eventDataEntity.getId();
		eventBO.updateEventCarInfo(activePersonaId, eventDataId, customCarEntity);

		ArrayOfRouteEntrantResult arrayOfRouteEntrantResult = new ArrayOfRouteEntrantResult();
		
		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
			RouteEntrantResult routeEntrantResult = new RouteEntrantResult();
			routeEntrantResult.setBestLapDurationInMilliseconds(racer.getBestLapDurationInMilliseconds());
			routeEntrantResult.setEventDurationInMilliseconds(racer.getEventDurationInMilliseconds());
			routeEntrantResult.setEventSessionId(eventSessionId);
			routeEntrantResult.setFinishReason(racer.getFinishReason());
			routeEntrantResult.setPersonaId(racer.getPersonaId());
			routeEntrantResult.setRanking(racer.getRank());
			routeEntrantResult.setTopSpeed(racer.getTopSpeed());
			// Does both teams are in actual race? This is checking a racers for their teamIds
			TeamsEntity teamsEntityTest = personaDAO.findById(racer.getPersonaId()).getTeam();
			if (teamsEntityTest != null) {
				Long playerTeamIdCheck = teamsEntityTest.getTeamId();
				if (preRegTeams && playerTeamIdCheck != null) {
					if (!eventSessionEntity.getTeam1Check() && team1id == playerTeamIdCheck) {
						eventSessionEntity.setTeam1Check(true);
						eventSessionDao.update(eventSessionEntity);
					}
					if (!eventSessionEntity.getTeam2Check() && team2id == playerTeamIdCheck) {
						eventSessionEntity.setTeam2Check(true);
						eventSessionDao.update(eventSessionEntity);
					}
				}
			}
			arrayOfRouteEntrantResult.getRouteEntrantResult().add(routeEntrantResult);
		}

		RouteEventResult routeEventResult = new RouteEventResult();
		int isDropableMode = 1;
		// Give rare drop if it's a online class-restricted race
		if (eventClass != 607077938 && arrayOfRouteEntrantResult.getRouteEntrantResult().size() >= 2) {
			isDropableMode = 2;
		}
		
		routeEventResult.setDurability(carDamageBO.updateDamageCar(activePersonaId, routeArbitrationPacket, routeArbitrationPacket.getNumberOfCollisions()));
		routeEventResult.setEntrants(arrayOfRouteEntrantResult);
		int currentEventId = eventEntity.getId();
		routeEventResult.setEventId(currentEventId);
		int tournamentEventId = parameterBO.getIntParam("TOURNAMENT_EVENTID");
		Long personaId = personaEntity.getPersonaId();
		if (currentEventId == tournamentEventId && !speedBugChance) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Event Session: " + eventSessionId), personaId);
		}
		if (currentEventId == tournamentEventId && speedBugChance) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This event can be affected by SpeedBug, restart the game."), personaId);
		}
		
		eventResultBO.defineFinishLobby(routeEventResult, eventSessionEntity);
		// eventResultBO.physicsMetricsInfoDebug(routeArbitrationPacket);
		int carclasshash = eventEntity.getCarClassHash();
		boolean isDNFActive = parameterBO.getBoolParam("DNF_ENABLED");
		if (carclasshash == 607077938) {
			isDNFActive = false; // Don't use DNF timeout on open-class racing
		}
		eventBO.sendXmppPacketRoute(eventSessionId, activePersonaId, routeArbitrationPacket, playerRank, isDNFActive, true);
		
		EventEntity eventEntity2 = eventDAO.findById(currentEventId);
		boolean isSingle = false;
		// +1 to play count for this track, MP
		if (eventDataEntity.getRank() == 1 && arrayOfRouteEntrantResult.getRouteEntrantResult().size() > 1) {
			eventEntity2.setFinishCount(eventEntity2.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity2);
			personaDAO.update(personaEntity);
		}
		// +1 to play count for this track, SP
		if (arrayOfRouteEntrantResult.getRouteEntrantResult().size() < 2) {
			eventEntity2.setFinishCount(eventEntity2.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity2);
			personaDAO.update(personaEntity);
			EventDataEntity eventDataEntitySP = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
			isSingle = true;
			eventDataEntitySP.setIsSingle(isSingle);
			eventDataDao.update(eventDataEntitySP);
		}
		if (playerRank < 4 && !isSingle) {
			String brandName = carClassesDAO.findByHash(carPhysicsHash).getManufactor();
			if (!brandName.contentEquals("AI") && !brandName.contentEquals("TRAFFIC")) {
				achievementsBO.applyBrandsAchievements(personaEntity, carBrandsList.getBrandInfo(carPhysicsHash, activePersonaId));
			}
		}
		// Check race record
		legitRaceBO.isRecordVaildRoute(routeArbitrationPacket, eventDataEntity, customCarEntity, isInterceptorEvent, speedBugChance, personaEntity, eventEntity);
		
		// Initiate the final team action check, only if both teams are registered for event
		// FIXME If the players "fast enough", this sequence will be executed more than 1 time, since PersonaWinner will be null for multiple players
		if (isWinnerPresented == null) {
			isWinnerPresented = activePersonaId;
			eventSessionEntity.setPersonaWinner(activePersonaId);
			if (preRegTeams) {
				// System.out.println("### TEAMS: EventSession " + eventSessionId + "has been completed, check");
				// openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Debug - Teams finish, init, " + eventSessionId), personaId);
				new Thread(new Runnable() {
					@Override
					public void run() {
						// System.out.println("### TEAMS: EventSession " + eventSessionId + "has been completed, init");
						teamsBo.teamAccoladesBasic(eventSessionId);
					}
				}).start();
				}
		}
		if (isInterceptorEvent) {
			interceptorAccolades(eventSessionEntity, activePersonaId, finishReason, isCopsFailed, playerName, routeEventResult, routeArbitrationPacket, 
					arrayOfRouteEntrantResult, isDropableMode);
		}
		
		if (isMission) {
			boolean isDone = eventMissionsBO.getEventMissionAccolades(eventEntity, eventMissionsEntity, activePersonaId, routeArbitrationPacket, finishReason);
			if (isDone) {
				routeEventResult.setAccolades(rewardRouteBO.getRouteAccolades(activePersonaId, routeArbitrationPacket, eventSessionEntity, arrayOfRouteEntrantResult, 5, true));
			}
			else {
				routeEventResult.setAccolades(new Accolades());
			}
		} // Normal rewards mode
		if (!isMission && !isInterceptorEvent && finishReason == 22) {
			routeEventResult.setAccolades(rewardRouteBO.getRouteAccolades(activePersonaId, routeArbitrationPacket, eventSessionEntity, arrayOfRouteEntrantResult, isDropableMode, false));
		}
		eventSessionDao.update(eventSessionEntity);
		return routeEventResult;
	}

	private void updateEventDataEntity(EventDataEntity eventDataEntity, ArbitrationPacket arbitrationPacket) {
		eventDataEntity.setAlternateEventDurationInMilliseconds(arbitrationPacket.getAlternateEventDurationInMilliseconds());
		eventDataEntity.setCarId(arbitrationPacket.getCarId());
		eventDataEntity.setEventDurationInMilliseconds(arbitrationPacket.getEventDurationInMilliseconds());
		eventDataEntity.setFinishReason(arbitrationPacket.getFinishReason());
		eventDataEntity.setHacksDetected(arbitrationPacket.getHacksDetected());
		eventDataEntity.setRank(arbitrationPacket.getRank());
	}
	
	private void interceptorAccolades (EventSessionEntity eventSessionEntity, Long activePersonaId, int finishReason, boolean isCopsFailed,
			String playerName, RouteEventResult routeEventResult, RouteArbitrationPacket routeArbitrationPacket, ArrayOfRouteEntrantResult arrayOfRouteEntrantResult,
			int isDropableMode) {
		Long eventSessionId = eventSessionEntity.getId();
		boolean isRacer = true;
		String[] personaCopsStr = eventSessionEntity.getPersonaCops().split(",");
		String[] personaRacersStr = eventSessionEntity.getPersonaRacers().split(",");
		Long[] personaCopsList = stringListConverter.StrToLongList(personaCopsStr);
		Long[] personaRacersList = stringListConverter.StrToLongList(personaRacersStr);
		for (Long personaCop : personaCopsList) {
			// System.out.println("Cop ID " + personaCop);
			if (personaCop.equals(activePersonaId)) {
				// System.out.println("Cop: " + playerName);
				isRacer = false;
			}
		}
		for (Long personaRacer : personaRacersList) {
			// System.out.println("Racer ID " + personaRacer);
			if (personaRacer.equals(activePersonaId)) {
				// System.out.println("Racer: " + playerName);
				isRacer = true;
			}
		} 
		if ((!isRacer && finishReason == 22) || (isRacer && finishReason == 16394)) {
			System.out.println("No rewards to isRacer " + isRacer + " " + playerName + ", session " + eventSessionId);
			routeEventResult.setAccolades(new Accolades()); // No rewards (Cop on finish or Racer on timeout)
		}
		if (isRacer && finishReason == 22) { // Rewards will be given to racers
			if (!isCopsFailed) { // If any racer has completed the route, cops has failed
				isCopsFailed = true;
				eventSessionEntity.setIsCopsFailed(isCopsFailed);
			}
			System.out.println("Rewards given to isRacer " + isRacer + " " + playerName + ", session " + eventSessionId);
			routeEventResult.setAccolades(rewardRouteBO.getRouteAccolades(activePersonaId, routeArbitrationPacket, eventSessionEntity, arrayOfRouteEntrantResult, isDropableMode, false)); 
		}
		if (!isRacer && finishReason == 16394 && !isCopsFailed) { // Rewards will be given to cops
			System.out.println("Rewards given to isRacer " + isRacer + " " + playerName + ", session " + eventSessionId);
			routeEventResult.setAccolades(rewardRouteBO.getRouteAccolades(activePersonaId, routeArbitrationPacket, eventSessionEntity, arrayOfRouteEntrantResult, isDropableMode, false)); 
		}
	}
}
