package com.soapboxrace.core.bo;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.soapboxrace.core.bo.util.AchievementType;
import com.soapboxrace.core.bo.util.RewardDestinyType;
import com.soapboxrace.core.bo.util.RewardType;
import com.soapboxrace.core.bo.util.StringListConverter;
import com.soapboxrace.core.dao.AchievementBrandsDAO;
import com.soapboxrace.core.dao.AchievementDAO;
import com.soapboxrace.core.dao.AchievementPersonaDAO;
import com.soapboxrace.core.dao.AchievementRankDAO;
import com.soapboxrace.core.dao.AchievementStateDAO;
import com.soapboxrace.core.dao.BadgeDefinitionDAO;
import com.soapboxrace.core.dao.BadgePersonaDAO;
import com.soapboxrace.core.dao.BasketDefinitionDAO;
import com.soapboxrace.core.dao.CarClassesDAO;
import com.soapboxrace.core.dao.CarSlotDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.RewardDropDAO;
import com.soapboxrace.core.dao.TokenSessionDAO;
import com.soapboxrace.core.dao.UserDAO;
import com.soapboxrace.core.engine.EngineException;
import com.soapboxrace.core.engine.EngineExceptionCode;
import com.soapboxrace.core.jpa.AchievementBrandsEntity;
import com.soapboxrace.core.jpa.AchievementDefinitionEntity;
import com.soapboxrace.core.jpa.AchievementPersonaEntity;
import com.soapboxrace.core.jpa.AchievementRankEntity;
import com.soapboxrace.core.jpa.AchievementStateEntity;
import com.soapboxrace.core.jpa.BadgeDefinitionEntity;
import com.soapboxrace.core.jpa.BadgePersonaEntity;
import com.soapboxrace.core.jpa.CarClassesEntity;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.PersonaMissionsEntity;
import com.soapboxrace.core.jpa.ProductEntity;
import com.soapboxrace.core.jpa.RewardDropEntity;
import com.soapboxrace.core.jpa.TokenSessionEntity;
import com.soapboxrace.core.jpa.TreasureHuntEntity;
import com.soapboxrace.core.jpa.UserEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.jaxb.http.AchievementDefinitionPacket;
import com.soapboxrace.jaxb.http.AchievementRankPacket;
import com.soapboxrace.jaxb.http.AchievementRewards;
import com.soapboxrace.jaxb.http.AchievementState;
import com.soapboxrace.jaxb.http.AchievementsPacket;
import com.soapboxrace.jaxb.http.ArbitrationPacket;
import com.soapboxrace.jaxb.http.ArrayOfAchievementDefinitionPacket;
import com.soapboxrace.jaxb.http.ArrayOfAchievementRankPacket;
import com.soapboxrace.jaxb.http.ArrayOfBadgeDefinitionPacket;
import com.soapboxrace.jaxb.http.ArrayOfCommerceItemTrans;
import com.soapboxrace.jaxb.http.ArrayOfInventoryItemTrans;
import com.soapboxrace.jaxb.http.ArrayOfOwnedCarTrans;
import com.soapboxrace.jaxb.http.ArrayOfWalletTrans;
import com.soapboxrace.jaxb.http.BadgeDefinitionPacket;
import com.soapboxrace.jaxb.http.CommerceItemTrans;
import com.soapboxrace.jaxb.http.CommerceResultStatus;
import com.soapboxrace.jaxb.http.DragArbitrationPacket;
import com.soapboxrace.jaxb.http.InvalidBasketTrans;
import com.soapboxrace.jaxb.http.PursuitArbitrationPacket;
import com.soapboxrace.jaxb.http.RouteArbitrationPacket;
import com.soapboxrace.jaxb.http.StatConversion;
import com.soapboxrace.jaxb.http.TeamEscapeArbitrationPacket;
import com.soapboxrace.jaxb.http.WalletTrans;
import com.soapboxrace.jaxb.xmpp.AchievementAwarded;
import com.soapboxrace.jaxb.xmpp.AchievementProgress;
import com.soapboxrace.jaxb.xmpp.AchievementsAwarded;

@Stateless
public class AchievementsBO {

	@EJB
	private AchievementDAO achievementDAO;

	@EJB
	private AchievementRankDAO achievementRankDAO;

	@EJB
	private BadgeDefinitionDAO badgeDefinitionDAO;
	
	@EJB
	private BadgePersonaDAO badgePersonaDAO;

	@EJB
	private AchievementPersonaDAO achievementPersonaDAO;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private PersonaDAO personaDAO;

	@EJB
	private AchievementStateDAO achievementStateDAO;

	@EJB
	private RewardDropDAO rewardDropDAO;

	@EJB
	private DropBO dropBO;

	@EJB
	private InventoryBO inventoryBO;

	@EJB
	private BasketDefinitionDAO basketDefinitionsDAO;

	@EJB
	private CarSlotDAO carSlotDAO;

	@EJB
	private TokenSessionDAO tokenSessionDAO;

	@EJB
	private LobbyDAO lobbyDAO;
	
	@EJB
	private PersonaBO personaBO;
	
	@EJB
	private UserDAO userDAO;
	
	@EJB
	private AchievementBrandsDAO achievementBrandsDAO;
	
	@EJB
	private EventSessionDAO eventSessionDAO;
	
	@EJB
	private BasketBO basketBO;
	
	@EJB
	private CarClassesDAO carClassesDAO;
	
	@EJB
	private PersonaMissionsBO personaMissionsBO;
	
	@EJB
	private StringListConverter stringListConverter;

	/**
	 * Get the achievements information for persona, including all available entries from DB
	 * @param personaId - ID of player persona
	 * @author Nilzao
	 */
	public AchievementsPacket loadall(Long personaId) {
		AchievementsPacket achievementsPacket = new AchievementsPacket();
		ArrayOfBadgeDefinitionPacket arrayOfBadgeDefinitionPacket = new ArrayOfBadgeDefinitionPacket();
		List<BadgeDefinitionPacket> badgeDefinitionPacketList = arrayOfBadgeDefinitionPacket.getBadgeDefinitionPacket();

		// Prepare all available achievement badges 
		List<BadgeDefinitionEntity> allBadges = badgeDefinitionDAO.getAll();
		for (BadgeDefinitionEntity badgeDefinitionEntity : allBadges) {
			BadgeDefinitionPacket badgeDefinitionPacket = new BadgeDefinitionPacket();
			badgeDefinitionPacket.setBackground(badgeDefinitionEntity.getBackground());
			badgeDefinitionPacket.setBadgeDefinitionId(badgeDefinitionEntity.getId().intValue());
			badgeDefinitionPacket.setBorder(badgeDefinitionEntity.getBorder());
			badgeDefinitionPacket.setDescription(badgeDefinitionEntity.getDescription());
			badgeDefinitionPacket.setIcon(badgeDefinitionEntity.getIcon());
			badgeDefinitionPacket.setName(badgeDefinitionEntity.getName());
			badgeDefinitionPacketList.add(badgeDefinitionPacket);
		}

		achievementsPacket.setBadges(arrayOfBadgeDefinitionPacket);

		ArrayOfAchievementDefinitionPacket arrayOfAchievementDefinitionPacket = new ArrayOfAchievementDefinitionPacket();
		List<AchievementDefinitionPacket> achievementDefinitionPacketList = arrayOfAchievementDefinitionPacket.getAchievementDefinitionPacket();
		List<AchievementDefinitionEntity> allAchievements = achievementDAO.getAll();

		PersonaEntity personaEntity = personaDAO.findById(personaId);
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		AchievementBrandsEntity achievementBrandsEntity = achievementBrandsDAO.findByPersona(personaId);
		if (achievementPersonaEntity.getCollectorCars() == 0) { // Re-calc car count if player login for first time with enabled Collector achievement
			int carsAmount = carSlotDAO.countPersonaCars(personaId).intValue();
			forceApplyCollector(carsAmount, personaEntity);
		}

		// Get the ranks & information about achievement stages
		for (AchievementDefinitionEntity achievementDefinitionEntity : allAchievements) {
			int displayAchievementId = 0;
			Long currentValue = getCurrentValue(achievementDefinitionEntity, achievementPersonaEntity, achievementBrandsEntity, personaEntity);
			AchievementDefinitionPacket achievementDefinitionPacket = new AchievementDefinitionPacket();
			achievementDefinitionPacket.setAchievementDefinitionId(achievementDefinitionEntity.getId().intValue());

			ArrayOfAchievementRankPacket arrayOfAchievementRankPacket = new ArrayOfAchievementRankPacket();
			List<AchievementRankPacket> achievementRankPacketList = arrayOfAchievementRankPacket.getAchievementRankPacket();

			// List<AchievementRankEntity> ranks = achievementDefinitionEntity.getRanks();
			List<AchievementRankEntity> ranks = achievementRankDAO.findByAchievementDefinitionId(achievementDefinitionEntity.getId());
			long tmpRankValue = 0;
			for (AchievementRankEntity achievementRankEntity : ranks) {
				AchievementRankPacket achievementRankPacket = new AchievementRankPacket();
				achievementRankPacket.setAchievementRankId(achievementRankEntity.getId().intValue());
				achievementRankPacket.setIsRare(achievementRankEntity.isRare());
				achievementRankPacket.setPoints(achievementRankEntity.getPoints());
				achievementRankPacket.setRank(achievementRankEntity.getRank());
				achievementRankPacket.setRarity(achievementRankEntity.getRarity());
				achievementRankPacket.setRewardDescription(achievementRankEntity.getRewardDescription());
				achievementRankPacket.setRewardType(achievementRankEntity.getRewardType());
				// achievementRankPacket.setRewardDescription("GM_ACHIEVEMENT_00000165");
				achievementRankPacket.setRewardVisualStyle(achievementRankEntity.getRewardVisualStyle());
				achievementRankPacket.setThresholdValue(achievementRankEntity.getThresholdValue());

				AchievementStateEntity personaAchievementRankState = achievementStateDAO.findByPersonaAchievementRank(personaEntity, achievementRankEntity);
				if (personaAchievementRankState != null) {
					try {
						displayAchievementId = personaAchievementRankState.getId();
						LocalDateTime achievedOn = personaAchievementRankState.getAchievedOn();
						GregorianCalendar gcal = GregorianCalendar.from(achievedOn.atZone(ZoneId.systemDefault()));
						XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
						achievementRankPacket.setAchievedOn(xmlCalendar);
					} catch (Exception e) {
						System.err.println("xml calendar str error");
					}
					achievementRankPacket.setState(personaAchievementRankState.getAchievementState());
				} else {
					if (currentValue.longValue() > tmpRankValue || tmpRankValue == 0l) {
						achievementRankPacket.setState(AchievementState.IN_PROGRESS);
					} else {
						achievementRankPacket.setState(AchievementState.LOCKED);
					}
					tmpRankValue = achievementRankEntity.getThresholdValue().longValue();
				}
				achievementRankPacketList.add(achievementRankPacket);
			}
			boolean displayAchievement = true;
			if (!achievementDefinitionEntity.isVisible() && displayAchievementId == 0) { 
				displayAchievement = false; // Display this achievement only for players, who get it earlier
			}
			achievementDefinitionPacket.setAchievementRanks(arrayOfAchievementRankPacket);

			achievementDefinitionPacket.setBadgeDefinitionId(achievementDefinitionEntity.getBadgeDefinition().getId().intValue());
			achievementDefinitionPacket.setCanProgress(achievementDefinitionEntity.isCanProgress());
			achievementDefinitionPacket.setCurrentValue(currentValue);
			achievementDefinitionPacket.setIsVisible(displayAchievement);
			achievementDefinitionPacket.setProgressText(achievementDefinitionEntity.getProgressText());
			achievementDefinitionPacket.setStatConversion(StatConversion.valueOf(achievementDefinitionEntity.getStatConversion()));
			achievementDefinitionPacketList.add(achievementDefinitionPacket);
		}
		achievementsPacket.setDefinitions(arrayOfAchievementDefinitionPacket);
		return achievementsPacket;
	}

	/**
	 * Get the current values of the requested achievement
	 * @param achievementDefinitionEntity - achievement definition
	 * @param achievementPersonaEntity - player persona's achievement values
	 * @author Nilzao, Hypercycle
	 */
	private Long getCurrentValue(AchievementDefinitionEntity achievementDefinitionEntity, AchievementPersonaEntity achievementPersonaEntity, 
			AchievementBrandsEntity achievementBrandsEntity, PersonaEntity personaEntity) {
		int intValue = achievementDefinitionEntity.getId().intValue();
		AchievementType achievementType = AchievementType.valueOf(intValue);
		
		// Car brands IDs can be taken from CarBrandType enum class
		switch (achievementType) {
		case AFTERMARKET_SPECIALIST:
			return 0l;
		case AIRTIME:
			return achievementPersonaEntity.getEventAerialTime();
		case ALFA_ROMEO_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getAlfaRomeoWins()).longValue();
		case ASTON_MARTIN_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getAstonMartinWins()).longValue();
		case AUDI_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getAudiWins()).longValue();
		case A_CLASS_CHAMPION:
			int restrictedClassAWins = achievementPersonaEntity.getRestrictedClassAWins();
			return Integer.valueOf(restrictedClassAWins).longValue();
		case BENTLEY_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getBentleyWins()).longValue();
		case BMW_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getBMWWins()).longValue();
		case B_CLASS_CHAMPION:
			int restrictedClassBWins = achievementPersonaEntity.getRestrictedClassBWins();
			return Integer.valueOf(restrictedClassBWins).longValue();
		case CADILLAC_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getCadillacWins()).longValue();
		case CAR_ARTIST:
			return 0l;
		case CATERHAM_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getCaterhamWins()).longValue();
		case CHEVROLET_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getChevroletWins()).longValue();
		case CHRYSLER_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getChryslerWins()).longValue();
		case COLLECTOR:
			int collectorCars = achievementPersonaEntity.getCollectorCars();
			return Integer.valueOf(collectorCars).longValue();
		case CREW_RACER:
			int privateRaces = achievementPersonaEntity.getPrivateRaces();
			return Integer.valueOf(privateRaces).longValue();
		case C_CLASS_CHAMPION:
			int restrictedClassCWins = achievementPersonaEntity.getRestrictedClassCWins();
			return Integer.valueOf(restrictedClassCWins).longValue();
		case DAILY_HUNTER:
			int dailyTreasureHunts = achievementPersonaEntity.getDailyTreasureHunts();
			return Integer.valueOf(dailyTreasureHunts).longValue();
		case DEVELOPER:
			return 0l;
		case DODGE_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getDodgeWins()).longValue();
		case DRAG_RACER:
			int mpDragWins = achievementPersonaEntity.getMpDragWins();
			return Integer.valueOf(mpDragWins).longValue();
		case D_CLASS_CHAMPION:
			int restrictedClassDWins = achievementPersonaEntity.getRestrictedClassDWins();
			return Integer.valueOf(restrictedClassDWins).longValue();
		case ENEMY_OF_THE_STATE:
			return achievementPersonaEntity.getPursuitCostToState();
		case EXPLORE_MODDER:
			return 0l;
		case E_CLASS_CHAMPION:
			int restrictedClassEWins = achievementPersonaEntity.getRestrictedClassEWins();
			return Integer.valueOf(restrictedClassEWins).longValue();
		case FORD_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getFordWins()).longValue();
		case FORD_SHELBY_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getFordShelbyWins()).longValue();
		case FRESH_COAT:
			return 0l;
		case GETAWAY_DRIVER:
			int teamScapeWins = achievementPersonaEntity.getTeamScapeWins();
			return Integer.valueOf(teamScapeWins).longValue();
		case HEAVY_HITTER:
			int teamScapePoliceDown = achievementPersonaEntity.getTeamScapePoliceDown();
			return Integer.valueOf(teamScapePoliceDown).longValue();
		case HUMMER_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getHummerWins()).longValue();
		case INFINITI_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getInfinitiWins()).longValue();
		case JAGUAR_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getJaguarWins()).longValue();
		case JEEP_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getJeepWins()).longValue();
		case KOENIGSEGG_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getKoenigseggWins()).longValue();
		case LAMBORGHINI_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getLamborghiniWins()).longValue();
		case LANCIA_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getLanciaWins()).longValue();
		case LEGENDARY_DRIVER:
			int score = achievementPersonaEntity.getPersona().getScore();
			return Integer.valueOf(score).longValue();
		case LEVEL_UP:
			int level = achievementPersonaEntity.getPersona().getLevel();
			return Integer.valueOf(level).longValue();
		case LEXUS_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getLexusWins()).longValue();
		case LONG_HAUL:
			return achievementPersonaEntity.getEventMeters();
		case LOTUS_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getLotusWins()).longValue();
		case MARUSSIA_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getMarussiaWins()).longValue();
		case MAZDA_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getMazdaWins()).longValue();
		case MCLAREN_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getMclarenWins()).longValue();
		case MERCEDES_BENZ_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getMercedesBenzWins()).longValue();
		case MITSUBISHI_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getMitsubishiWins()).longValue();
		case NISSAN_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getNissanWins()).longValue();
		case OPEN_BETA:
			return 0l;
		case OUTLAW:
			int pursuitWins = achievementPersonaEntity.getPursuitWins();
			return Integer.valueOf(pursuitWins).longValue();
		case PAGANI_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getPaganiWins()).longValue();
		case PAYDAY:
			int totalIncomeCash = achievementPersonaEntity.getTotalIncomeCash();
			return Integer.valueOf(totalIncomeCash).longValue();
		case PLYMOUTH_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getPlymouthWins()).longValue();
		case PONTIAC_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getPontiacWins()).longValue();
		case PORSCHE_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getPorscheWins()).longValue();
		case POWERING_UP:
			return Integer.valueOf(achievementPersonaEntity.getUsedPowerups()).longValue();
		case PRO_TUNER:
			return 0l;
		case PURSUIT_MODDER:
			return 0l;
		case RACE_MODDER:
			return 0l;
		case RENAULT_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getRenaultWins()).longValue();
		case SCION_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getScionWins()).longValue();
		case SHELBY_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getShelbyWins()).longValue();
		case SOLO_RACER:
			int spRaces = achievementPersonaEntity.getSpRaces();
			return Integer.valueOf(spRaces).longValue();
		case SUBARU_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getSubaruWins()).longValue();
		case S_CLASS_CHAMPION:
			int restrictedClassSWins = achievementPersonaEntity.getRestrictedClassSWins();
			return Integer.valueOf(restrictedClassSWins).longValue();
		case THREADING_THE_NEEDLE:
			int teamScapeBlocks = achievementPersonaEntity.getTeamScapeBlocks();
			return Integer.valueOf(teamScapeBlocks).longValue();
		case TOYOTA_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getToyotaWins()).longValue();
		case TREASURE_HUNTER:
			int treasureHunts = achievementPersonaEntity.getTreasureHunts();
			return Integer.valueOf(treasureHunts).longValue();
		case VAUXHALL_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getVauxhallWins()).longValue();
		case VOLKSWAGEN_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getVolkswagenWins()).longValue();
		case WORLD_RACER:
			int mpRaces = achievementPersonaEntity.getMpRaces();
			return Integer.valueOf(mpRaces).longValue();
		case XKR_SPEED_HUNTER:
			return 0l;
		case REACH_DRIVERAGE:
			int days = achievementPersonaEntity.getDriverAgeDays();
			return Integer.valueOf(days).longValue();
//		case WEV2_EXTRALVL:
//			int extraLVL = achievementPersonaEntity.getExtraLVL();
//			return Integer.valueOf(extraLVL).longValue();
		case WEV2_MVP:
			int teamRacesWon = achievementPersonaEntity.getTeamRacesWon();
			return Integer.valueOf(teamRacesWon).longValue();
		case WEV2_EARNSKILL:
			int Skills4Earned = achievementPersonaEntity.getSkills4Earned();
			return Integer.valueOf(Skills4Earned).longValue();
		case WEV2_BEGINNERSGUIDE:
			int levelBG = personaEntity.getLevel();
			return Integer.valueOf(levelBG).longValue();
		case WEV2_SELL_AFTERMARKET:
			int AftermarketSold = achievementPersonaEntity.getAftermarketSold();
			return Integer.valueOf(AftermarketSold).longValue();
		case WEV2_LUCKY_COLLECTOR:
			int containerCars = achievementPersonaEntity.getContainerCars();
			return Integer.valueOf(containerCars).longValue();
		case WEV2_DISCORDBOOST:
			UserEntity userEntity = personaEntity.getUser();
			int dBoostAmount = userEntity.getDBoostAmount();
			return Integer.valueOf(dBoostAmount).longValue();
		case BUGATTI_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getBugattiWins()).longValue();
		case FERRARI_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getFerrariWins()).longValue();
		case FIAT_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getFiatWins()).longValue();
		case HONDA_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getHondaWins()).longValue();
		case MASERATI_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getMaseratiWins()).longValue();
		case NFS_BRAND_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getNFSWins()).longValue();
		case SMART_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getSmartWins()).longValue();
		case TESLA_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getTeslaWins()).longValue();
		case FLANKER_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getFlankerWins()).longValue();
		case BUICK_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getBuickWins()).longValue();
		case POLESTAR_COLLECTOR:
			return Integer.valueOf(achievementBrandsEntity.getPolestarWins()).longValue();
		case WEV3_SIDEQUEST:
			int completedEvents = 0;
			String dailySeriesStr = achievementPersonaEntity.getDailySeries();
			if (dailySeriesStr != null) {
				String[] dailySeriesArray = dailySeriesStr.split(",");
				completedEvents = dailySeriesArray.length;
			}
			return Integer.valueOf(completedEvents).longValue();
		case WEV3_CEVENT_COPHUNT:
			int cEventProgress = personaMissionsBO.getPersonaMissions(personaEntity.getPersonaId()).getCEventGoalProgress();
			return Integer.valueOf(cEventProgress).longValue();
		default:
			break;
		}

		return 0l;
	}

	/**
	 * Achievement progression with integer values
	 * @param achievementPersonaEntity - player persona's achievement values
	 * @param achievementType - achievement type (enum)
	 * @param thresholdValue - current achievement progress value
	 * @author Nilzao, Hypercycle
	 */
	private void processAchievementByThresholdValue(AchievementPersonaEntity achievementPersonaEntity, AchievementType achievementType, Long thresholdValue) {
		PersonaEntity personaEntity = achievementPersonaEntity.getPersona();
		broadcastProgress(personaEntity, achievementType, thresholdValue);
		achievementPersonaDAO.update(achievementPersonaEntity);
		AchievementRankEntity achievementRankEntity = achievementRankDAO.findByAchievementDefinitionIdThresholdValue( //
				achievementType.getId(), thresholdValue);
		
		if (achievementRankEntity != null && achievementRankEntity.getAchievementDefinition().isVisible()) {
			AchievementDefinitionEntity achievementDefinitionEntity = achievementRankEntity.getAchievementDefinition();
			AchievementStateEntity achievementStateEntity = new AchievementStateEntity();
			achievementStateEntity.setAchievedOn(LocalDateTime.now());
			achievementStateEntity.setAchievementRank(achievementRankEntity);
			achievementStateEntity.setAchievementState(AchievementState.REWARD_WAITING);
			achievementStateEntity.setPersona(personaEntity);
			achievementStateDAO.insert(achievementStateEntity);
			int curScore = personaEntity.getScore();
			personaEntity.setScore(curScore + achievementRankEntity.getPoints());
			personaDAO.update(personaEntity);
			broadcastAchievement(personaEntity, achievementRankEntity);
			processAchievementByThresholdRange(achievementPersonaEntity, AchievementType.LEGENDARY_DRIVER,
					Integer.valueOf(personaEntity.getScore()).longValue());
			
			BadgePersonaEntity badgePersonaEntity = badgePersonaDAO.findByPersonaAndDefinition(personaEntity, achievementDefinitionEntity);
			if (badgePersonaEntity != null) {
				personaBO.updateBadgesProgress(personaEntity.getPersonaId(), achievementDefinitionEntity, achievementStateEntity, badgePersonaEntity.getSlot());
			}
		}
	}

	/**
	 * Achievement progression with different range values (Meters, time, multiple objects, etc)
	 * @param achievementPersonaEntity - player persona's achievement values
	 * @param achievementType - achievement type (enum)
	 * @param thresholdValue - current achievement progress value
	 * @author Nilzao, Hypercycle
	 */
	private void processAchievementByThresholdRange(AchievementPersonaEntity achievementPersonaEntity, AchievementType achievementType, Long thresholdValue) {
		PersonaEntity personaEntity = achievementPersonaEntity.getPersona();
		broadcastProgress(personaEntity, achievementType, thresholdValue);
		achievementPersonaDAO.update(achievementPersonaEntity);
		AchievementRankEntity achievementRankEntity = achievementRankDAO.findByAchievementDefinitionIdThresholdPersona(achievementType.getId(), thresholdValue,
				personaEntity);
		if (achievementRankEntity != null && achievementRankEntity.getAchievementDefinition().isVisible()) {
			AchievementDefinitionEntity achievementDefinitionEntity = achievementRankEntity.getAchievementDefinition();
			AchievementStateEntity achievementStateEntity = new AchievementStateEntity();
			achievementStateEntity.setAchievedOn(LocalDateTime.now());
			achievementStateEntity.setAchievementRank(achievementRankEntity);
			achievementStateEntity.setAchievementState(AchievementState.REWARD_WAITING);
			achievementStateEntity.setPersona(personaEntity);
			achievementStateDAO.insert(achievementStateEntity);
			personaEntity.setScore(personaEntity.getScore() + achievementRankEntity.getPoints());
			personaDAO.update(personaEntity);
			broadcastAchievement(personaEntity, achievementRankEntity);
			processAchievementByThresholdRange(achievementPersonaEntity, AchievementType.LEGENDARY_DRIVER,
					Integer.valueOf(personaEntity.getScore()).longValue());
			
			BadgePersonaEntity badgePersonaEntity = badgePersonaDAO.findByPersonaAndDefinition(personaEntity, achievementDefinitionEntity);
			if (badgePersonaEntity != null) {
				personaBO.updateBadgesProgress(personaEntity.getPersonaId(), achievementDefinitionEntity, achievementStateEntity, badgePersonaEntity.getSlot());
			}
		}
	}

	/**
	 * Achievement UI progress announcements
	 * @param personaEntity - player persona
	 * @param achievementType - achievement type (enum)
	 * @param currentValue - current achievement progress value
	 * @author Nilzao
	 */
	public void broadcastProgress(PersonaEntity personaEntity, AchievementType achievementType, Long currentValue) {
		AchievementsAwarded achievementsAwarded = new AchievementsAwarded();
		AchievementProgress achievementProgress = new AchievementProgress();
		achievementProgress.setAchievementDefinitionId(achievementType.getId());
		achievementProgress.setCurrentValue(currentValue);
		List<AchievementProgress> progressedList = new ArrayList<>();
		progressedList.add(achievementProgress);
		achievementsAwarded.setProgressed(progressedList);
		openFireSoapBoxCli.send(achievementsAwarded, personaEntity.getPersonaId());
	}

	/**
	 * Achievement's rank completion UI announcement
	 * @param personaEntity - player persona
	 * @param achievementRankEntity - achievement stage data
	 * @author Nilzao
	 */
	public void broadcastAchievement(PersonaEntity personaEntity, AchievementRankEntity achievementRankEntity) {
		AchievementsAwarded achievementsAwarded = new AchievementsAwarded();
		achievementsAwarded.setPersonaId(personaEntity.getPersonaId());
		achievementsAwarded.setScore(personaEntity.getScore());
		AchievementAwarded achievementAwarded = new AchievementAwarded();

		String achievedOnStr = "0001-01-01T00:00:00";
		try {
			LocalDate date = LocalDate.now();
			GregorianCalendar gcal = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
			XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
			xmlCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			achievedOnStr = xmlCalendar.toXMLFormat();
		} catch (Exception e) {
			System.err.println("xml calendar str error");
		}
		achievementAwarded.setAchievedOn(achievedOnStr);
		achievementAwarded.setAchievementDefinitionId(achievementRankEntity.getAchievementDefinition().getId());
		achievementAwarded.setAchievementRankId(achievementRankEntity.getId());
		achievementAwarded.setClip("AchievementFlasherBase");
		achievementAwarded.setClipLengthInSeconds(5);
		achievementAwarded.setDescription(achievementRankEntity.getAchievementDefinition().getBadgeDefinition().getDescription());
		achievementAwarded.setIcon(achievementRankEntity.getAchievementDefinition().getBadgeDefinition().getIcon());
		achievementAwarded.setName(achievementRankEntity.getAchievementDefinition().getBadgeDefinition().getName());
		achievementAwarded.setPoints(achievementRankEntity.getPoints());
		achievementAwarded.setRare(achievementRankEntity.isRare());
		achievementAwarded.setRarity(achievementRankEntity.getRarity());

		ArrayList<AchievementAwarded> achievements = new ArrayList<>();
		achievements.add(achievementAwarded);

		achievementsAwarded.setAchievements(achievements);
		achievementsAwarded.setScore(personaEntity.getScore());
		openFireSoapBoxCli.send(achievementsAwarded, personaEntity.getPersonaId());
	}

	/**
	 * Test attempt in custom server HUD alerts. Uses achievement's UI completion announcements as a base.
	 * It rewrites the temporal UI information about the player's score, so we need to take the score from the player's persona.
	 * @param personaId - ID of player persona
	 * @param text - main text to display
	 * @author Hypercycle
	 */
	public void broadcastUICustom(Long personaId, String text, String description, int seconds) {
		PersonaEntity personaEntity = personaDAO.findById(personaId);
		AchievementsAwarded achievementsAwarded = new AchievementsAwarded();
		achievementsAwarded.setPersonaId(personaId);
		achievementsAwarded.setScore(personaEntity.getScore()); // Take the value from persona, since this messages is not achievements
		AchievementAwarded achievementAwarded = new AchievementAwarded();

		String achievedOnStr = "0001-01-01T00:00:00";
		try {
			LocalDate date = LocalDate.now();
			GregorianCalendar gcal = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
			XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
			xmlCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			achievedOnStr = xmlCalendar.toXMLFormat();
		} catch (Exception e) {
			System.err.println("xml calendar str error");
		}
		// FIXME Add the DB-based message system, why not
		achievementAwarded.setAchievedOn(achievedOnStr);
		achievementAwarded.setAchievementDefinitionId((long) 104);
		achievementAwarded.setClip("AchievementFlasherBase");
		achievementAwarded.setClipLengthInSeconds(seconds);
		// MISSIONMODE, MAINTENANCEMODE
		achievementAwarded.setDescription(description); // Not used by the game, but used by us as the message type
		achievementAwarded.setIcon("BADGE18");
		achievementAwarded.setName(text); // Limited by 18 symbols
		achievementAwarded.setPoints(0);
		achievementAwarded.setRare(false);
		achievementAwarded.setRarity(0);

		ArrayList<AchievementAwarded> achievements = new ArrayList<>();
		achievements.add(achievementAwarded);

		achievementsAwarded.setAchievements(achievements);
		openFireSoapBoxCli.send(achievementsAwarded, personaId);
	}

	/**
	 * Claim achievement rewards
	 * @param personaId - ID of player persona
	 * @param achievementRankId - ID of achievement stage entry
	 * @author Nilzao, Hypercycle
	 */
	public AchievementRewards redeemReward(Long personaId, Long achievementRankId) {
		PersonaEntity personaEntity = personaDAO.findById(personaId);
		UserEntity userEntity = personaEntity.getUser();

		AchievementRankEntity achievementRankEntity = achievementRankDAO.findById(achievementRankId);
		AchievementStateEntity personaAchievementRank = achievementStateDAO.findByPersonaAchievementRank(personaEntity, achievementRankEntity);
		if (personaAchievementRank != null) {
			personaAchievementRank.setAchievementState(AchievementState.COMPLETED);
			achievementStateDAO.update(personaAchievementRank);
		}
//		// ExtraLVL achievement resets player's level to 10
//		if (achievementRankId == 505 || achievementRankId == 506 || achievementRankId == 507 || achievementRankId == 508 || achievementRankId == 509) {
//			personaEntity.setLevel(10);
//			personaEntity.setRep(30375);
//			personaEntity.setRepAtCurrentLevel(0);
//			personaDAO.update(personaEntity);
//		}

		String rewardTypeStr = achievementRankEntity.getRewardType();
		RewardType rewardType = RewardType.valueOf(rewardTypeStr);
		boolean isCardPack = RewardType.cardpack.equals(rewardType);

		AchievementRewards achievementRewards = new AchievementRewards();
		List<CommerceItemTrans> commerceItems = new ArrayList<>();

		List<RewardDropEntity> rewardDrops = rewardDropDAO.getRewardDrops(achievementRankEntity.getRewardDropId(), 
				achievementRankEntity.getNumberOfRewards(), isCardPack);
		
//      Originally, that code did increase the card packs with less than 5 defined cards amount, by adding a random items, up to 5.
//      But in fact, it only adds a mess to the less-than-5-cards rewards.		
//		if (isCardPack && rewardDrops.size() < 5) {
//			for (int i = 0; i < 5; i++) {
//				RewardDropEntity rewardDropEntity = new RewardDropEntity();
//				rewardDropEntity.setAmount(1);
//				rewardDropEntity.setProduct(dropBO.getRandomProductItem(null, 2, false));
//				rewardDropEntity.setRewardDestiny(RewardDestinyType.INVENTORY);
//				rewardDrops.add(rewardDropEntity);
//			}
//		}
		Collections.shuffle(rewardDrops);
		for (RewardDropEntity rewardDropEntity : rewardDrops) {
			CommerceItemTrans item = new CommerceItemTrans();
			ProductEntity product = rewardDropEntity.getProduct();

			RewardDestinyType rewardDestiny = rewardDropEntity.getRewardDestiny();
			switch (rewardDestiny) { // Depending on the type of reward object
			case CASH:
				personaEntity.setCash(personaEntity.getCash() + rewardDropEntity.getAmount());
				personaDAO.update(personaEntity);
				item.setHash(-429893590);
				String moneyFormat = NumberFormat.getNumberInstance(Locale.US).format(rewardDropEntity.getAmount());
				item.setTitle("$" + moneyFormat);
				break;
			case SPEEDBOOST:
				userEntity.setBoost(userEntity.getBoost() + rewardDropEntity.getAmount());
				userDAO.update(userEntity);
				item.setHash(723701634); // SpeedBoost Icon
				String boostFormat = NumberFormat.getNumberInstance(Locale.US).format(rewardDropEntity.getAmount());
				item.setTitle(boostFormat + " SPEEDBOOST");
				break;
			case GARAGE:
				if (rewardDrops.size() > 1) {
					CarClassesEntity carClassesEntity = carClassesDAO.findByProductId(product.getProductId());
					if (carClassesEntity == null) { // That Product ID is not referenced on Car Classes table
						throw new EngineException(EngineExceptionCode.CarDataInvalid, false);
					}
					item.setTitle(carClassesEntity.getModel());
				}
				else {
					item.setTitle(achievementRankEntity.getRewardText());
				}
				item.setHash(product.getHash());
				basketBO.buyCar(product.getProductId(), personaEntity, true, userEntity, false);
				break;
			case INVENTORY:
				String productTitle = product.getProductTitle();
				String title = productTitle.replace(" x15", "");
				int itemAmount = rewardDropEntity.getAmount();
				if (itemAmount > 1) {
					title = title + " x" + rewardDropEntity.getAmount().toString();
				}
				item.setHash(product.getHash());
				item.setTitle(title);
				// product.setUseCount(rewardDropEntity.getAmount().intValue());
				for (int i = 0; i < itemAmount; i++) {
					inventoryBO.addDroppedItem(product, personaEntity);
				}
				break;
			default:
				break;
			}
			commerceItems.add(item);
		}

		ArrayOfInventoryItemTrans arrayOfInventoryItemTrans = new ArrayOfInventoryItemTrans();

		ArrayOfCommerceItemTrans arrayOfCommerceItemTrans = new ArrayOfCommerceItemTrans();
		arrayOfCommerceItemTrans.getCommerceItemTrans().addAll(commerceItems);
		achievementRewards.setCommerceItems(arrayOfCommerceItemTrans);
		
		WalletTrans cashWallet = new WalletTrans();
		cashWallet.setBalance((double) personaEntity.getCash());
		cashWallet.setCurrency("CASH");
		WalletTrans boostWallet = new WalletTrans();
        boostWallet.setBalance((double) userEntity.getBoost());
        boostWallet.setCurrency("BOOST"); // why doesn't _NS work? Truly a mystery... - LeoCodes21

		ArrayOfWalletTrans arrayOfWalletTrans = new ArrayOfWalletTrans();
		arrayOfWalletTrans.getWalletTrans().add(cashWallet);
        arrayOfWalletTrans.getWalletTrans().add(boostWallet);

		achievementRewards.setWallets(arrayOfWalletTrans);
		achievementRewards.setStatus(CommerceResultStatus.SUCCESS);
		achievementRewards.setInventoryItems(arrayOfInventoryItemTrans);
		achievementRewards.setInvalidBasket(new InvalidBasketTrans());
		achievementRewards.setPurchasedCars(new ArrayOfOwnedCarTrans());

		achievementRewards.setVisualStyle(achievementRankEntity.getRewardVisualStyle());

		achievementRewards.setAchievementRankId(achievementRankId);
		// to debug inside game:
		// achievementRewards.setAchievementRankId(0L);

		return achievementRewards;
	}
	
	// ---------
	// Each achievement have it's own "call" method
		
	public void applyCommerceAchievement(PersonaEntity personaEntity) {
		// 
	}

	public void applyEventAchievement(PersonaEntity personaEntity) {
		//
	}
	
	/**
	 * Apply "Payday" achievement
	 * @param personaEntity - player persona
	 * @param incomeCash - last recieved reward value
	 * @author Nilzao
	 */
	public void applyPayDayAchievement(PersonaEntity personaEntity, double incomeCash) {
		if (incomeCash > 0) {
			AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
			int totalIncomeCash = achievementPersonaEntity.getTotalIncomeCash() + (int) incomeCash;
			achievementPersonaEntity.setTotalIncomeCash(totalIncomeCash);
			processAchievementByThresholdRange(achievementPersonaEntity, AchievementType.PAYDAY, Integer.valueOf(totalIncomeCash).longValue());
		}
	}
	
	/**
	 * Apply "Long Haul" achievement
	 * @param personaEntity - player persona
	 * @param eventLength - last event's length in meters
	 * @author Hypercycle
	 */
	public void applyEventKmsAchievement(PersonaEntity personaEntity, Long eventLength) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		Integer metersDriven = (int) (achievementPersonaEntity.getEventMeters() + (eventLength * 1000));
		achievementPersonaEntity.setEventMeters(metersDriven);
		processAchievementByThresholdRange(achievementPersonaEntity, AchievementType.LONG_HAUL, metersDriven.longValue());
	}
		
	/**
	 * Apply achievements depending on the lucky cards content
	 * @param personaEntity - player persona
	 * @param achievementType - achievement type (enum)
	 * @author Hypercycle
	 */
	public void applyDropAchievements(PersonaEntity personaEntity, AchievementType achievementType) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		switch (achievementType) {
		case WEV2_EARNSKILL:
			int skills4EarnedValue = achievementPersonaEntity.getSkills4Earned();
			skills4EarnedValue = skills4EarnedValue + 1;
			achievementPersonaEntity.setSkills4Earned(skills4EarnedValue);
			processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.WEV2_EARNSKILL, Integer.valueOf(skills4EarnedValue).longValue());
		break;
		} // More to come
	}

	/**
	 * Apply "Powering Up" achievement
	 * @param personaEntity - player persona
	 * @author Nilzao
	 */
	public void applyPowerupAchievement(PersonaEntity personaEntity) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		Integer usedPowerups = achievementPersonaEntity.getUsedPowerups() + 1;
		achievementPersonaEntity.setUsedPowerups(usedPowerups);
		processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.POWERING_UP, usedPowerups.longValue());
	}

	/**
	 * Apply "Treasure Hunter" achievement
	 * @param treasureHuntEntity - persona information entry of Treasure Hunt
	 * @author Nilzao
	 */
	public void applyTreasureHuntAchievement(TreasureHuntEntity treasureHuntEntity) {
		PersonaEntity personaEntity = personaDAO.findById(treasureHuntEntity.getPersonaId());
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		int treasureHunts = achievementPersonaEntity.getTreasureHunts() + 1;
		achievementPersonaEntity.setTreasureHunts(treasureHunts);
		processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.TREASURE_HUNTER, Integer.valueOf(treasureHunts).longValue());
	}

	/**
	 * Apply "Daily Hunter" achievement
	 * @param treasureHuntEntity - persona information entry of Treasure Hunt
	 * @author Nilzao
	 */
	public void applyDailyTreasureHuntAchievement(TreasureHuntEntity treasureHuntEntity) {
		PersonaEntity personaEntity = personaDAO.findById(treasureHuntEntity.getPersonaId());
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		Integer streak = treasureHuntEntity.getStreak();
		if (streak > achievementPersonaEntity.getDailyTreasureHunts()) {
		   achievementPersonaEntity.setDailyTreasureHunts(streak); 
		   processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.DAILY_HUNTER, streak.longValue());
		}
	}

	/**
	 * Apply various end-race achievements
	 * @param eventDataEntity - last event statistics
	 * @param routeArbitrationPacket - event arbitration information
	 * @param personaEntity - player persona
	 * @author Nilzao
	 */
	public void applyRaceAchievements(EventDataEntity eventDataEntity, RouteArbitrationPacket routeArbitrationPacket, PersonaEntity personaEntity) {
		// FIXME this code is a mess - Nilzao
		TokenSessionEntity tokenSessionEntity = tokenSessionDAO.findByUserId(personaEntity.getUser().getId());
		Long activeLobbyId = tokenSessionEntity.getActiveLobbyId();
		Boolean isPrivate = false;
		if (!activeLobbyId.equals(0l)) {
			isPrivate = eventSessionDAO.findById(eventDataEntity.getEventSessionId()).isPrivate();
		}
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		if (isPrivate) {
			int privateRaces = achievementPersonaEntity.getPrivateRaces();
			privateRaces = privateRaces + 1;
			achievementPersonaEntity.setPrivateRaces(privateRaces);
			processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.CREW_RACER, Integer.valueOf(privateRaces).longValue());
		} else if (!activeLobbyId.equals(0l)) {
			int mpRaces = achievementPersonaEntity.getMpRaces();
			mpRaces = mpRaces + 1;
			achievementPersonaEntity.setMpRaces(mpRaces);
			processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.WORLD_RACER, Integer.valueOf(mpRaces).longValue());
			if (routeArbitrationPacket.getRank() == 1) {
				AchievementType achievementType = null;
				Long thresholdValue = null;
				switch (eventDataEntity.getEvent().getCarClassHash()) {
				case -405837480: {
					achievementType = AchievementType.A_CLASS_CHAMPION;
					int restrictedClassAWins = achievementPersonaEntity.getRestrictedClassAWins();
					restrictedClassAWins++;
					thresholdValue = Integer.valueOf(restrictedClassAWins).longValue();
					achievementPersonaEntity.setRestrictedClassAWins(restrictedClassAWins);
					break;
				}
				case -406473455: {
					achievementType = AchievementType.B_CLASS_CHAMPION;
					int restrictedClassBWins = achievementPersonaEntity.getRestrictedClassBWins();
					restrictedClassBWins++;
					achievementPersonaEntity.setRestrictedClassBWins(restrictedClassBWins);
					thresholdValue = Integer.valueOf(restrictedClassBWins).longValue();
					break;
				}
				case 1866825865: {
					achievementType = AchievementType.C_CLASS_CHAMPION;
					int restrictedClassCWins = achievementPersonaEntity.getRestrictedClassCWins();
					restrictedClassCWins++;
					achievementPersonaEntity.setRestrictedClassCWins(restrictedClassCWins);
					thresholdValue = Integer.valueOf(restrictedClassCWins).longValue();
					break;
				}
				case 415909161: {
					achievementType = AchievementType.D_CLASS_CHAMPION;
					int restrictedClassDWins = achievementPersonaEntity.getRestrictedClassDWins();
					restrictedClassDWins++;
					achievementPersonaEntity.setRestrictedClassDWins(restrictedClassDWins);
					thresholdValue = Integer.valueOf(restrictedClassDWins).longValue();
					break;
				}
				case 872416321: {
					achievementType = AchievementType.E_CLASS_CHAMPION;
					int restrictedClassEWins = achievementPersonaEntity.getRestrictedClassEWins();
					restrictedClassEWins++;
					achievementPersonaEntity.setRestrictedClassEWins(restrictedClassEWins);
					thresholdValue = Integer.valueOf(restrictedClassEWins).longValue();
					break;
				}
				case -2142411446: {
					achievementType = AchievementType.S_CLASS_CHAMPION;
					int restrictedClassSWins = achievementPersonaEntity.getRestrictedClassSWins();
					restrictedClassSWins++;
					achievementPersonaEntity.setRestrictedClassSWins(restrictedClassSWins);
					thresholdValue = Integer.valueOf(restrictedClassSWins).longValue();
					break;
				}
				default:
					break;
				}
				if (achievementType != null) {
					processAchievementByThresholdValue(achievementPersonaEntity, achievementType, thresholdValue);
				}
			}
		} else {
			int spRaces = achievementPersonaEntity.getSpRaces();
			spRaces = spRaces + 1;
			achievementPersonaEntity.setSpRaces(spRaces);
			processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.SOLO_RACER, Integer.valueOf(spRaces).longValue());
		}
	}

	/**
	 * Apply "Outlaw" achievement
	 * @param personaEntity - player persona
	 * @author Nilzao
	 */
	public void applyOutlawAchievement(PersonaEntity personaEntity) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		int pursuitWins = achievementPersonaEntity.getPursuitWins();
		pursuitWins = pursuitWins + 1;
		achievementPersonaEntity.setPursuitWins(pursuitWins);
		processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.OUTLAW, Integer.valueOf(pursuitWins).longValue());
	}
	
	/**
	 * Apply "Veteran Driver" achievement.
	 * Achievement stage IDs is hardcoded, the rank-checking method might be re-done
	 * @param personaEntity - player persona
	 * @param driverAgeDays - days from player persona creation date
	 * @author Hypercycle
	 */
	public void applyDriverAgeAchievement(PersonaEntity personaEntity, int driverAgeDays) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		achievementPersonaEntity.setDriverAgeDays(driverAgeDays);
		achievementPersonaDAO.update(achievementPersonaEntity);
		boolean isNewRank = false;
		boolean reward = true;
		if (achievementPersonaEntity.getSBAgeTaken() >= 3) {reward = false;} // Prevent creating a new profiles for SB rewards
		
		if (driverAgeDays >= 365) {
			isNewRank = forceAchievementApply(529, personaEntity, reward);
			forceAchievementApply(528, personaEntity, reward);
			forceAchievementApply(527, personaEntity, reward);
			int sbAgeTaken = achievementPersonaEntity.getSBAgeTaken();
			if (sbAgeTaken < 3) {
				achievementPersonaEntity.setSBAgeTaken(sbAgeTaken + 1);
				achievementPersonaDAO.update(achievementPersonaEntity);
			}
		}
        if (!isNewRank && driverAgeDays >= 180) {
        	isNewRank = forceAchievementApply(528, personaEntity, reward);
			forceAchievementApply(527, personaEntity, reward);
		}
        if (!isNewRank && driverAgeDays >= 90) {
        	isNewRank = forceAchievementApply(527, personaEntity, reward);
		}
	}

	/**
	 * Apply "Drag Racer" achievement
	 * @param eventDataEntity - last event statistics
	 * @param dragArbitrationPacket - drag event arbitration information
	 * @param activePersonaId - player persona ID
	 * @author Nilzao
	 */
	public void applyDragAchievement(EventDataEntity eventDataEntity, DragArbitrationPacket dragArbitrationPacket, Long activePersonaId) {
		PersonaEntity personaEntity = personaDAO.findById(activePersonaId);
		TokenSessionEntity tokenSessionEntity = tokenSessionDAO.findByUserId(personaEntity.getUser().getId());
		Long activeLobbyId = tokenSessionEntity.getActiveLobbyId();
		if (!activeLobbyId.equals(0l)) {
			EventSessionEntity eventSessionEntity = eventSessionDAO.findById(eventDataEntity.getEventSessionId());
			if (!eventSessionEntity.isPrivate()) {
				if (dragArbitrationPacket.getRank() == 1) {
					AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
					int mpDragWins = achievementPersonaEntity.getMpDragWins();
					mpDragWins = mpDragWins + 1;
					achievementPersonaEntity.setMpDragWins(mpDragWins);
					processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.DRAG_RACER, Integer.valueOf(mpDragWins).longValue());
				}
			}
		}
	}

	/**
	 * Apply "Level Up" achievement
	 * @param personaEntity - player persona
	 * @author Nilzao
	 */
	public void applyLevelUpAchievement(PersonaEntity personaEntity) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.LEVEL_UP, Integer.valueOf(personaEntity.getLevel()).longValue());
	}
	
	/**
	 * Apply "Beginner's Guide" achievement
	 * @param personaEntity - player persona
	 * @author Hypercycle
	 */
	public void applyBeginnersGuideAchievement(PersonaEntity personaEntity) { // Needs level 6 to activate
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.WEV2_BEGINNERSGUIDE, Integer.valueOf(6).longValue());
	}
	
//	public void applyExtraLVLAchievement(PersonaEntity personaEntity) {
//		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
//		int extraLVLValue = achievementPersonaEntity.getExtraLVL();
//		extraLVLValue = extraLVLValue + 1;
//		achievementPersonaEntity.setExtraLVL(extraLVLValue);
//		processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.WEV2_EXTRALVL, Integer.valueOf(extraLVLValue).longValue());
//	}
	
	/**
	 * Apply "Most Valuable Player" achievement
	 * @param personaEntity - player persona
	 * @author Hypercycle
	 */
	public void applyTeamRacesWonAchievement(PersonaEntity personaEntity) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		int teamRacesWonValue = achievementPersonaEntity.getTeamRacesWon();
		teamRacesWonValue = teamRacesWonValue + 1;
		achievementPersonaEntity.setTeamRacesWon(teamRacesWonValue);
		processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.WEV2_MVP, Integer.valueOf(teamRacesWonValue).longValue());
	}
	
	/**
	 * Apply car brands achievements
	 * @param personaEntity - player persona, brandInfo - car brand information array
	 * @author Hypercycle
	 */
	public void applyBrandsAchievements(PersonaEntity personaEntity, Integer[] brandInfo) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.valueOf(brandInfo[0]), Integer.valueOf(brandInfo[1]).longValue());
	}

	/**
	 * Apply "Air Time" achievement
	 * @param arbitrationPacket - event arbitration information
	 * @param personaEntity - player persona
	 * @author Nilzao
	 */
	public void applyAirTimeAchievement(ArbitrationPacket arbitrationPacket, PersonaEntity personaEntity) {
		long specificJumpsDurationInMilliseconds = 0l;
		if (arbitrationPacket instanceof PursuitArbitrationPacket) {
			PursuitArbitrationPacket specificArbitrationPacket = (PursuitArbitrationPacket) arbitrationPacket;
			specificJumpsDurationInMilliseconds = specificArbitrationPacket.getSumOfJumpsDurationInMilliseconds();
		} else if (arbitrationPacket instanceof TeamEscapeArbitrationPacket) {
			TeamEscapeArbitrationPacket specificArbitrationPacket = (TeamEscapeArbitrationPacket) arbitrationPacket;
			specificJumpsDurationInMilliseconds = specificArbitrationPacket.getSumOfJumpsDurationInMilliseconds();
		} else if (arbitrationPacket instanceof RouteArbitrationPacket) {
			RouteArbitrationPacket specificArbitrationPacket = (RouteArbitrationPacket) arbitrationPacket;
			specificJumpsDurationInMilliseconds = specificArbitrationPacket.getSumOfJumpsDurationInMilliseconds();
		} else if (arbitrationPacket instanceof DragArbitrationPacket) {
			DragArbitrationPacket specificArbitrationPacket = (DragArbitrationPacket) arbitrationPacket;
			specificJumpsDurationInMilliseconds = specificArbitrationPacket.getSumOfJumpsDurationInMilliseconds();
		}
		if (specificJumpsDurationInMilliseconds > 0) {
			AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
			long sumOfJumpsDurationInMilliseconds = achievementPersonaEntity.getEventAerialTime();
			sumOfJumpsDurationInMilliseconds = sumOfJumpsDurationInMilliseconds + specificJumpsDurationInMilliseconds;
			achievementPersonaEntity.setEventAerialTime(sumOfJumpsDurationInMilliseconds);
			processAchievementByThresholdRange(achievementPersonaEntity, AchievementType.AIRTIME, sumOfJumpsDurationInMilliseconds);
		}
	}

	/**
	 * Apply "Enemy of the State" achievement
	 * @param arbitrationPacket - event arbitration information
	 * @param personaEntity - player persona
	 * @author Nilzao
	 */
	public void applyPursuitCostToState(ArbitrationPacket arbitrationPacket, PersonaEntity personaEntity) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		long pursuitCostToState = achievementPersonaEntity.getPursuitCostToState();
		if (arbitrationPacket instanceof PursuitArbitrationPacket) {
			PursuitArbitrationPacket specificArbitrationPacket = (PursuitArbitrationPacket) arbitrationPacket;
			pursuitCostToState = pursuitCostToState + Integer.valueOf(specificArbitrationPacket.getCostToState()).longValue();
		} else if (arbitrationPacket instanceof TeamEscapeArbitrationPacket) {
			TeamEscapeArbitrationPacket specificArbitrationPacket = (TeamEscapeArbitrationPacket) arbitrationPacket;
			pursuitCostToState = pursuitCostToState + Integer.valueOf(specificArbitrationPacket.getCostToState()).longValue();
		}
		if (pursuitCostToState > 0) {
			achievementPersonaEntity.setPursuitCostToState(pursuitCostToState);
			processAchievementByThresholdRange(achievementPersonaEntity, AchievementType.ENEMY_OF_THE_STATE, pursuitCostToState);
		}
	}

	/**
	 * Apply various Team Escape related achievements
	 * @param teamEscapeArbitrationPacket - TE event arbitration information
	 * @param personaEntity - player persona
	 * @author Nilzao
	 */
	public void applyTeamEscape(TeamEscapeArbitrationPacket teamEscapeArbitrationPacket, PersonaEntity personaEntity) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		if (teamEscapeArbitrationPacket.getCopsDisabled() > 0) {
			int copsDisabled = teamEscapeArbitrationPacket.getCopsDisabled() + achievementPersonaEntity.getTeamScapePoliceDown();
			achievementPersonaEntity.setTeamScapePoliceDown(copsDisabled);
			long copsDisabledLong = Integer.valueOf(copsDisabled).longValue();
			processAchievementByThresholdRange(achievementPersonaEntity, AchievementType.HEAVY_HITTER, copsDisabledLong);
		}
		if (teamEscapeArbitrationPacket.getRoadBlocksDodged() > 0) {
			int roadBlocksDodged = teamEscapeArbitrationPacket.getRoadBlocksDodged() + achievementPersonaEntity.getTeamScapeBlocks();
			achievementPersonaEntity.setTeamScapeBlocks(roadBlocksDodged);
			long roadBlocksDodgedLong = Integer.valueOf(roadBlocksDodged).longValue();
			processAchievementByThresholdRange(achievementPersonaEntity, AchievementType.THREADING_THE_NEEDLE, roadBlocksDodgedLong);
		}
	}

	/**
	 * Apply "Getaway Driver" achievement
	 * @param personaEntity - player persona
	 * @author Nilzao
	 */
	public void applyTeamEscapeGetAway(PersonaEntity personaEntity) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		int teamScapeWins = achievementPersonaEntity.getTeamScapeWins();
		teamScapeWins = teamScapeWins + 1;
		achievementPersonaEntity.setTeamScapeWins(teamScapeWins);
		long teamScapeWinsLong = Integer.valueOf(teamScapeWins).longValue();
		processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.GETAWAY_DRIVER, teamScapeWinsLong);
	}
	
	/**
	 * Apply "Aftermarket Seller" achievement
	 * @param personaEntity - player persona
	 * @author Hypercycle
	 */
	public void applyAftermarketSold(PersonaEntity personaEntity) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		int aftermarketSoldValue = achievementPersonaEntity.getAftermarketSold();
		aftermarketSoldValue = aftermarketSoldValue + 1;
		achievementPersonaEntity.setAftermarketSold(aftermarketSoldValue);
		processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.WEV2_SELL_AFTERMARKET, Integer.valueOf(aftermarketSoldValue).longValue());
	}
	
	/**
	 * Apply "Lucky Collector" achievement
	 * @param personaEntity - player persona
	 * @author Hypercycle
	 */
	public void applyLuckyCollector(PersonaEntity personaEntity) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		int containerCarsValue = achievementPersonaEntity.getContainerCars();
		containerCarsValue = containerCarsValue + 1;
		achievementPersonaEntity.setContainerCars(containerCarsValue);
		processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.WEV2_LUCKY_COLLECTOR, Integer.valueOf(containerCarsValue).longValue());
	}
	
	/**
	 * Apply "Our Supporter" achievement
	 * @param personaEntity - player persona
	 * @author Hypercycle
	 */
	public void applyDiscordBoost(PersonaEntity personaEntity) {
		UserEntity userEntity = personaEntity.getUser();
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		int dBoostValue = userEntity.getDBoostAmount();
		dBoostValue = dBoostValue + 1;
		userEntity.setDBoostAmount(dBoostValue);
		userDAO.update(userEntity);
		processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.WEV2_DISCORDBOOST, Integer.valueOf(dBoostValue).longValue());
	}
	
	/**
	 * Apply "Side-Quest" achievement
	 * @param personaEntity - player persona
	 * @author Hypercycle
	 */
	public void applyDailySeries(PersonaEntity personaEntity, int eventId) {
		boolean skipProgress = false;
		Long eventIdLong = (long) eventId;
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		String dailySeriesStr = achievementPersonaEntity.getDailySeries();
		List<Long> dailySeriesArray = new ArrayList<Long>();
		if (dailySeriesStr != null) {
			String[] dailySeriesStrArray = dailySeriesStr.split(",");
			Long[] dailySeriesLong = stringListConverter.StrToLongList(dailySeriesStrArray);
			dailySeriesArray = Stream.of(dailySeriesLong).collect(Collectors.toCollection(ArrayList::new));
			if (!dailySeriesArray.contains(eventIdLong)) {
				dailySeriesArray.add(eventIdLong); // Add the event ID to the completed events list
			}
			else {
				skipProgress = true; // Don't trigger the achievement progress, since the event is already completed
			}
		}
		else {dailySeriesArray.add(eventIdLong);} // Add the event ID as the first completed event
		if (!skipProgress) {
			int eventsAmount = dailySeriesArray.size();
			achievementPersonaEntity.setDailySeries(stringListConverter.listToStr(dailySeriesArray));
			processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.WEV3_SIDEQUEST, Integer.valueOf(eventsAmount).longValue());
		}
	}
	
	/**
	 * Apply "Cop Hunt" Community Event achievement
	 * @param personaEntity - player persona
	 * @author Hypercycle
	 */
	public void applyCEventAchievement(PersonaEntity personaEntity, PersonaMissionsEntity personaMissionsEntity, int cEventType) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		processAchievementByThresholdRange(achievementPersonaEntity, AchievementType.valueOf(cEventType), Integer.valueOf(personaMissionsEntity.getCEventGoalProgress()).longValue());
	}
	
	/**
	 * Apply specified achievement rank to player persona
	 * @param rankId - ID of the achievement stage
	 * @param personaEntity - player persona
	 * @param reward - is the reward items will be given
	 * @author Hypercycle
	 */
	// True - rank has been added, False - rank is already achieved
	// Reward: True - give the reward items, False - no reward items (react alike they given already)
	public boolean forceAchievementApply (int rankId, PersonaEntity personaEntity, boolean reward) {
		AchievementRankEntity achievementRankEntity = achievementRankDAO.findById((long) rankId);
		if (achievementStateDAO.findByPersonaAchievementRank(personaEntity, achievementRankEntity) == null) {
			AchievementStateEntity achievementStateEntity = new AchievementStateEntity();
			achievementStateEntity.setAchievedOn(LocalDateTime.now());
			achievementStateEntity.setAchievementRank(achievementRankEntity);
			if (reward) {achievementStateEntity.setAchievementState(AchievementState.REWARD_WAITING);}
			else {achievementStateEntity.setAchievementState(AchievementState.COMPLETED);}
			achievementStateEntity.setPersona(personaEntity);
			achievementStateDAO.insert(achievementStateEntity);
			personaEntity.setScore(personaEntity.getScore() + achievementRankEntity.getPoints());
			personaDAO.update(personaEntity);
			return true;
		}
		return false;
	}
	
	/**
	 * Apply "Collector" achievement
	 * @param personaEntity - player persona
	 * @author Hypercycle
	 */
	public void applyCollector(PersonaEntity personaEntity) {
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
		int collectorCarsValue = achievementPersonaEntity.getCollectorCars();
		int carsAmount = carSlotDAO.countPersonaCars(personaEntity.getPersonaId()).intValue();
		// Check out the last rank goal of Collector achievement
		if (carsAmount <= achievementRankDAO.findLastStage(achievementDAO.findById((long) 16)).getThresholdValue() && carsAmount > collectorCarsValue) { 
			int diff = carsAmount - collectorCarsValue;
			if (diff > 1) {
				System.out.println("Collector Bug: " + diff + ", personaId: " + personaEntity.getPersonaId());
			}
			collectorCarsValue = collectorCarsValue + 1;
			achievementPersonaEntity.setCollectorCars(collectorCarsValue);
			processAchievementByThresholdValue(achievementPersonaEntity, AchievementType.COLLECTOR, Integer.valueOf(carsAmount).longValue());
		}
	}
	
	/**
	 * Force-apply the Collector achievement stages (if player don't got it before)
	 * @param carsAmount - current car amount in persona's garage
	 * @param personaEntity - player persona
	 * @author Hypercycle
	 */
	// Checking each achievement stage there
	// Unoptimized - makes separate DB request for every achievement stage in persona, both times
	public void forceApplyCollector (int carsAmount, PersonaEntity personaEntity) {
		Integer[] ranksArray = new Integer[] {76,77,78,79,80};
		List<AchievementRankEntity> getRanksList = achievementRankDAO.findMultipleRanksById(ranksArray);
		AchievementPersonaEntity achievementPersonaEntity = achievementPersonaDAO.findByPersona(personaEntity);
	    // Does the persona have achievement ranks already? Get the car value then
		for (AchievementRankEntity collectorRank : getRanksList) {
			Long carsGoal = collectorRank.getThresholdValue();
			if (achievementStateDAO.findByPersonaAchievementRank(personaEntity, collectorRank) != null) {
				achievementPersonaEntity.setCollectorCars(carsGoal.intValue());
				// System.out.println("stage2 value: " + carsGoal.intValue());
			}
		}
		int i = 0; // First time and no ranks? Let's give them
		for (AchievementRankEntity collectorRank : getRanksList) {
			Long carsGoal = collectorRank.getThresholdValue();
			if (carsAmount > (carsGoal - 1) && achievementStateDAO.findByPersonaAchievementRank(personaEntity, collectorRank) == null) {
				forceAchievementApply(ranksArray[i], personaEntity, true);
				processAchievementByThresholdRange(achievementPersonaEntity, AchievementType.LEGENDARY_DRIVER, Integer.valueOf(personaEntity.getScore()).longValue());
				achievementPersonaEntity.setCollectorCars(carsGoal.intValue());
			}
			i++;
		}
		// After all of this, let's save the real player cars amount
		if (carsAmount > achievementPersonaEntity.getCollectorCars()) {
			Long carsLimit = achievementRankDAO.findLastStage(achievementDAO.findById((long) 16)).getThresholdValue();
			if (carsAmount > carsLimit) {achievementPersonaEntity.setCollectorCars(carsLimit.intValue());}
			else {achievementPersonaEntity.setCollectorCars(carsAmount);}
		}
		// System.out.println("final value: " + achievementPersonaEntity.getCollectorCars());
		achievementPersonaDAO.update(achievementPersonaEntity);
	}
	
	/**
	 * Re-calculate achievement score counter for persona, useful for debug and in case of progress issues
	 * @param personaEntity - player persona
	 * @author Hypercycle
	 */
	public void forceScoreCalc (PersonaEntity personaEntity) {
		int playerScoreNew = 0;
		List<AchievementStateEntity> stateList = achievementStateDAO.findAllOfPersona(personaEntity);
		for (AchievementStateEntity stateEntity : stateList) {
			int rankPoints = stateEntity.getAchievementRank().getPoints();
			playerScoreNew = playerScoreNew + rankPoints;
		}
		int oldScore = personaEntity.getScore();
		personaEntity.setScore(playerScoreNew);
		personaDAO.update(personaEntity);
		System.out.println("### Score of player " + personaEntity.getName() + " is re-calculated (from " + oldScore + " to " + playerScoreNew + ")");
	}
}