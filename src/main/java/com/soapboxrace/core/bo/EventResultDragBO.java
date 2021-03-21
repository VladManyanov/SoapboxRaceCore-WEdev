package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.CarBrandsList;
import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.CustomCarDAO;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppEvent;
import com.soapboxrace.jaxb.http.Accolades;
import com.soapboxrace.jaxb.http.ArrayOfDragEntrantResult;
import com.soapboxrace.jaxb.http.DragArbitrationPacket;
import com.soapboxrace.jaxb.http.DragEntrantResult;
import com.soapboxrace.jaxb.http.DragEventResult;
import com.soapboxrace.jaxb.xmpp.XMPP_DragEntrantResultType;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeDragEntrantResult;

@Stateless
public class EventResultDragBO {

	@EJB
	private EventSessionDAO eventSessionDao;

	@EJB
	private EventDataDAO eventDataDao;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private RewardDragBO rewardDragBO;

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
	private CustomCarDAO customCarDAO;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private EventBO eventBO;
	
	@EJB
	private LegitRaceBO legitRaceBO;
	
	@EJB
	private CarBrandsList carBrandsList;
	
	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
	private ParameterBO parameterBO;

	public DragEventResult handleDragEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, DragArbitrationPacket dragArbitrationPacket, Long eventEnded) {
		Long eventSessionId = eventSessionEntity.getId();
		eventSessionEntity.setEnded(System.currentTimeMillis());

		eventSessionDao.update(eventSessionEntity);
		int playerRank = dragArbitrationPacket.getRank();
		
		XMPP_DragEntrantResultType xmppDragResult = new XMPP_DragEntrantResultType();
		xmppDragResult.setEventDurationInMilliseconds(dragArbitrationPacket.getEventDurationInMilliseconds());
		xmppDragResult.setEventSessionId(eventSessionId);
		xmppDragResult.setFinishReason(dragArbitrationPacket.getFinishReason());
		xmppDragResult.setPersonaId(activePersonaId);
		xmppDragResult.setRanking(playerRank);
		xmppDragResult.setTopSpeed(dragArbitrationPacket.getTopSpeed());

		XMPP_ResponseTypeDragEntrantResult dragEntrantResultResponse = new XMPP_ResponseTypeDragEntrantResult();
		dragEntrantResultResponse.setDragEntrantResult(xmppDragResult);
		PersonaEntity personaEntity = personaDAO.findById(activePersonaId);
		String playerName = personaEntity.getName();
		
		EventDataEntity eventDataEntity = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
		// XKAYA's arbitration exploit fix
		if (eventDataEntity.getArbitration()) {
			System.out.println("WARINING - XKAYA's arbitration exploit attempt, driver: " + personaEntity.getName());
			return null;
		}
		eventDataEntity.setArbitration(eventDataEntity.getArbitration() ? false : true);
		int finishReason = dragArbitrationPacket.getFinishReason();
		if (finishReason == 22) { // Proceed with achievements only when finish is proper
			achievementsBO.applyAirTimeAchievement(dragArbitrationPacket, personaEntity);
			achievementsBO.applyDragAchievement(eventDataEntity, dragArbitrationPacket, activePersonaId);
			achievementsBO.applyEventKmsAchievement(personaEntity, (long) eventDataEntity.getEvent().getTrackLength());
		}

		int currentEventId = eventDataEntity.getEvent().getId();
		EventEntity eventEntity = eventDataEntity.getEvent();
		eventDataEntity.setAlternateEventDurationInMilliseconds(dragArbitrationPacket.getAlternateEventDurationInMilliseconds());
		eventDataEntity.setCarId(dragArbitrationPacket.getCarId());
		eventDataEntity.setEventDurationInMilliseconds(dragArbitrationPacket.getEventDurationInMilliseconds());
		eventDataEntity.setEventModeId(eventDataEntity.getEvent().getEventModeId());
		eventDataEntity.setFinishReason(dragArbitrationPacket.getFinishReason());
		eventDataEntity.setFractionCompleted(dragArbitrationPacket.getFractionCompleted());
		eventDataEntity.setHacksDetected(dragArbitrationPacket.getHacksDetected());
		eventDataEntity.setLongestJumpDurationInMilliseconds(dragArbitrationPacket.getLongestJumpDurationInMilliseconds());
		eventDataEntity.setNumberOfCollisions(dragArbitrationPacket.getNumberOfCollisions());
		eventDataEntity.setPerfectStart(dragArbitrationPacket.getPerfectStart());
		eventDataEntity.setPersonaId(activePersonaId);
		eventDataEntity.setRank(playerRank);
		eventDataEntity.setSumOfJumpsDurationInMilliseconds(dragArbitrationPacket.getSumOfJumpsDurationInMilliseconds());
		eventDataEntity.setTopSpeed(dragArbitrationPacket.getTopSpeed());
		eventDataEntity.setAvgSpeed(dragArbitrationPacket.getPhysicsMetrics().getSpeedAverage());
		
		boolean speedBugChance = eventResultBO.speedBugChance(personaEntity.getUser().getLastLogin());
		eventDataEntity.setSpeedBugChance(speedBugChance);
		int carVersion = eventResultBO.carVersionCheck(activePersonaId);
		eventDataEntity.setCarVersion(carVersion);
		eventDataEntity.setServerEventDuration(eventEnded - eventDataEntity.getServerEventDuration());
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
		// eventResultBO.physicsMetricsInfoDebug(dragArbitrationPacket);
		
		ArrayOfDragEntrantResult arrayOfDragEntrantResult = new ArrayOfDragEntrantResult();
		boolean isSingle = false;
		// +1 to play count for this track, MP
		if (playerRank == 1 && arrayOfDragEntrantResult.getDragEntrantResult().size() > 1) {
			eventEntity.setFinishCount(eventEntity.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity);
			personaDAO.update(personaEntity);
		}
		// +1 to play count for this track, SP
		if (arrayOfDragEntrantResult.getDragEntrantResult().size() < 2) {
			eventEntity.setFinishCount(eventEntity.getFinishCount() + 1);
			personaEntity.setRacesCount(personaEntity.getRacesCount() + 1);
			eventDAO.update(eventEntity);
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
		int carclasshash = eventEntity.getCarClassHash();
		boolean isDNFActive = parameterBO.getBoolParam("DNF_ENABLED");
		if (carclasshash == 607077938) {
			isDNFActive = false; // Don't use DNF timeout on open-class racing
		}
		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
			DragEntrantResult dragEntrantResult = new DragEntrantResult();
			dragEntrantResult.setEventDurationInMilliseconds(racer.getEventDurationInMilliseconds());
			dragEntrantResult.setEventSessionId(eventSessionId);
			dragEntrantResult.setFinishReason(racer.getFinishReason());
			dragEntrantResult.setPersonaId(racer.getPersonaId());
			dragEntrantResult.setRanking(racer.getRank());
			dragEntrantResult.setTopSpeed(racer.getTopSpeed());
			arrayOfDragEntrantResult.getDragEntrantResult().add(dragEntrantResult);

			if (!racer.getPersonaId().equals(activePersonaId)) {
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendDragEntrantInfo(dragEntrantResultResponse);
			}
			if (isDNFActive && playerRank == 1) { // FIXME can be executed twice with the sync finish place issues
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendDragEntrantInfo(dragEntrantResultResponse);
				xmppEvent.sendEventTimingOut(eventSessionId);
				eventResultBO.timeLimitTimer(eventSessionId, (long) 60000); // Default timeout time is 60 seconds
			}
		}

		DragEventResult dragEventResult = new DragEventResult();
		int isDropableMode = 1;
		// Give weak drop if it's a single-player drag
		if (arrayOfDragEntrantResult.getDragEntrantResult().size() < 2) {
			isDropableMode = 3;
		}
		if (finishReason == 22) {
			dragEventResult.setAccolades(rewardDragBO.getDragAccolades(activePersonaId, dragArbitrationPacket, eventSessionEntity, arrayOfDragEntrantResult, isDropableMode));
		}
		else {
			dragEventResult.setAccolades(new Accolades());
		}
		dragEventResult.setDurability(carDamageBO.updateDamageCar(activePersonaId, dragArbitrationPacket, dragArbitrationPacket.getNumberOfCollisions()));
		dragEventResult.setEntrants(arrayOfDragEntrantResult);
		dragEventResult.setEventId(currentEventId);
		eventResultBO.defineFinishLobby(dragEventResult, eventSessionEntity, personaEntity.getRaceAgain());
		
		// Check race record
		legitRaceBO.isRecordVaildDrag(dragArbitrationPacket, eventDataEntity, customCarEntity, speedBugChance, personaEntity, eventEntity);
		
		return dragEventResult;
	}

}
