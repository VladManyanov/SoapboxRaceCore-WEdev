package com.soapboxrace.core.bo;

import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.AchievementType;
import com.soapboxrace.core.bo.util.EventModeType;
import com.soapboxrace.core.bo.util.RewardVO;
import com.soapboxrace.core.dao.AchievementRankDAO;
import com.soapboxrace.core.dao.AchievementStateDAO;
import com.soapboxrace.core.dao.LevelRepDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ProductDAO;
import com.soapboxrace.core.dao.TreasureHuntDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.AchievementRankEntity;
import com.soapboxrace.core.jpa.CarSlotEntity;
import com.soapboxrace.core.jpa.CardDecks;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.ProductEntity;
import com.soapboxrace.core.jpa.SkillModPartEntity;
import com.soapboxrace.core.jpa.SkillModRewardType;
import com.soapboxrace.core.jpa.TreasureHuntEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.jaxb.http.Accolades;
import com.soapboxrace.jaxb.http.ArbitrationPacket;
import com.soapboxrace.jaxb.http.ArrayOfDragEntrantResult;
import com.soapboxrace.jaxb.http.ArrayOfLuckyDrawItem;
import com.soapboxrace.jaxb.http.ArrayOfRouteEntrantResult;
import com.soapboxrace.jaxb.http.EnumRewardCategory;
import com.soapboxrace.jaxb.http.EnumRewardType;
import com.soapboxrace.jaxb.http.LuckyDrawInfo;
import com.soapboxrace.jaxb.http.LuckyDrawItem;
import com.soapboxrace.jaxb.http.Reward;
import com.soapboxrace.jaxb.http.RewardPart;

@Stateless
public class RewardBO {

	@EJB
	private PersonaBO personaBo;

	@EJB
	private LevelRepDAO levelRepDao;

	@EJB
	private DropBO dropBO;

	@EJB
	private InventoryBO inventoryBO;

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private PersonaDAO personaDao;

	@EJB
	private ProductDAO productDAO;

	@EJB
	private AchievementsBO achievementsBO;
	
	@EJB
	private AchievementStateDAO achievementStateDao;
	
	@EJB
	private AchievementRankDAO achievementRankDao;
	
	@EJB
	private UserDAO userDao;
	
	@EJB
	private TreasureHuntDAO treasureHuntDao;
	
	@EJB
	private CommerceBO commerceBO;

	public Reward getFinalReward(Integer rep, Integer cash) {
		Reward finalReward = new Reward();
		finalReward.setRep(rep);
		finalReward.setTokens(cash);
		return finalReward;
	}

	public Boolean isLeveledUp(PersonaEntity personaEntity, Integer exp) {
		return (long) (personaEntity.getRepAtCurrentLevel() + exp) >= levelRepDao.findByLevel((long) personaEntity.getLevel()).getExpPoint();
	}

	public LuckyDrawInfo getLuckyDrawInfo(Integer rank, Integer level, PersonaEntity personaEntity, int isDropableMode, boolean isTeamRace) {
		LuckyDrawInfo luckyDrawInfo = new LuckyDrawInfo();
		if (!parameterBO.getBoolParam("ENABLE_DROP_ITEM")) {
			return luckyDrawInfo;
		}
		ArrayOfLuckyDrawItem arrayOfLuckyDrawItem = new ArrayOfLuckyDrawItem();
		arrayOfLuckyDrawItem.getLuckyDrawItem().add(getItemFromProduct(personaEntity, null, isDropableMode, isTeamRace));
		luckyDrawInfo.setCardDeck(CardDecks.forRank(rank));
		luckyDrawInfo.setItems(arrayOfLuckyDrawItem);
		return luckyDrawInfo;
	}

	public LuckyDrawItem getItemFromProduct(PersonaEntity personaEntity, String eventMode, int isDropableMode, boolean isTeamRace) {
		ProductEntity productEntity = dropBO.getRandomProductItem(eventMode, isDropableMode, isTeamRace);
		if (productEntity.getStars() != null && productEntity.getStars() == 4 && productEntity.getProductType().contentEquals("SKILLMODPART")) {
			achievementsBO.applyDropAchievements(personaEntity, AchievementType.WEV2_EARNSKILL);
		}
//		if (isTeamRace) {
//			System.out.println("### TEAMS: Player " + personaEntity.getName() + " has got the item: " + productEntity.getProductTitle());
//		}
		LuckyDrawItem luckyDrawItem = dropBO.copyProduct2LuckyDraw(productEntity);
		boolean inventoryFull = inventoryBO.isInventoryFull(productEntity, personaEntity);
//      if (productEntity.getProductTitle().contains("SPEEDBOOST")) {
//        	int sbAmount = parameterBO.getIntParam("REWARD_SB_AMOUNT");
//        	System.out.println("### Player " + personaEntity.getName() + " has recieved a " + sbAmount + " SpeedBoost reward.");
//			UserEntity userEntity = personaEntity.getUser();
//			userEntity.setBoost(userEntity.getBoost() + sbAmount);
//			userDao.update(userEntity);
//		}
		if (inventoryFull) {
			luckyDrawItem.setWasSold(true);
			if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
				int resalePrice = productEntity.getResalePrice();
				int cash = personaEntity.getCash();
				personaEntity.setCash(cash + resalePrice);
				personaDao.update(personaEntity);
			}
		} else {
			inventoryBO.addDroppedItem(productEntity, personaEntity);
		}
		return luckyDrawItem;
	}

	private int getMaxLevel(PersonaEntity personaEntity) {
		int maxLevel = 60; // Default NFSW level-cap
		maxLevel = parameterBO.getIntParam("MAX_LEVEL");
		return maxLevel;
	}

	public void applyRaceReward(Integer exp, Integer cash, PersonaEntity personaEntity) {
		int maxLevel = getMaxLevel(personaEntity);
		if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
			int incomeCash = personaEntity.getCash() + cash;
			final int maxCash = parameterBO.getMaxCash();
			if (incomeCash > maxCash) {
				int sbConvAmount = parameterBO.getIntParam("BOOST_CONVERT_AMOUNT");
				int sbConvCashValue = parameterBO.getIntParam("BOOST_CONVERT_CASHVALUE");
				incomeCash = commerceBO.limitBoostConversion(incomeCash, maxCash, sbConvCashValue, sbConvAmount, personaEntity.getUser(),
						personaEntity.getPersonaId(), true);
			} else if (incomeCash < 1) {
				incomeCash = 1;
			}
			personaEntity.setCash(incomeCash);
			achievementsBO.applyPayDayAchievement(personaEntity, cash);
		}

		if (parameterBO.getBoolParam("ENABLE_REPUTATION") && personaEntity.getLevel() < maxLevel) {
			Long expToNextLevel = levelRepDao.findByLevel((long) personaEntity.getLevel()).getExpPoint();
			Long expMax = (long) (personaEntity.getRepAtCurrentLevel() + exp);
			if (expMax >= expToNextLevel) {
				Boolean isLeveledUp = true;
				while (isLeveledUp) {
					personaEntity.setLevel(personaEntity.getLevel() + 1);
					achievementsBO.applyLevelUpAchievement(personaEntity);
//					if (personaEntity.getLevel() == 100) {
//						achievementsBO.applyExtraLVLAchievement(personaEntity);
//					}
					if (personaEntity.getLevel() >= 6) {
						AchievementRankEntity achievementRankEntity6 = achievementRankDao.findById((long) 516);
						if (achievementStateDao.findByPersonaAchievementRank(personaEntity, achievementRankEntity6) == null) {
							achievementsBO.applyBeginnersGuideAchievement(personaEntity);
						}
					}
					
					if (personaEntity.getLevel() == 100) { // If player don't have a previous achiv. stages
						AchievementRankEntity achievementRankEntity100 = achievementRankDao.findById((long) 111);
						if (achievementStateDao.findByPersonaAchievementRank(personaEntity, achievementRankEntity100) == null) {
							achievementsBO.forceAchievementApply(111, personaEntity, true);
							achievementsBO.forceAchievementApply(112, personaEntity, true);
							achievementsBO.forceAchievementApply(113, personaEntity, true);
							achievementsBO.forceAchievementApply(114, personaEntity, true);
						}
					}
					personaEntity.setRepAtCurrentLevel((int) (expMax - expToNextLevel));

					expToNextLevel = levelRepDao.findByLevel((long) personaEntity.getLevel()).getExpPoint();
					expMax = (long) (personaEntity.getRepAtCurrentLevel() + exp);

					isLeveledUp = (expMax >= expToNextLevel);
					if (personaEntity.getLevel() >= maxLevel) {
						isLeveledUp = false;
					}
				}
			} else {
				personaEntity.setRepAtCurrentLevel(expMax.intValue());
			}
			personaEntity.setRep(personaEntity.getRep() + exp);
		}
		personaDao.update(personaEntity);
	}

	public RewardPart getRewardPart(Integer rep, Integer cash, EnumRewardCategory category, EnumRewardType type) {
		RewardPart rewardPart = new RewardPart();
		rewardPart.setRepPart(rep);
		rewardPart.setRewardCategory(category);
		rewardPart.setRewardType(type);
		rewardPart.setTokenPart(cash);
		return rewardPart;
	}

	public void setTopSpeedReward(EventEntity eventEntity, float topSpeed, RewardVO rewardVO) {
		float minTopSpeedTrigger = eventEntity.getMinTopSpeedTrigger();
		if (topSpeed >= minTopSpeedTrigger) {
			float baseRep = rewardVO.getBaseRep();
			float baseCash = rewardVO.getBaseCash();
			float topSpeedCashMultiplier = eventEntity.getTopSpeedCashMultiplier();
			float topSpeedRepMultiplier = eventEntity.getTopSpeedRepMultiplier();
			Float highSpeedRep = baseRep * topSpeedRepMultiplier;
			Float highSpeedCash = baseCash * topSpeedCashMultiplier;
			rewardVO.add(highSpeedRep.intValue(), highSpeedCash.intValue(), EnumRewardCategory.BONUS, EnumRewardType.NONE);
		}
	}

	public void setSkillMultiplierReward(PersonaEntity personaEntity, RewardVO rewardVO, SkillModRewardType skillModRewardType) {
		CarSlotEntity defaultCarEntity = personaBo.getDefaultCarEntity(personaEntity.getPersonaId());
		Set<SkillModPartEntity> skillModParts = defaultCarEntity.getOwnedCar().getCustomCar().getSkillModParts();
		float skillMultiplier = 0f;
		float maxSkillMultiplier = 30f;
		if (SkillModRewardType.EXPLORER.equals(skillModRewardType)) {
			maxSkillMultiplier = 50f;
		}
		for (SkillModPartEntity skillModPartEntity : skillModParts) {
			ProductEntity productEntity = productDAO.findByHash(skillModPartEntity.getSkillModPartAttribHash());
			if (productEntity != null && productEntity.getProductTitle().equals(skillModRewardType.toString())) {
				float skillValue = productEntity.getSkillValue();
				skillMultiplier = skillMultiplier + skillValue;
			}
		}
		float finalSkillMultiplier = Math.min(maxSkillMultiplier, skillMultiplier) / 100;
		float cash = rewardVO.getCash();
		Float finalCash = cash * finalSkillMultiplier;
		rewardVO.add(0, finalCash.intValue(), EnumRewardCategory.SKILL_MOD, EnumRewardType.TOKEN_AMPLIFIER);
	}

	public Accolades getAccolades(PersonaEntity personaEntity, ArbitrationPacket arbitrationPacket, RewardVO rewardVO, int isDropableMode, boolean isTeamRace, boolean noLuckyDraw) {
		Accolades accolades = new Accolades();
		accolades.setFinalRewards(getFinalReward(rewardVO.getRep(), rewardVO.getCash()));
		accolades.setHasLeveledUp(isLeveledUp(personaEntity, rewardVO.getRep()));
		if (!noLuckyDraw) {
			accolades.setLuckyDrawInfo(getLuckyDrawInfo(arbitrationPacket.getRank(), personaEntity.getLevel(), personaEntity, isDropableMode, isTeamRace));
		}
		accolades.setOriginalRewards(getFinalReward(rewardVO.getRep(), rewardVO.getCash()));
		accolades.setRewardInfo(rewardVO.getArrayOfRewardPart());
		return accolades;
	}

	public void setMultiplierReward(EventEntity eventEntity, RewardVO rewardVO) {
		float rep = rewardVO.getRep();
		float cash = rewardVO.getCash();
		float finalRepRewardMultiplier = eventEntity.getFinalRepRewardMultiplier();
		float finalCashRewardMultiplier = eventEntity.getFinalCashRewardMultiplier();
		Float finalRep = ((rep * finalRepRewardMultiplier) * parameterBO.getFloatParam("REWARD_REP_MULTIPLIER")); // Overall rewards multipliers
		Float finalCash = ((cash * finalCashRewardMultiplier) * parameterBO.getFloatParam("REWARD_CASH_MULTIPLIER"));
		rewardVO.add(finalRep.intValue(), 0, EnumRewardCategory.AMPLIFIER, EnumRewardType.REP_AMPLIFIER);
		rewardVO.add(0, finalCash.intValue(), EnumRewardCategory.AMPLIFIER, EnumRewardType.TOKEN_AMPLIFIER);
	}

	public void setPerfectStartReward(EventEntity eventEntity, int perfectStart, RewardVO rewardVO) {
		if (perfectStart == 1) {
			float baseRep = rewardVO.getBaseRep();
			float baseCash = rewardVO.getBaseCash();
			float perfectStartCashMultiplier = eventEntity.getPerfectStartCashMultiplier();
			float perfectStartRepMultiplier = eventEntity.getPerfectStartRepMultiplier();
			Float perfectStartRep = baseRep * perfectStartRepMultiplier;
			Float perfectStartCash = baseCash * perfectStartCashMultiplier;
			rewardVO.add(perfectStartRep.intValue(), perfectStartCash.intValue(), EnumRewardCategory.BONUS, EnumRewardType.NONE);
		}
	}
	
	public void setClassBonusReward(EventEntity eventEntity, int carClassHash, RewardVO rewardVO) {
		if (parameterBO.getIntParam("CLASSBONUS_CARCLASSHASH") == carClassHash) {
			float baseRep = rewardVO.getBaseRep();
			float baseCash = rewardVO.getBaseCash();
			float WeeklyClassBonusCashMultiplier = (float) parameterBO.getFloatParam("CLASSBONUS_MULTIPLIER");
			float WeeklyClassBonusRepMultiplier = (float) parameterBO.getFloatParam("CLASSBONUS_MULTIPLIER");
			Float WeeklyClassBonusRep = baseRep * WeeklyClassBonusCashMultiplier;
			Float WeeklyClassBonusCash = baseCash * WeeklyClassBonusRepMultiplier;
			rewardVO.add(WeeklyClassBonusRep.intValue(), WeeklyClassBonusCash.intValue(), EnumRewardCategory.BONUS, EnumRewardType.NONE);
		}
	}

	public Float getPlayerLevelConst(int playerLevel, float levelCashRewardMultiplier) {
		Float level = (float) playerLevel;
		return levelCashRewardMultiplier * level.floatValue();
	}

	public Float getTimeConst(Long legitTime, Long routeTime) {
		Float timeConst = legitTime.floatValue() / routeTime.floatValue();
		return Math.min(timeConst, 1f);
	}

	public int getBaseReward(float baseReward, float playerLevelConst, float timeConst) {
		Float baseRewardResult = baseReward * playerLevelConst * timeConst;
		return baseRewardResult.intValue();
	}

	public void setBaseReward(PersonaEntity personaEntity, EventEntity eventEntity, ArbitrationPacket arbitrationPacket, RewardVO rewardVO) {
		Float baseRep = (float) eventEntity.getBaseRepReward();
		Float baseCash = (float) eventEntity.getBaseCashReward();
		int levelCheckRep = personaEntity.getLevel(); // Maximum reward-level, works as a lock for reward progression
		int levelLockRep = parameterBO.getIntParam("REWARD_LEVELLOCK");
		if (levelLockRep < levelCheckRep) {
			levelCheckRep = levelLockRep;
		}
		int levelCheckCash = levelCheckRep; // Minimum reward-level, works as a minimal reward-level of reward progression
		int levelLockCash = parameterBO.getIntParam("REWARD_MINLEVELLOCK");
		if (levelCheckCash < levelLockCash) {
			levelCheckCash = levelLockCash;
		}
		Float playerLevelRepConst = getPlayerLevelConst(levelCheckRep, eventEntity.getLevelRepRewardMultiplier());
		Float playerLevelCashConst = getPlayerLevelConst(levelCheckCash, eventEntity.getLevelCashRewardMultiplier());
		Float timeConst = getTimeConst(eventEntity.getLegitTime(), arbitrationPacket.getEventDurationInMilliseconds());
		rewardVO.setBaseRep(getBaseReward(baseRep, playerLevelRepConst, timeConst));
		rewardVO.setBaseCash(getBaseReward(baseCash, playerLevelCashConst, timeConst));
	}
	
	public void setBaseRewardDrag(PersonaEntity personaEntity, EventEntity eventEntity, ArbitrationPacket arbitrationPacket, RewardVO rewardVO, ArrayOfDragEntrantResult arrayOfDragEntrantResult) {
		Float baseRep = (float) 0;
		Float baseCash = (float) 0;
		
		if (arrayOfDragEntrantResult.getDragEntrantResult().size() < 2) {
			baseRep = (float) (eventEntity.getBaseRepReward() * parameterBO.getFloatParam("SP_REWARD_DRAG_MULTIPLIER"));
			baseCash = (float) (eventEntity.getBaseCashReward() * parameterBO.getFloatParam("SP_REWARD_DRAG_MULTIPLIER"));
		}
		else {
			baseRep = (float) eventEntity.getBaseRepReward();
			baseCash = (float) eventEntity.getBaseCashReward();
		}
		int levelCheckRep = personaEntity.getLevel(); // Maximum reward-level, works as a lock for reward progression
		int levelLockRep = parameterBO.getIntParam("REWARD_LEVELLOCK");
		if (levelLockRep < levelCheckRep) {
			levelCheckRep = levelLockRep;
		}
		int levelCheckCash = levelCheckRep; // Minimum reward-level, works as a minimal reward-level of reward progression
		int levelLockCash = parameterBO.getIntParam("REWARD_MINLEVELLOCK");
		if (levelCheckCash < levelLockCash) {
			levelCheckCash = levelLockCash;
		}
		Float playerLevelRepConst = getPlayerLevelConst(levelCheckRep, eventEntity.getLevelRepRewardMultiplier());
		Float playerLevelCashConst = getPlayerLevelConst(levelCheckCash, eventEntity.getLevelCashRewardMultiplier());
		Float timeConst = getTimeConst(eventEntity.getLegitTime(), arbitrationPacket.getEventDurationInMilliseconds());
		rewardVO.setBaseRep(getBaseReward(baseRep, playerLevelRepConst, timeConst));
		rewardVO.setBaseCash(getBaseReward(baseCash, playerLevelCashConst, timeConst));
	}
	
	public void setBaseRewardRace(PersonaEntity personaEntity, EventEntity eventEntity, ArbitrationPacket arbitrationPacket, RewardVO rewardVO, ArrayOfRouteEntrantResult arrayOfRouteEntrantResult) {
		Float baseRep = (float) 0;
		Float baseCash = (float) 0;
		
		if (arrayOfRouteEntrantResult.getRouteEntrantResult().size() < 2) {
			baseRep = (float) (eventEntity.getBaseRepReward() * parameterBO.getFloatParam("SP_REWARD_RACE_MULTIPLIER"));
			baseCash = (float) (eventEntity.getBaseCashReward() * parameterBO.getFloatParam("SP_REWARD_RACE_MULTIPLIER"));
		}
		else {
			baseRep = (float) eventEntity.getBaseRepReward();
			baseCash = (float) eventEntity.getBaseCashReward();
		}
		
		int levelCheckRep = personaEntity.getLevel(); // Maximum reward-level, works as a lock for reward progression
		int levelLockRep = parameterBO.getIntParam("REWARD_LEVELLOCK");
		if (levelLockRep < levelCheckRep) {
			levelCheckRep = levelLockRep;
		}
		int levelCheckCash = levelCheckRep; // Minimum reward-level, works as a minimal reward-level of reward progression
		int levelLockCash = parameterBO.getIntParam("REWARD_MINLEVELLOCK");
		if (levelCheckCash < levelLockCash) {
			levelCheckCash = levelLockCash;
		}
		Float playerLevelRepConst = getPlayerLevelConst(levelCheckRep, eventEntity.getLevelRepRewardMultiplier());
		Float playerLevelCashConst = getPlayerLevelConst(levelCheckCash, eventEntity.getLevelCashRewardMultiplier());
		Float timeConst = getTimeConst(eventEntity.getLegitTime(), arbitrationPacket.getEventDurationInMilliseconds());
		rewardVO.setBaseRep(getBaseReward(baseRep, playerLevelRepConst, timeConst));
		rewardVO.setBaseCash(getBaseReward(baseCash, playerLevelCashConst, timeConst));
	}

	public void setRankReward(EventEntity eventEntity, ArbitrationPacket routeArbitrationPacket, RewardVO rewardVO) {
		float rankRepMultiplier = 0f;
		float rankCashMultiplier = 0f;
		if (EventModeType.TEAM_ESCAPE.getId() == eventEntity.getEventModeId()) { // All Team Escape finishers will get highest reward
			rankRepMultiplier = eventEntity.getRank1RepMultiplier();
			rankCashMultiplier = eventEntity.getRank1CashMultiplier();
		}
		else {
			switch (routeArbitrationPacket.getRank()) {
			case 1:
				rankRepMultiplier = eventEntity.getRank1RepMultiplier();
				rankCashMultiplier = eventEntity.getRank1CashMultiplier();
				break;
			case 2:
				rankRepMultiplier = eventEntity.getRank2RepMultiplier();
				rankCashMultiplier = eventEntity.getRank2CashMultiplier();
				break;
			case 3:
				rankRepMultiplier = eventEntity.getRank3RepMultiplier();
				rankCashMultiplier = eventEntity.getRank3CashMultiplier();
				break;
			case 4:
				rankRepMultiplier = eventEntity.getRank4RepMultiplier();
				rankCashMultiplier = eventEntity.getRank4CashMultiplier();
				break;
			case 5:
				rankRepMultiplier = eventEntity.getRank5RepMultiplier();
				rankCashMultiplier = eventEntity.getRank5CashMultiplier();
				break;
			case 6:
				rankRepMultiplier = eventEntity.getRank6RepMultiplier();
				rankCashMultiplier = eventEntity.getRank6CashMultiplier();
				break;
			case 7:
				rankRepMultiplier = eventEntity.getRank7RepMultiplier();
				rankCashMultiplier = eventEntity.getRank7CashMultiplier();
				break;
			case 8:
				rankRepMultiplier = eventEntity.getRank8RepMultiplier();
				rankCashMultiplier = eventEntity.getRank8CashMultiplier();
				break;
			default:
				break;
			}
		}
		
		float baseRep = rewardVO.getBaseRep();
		float baseCash = rewardVO.getBaseCash();
		Float rankRepResult = baseRep * rankRepMultiplier;
		Float cashRepResult = baseCash * rankCashMultiplier;
		rewardVO.add(rankRepResult.intValue(), cashRepResult.intValue(), EnumRewardCategory.BONUS, EnumRewardType.NONE);
	}

	public RewardVO getRewardVO(PersonaEntity personaEntity) {
		Boolean enableEconomy = parameterBO.getBoolParam("ENABLE_ECONOMY");
		Boolean enableReputation = parameterBO.getBoolParam("ENABLE_REPUTATION");
		UserEntity userEntity = personaEntity.getUser();
		int maxLevel = getMaxLevel(personaEntity);
		if (personaEntity.getLevel() >= maxLevel) {
			enableReputation = false;
		}
		if (userEntity.getBoost() > 9999999) {
			enableEconomy = false;
		}
		return new RewardVO(enableEconomy, enableReputation);
	}

	public void setPursuitParamReward(float rewardValue, EnumRewardType enumRewardType, RewardVO rewardVO) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("PURSUIT_");
		stringBuilder.append(enumRewardType.toString());
		String rewardMultiplierStr = stringBuilder.toString().concat("_REP_MULTIPLIER");
		String cashMultiplierStr = stringBuilder.toString().concat("_CASH_MULTIPLIER");
		float rewardMultiplier = parameterBO.getFloatParam(rewardMultiplierStr);
		float cashMultiplier = parameterBO.getFloatParam(cashMultiplierStr);
		float baseRep = rewardVO.getBaseRep();
		float baseCash = rewardVO.getBaseCash();
		Float repReward = baseRep * rewardValue * rewardMultiplier;
		Float cashReward = baseCash * rewardValue * cashMultiplier;
		rewardVO.add(repReward.intValue(), cashReward.intValue(), EnumRewardCategory.PURSUIT, enumRewardType);
	}
	
	public void setTHStreakReward(Long personaId, RewardVO rewardVO) {
		float repOrig = rewardVO.getRep();
		float cashOrig = rewardVO.getCash();
		TreasureHuntEntity treasureHuntEntity = treasureHuntDao.findById(personaId);
		float thStreakMultiplier = 1;
		int curStreak = treasureHuntEntity.getStreak();
		if (!treasureHuntEntity.getIsStreakBroken() && curStreak > 0) {
			thStreakMultiplier = (1 + ((float) curStreak / 300)); // Example - 50 days / 300 + 1 = 1.17 multiplier (17%+)
		}
		Float finalRep = (repOrig * thStreakMultiplier * parameterBO.getFloatParam("REWARD_REP_MULTIPLIER") - repOrig); // Overall rewards multipliers
		Float finalCash = (cashOrig * thStreakMultiplier * parameterBO.getFloatParam("REWARD_CASH_MULTIPLIER") - cashOrig);
		rewardVO.add(finalRep.intValue(), finalCash.intValue(), EnumRewardCategory.BONUS, EnumRewardType.NONE);
	}
}
