package com.soapboxrace.core.bo;

import java.math.BigInteger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.RecordsDAO;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.ArbitrationPacket;
import com.soapboxrace.jaxb.http.DragArbitrationPacket;
import com.soapboxrace.jaxb.http.PursuitArbitrationPacket;
import com.soapboxrace.jaxb.http.RouteArbitrationPacket;
import com.soapboxrace.jaxb.http.TeamEscapeArbitrationPacket;

@Stateless
public class LegitRaceBO {

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private SocialBO socialBo;
	
	@EJB
    private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
	private RecordsDAO recordsDAO;
	
	@EJB
	private RecordsBO recordsBO;

	public boolean isLegit(Long activePersonaId, ArbitrationPacket arbitrationPacket, EventSessionEntity sessionEntity, boolean isSingle) {
		int minimumTime = 0;
		long eventMinTime = sessionEntity.getEvent().getMinTime();
		String eventName = sessionEntity.getEvent().getName();
		if (eventMinTime != 0) {
			minimumTime = (int) eventMinTime;
		}
		String eventType = null;

		if (arbitrationPacket instanceof PursuitArbitrationPacket) {
			minimumTime = parameterBO.getIntParam("PURSUIT_MINIMUM_TIME");
		    eventType = "Pursuit";
		}
		else if (arbitrationPacket instanceof RouteArbitrationPacket) {
			minimumTime = parameterBO.getIntParam("ROUTE_MINIMUM_TIME");
		    eventType = "Race";
		}
		else if (arbitrationPacket instanceof TeamEscapeArbitrationPacket) {
			minimumTime = parameterBO.getIntParam("TE_MINIMUM_TIME");
	        eventType = "Team Escape";
		}
		else if (arbitrationPacket instanceof DragArbitrationPacket) {
			minimumTime = parameterBO.getIntParam("DRAG_MINIMUM_TIME");
		    eventType = "Drag";
		}

		long eventDuration = arbitrationPacket.getEventDurationInMilliseconds();
//		final long timeDiff = sessionEntity.getEnded() - sessionEntity.getStarted(); // SessionEnded is NULL sometimes, eventDuration time would be more efficient
		boolean legit = eventDuration >= minimumTime;
		boolean finishReasonLegit = true;
		String isSingleText = "MP";
		if (isSingle = true) {
			isSingleText = "SP";
		}
		
		// 0 - quitted from race, 22 - finished, 518 - escaped from SP pursuit, 266 - busted on SP & MP pursuit, 8202 - aborted on TE, 16394 - DNF or timeout
		if (arbitrationPacket.getFinishReason() != 0 && arbitrationPacket.getFinishReason() != 266 && arbitrationPacket.getFinishReason() != 8202) {
			finishReasonLegit = false;
		}
		if (!legit && !finishReasonLegit) {
			socialBo.sendReport(0L, activePersonaId, 3, String.format(eventType + " (" + eventName + "), abnormal event time (ms, session: " + sessionEntity.getId() + "): %d", eventDuration), (int) arbitrationPacket.getCarId(), 0, 0L);
		}
		if (eventDuration > 10000000) { // 4294967295 is not a vaild race time...
			socialBo.sendReport(0L, activePersonaId, 3, String.format(eventType + ", error/auto-finish (rank: " + arbitrationPacket.getRank() + ")  event time (ms): %d", eventDuration), (int) arbitrationPacket.getCarId(), 0, 0L);
		}
		if (!legit && eventType.contentEquals("Pursuit")) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### To get the reward, you need to stay in Pursuit longer."), activePersonaId);
		}
		if (arbitrationPacket.getHacksDetected() > 0 && sessionEntity.getEvent().getId() != 1000) { // 1000 - Cheat-legal freeroam event
			socialBo.sendReport(0L, activePersonaId, 3, ("Cheat report, during a " + eventType + " (" + eventName + "), " + isSingleText), (int) arbitrationPacket.getCarId(), 0,
					arbitrationPacket.getHacksDetected());
		}
		return legit;
	}
	
	public void isRecordVaildRoute(RouteArbitrationPacket routeArbitrationPacket, EventDataEntity eventDataEntity, 
			CustomCarEntity customCarEntity, boolean isInterceptorEvent, boolean speedBugChance, PersonaEntity personaEntity,
			EventEntity eventEntity) {
		boolean raceIssues = false;
		int eventClass = eventEntity.getCarClassHash();
		Long personaId = personaEntity.getPersonaId();
				
		Long raceHacks = routeArbitrationPacket.getHacksDetected();
		Long raceTime = eventDataEntity.getEventDurationInMilliseconds();
		Long timeDiff = raceTime - eventDataEntity.getAlternateEventDurationInMilliseconds(); // If the time & altTime is differs so much, the player's data might be wrong
		int playerPhysicsHash = customCarEntity.getPhysicsProfileHash();
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(playerPhysicsHash);
				
		if (carClassesEntity.getModelSmall() == null || isInterceptorEvent) { // If the car doesn't have a modelSmall name - we will not allow it for records
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Records cannot be saved on this car or event."), personaId);
		}
		if (!raceIssues && speedBugChance) { // Prevent possibly speed-bugged time to be saved
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Your game session is too long to save a record, restart the game."), personaId);
		}
		if (!raceIssues && eventEntity.getMinTime() >= raceTime) { // Race minimal time-limit
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Minimal race time limit, record will be not saved."), personaId);
		}
		if (!raceIssues && (routeArbitrationPacket.getFinishReason() != 22 || (raceHacks != 0 && raceHacks != 32) 
				|| (timeDiff > 1000 || timeDiff < -1000) || raceTime > 2000000 
				|| (eventClass != 607077938 && eventClass != customCarEntity.getCarClassHash()))) {
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Invaild race session, restart the game and try again."), personaId);
		}
				
		// If some server admin did a manual player unban via DB, and forgot to uncheck the userBan field for him, this player should know about it
		BigInteger zeroCheck = new BigInteger("0");
		if (!recordsDAO.countBannedRecords(personaEntity.getUser().getId()).equals(zeroCheck)) {
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Some records on this account is still banned, contact to server staff."), personaId);
		}
		if (!raceIssues) {
			recordsBO.submitRecord(eventEntity, personaEntity, eventDataEntity, customCarEntity, carClassesEntity);
		}
	}
	
	public void isRecordVaildDrag(DragArbitrationPacket dragArbitrationPacket, EventDataEntity eventDataEntity, 
			CustomCarEntity customCarEntity, boolean speedBugChance, PersonaEntity personaEntity,
			EventEntity eventEntity) {
		boolean raceIssues = false;
		Long personaId = personaEntity.getPersonaId();
		
		Long raceHacks = dragArbitrationPacket.getHacksDetected();
		Long raceTime = eventDataEntity.getEventDurationInMilliseconds();
		Long timeDiff = raceTime - eventDataEntity.getAlternateEventDurationInMilliseconds(); // If the time & altTime is differs so much, the player's data might be wrong
		int playerPhysicsHash = customCarEntity.getPhysicsProfileHash();
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(playerPhysicsHash);
				
		if (speedBugChance) { // Prevent possibly speed-bugged time to be saved
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Your game session is too long to save a record, restart the game."), personaId);
		}
		if (!raceIssues && (carClassesEntity.getModelSmall() == null || customCarEntity.getCarClassHash() == 0)) { // If the car doesn't have a modelSmall name - we will not allow it for records
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Records cannot be saved on this car."), personaId);
		}
		if (!raceIssues && eventEntity.getMinTime() >= raceTime) { // Race minimal time-limit
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Minimal race time limit, record will be not saved."), personaId);
		}
		if (!raceIssues && (dragArbitrationPacket.getFinishReason() != 22 || (raceHacks != 0 && raceHacks != 32) 
				|| eventEntity.getMinTime() >= raceTime || (timeDiff > 1000 || timeDiff < -1000) || raceTime > 2000000)) {
			raceIssues = true;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Invaild race session, restart the game and try again."), personaId);
		}
		if (!raceIssues) {
			recordsBO.submitRecord(eventEntity, personaEntity, eventDataEntity, customCarEntity, carClassesEntity);
		}
	}
	
	// FINISHREASON_Unknown = 0;
	// FINISHREASON_Completed = 2;
	// FINISHREASON_Succeeded = 6;
	// FINISHREASON_DidNotFinish = 10;
	// FINISHREASON_CrossedFinish = 22;
	// FINISHREASON_KnockedOut = 42;
	// FINISHREASON_Totalled = 74;
	// FINISHREASON_EngineBlown = 138;
	// FINISHREASON_Busted = 266;
	// FINISHREASON_Evaded = 518;
	// FINISHREASON_ChallengeCompleted = 1030;
	// FINISHREASON_Disconnected = 2058;
	// FINISHREASON_FalseStart = 4106;
	// FINISHREASON_Aborted = 8202;
	// FINISHREASON_TimedOut = 16394;
	// FINISHREASON_TimeLimitExpired = 32774;
	// FINISHREASON_PauseDetected = 65546;
	// FINISHREASON_SpeedHacking = 131082;
	// FINISHREASON_CodePatchDetected = 262154;
	// FINISHREASON_BadVerifierResponse = 524298;
}
