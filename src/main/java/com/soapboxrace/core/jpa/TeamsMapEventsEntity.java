package com.soapboxrace.core.jpa;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "TEAMS_MAPEVENTS")
@NamedQueries({ //
		@NamedQuery(name = "TeamsMapEventsEntity.findByRegion", query = "SELECT obj FROM TeamsMapEventsEntity obj WHERE obj.region = :region") //
})
public class TeamsMapEventsEntity {
	
	@Id
	private int eventId;

	@ManyToOne
	@JoinColumn(name = "REGIONID", referencedColumnName = "REGIONID", foreignKey = @ForeignKey(name = "TEAMS_MAPEVENTS_FK_1"))
	private TeamsRegionsEntity region;
	
	@ManyToOne
	@JoinColumn(name = "TEAMWINNERID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "TEAMS_MAPEVENTS_FK"))
	private TeamsEntity teamWinner;
	
	private int forceVehicleHash;
	private float posX;
	private float posY;

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}
	
	public TeamsRegionsEntity getRegion() {
		return region;
	}

	public void setRegion(TeamsRegionsEntity region) {
		this.region = region;
	}
	
	public TeamsEntity getTeamWinner() {
		return teamWinner;
	}

	public void setTeamWinner(TeamsEntity teamWinner) {
		this.teamWinner = teamWinner;
	}

	public int getForceVehicleHash() {
		return forceVehicleHash;
	}

	public void setForceVehicleHash(int forceVehicleHash) {
		this.forceVehicleHash = forceVehicleHash;
	}
	
	public float getPosX() {
		return posX;
	}

	public void setPosX(float posX) {
		this.posX = posX;
	}
	
	public float getPosY() {
		return posY;
	}

	public void setPosY(float posY) {
		this.posY = posY;
	}

}
