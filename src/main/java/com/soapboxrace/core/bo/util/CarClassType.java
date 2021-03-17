package com.soapboxrace.core.bo.util;

import java.util.Arrays;

public enum CarClassType {

	/**
	 * S-Class (750 to 1000 R, Highest)
	 */
	S_CLASS(-2142411446), //
	/**
	 * A-Class (600 to 750 R)
	 */
	A_CLASS(-405837480), //
	/**
	 * B-Class (500 to 600 R)
	 */
	B_CLASS(-406473455), //
	/**
	 * C-Class (400 to 500 R)
	 */
	C_CLASS(1866825865), //
	/**
	 * D-Class (250 to 400 R)
	 */
	D_CLASS(415909161), //
	/** 
	 * E-Class (30 to 250 R, Lowest)
	 */
	E_CLASS(872416321), //
	/**
	 * Misc-Class (Traffic & Drift-Spec vehicles)
	 */
	MISC(0), //
	/**
	 * Open Class (Event Restriction)
	 */
	OPEN_CLASS(607077938); //

	private int id;

	private CarClassType(int id) {
		this.id = id;
	}

	public int getId() {
		return Integer.valueOf(id);
	}
	
	public Long getIdLong() {
		return Integer.valueOf(id).longValue();
	}

	public static CarClassType valueOf(int value) {
		return Arrays.stream(values()).filter(legNo -> legNo.id == value).findFirst().get();
	}

}
