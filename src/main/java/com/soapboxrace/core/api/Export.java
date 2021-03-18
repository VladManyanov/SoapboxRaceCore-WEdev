package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.PersonaBO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.jaxb.http.OwnedCarTrans;
import com.soapboxrace.jaxb.util.JAXBUtility;

@Path("/export")
public class Export {

	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private PersonaDAO personaDAO;
	
	@EJB
	private PersonaBO personaBO;

	@POST
	@Path("/getPlayerDefaultCar")
	@Produces(MediaType.TEXT_PLAIN)
	public String getPlayerDefaultCar(@FormParam("adminToken") String adminToken, @FormParam("playerName") String playerName) {
		if (parameterBO.getStrParam("TOKEN_ADMIN").equals(adminToken)) {
			PersonaEntity personaEntity = personaDAO.findByNameIgnoreCase(playerName);
			OwnedCarTrans ownedCarTrans = personaBO.getDefaultCar(personaEntity.getPersonaId());
			return JAXBUtility.marshal(ownedCarTrans);
		}
		return "ERROR: invalid token";
	}
	
}
