package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.bo.AchievementsBO;
import com.soapboxrace.core.bo.EventBO;
import com.soapboxrace.core.bo.EventPowerupsBO;
import com.soapboxrace.core.bo.InventoryBO;
import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.PersonaBO;
import com.soapboxrace.core.bo.TokenSessionBO;
import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.bo.util.StringListConverter;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.PersonaPresenceEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypePowerupActivated;

@Path("/powerups")
public class Powerups {

	@EJB
	private TokenSessionBO tokenBO;

	@EJB
	private InventoryBO inventoryBO;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private AchievementsBO achievementsBO;

	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private EventSessionDAO eventSessionDao;
	
	@EJB
	private PersonaDAO personaDAO;
	
	@EJB
	private EventPowerupsBO eventPowerupsBO;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private EventBO eventBO;
	
	@EJB
	private StringListConverter stringListConverter;

	@POST
	@Path("/activated/{powerupHash}")
	@Produces(MediaType.APPLICATION_XML)
	public String activated(@HeaderParam("securityToken") String securityToken, @PathParam(value = "powerupHash") Integer powerupHash,
			@QueryParam("targetId") Long targetId, @QueryParam("receivers") String receivers, @QueryParam("eventSessionId") Long eventSessionId) {
		Long[] infoPackage = tokenBO.getActivePersonaUserTeamId(securityToken);
		Long activePersonaId = infoPackage[0].longValue();
		Long userId = infoPackage[1].longValue();
//		Long teamId = infoPackage[2].longValue();
		PersonaPresenceEntity personaPresenceEntity = personaPresenceDAO.findByUserId(userId);
//		Long serverEventSessionId = personaPresenceEntity.getCurrentEventSessionId();
		boolean isPUDisabled = personaPresenceEntity.getDisablePU();

		if (parameterBO.getBoolParam("POWERUPS_ENABLED")) {
			if (isPUDisabled) {
				return "";
			}
			XMPP_ResponseTypePowerupActivated powerupActivatedResponse = eventPowerupsBO.powerupResponse(powerupHash, targetId, activePersonaId);
			// Experimental access timeout fix
			new Thread(new Runnable() {
				@Override
				public void run() {
					openFireSoapBoxCli.send(powerupActivatedResponse, activePersonaId);
				}
			}).start();
			for (String receiver : receivers.split("-")) {
				Long receiverPersonaId = Long.valueOf(receiver);
				if (receiverPersonaId > 10 && !activePersonaId.equals(receiverPersonaId)) {
					openFireSoapBoxCli.send(powerupActivatedResponse, receiverPersonaId);
				}
		    }
			
            // If player has played on any of events, game will never set the eventSession to 0 again until the restart
			// So we check it on the server-side
			Long eventDataId = personaPresenceEntity.getCurrentEventDataId();
			if (eventDataId != null) {
				eventPowerupsBO.recordPowerups(powerupHash, userId, eventDataId);
			}
		}

		if (!inventoryBO.hasItem(activePersonaId, powerupHash)) {
//			System.out.println(String.format("Persona %d doesn't have powerup %d", activePersonaId, powerupHash));
			return "";
		}
		if (parameterBO.getBoolParam("ENABLE_POWERUP_DECREASE")) {
			inventoryBO.decrementUsage(activePersonaId, powerupHash);
		}
		
		PersonaEntity personaEntity = personaBO.getPersonaById(activePersonaId);
		achievementsBO.applyPowerupAchievement(personaEntity);
		return "";
	}
}
