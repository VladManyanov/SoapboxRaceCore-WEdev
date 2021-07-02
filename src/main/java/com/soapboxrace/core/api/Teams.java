package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.TeamsBO;

@Path("/Teams")
public class Teams {

	@EJB
	private TeamsBO bo;

	@EJB
	private ParameterBO parameterBO;

	// WEv2 Teams Test - Hypercycle
	@POST
	@Path("/teamJoin")
	@Produces(MediaType.TEXT_HTML)
	public String teamJoin(@FormParam("teamName") String teamName, @FormParam("email") String email, @FormParam("password") String password, @FormParam("nickname") String nickname) {
		if (email.isEmpty() || password.isEmpty() || nickname.isEmpty() || teamName.isEmpty()) {
			return "ERROR: empty email, password, nickname or team name";
		}
		return bo.teamJoin(teamName, email, password, nickname);
	}

	@POST
	@Path("/teamCreate")
	@Produces(MediaType.TEXT_HTML)
	public String teamCreateAdmin(@FormParam("teamModerationToken") String teamModerationToken, @FormParam("teamName") String teamName, @FormParam("leaderName") String leaderName, @FormParam("openEntry") boolean openEntry) {
		if (parameterBO.getStrParam("TEAM_MODERATION_TOKEN").equals(teamModerationToken)) {
			return bo.teamCreate(teamName, leaderName, openEntry);
		}
		if (teamName.isEmpty() || leaderName.isEmpty()) {
			return "ERROR: empty team name or team leader";
		}
		return "ERROR: invalid token (not a team manager? quit right now)";
	}
	
//	@POST
//	@Path("/teamManage")
//	@Produces(MediaType.TEXT_HTML)
//	public String teamManage() {
//		
//	}
	
//	@POST
//	@Path("/teamLeaderboard")
//	@Produces(MediaType.TEXT_HTML)
//	public String teamLeaderboard() {
//		
//	}
}
