package com.soapboxrace.core.api;

import java.net.URI;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.soapboxrace.core.api.util.HwBan;
import com.soapboxrace.core.api.util.LauncherChecks;
import com.soapboxrace.core.api.util.Secured;
import com.soapboxrace.core.bo.AuthenticationBO;
import com.soapboxrace.core.bo.FriendBO;
import com.soapboxrace.core.bo.InviteTicketBO;
import com.soapboxrace.core.bo.LobbyBO;
import com.soapboxrace.core.bo.MatchmakingBO;
import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.PersonaBO;
import com.soapboxrace.core.bo.TokenSessionBO;
import com.soapboxrace.core.bo.UserBO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.TokenSessionEntity;
import com.soapboxrace.jaxb.http.UserInfo;
import com.soapboxrace.jaxb.login.LoginStatusVO;

@Path("User")
public class User {

	@Context
	UriInfo uri;

	@Context
	private HttpServletRequest sr;

	@EJB
	private AuthenticationBO authenticationBO;

	@EJB
	private UserBO userBO;

	@EJB
	private TokenSessionBO tokenBO;

	@EJB
	private InviteTicketBO inviteTicketBO;

	@EJB
	private FriendBO friendBO;

	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private LobbyBO lobbyBO;
	
	@EJB
	private MatchmakingBO matchmakingBO;
	
	@EJB
	private TokenSessionDAO tokenDAO;

	@POST
	@Secured
	@Path("GetPermanentSession")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPermanentSession(@HeaderParam("securityToken") String securityToken, @HeaderParam("userId") Long userId) {
		URI myUri = uri.getBaseUri();
		String randomUUID = tokenBO.createToken(userId, myUri.getHost());
		tokenBO.createPresenceEntry(userId);
		UserInfo userInfo = userBO.getUserById(userId);
		userInfo.getUser().setSecurityToken(randomUUID);
		userBO.createXmppUser(userInfo);
		System.out.println("### User logged in (getPermanentSession), ID: " + userId);
		return Response.ok(userInfo).build();
	}

	@POST
	@Secured
	@HwBan
	@Path("SecureLoginPersona")
	@Produces(MediaType.APPLICATION_XML)
	public String secureLoginPersona(@HeaderParam("securityToken") String securityToken, @HeaderParam("userId") Long userId,
			@QueryParam("personaId") Long personaId) {
		tokenBO.setActivePersonaId(securityToken, personaId, false);
		userBO.secureLoginPersona(userId, personaId);
		return "";
	}

	@POST
	@Secured
	@Path("SecureLogoutPersona")
	@Produces(MediaType.APPLICATION_XML)
	public String secureLogoutPersona(@HeaderParam("securityToken") String securityToken, @HeaderParam("userId") Long userId,
			@QueryParam("personaId") Long personaId) {
		PersonaEntity personaEntity = personaBO.getPersonaById(personaId);
		friendBO.sendXmppPresenceToAllFriends(personaEntity, 0);
		matchmakingBO.resetIgnoredEvents(personaId);
		matchmakingBO.removePlayerFromQueue(personaId);
		tokenBO.resetRaceNow(securityToken);
		friendBO.sendXmppPresenceToAllFriends(personaEntity, 0);
		tokenBO.setActivePersonaId(securityToken, 0L, true);
		personaPresenceDAO.updateCurrentEventPost(personaId, null, 0, null, false);
		return "";
	}

	@POST
	@Secured
	@Path("SecureLogout")
	@Produces(MediaType.APPLICATION_XML)
	public String secureLogout(@HeaderParam("securityToken") String securityToken) {
		TokenSessionEntity tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		Long activePersonaId = tokenSessionEntity.getActivePersonaId();
		if (activePersonaId == null || activePersonaId.equals(0l)) {
			return "";
		}
		Long userId = tokenSessionEntity.getUserId();
		Long activeLobbyId = tokenSessionEntity.getActiveLobbyId();
		lobbyBO.deleteLobbyEntrant(activePersonaId, activeLobbyId); // Remove the player from current lobby, if any present
		matchmakingBO.resetIgnoredEvents(activePersonaId);
		matchmakingBO.removePlayerFromQueue(activePersonaId);
		PersonaEntity personaEntity = personaBO.getPersonaById(activePersonaId);
		tokenBO.resetRaceNow(securityToken);
		personaPresenceDAO.userQuitUpdate(userId);
		friendBO.sendXmppPresenceToAllFriends(personaEntity, 0);
		tokenBO.setActivePersonaId(securityToken, 0L, true);
		System.out.println("### User logged out (SecureLogout), ID: " + userId);
		return "";
	}

	@GET
	@Path("authenticateUser")
	@Produces(MediaType.APPLICATION_XML)
	@LauncherChecks
	public Response authenticateUser(@QueryParam("email") String email, @QueryParam("password") String password, @HeaderParam("User-Agent") String UserAgent, @HeaderParam("X-UserAgent") String XUserAgent) {
		LoginStatusVO loginStatusVO = tokenBO.login(email, password, sr);
		
//		if (XUserAgent != null) {
//			loginStatusVO = new LoginStatusVO(0l, "", false);
//			loginStatusVO.setDescription("Attention! Hacking attempt. Use legal launchers.");
//			return Response.serverError().entity(loginStatusVO).build();
//		}
//		LaunchFilter lf = new LaunchFilter(parameterBO);
//		if (lf.isRWSystem(UserAgent)) {
//			if (!lf.checkVersionRW()) {
//				loginStatusVO = new LoginStatusVO(0l, "", false);
//				loginStatusVO.setDescription(LaunchFilter.msg_OldVersionlauncher);
//				return Response.serverError().entity(loginStatusVO).build();
//			}
//		}
//		else if (parameterBO.getBoolParam("RWAC_LAUNCHER_PROTECTION")) {
//			loginStatusVO = new LoginStatusVO(0l, "", false);
//			loginStatusVO.setDescription(LaunchFilter.msg_InvalidLauncher);
//			return Response.serverError().entity(loginStatusVO).build();
//		}
		
		if (loginStatusVO.isLoginOk()) {
			return Response.ok(loginStatusVO).build();
		}
		return Response.serverError().entity(loginStatusVO).build();
	}

	@GET
	@Path("createUser")
	@Produces(MediaType.APPLICATION_XML)
	@LauncherChecks
	public Response createUser(@QueryParam("email") String email, @QueryParam("password") String password, @QueryParam("inviteTicket") String inviteTicket) {
		LoginStatusVO loginStatusVO = tokenBO.checkGeoIp(sr.getRemoteAddr());
		if (!loginStatusVO.isLoginOk()) {
			return Response.serverError().entity(loginStatusVO).build();
		}
		loginStatusVO = userBO.createUserWithTicket(email, password, inviteTicket);
		if (loginStatusVO != null && loginStatusVO.isLoginOk()) {
			loginStatusVO = tokenBO.login(email, password, sr);
			return Response.ok(loginStatusVO).build();
		}
		return Response.serverError().entity(loginStatusVO).build();
	}

}
