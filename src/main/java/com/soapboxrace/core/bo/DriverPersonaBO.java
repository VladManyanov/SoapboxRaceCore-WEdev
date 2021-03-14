package com.soapboxrace.core.bo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.AchievementPersonaDAO;
import com.soapboxrace.core.dao.AchievementStateDAO;
import com.soapboxrace.core.dao.CarSlotDAO;
import com.soapboxrace.core.dao.FriendListDAO;
import com.soapboxrace.core.dao.InventoryDAO;
import com.soapboxrace.core.dao.InventoryItemDAO;
import com.soapboxrace.core.dao.LobbyEntrantDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaPresenceDAO;
import com.soapboxrace.core.dao.RecordsDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.dao.TreasureHuntDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.AchievementDefinitionEntity;
import com.soapboxrace.core.jpa.AchievementRankEntity;
import com.soapboxrace.core.jpa.BadgeDefinitionEntity;
import com.soapboxrace.core.jpa.BadgePersonaEntity;
import com.soapboxrace.core.jpa.CarSlotEntity;
import com.soapboxrace.core.jpa.FriendListEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.TreasureHuntEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.core.xmpp.OpenFireRestApiCli;
import com.soapboxrace.jaxb.http.ArrayOfBadgePacket;
import com.soapboxrace.jaxb.http.ArrayOfPersonaBase;
import com.soapboxrace.jaxb.http.ArrayOfString;
import com.soapboxrace.jaxb.http.BadgePacket;
import com.soapboxrace.jaxb.http.PersonaBase;
import com.soapboxrace.jaxb.http.ProfileData;

@Stateless
public class DriverPersonaBO {

	@EJB
	private UserDAO userDao;

	@EJB
	private PersonaDAO personaDao;

	@EJB
	private LobbyEntrantDAO lobbyEntrantDAO;

	@EJB
	private CarSlotDAO carSlotDAO;

	@EJB
	private TreasureHuntDAO treasureHuntDAO;

	@EJB
	private InventoryDAO inventoryDAO;

	@EJB
	private InventoryItemDAO inventoryItemDAO;

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private InventoryBO inventoryBO;

	@EJB
	private TokenSessionDAO tokenSessionDAO;

	@EJB
	private AchievementPersonaDAO achievementPersonaDAO;

	@EJB
	private AchievementStateDAO achievementStateDAO;

	@EJB
	private OpenFireRestApiCli openFireRestApiCli;
	
	@EJB
	private RecordsDAO recordsDAO;
	
	@EJB
	private PersonaPresenceDAO personaPresenceDAO;
	
	@EJB
	private FriendListDAO friendListDAO;

	public ProfileData createPersona(Long userId, PersonaEntity personaEntity) {
		UserEntity userEntity = userDao.findById(userId);

		int size = 0;
		for (PersonaEntity persona : userEntity.getListOfProfile()) {
			if (!persona.isHidden()) { // Don't count "hidden" drivers
				size++;
			}
		}
		if (size >= 3) {
			return null;
		}
		// Don't let the HEX cheaters get the player-exclusive avatars
		if (personaEntity.getIconIndex() < 0 || personaEntity.getIconIndex() > parameterBO.getIntParam("MAX_PLAYERICON_INDEX")) {
			return null;
        }

		personaEntity.setUser(userEntity);
		personaEntity.setCash(parameterBO.getIntParam("STARTING_CASH_AMOUNT"));
		personaEntity.setLevel(parameterBO.getIntParam("STARTING_LEVEL_NUMBER"));
		personaEntity.setCreated(LocalDateTime.now());
		personaEntity.setHidden(false);
		personaEntity.setIgnoreRaces(true);
		personaEntity.setPriorityMMTimeout(parameterBO.getIntParam("RACENOW_PRIORITYTIMER"));
		personaDao.insert(personaEntity);

		inventoryBO.createInventory(personaEntity);
		createThInformation(personaEntity);

		return castPersonaEntity(personaEntity);
	}

	private ProfileData castPersonaEntity(PersonaEntity personaEntity) {
		ProfileData profileData = new ProfileData();
		UserEntity userEntity = personaEntity.getUser();
		// switch to apache beanutils copy
		profileData.setName(personaEntity.getName());
		profileData.setCash(personaEntity.getCash());
		profileData.setBoost(userEntity.getBoost());
		profileData.setIconIndex(personaEntity.getIconIndex());
		profileData.setPersonaId(personaEntity.getPersonaId());
		profileData.setLevel(personaEntity.getLevel());
		return profileData;
	}

	public ProfileData getPersonaInfo(Long personaId) {
		PersonaEntity personaEntity = personaDao.findById(personaId);
		ProfileData profileData = castPersonaEntity(personaEntity);
		List<BadgePersonaEntity> listOfBadges = personaEntity.getListOfBadges();
		ArrayOfBadgePacket arrayOfBadgePacket = new ArrayOfBadgePacket();
		List<BadgePacket> badgePacketList = arrayOfBadgePacket.getBadgePacket();
		for (BadgePersonaEntity badgePersonaEntity : listOfBadges) {
			BadgePacket badgePacket = new BadgePacket();
			AchievementRankEntity achievementRank = badgePersonaEntity.getAchievementRank();
			AchievementDefinitionEntity achievementDefinition = achievementRank.getAchievementDefinition();
			BadgeDefinitionEntity badgeDefinition = achievementDefinition.getBadgeDefinition();
			badgePacket.setBadgeDefinitionId(badgeDefinition.getId().intValue());
			badgePacket.setIsRare(false);
			badgePacket.setRarity(0f);
			badgePacket.setSlotId(badgePersonaEntity.getSlot());
			badgePacket.setAchievementRankId(achievementRank.getId().intValue());
			badgePacketList.add(badgePacket);
		}

		profileData.setBadges(arrayOfBadgePacket);
		profileData.setMotto(personaEntity.getMotto());
		profileData.setPercentToLevel(personaEntity.getPercentToLevel());
		profileData.setRating(personaEntity.getRating());
		profileData.setRep(personaEntity.getRep());
		profileData.setRepAtCurrentLevel(personaEntity.getRepAtCurrentLevel());
		profileData.setScore(personaEntity.getScore());
		return profileData;
	}

	public ArrayOfPersonaBase getPersonaBaseFromList(List<Long> personaIdList) {
		ArrayOfPersonaBase arrayOfPersonaBase = new ArrayOfPersonaBase();
		for (Long personaId : personaIdList) {
			PersonaEntity personaEntity = personaDao.findById(personaId);
			if (personaEntity == null) {
				return arrayOfPersonaBase;
			}
			PersonaBase personaBase = new PersonaBase();
			List<BadgePersonaEntity> listOfBadges = personaEntity.getListOfBadges();
			ArrayOfBadgePacket arrayOfBadgePacket = new ArrayOfBadgePacket();
			List<BadgePacket> badgePacketList = arrayOfBadgePacket.getBadgePacket();
			for (BadgePersonaEntity badgePersonaEntity : listOfBadges) {
				BadgePacket badgePacket = new BadgePacket();
				AchievementRankEntity achievementRank = badgePersonaEntity.getAchievementRank();
				AchievementDefinitionEntity achievementDefinition = achievementRank.getAchievementDefinition();
				BadgeDefinitionEntity badgeDefinition = achievementDefinition.getBadgeDefinition();
				badgePacket.setBadgeDefinitionId(badgeDefinition.getId().intValue());
				badgePacket.setIsRare(false);
				badgePacket.setRarity(0f);
				badgePacket.setSlotId(badgePersonaEntity.getSlot());
				badgePacket.setAchievementRankId(achievementRank.getId().intValue());
				badgePacketList.add(badgePacket);
			}
			personaBase.setBadges(arrayOfBadgePacket);
			personaBase.setIconIndex(personaEntity.getIconIndex());
			personaBase.setLevel(personaEntity.getLevel());
			personaBase.setMotto(personaEntity.getMotto());
			personaBase.setName(personaEntity.getName());
			personaBase.setPresence(1);
			personaBase.setPersonaId(personaEntity.getPersonaId());
			personaBase.setScore(personaEntity.getScore());
			personaBase.setUserId(personaEntity.getUser().getId());
			arrayOfPersonaBase.getPersonaBase().add(personaBase);
		}
		return arrayOfPersonaBase;
	}
	
	public PersonaBase getPersonaBase(PersonaEntity personaEntity) {
        PersonaBase personaBase = new PersonaBase();
        List<BadgePersonaEntity> listOfBadges = personaEntity.getListOfBadges();
		ArrayOfBadgePacket arrayOfBadgePacket = new ArrayOfBadgePacket();
		List<BadgePacket> badgePacketList = arrayOfBadgePacket.getBadgePacket();
		for (BadgePersonaEntity badgePersonaEntity : listOfBadges) {
			BadgePacket badgePacket = new BadgePacket();
			AchievementRankEntity achievementRank = badgePersonaEntity.getAchievementRank();
			AchievementDefinitionEntity achievementDefinition = achievementRank.getAchievementDefinition();
			BadgeDefinitionEntity badgeDefinition = achievementDefinition.getBadgeDefinition();
			badgePacket.setBadgeDefinitionId(badgeDefinition.getId().intValue());
			badgePacket.setIsRare(false);
			badgePacket.setRarity(0f);
			badgePacket.setSlotId(badgePersonaEntity.getSlot());
			badgePacket.setAchievementRankId(achievementRank.getId().intValue());
			badgePacketList.add(badgePacket);
		}
		personaBase.setBadges(arrayOfBadgePacket);
        personaBase.setIconIndex(personaEntity.getIconIndex());
        personaBase.setLevel(personaEntity.getLevel());
        personaBase.setMotto(personaEntity.getMotto());
        personaBase.setName(personaEntity.getName());
        personaBase.setPresence(1);
        personaBase.setPersonaId(personaEntity.getPersonaId());
        personaBase.setScore(personaEntity.getScore());
        personaBase.setUserId(personaEntity.getUser().getId());
        return personaBase;
    }

	public void deletePersona(Long personaId) {
		PersonaEntity personaEntity = personaDao.findById(personaId);
		List<CarSlotEntity> carSlots = carSlotDAO.findByPersonaId(personaId);
		for (CarSlotEntity carSlotEntity : carSlots) {
			carSlotDAO.delete(carSlotEntity);
		}
		
		deletePersonaFriendList(personaId, personaEntity.getUser().getId());
		recordsDAO.deletePersonaRecords(personaEntity);
		carSlotDAO.deleteByPersona(personaEntity);
		lobbyEntrantDAO.deleteByPersona(personaEntity);
		treasureHuntDAO.deleteByPersona(personaEntity.getPersonaId());
		inventoryItemDAO.deleteByPersona(personaId);
		inventoryDAO.deleteByPersona(personaId);
		achievementPersonaDAO.deleteByPersona(personaId);
		achievementStateDAO.deleteByPersona(personaId);
		personaDao.delete(personaEntity);
	}
	
	// Works as a recycle bin - holds a drivers for some time, in case of player's mistakes
	public void deletePersonaTemp(Long personaId) {
		PersonaEntity personaEntity = personaDao.findById(personaId);
		
		deletePersonaFriendList(personaId, personaEntity.getUser().getId());
		personaEntity.setTeam(null);
		personaEntity.setName(personaEntity.getName() + "_TD");
		personaEntity.setCreated(LocalDateTime.now()); // can check when driver got deleted
		recordsDAO.deletePersonaRecords(personaEntity);
		personaEntity.setHidden(true); // Hides the persona from user and other players
		personaDao.update(personaEntity);
	}
	
	public void deletePersonaFriendList(Long personaId, Long userId) {
		friendListDAO.deleteByPersona(personaId); // Delete all B-side friend entries
		List<FriendListEntity> friendList = friendListDAO.findByOwnerId(userId); // Get the user friend-list
		FriendListEntity checkFriendEntity = null;
		for (FriendListEntity friendEntity : friendList) {
			checkFriendEntity = friendListDAO.findByOwnerIdAndFriendPersona(userId, friendEntity.getPersonaId());
			if (checkFriendEntity == null) { // Check for non-removed A-side friend entries
				friendListDAO.delete(friendEntity);
			}
		}
	}

	public void updateStatusMessage(String message, Long personaId) {
		PersonaEntity personaEntity = personaDao.findById(personaId);
		personaEntity.setMotto(message);
		personaDao.update(personaEntity);
	}

	public ArrayOfString reserveName(String name) {
		ArrayOfString arrayOfString = new ArrayOfString();
		if (personaDao.findByName(name) != null) {
			arrayOfString.getString().add("NONE"); // TODO Random nickname generation?
		}
		return arrayOfString;
	}

	public void createThInformation(PersonaEntity personaEntity) {
		TreasureHuntEntity treasureHuntEntity = new TreasureHuntEntity();
		treasureHuntEntity.setCoinsCollected(0);
		treasureHuntEntity.setIsStreakBroken(false);
		treasureHuntEntity.setNumCoins(15);
		treasureHuntEntity.setPersonaId(personaEntity.getPersonaId());
		treasureHuntEntity.setReviveCount(0);
		treasureHuntEntity.setSeed(-1142185119);
		treasureHuntEntity.setStreak(1);
		treasureHuntEntity.setThDate(LocalDate.now());
		treasureHuntDAO.insert(treasureHuntEntity);
	}

}
