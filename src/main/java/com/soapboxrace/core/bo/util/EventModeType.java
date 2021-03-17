package com.soapboxrace.core.bo.util;

import java.util.Arrays;

public enum EventModeType {

	/**
	 * Circuit (SP / MP)
	 */
	CIRCUIT(4), //
	/**
	 * Sprint (SP / MP)
	 */
	SPRINT(9), //
	/**
	 * Pursuit Outrun (SP)
	 */
	PURSUIT_OUTRUN(12), //
	/**
	 * Drag (SP / MP)
	 */
	DRAG(19), //
	/**
	 * Meeting Place (MP)
	 */
	MEETING_PLACE(22), //
	/** 
	 * Team Escape (MP)
	 */
	TEAM_ESCAPE(24), //
	/**
	 * Interceptor (Pseudo game mode, MP)
	 */
	INTERCEPTOR(100); //

	private int id;

	private EventModeType(int id) {
		this.id = id;
	}

	public int getId() {
		return Integer.valueOf(id);
	}
	
	public Long getIdLong() {
		return Integer.valueOf(id).longValue();
	}

	public static EventModeType valueOf(int value) {
		return Arrays.stream(values()).filter(legNo -> legNo.id == value).findFirst().get();
	}

}
