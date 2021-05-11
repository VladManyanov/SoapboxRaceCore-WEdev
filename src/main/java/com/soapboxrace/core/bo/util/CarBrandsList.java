package com.soapboxrace.core.bo.util;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.AchievementBrandsDAO;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.jpa.AchievementBrandsEntity;

@Stateless
public class CarBrandsList {
	
	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
	private AchievementBrandsDAO achievementBrandsDAO;

	public Integer[] getBrandInfo(int carHash, Long personaId) {
		AchievementBrandsEntity achievementBrandsEntity = achievementBrandsDAO.findById(personaId);
		String brandName = carClassesDAO.findByHash(carHash).getManufactor();
		int brandAchievementDefId = 0;
		int winsCount = 0;
		Integer[] brandInfo = new Integer[2];
		switch (brandName) {
		// Default achievement brands set
		case "ALFA ROMEO":
			brandAchievementDefId = 28;
			winsCount = achievementBrandsEntity.getAlfaRomeoWins() + 1;
			achievementBrandsEntity.setAlfaRomeoWins(winsCount);
			break;
		case "ASTON MARTIN":
			brandAchievementDefId = 29;
			winsCount = achievementBrandsEntity.getAstonMartinWins() + 1;
			achievementBrandsEntity.setAstonMartinWins(winsCount);
			break;
		case "AUDI":
			brandAchievementDefId = 30;
			winsCount = achievementBrandsEntity.getAudiWins() + 1;
			achievementBrandsEntity.setAudiWins(winsCount);
			break;
		case "BENTLEY":
			brandAchievementDefId = 31;
			winsCount = achievementBrandsEntity.getBentleyWins() + 1;
			achievementBrandsEntity.setBentleyWins(winsCount);
			break;
		case "BMW":
			brandAchievementDefId = 32;
			winsCount = achievementBrandsEntity.getBMWWins() + 1;
			achievementBrandsEntity.setBMWWins(winsCount);
			break;
		case "CADILLAC":
			brandAchievementDefId = 33;
			winsCount = achievementBrandsEntity.getCadillacWins() + 1;
			achievementBrandsEntity.setCadillacWins(winsCount);
			break;
		case "CATERHAM":
			brandAchievementDefId = 34;
			winsCount = achievementBrandsEntity.getCaterhamWins() + 1;
			achievementBrandsEntity.setCaterhamWins(winsCount);
			break;
		case "CHEVROLET":
			brandAchievementDefId = 35;
			winsCount = achievementBrandsEntity.getChevroletWins() + 1;
			achievementBrandsEntity.setChevroletWins(winsCount);
			break;
		case "CHRYSLER":
			brandAchievementDefId = 36;
			winsCount = achievementBrandsEntity.getChryslerWins() + 1;
			achievementBrandsEntity.setChryslerWins(winsCount);
			break;
		case "DODGE":
			brandAchievementDefId = 37;
			winsCount = achievementBrandsEntity.getDodgeWins() + 1;
			achievementBrandsEntity.setDodgeWins(winsCount);
			break;
		case "FORD":
			brandAchievementDefId = 38;
			winsCount = achievementBrandsEntity.getFordWins() + 1;
			achievementBrandsEntity.setFordWins(winsCount);
			break;
		case "FORD SHELBY":
			brandAchievementDefId = 39;
			winsCount = achievementBrandsEntity.getFordShelbyWins() + 1;
			achievementBrandsEntity.setFordShelbyWins(winsCount);
			break;
		case "HUMMER":
			brandAchievementDefId = 40;
			winsCount = achievementBrandsEntity.getHummerWins() + 1;
			achievementBrandsEntity.setHummerWins(winsCount);
			break;
		case "INFINITI":
			brandAchievementDefId = 41;
			winsCount = achievementBrandsEntity.getInfinitiWins() + 1;
			achievementBrandsEntity.setInfinitiWins(winsCount);
			break;
		case "JAGUAR":
			brandAchievementDefId = 42;
			winsCount = achievementBrandsEntity.getJaguarWins() + 1;
			achievementBrandsEntity.setJaguarWins(winsCount);
			break;
		case "JEEP":
			brandAchievementDefId = 43;
			winsCount = achievementBrandsEntity.getJeepWins() + 1;
			achievementBrandsEntity.setJeepWins(winsCount);
			break;
		case "KOENIGSEGG":
			brandAchievementDefId = 44;
			winsCount = achievementBrandsEntity.getKoenigseggWins() + 1;
			achievementBrandsEntity.setKoenigseggWins(winsCount);
			break;
		case "LAMBORGHINI":
			brandAchievementDefId = 45;
			winsCount = achievementBrandsEntity.getLamborghiniWins() + 1;
			achievementBrandsEntity.setLamborghiniWins(winsCount);
			break;
		case "LANCIA":
			brandAchievementDefId = 46;
			winsCount = achievementBrandsEntity.getLanciaWins() + 1;
			achievementBrandsEntity.setLanciaWins(winsCount);
			break;
		case "LEXUS":
			brandAchievementDefId = 47;
			winsCount = achievementBrandsEntity.getLexusWins() + 1;
			achievementBrandsEntity.setLexusWins(winsCount);
			break;
		case "LOTUS":
			brandAchievementDefId = 48;
			winsCount = achievementBrandsEntity.getLotusWins() + 1;
			achievementBrandsEntity.setLotusWins(winsCount);
			break;
		case "MARUSSIA":
			brandAchievementDefId = 49;
			winsCount = achievementBrandsEntity.getMarussiaWins() + 1;
			achievementBrandsEntity.setMarussiaWins(winsCount);
			break;
		case "MAZDA":
			brandAchievementDefId = 50;
			winsCount = achievementBrandsEntity.getMazdaWins() + 1;
			achievementBrandsEntity.setMazdaWins(winsCount);
			break;
		case "MCLAREN":
			brandAchievementDefId = 51;
			winsCount = achievementBrandsEntity.getMclarenWins() + 1;
			achievementBrandsEntity.setMclarenWins(winsCount);
			break;
		case "MERCEDES-BENZ":
			brandAchievementDefId = 52;
			winsCount = achievementBrandsEntity.getMercedesBenzWins() + 1;
			achievementBrandsEntity.setMercedesBenzWins(winsCount);
			break;
		case "MITSUBISHI":
			brandAchievementDefId = 53;
			winsCount = achievementBrandsEntity.getMitsubishiWins() + 1;
			achievementBrandsEntity.setMitsubishiWins(winsCount);
			break;
		case "NISSAN":
			brandAchievementDefId = 54;
			winsCount = achievementBrandsEntity.getNissanWins() + 1;
			achievementBrandsEntity.setNissanWins(winsCount);
			break;
		case "PAGANI":
			brandAchievementDefId = 55;
			winsCount = achievementBrandsEntity.getPaganiWins() + 1;
			achievementBrandsEntity.setPaganiWins(winsCount);
			break;
		case "PLYMOUTH":
			brandAchievementDefId = 56;
			winsCount = achievementBrandsEntity.getPlymouthWins() + 1;
			achievementBrandsEntity.setPlymouthWins(winsCount);
			break;
		case "PONTIAC":
			brandAchievementDefId = 57;
			winsCount = achievementBrandsEntity.getPontiacWins() + 1;
			achievementBrandsEntity.setPontiacWins(winsCount);
			break;
		case "PORSCHE":
			brandAchievementDefId = 58;
			winsCount = achievementBrandsEntity.getPorscheWins() + 1;
			achievementBrandsEntity.setPorscheWins(winsCount);
			break;
		case "RENAULT":
			brandAchievementDefId = 59;
			winsCount = achievementBrandsEntity.getRenaultWins() + 1;
			achievementBrandsEntity.setRenaultWins(winsCount);
			break;
		case "SCION":
			brandAchievementDefId = 60;
			winsCount = achievementBrandsEntity.getScionWins() + 1;
			achievementBrandsEntity.setScionWins(winsCount);
			break;
		case "SHELBY":
			brandAchievementDefId = 61;
			winsCount = achievementBrandsEntity.getShelbyWins() + 1;
			achievementBrandsEntity.setShelbyWins(winsCount);
			break;
		case "SUBARU":
			brandAchievementDefId = 62;
			winsCount = achievementBrandsEntity.getSubaruWins() + 1;
			achievementBrandsEntity.setSubaruWins(winsCount);
			break;
		case "TOYOTA":
			brandAchievementDefId = 63;
			winsCount = achievementBrandsEntity.getToyotaWins() + 1;
			achievementBrandsEntity.setToyotaWins(winsCount);
			break;
		case "VAUXHALL":
			brandAchievementDefId = 64;
			winsCount = achievementBrandsEntity.getVauxhallWins() + 1;
			achievementBrandsEntity.setVauxhallWins(winsCount);
			break;
		case "VOLKSWAGEN":
			brandAchievementDefId = 65;
			winsCount = achievementBrandsEntity.getVolkswagenWins() + 1;
			achievementBrandsEntity.setVolkswagenWins(winsCount);
			break;
		// Additional brands
		case "BUGATTI":
			brandAchievementDefId = 107;
			winsCount = achievementBrandsEntity.getBugattiWins() + 1;
			achievementBrandsEntity.setBugattiWins(winsCount);
			break;
		case "FERRARI":
			brandAchievementDefId = 108;
			winsCount = achievementBrandsEntity.getFerrariWins() + 1;
			achievementBrandsEntity.setFerrariWins(winsCount);
			break;
		case "FIAT":
			brandAchievementDefId = 109;
			winsCount = achievementBrandsEntity.getFiatWins() + 1;
			achievementBrandsEntity.setFiatWins(winsCount);
			break;
		case "HONDA":
			brandAchievementDefId = 110;
			winsCount = achievementBrandsEntity.getHondaWins() + 1;
			achievementBrandsEntity.setHondaWins(winsCount);
			break;
		case "MASERATI":
			brandAchievementDefId = 111;
			winsCount = achievementBrandsEntity.getMaseratiWins() + 1;
			achievementBrandsEntity.setMaseratiWins(winsCount);
			break;
		case "NFS":
			brandAchievementDefId = 112;
			winsCount = achievementBrandsEntity.getNFSWins() + 1;
			achievementBrandsEntity.setNFSWins(winsCount);
			break;
		case "SMART":
			brandAchievementDefId = 113;
			winsCount = achievementBrandsEntity.getSmartWins() + 1;
			achievementBrandsEntity.setSmartWins(winsCount);
			break;
		case "TESLA":
			brandAchievementDefId = 114;
			winsCount = achievementBrandsEntity.getTeslaWins() + 1;
			achievementBrandsEntity.setTeslaWins(winsCount);
			break;
		case "FLANKER":
			brandAchievementDefId = 115;
			winsCount = achievementBrandsEntity.getFlankerWins() + 1;
			achievementBrandsEntity.setFlankerWins(winsCount);
			break;
		case "BUICK":
			brandAchievementDefId = 117;
			winsCount = achievementBrandsEntity.getBuickWins() + 1;
			achievementBrandsEntity.setBuickWins(winsCount);
			break;
		case "POLESTAR":
			brandAchievementDefId = 119;
			winsCount = achievementBrandsEntity.getPolestarWins() + 1;
			achievementBrandsEntity.setPolestarWins(winsCount);
			break;
		}
		achievementBrandsDAO.update(achievementBrandsEntity);
		brandInfo[0] = brandAchievementDefId;
		brandInfo[1] = winsCount;
		return brandInfo;
	}

}