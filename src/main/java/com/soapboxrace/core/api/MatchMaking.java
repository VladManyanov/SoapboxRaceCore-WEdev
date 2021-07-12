package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.api.util.Secured;
import com.soapboxrace.core.bo.EventBO;
import com.soapboxrace.core.bo.EventMissionsBO;
import com.soapboxrace.core.bo.EventResultBO;
import com.soapboxrace.core.bo.FriendBO;
import com.soapboxrace.core.bo.LobbyBO;
import com.soapboxrace.core.bo.LobbyCountdownBO;
import com.soapboxrace.core.bo.MatchmakingBO;
import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.PersonaBO;
import com.soapboxrace.core.bo.TokenSessionBO;
import com.soapboxrace.core.bo.util.CarClassType;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.EventDAO;
import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.dao.LobbyEntrantDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.engine.EngineException;
import com.soapboxrace.core.engine.EngineExceptionCode;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.LobbyEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.PersonaPresenceEntity;
import com.soapboxrace.core.jpa.TokenSessionEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.CustomCarTrans;
import com.soapboxrace.jaxb.http.LobbyInfo;
import com.soapboxrace.jaxb.http.SecurityChallenge;
import com.soapboxrace.jaxb.http.SessionInfo;

@Path("/matchmaking")
public class MatchMaking {

	@EJB
	private EventBO eventBO;

	@EJB
	private LobbyBO lobbyBO;

	@EJB
	private TokenSessionBO tokenSessionBO;

	@EJB
	private PersonaBO personaBO;
	
	@EJB
    private LobbyDAO lobbyDAO;
	
	@EJB
	private EventResultBO eventResultBO;
	
	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
	private EventMissionsBO eventMissionsBO;
	
	@EJB
	private EventDAO eventDAO;
	
	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private FriendBO friendBO;
	
	@EJB
	private PersonaDAO personaDAO;
	
	@EJB
	private MatchmakingBO matchmakingBO;
	
	@EJB
	private LobbyCountdownBO lobbyCountdownBO;
	
	@EJB
	private LobbyEntrantDAO lobbyEntrantDAO;
	
	@EJB
    private ParameterBO parameterBO;
	
	@EJB
    private TokenSessionDAO tokenDAO;
	
	@Context
	private HttpServletRequest sr;

	@PUT
	@Secured
	@Path("/joinqueueracenow")
	@Produces(MediaType.APPLICATION_XML)
	public String joinQueueRaceNow(@HeaderParam("securityToken") String securityToken) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		CustomCarTrans customCar = personaBO.getDefaultCar(activePersonaId).getCustomCar();
		int playerCarClass = customCar.getCarClassHash();
		boolean isSClassFilterActive = parameterBO.getBoolParam("RACENOW_SCLASS_SEPARATE");
		if (playerCarClass == CarClassType.S_CLASS.getId() && isSClassFilterActive) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### S-Class cars matchmaking is separate."), activePersonaId);
		}
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(customCar.getPhysicsProfileHash());
		if (!carClassesEntity.getQuickRaceAllowed()) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You cannot join to racing on this vehicle."), activePersonaId);
			throw new EngineException(EngineExceptionCode.GameLocked, false);
		}
		LobbyEntity playerLobby = lobbyDAO.findByHosterPersona(activePersonaId);
		if (playerLobby != null) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You already have a lobby started..."), activePersonaId);
			lobbyCountdownBO.shutdownLobby(playerLobby);
			throw new EngineException(EngineExceptionCode.GameLocked, false);
		}
		else {
			lobbyBO.joinFastLobby(activePersonaId, playerCarClass, customCar.getRaceFilter(), isSClassFilterActive, 1);
		}
		return "";
	}

	@PUT
	@Secured
	@Path("/joinqueueevent/{eventId}")
	@Produces(MediaType.APPLICATION_XML)
	public String joinQueueEvent(@HeaderParam("securityToken") String securityToken, @PathParam("eventId") int eventId) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		if (matchmakingBO.isPlayerOnMMSearch(activePersonaId)) { // Player can't search the race, while searching the race...
			throw new EngineException(EngineExceptionCode.GameLocked, false);
		}
		CustomCarTrans customCar = personaBO.getDefaultCar(activePersonaId).getCustomCar();
		lobbyBO.joinQueueEvent(activePersonaId, eventId, customCar.getCarClassHash());
		return "";
	}

	@PUT
	@Secured
	@Path("/leavequeue")
	@Produces(MediaType.APPLICATION_XML)
	public String leaveQueue(@HeaderParam("securityToken") String securityToken) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		matchmakingBO.removePlayerFromQueue(activePersonaId);
		LobbyEntity lobbyEntity = lobbyDAO.findByHosterPersona(activePersonaId);
		lobbyCountdownBO.shutdownLobby(lobbyEntity);
		//System.out.println("### /leavequeue");
		tokenSessionBO.resetRaceNow(securityToken);
		return "";
	}

	@PUT
	@Secured
	@Path("/leavelobby")
	@Produces(MediaType.APPLICATION_XML)
	public String leavelobby(@HeaderParam("securityToken") String securityToken) {
		TokenSessionEntity tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		Long activePersonaId = tokenSessionEntity.getActivePersonaId();
		Long activeLobbyId = tokenSessionEntity.getActiveLobbyId();
		lobbyBO.deleteLobbyEntrant(activePersonaId, activeLobbyId);
		LobbyEntity lobbyEntity = lobbyDAO.findById(activeLobbyId);
		lobbyCountdownBO.shutdownLobby(lobbyEntity);
		tokenSessionBO.resetRaceNow(securityToken);
		//System.out.println("### /leavelobby");
		return "";
	}

	@GET
	@Secured
	@Path("/launchevent/{eventId}") // Starts single-player event
	@Produces(MediaType.APPLICATION_XML)
	public SessionInfo launchEvent(@HeaderParam("securityToken") String securityToken, @PathParam("eventId") int eventId) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		SecurityChallenge securityChallenge = new SecurityChallenge();
		securityChallenge.setChallengeId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		securityChallenge.setLeftSize(14);
		securityChallenge.setPattern("FFFFFFFFFFFFFFFF");
		securityChallenge.setRightSize(50);
		SessionInfo sessionInfo = new SessionInfo();
		sessionInfo.setChallenge(securityChallenge);
		sessionInfo.setEventId(eventId);
		EventSessionEntity createEventSession = eventBO.createSPEventSession(eventId, activePersonaId);
		Long eventSessionId = createEventSession.getId();
		
		sessionInfo.setSessionId(eventSessionId);
		tokenSessionBO.setActiveLobbyId(securityToken, 0L);
	
		EventEntity eventEntity = eventDAO.findById(eventId);
		eventMissionsBO.getEventMissionInfo(eventEntity, activePersonaId);
		
		return sessionInfo;
	}

	@PUT
	@Secured
	@Path("/makeprivatelobby/{eventId}")
	@Produces(MediaType.APPLICATION_XML)
	public String makePrivateLobby(@HeaderParam("securityToken") String securityToken, @PathParam("eventId") int eventId, @PathParam("carClassHash") int carClassHash) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		CustomCarTrans customCar = personaBO.getDefaultCar(activePersonaId).getCustomCar();
		lobbyBO.createPrivateLobby(activePersonaId, eventId, customCar.getCarClassHash());
		return "";
	}

	@PUT
	@Secured
	@Path("/acceptinvite")
	@Produces(MediaType.APPLICATION_XML)
	public LobbyInfo acceptInvite(@HeaderParam("securityToken") String securityToken, @QueryParam("lobbyInviteId") Long lobbyInviteId) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		LobbyEntity checkLobbyEntity = lobbyDAO.findById(lobbyInviteId);
		if (checkLobbyEntity == null) { // Since our requested lobby doesn't exist for some reason, we should create a new one
			int playerCarClass = personaBO.getCurrentPlayerCarClass(activePersonaId);
			
			PersonaPresenceEntity personaPresenceEntity = personaPresenceDAO.findByUserId(tokenSessionBO.getUser(securityToken).getId());
			System.out.println("### Debug /acceptinvite newLobby: + " + tokenSessionBO.getSearchEventId(securityToken) + " carClass " + playerCarClass + " persona " + activePersonaId + " eventSession " + personaPresenceEntity.getCurrentEventSessionId());
			
			lobbyInviteId = lobbyBO.createLobby(personaDAO.findById(activePersonaId), tokenSessionBO.getSearchEventId(securityToken), false, true, playerCarClass);
			//System.out.println("### /acceptinvite newlobby");
		}
		tokenSessionBO.setActiveLobbyId(securityToken, lobbyInviteId);
		//System.out.println("### /acceptinvite");
		return lobbyBO.acceptinvite(activePersonaId, lobbyInviteId);
	}

	@PUT
	@Secured
	@Path("/declineinvite")
	@Produces(MediaType.APPLICATION_XML)
	public String declineInvite(@HeaderParam("securityToken") String securityToken, @QueryParam("lobbyInviteId") Long lobbyInviteId) {
		LobbyEntity lobbyEntity = lobbyDAO.findById(lobbyInviteId);
		TokenSessionEntity tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		int searchEventId = tokenSessionEntity.getSearchEventId();
		Long activePersonaId = tokenSessionEntity.getActivePersonaId();
		lobbyCountdownBO.shutdownLobby(lobbyEntity);
		PersonaEntity personaEntity = personaDAO.findById(activePersonaId);
		if (!tokenSessionEntity.isMapHostedEvent() && personaEntity.isIgnoreRaces() && searchEventId != 0) { // For some reason, searchEventId can be 0 at this point
			matchmakingBO.ignoreEvent(activePersonaId, eventDAO.findById(searchEventId));
			//System.out.println("### /declineinvite ignore");
		}
		matchmakingBO.removePlayerFromQueue(activePersonaId);
		tokenSessionBO.resetRaceNow(securityToken);
		//System.out.println("### /declineinvite");
		return "";
	}

}
