package com.soapboxrace.core.bo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.dao.InviteTicketDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ServerInfoDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.InviteTicketEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.core.xmpp.OpenFireRestApiCli;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.ArrayOfProfileData;
import com.soapboxrace.jaxb.http.FriendResult;
import com.soapboxrace.jaxb.http.ProfileData;
import com.soapboxrace.jaxb.http.User;
import com.soapboxrace.jaxb.http.UserInfo;
import com.soapboxrace.jaxb.login.LoginStatusVO;

@Stateless
public class UserBO {

	@EJB
	private UserDAO userDao;

	@EJB
	private InviteTicketDAO inviteTicketDAO;

	@EJB
	private ServerInfoDAO serverInfoDAO;

	@EJB
	private OpenFireRestApiCli xmppRestApiCli;

	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private PersonaDAO personaDAO;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private AchievementsBO achievementsBO;
	
	@EJB
	private TokenSessionDAO tokenDAO;

	public void createXmppUser(UserInfo userInfo) {
		String securityToken = userInfo.getUser().getSecurityToken();
		String xmppPasswd = securityToken.substring(0, 16);
		List<ProfileData> profileData = userInfo.getPersonas().getProfileData();
		for (ProfileData persona : profileData) {
			createXmppUser(persona.getPersonaId(), xmppPasswd);
		}
	}

	public void createXmppUser(Long personaId, String xmppPasswd) {
		xmppRestApiCli.createUpdatePersona(personaId, xmppPasswd);
	}

	public UserEntity createUser(String email, String passwd) {
		UserEntity userEntity = new UserEntity();
		email = email.toLowerCase();
		userEntity.setEmail(email);
		userEntity.setPassword(passwd);
		userEntity.setCreated(LocalDateTime.now());
		userEntity.setLastLogin(LocalDateTime.now());
		userDao.insert(userEntity);
		return userEntity;
	}

	public LoginStatusVO createUserWithTicket(String email, String passwd, String ticket) {
		LoginStatusVO loginStatusVO = new LoginStatusVO(0L, "", false);
		InviteTicketEntity inviteTicketEntity = new InviteTicketEntity();
		inviteTicketEntity.setTicket("empty-ticket");
		String ticketToken = parameterBO.getStrParam("TICKET_TOKEN");
		if (ticketToken != null && !ticketToken.equals("null")) {
			inviteTicketEntity = inviteTicketDAO.findByTicket(ticket);
			if (inviteTicketEntity == null || inviteTicketEntity.getTicket() == null || inviteTicketEntity.getTicket().isEmpty()) {
				loginStatusVO.setDescription("Registration Error: Invalid Ticket!");
				return loginStatusVO;
			}
			if (inviteTicketEntity.getUser() != null) {
				loginStatusVO.setDescription("Registration Error: Ticket already in use!");
				return loginStatusVO;
			}
		}
		UserEntity userEntityTmp = userDao.findByEmail(email);
		if (userEntityTmp != null) {
			if (userEntityTmp.getEmail() != null) {
				loginStatusVO.setDescription("Registration Error: Email already exists!");
				return loginStatusVO;
			}
		}
		UserEntity userEntity = createUser(email, passwd);
		inviteTicketEntity.setUser(userEntity);
		inviteTicketDAO.insert(inviteTicketEntity);
		loginStatusVO = new LoginStatusVO(userEntity.getId(), "", true);
		serverInfoDAO.updateNumberOfRegistered();
		return loginStatusVO;
	}

	public UserInfo secureLoginPersona(Long userId, Long personaId) {
		UserInfo userInfo = new UserInfo();
		userInfo.setPersonas(new ArrayOfProfileData());
		com.soapboxrace.jaxb.http.User user = new com.soapboxrace.jaxb.http.User();
		user.setUserId(userId);
		userInfo.setUser(user);
		return userInfo;
	}

	public UserInfo getUserById(Long userId) {
		UserEntity userEntity = userDao.findById(userId);
		UserInfo userInfo = new UserInfo();
		ArrayOfProfileData arrayOfProfileData = new ArrayOfProfileData();
		List<PersonaEntity> listOfProfile = userEntity.getListOfProfile();
		for (PersonaEntity personaEntity : listOfProfile) {
			if (personaEntity.isHidden()) {
				continue; // Hidden persona is excluded
			}
			// switch to apache beanutils copy
			ProfileData profileData = new ProfileData();
			profileData.setName(personaEntity.getName());
			profileData.setCash(personaEntity.getCash());
			profileData.setBoost(userEntity.getBoost());
			profileData.setIconIndex(personaEntity.getIconIndex());
			profileData.setPersonaId(personaEntity.getPersonaId());
			profileData.setLevel(personaEntity.getLevel());
			arrayOfProfileData.getProfileData().add(profileData);
			int days = (int) ChronoUnit.DAYS.between(personaEntity.getCreated(), LocalDateTime.now());
			achievementsBO.applyDriverAgeAchievement(personaEntity, days);
		}
		userInfo.setPersonas(arrayOfProfileData);
		User user = new User();
		user.setUserId(userId);
		userInfo.setUser(user);
		return userInfo;
	}
	
	// Send persona's money to another persona (/SENDMONEY nickName money)
	// FriendResult allows to stop the function with "return null", since it's called from FriendBO
	public FriendResult sendMoney(PersonaEntity personaEntity, String displayName) {
		Long personaId = personaEntity.getPersonaId();
		if (!parameterBO.getBoolParam("MONEY_TRANSFER")) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Money Transfers is disabled."), personaId);
			return null;
		}
		String entryValue = displayName.replaceFirst("/SENDMONEY ", "");
        String[] values = entryValue.split(" ");
        
        String entryName = values[0].toString(); // Nickname value
        int entryCash = 0;
        try {
        	entryCash = Integer.parseInt(values[1].toString()); // Cash value
        } catch (NumberFormatException|ArrayIndexOutOfBoundsException ex) {
        	openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Money number is invaild, try again."), personaId);
        	return null;
        }
        if (entryCash < 1) {
    		openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Unable to send a nothing."), personaId);
    		return null;
        }
        
        // Sender's info
		UserEntity userEntitySender = personaEntity.getUser();
		int personaMoneySender = personaEntity.getCash();
		int moneyGivenAlready = userEntitySender.getMoneyGiven();
		boolean isStaff = userEntitySender.isAdmin();
		int levelCap = parameterBO.getIntParam("SENDMONEY_LEVELCAP");
        
		boolean premiumStatusSender = userEntitySender.isPremium();
		int sendLimit = 0;
		if (!premiumStatusSender) {
			sendLimit = parameterBO.getIntParam("MAX_SENDMONEY_FREE");
		}
		if (premiumStatusSender) {
			sendLimit = parameterBO.getIntParam("MAX_SENDMONEY_PREMIUM");
		}
		if (moneyGivenAlready >= sendLimit && !isStaff) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Unable to send money - transaction limit is already reached.\n"
					+ "## Limit resets every Monday."), personaId);
			return null;
		}
		if (entryCash > personaMoneySender) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Value is bigger than your money, check the value and try again."), personaId);
			return null;
		}
		if (entryCash > sendLimit && !isStaff) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Cannot send more than the limit ($" + sendLimit + " per week."), personaId);
			return null;
		}
		if (personaEntity.getLevel() < levelCap) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### To send money, you should have Level " + levelCap + " or higher."), personaId);
			return null;
		}
		
		PersonaEntity personaEntityTarget = personaDAO.findByName(entryName);
		if (personaEntityTarget == null) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Wrong nickname, check the name and try again."), personaId);
			return null;
		}
		if (personaEntityTarget.getName().contentEquals(personaEntity.getName())) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You already have your own money."), personaId);
			return null;
		}
		
		else {
			// Target player's info
			int personaMoneyTarget = personaEntityTarget.getCash();
			int moneyLimit = parameterBO.getMaxCash();
			int moneyDiff = 0;

			if (personaMoneyTarget >= moneyLimit) {
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This player cannot get more money."), personaId);
				return null;
			}
			
			else {
				// If the money value is greater than available send amount, we change it to the max from that amount
				if (!isStaff) {
					int givenMoneyDiff = sendLimit - moneyGivenAlready;
					if (entryCash > givenMoneyDiff) { 
						entryCash = givenMoneyDiff;
					}
				}
				int personaMoneyTargetNew = personaMoneyTarget + entryCash;
				if (personaMoneyTargetNew > moneyLimit) {
					personaMoneyTargetNew = moneyLimit;
				}
				
				moneyDiff = personaMoneyTargetNew - personaMoneyTarget;
				personaEntityTarget.setCash(personaMoneyTargetNew);
				personaEntity.setCash(personaMoneySender - moneyDiff);
				int moneyGivenFinal = moneyGivenAlready + moneyDiff;
				if (isStaff) {
					moneyGivenFinal = 0;
				}
				userEntitySender.setMoneyGiven(moneyGivenFinal);
				int moneyGivenFinal2 = sendLimit - moneyGivenFinal;
				
				personaDAO.update(personaEntityTarget);
				personaDAO.update(personaEntity);
				userDao.update(userEntitySender);
				
				String senderName = personaEntity.getName();
				String targetName = personaEntityTarget.getName();
				openFireSoapBoxCli.send(XmppChat.createSystemMessage("### $" + (int) moneyDiff + " has been sent to this persona.\n"
						+ "## You can still send $" + moneyGivenFinal2 + " on this week."), personaId);
				String message = ":heavy_minus_sign:"
		        		+ "\n:money_with_wings: **|** Nгрок **" + senderName + "** отправил **$" + (int) moneyDiff + "** игроку **" + targetName + "**."
		        		+ "\n:money_with_wings: **|** Player **" + senderName + "** has sent **$" + (int) moneyDiff + "** to player **" + targetName + "**.";
				discordBot.sendMessage(message);
				System.out.println("Player " + senderName + " has sent $" + (int) moneyDiff + " to player " + targetName + "(Before: " + personaMoneyTarget + 
						", after: " + personaMoneyTargetNew + ")");
			}
		}
		return null;
	}
	
	// Get extra reserve money to current persona - re-fill the persona's cash account
	// Obsolete, only for old players
	public void getMoney(PersonaEntity personaEntity) {
		UserEntity userEntity = personaEntity.getUser();
		int extraMoneyCur = userEntity.getExtraMoney(); // Orig. full value
		int personaMoney = personaEntity.getCash();
		Long personaId = personaEntity.getPersonaId();
		
		int extraMoneyLimited = extraMoneyCur; // Value with cash limit applied
		int moneyLimit = parameterBO.getMaxCash();
		int moneyDiff = 0;
		
		if (extraMoneyCur == 0) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### You cannot have any money on the WeBank."), personaId);
		}
		if (personaMoney >= moneyLimit) {
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Your money limit is reached already."), personaId);
		}
		else { // Transaction
			if (extraMoneyCur > moneyLimit) {
				extraMoneyLimited = moneyLimit;
			}
			int personaMoneyNew = personaMoney + extraMoneyLimited;
			if (personaMoneyNew > moneyLimit) {
				personaMoneyNew = moneyLimit;
			}
			moneyDiff = personaMoneyNew - personaMoney;
			userEntity.setExtraMoney(extraMoneyCur - moneyDiff);
			
			personaEntity.setCash(personaMoneyNew);
			personaDAO.update(personaEntity);
			userDao.update(userEntity);
			
			String senderName = personaEntity.getName();
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### $" + (int) moneyDiff + " has been added to your cash account.\n"
					+ "## Current WeBank money amount: $" + (int) userEntity.getExtraMoney() + "\n"
					+ "## Please re-login into account."), personaId);
			System.out.println("Player " + senderName + " has taken $" + (int) moneyDiff + " from his Bank account.");
		}
	}
	
	// Alternative Freeroam sync module (experimental)
	public boolean isFRSyncAlt(String securityToken) {
		if (securityToken == null) {return false;}
		Long userId = tokenDAO.findBySecurityToken(securityToken).getUserId();
		UserEntity userEntity = userDao.findById(userId);
		if (userEntity == null) {return false;}
		return userEntity.getFRSyncAlt();
	}
}
