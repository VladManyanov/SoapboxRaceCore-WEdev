package com.soapboxrace.jaxb.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Общая информация о сервере и событиях
 * @author Hypercycle
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfServerInfo", propOrder = {
	"playerscount",
	"playerspeak",
	"playersregistered",
	"bonusclass",
	"challengeseriesevent",
	"trackrotation",
})
public class ArrayOfServerInfo {
	@XmlElement(name = "PlayersCount")
	private int playerscount;
	@XmlElement(name = "PlayersPeak")
	private int playerspeak;
	@XmlElement(name = "PlayersRegistered")
	private int playersregistered;
	@XmlElement(name = "BonusClass")
	private String bonusclass;
	@XmlElement(name = "ChallengeSeriesEvent")
	private int challengeseriesevent;
	@XmlElement(name = "TrackRotation")
	private int trackrotation;
	
	/**
	 * Set the PlayersCount variable
	 * @param playerscount
	 */
	public int getPlayersCount() {
		return playerscount;
	}
	public void setPlayersCount(int playerscount) {
		this.playerscount = playerscount;
	}
	/**
	 * Set the PlayersPeak variable
	 * @param playerspeak
	 */
	public int getPlayersPeak() {
		return playerspeak;
	}
	public void setPlayersPeak(int playerspeak) {
		this.playerspeak = playerspeak;
	}
	/**
	 * Set the PlayersRegistered variable
	 * @param playersregistered
	 */
	public int getPlayersRegistered() {
		return playersregistered;
	}
	public void setPlayersRegistered(int playersregistered) {
		this.playersregistered = playersregistered;
	}
	/**
	 * Set the BonusClass variable
	 * @param bonusclass
	 */
	public String getBonusClass() {
		return bonusclass;
	}
	public void setBonusClass(String bonusclass) {
		this.bonusclass = bonusclass;
	}
	/**
	 * Set the ChallengeSeriesEvent variable
	 * @param challengeseriesevent
	 */
	public int getChallengeSeriesEvent() {
		return challengeseriesevent;
	}
	public void setChallengeSeriesEvent(int challengeseriesevent) {
		this.challengeseriesevent = challengeseriesevent;
	}
	/**
	 * Set the TrackRotation variable
	 * @param trackrotation
	 */
	public int getTrackRotation() {
		return trackrotation;
	}
	public void setTrackRotation(int trackrotation) {
		this.trackrotation = trackrotation;
	}
}
