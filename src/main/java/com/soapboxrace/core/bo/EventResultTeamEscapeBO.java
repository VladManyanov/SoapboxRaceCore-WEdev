package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.CustomCarDAO;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventMissionsDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventMissionsEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppEvent;
import com.soapboxrace.jaxb.http.Accolades;
import com.soapboxrace.jaxb.http.ArrayOfTeamEscapeEntrantResult;
import com.soapboxrace.jaxb.http.TeamEscapeArbitrationPacket;
import com.soapboxrace.jaxb.http.TeamEscapeEntrantResult;
import com.soapboxrace.jaxb.http.TeamEscapeEventResult;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeTeamEscapeEntrantResult;
import com.soapboxrace.jaxb.xmpp.XMPP_TeamEscapeEntrantResultType;

@Stateless
public class EventResultTeamEscapeBO {

	@EJB
	private EventSessionDAO eventSessionDao;

	@EJB
	private EventDataDAO eventDataDao;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private RewardTeamEscapeBO rewardTeamEscapeBO;

	@EJB
	private CarDamageBO carDamageBO;

	@EJB
	private AchievementsBO achievementsBO;

	@EJB
	private PersonaDAO personaDAO;
	
	@EJB
	private EventDAO eventDAO;
	
	@EJB
	private EventResultBO eventResultBO;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private CustomCarDAO customCarDAO;
	
	@EJB
	private EventMissionsDAO eventMissionsDAO;
	
	@EJB
	private EventMissionsBO eventMissionsBO;
	
	@EJB
	private EventBO eventBO;
	
	@EJB
	private ParameterBO parameterBO;

	public TeamEscapeEventResult handleTeamEscapeEnd(EventSessionEntity eventSessionEntity, Long activePersonaId,
			TeamEscapeArbitrationPacket teamEscapeArbitrationPacket, Long eventEnded) {
		Long eventSessionId = eventSessionEntity.getId();
		eventSessionEntity.setEnded(System.currentTimeMillis());

		eventSessionDao.update(eventSessionEntity);

		XMPP_TeamEscapeEntrantResultType xmppTeamEscapeResult = new XMPP_TeamEscapeEntrantResultType();
		xmppTeamEscapeResult.setEventDurationInMilliseconds(teamEscapeArbitrationPacket.getEventDurationInMilliseconds());
		xmppTeamEscapeResult.setEventSessionId(eventSessionId);
		xmppTeamEscapeResult.setFinishReason(teamEscapeArbitrationPacket.getFinishReason());
		xmppTeamEscapeResult.setPersonaId(activePersonaId);

		XMPP_ResponseTypeTeamEscapeEntrantResult teamEscapeEntrantResultResponse = new XMPP_ResponseTypeTeamEscapeEntrantResult();
		teamEscapeEntrantResultResponse.setTeamEscapeEntrantResult(xmppTeamEscapeResult);

		PersonaEntity personaEntity = personaDAO.findById(activePersonaId);
		String playerName = personaEntity.getName();
		EventMissionsEntity eventMissionsEntity = eventMissionsDAO.getEventMission(eventSessionEntity.getEvent());
		boolean isMission = eventMissionsEntity != null ? true : false;

		EventDataEntity eventDataEntity = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
		// XKAYA's arbitration exploit fix
		boolean arbitStatus = eventDataEntity.getArbitration();
		if (arbitStatus) {
			System.out.println("WARINING - XKAYA's arbitration exploit attempt, driver: " + playerName);
			return null;
		}
		eventDataEntity.setArbitration(arbitStatus ? false : true);
		int currentEventId = eventDataEntity.getEvent().getId();
		int finishReason = teamEscapeArbitrationPacket.getFinishReason();
		if (finishReason == 22) { // Proceed with achievements only when finish is proper
			achievementsBO.applyAirTimeAchievement(teamEscapeArbitrationPacket, personaEntity);
			achievementsBO.applyPursuitCostToState(teamEscapeArbitrationPacket, personaEntity);
			achievementsBO.applyTeamEscape(teamEscapeArbitrationPacket, personaEntity);
			achievementsBO.applyEventKmsAchievement(personaEntity, (long) eventDataEntity.getEvent().getTrackLength());
		}
		eventDataEntity.setServerEventDuration(eventEnded - eventDataEntity.getServerEventDuration());
		eventDataEntity.setAlternateEventDurationInMilliseconds(teamEscapeArbitrationPacket.getAlternateEventDurationInMilliseconds());
		eventDataEntity.setBustedCount(teamEscapeArbitrationPacket.getBustedCount());
		eventDataEntity.setCarId(teamEscapeArbitrationPacket.getCarId());
		eventDataEntity.setCopsDeployed(teamEscapeArbitrationPacket.getCopsDeployed());
		eventDataEntity.setCopsDisabled(teamEscapeArbitrationPacket.getCopsDisabled());
		eventDataEntity.setCopsRammed(teamEscapeArbitrationPacket.getCopsRammed());
		eventDataEntity.setCostToState(teamEscapeArbitrationPacket.getCostToState());
		eventDataEntity.setDistanceToFinish(teamEscapeArbitrationPacket.getDistanceToFinish());
		eventDataEntity.setEventDurationInMilliseconds(teamEscapeArbitrationPacket.getEventDurationInMilliseconds());
		eventDataEntity.setEventModeId(eventDataEntity.getEvent().getEventModeId());
		eventDataEntity.setFinishReason(teamEscapeArbitrationPacket.getFinishReason());
		eventDataEntity.setFractionCompleted(teamEscapeArbitrationPacket.getFractionCompleted());
		eventDataEntity.setHacksDetected(teamEscapeArbitrationPacket.getHacksDetected());
		eventDataEntity.setInfractions(teamEscapeArbitrationPacket.getInfractions());
		eventDataEntity.setLongestJumpDurationInMilliseconds(teamEscapeArbitrationPacket.getLongestJumpDurationInMilliseconds());
		eventDataEntity.setNumberOfCollisions(teamEscapeArbitrationPacket.getNumberOfCollisions());
		eventDataEntity.setPerfectStart(teamEscapeArbitrationPacket.getPerfectStart());
		eventDataEntity.setRank(teamEscapeArbitrationPacket.getRank());
		eventDataEntity.setPersonaId(activePersonaId);
		eventDataEntity.setRoadBlocksDodged(teamEscapeArbitrationPacket.getRoadBlocksDodged());
		eventDataEntity.setSpikeStripsDodged(teamEscapeArbitrationPacket.getSpikeStripsDodged());
		eventDataEntity.setSumOfJumpsDurationInMilliseconds(teamEscapeArbitrationPacket.getSumOfJumpsDurationInMilliseconds());
		eventDataEntity.setTopSpeed(teamEscapeArbitrationPacket.getTopSpeed());
		eventDataEntity.setAvgSpeed(teamEscapeArbitrationPacket.getPhysicsMetrics().getSpeedAverage());
		
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
		
		ArrayOfTeamEscapeEntrantResult arrayOfTeamEscapeEntrantResult = new ArrayOfTeamEscapeEntrantResult();
		EventEntity eventEntity = eventDAO.findById(currentEventId);
		// +1 to play count for this track, MP
		if (eventDataEntity.getRank() == 1 && arrayOfTeamEscapeEntrantResult.getTeamEscapeEntrantResult().size() > 1) {
			eventEntity.setFinishCount(eventEntity.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity);
			personaDAO.update(personaEntity);
		}
		// +1 to play count for this track, SP (No default SP TEs)
		if (arrayOfTeamEscapeEntrantResult.getTeamEscapeEntrantResult().size() < 2) {
			eventEntity.setFinishCount(eventEntity.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity);
			personaDAO.update(personaEntity);
			EventDataEntity eventDataEntitySP = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
			eventDataEntitySP.setIsSingle(true);
			eventDataDao.update(eventDataEntitySP);
		}
		int carclasshash = eventEntity.getCarClassHash();
		boolean isDNFActive = parameterBO.getBoolParam("DNF_ENABLED");
		if (carclasshash == 607077938) {
			isDNFActive = false; // Don't use DNF timeout on open-class racing
		}
		boolean oneGetAway = false;
		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
			TeamEscapeEntrantResult teamEscapeEntrantResult = new TeamEscapeEntrantResult();
			teamEscapeEntrantResult.setDistanceToFinish(racer.getDistanceToFinish());
			teamEscapeEntrantResult.setEventDurationInMilliseconds(racer.getEventDurationInMilliseconds());
			teamEscapeEntrantResult.setEventSessionId(eventSessionId);
			teamEscapeEntrantResult.setFinishReason(racer.getFinishReason());
			if (racer.getFinishReason() == 22) {
				oneGetAway = true;
			}
			teamEscapeEntrantResult.setFractionCompleted(racer.getFractionCompleted());
			teamEscapeEntrantResult.setPersonaId(racer.getPersonaId());
			teamEscapeEntrantResult.setRanking(racer.getRank());
			arrayOfTeamEscapeEntrantResult.getTeamEscapeEntrantResult().add(teamEscapeEntrantResult);

			if (!racer.getPersonaId().equals(activePersonaId)) {
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendTeamEscapeEntrantInfo(teamEscapeEntrantResultResponse); // Restart the Team Escape timeout timer
			}
			if (isDNFActive && racer.getFinishReason() != 266 && teamEscapeArbitrationPacket.getRank() == 1) { // FIXME can be executed twice with the sync finish place issues
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendTeamEscapeEntrantInfo(teamEscapeEntrantResultResponse); // Restart the Team Escape timeout timer
				xmppEvent.sendEventTimingOut(eventSessionId);
				eventResultBO.timeLimitTimer(eventSessionId, (long) 60000); // Default timeout time is 60 seconds
			}
		}
		if (oneGetAway && teamEscapeArbitrationPacket.getRank() == eventDataDao.getRacers(eventSessionId).size()) {
			for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
				Long personaId = racer.getPersonaId();
				PersonaEntity personaEntityGetAway = personaDAO.findById(personaId);
				achievementsBO.applyTeamEscapeGetAway(personaEntityGetAway);
			}
		}

		TeamEscapeEventResult teamEscapeEventResult = new TeamEscapeEventResult();
		if (isMission) {
			boolean isDone = eventMissionsBO.getEventMissionAccolades(eventEntity, eventMissionsEntity, activePersonaId, teamEscapeArbitrationPacket, finishReason);
			if (isDone) {
				teamEscapeEventResult.setAccolades(rewardTeamEscapeBO.getTeamEscapeAccolades(activePersonaId, teamEscapeArbitrationPacket, eventSessionEntity, 5, true));
			}
			else {
				teamEscapeEventResult.setAccolades(new Accolades());
			}
		}
		if (!isMission) {
			if (finishReason == 22 && arrayOfTeamEscapeEntrantResult.getTeamEscapeEntrantResult().size() > 1) {
				teamEscapeEventResult.setAccolades(rewardTeamEscapeBO.getTeamEscapeAccolades(activePersonaId, teamEscapeArbitrationPacket, eventSessionEntity, 1, false));
			}
			else { // No rewards on timeout
				teamEscapeEventResult.setAccolades(new Accolades());
			}
		}
		teamEscapeEventResult.setDurability(carDamageBO.updateDamageCar(activePersonaId, teamEscapeArbitrationPacket, teamEscapeArbitrationPacket.getNumberOfCollisions()));
		teamEscapeEventResult.setEntrants(arrayOfTeamEscapeEntrantResult);
		teamEscapeEventResult.setEventId(currentEventId);
		eventResultBO.defineFinishLobby(teamEscapeEventResult, eventSessionEntity, personaEntity.getRaceAgain());
		return teamEscapeEventResult;
	}
}
