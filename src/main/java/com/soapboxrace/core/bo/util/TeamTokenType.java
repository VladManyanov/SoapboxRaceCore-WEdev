package com.soapboxrace.core.bo.util;

import java.util.Arrays;

public enum TeamTokenType {

	/**
	 * Team Racing Token
	 */
	AIRDROP(1), //
	/**
	 * Team Racing Token
	 */
	BLITZKRIEG(2), //
	/**
	 * Team Racing Token
	 */
	WHITELIST(3), //
	/**
	 * Team Racing Token
	 */
	INCOME(4), //
	/**
	 * Team Racing Token
	 */
	TREASURE_KEEPER(5), //
	/**
	 * Team Racing Token
	 */
	FORCE_TOLL(6), //
	/**
	 * Team Racing Token
	 */
	TEAM_MANAGER(7), //
	/**
	 * Team Racing Token
	 */
	NEUTRAL_ZONE(8), //
	/**
	 * Team Racing Token
	 */
	WINNING_TAX(9), //
	/**
	 * Team Racing Token
	 */
	DRIFTERS_LAND(10), //
	/**
	 * Team Racing Token
	 */
	LORDOFTHEDIAMONDS(11), //
	/**
	 * Team Racing Token
	 */
	DIVERSION(12), //
	/**
	 * Team Racing Token
	 */
	ARABIAN_CASINO(13), //
	/**
	 * Team Racing Token (Random)
	 */
	PANDORA_BOX(14), //
	/**
	 * Team Racing Token (Pandora Box only)
	 */
	FAIL_TAX(15), //
	/**
	 * Team Racing Token (Pandora Box only)
	 */
	LUCKY_CLEVER(16), //
	/**
	 * Team Racing Token (Pandora Box only)
	 */
	HAMMERTIME(17); //

	private int id;

	private TeamTokenType(int id) {
		this.id = id;
	}

	public Long getId() {
		return Integer.valueOf(id).longValue();
	}

	public static TeamTokenType valueOf(int value) {
		return Arrays.stream(values()).filter(legNo -> legNo.id == value).findFirst().get();
	}

}
