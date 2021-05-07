package com.soapboxrace.core.bo;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.StringListConverter;
import com.soapboxrace.core.dao.EventCarInfoDAO;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventPowerupsDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.NewsArticlesDAO;
import com.soapboxrace.core.dao.ParameterDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.dao.VisualPartDAO;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.EventCarInfoEntity;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventPowerupsEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.NewsArticlesEntity;
import com.soapboxrace.core.jpa.ParameterEntity;
import com.soapboxrace.core.jpa.PerformancePartEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.SkillModPartEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppEvent;
import com.soapboxrace.jaxb.http.RouteArbitrationPacket;
import com.soapboxrace.jaxb.xmpp.XMPP_DragEntrantResultType;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeDragEntrantResult;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeRouteEntrantResult;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeTeamEscapeEntrantResult;
import com.soapboxrace.jaxb.xmpp.XMPP_RouteEntrantResultType;
import com.soapboxrace.jaxb.xmpp.XMPP_TeamEscapeEntrantResultType;

@Stateless
public class EventBO {

	@EJB
	private EventDAO eventDao;

	@EJB
	private EventSessionDAO eventSessionDao;

	@EJB
	private EventDataDAO eventDataDao;
	
	@EJB
	private EventPowerupsDAO eventPowerupsDao;

	@EJB
	private PersonaDAO personaDao;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private ParameterDAO parameterDAO;
	
	@EJB
	private EventResultBO eventResultBO;
	
	@EJB
	private NewsArticlesDAO newsArticlesDAO;
	
	@EJB
	private UserDAO userDao;
	
	@EJB
	private EventCarInfoDAO eventCarInfoDao;
	
	@EJB
	private VisualPartDAO visualPartDao;
	
	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private StringListConverter stringListConverter;

	public List<EventEntity> getAvailableEvents(Long personaId, boolean seqCSeries) {
		PersonaEntity personaEntity = personaDao.findById(personaId);
		if (!seqCSeries) { // Default AvailableAtLevel events list
			return eventDao.findByRotation(personaEntity.getLevel());
		}
		else { // AvailableAtLevel events list without Daily Series events
			int[] dailySeriesIntArray = getDailySeriesArray();
			List<EventEntity> availableEvents = eventDao.findByRotationBase(personaEntity.getLevel());
			EventEntity csEvent = eventDao.findByIdDetached(dailySeriesIntArray[personaEntity.getSeqCSCurrentEvent()]);
			csEvent.setIsEnabled(true);
			availableEvents.add(csEvent); // Add the current Daily Series event to the list
			return availableEvents;
		}
	}
	
	public int[] getDailySeriesArray() {
		String dailySeriesStr = parameterBO.getStrParam("DAILYSERIES_SCHEDULE");
		return Stream.of(dailySeriesStr.split(",")).mapToInt(Integer::parseInt).toArray();
	}

	public Long createEventDataSession(Long personaId, Long eventSessionId, Long eventTimer) {
		EventSessionEntity eventSessionEntity = findEventSessionById(eventSessionId);
		EventDataEntity eventDataEntity = new EventDataEntity();
		eventDataEntity.setPersonaId(personaId);
		eventDataEntity.setEventSessionId(eventSessionId);
		eventDataEntity.setEvent(eventSessionEntity.getEvent());
		eventDataEntity.setServerEventDuration(eventTimer); // Temp value of the event timer (current system time)
		eventDataEntity.setDate(LocalDateTime.now());
		eventDataDao.insert(eventDataEntity);
		return eventDataEntity.getId();
	}
	
	public void createEventPowerupsSession(Long personaId, Long eventDataId) {
		EventPowerupsEntity eventPowerupsEntity = new EventPowerupsEntity();
		eventPowerupsEntity.setEventData(eventDataId);
		eventPowerupsDao.insert(eventPowerupsEntity);
	}
	
	public void createEventCarInfo(Long personaId, Long eventDataId) {
		EventCarInfoEntity eventCarInfoEntity = new EventCarInfoEntity();
		eventCarInfoEntity.setEventData(eventDataId);
		eventCarInfoEntity.setPersonaId(personaId);
		eventCarInfoDao.insert(eventCarInfoEntity);
	}
	
	// For old records without EventCarInfo data
	public EventCarInfoEntity createDummyEventCarInfo() {
		EventCarInfoEntity eventCarInfoEntity = new EventCarInfoEntity();
		eventCarInfoEntity.setBodykit(false);
		eventCarInfoEntity.setSpoiler(false);
		eventCarInfoEntity.setLowkit(false);
		eventCarInfoEntity.setRating(0);
		eventCarInfoEntity.setPerfParts(null);
		eventCarInfoEntity.setSkillParts(null);
		return eventCarInfoEntity;
	}

	public EventSessionEntity createSPEventSession(int eventId, Long personaId) {
		EventEntity eventEntity = eventDao.findById(eventId);
		if (eventEntity == null) {
			return null;
		}
		EventSessionEntity eventSessionEntity = new EventSessionEntity();
		eventSessionEntity.setEvent(eventEntity);
		eventSessionEntity.setStarted(System.currentTimeMillis());
		eventSessionEntity.setTeamNOS(false); // Temporal value
		eventSessionEntity.setPlayerList(personaId.toString()); // Save the Id of the persona-hoster
		eventSessionEntity.setLobbyId(0L); // Lobby ID could be defined during lobby logic
		eventSessionDao.insert(eventSessionEntity);
		return eventSessionEntity;
	}
	
	public void updateEventCarInfo(Long personaId, Long eventDataId, CustomCarEntity customCarEntity) {
		EventCarInfoEntity eventCarInfoEntity = eventCarInfoDao.findByEventData(eventDataId);
		Set<SkillModPartEntity> skillModsArray = customCarEntity.getSkillModParts();
		Set<PerformancePartEntity> perfArray = customCarEntity.getPerformanceParts();
		int carRating = customCarEntity.getRating();
		
		eventCarInfoEntity.setSkillParts(stringListConverter.skillModsStrArray(skillModsArray));
		eventCarInfoEntity.setPerfParts(stringListConverter.perfPartsStrArray(perfArray));
		
		boolean hasBodykit = visualPartDao.isBodykitInstalled(customCarEntity);
		boolean hasSpoiler = visualPartDao.isSpoilerInstalled(customCarEntity);
		boolean hasLowkit = visualPartDao.isLowkitInstalled(customCarEntity);
		
		eventCarInfoEntity.setBodykit(hasBodykit);
		eventCarInfoEntity.setSpoiler(hasSpoiler);
		eventCarInfoEntity.setLowkit(hasLowkit);
		eventCarInfoEntity.setRating(carRating);
		eventCarInfoEntity.setEventEnded(true);
		eventCarInfoDao.update(eventCarInfoEntity);
	}
	
	// XMPP player result packet - can be used as the finish signal, or as a race abort signal to other players
	public void sendXmppPacketRoute(Long eventSessionId, Long activePersonaId, RouteArbitrationPacket routeArbitrationPacket, 
			int playerRank, boolean isDNFActive, boolean isRaceEnd) {
		XMPP_RouteEntrantResultType xmppRouteResult = new XMPP_RouteEntrantResultType();
		if (!isRaceEnd) { // Abort
			xmppRouteResult.setBestLapDurationInMilliseconds((long) 0);
			xmppRouteResult.setEventDurationInMilliseconds((long) 0);
			xmppRouteResult.setEventSessionId(eventSessionId);
			xmppRouteResult.setFinishReason(8202);
			xmppRouteResult.setPersonaId(activePersonaId);
			xmppRouteResult.setRanking(playerRank);
		}
		else { // Race end
			xmppRouteResult.setBestLapDurationInMilliseconds(routeArbitrationPacket.getBestLapDurationInMilliseconds());
			xmppRouteResult.setEventDurationInMilliseconds(routeArbitrationPacket.getEventDurationInMilliseconds());
			xmppRouteResult.setEventSessionId(eventSessionId);
			xmppRouteResult.setFinishReason(routeArbitrationPacket.getFinishReason());
			xmppRouteResult.setPersonaId(activePersonaId);
			xmppRouteResult.setRanking(playerRank);
			xmppRouteResult.setTopSpeed(routeArbitrationPacket.getTopSpeed());
		}
		XMPP_ResponseTypeRouteEntrantResult routeEntrantResultResponse = new XMPP_ResponseTypeRouteEntrantResult();
		routeEntrantResultResponse.setRouteEntrantResult(xmppRouteResult);

		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
			if (!racer.getPersonaId().equals(activePersonaId)) {
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendRaceEntrantInfo(routeEntrantResultResponse);
			}
			if (isDNFActive && isRaceEnd && playerRank == 1) { // FIXME can be executed twice with the sync finish place issues
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendRaceEntrantInfo(routeEntrantResultResponse);
				xmppEvent.sendEventTimingOut(eventSessionId);
				eventResultBO.timeLimitTimer(eventSessionId, (long) 60000); // Default timeout time is 60 seconds
			}
		}
	}
	
	public void sendXmppPacketTEAbort(Long eventSessionId, Long activePersonaId) {
		XMPP_TeamEscapeEntrantResultType xmppTeamEscapeResult = new XMPP_TeamEscapeEntrantResultType();
		xmppTeamEscapeResult.setEventDurationInMilliseconds((long) 0);
		xmppTeamEscapeResult.setEventSessionId(eventSessionId);
		xmppTeamEscapeResult.setFinishReason(8202);
		xmppTeamEscapeResult.setRanking((short) 0); 
		xmppTeamEscapeResult.setPersonaId(activePersonaId);

		XMPP_ResponseTypeTeamEscapeEntrantResult teamEscapeEntrantResultResponse = new XMPP_ResponseTypeTeamEscapeEntrantResult();
		teamEscapeEntrantResultResponse.setTeamEscapeEntrantResult(xmppTeamEscapeResult);
		
		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
			if (!racer.getPersonaId().equals(activePersonaId)) {
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendTeamEscapeEntrantInfo(teamEscapeEntrantResultResponse);
			}
		}
	}
	
	public void sendXmppPacketDragAbort(Long eventSessionId, Long activePersonaId) {
		XMPP_DragEntrantResultType xmppDragResult = new XMPP_DragEntrantResultType();
		xmppDragResult.setEventDurationInMilliseconds((long) 0);
		xmppDragResult.setEventSessionId(eventSessionId);
		xmppDragResult.setFinishReason(8202);
		xmppDragResult.setPersonaId(activePersonaId);
		xmppDragResult.setRanking(0);

		XMPP_ResponseTypeDragEntrantResult dragEntrantResultResponse = new XMPP_ResponseTypeDragEntrantResult();
		dragEntrantResultResponse.setDragEntrantResult(xmppDragResult);
		
		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
			if (!racer.getPersonaId().equals(activePersonaId)) {
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendDragEntrantInfo(dragEntrantResultResponse);
			}
		}
	}
	
	// Change the current events list (every week)
	// If ROTATION_COUNT defined as 1, server will not change it (set all event's rotation ids to 1)
	// Rotation on "0" - track is always enable, "999" - not used
	// To display more than #3 rotation news (in-game), a new locale strings should be created
	@Schedule(dayOfWeek = "MON", persistent = false)
	public String eventRotation() {
		int rotationCount = parameterBO.getIntParam("ROTATION_COUNT");
		if (rotationCount == 1) {
			return "";
		}
		ParameterEntity parameterEntity = parameterDAO.findById("ROTATIONID");
		int rotationCur = Integer.valueOf(parameterEntity.getValue()) + 1;
		if (rotationCur > rotationCount) {
			rotationCur = 1;
		}
		parameterEntity.setValue(String.valueOf(rotationCur));
		parameterDAO.update(parameterEntity);
		
		NewsArticlesEntity newsRotation = newsArticlesDAO.findByName("ROTATION");
		newsRotation.setShortTextHALId("TXT_NEWS_WEV2_ROTATION_" + rotationCur + "_SHORT");
		newsRotation.setLongTextHALId("TXT_NEWS_WEV2_ROTATION_" + rotationCur + "_FULL");
		newsArticlesDAO.update(newsRotation);
		
		// Reset money send limits
		userDao.resetMoneySendLimit();
		return "";
	}
	
	// Change the current reward-bonus (and team-racing) class
	// Array structure: Sunday,Monday,Tuesday,Wednesday,Thursday,Friday,Saturday
	// "NP" class parameter means no power-ups day, "0" disables class bonus
	@Schedule(dayOfWeek = "*", persistent = false)
	public String bonusClassRotation() {
		ParameterEntity parameterEntity = parameterDAO.findById("CLASSBONUS_CARCLASSHASH");
		String bonusClassStr = parameterBO.getStrParam("CLASSBONUS_SCHEDULE");
		String[] bonusClassArray = bonusClassStr.split(",");
		if (bonusClassArray.length != 7) {
			System.out.println("### BonusClassRotation is not defined or not vaild!");
			parameterEntity.setValue("0"); // No selected car class
			parameterDAO.update(parameterEntity);
			return "";
		}
		int dayOfWeekInt = (new GregorianCalendar().get(Calendar.DAY_OF_WEEK)) - 1; // Calendar have "1 - 7" numbers, while we need "0 - 6"
		String todayClass = bonusClassArray[dayOfWeekInt];
		parameterEntity.setValue(String.valueOf(eventResultBO.getCarClassInt(todayClass)));
		parameterDAO.update(parameterEntity);
		
		if (todayClass.contentEquals("NP")) {
			ParameterEntity parameterPUEntity = parameterDAO.findById("POWERUPS_NOPUDAY");
			parameterPUEntity.setValue("true");
			parameterDAO.update(parameterPUEntity);
			
			NewsArticlesEntity newsWednesday = newsArticlesDAO.findByName("NOPOWERUPSDAY");
			newsWednesday.setIsEnabled(true);
			newsArticlesDAO.update(newsWednesday);
			
			NewsArticlesEntity newsBonusClass = newsArticlesDAO.findByName("BONUSCLASS");
			newsBonusClass.setIsEnabled(false);
			newsArticlesDAO.update(newsBonusClass);
		}
		if (!todayClass.contentEquals("NP")) {
			ParameterEntity parameterPUEntity = parameterDAO.findById("POWERUPS_NOPUDAY");
			parameterPUEntity.setValue("false");
			parameterDAO.update(parameterPUEntity);
			
			NewsArticlesEntity newsWednesday = newsArticlesDAO.findByName("NOPOWERUPSDAY");
			newsWednesday.setIsEnabled(false);
			newsArticlesDAO.update(newsWednesday);
		}
		if (todayClass.contentEquals("0")) {
			NewsArticlesEntity newsBonusClass = newsArticlesDAO.findByName("BONUSCLASS");
			newsBonusClass.setIsEnabled(false);
			newsArticlesDAO.update(newsBonusClass);
		}
		if (!todayClass.contentEquals("0") && !todayClass.contentEquals("NP")) {
			NewsArticlesEntity newsBonusClass = newsArticlesDAO.findByName("BONUSCLASS");
			newsBonusClass.setShortTextHALId("TXT_NEWS_WEV2_BONUSCLASS_" + todayClass + "_SHORT");
			newsBonusClass.setLongTextHALId("TXT_NEWS_WEV2_BONUSCLASS_" + todayClass + "_FULL");
			newsBonusClass.setIsEnabled(true);
			newsArticlesDAO.update(newsBonusClass);
		}
		return "";
	}
	
	public EventSessionEntity findEventSessionById(Long id) {
		return eventSessionDao.findById(id);
	}
}
