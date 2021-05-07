package com.soapboxrace.core.bo;

import com.soapboxrace.core.api.util.MiscUtils;
import com.soapboxrace.core.dao.BanDAO;
import com.soapboxrace.core.dao.HardwareInfoDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.RecordsDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.BanEntity;
import com.soapboxrace.core.jpa.HardwareInfoEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;

import com.soapboxrace.core.bo.util.DiscordWebhook;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class AdminBO {
    @EJB
    private TokenSessionBO tokenSessionBo;

    @EJB
    private PersonaDAO personaDao;

    @EJB
    private UserDAO userDao;

    @EJB
    private BanDAO banDAO;

    @EJB
    private HardwareInfoDAO hardwareInfoDAO;

    @EJB
    private OpenFireSoapBoxCli openFireSoapBoxCli;
    
    @EJB
	private DiscordWebhook discordBot;
    
    @EJB
    private RecordsDAO recordsDAO;

    public void sendCommand(Long personaId, Long abuserPersonaId, String command) {
        CommandInfo commandInfo = CommandInfo.parse(command);
        PersonaEntity personaEntity = personaDao.findById(abuserPersonaId);
        PersonaEntity personaEntityAdmin = personaDao.findById(personaId);
        
        String bannedPlayer = personaEntity.getName();
        String adminPlayer = personaEntityAdmin.getName();
        UserEntity userEntity = personaEntity.getUser();
        BanEntity earlierBanEntity = banDAO.findByUser(userEntity);

        if (personaEntity == null) 
            return;

        switch (commandInfo.action) {
            case BAN:
            case BAN_F:
                if (earlierBanEntity != null && !earlierBanEntity.getType().contentEquals("CHAT_BAN")) {
                    openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is already banned."), personaId);
                    break;
                }
                if (earlierBanEntity == null) {
                	sendBan(personaEntity, commandInfo.timeEnd, commandInfo.reason, commandInfo.type);
                }
                if (earlierBanEntity != null && earlierBanEntity.getType().contentEquals("CHAT_BAN")) {
                	changeChatBan(earlierBanEntity, personaEntity, commandInfo.timeEnd, commandInfo.reason, commandInfo.type);
                }
                
                recordsDAO.banRecords(userEntity);
                userDao.ignoreHWBanDisable(userEntity.getId());
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is banned."), personaId);
                String message = ":heavy_minus_sign:"
                		+ "\n:hammer: **|** Nгрок **" + bannedPlayer + "** был забанен модератором **" + adminPlayer + "**. Помянем его."
                		+ "\n:hammer: **|** Player **" + bannedPlayer + "** was banned by moderator **" + adminPlayer + "**. Remember him.";
        		discordBot.sendMessage(message);
                break;
            case KICK:
                sendKick(personaEntity.getPersonaId());
                System.out.println("Player " + personaEntity.getName() + " was kicked, by " + adminPlayer);
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Tactical kick is deployed."), personaId);
                break;
            case IGNORE_HW:
                userDao.ignoreHWBan(userEntity.getId());
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This user is allowed to play with banned HW entrys."), personaId);
                break;
            case CHAT_BAN:
            	BanEntity earlierBannedEntity = banDAO.findByUser(userEntity);
                if (earlierBannedEntity != null) {
                    openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is already banned or chat-banned."), personaId);
                    break;
                }
                if (earlierBannedEntity == null) {
                	sendBan(personaEntity, commandInfo.timeEnd, commandInfo.reason, commandInfo.type);
                }
                
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is chat-banned."), personaId);
                String messageCB = ":heavy_minus_sign:"
                		+ "\n:hammer: **|** Nгроку **" + bannedPlayer + "** был отключён чат модератором **" + adminPlayer + "**."
                		+ "\n:hammer: **|** Player **" + bannedPlayer + "** was chat-banned by moderator **" + adminPlayer + "**.";
        		discordBot.sendMessage(messageCB);
                break;
            case UNBAN:
            	boolean unbanAction = true;
            	if (earlierBanEntity == null) {
                    openFireSoapBoxCli.send(XmppChat.createSystemMessage("### User is not banned, checking HW data."), personaId);
                    unbanAction = false;
                }
            	banDAO.unbanUser(userEntity);
            	recordsDAO.unbanRecords(userEntity);
            	// User can have a lot of HW entries
				List<HardwareInfoEntity> hardwareInfoList = hardwareInfoDAO.findByUserId(userEntity.getId());
				int i = 0; // HW entries total counter
				if (hardwareInfoList == null) { // If the player tried to create new accounts, or if other player have the same HW config
                	openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Unable to find HW entry for user, checking his original HW..."), personaId);
                	HardwareInfoEntity hardwareInfoEntityCheck = hardwareInfoDAO.findByHardwareHash(userEntity.getGameHardwareHash());
                	if (hardwareInfoEntityCheck != null) {
                		hardwareInfoEntityCheck.setBanned(false);
    					hardwareInfoDAO.update(hardwareInfoEntityCheck);
    					i++;
                	}
                	UserEntity bannedGuyEntity = userDao.findById(hardwareInfoEntityCheck.getUserId()); // Detect if the other account is a double-account
                	if (bannedGuyEntity.getPassword().equals(userEntity.getPassword()) || bannedGuyEntity.getIpAddress().equals(userEntity.getIpAddress())) {
                		openFireSoapBoxCli.send(XmppChat.createSystemMessage("### This guy have other accounts, password hash or IP is the same."), personaId);
                	}
				}
				else {
					i = hardwareInfoList.size();
					for (HardwareInfoEntity hwEntry : hardwareInfoList) {
						hwEntry.setBanned(false);
						hardwareInfoDAO.update(hwEntry);
					}
				}
                openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Unban is done, HW entries: " + i + "."), personaId);
                if (unbanAction) {
                	String messageU = ":heavy_minus_sign:"
                    		+ "\n:hammer: **|** Nгрок **" + bannedPlayer + "** был разбанен модератором **" + adminPlayer + "**. С возвращением."
                    		+ "\n:hammer: **|** Player **" + bannedPlayer + "** was unbanned by moderator **" + adminPlayer + "**. Welcome back.";
            		discordBot.sendMessage(messageU);
                }
                break;
            default:
                break;
        }
    }

    // How to use: player report > /ban [time] <reason> (example: /ban 28d14h Any Reason With Spaces)
    // Base code taken from SBRW Apex sources, by HeyItsLeo
    private void sendBan(PersonaEntity personaEntity, LocalDateTime endsOn, String reason, String type) {
        UserEntity userEntity = personaEntity.getUser();
        BanEntity banEntity = new BanEntity();
        banEntity.setUserEntity(userEntity);
        banEntity.setEndsAt(endsOn);
        banEntity.setReason(reason);
        banEntity.setType(type);
        banEntity.setData(userEntity.getEmail());
        banDAO.insert(banEntity);
        userDao.update(userEntity);
        sendKick(personaEntity.getPersonaId());

        if (!type.contentEquals("CHAT_BAN")) {
        	List<HardwareInfoEntity> hardwareInfoList = hardwareInfoDAO.findByUserId(userEntity.getId());
        	if (hardwareInfoList != null) {
        		for (HardwareInfoEntity hwEntry : hardwareInfoList) {
                	Long userId = hwEntry.getUserId();
                	Long userIdOld = hwEntry.getUserIdOld();
                	if (userIdOld != null && userId.intValue() != userIdOld.intValue()) { 
                		continue; // Don't ban the HWs which is used on 2 or more accounts
                    }
                	else {
                		hwEntry.setBanned(true);
                        hardwareInfoDAO.update(hwEntry);
                	}
                }
        	}
        }
    }
    
    // If user already got a Chat-Ban, and the full ban is required
    private void changeChatBan(BanEntity earlierBanEntity, PersonaEntity personaEntity, LocalDateTime endsOn, String reason, String type) {
        UserEntity userEntity = personaEntity.getUser();
        BanEntity banEntity = new BanEntity();
        banEntity.setEndsAt(endsOn);
        banEntity.setReason(reason);
        banEntity.setType(type);
        banDAO.update(earlierBanEntity);
        sendKick(personaEntity.getPersonaId());

        List<HardwareInfoEntity> hardwareInfoList = hardwareInfoDAO.findByUserId(userEntity.getId());
        for (HardwareInfoEntity hwEntry : hardwareInfoList) {
            if (hwEntry != null && (hwEntry.getUserIdOld() == null || hwEntry.getUserIdOld() == hwEntry.getUserId())) { // Don't ban the HWs which is used on 2 or more accounts
            	hwEntry.setBanned(true);
                hardwareInfoDAO.update(hwEntry);
            }
        }
    }

    private void sendKick(Long personaId) {
        openFireSoapBoxCli.send("<NewsArticleTrans><ExpiryTime><", personaId);
    }
    
    public String renamePersonaAdmin(String nickname, String newNickname) {
    	PersonaEntity personaEntity = personaDao.findByName(nickname);
    	PersonaEntity personaEntityCheck = personaDao.findByName(newNickname);
    	if (personaEntity == null) {
    		return "ERROR: wrong nickname";
    	}
    	if (personaEntityCheck != null) {
    		return "ERROR: this nickname is already taken";
    	}
    	personaEntity.setName(newNickname);
    	personaDao.update(personaEntity);
    	recordsDAO.changeRecordsNickname(personaEntity);
    	
    	sendKick(personaEntity.getPersonaId());
    	System.out.println("### Player nickname of "+ nickname +" has been changed to "+ newNickname +".");
		return "Player's nickname has been changed.";
	}

    private static class CommandInfo {
        public CommandInfo.CmdAction action;
        public String reason;
        public LocalDateTime timeEnd;
        public String type;

        public static CommandInfo parse(String cmd) {
            cmd = cmd.replaceFirst("/", "");

            String[] split = cmd.split(" ");
            CommandInfo.CmdAction action;
            CommandInfo info = new CommandInfo();

            switch (split[0].toLowerCase().trim()) {
                case "ban":
                    action = CmdAction.BAN;
                    break;
                case "ban_f":
                    action = CmdAction.BAN_F;
                    break;
                case "ignore_hw":
                    action = CmdAction.IGNORE_HW;
                    break;
                case "kick":
                    action = CmdAction.KICK;
                    break;
                case "chatban":
                    action = CmdAction.CHAT_BAN;
                    break;
                case "unban":
                    action = CmdAction.UNBAN;
                    break;
                default:
                    action = CmdAction.UNKNOWN;
                    break;
            }

            info.action = action;

            switch (action) {
                case BAN: {
                    LocalDateTime endTime = null;
                    String reason = null;

                    if (split.length >= 2) {
                        long givenTime = MiscUtils.lengthToMiliseconds(split[1]);
                        if (givenTime != 0) {
                            endTime = LocalDateTime.now().plusSeconds(givenTime / 1000);
                            info.timeEnd = endTime;

                            if (split.length > 2) {
                                reason = MiscUtils.argsToString(split, 2, split.length);
                            }
                        } 
                    }

                    info.reason = reason;
                    info.type = "EMAIL_BAN";
                    break;
                }
                case CHAT_BAN: {
                    LocalDateTime endTime = null;
                    String reason = null;

                    if (split.length >= 2) {
                        long givenTime = MiscUtils.lengthToMiliseconds(split[1]);
                        if (givenTime != 0) {
                            endTime = LocalDateTime.now().plusSeconds(givenTime / 1000);
                            info.timeEnd = endTime;

                            if (split.length > 2) {
                                reason = MiscUtils.argsToString(split, 2, split.length);
                            }
                        } 
                    }

                    info.reason = reason;
                    info.type = "CHAT_BAN";
                    break;
                }
                case BAN_F: {
                    String reason = MiscUtils.argsToString(split, 1, split.length);
                    LocalDateTime endTime = LocalDateTime.of(3000, 01, 01, 01, 23, 45); // WEv2 ban until Futurama starts
                    info.timeEnd = endTime;
                    info.reason = reason;
                    info.type = "EMAIL_BAN";
                    break;
                }
            }

            return info;
        }

        public enum CmdAction {
            KICK,
            BAN,
            BAN_F,
            UNBAN,
            IGNORE_HW,
            CHAT_BAN,
            UNKNOWN
        }
    }
}