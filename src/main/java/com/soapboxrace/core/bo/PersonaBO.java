package com.soapboxrace.core.bo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.bo.util.OwnedCarConverter;
import com.soapboxrace.core.dao.AchievementDAO;
import com.soapboxrace.core.dao.AchievementRankDAO;
import com.soapboxrace.core.dao.AchievementStateDAO;
import com.soapboxrace.core.dao.BadgeDefinitionDAO;
import com.soapboxrace.core.dao.BadgePersonaDAO;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.CarSlotDAO;
import com.soapboxrace.core.dao.LevelRepDAO;
import com.soapboxrace.core.dao.OwnedCarDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.AchievementDefinitionEntity;
import com.soapboxrace.core.jpa.AchievementRankEntity;
import com.soapboxrace.core.jpa.AchievementStateEntity;
import com.soapboxrace.core.jpa.BadgePersonaEntity;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.CarSlotEntity;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.LevelRepEntity;
import com.soapboxrace.core.jpa.OwnedCarEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.jaxb.http.BadgeBundle;
import com.soapboxrace.jaxb.http.BadgeInput;
import com.soapboxrace.jaxb.http.OwnedCarTrans;

@Stateless
public class PersonaBO {

	@EJB
	private PersonaDAO personaDAO;

	@EJB
	private CarSlotDAO carSlotDAO;

	@EJB
	private LevelRepDAO levelRepDAO;

	@EJB
	private OwnedCarDAO ownedCarDAO;

	@EJB
	private BadgeDefinitionDAO badgeDefinitionDAO;

	@EJB
	private BadgePersonaDAO badgePersonaDAO;

	@EJB
	private AchievementStateDAO achievementStateDAO;
	
	@EJB
	private AchievementDAO achievementDAO;
	
	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
	private CommerceBO commerceBO;
	
	@EJB
	private AchievementRankDAO achievementRankDAO;
	
	@EJB
	private DriverPersonaBO driverPersonaBO;
	
	@EJB
	private DiscordWebhook discordBot;

	public void changeDefaultCar(Long personaId, Long defaultCarId) {
		PersonaEntity personaEntity = personaDAO.findById(personaId);
		List<CarSlotEntity> carSlotList = carSlotDAO.findByPersonaId(personaId);
		int i = 0;
		for (CarSlotEntity carSlotEntity : carSlotList) {
			if (carSlotEntity.getOwnedCar().getId().equals(defaultCarId)) {
				break;
			}
			i++;
		}
		personaEntity.setCurCarIndex(i);
		personaDAO.update(personaEntity);
	}

	public PersonaEntity getPersonaById(Long personaId) {
		return personaDAO.findById(personaId);
	}

	public CarSlotEntity getDefaultCarEntity(Long personaId) {
		PersonaEntity personaEntity = personaDAO.findById(personaId);
		// if (personaEntity == null) {
		// personaEntity = personaDAO.findById(100l);
		// }
		int carSlotCount = carSlotDAO.countPersonaCars(personaId).intValue();
		Integer curCarIndex = personaEntity.getCurCarIndex();
		if (carSlotCount > 0) {
			if (curCarIndex >= carSlotCount) {
				 curCarIndex = carSlotCount - 1;
	             personaEntity.setCurCarIndex(curCarIndex);
	             personaDAO.update(personaEntity);
			}
			CarSlotEntity carSlotEntity = carSlotDAO.findCarByPersonaId(personaEntity, curCarIndex);
			CustomCarEntity customCar = carSlotEntity.getOwnedCar().getCustomCar();
			customCar.getPaints().size();
			customCar.getPerformanceParts().size();
			customCar.getSkillModParts().size();
			customCar.getVisualParts().size();
			customCar.getVinyls().size();
			return carSlotEntity;
		}
		return null;
	}
	
	public int getCurrentPlayerCarClass(Long personaId) {
		PersonaEntity personaEntity = personaDAO.findById(personaId);
		List<CarSlotEntity> carSlotList = getPersonasCar(personaId);
		Integer curCarIndex = personaEntity.getCurCarIndex();
		if (!carSlotList.isEmpty()) {
			if (curCarIndex >= carSlotList.size()) {
				curCarIndex = carSlotList.size() - 1;
				CarSlotEntity ownedCarEntity = carSlotList.get(curCarIndex);
				changeDefaultCar(personaId, ownedCarEntity.getId());
			}
			CarSlotEntity carSlotEntity = carSlotList.get(curCarIndex);
			CustomCarEntity customCar = carSlotEntity.getOwnedCar().getCustomCar();
			return customCar.getCarClassHash();
		}
		return 0;
	}

	public OwnedCarTrans getDefaultCar(Long personaId) {
		CarSlotEntity carSlotEntity = getDefaultCarEntity(personaId);
		if (carSlotEntity == null) {
			return new OwnedCarTrans();
		}
		OwnedCarEntity ownedCarEntity = carSlotEntity.getOwnedCar();
		CustomCarEntity customCarEntityVer = ownedCarEntity.getCustomCar();
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(customCarEntityVer.getPhysicsProfileHash());
		// Re-calc class & rating
		if (ownedCarEntity.getCarVersion() != carClassesEntity.getCarVersion() || customCarEntityVer.getRating() == 0) {
			ownedCarEntity.setCarVersion(carClassesEntity.getCarVersion());
			commerceBO.calcNewCarClass(customCarEntityVer);
		}
        OwnedCarTrans ownedCarTrans = OwnedCarConverter.entity2Trans(carSlotEntity.getOwnedCar());
		return ownedCarTrans;
	}

	public List<CarSlotEntity> getPersonasCar(Long personaId) {
		return carSlotDAO.findByPersonaId(personaId);
	}

	public LevelRepEntity getLevelInfoByLevel(Long level) {
		return levelRepDAO.findByLevel(level);
	}

	public OwnedCarEntity getCarByOwnedCarId(Long ownedCarId) {
		OwnedCarEntity ownedCarEntity = ownedCarDAO.findById(ownedCarId);
		CustomCarEntity customCar = ownedCarEntity.getCustomCar();
		customCar.getPaints().size();
		customCar.getPerformanceParts().size();
		customCar.getSkillModParts().size();
		customCar.getVisualParts().size();
		customCar.getVinyls().size();
		return ownedCarEntity;
	}

	// Install new badges from the game client request
	// Note: to properly recognize the custom achievement ranks, we should resolve the current rank manually
	public void updateBadges(Long activePersonaId, BadgeBundle badgeBundle) {
		PersonaEntity persona = personaDAO.findById(activePersonaId);
		List<BadgePersonaEntity> listOfBadges = new ArrayList<>();
		List<BadgeInput> badgeInputs = badgeBundle.getBadgeInputs();
		for (BadgeInput badgeInput : badgeInputs) {
			int idCheck = badgeInput.getBadgeDefinitionId();
			AchievementDefinitionEntity achievementDefinitionEntity = achievementDAO.findByBadgeId((long) idCheck);		
			AchievementStateEntity achievementStateEntity = achievementStateDAO.findByPersonaBadge(persona, (long) idCheck); // Ordering by IDs to get the highest current rank
			AchievementRankEntity achievementRank = achievementStateEntity.getAchievementRank();
			BadgePersonaEntity badgePersonaEntity = new BadgePersonaEntity();
			badgePersonaEntity.setSlot(badgeInput.getSlotId());
			badgePersonaEntity.setPersona(persona);
			badgePersonaEntity.setAchievementRank(achievementRank);
			badgePersonaEntity.setAchievementId(achievementDefinitionEntity);
			listOfBadges.add(badgePersonaEntity);
		}
		badgePersonaDAO.deleteByPersona(persona);
		persona.setListOfBadges(listOfBadges);
		personaDAO.update(persona);
	}
	
	// Update the existing badges on DB, when the player has progress on them
	public void updateBadgesProgress(Long activePersonaId, AchievementDefinitionEntity achievementDefinitionEntity, 
			AchievementStateEntity achievementStateEntity, short slot) {
		PersonaEntity persona = personaDAO.findById(activePersonaId);
		AchievementRankEntity achievementRank = achievementStateEntity.getAchievementRank();
		BadgePersonaEntity badgePersonaEntity = new BadgePersonaEntity();
		badgePersonaEntity.setSlot(slot);
		badgePersonaEntity.setPersona(persona);
		badgePersonaEntity.setAchievementRank(achievementRank);
		badgePersonaEntity.setAchievementId(achievementDefinitionEntity);
		badgePersonaDAO.deleteByPersonaSlot(persona, slot);
		badgePersonaDAO.update(badgePersonaEntity);
	}
	
	// Delete the temp-hidden drivers after 30 days
	@Schedule(dayOfWeek = "MON", persistent = false)
	public void tempHiddenPersonaDeletion() {
		List<PersonaEntity> hiddenPersonas = personaDAO.findAllHiddenDrivers();
		int i = 0;
		if (hiddenPersonas != null) {
			for (PersonaEntity hiddenPersona : hiddenPersonas) { 
				// "Created" time field was changed to reflect the temp-removal date
				Long days = ChronoUnit.DAYS.between(LocalDateTime.now(), hiddenPersona.getCreated().plusDays(30));
				if (days <= 0) {
					driverPersonaBO.deletePersona(hiddenPersona.getPersonaId());
					i++;
				}
			}
		}
		System.out.println("### Persona Temp Storage clean-up: " + i + " drivers was permanently deleted.");
		String message = ":heavy_minus_sign:"
        		+ "\n:x: **|** Было окончательно удалено " + i + " водителей из хранилища скрытых водителей."
        		+ "\n:x: **|** " + i + " drivers from Hidden Drivers Storage were permamently deleted.";
		discordBot.sendMessage(message);
	}
	
	
	// Version with Extra-LVL achievement code & slot lock, unused
//	public void updateBadges(Long activePersonaId, BadgeBundle badgeBundle) {
//		PersonaEntity persona = personaDAO.findById(activePersonaId);
//		List<BadgePersonaEntity> listOfBadges = new ArrayList<>();
//		List<BadgeInput> badgeInputs = badgeBundle.getBadgeInputs();
//		boolean slot0IsLocked = false;
//		AchievementRankEntity achievementRankEntityCheck = new AchievementRankEntity();
//		// Extra-Level achiv. must be not removeable
//		List<BadgePersonaEntity> listToCheck = persona.getListOfBadges();
//		for (BadgePersonaEntity badgeEntry : listToCheck) { // Checking existing player's badge list
//			Long idToCheck = badgeEntry.getAchievementRank().getAchievementDefinition().getId();
//			if (idToCheck == 100) { // 100 - DefinitionId of ExtraLVL achiv.
//				slot0IsLocked = true;
//				achievementRankEntityCheck = badgeEntry.getAchievementRank();
//				break;
//			}
//		}
//		for (BadgeInput badgeInput : badgeInputs) {
//			int idCheck = badgeInput.getBadgeDefinitionId();
//			AchievementStateEntity achievementStateEntity = achievementStateDAO.findByPersonaBadge(persona, (long) idCheck);
//			
//			AchievementRankEntity achievementRank = achievementStateEntity.getAchievementRank();
//			BadgePersonaEntity badgePersonaEntity = new BadgePersonaEntity();
//			if (badgeInput.getSlotId() == 0 && slot0IsLocked) { // Skip the Slot 0 operations, allowing other slots to be replaced
//				continue;
//			}
//			else {
//				badgePersonaEntity.setSlot(badgeInput.getSlotId());
//			}
//			badgePersonaEntity.setPersona(persona);
//			badgePersonaEntity.setAchievementRank(achievementRank);
//			listOfBadges.add(badgePersonaEntity);
//		}
//		if (slot0IsLocked) {
//			badgePersonaDAO.deleteByPersonaButExcludeRank(persona, achievementRankEntityCheck);
//		}
//		else {
//			badgePersonaDAO.deleteByPersona(persona);
//		}
//		persona.setListOfBadges(listOfBadges);
//		personaDAO.update(persona);
//	}

}
