package com.soapboxrace.core.bo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Session;

import org.apache.commons.codec.digest.DigestUtils;

import com.soapboxrace.core.dao.AchievementRankDAO;
import com.soapboxrace.core.dao.AchievementStateDAO;
import com.soapboxrace.core.dao.DonateHistoryDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ProductDAO;
import com.soapboxrace.core.dao.PromoCodeDAO;
import com.soapboxrace.core.dao.SalesDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.jpa.AchievementRankEntity;
import com.soapboxrace.core.jpa.AchievementStateEntity;
import com.soapboxrace.core.jpa.DonateHistoryEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.PromoCodeEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.jaxb.http.AchievementState;

@Stateless
public class PromoCodeBO {

	@EJB
	private PromoCodeDAO promoCodeDao;

	@EJB
	private UserDAO userDao;
	
	@EJB
	private PersonaDAO personaDao;
	
	@EJB
	private AchievementStateDAO achievementStateDao;
	
	@EJB
	private AchievementRankDAO achievementRankDao;
	
	@EJB
	private ProductDAO productDAO;
	
	@EJB
	private SalesDAO salesDAO;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private AchievementRankDAO achievementRankDAO;
	
	@EJB
	private AchievementsBO achievementsBO;
	
	@EJB
	private CommerceBO commerceBO;

	@EJB
	private DonateHistoryDAO donateHistoryDAO;
	
	@Resource(mappedName = "java:jboss/mail/Gmail")
	private Session mailSession;

	public String createPromoCode(String codeType) {
		String promoCode = "WE-" + (Long.toHexString(Double.doubleToLongBits(Math.random()))).toUpperCase();
		PromoCodeEntity promoCodeEntity = new PromoCodeEntity();
		promoCodeEntity.setIsUsed(false);
		promoCodeEntity.setPromoCode(promoCode);
		promoCodeEntity.setCodeType(codeType);
		promoCodeDao.insert(promoCodeEntity);
		return "Premium Code (" + codeType + ") is created: " + promoCode;
	}

	private UserEntity checkLogin(String email, String password) {
		password = (DigestUtils.sha1Hex(password));
		if (email != null && !email.isEmpty() && !password.isEmpty()) {
			UserEntity userEntity = userDao.findByEmail(email);
			if (userEntity != null) {
				if (password.equals(userEntity.getPassword())) {
					return userEntity;
				}
			}
		}
		return null;
	}

	public String usePromoCode(String promoCode, String email, String password, String nickname, String token) {
		UserEntity userEntity = null;
		// If user himself activates a code
		if (token == null) {
			userEntity = checkLogin(email, password);
			if (userEntity == null) {
				return "ERROR: invalid email or password";
			}
		}
//		if (userEntity.isPremium()) {
//			return "ERROR: this account already have a premium";
//		}
		PromoCodeEntity promoCodeEntity = promoCodeDao.findByCode(promoCode);
		if (promoCodeEntity == null) {
			return "ERROR: invalid promo code";
		}
		if (promoCodeEntity.getIsUsed()) {
			return "ERROR: this promo code is expired or used";
		}
		
		PersonaEntity personaEntity = personaDao.findByName(nickname);
		if (personaEntity == null) {
			return "ERROR: wrong nickname, please try again";
		}
		if (token != null) {
			userEntity = personaEntity.getUser();
		}
		// TODO Additional promo-codes functional
		return premiumActivation(nickname, promoCodeEntity, personaEntity, userEntity);
	}
	
	public String processDonateRequest(float currencyAmount, String transactionId, PersonaEntity personaEntity, String optionType) {
		DonateHistoryEntity donateHistoryEntity = new DonateHistoryEntity();
		donateHistoryEntity.setCurrencyAmount(currencyAmount);
		donateHistoryEntity.setTransactionId(transactionId);
		donateHistoryEntity.setDate(LocalDateTime.now());
		donateHistoryEntity.setPersona(personaEntity); 
		donateHistoryEntity.setUser(personaEntity.getUser());
		donateHistoryEntity.setOptionType(optionType);
		donateHistoryDAO.insert(donateHistoryEntity);
		
		String promoCode = "WE-" + (Long.toHexString(Double.doubleToLongBits(Math.random()))).toUpperCase();
		PromoCodeEntity promoCodeEntity = new PromoCodeEntity();
		promoCodeEntity.setIsUsed(false);
		promoCodeEntity.setPromoCode(promoCode);
		promoCodeEntity.setCodeType(optionType);
		promoCodeDao.insert(promoCodeEntity);
		
		premiumActivation(personaEntity.getName(), promoCodeEntity, personaEntity, personaEntity.getUser());
		return "ok";
	}
	
	public String premiumActivation(String nickname, PromoCodeEntity promoCodeEntity, PersonaEntity personaEntity, UserEntity userEntity) {
		String premiumCodeType = promoCodeEntity.getCodeType();
		int maxCashLimit = parameterBO.getMaxCash();
		int sbConvAmount = parameterBO.getIntParam("BOOST_CONVERT_AMOUNT");
		int sbConvCashValue = parameterBO.getIntParam("BOOST_CONVERT_CASHVALUE");
		int premiumMoneyValue = 0;
		int playerInitialCash = personaEntity.getCash();
		int finalValue = 0;
		int playerInitialLevel = personaEntity.getLevel();
		Long personaId = personaEntity.getPersonaId();
		int maxLevelCap = parameterBO.getIntParam("MAX_LEVEL");
		// Predefined World Evolved premium types - Hypercycle
		// TODO Kick the player while applying the premium?
		switch (premiumCodeType) {
		    case "powerup":
		    	if (!userEntity.isPremium()) {
		    		premiumAchievementApply(126, personaEntity, false);
		    	
		    		premiumMoneyValue = 5000000;
		    		finalValue = playerInitialCash + premiumMoneyValue;
		    		finalValue = commerceBO.limitBoostConversion(finalValue, maxCashLimit, sbConvCashValue, sbConvAmount, userEntity, personaId, false);
		    		personaEntity.setCash(finalValue);
		    		
		    		if (playerInitialLevel < 25) {
		    			personaEntity.setLevel(25);
		    		}
		    		Integer[] level20RanksArray = new Integer[] {111};
		    		premiumAchievementLevelRankApply(level20RanksArray, personaEntity);
		    		
		    		personaDao.update(personaEntity);
		    		userEntity.setPremiumType(premiumCodeType);
		    		userDao.update(userEntity);
		    	
		    		promoCodeEntity.setIsUsed(true);
					promoCodeEntity.setUser(userEntity);
					promoCodeDao.update(promoCodeEntity);
					System.out.println("Player " + nickname + " got the Promo Code "
							+ "(Type: " + premiumCodeType + ", Money amount BEFORE: " + playerInitialCash + ", level BEFORE: " + playerInitialLevel + ")");
					return "Power-Up is activated (restart the game).";
		    	}
		    	return "ERROR: this account is already got a higher Premium";
		    case "base":
		    	premiumAchievementApply(126, personaEntity, false);
		    	premiumAchievementApply(501, personaEntity, false);
		    	premiumCarSlots(200, userEntity);
		    	
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumCodeType);
		    	userEntity.setPremiumDate(LocalDate.now());
		    	userDao.update(userEntity);
		    	
				promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code "
						+ "(Type: " + premiumCodeType + ", Money amount BEFORE: " + playerInitialCash + ", level BEFORE: " + playerInitialLevel + ")");
				return "Premium Base is activated (restart the game).";
		    case "plus":
		    	premiumAchievementApply(126, personaEntity, false);
		    	premiumAchievementApply(501, personaEntity, false);
		    	premiumAchievementApply(502, personaEntity, false);
		    	premiumCarSlots(200, userEntity);
		    	
		    	premiumMoneyValue = 10000000;
	    		finalValue = playerInitialCash + premiumMoneyValue;
	    		finalValue = commerceBO.limitBoostConversion(finalValue, maxCashLimit, sbConvCashValue, sbConvAmount, userEntity, personaId, false);
	    		personaEntity.setCash(finalValue);
	    		
	    		if (playerInitialLevel < 40) {
	    			personaEntity.setLevel(40);
	    		}
	    		Integer[] level40RanksArray = new Integer[] {111,112};
	    		premiumAchievementLevelRankApply(level40RanksArray, personaEntity);
	    		
	    		personaDao.update(personaEntity);
		    	
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumCodeType);
		    	userEntity.setPremiumDate(LocalDate.now());
		    	userDao.update(userEntity);
		    	
		    	promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code "
						+ "(Type: " + premiumCodeType + ", Money amount BEFORE: " + playerInitialCash + ", level BEFORE: " + playerInitialLevel + ")");
				return "Premium+ is activated (restart the game).";
		    case "full":
		    	premiumAchievementApply(126, personaEntity, false);
		    	premiumAchievementApply(501, personaEntity, false);
		    	premiumAchievementApply(502, personaEntity, false);
		    	premiumAchievementApply(503, personaEntity, true);
		    	premiumCarSlots(200, userEntity);
		    	
		    	premiumMoneyValue = 30000000;
	    		finalValue = playerInitialCash + premiumMoneyValue;
	    		finalValue = commerceBO.limitBoostConversion(finalValue, maxCashLimit, sbConvCashValue, sbConvAmount, userEntity, personaId, false);
	    		
	    		personaEntity.setCash(finalValue);
	    		if (playerInitialLevel < 75) {
	    			personaEntity.setLevel(75);
	    		}
	    		Integer[] level75RanksArray = new Integer[] {111,112,113};
	    		premiumAchievementLevelRankApply(level75RanksArray, personaEntity);
	    		
		    	personaDao.update(personaEntity);
		    	
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumCodeType);
		    	userEntity.setPremiumDate(LocalDate.now());
		    	userDao.update(userEntity);
		    	
		    	promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code "
						+ "(Type: " + premiumCodeType + ", Money amount BEFORE: " + playerInitialCash + ", level BEFORE: " + playerInitialLevel + ")");
				return "Premium Full is activated (restart the game).";
		    case "moneydrop":
		    	premiumMoneyValue = 10000000;
		    	finalValue = playerInitialCash + premiumMoneyValue;
		    	finalValue = commerceBO.limitBoostConversion(finalValue, maxCashLimit, sbConvCashValue, sbConvAmount, userEntity, personaId, false);
		    	personaEntity.setCash(finalValue);
		    	
		    	personaDao.update(personaEntity);
		    	
		    	promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code "
						+ "(Type: " + premiumCodeType + ", Money amount BEFORE: " + playerInitialCash + ", level BEFORE: " + playerInitialLevel + ")");
				return "Money Drop is activated (restart the game).";
		    case "moneydropx10":
		    	premiumMoneyValue = 100000000;
		    	finalValue = playerInitialCash + premiumMoneyValue;
		    	finalValue = commerceBO.limitBoostConversion(finalValue, maxCashLimit, sbConvCashValue, sbConvAmount, userEntity, personaId, false);
		    	personaEntity.setCash(finalValue);
		    	personaDao.update(personaEntity);
		    	
		    	promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code "
						+ "(Type: " + premiumCodeType + ", Money amount BEFORE: " + playerInitialCash + ", level BEFORE: " + playerInitialLevel + ")");
				return "Money Drop is activated (restart the game).";
		    case "garage50":
		    	premiumCarSlots(250, userEntity);
		    	
		    	promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code "
						+ "(Type: " + premiumCodeType + ", Money amount BEFORE: " + playerInitialCash + ", level BEFORE: " + playerInitialLevel + ")");
				return "Garage50+ is activated (restart the game).";
		    case "garage150":
		    	premiumCarSlots(350, userEntity);
		    	
		    	promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code "
						+ "(Type: " + premiumCodeType + ", Money amount BEFORE: " + playerInitialCash + ", level BEFORE: " + playerInitialLevel + ")");
				return "Garage150+ is activated (restart the game).";
		    case "levelup":
		    	int newLevel = playerInitialLevel + 25;
		    	int cashDiff = 0;
		    	if (newLevel > maxLevelCap) {
		    		newLevel = maxLevelCap;
		    		// Unused levels will be converted into IGC
		    		int levelCashDiff = (playerInitialLevel + 25) - maxLevelCap;
		    		cashDiff = 500000 * levelCashDiff;
		    		int cashValue = personaEntity.getCash() + cashDiff;
		    		cashValue = commerceBO.limitBoostConversion(cashValue, maxCashLimit, sbConvCashValue, sbConvAmount, userEntity, personaId, false);
		    		personaEntity.setCash(cashValue);
		    	}
		    	personaEntity.setLevel(newLevel);
		    	personaDao.update(personaEntity);
		    	
		    	promoCodeEntity.setIsUsed(true);
				promoCodeEntity.setUser(userEntity);
				promoCodeDao.update(promoCodeEntity);
				System.out.println("Player " + nickname + " got the Promo Code "
						+ "(Type: " + premiumCodeType + ", Money amount BEFORE: " + playerInitialCash + ", level BEFORE: " + playerInitialLevel + ")");
				String finalOutput = "Level-Up is activated (restart the game).";
				if (cashDiff > 0) {
					finalOutput = finalOutput.concat("Unused levels has been converted to IGC - " + cashDiff + ".");
				}
				return finalOutput;
            default:
            	return "ERROR: invaild Premium code, please contact to server support";
		}
	}
	
	// debug temporary premium activation stuff - Hypercycle
	public String useDebug(String premiumType, String extraMoney, String nickname, String timeYear, String timeMonth, String timeDay) {
		UserEntity userEntity = null;
		PersonaEntity personaEntity = personaDao.findByName(nickname);
		if (personaEntity == null) {
			return "ERROR: wrong nickname";
		}
		userEntity = personaEntity.getUser();
		int extraMoneyConvert = 0;
		// yes yes, parsing the single string would be more efficient
		int timeYearConvert = Integer.parseInt(timeYear);
		int timeMonthConvert = Integer.parseInt(timeMonth);
		int timeDayConvert = Integer.parseInt(timeDay);
		switch (premiumType) {
		    case "powerup":
		    	premiumAchievementApply(126, personaEntity, false);
		    	userEntity.setExtraMoney(extraMoneyConvert);
		    	userDao.update(userEntity);
				return nickname + " - DONE";
		    case "base":
		    	premiumAchievementApply(126, personaEntity, false);
		    	premiumAchievementApply(501, personaEntity, false);
		    	userEntity.setExtraMoney(extraMoneyConvert);
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumType);
		    	userEntity.setPremiumDate(LocalDate.of(timeYearConvert, timeMonthConvert, timeDayConvert));
		    	userDao.update(userEntity);
				return nickname + " - DONE";
		    case "plus":
		    	premiumAchievementApply(126, personaEntity, false);
		    	premiumAchievementApply(501, personaEntity, false);
		    	premiumAchievementApply(502, personaEntity, false);
		    	userEntity.setExtraMoney(extraMoneyConvert);
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumType);
		    	userEntity.setPremiumDate(LocalDate.of(timeYearConvert, timeMonthConvert, timeDayConvert));
		    	userDao.update(userEntity);
				return nickname + " - DONE";
		    case "full":
		    	premiumAchievementApply(126, personaEntity, false);
		    	premiumAchievementApply(501, personaEntity, false);
		    	premiumAchievementApply(502, personaEntity, false);
		    	premiumAchievementApply(503, personaEntity, true);
		    	userEntity.setExtraMoney(extraMoneyConvert);
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumType);
		    	userEntity.setPremiumDate(LocalDate.of(timeYearConvert, timeMonthConvert, timeDayConvert));
		    	userDao.update(userEntity);
				return nickname + " - DONE";
		    case "unlim":
		    	premiumAchievementApply(126, personaEntity, false);
		    	premiumAchievementApply(501, personaEntity, false);
		    	premiumAchievementApply(502, personaEntity, false);
		    	premiumAchievementApply(503, personaEntity, true);
		    	premiumAchievementApply(504, personaEntity, true);
		    	userEntity.setExtraMoney(extraMoneyConvert);
		    	userEntity.setPremium(true);
		    	userEntity.setPremiumType(premiumType);
		    	userEntity.setPremiumDate(LocalDate.of(timeYearConvert, timeMonthConvert, timeDayConvert));
		    	userDao.update(userEntity);
				return nickname + " - DONE";
            default:
            	return "ERROR: invaild infos, try again";
		}
	}
	
	private void premiumAchievementApply (int rankId, PersonaEntity personaEntity, boolean giveReward) {
		AchievementRankEntity achievementRankEntity = achievementRankDao.findById((long) rankId);
		if (achievementStateDao.findByPersonaAchievementRank(personaEntity, achievementRankEntity) == null) {
			AchievementStateEntity achievementStateEntity = new AchievementStateEntity();
			achievementStateEntity.setAchievedOn(LocalDateTime.now());
			achievementStateEntity.setAchievementRank(achievementRankEntity);
			if (giveReward) {
				achievementStateEntity.setAchievementState(AchievementState.REWARD_WAITING);
			}
			else {
				achievementStateEntity.setAchievementState(AchievementState.COMPLETED);
			}
			achievementStateEntity.setPersona(personaEntity);
			achievementStateDao.insert(achievementStateEntity);
		}
	}
	
	private void premiumAchievementLevelRankApply (Integer[] ranksArray, PersonaEntity personaEntity) {
		List<AchievementRankEntity> getLevelRanksList = achievementRankDAO.findMultipleRanksById(ranksArray);
		int i = 0;
		for (AchievementRankEntity collectorRank : getLevelRanksList) {
			if (achievementStateDao.findByPersonaAchievementRank(personaEntity, collectorRank) == null) {
				achievementsBO.forceAchievementApply(ranksArray[i], personaEntity, true);
			}
			i++;
		}
	}
	
	private void premiumCarSlots (int carSlots, UserEntity userEntity) {
		List<PersonaEntity> listOfProfile = userEntity.getListOfProfile();
		for (PersonaEntity personaEntityUser : listOfProfile) {
			personaEntityUser.setCarSlots(carSlots);
			personaDao.update(personaEntityUser);
		}
	}

}
