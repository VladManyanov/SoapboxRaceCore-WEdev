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
import com.soapboxrace.core.jpa.FriendListEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.ReportEntity;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.core.xmpp.OpenFireRestApiCli;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.FriendPersona;
import com.soapboxrace.jaxb.http.FriendResult;
import com.soapboxrace.jaxb.xmpp.XMPP_FriendPersonaType;
import com.soapboxrace.xmpp.openfire.XmppFriend;

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
	public FriendResult sendFriendRequest(Long personaId, String displayName, String reqMessage) {
		boolean teamsActionInit = false;
		PersonaEntity personaSender = personaDAO.findById(personaId);
		if (displayName.contains("/")) {
			if (displayName.contains("/TEAMJOIN ")) {
				teamsActionInit = true;
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
//						openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You're joined to team!"), personaId);
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
			if (displayName.contains("/TEAMLEAVE")) {
				teamsActionInit = true;
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
			if (displayName.contains("/TEAMKICK ")) {
				// System.out.println("TeamKick init");
				teamsActionInit = true;
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
			if (displayName.contains("/TEAMBREAK")) {
				System.out.println("TeamBreak init");
				teamsActionInit = true;
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
			if (displayName.contains("/TEAMPLAYERS ")) {
				teamsActionInit = true;
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
			if (displayName.contains("/TEAMENTRY ")) {
				teamsActionInit = true;
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
			if (displayName.contains("/VINYL ")) {
				vinylStorageBO.vinylStorageApply(personaId, displayName);
				return null;
			}
			if (displayName.contentEquals("/VINYLUPLOAD")) {
				vinylStorageBO.vinylStorageUpload(personaId);
				return null;
			}
			if (displayName.contains("/VINYLREMOVE ")) {
				vinylStorageBO.vinylStorageRemove(personaId, displayName);
				return null;
			}
			if (displayName.contains("/VINYLWIPEALL")) {
				vinylStorageBO.vinylStorageRemoveAll(personaId);
				return null;
			}
			if (displayName.contains("/MODDER")) {
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
			if (displayName.contains("/SENDMONEY ")) {
				userBO.sendMoney(personaSender, displayName);
				return null;
			}
			// Get extra reserve money to current persona
			if (displayName.contains("/GETMONEY")) {
				userBO.getMoney(personaSender);
				return null;
			}
			// Freeroam Sync module switch (experimental)
			if (displayName.contains("/SYNCSWITCH")) {
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
			if (displayName.contains("/RECALCSCORE")) {
				achievementsBO.forceScoreCalc(personaSender);
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Score is re-calculated, re-login into persona."), personaId);
				return null;
			}
			// Remove hidden items from car's special slots
			if (displayName.contains("/DELHIDDENPARTS")) {
				visualPartDAO.deleteHiddenItems(personaBO.getDefaultCarEntity(personaId).getOwnedCar().getCustomCar());
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Hidden parts is removed, go to the Garage and return back."), personaId);
				return null;
			}
			// Switch the "Ignore races on search" feature for persona
			if (displayName.contains("/IGNORERACES")) {
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
			if (displayName.contains("/SEARCHTIMEOUT ")) {
				int seconds = 0;
				try {
					seconds = Integer.parseInt(displayName.replaceFirst("/SEARCHTIMEOUT ", ""));
		        } catch (NumberFormatException|ArrayIndexOutOfBoundsException ex) {
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
		}

		// default add-a-friend interaction
		if (!teamsActionInit) {
			PersonaEntity personaInvited = personaDAO.findByName(displayName);
			Long senderId = personaSender.getPersonaId();
			Long senderUserId = personaSender.getUser().getId();
			if (personaSender == null || personaInvited == null) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Wrong nickname, try again."), senderId);
				return null;
			}
			Long invitedId = personaInvited.getPersonaId();
			Long invitedUserId = personaInvited.getUser().getId();
			if (senderId == invitedId || senderUserId == invitedUserId) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You cannot be a friend for yourself."), senderId);
				return null;
			}
			FriendListEntity friendListEntity = friendListDAO.findByOwnerIdAndFriendPersona(invitedUserId, senderId);
			if (friendListEntity != null) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This player is already on your friend-list."), senderId);
				return null;
			}
			XMPP_FriendPersonaType friendPersonaType = new XMPP_FriendPersonaType();
			friendPersonaType.setIconIndex(personaSender.getIconIndex());
			friendPersonaType.setLevel(personaSender.getLevel());
			friendPersonaType.setName(personaSender.getName());
			friendPersonaType.setOriginalName(personaSender.getName());
			friendPersonaType.setPersonaId(senderId);
			friendPersonaType.setPresence(3);
			friendPersonaType.setUserId(senderUserId);

			XmppFriend xmppFriend = new XmppFriend(invitedId, openFireSoapBoxCli);
			xmppFriend.sendFriendRequest(friendPersonaType);
			friendBO.createNewFriendListEntry(senderId, invitedUserId, senderUserId, false, false);

			FriendPersona friendPersona = new FriendPersona();
			friendPersona.setIconIndex(personaInvited.getIconIndex());
			friendPersona.setLevel(personaInvited.getLevel());
			friendPersona.setName(personaInvited.getName());
			friendPersona.setOriginalName(personaInvited.getName());
			friendPersona.setPersonaId(invitedId);
			friendPersona.setPresence(0);
			friendPersona.setSocialNetwork(0);
			friendPersona.setUserId(invitedUserId);

			FriendResult friendResult = new FriendResult();
			friendResult.setPersona(friendPersona);
			friendResult.setResult(0);
			return friendResult;
		}
		return null;
	}

}
