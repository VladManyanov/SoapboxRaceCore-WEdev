package com.soapboxrace.core.bo;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.FriendListDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.dao.ReportDAO;
import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.dao.VinylStorageDAO;
import com.soapboxrace.core.dao.VisualPartDAO;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.ReportEntity;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.core.xmpp.OpenFireRestApiCli;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.FriendResult;

@Stateless
public class WindowCommandsBO {

	@EJB
	private PersonaDAO personaDAO;

	@EJB
	private FriendListDAO friendListDAO;

	@EJB
	private DriverPersonaBO driverPersonaBO;

	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private TeamsBO teamsBO;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private OpenFireRestApiCli openFireRestApiCli;

	@EJB
	private TokenSessionDAO tokenSessionDAO;
	
	@EJB
	private TeamsDAO teamsDAO;
	
	@EJB
	private ReportDAO reportDAO;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private CommerceBO commerceBO;
	
	@EJB
	private VinylStorageDAO vinylStorageDAO;
	
	@EJB
	private VinylStorageBO vinylStorageBO;
	
	@EJB
	private UserDAO userDAO;
	
	@EJB
	private UserBO userBO;
	
	@EJB
	private AchievementsBO achievementsBO;
	
	@EJB
	private VisualPartDAO visualPartDAO;
	
	@EJB
	private FriendBO friendBO;

	// Teams actions parser into "add a friend" window - Hypercycle
	// XMPP messages can go into timeouts, if ' symbol is used
	// so i did some weird 'else' outputs for messages
	// FIXME Need to rework that code
	public FriendResult WindowCommandChoice(Long personaId, PersonaEntity personaSender, String displayName) {
		if (displayName.startsWith("/TEAMJOIN ")) {
			String teamName = displayName.replaceFirst("/TEAMJOIN ", "");
			TeamsEntity teamToJoin = teamsDAO.findByName(teamName);
			if (teamToJoin == null) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This team is not exist."), personaId);
				return null;
			}
			if (personaSender.getLevel() < 30) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### To participate on teams, LVL 30 and higher is required."), personaId);
				return null;
			}
			if (!teamToJoin.getOpenEntry()) {
				Long teamleaderId = teamToJoin.getLeader().getPersonaId();
				List<ReportEntity> teamInviteCheck = reportDAO.findTeamInvite(teamleaderId, personaId);
				if (teamToJoin.getPlayersCount() >= 8) {
					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This team is full."), personaId);
					return null;
				}
				if (!teamInviteCheck.isEmpty() && teamToJoin.getPlayersCount() < 8) {
					teamsBO.teamJoinIG(personaSender, teamToJoin);
					reportDAO.deleteTeamInvite(teamleaderId, personaId);
//					openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You're joined to team!"), personaId);
					return null;
				}
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This team is invite-only."), personaId);
				return null;
			}
			if (personaSender.getTeam() == teamToJoin) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You already on this team..."), personaId);
				return null;
			}
			if (personaSender.getTeam() != null) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You already on another team..."), personaId);
				return null;
			}
			if (teamToJoin.getPlayersCount() >= 8) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This team is full."), personaId);
				return null;
			}
			teamsBO.teamJoinIG(personaSender, teamToJoin);
			return null;
		}
		if (displayName.startsWith("/TEAMLEAVE")) {
			TeamsEntity playerTeamLeave = personaSender.getTeam();
			if (playerTeamLeave == null) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Get a team first..."), personaId);
				return null;
			}
			if (playerTeamLeave.getLeader() == personaSender) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You can't leave your own team."), personaId);
				return null;
			}
			teamsBO.teamLeaveIG(personaSender, playerTeamLeave);
			return null;
		}
		if (displayName.startsWith("/TEAMKICK ")) {
			// System.out.println("TeamKick init");
			TeamsEntity leaderTeam = personaSender.getTeam();
			String badTeammateName = displayName.replaceFirst("/TEAMKICK ", "");
			PersonaEntity badTeammate = personaDAO.findByName(badTeammateName);
			if (leaderTeam == null) {
				// System.out.println("TeamKick leaderTeam null");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Get a team first..."), personaId);
				return null;
			}
			if (leaderTeam.getLeader() != personaSender) {
				// System.out.println("TeamKick wrongLeader");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You're is not a team leader."), personaId);
				return null;
			}
			if (leaderTeam.getLeader() == badTeammate) {
				// System.out.println("TeamKick KickYourself");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You can't leave your own team."), personaId);
				return null;
			}
			if (badTeammate == null) {
				// System.out.println("TeamKick wrongNick");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Wrong nickname."), personaId);
				return null;
			}
			if (badTeammate.getTeam() != leaderTeam) {
				// System.out.println("TeamKick wrongPlayerTeam");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This player is not on your team..."), personaId);
				return null;
			}
			else {
				// System.out.println("TeamKick else");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Player is no longer on this team."), personaId);
			}
			// System.out.println("TeamKick leave init");
			teamsBO.teamLeaveIG(badTeammate, leaderTeam);
			return null;
		}
		if (displayName.contentEquals("/TEAMBREAK")) {
			System.out.println("TeamBreak init");
			TeamsEntity leaderTeam = personaSender.getTeam();
			if (leaderTeam == null) {
				System.out.println("TeamBreak leaderTeam null");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Get a team first..."), personaId);
				return null;
			}
			if (leaderTeam.getLeader() != personaSender) {
				System.out.println("TeamBreak wrongLeader");
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You're is not a team leader."), personaId);
				return null;
			}
			System.out.println("TeamBreak exit init");
			teamsBO.teamBreakIG(personaSender, leaderTeam);
			return null;
		}
		if (displayName.startsWith("/TEAMPLAYERS ")) {
			String teamPlayers = "";
			String teamName = displayName.replaceFirst("/TEAMPLAYERS ", "");
			TeamsEntity teamToCheck = teamsDAO.findByName(teamName);
			if (teamToCheck == null) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This team is not exist."), personaId);
				return null;
			}
			if (teamToCheck.getPlayersCount() >= 1) {
				List<PersonaEntity> listOfProfiles = teamToCheck.getListOfTeammates();
				for (PersonaEntity personaEntityTeam : listOfProfiles) {
					teamPlayers = teamPlayers.concat(personaEntityTeam.getName() + " ");
				}
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Team players: " + teamPlayers), personaId);
				return null;
			}
			return null;
		}
		if (displayName.startsWith("/TEAMENTRY ")) {
			TeamsEntity leaderTeam = personaSender.getTeam();
			String entryValue = displayName.replaceFirst("/TEAMENTRY ", "");
			boolean openEntryBool = false;
			if (entryValue.contentEquals("PUBLIC")) {
				openEntryBool = true;
			}
			if (entryValue.contentEquals("PRIVATE")) {
				openEntryBool = false;
			}
			if (leaderTeam == null) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Get a team first..."), personaId);
				return null;
			}
			if (leaderTeam.getLeader() != personaSender) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You're is not a team leader."), personaId);
				return null;
			}
			if (leaderTeam.getOpenEntry() == openEntryBool) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Team already has that status."), personaId);
				return null;
			}
			if (entryValue.contentEquals("PUBLIC") || entryValue.contentEquals("PRIVATE")) {
				teamsBO.teamEntryIG(openEntryBool, leaderTeam);
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Team's entry rule has changed."), personaId);
			return null;
		    }
			return null;
		}
		
		// Applies the vinyl from DB, uses OwnedCarTrans as a blank for already existed scripts (Not the ideal way...)
		if (displayName.startsWith("/VINYL ")) {
			vinylStorageBO.vinylStorageApply(personaId, displayName);
			return null;
		}
		if (displayName.contentEquals("/VINYLUPLOAD")) {
			vinylStorageBO.vinylStorageUpload(personaId);
			return null;
		}
		if (displayName.startsWith("/VINYLREMOVE ")) {
			vinylStorageBO.vinylStorageRemove(personaId, displayName);
			return null;
		}
		if (displayName.contentEquals("/VINYLWIPEALL")) {
			vinylStorageBO.vinylStorageRemoveAll(personaId);
			return null;
		}
		if (displayName.contentEquals("/MODDER")) {
			UserEntity userEntity = personaSender.getUser();
			if (userEntity.isModder()) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You're already have a Modder status."), personaId);
				return null;
			}
			userEntity.setModder(true);
			userDAO.update(userEntity);
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Modder access is enabled, please restart the game."), personaId);
			return null;
		}
		// Send persona's money to another persona (/SENDMONEY nickName money)
		if (displayName.startsWith("/SENDMONEY ")) {
			userBO.sendMoney(personaSender, displayName);
			return null;
		}
		// Get extra reserve money to current persona
		if (displayName.contentEquals("/GETMONEY")) {
			userBO.getMoney(personaSender);
			return null;
		}
		// Freeroam Sync module switch (experimental)
		if (displayName.contentEquals("/SYNCSWITCH")) {
			UserEntity userEntity = personaSender.getUser();
			if (userEntity.getFRSyncAlt()) {
				userEntity.setFRSyncAlt(false);
				userDAO.update(userEntity);
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Sync shard is Main now, go to the Garage and back."), personaId);
				return null;
			}
			userEntity.setFRSyncAlt(true);
			userDAO.update(userEntity);
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Sync shard is Alternative now, go to the Garage and back."), personaId);
			return null;
		}
		// Re-calc persona's score counter
		if (displayName.contentEquals("/RECALCSCORE")) {
			achievementsBO.forceScoreCalc(personaSender);
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Score is re-calculated, re-login into persona."), personaId);
			return null;
		}
		// Remove hidden items from car's special slots
		if (displayName.contentEquals("/DELHIDDENPARTS")) {
			visualPartDAO.deleteHiddenItems(personaBO.getDefaultCarEntity(personaId).getOwnedCar().getCustomCar());
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Hidden parts is removed, go to the Garage and return back."), personaId);
			return null;
		}
		// Switch the "Ignore races on search" feature for persona
		if (displayName.contentEquals("/IGNORERACES")) {
			if (personaSender.isIgnoreRaces()) {
				personaSender.setIgnoreRaces(false);
				personaDAO.update(personaSender);
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Ignore Races feature is disabled."), personaId);
			} else {
				personaSender.setIgnoreRaces(true);
				personaDAO.update(personaSender);
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Server will mark the declined races as to be ignored."), personaId);
			}
			return null;
		}
		// Set MM Priority search timeout for persona
		if (displayName.startsWith("/SEARCHTIMEOUT ")) {
			int seconds = 0;
			try {
				seconds = Integer.parseInt(displayName.replaceFirst("/SEARCHTIMEOUT ", ""));
		    } 
			catch (NumberFormatException|ArrayIndexOutOfBoundsException ex) {
		       	openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Time value is invaild, try again."), personaId);
		       	return null;
		    }
			if (seconds < 0 || seconds > 600) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Acceptable time values is from 0 to 600."), personaId);
				return null;
			}
			personaSender.setPriorityMMTimeout(seconds * 1000); // Milliseconds
			personaDAO.update(personaSender);
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Search timeout value has been saved."), personaId);
			return null;
		}
		return null;
	}
}
