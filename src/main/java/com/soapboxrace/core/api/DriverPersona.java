package com.soapboxrace.core.api;

import java.io.InputStream;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import com.soapboxrace.core.api.util.Secured;
import com.soapboxrace.core.bo.DriverPersonaBO;
import com.soapboxrace.core.bo.FriendBO;
import com.soapboxrace.core.bo.MatchmakingBO;
import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.TokenSessionBO;
import com.soapboxrace.core.bo.UserBO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.PersonaPresenceEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.jaxb.http.ArrayOfInt;
import com.soapboxrace.jaxb.http.ArrayOfLong;
import com.soapboxrace.jaxb.http.ArrayOfPersonaBase;
import com.soapboxrace.jaxb.http.ArrayOfString;
import com.soapboxrace.jaxb.http.PersonaIdArray;
import com.soapboxrace.jaxb.http.PersonaMotto;
import com.soapboxrace.jaxb.http.PersonaPresence;
import com.soapboxrace.jaxb.http.ProfileData;
import com.soapboxrace.jaxb.util.JAXBUtility;

@Path("/DriverPersona")
public class DriverPersona {

	private final Pattern NAME_PATTERN = Pattern.compile("^[A-Z0-9]{3,15}$");

	@EJB
	private DriverPersonaBO bo;

	@EJB
	private UserBO userBo;

	@EJB
	private TokenSessionBO tokenSessionBo;

	@EJB
	private PersonaDAO personaDAO;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private FriendBO friendBO;

	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private MatchmakingBO matchmakingBO;

	// Level calc - summary amount of EXP for all levels, consider a DB's level_rep values to sum for every level stage
	// DB's level exp calc by Metonator's EXP generator
	// Any level above 100 will cause THunts to disappear from map, game issue
	@GET
	@Path("/GetExpLevelPointsMap")
	@Produces(MediaType.APPLICATION_XML)
	public ArrayOfInt getExpLevelPointsMap() {
		ArrayOfInt arrayOfInt = new ArrayOfInt();
		arrayOfInt.getInt().add(100); // 1
		arrayOfInt.getInt().add(975);
		arrayOfInt.getInt().add(2025);
		arrayOfInt.getInt().add(3625);
		arrayOfInt.getInt().add(5875); 
		arrayOfInt.getInt().add(8875);
		arrayOfInt.getInt().add(12725);
		arrayOfInt.getInt().add(17525);
		arrayOfInt.getInt().add(23375);
		arrayOfInt.getInt().add(30375); // 10
		arrayOfInt.getInt().add(39375);
		arrayOfInt.getInt().add(50575);
		arrayOfInt.getInt().add(64175);
		arrayOfInt.getInt().add(80375);
		arrayOfInt.getInt().add(99375);
		arrayOfInt.getInt().add(121375);
		arrayOfInt.getInt().add(146575);
		arrayOfInt.getInt().add(175175);
		arrayOfInt.getInt().add(207375);
		arrayOfInt.getInt().add(243375); // 20
		arrayOfInt.getInt().add(283375);
		arrayOfInt.getInt().add(327575);
		arrayOfInt.getInt().add(376175);
		arrayOfInt.getInt().add(429375);
		arrayOfInt.getInt().add(487375);
		arrayOfInt.getInt().add(550375);
		arrayOfInt.getInt().add(618575);
		arrayOfInt.getInt().add(692175);
		arrayOfInt.getInt().add(771375);
		arrayOfInt.getInt().add(856375); // 30
		arrayOfInt.getInt().add(950875);
		arrayOfInt.getInt().add(1055275);
		arrayOfInt.getInt().add(1169975);
		arrayOfInt.getInt().add(1295375);
		arrayOfInt.getInt().add(1431875);
		arrayOfInt.getInt().add(1579875);
		arrayOfInt.getInt().add(1739775);
		arrayOfInt.getInt().add(1911975);
		arrayOfInt.getInt().add(2096875);
		arrayOfInt.getInt().add(2294875); // 40
		arrayOfInt.getInt().add(2506375);
		arrayOfInt.getInt().add(2731775);
		arrayOfInt.getInt().add(2971475);
		arrayOfInt.getInt().add(3225875);
		arrayOfInt.getInt().add(3495375);
		arrayOfInt.getInt().add(3780375);
		arrayOfInt.getInt().add(4081275);
		arrayOfInt.getInt().add(4398475);
		arrayOfInt.getInt().add(4732375);
		arrayOfInt.getInt().add(5083375); // 50
		arrayOfInt.getInt().add(5481355);
		arrayOfInt.getInt().add(5898805);
		arrayOfInt.getInt().add(6336165);
		arrayOfInt.getInt().add(6793875);
		arrayOfInt.getInt().add(7272375);
		arrayOfInt.getInt().add(7772105);
		arrayOfInt.getInt().add(8293505);
		arrayOfInt.getInt().add(8837015);
		arrayOfInt.getInt().add(9403075);
		arrayOfInt.getInt().add(9992125); // 60
		arrayOfInt.getInt().add(10604605);
		arrayOfInt.getInt().add(11240955);
		arrayOfInt.getInt().add(11901615);
		arrayOfInt.getInt().add(12587025);
		arrayOfInt.getInt().add(13297625);
		arrayOfInt.getInt().add(14033855);
		arrayOfInt.getInt().add(14796155);
		arrayOfInt.getInt().add(15584965);
		arrayOfInt.getInt().add(16400725);
		arrayOfInt.getInt().add(17243875); // 70
		arrayOfInt.getInt().add(18114855);
		arrayOfInt.getInt().add(19014105);
		arrayOfInt.getInt().add(19942065);
		arrayOfInt.getInt().add(20899175);
		arrayOfInt.getInt().add(21885875);
		arrayOfInt.getInt().add(22902605);
		arrayOfInt.getInt().add(23949805);
		arrayOfInt.getInt().add(25027915);
		arrayOfInt.getInt().add(26137375);
		arrayOfInt.getInt().add(27278625); // 80
		arrayOfInt.getInt().add(28452105);
		arrayOfInt.getInt().add(29658255);
		arrayOfInt.getInt().add(30897515);
		arrayOfInt.getInt().add(32170325);
		arrayOfInt.getInt().add(33477125);
		arrayOfInt.getInt().add(34818355);
		arrayOfInt.getInt().add(36194455);
		arrayOfInt.getInt().add(37605865);
		arrayOfInt.getInt().add(39053025);
		arrayOfInt.getInt().add(40536375); // 90
		arrayOfInt.getInt().add(42056355);
		arrayOfInt.getInt().add(43613405);
		arrayOfInt.getInt().add(45207965);
		arrayOfInt.getInt().add(46840475);
		arrayOfInt.getInt().add(48511375);
		arrayOfInt.getInt().add(50221105);
		arrayOfInt.getInt().add(51970105);
		arrayOfInt.getInt().add(53758815);
		arrayOfInt.getInt().add(55587675);
		arrayOfInt.getInt().add(57457125); // 100
		return arrayOfInt;
	}

	@GET
	@Path("/GetPersonaInfo")
	@Produces(MediaType.APPLICATION_XML)
	public ProfileData getPersonaInfo(@QueryParam("personaId") Long personaId) {
		return bo.getPersonaInfo(personaId);
	}

	@POST
	@Secured
	@Path("/ReserveName")
	@Produces(MediaType.APPLICATION_XML)
	public ArrayOfString reserveName(@QueryParam("name") String name) {
		return bo.reserveName(name);
	}

	@POST
	@Secured
	@Path("/UnreserveName")
	@Produces(MediaType.APPLICATION_XML)
	public String UnreserveName(@QueryParam("name") String name) {
		return "";
	}

	@POST
	@Secured
	@Path("/CreatePersona")
	@Produces(MediaType.APPLICATION_XML)
	public Response createPersona(@HeaderParam("userId") Long userId, @HeaderParam("securityToken") String securityToken, @QueryParam("name") String name,
			@QueryParam("iconIndex") int iconIndex, @QueryParam("clan") String clan, @QueryParam("clanIcon") String clanIcon) {
		if (!NAME_PATTERN.matcher(name).matches()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Invalid name. Can only contain A-Z, 0-9, and can be between 3 and 15 characters.")
					.build();
		}
		ArrayOfString nameReserveResult = bo.reserveName(name);

		if (!nameReserveResult.getString().isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Player with this name already exists!").build();
			// throw new WebServiceException("Player with this name already exists!");
		}

		PersonaEntity personaEntity = new PersonaEntity();
		name = name.toUpperCase();
		personaEntity.setName(name);
		personaEntity.setIconIndex(iconIndex);
		personaEntity.setCarSlots(parameterBO.getCarLimit(securityToken));

		ProfileData persona = bo.createPersona(userId, personaEntity);

		if (persona == null) {
			return Response.status(Response.Status.FORBIDDEN).entity("Can't have more than 3 personas").build();
		}

		long personaId = persona.getPersonaId();
		userBo.createXmppUser(personaId, securityToken.substring(0, 16));
		return Response.ok(persona).build();
	}

	@POST
	@Secured
	@Path("/DeletePersona")
	@Produces(MediaType.APPLICATION_XML)
	public String deletePersona(@QueryParam("personaId") Long personaId, @HeaderParam("securityToken") String securityToken) {
		tokenSessionBo.verifyPersona(securityToken, personaId);
		tokenSessionBo.setActivePersonaId(securityToken, 0L, true);
		String deletedName = personaDAO.findById(personaId).getName();
		if (parameterBO.getBoolParam("PERSONADELETE_TEMPMODE")) {
			System.out.println("### User with mail " + tokenSessionBo.getUser(securityToken).getEmail() + " has deleted one of his driver persona (Temp mode): " + deletedName);
			bo.deletePersonaTemp(personaId);
		}
		else {
			System.out.println("### User with mail " + tokenSessionBo.getUser(securityToken).getEmail() + " has deleted one of his driver persona: " + deletedName);
			bo.deletePersona(personaId);
		}
		return "<long>0</long>";
	}

	@POST
	@Path("/GetPersonaBaseFromList")
	@Produces(MediaType.APPLICATION_XML)
	public ArrayOfPersonaBase getPersonaBaseFromList(InputStream is) {
		PersonaIdArray personaIdArray = JAXBUtility.unMarshal(is, PersonaIdArray.class);
		ArrayOfLong personaIds = personaIdArray.getPersonaIds();
		return bo.getPersonaBaseFromList(personaIds.getLong());
	}

	@POST
	@Secured
	@Path("/UpdatePersonaPresence")
	@Produces(MediaType.APPLICATION_XML)
	public String updatePersonaPresence(@HeaderParam("securityToken") String securityToken, @QueryParam("presence") int presence) {
		long activePersonaId = tokenSessionBo.getActivePersonaId(securityToken);
		if (activePersonaId == 0L) {
			return "";
		}
		PersonaEntity personaEntity = personaDAO.findById(activePersonaId);
		personaPresenceDAO.updatePersonaPresence(activePersonaId, presence);
		friendBO.sendXmppPresenceToAllFriends(personaEntity, presence);
		matchmakingBO.removePlayerFromQueue(activePersonaId);
		return "";
	}

	@GET
	@Secured
	@Path("/GetPersonaPresenceByName")
	@Produces(MediaType.APPLICATION_XML)
	public String getPersonaPresenceByName(@QueryParam("displayName") String displayName) {
		PersonaEntity personaEntity = personaDAO.findByName(displayName);
		if (personaEntity == null) {
			return "";
		}
		Long personaId = personaEntity.getPersonaId();
		Long userId = personaEntity.getUser().getId();
		
		PersonaPresence personaPresenceByName = new PersonaPresence();
		personaPresenceByName.setPersonaId(personaId);
		personaPresenceByName.setPresence(0); // Offline by default
		personaPresenceByName.setUserId(userId);
		PersonaPresenceEntity personaPresenceEntity = personaPresenceDAO.findByUserId(userId);
		if (personaPresenceEntity != null && personaPresenceEntity.getActivePersonaId() != 0) {
			personaPresenceByName.setPresence(personaPresenceEntity.getPersonaPresence());
		}
		if (personaPresenceByName.getPersonaId() == 0) {
			return "";
		}
		return JAXBUtility.marshal(personaPresenceByName);
	}

	@POST
	@Secured
	@Path("/UpdateStatusMessage")
	@Produces(MediaType.APPLICATION_XML)
	public PersonaMotto updateStatusMessage(InputStream statusXml, @HeaderParam("securityToken") String securityToken, @Context Request request) {
		PersonaMotto personaMotto = JAXBUtility.unMarshal(statusXml, PersonaMotto.class);
		tokenSessionBo.verifyPersona(securityToken, personaMotto.getPersonaId());

		bo.updateStatusMessage(personaMotto.getMessage(), personaMotto.getPersonaId());
		return personaMotto;
	}
}
