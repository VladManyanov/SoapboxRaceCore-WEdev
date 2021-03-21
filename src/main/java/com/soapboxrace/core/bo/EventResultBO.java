package com.soapboxrace.core.bo;

import java.time.LocalDateTime;
import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.CarSlotEntity;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.LobbyEntity;
import com.soapboxrace.core.jpa.OwnedCarEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppEvent;
import com.soapboxrace.jaxb.http.ArbitrationPacket;
import com.soapboxrace.jaxb.http.DragArbitrationPacket;
import com.soapboxrace.jaxb.http.DragEventResult;
import com.soapboxrace.jaxb.http.EventResult;
import com.soapboxrace.jaxb.http.ExitPath;
import com.soapboxrace.jaxb.http.PursuitArbitrationPacket;
import com.soapboxrace.jaxb.http.PursuitEventResult;
import com.soapboxrace.jaxb.http.RouteArbitrationPacket;
import com.soapboxrace.jaxb.http.RouteEventResult;
import com.soapboxrace.jaxb.http.TeamEscapeArbitrationPacket;
import com.soapboxrace.jaxb.http.TeamEscapeEventResult;

@Stateless
public class EventResultBO {

	@EJB
	private EventResultRouteBO eventResultRouteBO;

	@EJB
	private EventResultDragBO eventResultDragBO;

	@EJB
	private EventResultTeamEscapeBO eventResultTeamEscapeBO;

	@EJB
	private EventResultPursuitBO eventResultPursuitBO;
	
	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
	private EventDataDAO eventDataDao;
	
	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private LobbyDAO lobbyDAO;
	
	@EJB
	private LobbyBO lobbyBO;
	
	@EJB
	private LobbyCountdownBO lobbyCountdownBO;

	@Resource
    private TimerService timerService;
	
	public PursuitEventResult handlePursuitEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, PursuitArbitrationPacket pursuitArbitrationPacket,
			Boolean isBusted, Long eventEnded) {
		return eventResultPursuitBO.handlePursuitEnd(eventSessionEntity, activePersonaId, pursuitArbitrationPacket, isBusted, eventEnded);
	}

	public RouteEventResult handleRaceEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, RouteArbitrationPacket routeArbitrationPacket, Long eventEnded) {
		return eventResultRouteBO.handleRaceEnd(eventSessionEntity, activePersonaId, routeArbitrationPacket, eventEnded);
	}

	public DragEventResult handleDragEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, DragArbitrationPacket dragArbitrationPacket, Long eventEnded) {
		return eventResultDragBO.handleDragEnd(eventSessionEntity, activePersonaId, dragArbitrationPacket, eventEnded);
	}

	public TeamEscapeEventResult handleTeamEscapeEnd(EventSessionEntity eventSessionEntity, Long activePersonaId,
			TeamEscapeArbitrationPacket teamEscapeArbitrationPacket, Long eventEnded) {
		return eventResultTeamEscapeBO.handleTeamEscapeEnd(eventSessionEntity, activePersonaId, teamEscapeArbitrationPacket, eventEnded);
	}
	
	public void timeLimitTimer (Long eventSessionId, Long timeLimit) {
		TimerConfig timerConfig = new TimerConfig();
	    timerConfig.setInfo(eventSessionId);
	    timerService.createSingleActionTimer(timeLimit, timerConfig);
	    
//	    timeLimitInterceptorAlert(eventSessionId, (timeLimit - 60000));
	}
	
	@Timeout
	public void timeLimitAction (Timer timer) {
		Long eventSessionId = (long) timer.getInfo();
		forceStopEvent(eventSessionId);
	}
	
	// FIXME Could be done better
//	public void timeLimitInterceptorAlert (Long eventSessionId, Long timeLimit) {
//		try {
//			Thread.sleep(timeLimit);
//		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		force60SecsTimer(eventSessionId);
//	}
	
	public void forceStopEvent(Long eventSessionId) {
		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
			if (racer.getFinishReason() == 0) { // Racer has not finished yet
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendEventTimedOut(eventSessionId);
			}
		}
	}
	
	public void force60SecsTimer(Long eventSessionId) {
		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendEventTimingOut(eventSessionId);
			}
	}
	
	// after 2 hours of playing, NFSW's time system can glitch sometimes, giving a possible player advantage
	// so server will save this value is player was logged for 2 hours and more
	public boolean speedBugChance (LocalDateTime lastLogin) {
		boolean speedBugChance = false;
		if (lastLogin.plusHours(2).isBefore(LocalDateTime.now()) ) {
			speedBugChance = true;
		}
		return speedBugChance;
	}
	
	public int carVersionCheck(Long personaId) {
		CarSlotEntity carSlotEntity = personaBO.getDefaultCarEntity(personaId);
		OwnedCarEntity ownedCarEntity = carSlotEntity.getOwnedCar();
		CustomCarEntity customCarEntityVer = ownedCarEntity.getCustomCar();
		
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(customCarEntityVer.getPhysicsProfileHash());
		return carClassesEntity.getCarVersion();
	}
	
	public void physicsMetricsInfoDebug(ArbitrationPacket arbitrationPacket) {
		System.out.println("### TEST Acceleration Average: " + (arbitrationPacket.getPhysicsMetrics().getAccelerationAverage() * 3.6));
		System.out.println("### TEST Acceleration Max: " + (arbitrationPacket.getPhysicsMetrics().getAccelerationMaximum() * 3.6));
		System.out.println("### TEST Acceleration Median: " + (arbitrationPacket.getPhysicsMetrics().getAccelerationMedian() * 3.6));
	    System.out.println("### TEST Speed Average: " + (arbitrationPacket.getPhysicsMetrics().getSpeedAverage() * 3.6));
		System.out.println("### TEST Speed Max: " + (arbitrationPacket.getPhysicsMetrics().getSpeedMaximum() * 3.6));
		System.out.println("### TEST Speed Median: " + (arbitrationPacket.getPhysicsMetrics().getSpeedMedian() * 3.6));
	}
	
	public String getCarClassLetter(int carClassHash) {
		String letter = "";
		switch(carClassHash) {
		case 872416321:
			letter = "E";
			break;
		case 415909161:
			letter = "D";
			break;
		case 1866825865:
			letter = "C";
			break;
		case -406473455:
			letter = "B";
			break;
		case -405837480:
			letter = "A";
			break;
		case -2142411446:
			letter = "S";
			break;
		case 607077938:
			letter = "ALL";
			break;
		default:
			letter = "NPC";
			break;
		}
		return letter;
	}
	
	public int getCarClassInt(String carClassLetter) {
		int carClassHash = 0;
		switch(carClassLetter) {
		case "E":
			carClassHash = 872416321;
			break;
		case "D":
			carClassHash = 415909161;
			break;
		case "C":
			carClassHash = 1866825865;
			break;
		case "B":
			carClassHash = -406473455;
			break;
		case "A":
			carClassHash = -405837480;
			break;
		case "S":
			carClassHash = -2142411446;
			break;
		case "ALL":
			carClassHash = 607077938;
			break;
		default:
			carClassHash = 0;
			break;
		}
		return carClassHash;
	}
	
	// Drift-Spec can be modified on the client side, but we don't want to let modders participate on the events
	// FIXME Test code
	public boolean modCarCheck(Long personaId) {
		CarSlotEntity carSlotEntity = personaBO.getDefaultCarEntity(personaId);
		OwnedCarEntity ownedCarEntity = carSlotEntity.getOwnedCar();
		CustomCarEntity customCarEntity = ownedCarEntity.getCustomCar();
		int carPhysicsHash = customCarEntity.getPhysicsProfileHash();
		boolean isModCar = false;
		if (carPhysicsHash == 202813212 || carPhysicsHash == -840317713 || carPhysicsHash == -845093474 || carPhysicsHash == -133221572 || carPhysicsHash == -409661256) {
			isModCar = true;
		}
		return isModCar;
	}
	
	public EventResult defineFinishLobby(EventResult eventResult, EventSessionEntity eventSessionEntity, boolean raceAgain) {
		eventResult.setEventSessionId(eventSessionEntity.getId());
		Long lobbyId = eventSessionEntity.getLobbyId();
		if (lobbyId == 0 || !raceAgain) { // Don't initiate Race Again if the race is single-player, or player has disabled it
			eventResult.setExitPath(ExitPath.EXIT_TO_FREEROAM);
			eventResult.setInviteLifetimeInMilliseconds(0);
			eventResult.setLobbyInviteId(0);
			eventResult.setPersonaId(eventResult.getPersonaId());
		}
		else {
			LobbyEntity oldLobbyEntity = lobbyDAO.findById(lobbyId);
			if (oldLobbyEntity.isStarted()) {
				oldLobbyEntity.setStarted(false); // Unlock our lobby for players
				oldLobbyEntity.setLobbyDateTimeStart(new Date());
				lobbyCountdownBO.scheduleLobbyStart(oldLobbyEntity);
				lobbyDAO.update(oldLobbyEntity);
			}
			
			eventResult.setExitPath(ExitPath.EXIT_TO_LOBBY);
			// 8s delay is to prevent the transition to un-existing lobby, if too late
			eventResult.setInviteLifetimeInMilliseconds(lobbyBO.getLobbyCountdownInMilliseconds(oldLobbyEntity.getLobbyDateTimeStart()) - 8000); 
			eventResult.setLobbyInviteId(lobbyId);
			eventResult.setPersonaId(eventResult.getPersonaId());
		}
		System.out.println("### defineFinishLobby");
		return eventResult;
	}
}
