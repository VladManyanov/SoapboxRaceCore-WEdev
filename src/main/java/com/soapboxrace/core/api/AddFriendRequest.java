package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.api.util.Secured;
import com.soapboxrace.core.bo.FriendBO;
import com.soapboxrace.jaxb.http.FriendResult;
import com.soapboxrace.jaxb.util.JAXBUtility;

@Path("/addfriendrequest")
public class AddFriendRequest {
	
	@EJB
	private FriendBO bo;

	@GET
	@Secured
	@Produces(MediaType.APPLICATION_XML)
	public String addFriendRequest(@QueryParam("personaId") Long personaId, @QueryParam("displayName") String displayName,
			@QueryParam("reqMessage") String reqMessage) {
		FriendResult friendResult = bo.sendFriendRequest(personaId, displayName, reqMessage);
		if (friendResult == null) {
			return "";
		}
		return JAXBUtility.marshal(friendResult);
	}

}
