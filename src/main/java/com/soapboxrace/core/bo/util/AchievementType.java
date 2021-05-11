package com.soapboxrace.core.bo.util;

import java.util.Arrays;

public enum AchievementType {

	/**
	 * win [x] a-class restricted multiplayer sprint & circuits
	 */
	A_CLASS_CHAMPION(1), //
	/**
	 * win [x] b-class restricted multiplayer sprints & circuits
	 */
	B_CLASS_CHAMPION(2), //
	/**
	 * win [x] c-class restricted multiplayer sprints & circuits
	 */
	C_CLASS_CHAMPION(3), //
	/**
	 * win [x] d-class restricted multiplayer sprints & circuits
	 */
	D_CLASS_CHAMPION(4), //
	/**
	 * win [x] e-class restricted multiplayer sprints & circuits
	 */
	E_CLASS_CHAMPION(5), //
	/**
	 * win [x] s-class restricted multiplayer sprints & circuits
	 */
	S_CLASS_CHAMPION(6), //
	/**
	 * win [x] multiplayer drag races
	 */
	DRAG_RACER(7), //
	/**
	 * accumulate [x] cost to state incurred in pursuits
	 */
	ENEMY_OF_THE_STATE(8), //
	/**
	 * successfully evade the cops [x] times in pursuit outrun
	 */
	OUTLAW(9), //
	/**
	 * disable [x] cops in team escape
	 */
	HEAVY_HITTER(10), //
	/**
	 * dodge [x] roadblocks in team escape
	 */
	THREADING_THE_NEEDLE(11), //
	/**
	 * complete [x] team escapes with at least one teammate evading
	 */
	GETAWAY_DRIVER(12), //
	/**
	 * accumulate [x] of total airtime in events
	 */
	AIRTIME(13), //
	/**
	 * drive [x] (km/mi) in events
	 */
	LONG_HAUL(14), //
	/**
	 * install [x] aftermarket parts
	 */
	AFTERMARKET_SPECIALIST(15), //
	/**
	 * own [x] cars in your garage
	 */
	COLLECTOR(16), //
	/**
	 * earn [x] cash total
	 */
	PAYDAY(17), //
	/**
	 * install [x] performance parts (3-star or better)
	 */
	PRO_TUNER(18), //
	/**
	 * install [x] explore skill mods (3-star or better)
	 */
	EXPLORE_MODDER(19), //
	/**
	 * install [x] pursuit skill mods (3-star or better)
	 */
	PURSUIT_MODDER(20), //
	/**
	 * install [x] race skill mods (3-star or better)
	 */
	RACE_MODDER(21), //
	/**
	 * apply [x] paints
	 */
	FRESH_COAT(22), //
	/**
	 * reach driver level [x]
	 */
	LEVEL_UP(23), //
	/**
	 * reach [x] total driver score by completing achievements
	 */
	LEGENDARY_DRIVER(24), //
	/**
	 * applly [x] vinyls
	 */
	CAR_ARTIST(25), //
	/**
	 * participated in the need for speed world open beta
	 */
	OPEN_BETA(26), //
	/**
	 * need for speed world developer
	 */
	DEVELOPER(27), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	ALFA_ROMEO_COLLECTOR(28), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	ASTON_MARTIN_COLLECTOR(29), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	AUDI_COLLECTOR(30), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	BENTLEY_COLLECTOR(31), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	BMW_COLLECTOR(32), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	CADILLAC_COLLECTOR(33), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	CATERHAM_COLLECTOR(34), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	CHEVROLET_COLLECTOR(35), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	CHRYSLER_COLLECTOR(36), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	DODGE_COLLECTOR(37), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	FORD_COLLECTOR(38), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	FORD_SHELBY_COLLECTOR(39), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	HUMMER_COLLECTOR(40), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	INFINITI_COLLECTOR(41), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	JAGUAR_COLLECTOR(42), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	JEEP_COLLECTOR(43), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	KOENIGSEGG_COLLECTOR(44), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	LAMBORGHINI_COLLECTOR(45), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	LANCIA_COLLECTOR(46), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	LEXUS_COLLECTOR(47), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	LOTUS_COLLECTOR(48), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	MARUSSIA_COLLECTOR(49), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	MAZDA_COLLECTOR(50), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	MCLAREN_COLLECTOR(51), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	MERCEDES_BENZ_COLLECTOR(52), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	MITSUBISHI_COLLECTOR(53), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	NISSAN_COLLECTOR(54), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	PAGANI_COLLECTOR(55), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	PLYMOUTH_COLLECTOR(56), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	PONTIAC_COLLECTOR(57), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	PORSCHE_COLLECTOR(58), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	RENAULT_COLLECTOR(59), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	SCION_COLLECTOR(60), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	SHELBY_COLLECTOR(61), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	SUBARU_COLLECTOR(62), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	TOYOTA_COLLECTOR(63), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	VAUXHALL_COLLECTOR(64), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	VOLKSWAGEN_COLLECTOR(65), //
	/**
	 * activate [x] powerup(s)
	 */
	POWERING_UP(66), //
	/**
	 * play [x] multiplayer sprints & circuits
	 */
	WORLD_RACER(67), //
	/**
	 * complete [x] treasure hunts
	 */
	TREASURE_HUNTER(68), //
	/**
	 * complete [x] consecutive treasure hunts
	 */
	DAILY_HUNTER(69), //
	/**
	 * play [x] sprints & circuits in private matches
	 */
	CREW_RACER(70), //
	/**
	 * play [x] sprints & circuits in single player
	 */
	SOLO_RACER(71), //
	/**
	 * complete a treasure hunt in under [x] using the jaguar xkr
	 */
	XKR_SPEED_HUNTER(72), //
	/**
	 * reach X days from driver creation date
	 */
	REACH_DRIVERAGE(73), //
	/**
	 * ???
	 */
	WEV2_EXTRALVL(100), //
	/**
	 * win races for your team
	 */
	WEV2_MVP(101), //
	/**
	 * got a 4-star skills on class-restricted races or treasure hunt
	 */
	WEV2_EARNSKILL(102), //
	/**
	 * unlock access to multiplayer racing
	 */
	WEV2_BEGINNERSGUIDE(103), //
	/**
	 * sell X amount of aftermarket parts
	 */
	WEV2_SELL_AFTERMARKET(104), //
	/**
	 * get X cars of A or S class (or rare) cars from car lootbox
	 */
	WEV2_LUCKY_COLLECTOR(105), //
	/**
	 * donate for Discord Boosts
	 */
	WEV2_DISCORDBOOST(106), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	BUGATTI_COLLECTOR(107), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	FERRARI_COLLECTOR(108), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	FIAT_COLLECTOR(109), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	HONDA_COLLECTOR(110), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	MASERATI_COLLECTOR(111), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	NFS_BRAND_COLLECTOR(112), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	SMART_COLLECTOR(113), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	TESLA_COLLECTOR(114), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	FLANKER_COLLECTOR(115), //
	/**
	 * complete the events of daily Challenge Series
	 */
	WEV3_SIDEQUEST(116), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	BUICK_COLLECTOR(117), //
	/**
	 * Community Event: complete X events with X cops being stopped
	 */
	WEV3_CEVENT_COPHUNT(118), //
	/**
	 * own [x] [car-brand](s) in your garage
	 */
	POLESTAR_COLLECTOR(119), //
	/**
	 * drive in all events
	 */
	WEV3_TRAVELLER(120), //
	/**
	 * install 4-star skills on all slots for X cars
	 */
	WEV3_SKILL_MASTER(121); //
	

	private int id;

	private AchievementType(int id) {
		this.id = id;
	}

	public Long getId() {
		return Integer.valueOf(id).longValue();
	}

	public static AchievementType valueOf(int value) {
		return Arrays.stream(values()).filter(legNo -> legNo.id == value).findFirst().get();
	}

}
