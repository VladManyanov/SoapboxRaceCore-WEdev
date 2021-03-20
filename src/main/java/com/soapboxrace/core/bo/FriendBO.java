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
import com.soapboxrace.core.jpa.FriendListEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.PersonaPresenceEntity;
import com.soapboxrace.core.jpa.TokenSessionEntity;
import com.soapboxrace.core.xmpp.OpenFireRestApiCli;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.ArrayOfBadgePacket;
import com.soapboxrace.jaxb.http.ArrayOfFriendPersona;
import com.soapboxrace.jaxb.http.FriendPersona;
import com.soapboxrace.jaxb.http.FriendResult;
import com.soapboxrace.jaxb.http.PersonaBase;
import com.soapboxrace.jaxb.http.PersonaFriendsList;
import com.soapboxrace.jaxb.xmpp.XMPP_FriendPersonaType;
import com.soapboxrace.jaxb.xmpp.XMPP_FriendResultType;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeFriendResult;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypePersonaBase;
import com.soapboxrace.xmpp.openfire.XmppFriend;

@Stateless
public class FriendBO {

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
	private WindowCommandsBO windowCommandsBO;

	// Loading the player friend-list
	public PersonaFriendsList getFriendListFromUserId(Long userId) {
		ArrayOfFriendPersona arrayOfFriendPersona = new ArrayOfFriendPersona();
		List<FriendPersona> friendPersonaList = arrayOfFriendPersona.getFriendPersona();

		List<FriendListEntity> friendList = friendListDAO.findByOwnerId(userId);
		for (FriendListEntity entity : friendList) {
			PersonaEntity personaEntity = personaDAO.findById(entity.getPersonaId());
			if (personaEntity == null || entity.getIsBlocked()) {
				continue;
			}

			int presence = 3; // 0 - offline, 1 - freeroam, 2 - racing or safehouse, 3 - friend request
//			if (entity.getUserOwnerId().equals(userId) && !entity.getIsAccepted()) {
//				presence = 0; // User A awaits for the User B invite decision, display as "offline"
//			}
			if (entity.getIsAccepted()) {
				PersonaPresenceEntity personaPresenceEntity = personaPresenceDAO.findByUserId(entity.getUserId());
				if (personaPresenceEntity != null) {
					presence = personaPresenceEntity.getPersonaPresence();
				}
			}
			addPersonaToFriendList(friendPersonaList, personaEntity, presence);
		}

		PersonaFriendsList personaFriendsList = new PersonaFriendsList();
		personaFriendsList.setFriendPersona(arrayOfFriendPersona);
		return personaFriendsList;
	}

	private void addPersonaToFriendList(List<FriendPersona> friendPersonaList, PersonaEntity personaEntity, int presence) {
		FriendPersona friendPersona = new FriendPersona();
		friendPersona.setIconIndex(personaEntity.getIconIndex());
		friendPersona.setLevel(personaEntity.getLevel());
		friendPersona.setName(personaEntity.getName());
		friendPersona.setOriginalName(personaEntity.getName());
		friendPersona.setPersonaId(personaEntity.getPersonaId());
		friendPersona.setPresence(presence);
		friendPersona.setSocialNetwork(0);
		friendPersona.setUserId(personaEntity.getUser().getId());
		friendPersonaList.add(friendPersona);
	}

	// Accept or decline the friend request
	public PersonaBase sendResponseFriendRequest(Long personaId, Long friendPersonaId, int resolution) {
		// Execute some DB things
		PersonaEntity personaInvited = personaDAO.findById(personaId);
		PersonaEntity personaSender = personaDAO.findById(friendPersonaId);
		if (personaInvited == null || personaSender == null) {
			return null;
		}

		Long personaSenderId = personaSender.getPersonaId();
		Long personaSenderUser = personaSender.getUser().getId();
		Long personaInvitedId = personaInvited.getPersonaId();
		Long personaInvitedUser = personaInvited.getUser().getId();
		
		if (resolution == 0) {
			removeFriend(personaInvitedId, personaSenderId);
			return null;
		}

		FriendListEntity friendListEntity = friendListDAO.findByOwnerIdAndFriendPersona(personaInvitedUser, personaSenderId);
		if (friendListEntity == null) {
			return null;
		}
		friendListEntity.setIsAccepted(true);
		friendListEntity.setIsBlocked(false);
		friendListDAO.update(friendListEntity);

		// Insert db record for sender player
		friendListEntity = friendListDAO.findByOwnerIdAndFriendPersona(personaSenderUser, personaInvitedId);
		if (friendListEntity == null) {
			createNewFriendListEntry(personaInvitedId, personaSenderUser, personaInvitedUser, true, false);
		}

		// Send all info to personaSender
		FriendPersona friendPersona = new FriendPersona();
		friendPersona.setIconIndex(personaInvited.getIconIndex());
		friendPersona.setLevel(personaInvited.getLevel());
		friendPersona.setName(personaInvited.getName());
		friendPersona.setOriginalName(personaInvited.getName());
		friendPersona.setPersonaId(personaInvitedId);
		friendPersona.setPresence(3);
		friendPersona.setUserId(personaInvitedUser);

		XMPP_FriendResultType friendResultType = new XMPP_FriendResultType();
		friendResultType.setFriendPersona(friendPersona);
		friendResultType.setResult(resolution);

		XMPP_ResponseTypeFriendResult responseTypeFriendResult = new XMPP_ResponseTypeFriendResult();
		responseTypeFriendResult.setFriendResult(friendResultType);

		XmppFriend xmppFriend = new XmppFriend(personaSenderId, openFireSoapBoxCli);
		xmppFriend.sendResponseFriendRequest(responseTypeFriendResult);

		PersonaBase personaBase = new PersonaBase();
		personaBase.setBadges(new ArrayOfBadgePacket());
		personaBase.setIconIndex(personaSender.getIconIndex());
		personaBase.setLevel(personaSender.getLevel());
		personaBase.setMotto(personaSender.getMotto());
		personaBase.setName(personaSender.getName());
		personaBase.setPersonaId(personaSenderId);
		personaBase.setPresence(0);
		personaBase.setScore(personaSender.getScore());
		personaBase.setUserId(personaSenderUser);
		return personaBase;
	}
	
	public FriendResult sendFriendRequest(Long personaId, String displayName, String reqMessage) {
		PersonaEntity personaSender = personaDAO.findById(personaId);
		if (displayName.startsWith("/")) { // Execute custom client commands
			windowCommandsBO.WindowCommandChoice(personaId, personaSender, displayName);
			return null;
		}
		else { // default add-a-friend interaction
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
			createNewFriendListEntry(senderId, invitedUserId, senderUserId, false, false);

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
	}

	public void createNewFriendListEntry(Long friendPersonaId, Long userSenderId, Long userInvitedId, boolean isAccepted, boolean isBlocked) {
		FriendListEntity friendListInsert = new FriendListEntity();
		friendListInsert.setUserOwnerId(userSenderId);
		friendListInsert.setUserId(userInvitedId);
		friendListInsert.setPersonaId(friendPersonaId);
		friendListInsert.setIsAccepted(isAccepted);
		friendListInsert.setIsBlocked(isBlocked);
		friendListDAO.insert(friendListInsert);
	}
	
	public void removeFriend(Long personaId, Long friendPersonaId) {
		PersonaEntity personaInvited = personaDAO.findById(personaId);
		PersonaEntity personaSender = personaDAO.findById(friendPersonaId);
		if (personaInvited == null || personaSender == null) {
			return;
		}

		FriendListEntity friendListEntity = friendListDAO.findByOwnerIdAndFriendPersona(personaInvited.getUser().getId(), personaSender.getPersonaId());
		if (friendListEntity != null) {
			isFriendBlocker(friendListEntity);
		}

		friendListEntity = friendListDAO.findByOwnerIdAndFriendPersona(personaSender.getUser().getId(), personaInvited.getPersonaId());
		if (friendListEntity != null) {
			isFriendBlocker(friendListEntity);
		}
	}
	
	public void isFriendBlocker(FriendListEntity friendListEntity) {
		if (!friendListEntity.getIsBlocked()) {
			friendListDAO.delete(friendListEntity);
		}
		else {
			friendListEntity.setIsAccepted(false);
			friendListDAO.update(friendListEntity);
		}
	}

	public void sendXmppPresence(PersonaEntity personaEntity, int presence, Long to) {
		XMPP_ResponseTypePersonaBase personaPacket = new XMPP_ResponseTypePersonaBase();
		PersonaBase xmppPersonaBase = new PersonaBase();
		xmppPersonaBase.setBadges(new ArrayOfBadgePacket());
		xmppPersonaBase.setIconIndex(personaEntity.getIconIndex());
		xmppPersonaBase.setLevel(personaEntity.getLevel());
		xmppPersonaBase.setMotto(personaEntity.getMotto());
		xmppPersonaBase.setName(personaEntity.getName());
		xmppPersonaBase.setPersonaId(personaEntity.getPersonaId());
		xmppPersonaBase.setPresence(presence);
		xmppPersonaBase.setScore(personaEntity.getScore());
		xmppPersonaBase.setUserId(personaEntity.getUser().getId());
		personaPacket.setPersonaBase(xmppPersonaBase);
		openFireSoapBoxCli.send(personaPacket, to);
	}

	// Update the player status for his friend-list
	public void sendXmppPresenceToAllFriends(PersonaEntity personaEntity, int presence) {
		List<FriendListEntity> friends = friendListDAO.findAcceptedByOwnerId(personaEntity.getUser().getId());
		if (friends != null) {
			for (FriendListEntity friend : friends) {
				if (!personaPresenceDAO.isUserNotOnline(friend.getUserId())) {
					sendXmppPresence(personaEntity, presence, friend.getPersonaId());
				}
			}
		}
	}

//	@Schedule(minute = "*", hour = "*", persistent = false)
	public void updateOfflinePresence() {
		List<TokenSessionEntity> findByActive = tokenSessionDAO.findByActive();
		for (TokenSessionEntity tokenSessionEntity : findByActive) {
			Long activePersonaId = tokenSessionEntity.getActivePersonaId();
			if (!openFireRestApiCli.isOnline(activePersonaId)) {
				PersonaEntity personaEntity = personaBO.getPersonaById(activePersonaId);
				sendXmppPresenceToAllFriends(personaEntity, 0);
				personaPresenceDAO.updatePersonaPresence(activePersonaId, 0);
			}
		}
	}
}
