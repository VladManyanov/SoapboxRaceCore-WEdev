package com.soapboxrace.core.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.bo.util.CommerceOp;
import com.soapboxrace.core.bo.util.OwnedCarConverter;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.CarSlotDAO;
import com.soapboxrace.core.dao.InventoryDAO;
import com.soapboxrace.core.dao.InventoryItemDAO;
import com.soapboxrace.core.dao.PaintDAO;
import com.soapboxrace.core.dao.PerformancePartDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.ProductDAO;
import com.soapboxrace.core.dao.SkillModPartDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.dao.VinylDAO;
import com.soapboxrace.core.dao.VinylProductDAO;
import com.soapboxrace.core.dao.VisualPartDAO;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.CarSlotEntity;
import com.soapboxrace.core.jpa.CustomCarEntity;
import com.soapboxrace.core.jpa.InventoryItemEntity;
import com.soapboxrace.core.jpa.OwnedCarEntity;
import com.soapboxrace.core.jpa.PerformancePartEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.ProductEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.core.jpa.VinylProductEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;
import com.soapboxrace.jaxb.http.BasketItemTrans;
import com.soapboxrace.jaxb.http.CommerceSessionTrans;
import com.soapboxrace.jaxb.http.CustomCarTrans;
import com.soapboxrace.jaxb.http.CustomPaintTrans;
import com.soapboxrace.jaxb.http.EntitlementItemTrans;
import com.soapboxrace.jaxb.http.OwnedCarTrans;
import com.soapboxrace.jaxb.http.PerformancePartTrans;
import com.soapboxrace.jaxb.http.SkillModPartTrans;
import com.soapboxrace.jaxb.http.VisualPartTrans;

@Stateless
public class CommerceBO {
	@EJB
	private PersonaBO personaBO;

	@EJB
	private InventoryBO inventoryBO;

	@EJB
	private CarSlotDAO carSlotDAO;

	@EJB
	private PersonaDAO personaDAO;

	@EJB
	private ProductDAO productDAO;

	@EJB
	private VinylProductDAO vinylProductDAO;

	@EJB
	private InventoryDAO inventoryDAO;

	@EJB
	private InventoryItemDAO inventoryItemDAO;

	@EJB
	private ParameterBO parameterBO;

	@EJB
	private PaintDAO paintDAO;

	@EJB
	private PerformancePartDAO performancePartDAO;

	@EJB
	private SkillModPartDAO skillModPartDAO;

	@EJB
	private VinylDAO vinylDAO;

	@EJB
	private VisualPartDAO visualPartDAO;

	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
    private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private UserDAO userDAO;

	public OwnedCarTrans responseCar(CommerceSessionTrans commerceSessionTrans) {
		OwnedCarTrans ownedCarTrans = new OwnedCarTrans();
		ownedCarTrans.setCustomCar(commerceSessionTrans.getUpdatedCar().getCustomCar());
		ownedCarTrans.setDurability(commerceSessionTrans.getUpdatedCar().getDurability());
		ownedCarTrans.setHeat(commerceSessionTrans.getUpdatedCar().getHeat());
		ownedCarTrans.setId(commerceSessionTrans.getUpdatedCar().getId());
		ownedCarTrans.setOwnershipType(commerceSessionTrans.getUpdatedCar().getOwnershipType());
		return ownedCarTrans;
	}

	public CommerceOp detectCommerceOperation(CommerceSessionTrans commerceSessionTrans, CarSlotEntity defaultCarEntity) {
		OwnedCarTrans ownedCarTrans = OwnedCarConverter.entity2Trans(defaultCarEntity.getOwnedCar());
		CustomCarTrans customCarTransDB = ownedCarTrans.getCustomCar();
		List<CustomPaintTrans> customPaintTransDB = customCarTransDB.getPaints().getCustomPaintTrans();
		List<PerformancePartTrans> performancePartTransDB = customCarTransDB.getPerformanceParts().getPerformancePartTrans();
		List<SkillModPartTrans> skillModPartTransDB = customCarTransDB.getSkillModParts().getSkillModPartTrans();
		List<VisualPartTrans> visualPartTransDB = customCarTransDB.getVisualParts().getVisualPartTrans();

		CustomCarTrans customCarTrans = commerceSessionTrans.getUpdatedCar().getCustomCar();
		List<CustomPaintTrans> customPaintTrans = customCarTrans.getPaints().getCustomPaintTrans();
		List<PerformancePartTrans> performancePartTrans = customCarTrans.getPerformanceParts().getPerformancePartTrans();
		List<SkillModPartTrans> skillModPartTrans = customCarTrans.getSkillModParts().getSkillModPartTrans();
		List<VisualPartTrans> visualPartTrans = customCarTrans.getVisualParts().getVisualPartTrans();

		if (skillModPartTrans.size() != skillModPartTransDB.size() || !skillModPartTrans.containsAll(skillModPartTransDB)
				|| !skillModPartTransDB.containsAll(skillModPartTrans)) {
			return CommerceOp.SKILL;
		}
		if (!performancePartTrans.containsAll(performancePartTransDB) || !performancePartTransDB.containsAll(performancePartTrans)) {
			return CommerceOp.PERFORMANCE;
		}
		if (!visualPartTrans.containsAll(visualPartTransDB) || !visualPartTransDB.containsAll(visualPartTrans)) {
			return CommerceOp.VISUAL;
		}
		if (!customPaintTrans.containsAll(customPaintTransDB) || !customPaintTransDB.containsAll(customPaintTrans)) {
			return CommerceOp.PAINTS;
		}
		return CommerceOp.VINYL;
	}

	public void updateCar(CommerceOp commerceOp, CommerceSessionTrans commerceSessionTrans, CarSlotEntity defaultCarEntity) {
		CustomCarTrans customCarTrans = commerceSessionTrans.getUpdatedCar().getCustomCar();
		OwnedCarEntity ownedCarEntity = defaultCarEntity.getOwnedCar();
		CustomCarEntity customCarEntity = ownedCarEntity.getCustomCar();
		switch (commerceOp) {
		case PAINTS:
			paintDAO.deleteByCustomCar(customCarEntity);
			OwnedCarConverter.paints2NewEntity(customCarTrans, customCarEntity);
			break;
		case PERFORMANCE:
			// FIXME Quick limitation to prevent the tuning of traffic cars
//			if (customCarEntity.getPhysicsProfileHash() == 1998148470 || 
//					customCarEntity.getPhysicsProfileHash() == -1395003737 ||
//					customCarEntity.getPhysicsProfileHash() == -176312983 ||
//					customCarEntity.getPhysicsProfileHash() == -1288301010 ||
//					customCarEntity.getPhysicsProfileHash() == -794649382 ||
//					customCarEntity.getPhysicsProfileHash() == -1794142318) {
//				break;
//			}
			performancePartDAO.deleteByCustomCar(customCarEntity);
			OwnedCarConverter.performanceParts2NewEntity(customCarTrans, customCarEntity);
			CarClassesEntity carClassesEntity = carClassesDAO.findByHash(customCarEntity.getPhysicsProfileHash());
			if (ownedCarEntity.getCarVersion() != carClassesEntity.getCarVersion()) {
				ownedCarEntity.setCarVersion(carClassesEntity.getCarVersion());
			}
			calcNewCarClass(customCarEntity);
			break;
		case SKILL:
			skillModPartDAO.deleteByCustomCar(customCarEntity);
			OwnedCarConverter.skillModParts2NewEntity(customCarTrans, customCarEntity);
			break;
		case VINYL:
			vinylDAO.deleteByCustomCar(customCarEntity);
			OwnedCarConverter.vinyls2NewEntity(customCarTrans, customCarEntity);
			break;
		case VISUAL:
			visualPartDAO.deleteByCustomCar(customCarEntity);
			OwnedCarConverter.visualParts2NewEntity(customCarTrans, customCarEntity);
			break;
		default:
			break;
		}
		carSlotDAO.update(defaultCarEntity);
	}
	
	public void updateCarVinyl(CustomCarTrans customVinylTrans, CarSlotEntity defaultCarEntity) {
		CustomCarTrans customCarTrans = customVinylTrans;
		OwnedCarEntity ownedCarEntity = defaultCarEntity.getOwnedCar();
		CustomCarEntity customCarEntity = ownedCarEntity.getCustomCar();
		paintDAO.deleteByCustomCar(customCarEntity);
		vinylDAO.deleteByCustomCar(customCarEntity);
		
		OwnedCarConverter.paints2NewEntity(customCarTrans, customCarEntity);
		OwnedCarConverter.vinyls2NewEntity(customCarTrans, customCarEntity);
	}

	public void calcNewCarClass(CustomCarEntity customCarEntity) {
		int physicsProfileHash = customCarEntity.getPhysicsProfileHash();
		CarClassesEntity carClassesEntity = carClassesDAO.findByHash(physicsProfileHash);
		if(carClassesEntity == null) {
			return;
		}
		Set<PerformancePartEntity> performanceParts = customCarEntity.getPerformanceParts();
		int topSpeed = 0;
		int accel = 0;
		int handling = 0;
		for (PerformancePartEntity performancePartEntity : performanceParts) {
			int perfHash = performancePartEntity.getPerformancePartAttribHash();
			if (perfHash == 1158503258) {
				customCarEntity.setRaceFilter(0);
			}
			if (perfHash == 1918528298) {
				customCarEntity.setRaceFilter(1);
			}
			if (perfHash == 2068482365) {
				customCarEntity.setRaceFilter(2);
			}
			if (perfHash == -1099229576) {
				customCarEntity.setRaceFilter(3);
			}
			if (perfHash == 1044347404) {
				customCarEntity.setRaceFilter(4);
			}
			ProductEntity productEntity = productDAO.findByHash(perfHash);
			topSpeed = productEntity.getTopSpeed().intValue() + topSpeed;
			accel = productEntity.getAccel().intValue() + accel;
			handling = productEntity.getHandling().intValue() + handling;
		}
		float tt = (float) (topSpeed * 0.01);
		float ta = (float) (accel * 0.01);
		float th = (float) (handling * 0.01);
		float totalChanges = 1 / (((tt + ta + th) * 0.666666666666666f) + 1f);
		tt = tt * totalChanges;
		ta = ta * totalChanges;
		th = th * totalChanges;
		float finalConstant = 1 - tt - ta - th;

		Float finalTopSpeed1 = carClassesEntity.getTsVar1().floatValue() * th;
		Float finalTopSpeed2 = carClassesEntity.getTsVar2().floatValue() * ta;
		Float finalTopSpeed3 = carClassesEntity.getTsVar3().floatValue() * tt;
		Float finalTopSpeed = (finalConstant * carClassesEntity.getTsStock().floatValue()) + finalTopSpeed1.floatValue() + finalTopSpeed2.floatValue()
				+ finalTopSpeed3.floatValue();

//		System.out.println(finalTopSpeed.intValue());

		Float finalAccel1 = carClassesEntity.getAcVar1().floatValue() * th;
		Float finalAccel2 = carClassesEntity.getAcVar2().floatValue() * ta;
		Float finalAccel3 = carClassesEntity.getAcVar3().floatValue() * tt;
		Float finalAccel = (finalConstant * carClassesEntity.getAcStock().floatValue()) + finalAccel1.floatValue() + finalAccel2.floatValue()
				+ finalAccel3.floatValue();

//		System.out.println(finalAccel.intValue());

		Float finalHandling1 = carClassesEntity.getHaVar1().floatValue() * th;
		Float finalHandling2 = carClassesEntity.getHaVar2().floatValue() * ta;
		Float finalHandling3 = carClassesEntity.getHaVar3().floatValue() * tt;
		Float finalHandling = (finalConstant * carClassesEntity.getHaStock().floatValue()) + finalHandling1.floatValue() + finalHandling2.floatValue()
				+ finalHandling3.floatValue();

//		System.out.println(finalHandling.intValue());

		Float finalClass = (finalTopSpeed.intValue() + finalAccel.intValue() + finalHandling.intValue()) / 3f;
//		System.out.println(finalClass.intValue());
		int finalClassInt = finalClass.intValue();

		// move to new method
		int carclassHash = 0; // NPC cars, Drift-Spec
		if (finalClassInt >= 40 && finalClassInt < 250) {
			carclassHash = 872416321;
		} else if (finalClassInt >= 250 && finalClassInt < 400) {
			carclassHash = 415909161;
		} else if (finalClassInt >= 400 && finalClassInt < 500) {
			carclassHash = 1866825865;
		} else if (finalClassInt >= 500 && finalClassInt < 600) {
			carclassHash = -406473455;
		} else if (finalClassInt >= 600 && finalClassInt < 750) {
			carclassHash = -405837480;
		} else if (finalClassInt >= 750) {
			carclassHash = -2142411446;
		}

		customCarEntity.setCarClassHash(carclassHash);
		customCarEntity.setRating(finalClassInt);
	}

	private void disableItem(ProductEntity productEntity) {
		Boolean disableItemAfterBuy = parameterBO.getBoolParam("DISABLE_ITEM_AFTER_BUY");
		if (disableItemAfterBuy) {
			productEntity.setEnabled(false);
			productDAO.update(productEntity);
		}
	}

	private void disableItem(VinylProductEntity vinylProductEntity) {
		Boolean disableItemAfterBuy = parameterBO.getBoolParam("DISABLE_ITEM_AFTER_BUY");
		if (disableItemAfterBuy) {
			vinylProductEntity.setEnabled(false);
			vinylProductDAO.update(vinylProductEntity);
		}
	}

	public void updateEconomy(CommerceOp commerceOp, List<BasketItemTrans> basketItemTransList, CommerceSessionTrans commerceSessionTrans,
			CarSlotEntity defaultCarEntity) {
		if (parameterBO.getBoolParam("ENABLE_ECONOMY")) {
			OwnedCarTrans ownedCarTrans = OwnedCarConverter.entity2Trans(defaultCarEntity.getOwnedCar());
			CustomCarTrans customCarTransDB = ownedCarTrans.getCustomCar();
			CustomCarTrans customCarTrans = commerceSessionTrans.getUpdatedCar().getCustomCar();
			int basketTotalValue;
			if (CommerceOp.VINYL.equals(commerceOp)) {
				basketTotalValue = getVinylTotalValue(basketItemTransList);
			} else {
				basketTotalValue = getBasketTotalValue(basketItemTransList);
			}
			int resellTotalValue = 0;
			switch (commerceOp) {
			case PERFORMANCE:
				List<PerformancePartTrans> performancePartTransDB = customCarTransDB.getPerformanceParts().getPerformancePartTrans();
				List<PerformancePartTrans> performancePartTrans = customCarTrans.getPerformanceParts().getPerformancePartTrans();
				ArrayList<PerformancePartTrans> performancePartTransListTmp = new ArrayList<>(performancePartTransDB);
				List<PerformancePartTrans> performancePartsFromBasket = inventoryBO.getPerformancePartsFromBasket(basketItemTransList);
				performancePartTransListTmp.removeAll(performancePartsFromBasket);
				performancePartTransListTmp.removeAll(performancePartTrans);
				for (PerformancePartTrans performancePartTransTmp : performancePartTransListTmp) {
					ProductEntity productEntity = productDAO.findByHash(performancePartTransTmp.getPerformancePartAttribHash());
					if (productEntity != null) {
						resellTotalValue = Integer.sum(resellTotalValue, productEntity.getResalePrice());
					} else {
						System.err.println("INVALID HASH: [" + performancePartTransTmp.getPerformancePartAttribHash() + "]");
					}
				}
				break;
			case SKILL:
				List<SkillModPartTrans> skillModPartTransDB = customCarTransDB.getSkillModParts().getSkillModPartTrans();
				List<SkillModPartTrans> skillModPartTrans = customCarTrans.getSkillModParts().getSkillModPartTrans();
				List<SkillModPartTrans> skillModPartTransListTmp = new ArrayList<>(skillModPartTransDB);
				List<SkillModPartTrans> skillModPartsFromBasket = inventoryBO.getSkillModPartsFromBasket(basketItemTransList);
				skillModPartTransListTmp.removeAll(skillModPartsFromBasket);
				skillModPartTransListTmp.removeAll(skillModPartTrans);
				for (SkillModPartTrans skillModPartTransTmp : skillModPartTransListTmp) {
					ProductEntity productEntity = productDAO.findByHash(skillModPartTransTmp.getSkillModPartAttribHash());
					if (productEntity != null) {
						resellTotalValue = Integer.sum(resellTotalValue, productEntity.getResalePrice());
					} else {
						System.err.println("INVALID HASH: [" + skillModPartTransTmp.getSkillModPartAttribHash() + "]");
					}
				}
				break;
			case VISUAL:
                // FIXME Does not work for now, even not goes to UpdateEconomy
//				if (parameterBO.getIntParam("ITEM_RESALEREWARD") > 0) {
//                	resellTotalValue = Float.sum(resellTotalValue, parameterBO.getIntParam("ITEM_RESALEREWARD"));
//                	System.out.println("DEBUG VISUAL ResellValue: " + resellTotalValue);
//                }
				break;
			default:
				break;
			}
			List<EntitlementItemTrans> entitlementItemTransList = commerceSessionTrans.getEntitlementsToSell().getItems().getEntitlementItemTrans();
			if (entitlementItemTransList != null && !entitlementItemTransList.isEmpty()) {
				for (EntitlementItemTrans entitlementItemTransTmp : entitlementItemTransList) {
					String entitlementId = entitlementItemTransTmp.getEntitlementId();
					Long personaId = defaultCarEntity.getPersona().getPersonaId();
					InventoryItemEntity inventoryItem = inventoryItemDAO.findByEntitlementTagAndPersona(personaId, entitlementId);
					Integer hash = inventoryItem.getHash();
					ProductEntity productEntity = productDAO.findByHash(hash);
					if (productEntity != null) {
						resellTotalValue = Integer.sum(resellTotalValue, productEntity.getResalePrice());
					}
				}
			}
			int result = Integer.sum(basketTotalValue, (resellTotalValue * -1)) * -1;
//			System.out.println("basket: [" + basketTotalValue + "]");
//			System.out.println("resell: [" + resellTotalValue + "]");
//			System.out.println("result: [" + result + "]");
			PersonaEntity persona = defaultCarEntity.getPersona();
			int cash = persona.getCash();
			persona.setCash(Integer.sum(cash, result));
			personaDAO.update(persona);
		}
	}

	private int getVinylTotalValue(List<BasketItemTrans> basketItemTransList) {
		int price = 0;
		for (BasketItemTrans basketItemTrans : basketItemTransList) {
			VinylProductEntity vinylProductEntity = vinylProductDAO.findByProductId(basketItemTrans.getProductId());
			if (vinylProductEntity != null) {
				price = Integer.sum(price, vinylProductEntity.getPrice());
				disableItem(vinylProductEntity);
			} else {
				System.err.println("product [" + basketItemTrans.getProductId() + "] not found");
			}
		}
		return price;
	}

	private int getBasketTotalValue(List<BasketItemTrans> basketItemTransList) {
		int price = 0;
		for (BasketItemTrans basketItemTrans : basketItemTransList) {
			ProductEntity productEntity = productDAO.findByProductId(basketItemTrans.getProductId());
			if (productEntity != null) {
				price = Integer.sum(price, productEntity.getPrice());
				disableItem(productEntity);
			} else {
				System.err.println("product [" + basketItemTrans.getProductId() + "] not found");
			}
		}
		return price;
	}
	
	public int limitBoostConversion (int money, int maxCashLimit, int sbConvCashValue, int sbConvAmount, UserEntity userEntity,
			Long personaId, boolean inGame) {
		int sbConvTries = 0;
		while (money >= maxCashLimit) { // Convert the over-limit money in SpeedBoost
			money = money - sbConvCashValue;
			userEntity.setBoost(userEntity.getBoost() + sbConvAmount);
			sbConvTries++;
		}
		if (inGame) {
			userDAO.update(userEntity); // Update the User data
			int sbConvValue = sbConvAmount * sbConvTries;
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### Money limit - some of your money has been exchanged to " + sbConvValue + " SpeedBoost."), personaId);
		}
		return money;
	}

}
