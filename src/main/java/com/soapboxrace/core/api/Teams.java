package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.TeamsBO;
import com.soapboxrace.core.dao.ParameterDAO;
import com.soapboxrace.core.jpa.ParameterEntity;

@Path("/Teams")
public class Teams {

	@EJB
	private TeamsBO bo;

	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private ParameterDAO parameterDAO;

	// TODO Web-based Teams join & management
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
		return "ERROR: invalid token (not a server staff? quit right now)";
	}
	
	@POST
	@Path("/toggleSeason")
	@Produces(MediaType.TEXT_HTML)
	public String toggleSeasonAdmin(@FormParam("teamModerationToken") String teamModerationToken, @FormParam("startSeasonChoose") boolean startSeasonChoose) {
		ParameterEntity curSeasonValue = parameterDAO.findById("TEAM_CURRENTSEASON");
		int currentSeason = Integer.parseInt(curSeasonValue.getValue());
		if (startSeasonChoose && currentSeason == 0) { // Start the new season
			return bo.executeSeasonChange(curSeasonValue, startSeasonChoose);
		}
		if (startSeasonChoose && currentSeason != 0) { // Can't start season if it's already started
			return "ERROR: Team Racing season is already active";
		}
		if (!startSeasonChoose && currentSeason != 0) { // Close current active season
			return bo.executeSeasonChange(curSeasonValue, startSeasonChoose);
		}
		if (!startSeasonChoose && currentSeason == 0) { // Can't close season if season is not active at all
			return "ERROR: Team Racing season is not active";
		}
		return "ERROR: invalid token (not a server staff? quit right now)";
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
