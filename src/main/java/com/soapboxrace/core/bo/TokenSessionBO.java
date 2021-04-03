package com.soapboxrace.core.bo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;

import com.soapboxrace.core.api.util.GeoIp2;
import com.soapboxrace.core.api.util.UUIDGen;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.dao.ServerInfoDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.PersonaPresenceEntity;
import com.soapboxrace.core.jpa.ServerInfoEntity;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.jpa.TokenSessionEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.jaxb.login.LoginStatusVO;

@Stateless
public class TokenSessionBO {
	@EJB
	private TokenSessionDAO tokenDAO;

	@EJB
	private UserDAO userDAO;

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private GetServerInformationBO serverInfoBO;

	@EJB
	private OnlineUsersBO onlineUsersBO;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private ServerInfoDAO serverInfoDAO;
	
	@EJB
	private PersonaDAO personaDAO;

	private static Map<String, TokenSessionEntity> activePersonas = new HashMap<>(300);

	public boolean verifyToken(Long userId, String securityToken) {
		TokenSessionEntity tokenSessionEntity = activePersonas.get(securityToken);
		if (tokenSessionEntity == null) {
			tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		}
		if (tokenSessionEntity == null || !tokenSessionEntity.getUserId().equals(userId)) {
			System.out.println("### User " + userId + " has falied the token verification.");
			return false;
		}
		long time = new Date().getTime();
		long tokenTime = tokenSessionEntity.getExpirationDate().getTime();
		if (time > tokenTime) {
			System.out.println("### User " + userId + " has falied the token verification.");
			return false;
		}
		tokenSessionEntity.setExpirationDate(getMinutes(6));
		tokenDAO.update(tokenSessionEntity);
		// activePersonas.put(securityToken, tokenSessionEntity);
		return true;
	}

	public String createToken(Long userId, String clientHostName) {
		TokenSessionEntity tokenSessionEntity = null;
		try {
			tokenSessionEntity = tokenDAO.findByUserId(userId);
		} catch (Exception e) {
			// not found
		}
		String randomUUID = UUIDGen.getRandomUUID();
		UserEntity userEntity = userDAO.findById(userId);
		Date expirationDate = getMinutes(15);
		if (tokenSessionEntity == null) {
			tokenSessionEntity = new TokenSessionEntity();
			tokenSessionEntity.setExpirationDate(expirationDate);
			tokenSessionEntity.setSecurityToken(randomUUID);
			tokenSessionEntity.setUserId(userId);
			tokenSessionEntity.setPremium(userEntity.isPremium());
			tokenSessionEntity.setClientHostIp(clientHostName);
			tokenDAO.insert(tokenSessionEntity);
		} else {
			tokenSessionEntity.setExpirationDate(expirationDate);
			tokenSessionEntity.setSecurityToken(randomUUID);
			tokenSessionEntity.setUserId(userId);
			tokenSessionEntity.setPremium(userEntity.isPremium());
			tokenSessionEntity.setClientHostIp(clientHostName);
			tokenDAO.update(tokenSessionEntity);
		}
		return randomUUID;
	}
	
	public void createPresenceEntry(Long userId) {
		PersonaPresenceEntity personaPresenceEntity = null;
		try {
			personaPresenceEntity = personaPresenceDAO.findByUserId(userId);
		} catch (Exception e) {
			// not found
		}
		if (personaPresenceEntity == null) {
			personaPresenceEntity = new PersonaPresenceEntity();
			personaPresenceEntity.setUserId(userId);
			personaPresenceEntity.setPersonaPresence(0);
			personaPresenceDAO.insert(personaPresenceEntity);
		} else {
			personaPresenceEntity.setUserId(userId);
			personaPresenceEntity.setPersonaPresence(0);
			personaPresenceDAO.update(personaPresenceEntity);
		}
	}

	public boolean verifyPersona(String securityToken, Long personaId) {
		TokenSessionEntity tokenSession = tokenDAO.findBySecurityToken(securityToken);
		if (tokenSession == null) {
			throw new NotAuthorizedException("Invalid session...");
		}

		UserEntity user = userDAO.findById(tokenSession.getUserId());
		if (!user.ownsPersona(personaId)) {
			throw new NotAuthorizedException("Persona is not owned by user");
		}
		return true;
	}

	private Date getMinutes(int minutes) {
		long time = new Date().getTime();
		time = time + (minutes * 60000);
		Date date = new Date(time);
		return date;
	}

	public LoginStatusVO checkGeoIp(String ip) {
		LoginStatusVO loginStatusVO = new LoginStatusVO(0L, "", false);
		String allowedCountries = serverInfoBO.getServerInformation().getAllowedCountries();
		if (allowedCountries != null && !allowedCountries.isEmpty()) {
			String geoip2DbFilePath = parameterBO.getStrParam("GEOIP2_DB_FILE_PATH");
			GeoIp2 geoIp2 = GeoIp2.getInstance(geoip2DbFilePath);
			if (geoIp2.isCountryAllowed(ip, allowedCountries)) {
				return new LoginStatusVO(0L, "", true);
			} else {
				loginStatusVO.setDescription("GEOIP BLOCK ACTIVE IN THIS SERVER, ALLOWED COUNTRIES: [" + allowedCountries + "]");
			}
		} else {
			return new LoginStatusVO(0L, "", true);
		}
		return loginStatusVO;
	}

	public LoginStatusVO login(String email, String password, HttpServletRequest httpRequest) {
		LoginStatusVO loginStatusVO = checkGeoIp(httpRequest.getRemoteAddr());
		if (!loginStatusVO.isLoginOk()) {
			return loginStatusVO;
		}
		loginStatusVO = new LoginStatusVO(0L, "", false);

		if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
			UserEntity userEntity = userDAO.findByEmail(email);
			if (userEntity != null) {
				ServerInfoEntity serverInfoEntity = serverInfoDAO.findInfo();
				int numberOfUsersOnlineNow = serverInfoEntity.getOnlineNumber();
				int maxOnlinePlayers = parameterBO.getIntParam("MAX_ONLINE_PLAYERS");
				Boolean serverMaintenance = parameterBO.getBoolParam("SERVERMAINTENANCE");
				
				if (userEntity.isPremium() && parameterBO.getBoolParam("PREMIUM_TIMELIMITED")) {
				    LocalDate premiumDate = userEntity.getPremiumDate();
				    LocalDate nowDate = LocalDate.now();
				
				    if (userEntity.getPremiumDate() != null) {
				    Integer days = (int) ChronoUnit.DAYS.between(nowDate, premiumDate.plusDays(186));
				      if (days <= 0) {
				    	  userEntity.setPremium(false);
				    	  userEntity.setPremiumDate(null);
				    	  userEntity.setPremiumType(null);
				  		  userDAO.update(userEntity);
				  		  
				  		  loginStatusVO.setDescription("Your Premium has been expired. Log in again to continue.");
						  return loginStatusVO;
				        }
				      }
				  }
				
				if (numberOfUsersOnlineNow >= maxOnlinePlayers && !userEntity.isPremium()) {
					loginStatusVO.setDescription("Server is full!");
					return loginStatusVO;
				}
				if (serverMaintenance && !userEntity.isAdmin()) {
					loginStatusVO.setDescription("Server is on a update, or not avaliable to play for now.");
					return loginStatusVO;
				}
				
				if (password.equals(userEntity.getPassword())) {
					if (userEntity.getHwid() == null || userEntity.getHwid().trim().isEmpty()) {
						userEntity.setHwid(httpRequest.getHeader("X-HWID"));
					}

					if (userEntity.getIpAddress() == null || userEntity.getIpAddress().trim().isEmpty()) {
						String forwardedFor;
						if ((forwardedFor = httpRequest.getHeader("X-Forwarded-For")) != null && parameterBO.getBoolParam("USE_FORWARDED_FOR")) {
							userEntity.setIpAddress(parameterBO.getBoolParam("GOOGLE_LB_ENABLED") ? forwardedFor.split(",")[0] : forwardedFor);
						} else {
							userEntity.setIpAddress(httpRequest.getRemoteAddr());
						}
					}
					
				    if (httpRequest.getHeader("User-Agent") != null) {
				    	userEntity.setUA(httpRequest.getHeader("User-Agent"));
				    }
				    if (httpRequest.getHeader("X-UserAgent") != null) {
				    	userEntity.setUA(httpRequest.getHeader("X-UserAgent"));
				    }
				    if (httpRequest.getHeader("User-Agent") == null && httpRequest.getHeader("X-UserAgent") == null) {
				    	userEntity.setUA("Undefined NULLs");
				    }

					userEntity.setLastLogin(LocalDateTime.now());
					userDAO.update(userEntity);
					Long userId = userEntity.getId();
					String randomUUID = createToken(userId, null);
					createPresenceEntry(userId);
					loginStatusVO = new LoginStatusVO(userId, randomUUID, true);
					loginStatusVO.setDescription("");

					return loginStatusVO;
				}
			}
		}
		loginStatusVO.setDescription("LOGIN ERROR");
		return loginStatusVO;
	}

	public Long getActivePersonaId(String securityToken) {
		TokenSessionEntity tokenSessionEntity = activePersonas.get(securityToken);
		if (tokenSessionEntity == null) {
			tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		}
		return tokenSessionEntity.getActivePersonaId();
	}
	
	// Sends ActivePersonaId, UserId and TeamId
	public Long[] getActivePersonaUserTeamId(String securityToken) {
		TokenSessionEntity tokenSessionEntity = activePersonas.get(securityToken);
		if (tokenSessionEntity == null) {
			tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		}
		Long[] infoPackage = new Long[3];
		infoPackage[0] = tokenSessionEntity.getActivePersonaId();
		infoPackage[1] = tokenSessionEntity.getUserId();
		if (tokenSessionEntity.getTeamId() == null) {infoPackage[2] = (long) 0;}
		else {infoPackage[2] = tokenSessionEntity.getTeamId();}
		
		return infoPackage;
	}

	@SuppressWarnings("null")
	public void setActivePersonaId(String securityToken, Long personaId, Boolean isLogout) {
		TokenSessionEntity tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		Long userId = tokenSessionEntity.getUserId();
		PersonaPresenceEntity personaPresenceEntity = personaPresenceDAO.findByUserId(userId);

		if (!isLogout) {
			if (!userDAO.findById(userId).ownsPersona(personaId)) {
				throw new NotAuthorizedException("Persona not owned by user");
			}
		}
		tokenSessionEntity.setActivePersonaId(personaId);
		tokenSessionEntity.setIsLoggedIn(!isLogout);
		
		Long teamId = 0L;
		TeamsEntity teamsEntity = null;
		if (personaId != 0) {teamsEntity = personaDAO.findById(personaId).getTeam();}
		if (teamsEntity != null) {teamId = teamsEntity.getTeamId();}
		tokenSessionEntity.setTeamId(teamId);
		
		int presence = 1;
		if (personaId.equals(0L)) {
			presence = 0; // For user logout
		}
		personaPresenceEntity.setPersonaPresence(presence);
		personaPresenceEntity.setActivePersonaId(personaId);
		// activePersonas.put(securityToken, tokenSessionEntity);
		tokenDAO.update(tokenSessionEntity);
		personaPresenceDAO.update(personaPresenceEntity);
	}

	public String getActiveRelayCryptoTicket(String securityToken) {
		TokenSessionEntity tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		return tokenSessionEntity.getRelayCryptoTicket();
	}

	public Long getActiveLobbyId(String securityToken) {
		TokenSessionEntity tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		return tokenSessionEntity.getActiveLobbyId();
	}

	public void setActiveLobbyId(String securityToken, Long lobbyId) {
		TokenSessionEntity tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		tokenSessionEntity.setActiveLobbyId(lobbyId);
		// activePersonas.put(securityToken, tokenSessionEntity);
		tokenDAO.update(tokenSessionEntity);
	}
	
	public int getSearchEventId(String securityToken) {
		TokenSessionEntity tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		// System.out.println("### getSearchEventId: " + tokenSessionEntity.getSearchEventId());
		return tokenSessionEntity.getSearchEventId();
	}
	
	// Save the event ID value for search queue
	public boolean setSearchEventId(Long activePersonaId, int eventId) {
		TokenSessionEntity tokenSessionEntity = tokenDAO.findByActivePersonaId(activePersonaId);
		if (tokenSessionEntity == null) {return false;} // If that activePersonaId is not active already...
		tokenSessionEntity.setSearchEventId(eventId);
		tokenDAO.update(tokenSessionEntity);
		return true;
		// System.out.println("### setSearchEventId: " + eventId);
	}
	
	public boolean isMapHostedEvent(String securityToken) {
		TokenSessionEntity tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		return tokenSessionEntity.isMapHostedEvent();
	}
	
	// Does the player starts a queue from map?
	public void setMapHostedEvent(Long activePersonaId, boolean mapHostedEvent) {
		TokenSessionEntity tokenSessionEntity = tokenDAO.findByActivePersonaId(activePersonaId);
		tokenSessionEntity.setMapHostedEvent(mapHostedEvent);
		tokenDAO.update(tokenSessionEntity);
	}
	
	public void resetRaceNow(String securityToken) {
		TokenSessionEntity tokenSessionEntity = tokenDAO.findBySecurityToken(securityToken);
		tokenSessionEntity.setMapHostedEvent(false);
		tokenSessionEntity.setActiveLobbyId(0L);
		tokenSessionEntity.setSearchEventId(0);
		tokenDAO.update(tokenSessionEntity);
	}

	public boolean isPremium(String securityToken) {
		return tokenDAO.findBySecurityToken(securityToken).isPremium();
	}

	public boolean isAdmin(String securityToken) {
		return getUser(securityToken).isAdmin();
	}

	public UserEntity getUser(String securityToken) {
		return userDAO.findById(tokenDAO.findBySecurityToken(securityToken).getUserId());
	}
	
}
