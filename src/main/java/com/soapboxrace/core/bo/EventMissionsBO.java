package com.soapboxrace.core.bo;

import java.time.LocalDate;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.TimeReadConverter;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventMissionsDAO;
import com.soapboxrace.core.dao.ParameterDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventMissionsEntity;
import com.soapboxrace.core.jpa.ParameterEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.jaxb.http.ArbitrationPacket;

@Stateless
public class EventMissionsBO {
	
	@EJB
	private PersonaDAO personaDao;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private EventMissionsDAO eventMissionsDAO;
	
	@EJB
	private EventDAO eventDAO;
	
	@EJB
	private AchievementsBO achievementsBO;
	
	@EJB
	private ParameterDAO parameterDAO;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private EventBO eventBO;
	
	@EJB
	private TokenSessionBO tokenSessionBO;
	
	@EJB
	private TimeReadConverter timeReadConverter;

	public void getEventMissionInfo(EventEntity eventEntity, Long activePersonaId) {
		EventMissionsEntity eventMissionsEntity = eventMissionsDAO.getEventMission(eventEntity);
		PersonaEntity personaEntity = personaDao.findById(activePersonaId);
		boolean seqCSeries = personaEntity.getUser().getIsSeqDailySeries();
		LocalDate dailyRaceDate = personaEntity.getDailyRaceDate();
		if (eventMissionsEntity != null) {
			String eventType = eventMissionsEntity.getEventType();
			String msgTarget = "!!!";
			String message = "!pls fix!";
			String msgMode = "MISSIONMODE";
			
			Long timeLimit = eventEntity.getTimeLimit();
			float speedGoal = eventMissionsEntity.getAvgSpeed();
			LocalDate curDate = LocalDate.now();
			switch (eventType) {
			case "TimeAttack":
				msgTarget = timeReadConverter.convertRecord(timeLimit);
				message = msgTarget;
				break;
			case "Race":
				message = "TXT_WEV3_BASEANNOUNCER_RACE_GOAL";
				break;
			case "Escort":
				message = "TXT_WEV3_BASEANNOUNCER_ESCORT_GOAL";
				break;
			case "Speedtrap":
				msgMode = "MISSIONMODE_SPEEDTRAP";
				msgTarget = Math.round(speedGoal) + " KM/H";
				message = msgTarget;
				break;
			}
			achievementsBO.broadcastUICustom(activePersonaId, message, msgMode, 5);
			// Daily Race's reward can be given only once per day
			if (!seqCSeries && dailyRaceDate != null && dailyRaceDate.equals(curDate)) { 
				String messageNoReward = "TXT_WEV3_BASEANNOUNCER_MISSION_REPLAY";
				achievementsBO.broadcastUICustom(activePersonaId, messageNoReward, "MISSIONMODE", 3);
			}
		}
	}
	
	public boolean getEventMissionAccolades(EventEntity eventEntity, EventMissionsEntity eventMissionsEntity, Long activePersonaId,
			ArbitrationPacket arbitrationPacket, int finishReason) {
		PersonaEntity personaEntity = personaDao.findById(activePersonaId);
		boolean seqCSeries = personaEntity.getUser().getIsSeqDailySeries();
		LocalDate dailyRaceDate = personaEntity.getDailyRaceDate();
		String eventType = eventMissionsEntity.getEventType();
		String message = "TXT_WEV3_BASEANNOUNCER_MISSIONRESULT_FAIL";
		String msgType = "MISSIONRESULTMODE";
		
		Long playerTime = arbitrationPacket.getEventDurationInMilliseconds();
		int playerRank = arbitrationPacket.getRank();
		float playerAvgSpeed = arbitrationPacket.getPhysicsMetrics().getSpeedAverage();
				
		Long timeLimit = eventEntity.getTimeLimit();
		float speedTarget = eventMissionsEntity.getAvgSpeed();
		LocalDate curDate = LocalDate.now();
		boolean isDone = false;
		switch (eventType) {
		case "TimeAttack":
			if (finishReason == 22 && playerTime < timeLimit) {
				isDone = true;
				message = "TXT_WEV3_BASEANNOUNCER_MISSIONRESULT_WIN";
			}
			break;
		case "Race":
			if (finishReason == 22 && playerRank == 1) {
				isDone = true;
				message = "TXT_WEV3_BASEANNOUNCER_MISSIONRESULT_WIN";
			}
			break;
		case "Escort":
			if (finishReason == 22 && playerRank == 2 && playerTime < timeLimit) {
				isDone = true;
				message = "TXT_WEV3_BASEANNOUNCER_MISSIONRESULT_WIN";
			}
			break;
		case "Speedtrap":
			long playerSpeed = Math.round(playerAvgSpeed * 3.6);
			message = playerSpeed + " KM/H";
			if (finishReason == 22 && playerSpeed >= speedTarget) {
				isDone = true;
				msgType = "MISSION_AVGSPEEDDONE_RESULTMODE";
			}
			else {
				msgType = "MISSION_AVGSPEEDFAIL_RESULTMODE";
			}
			break;
		}
		if (isDone && (seqCSeries || dailyRaceDate == null || !dailyRaceDate.equals(curDate))) {
			personaEntity.setDailyRaceDate(curDate);
			if (seqCSeries) { // Sequential mode
				int seqCSCurrentEvent = personaEntity.getSeqCSCurrentEvent();
				int[] dailySeriesIntArray = eventBO.getDailySeriesArray();
				if (seqCSCurrentEvent < (dailySeriesIntArray.length - 1)) {
					personaEntity.setSeqCSCurrentEvent(seqCSCurrentEvent + 1);
				}
				else {
					personaEntity.setSeqCSCurrentEvent(0); // Reset the Daily Series sequence
				}
			}
			personaDao.update(personaEntity);
			achievementsBO.applyDailySeries(personaEntity, eventEntity.getId());
		}
		else {
			if (!seqCSeries) {
				isDone = false; // No Rewards, since it's a replay
			}
		}
		achievementsBO.broadcastUICustom(activePersonaId, message, msgType, 5);
		return isDone;
	}
	
	// Daily challenge races rotation
	// Array contains the list of eventIds
	@Schedule(dayOfWeek = "*", persistent = false)
	public String dailySeriesRotation() {
		if (parameterBO.getBoolParam("DAILYSERIES_ROTATION")) {
			ParameterEntity parameterEntity = parameterDAO.findById("DAILYSERIES_CURRENTID");
			int[] dailySeriesIntArray = eventBO.getDailySeriesArray();
			if (dailySeriesIntArray == null) {
				System.out.println("### DailySeriesRotation is not defined!");
				return "";
			}
			if (dailySeriesIntArray.length < 2) {
				System.out.println("### DailySeriesRotation should contain 2 events or more.");
				return "";
			}
			int currentArrayId = Integer.parseInt(parameterEntity.getValue());
			int currentEventId = dailySeriesIntArray[currentArrayId];
			updateEventStatus(currentEventId, false); // Disable previous event
			
			currentArrayId++;
			if (currentArrayId >= dailySeriesIntArray.length) {currentArrayId = 0;} // Reset the rotation
			currentEventId = dailySeriesIntArray[currentArrayId];
			
			updateEventStatus(currentEventId, true); // Enable new event
			parameterEntity.setValue(String.valueOf(currentArrayId));
			parameterDAO.update(parameterEntity);
		}
		return "";
	}
	
	private void updateEventStatus (int eventId, boolean isEnabled) {
		EventEntity event = eventDAO.findById(eventId);
		event.setIsEnabled(isEnabled);
		eventDAO.update(event);
	}
	
}
