package com.soapboxrace.jaxb.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Информация о поиске лобби в Быстрой Гонке (Race Now)
 * @author Hypercycle
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MatchmakingInfo", propOrder = {
	"playercount_all",
	"playercount_s",
	"playercount_a",
	"playercount_b",
	"playercount_c",
	"playercount_d",
	"playercount_e",
	"playercount_misc",
	"activelobbies",
})
public class ArrayOfMMLobbies {
	@XmlElement(name = "PlayerCount_All")
	private int playercount_all;
	@XmlElement(name = "PlayerCount_S")
	private int playercount_s;
	@XmlElement(name = "PlayerCount_A")
	private int playercount_a;
	@XmlElement(name = "PlayerCount_B")
	private int playercount_b;
	@XmlElement(name = "PlayerCount_C")
	private int playercount_c;
	@XmlElement(name = "PlayerCount_D")
	private int playercount_d;
	@XmlElement(name = "PlayerCount_E")
	private int playercount_e;
	@XmlElement(name = "PlayerCount_Misc")
	private int playercount_misc;
	@XmlElement(name = "ActiveLobby")
	private List<MMLobbyInfo> activelobbies;
	
	public ArrayOfMMLobbies() {
		activelobbies = new ArrayList<MMLobbyInfo>();
	}
	/**
	 * Количество игроков в поиске Race Now
	 * @param playercount_all
	 */
	public int getPlayerCountAll() {
		return playercount_all;
	}
	public void setPlayerCountAll(int playercount_all) {
		this.playercount_all = playercount_all;
	}
	/**
	 * Количество игроков в поиске Race Now с классом S
	 * @param playercount_s
	 */
	public int getPlayerCountS() {
		return playercount_s;
	}
	public void setPlayerCountS(int playercount_s) {
		this.playercount_s = playercount_s;
	}
	/**
	 * Количество игроков в поиске Race Now с классом A
	 * @param playercount_a
	 */
	public int getPlayerCountA() {
		return playercount_a;
	}
	public void setPlayerCountA(int playercount_a) {
		this.playercount_a = playercount_a;
	}
	/**
	 * Количество игроков в поиске Race Now с классом B
	 * @param playercount_b
	 */
	public int getPlayerCountB() {
		return playercount_b;
	}
	public void setPlayerCountB(int playercount_b) {
		this.playercount_b = playercount_b;
	}
	/**
	 * Количество игроков в поиске Race Now с классом C
	 * @param playercount_c
	 */
	public int getPlayerCountC() {
		return playercount_c;
	}
	public void setPlayerCountC(int playercount_c) {
		this.playercount_c = playercount_c;
	}
	/**
	 * Количество игроков в поиске Race Now с классом D
	 * @param playercount_d
	 */
	public int getPlayerCountD() {
		return playercount_d;
	}
	public void setPlayerCountD(int playercount_d) {
		this.playercount_d = playercount_d;
	}
	/**
	 * Количество игроков в поиске Race Now с классом E
	 * @param playercount_e
	 */
	public int getPlayerCountE() {
		return playercount_e;
	}
	public void setPlayerCountE(int playercount_e) {
		this.playercount_e = playercount_e;
	}
	/**
	 * Количество игроков в поиске Race Now с машиной траффика или Drift-Spec
	 * @param playercount_misc
	 */
	public int getPlayerCountMisc() {
		return playercount_misc;
	}
	public void setPlayerCountMisc(int playercount_misc) {
		this.playercount_misc = playercount_misc;
	}
	/**
	 * Добавить активное лобби в список отображаемых лобби
	 * @param eventMode Режим заезда
	 * @param eventName Название заезда
	 * @param eventClass Классовое ограничение заезда
	 * @param hosterClass Класс автомобиля первого игрока лобби
	 * @param isTeamRace Есть ли в лобби игроки от команд
	 * @param isPlayersInside Есть ли внутри лобби игроки
	 */
	public void add(String eventMode, String eventName, String eventClass, String hosterClass, boolean isTeamRace, boolean isPlayersInside) {
		activelobbies.add(new MMLobbyInfo(eventMode, eventName, eventClass, hosterClass, isTeamRace, isPlayersInside));
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "MMLobbyInfo", propOrder = {
		"EVENT_MODE",
		"EVENT_NAME",
		"EVENT_CLASS",
		"HOSTER_CLASS",
		"ISTEAMRACE",
		"ISPLAYERSINSIDE",
	})
	public static class MMLobbyInfo {

		@XmlElement(name = "EventMode")
		private String EVENT_MODE;
		@XmlElement(name = "EventName")
		private String EVENT_NAME;
		@XmlElement(name = "EventClass")
		private String EVENT_CLASS;
		@XmlElement(name = "HosterClass")
		private String HOSTER_CLASS;
		@XmlElement(name = "IsTeamRace")
		private boolean ISTEAMRACE;
		@XmlElement(name = "IsPlayersInside")
		private boolean ISPLAYERSINSIDE;
		
		protected MMLobbyInfo (String eventMode, String eventName, String eventClass, String hosterClass, boolean isTeamRace, boolean isPlayersInside) {
			EVENT_MODE = eventMode;
			EVENT_NAME = eventName;
			EVENT_CLASS = eventClass;
			HOSTER_CLASS = hosterClass;
			ISTEAMRACE = isTeamRace;
			ISPLAYERSINSIDE = isPlayersInside;
		}
	}
}
