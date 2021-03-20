package com.soapboxrace.core.bo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.DiscordWebhook;
import com.soapboxrace.core.bo.util.OwnedCarConverter;
import com.soapboxrace.core.dao.AchievementRankDAO;
import com.soapboxrace.core.dao.BasketDefinitionDAO;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.CarSlotDAO;
import com.soapboxrace.core.dao.CustomCarDAO;
import com.soapboxrace.core.dao.InventoryDAO;
import com.soapboxrace.core.dao.InventoryItemDAO;
import com.soapboxrace.core.dao.OwnedCarDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ProductDAO;
import com.soapboxrace.core.dao.RewardDropDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.dao.TreasureHuntDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.dao.VisualPartDAO;
import com.soapboxrace.core.jpa.BasketDefinitionEntity;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.CarSlotEntity;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.InventoryEntity;
import com.soapboxrace.core.jpa.InventoryItemEntity;
import com.soapboxrace.core.jpa.OwnedCarEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.ProductEntity;
import com.soapboxrace.core.jpa.TreasureHuntEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.jaxb.http.ArrayOfCommerceItemTrans;
import com.soapboxrace.jaxb.http.CommerceItemTrans;
import com.soapboxrace.jaxb.http.CommerceResultStatus;
import com.soapboxrace.jaxb.http.CommerceResultTrans;
import com.soapboxrace.jaxb.http.OwnedCarTrans;
import com.soapboxrace.jaxb.util.JAXBUtility;

@Stateless
public class BasketBO {

	@EJB
	private PersonaBO personaBo;

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private BasketDefinitionDAO basketDefinitionsDAO;

	@EJB
	private CarSlotDAO carSlotDAO;

	@EJB
	private OwnedCarDAO ownedCarDAO;

	@EJB
	private CustomCarDAO customCarDAO;

	@EJB
	private TokenSessionDAO tokenDAO;

	@EJB
	private ProductDAO productDao;

	@EJB
	private PersonaDAO personaDao;

	@EJB
	private TokenSessionBO tokenSessionBO;

	@EJB
    private TreasureHuntDAO treasureHuntDAO;
	
	@EJB
	private InventoryDAO inventoryDao;

	@EJB
	private InventoryItemDAO inventoryItemDao;
	
	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
	private DiscordWebhook discordBot;
	
	@EJB
	private VisualPartDAO visualPartDAO;
	
	@EJB
	private RewardDropDAO rewardDropDAO;

	@EJB
	private DropBO dropBO;
	
	@EJB
	private InventoryBO inventoryBO;
	
	@EJB
	private AchievementRankDAO achievementRankDAO;
	
	@EJB
	private UserDAO userDAO;
	
	@EJB
	private CommerceBO commerceBO;
	
	@EJB
	private AchievementsBO achievementsBO;

	private OwnedCarTrans getCar(String productId) {
		BasketDefinitionEntity basketDefinitonEntity = basketDefinitionsDAO.findById(productId);
		if (basketDefinitonEntity == null) {
			throw new IllegalArgumentException(String.format("No basket definition for %s", productId));
		}
		String ownedCarTrans = basketDefinitonEntity.getOwnedCarTrans();
		return JAXBUtility.unMarshal(ownedCarTrans, OwnedCarTrans.class);
	}

	public CommerceResultStatus repairCar(String productId, PersonaEntity personaEntity) {
		CarSlotEntity defaultCarEntity = personaBo.getDefaultCarEntity(personaEntity.getPersonaId());
		int price = (int) (productDao.findByProductId(productId).getPrice() * (100 - defaultCarEntity.getOwnedCar().getDurability()));
		if (personaEntity.getCash() < price) {
			return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
		}
		if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
			personaEntity.setCash(personaEntity.getCash() - price);
		}
		personaDao.update(personaEntity);

		defaultCarEntity.getOwnedCar().setDurability(100);

		carSlotDAO.update(defaultCarEntity);
		return CommerceResultStatus.SUCCESS;
	}
	
	public CommerceResultStatus restoreTreasureHunt(String productId, PersonaEntity personaEntity) {
		Long personaId = personaEntity.getPersonaId();
        TreasureHuntEntity treasureHuntEntity = treasureHuntDAO.findById(personaId);
        int reviveCount = treasureHuntEntity.getReviveCount();
        if (reviveCount == 0) {
        	reviveCount = 1;
        }
        else {
        	reviveCount++;
        }
        
        int price = ((int) productDao.findByProductId(productId).getPrice());
        int playerMoney = personaEntity.getCash();
        if (playerMoney < price) {
            return CommerceResultStatus.FAIL_LOCKED_PRODUCT_NOT_ACCESSIBLE_TO_THIS_USER;
        }
        if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
            personaEntity.setCash(playerMoney - price);
        }
        treasureHuntEntity.setIsStreakBroken(false);
        treasureHuntEntity.setReviveCount(reviveCount);
        treasureHuntDAO.update(treasureHuntEntity);
        personaDao.update(personaEntity);

        return CommerceResultStatus.SUCCESS;
    }

	public CommerceResultStatus buyPowerups(String productId, PersonaEntity personaEntity) {
		if (!parameterBO.getBoolParam("ENABLE_ECONOMY")) {
			return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
		}
		if (!parameterBO.getBoolParam("ENABLE_POWERUP_PURCHASE")) {
			return CommerceResultStatus.FAIL_INVALID_BASKET;
		}
		ProductEntity powerupProduct = productDao.findByProductId(productId);
		InventoryEntity inventoryEntity = inventoryDao.findByPersonaId(personaEntity.getPersonaId());

		if (powerupProduct == null) {
			return CommerceResultStatus.FAIL_INVALID_BASKET;
		}
		if (personaEntity.getCash() < powerupProduct.getPrice()) {
			return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
		}

		InventoryItemEntity item = null;

		for (InventoryItemEntity i : inventoryEntity.getItems()) {
			if (i.getHash().equals(powerupProduct.getHash().intValue())) {
				item = i;
				break;
			}
		}

		if (item == null) {
			return CommerceResultStatus.FAIL_INVALID_BASKET;
		}
		boolean upgradedAmount = false;
		int newUsageCount = item.getRemainingUseCount() + 15;
		if (newUsageCount > 99)
			newUsageCount = 99;

		if (item.getRemainingUseCount() != newUsageCount)
			upgradedAmount = true;

		item.setRemainingUseCount(newUsageCount);
		inventoryItemDao.update(item);

		if (upgradedAmount) {
			personaEntity.setCash(personaEntity.getCash() - powerupProduct.getPrice());
			personaDao.update(personaEntity);
		}
		return CommerceResultStatus.SUCCESS;
	}

	public CommerceResultStatus buyCar(String productId, PersonaEntity personaEntity, boolean webAction, UserEntity userEntity) {
		if (!webAction && getPersonaCarCount(personaEntity.getPersonaId()) >= personaEntity.getCarSlots()) {
			return CommerceResultStatus.FAIL_INSUFFICIENT_CAR_SLOTS;
		}
		ProductEntity productEntity = productDao.findByProductId(productId);
		boolean isEconomyOn = parameterBO.getBoolParam("ENABLE_ECONOMY");
		
		if (!webAction && isEconomyOn) {
			String currencyType = productEntity.getCurrency();
			switch (currencyType) {
			case "CASH":
				if (productEntity == null || personaEntity.getCash() < productEntity.getPrice()) {
					return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
				}
				personaEntity.setCash(personaEntity.getCash() - productEntity.getPrice());
				personaDao.update(personaEntity);
				break;
			case "_NS":
				int speedBoost = userEntity.getBoost();
				if (productEntity == null || speedBoost < productEntity.getPrice()) {
					return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
				}
				userEntity.setBoost(speedBoost - productEntity.getPrice());
				userDAO.update(userEntity);
				break;
			}
		}
		
		OwnedCarTrans ownedCarTrans = getCar(productId);
		ownedCarTrans.setId(0L);
		ownedCarTrans.getCustomCar().setId(0);
		CarSlotEntity carSlotEntity = new CarSlotEntity();
		carSlotEntity.setPersona(personaEntity);

		OwnedCarEntity ownedCarEntity = new OwnedCarEntity();
		ownedCarEntity.setCarSlot(carSlotEntity);
		CustomCarEntity customCarEntity = new CustomCarEntity();
		customCarEntity.setOwnedCar(ownedCarEntity);
		ownedCarEntity.setCustomCar(customCarEntity);
		carSlotEntity.setOwnedCar(ownedCarEntity);
		OwnedCarConverter.trans2Entity(ownedCarTrans, ownedCarEntity);
		OwnedCarConverter.details2NewEntity(ownedCarTrans, ownedCarEntity);
		
		// Getting the current car version for a new car
		CustomCarEntity customCarEntityVer = ownedCarEntity.getCustomCar();
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(customCarEntityVer.getPhysicsProfileHash());
		if (carClassesEntity == null) {
			return CommerceResultStatus.FAIL_INVALID_BASKET;
		}
		commerceBO.calcNewCarClass(customCarEntityVer); // Calc car class and new rating value
		ownedCarEntity.setCarVersion(carClassesEntity.getCarVersion());
		carSlotDAO.insert(carSlotEntity);
		
		personaBo.changeDefaultCar(personaEntity.getPersonaId(), carSlotEntity.getOwnedCar().getId());
		if (parameterBO.getBoolParam("DISABLE_ITEM_AFTER_BUY")) {
			productEntity.setEnabled(false);
			productDao.update(productEntity);
		}
		achievementsBO.applyCollector(personaEntity);
		return CommerceResultStatus.SUCCESS;
	}
	
	// Re-used code from achievements reward drops
	// FIXME Add the cash & cars drop ability
	public CommerceResultStatus buyCardPack(String productId, PersonaEntity personaEntity, CommerceResultTrans commerceResultTrans) {
        ProductEntity bundleProduct = productDao.findByProductId(productId);
        UserEntity userEntity = personaEntity.getUser();
        if (bundleProduct == null) {
            return CommerceResultStatus.FAIL_INVALID_BASKET;
        }
		
        ArrayOfCommerceItemTrans arrayOfCommerceItemTrans = new ArrayOfCommerceItemTrans();
        commerceResultTrans.setCommerceItems(arrayOfCommerceItemTrans);
		List<CommerceItemTrans> commerceItems = new ArrayList<>();
		String cardPackType = "";
		boolean randomMultiplier = false;
		int doubleItem = 0;
		switch (productId) {
		case "SRV-CARDPACK1":
			cardPackType = "VISUALPART";
			break;
		case "SRV-CARDPACK2":
			cardPackType = "PERFORMANCEPART";
			break;
		case "SRV-CARDPACK3":
			cardPackType = "SKILLMODPART";
			break;
		case "SRV-CARDPACK6":
			cardPackType = "POWERPACK";
			randomMultiplier = true;
			Random random = new Random();
			doubleItem = random.nextInt(4);
			break;
		}
		List<ProductEntity> productDrops = rewardDropDAO.getBundleDrops(cardPackType);
		Collections.shuffle(productDrops);
		boolean inventoryFull = inventoryBO.isInventoryFull(cardPackType, personaEntity);
		if (inventoryFull) {
			return CommerceResultStatus.FAIL_MAX_STACK_OR_RENTAL_LIMIT;
		}
		
		String currencyType = bundleProduct.getCurrency(); // CASH or _NS (Boost)
		int bundlePrice = bundleProduct.getPrice();
        if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
        	if (currencyType.contentEquals("CASH") && personaEntity.getCash() < bundlePrice) {
	        	return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
			}
        	if (currencyType.contentEquals("_NS") && userEntity.getBoost() < bundlePrice) {
	        	return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
			}
        	switch (currencyType) {
        	case "CASH":
        		personaEntity.setCash(personaEntity.getCash() - bundlePrice);
        		personaDao.update(personaEntity);
        		break;
        	case "_NS":
        		userEntity.setBoost(userEntity.getBoost() - bundlePrice);
        		userDAO.update(userEntity);
        		break;
        	}
		}
		
		int count = 0;
		for (ProductEntity productDropEntity : productDrops) {
			CommerceItemTrans item = new CommerceItemTrans();
//			if (productDropEntity.getProductType().contentEquals("CASH")) { // Not used
//				float cashDrop = productDropEntity.getPrice();
//				personaEntity.setCash(personaEntity.getCash() + cashDrop);
//				personaDao.update(personaEntity);
//				item.setHash(-429893590);
//				String moneyFormat = NumberFormat.getNumberInstance(Locale.US).format(cashDrop);
//				item.setTitle("$" + moneyFormat);
//			}
//			else {
				String productTitle = productDropEntity.getProductTitle();
				item.setHash(productDropEntity.getHash());
//				System.out.println("count: " + count);
//				System.out.println("doubleItem: " + doubleItem);
				if (randomMultiplier && count == doubleItem) {
//					System.out.println("concat ACTIVE");
					productTitle = productTitle.concat(" x2");
				}
				item.setTitle(productTitle);
				productDropEntity.setUseCount(1);
				inventoryBO.addDroppedItem(productDropEntity, personaEntity);
//				System.out.println("drop: " + productTitle);
				if (productTitle.contains(" x2")) { // Add this item again
//					System.out.println("drop TWICE: " + productTitle);
					inventoryBO.addDroppedItem(productDropEntity, personaEntity);
				}
				count++;
//			}
			commerceItems.add(item);
		}
		arrayOfCommerceItemTrans.getCommerceItemTrans().addAll(commerceItems);
		commerceResultTrans.setCommerceItems(arrayOfCommerceItemTrans);
		return CommerceResultStatus.SUCCESS;
    }
	
	// IGC to Boost conversion Card Pack
	public CommerceResultStatus buyBoostConversion(String productId, PersonaEntity personaEntity, CommerceResultTrans commerceResultTrans) {
	    ProductEntity bundleProduct = productDao.findByProductId(productId);
	    UserEntity userEntity = personaEntity.getUser();
	    if (bundleProduct == null) {
	        return CommerceResultStatus.FAIL_INVALID_BASKET;
	    }
	    int bundlePrice = bundleProduct.getPrice();
        if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
	        if (personaEntity.getCash() < bundlePrice) {
		       	return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
			}
	    	personaEntity.setCash(personaEntity.getCash() - bundlePrice);
	    	personaDao.update(personaEntity);
		}
			
	    ArrayOfCommerceItemTrans arrayOfCommerceItemTrans = new ArrayOfCommerceItemTrans();
	    commerceResultTrans.setCommerceItems(arrayOfCommerceItemTrans);
		List<CommerceItemTrans> commerceItems = new ArrayList<>();

		CommerceItemTrans item = new CommerceItemTrans();
		int boostAmount = parameterBO.getIntParam("BOOST_CONVERT_AMOUNT");
		item.setHash(723701634); // Special item with icon
		
		item.setTitle(boostAmount + " SPEEDBOOST");
		userEntity.setBoost(userEntity.getBoost() + boostAmount);
		userDAO.update(userEntity);
		commerceItems.add(item);
			
		arrayOfCommerceItemTrans.getCommerceItemTrans().addAll(commerceItems);
		commerceResultTrans.setCommerceItems(arrayOfCommerceItemTrans);
		String playerName = personaEntity.getName();
		System.out.println(playerName + " has exchanged his IGC to Boost (" + boostAmount + ").");
		return CommerceResultStatus.SUCCESS;
	}
	
	// Acquire the Premium with SpeedBoost
	public CommerceResultStatus buyPremiumSB(String productId, PersonaEntity personaEntity, CommerceResultTrans commerceResultTrans) {
	    return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
        // FIXME Do the code until players got a needed SB amount...
	}
	
	// Gives a random available stock car
	public CommerceResultStatus buyCarRandom(String productId, PersonaEntity personaEntity) {
		if (getPersonaCarCount(personaEntity.getPersonaId()) >= personaEntity.getCarSlots()) {
			return CommerceResultStatus.FAIL_INSUFFICIENT_CAR_SLOTS;
		}
		ProductEntity productEntity = productDao.findByProductId(productId);
		if (productEntity == null || personaEntity.getCash() < productEntity.getPrice()) {
			return CommerceResultStatus.FAIL_INSUFFICIENT_FUNDS;
		}
		
		Random rand = new Random();
		boolean isGoodRange = false;
		int lootboxType = 0; // 0 - not available, 1 - B-A-S cars & rare, 2 - E-D-C cars, 3 - Pre-Tuned cars
		String randomProductId = "";
		isGoodRange = rand.nextBoolean();
		// Cutting down the chance from 50% to 25%
		if (isGoodRange && parameterBO.getBoolParam("ITEM_LOOTBOX_REDUCECHANCE")) {
			isGoodRange = rand.nextBoolean();
		}
		if (isGoodRange) {lootboxType = 1;}
        if (!isGoodRange) {lootboxType = 2;}
		
		List<CarClassesEntity> carList = carClassesDAO.findByLootboxType(lootboxType);
		CarClassesEntity selectedCar = carList.get(rand.nextInt(carList.size()));
		randomProductId = selectedCar.getProductId();

		OwnedCarTrans ownedCarTrans = getCar(randomProductId);
		ownedCarTrans.setId(0L);
		ownedCarTrans.getCustomCar().setId(0);
		CarSlotEntity carSlotEntity = new CarSlotEntity();
		carSlotEntity.setPersona(personaEntity);

		OwnedCarEntity ownedCarEntity = new OwnedCarEntity();
		ownedCarEntity.setCarSlot(carSlotEntity);
		CustomCarEntity customCarEntity = new CustomCarEntity();
		
		customCarEntity.setOwnedCar(ownedCarEntity);
		ownedCarEntity.setCustomCar(customCarEntity);
		carSlotEntity.setOwnedCar(ownedCarEntity);
		OwnedCarConverter.trans2Entity(ownedCarTrans, ownedCarEntity);
		OwnedCarConverter.details2NewEntity(ownedCarTrans, ownedCarEntity);

		carSlotDAO.insert(carSlotEntity);

		if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
			personaEntity.setCash(personaEntity.getCash() - productEntity.getPrice());
		}
		int carClassHash = customCarEntity.getCarClassHash();
		personaDao.update(personaEntity);
		personaBo.changeDefaultCar(personaEntity.getPersonaId(), carSlotEntity.getOwnedCar().getId());
		
		String playerName = personaEntity.getName();
		String carName = selectedCar.getFullName();
		String message = ":heavy_minus_sign:"
        		+ "\n:shopping_cart: **|** Nгрок **" + playerName + "** купил контейнер с автомобилем и получил **" + carName + "**!"
        		+ "\n:shopping_cart: **|** Player **" + playerName + "** has bought the car container and got a **" + carName + "**!";
		discordBot.sendMessage(message);

		// A, S class or rare car
		if (carClassHash == -405837480 || carClassHash == -2142411446 || selectedCar.isRare()) {
			achievementsBO.applyLuckyCollector(personaEntity);
		}
		achievementsBO.applyCollector(personaEntity);
		return CommerceResultStatus.SUCCESS;
	}

	public int getPersonaCarCount(Long personaId) {
		return getPersonasCar(personaId).size();
	}

	public List<CarSlotEntity> getPersonasCar(Long personaId) {
		List<CarSlotEntity> findByPersonaId = carSlotDAO.findByPersonaId(personaId);
		for (CarSlotEntity carSlotEntity : findByPersonaId) {
			CustomCarEntity customCar = carSlotEntity.getOwnedCar().getCustomCar();
			customCar.getPaints().size();
			customCar.getPerformanceParts().size();
			customCar.getSkillModParts().size();
			customCar.getVisualParts().size();
			customCar.getVinyls().size();
		}
		return findByPersonaId;
	}

	public boolean sellCar(String securityToken, Long personaId, Long serialNumber) {
		this.tokenSessionBO.verifyPersona(securityToken, personaId);
		PersonaEntity personaEntity = personaDao.findById(personaId);
		
		OwnedCarEntity ownedCarEntity = ownedCarDAO.findById(serialNumber);
		if (ownedCarEntity == null) {
			return false;
		}
		CarSlotEntity carSlotEntity = ownedCarEntity.getCarSlot();
		if (carSlotEntity == null) {
			return false;
		}
		if (!carSlotEntity.getPersona().getPersonaId().equals(personaEntity.getPersonaId())) {
            return false;
        }
		int personaCarCount = getPersonaCarCount(personaId);
		if (personaCarCount <= 1) {
			return false;
		}

		final int maxCash = parameterBO.getMaxCash();
		if (personaEntity.getCash() < maxCash) {
			int cashTotal = (int) (personaEntity.getCash() + ownedCarEntity.getCustomCar().getResalePrice());
			if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
				personaEntity.setCash(Math.max(0, Math.min(maxCash, cashTotal)));
			}
		}

		CarSlotEntity defaultCarEntity = personaBo.getDefaultCarEntity(personaId);

		int curCarIndex = personaEntity.getCurCarIndex();
		if (defaultCarEntity.getId().equals(carSlotEntity.getId())) {
			curCarIndex = 0;
		} else {
			List<CarSlotEntity> personasCar = personaBo.getPersonasCar(personaId);
			int curCarIndexTmp = curCarIndex;
			for (int i = 0; i < curCarIndexTmp; i++) {
				if (personasCar.get(i).getId().equals(carSlotEntity.getId())) {
					curCarIndex--;
					break;
				}
			}
		}
		carSlotDAO.delete(carSlotEntity);
		personaEntity.setCurCarIndex(curCarIndex);
		personaDao.update(personaEntity);
		return true;
	}

}
