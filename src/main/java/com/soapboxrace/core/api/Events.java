package com.soapboxrace.core.api;

import java.util.List;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.api.util.Secured;
import com.soapboxrace.core.bo.EventBO;
import com.soapboxrace.core.bo.EventsBO;
import com.soapboxrace.core.bo.LobbyBO;
import com.soapboxrace.core.bo.MatchmakingBO;
import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.PersonaBO;
import com.soapboxrace.core.bo.TokenSessionBO;
import com.soapboxrace.core.bo.util.EventModeType;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.jaxb.http.ArrayOfEventDefinition;
import com.soapboxrace.jaxb.http.ArrayOfInt;
import com.soapboxrace.jaxb.http.CustomCarTrans;
import com.soapboxrace.jaxb.http.EventDefinition;
import com.soapboxrace.jaxb.http.EventsPacket;
import com.soapboxrace.jaxb.http.OwnedCarTrans;
import com.soapboxrace.jaxb.http.TreasureHuntEventSession;
import com.soapboxrace.jaxb.http.Vector3;

@Path("/events")
public class Events {

	@EJB
	private EventBO eventBO;

	@EJB
	private EventsBO eventsBO;

	@EJB
	private TokenSessionBO tokenSessionBO;

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
	private MatchmakingBO matchmakingBO;
	
	@EJB
	private LobbyBO lobbyBO;

	@GET
	@Path("/availableatlevel")
	@Produces(MediaType.APPLICATION_XML)
	public EventsPacket availableAtLevel(@HeaderParam("securityToken") String securityToken) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		boolean seqCSeries = tokenSessionBO.getUser(securityToken).getIsSeqDailySeries();
		lobbyBO.preparePlayerForFreeroam(activePersonaId, securityToken);
		
		OwnedCarTrans defaultCar = personaBO.getDefaultCar(activePersonaId);
		CustomCarTrans customCarTrans = defaultCar.getCustomCar();
		int carClassHash = customCarTrans.getCarClassHash();
		int carPhysicsHash = customCarTrans.getPhysicsProfileHash();
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(carPhysicsHash);
		boolean isModCar = false;
		if (!carClassesEntity.getQuickRaceAllowed()) {
			isModCar = true;
		}

		EventsPacket eventsPacket = new EventsPacket();
		ArrayOfEventDefinition arrayOfEventDefinition = new ArrayOfEventDefinition();
		List<EventEntity> availableEvents = eventBO.getAvailableEvents(activePersonaId, seqCSeries);
		for (EventEntity eventEntity : availableEvents) {
			String carModel = eventEntity.getCarModel();
			int eventClassHash = eventEntity.getCarClassHash();
			boolean isLockedAlready = false;
			// Event car model restriction (if present)
			if ((eventClassHash != 607077938 && carClassHash != eventClassHash) || (carModel != null && !defaultCar.getCustomCar().getName().equalsIgnoreCase(carModel))) {
				isLockedAlready = true;
				eventEntity.setLocked(true);
			}
			// If player drives a AI or Drift-Spec car, he will be unable to play Drag events
			if (!isLockedAlready && EventModeType.DRAG.getId() == eventEntity.getEventModeId() && (carClassHash == 0 || carClassHash == 1337)) {
				isLockedAlready = true;
				eventEntity.setLocked(true);
			}
			// ModCars cannot join to events (ex. for Private Freeroam)
			if (!isLockedAlready && isModCar && eventEntity.getId() != 1000) {
				eventEntity.setLocked(true);
			}
			arrayOfEventDefinition.getEventDefinition().add(getEventDefinitionWithId(eventEntity));
		}
		eventsPacket.setEvents(arrayOfEventDefinition);
		return eventsPacket;
	}

	private EventDefinition getEventDefinitionWithId(EventEntity eventEntity) {
		EventDefinition eventDefinition = new EventDefinition();
		eventDefinition.setCarClassHash(eventEntity.getCarClassHash());
		eventDefinition.setCoins(0);
		Vector3 vector3 = new Vector3();
		vector3.setX(0);
		vector3.setY(0);
		vector3.setZ(0);
		eventDefinition.setEngagePoint(vector3);
		eventDefinition.setEventId(eventEntity.getId());
		eventDefinition.setEventLocalization(953953223);
		eventDefinition.setEventModeDescriptionLocalization(1204604434);
		eventDefinition.setEventModeIcon("GameModeIcon_Sprint");
		eventDefinition.setEventModeId(eventEntity.getEventModeId());
		eventDefinition.setEventModeLocalization(-1152300457);
		eventDefinition.setIsEnabled(eventEntity.getIsEnabled());
		eventDefinition.setIsLocked(eventEntity.getIsLocked());
		eventDefinition.setLaps(0);
		eventDefinition.setLength(0);
		eventDefinition.setMaxClassRating(eventEntity.getMaxCarClassRating());
		eventDefinition.setMaxEntrants(2);
		eventDefinition.setMaxLevel(eventEntity.getMaxLevel());
		eventDefinition.setMinClassRating(eventEntity.getMinCarClassRating());
		eventDefinition.setMinEntrants(2);
		eventDefinition.setMinLevel(eventEntity.getMinLevel());
		eventDefinition.setRegionLocalization(0);
		eventDefinition.setRewardModes(new ArrayOfInt());
		eventDefinition.setTimeLimit(0);
		eventDefinition.setTrackLayoutMap("nothing");
		eventDefinition.setTrackLocalization(-491522157);
		return eventDefinition;
	}

	@GET
	@Path("/gettreasurehunteventsession")
	@Produces(MediaType.APPLICATION_XML)
	public TreasureHuntEventSession getTreasureHuntEventSession(@HeaderParam("securityToken") String securityToken) {
		if (parameterBO.getBoolParam("ENABLE_TREASURE_HUNT")) {
			Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
			return eventsBO.getTreasureHuntEventSession(activePersonaId);
		}
		return new TreasureHuntEventSession();
	}

	@GET
	@Secured
	@Path("/notifycoincollected")
	@Produces(MediaType.APPLICATION_XML)
	public String notifyCoinCollected(@HeaderParam("securityToken") String securityToken, @QueryParam("coins") Integer coins) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		return eventsBO.notifyCoinCollected(activePersonaId, coins);
	}

	@GET
	@Secured
	@Path("/accolades")
	@Produces(MediaType.APPLICATION_XML)
	public String accolades(@HeaderParam("securityToken") String securityToken) {
		Long activePersonaId = tokenSessionBO.getActivePersonaId(securityToken);
		return eventsBO.accolades(activePersonaId, true);
	}

	@GET
	@Secured
	@Path("/instancedaccolades")
	@Produces(MediaType.APPLICATION_XML)
	public String instancedAccolades(@QueryParam("eventSessionId") Long eventSessionId) {
		return "";
	}
}
