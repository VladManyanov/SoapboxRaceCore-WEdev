package com.soapboxrace.core.jpa;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "TEAMS_REGIONS")
@NamedQueries({ //
		@NamedQuery(name = "TeamsRegionsEntity.findAllRegions", query = "SELECT obj FROM TeamsRegionsEntity obj") //
})
public class TeamsRegionsEntity {

	@Id
	@Column(name = "REGIONID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long regionId;
	private String regionName;
	
	@ManyToOne
	@JoinColumn(name = "TEAMOWNERID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "TEAMS_REGIONS_FK"))
	private TeamsEntity teamOwner;

	@ManyToOne
	@JoinColumn(name = "TEAMPREVOWNERID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "TEAMS_REGIONS_FK_1"))
	private TeamsEntity teamPrevOwner;
	
	@OneToMany(mappedBy = "region", targetEntity = TeamsMapEventsEntity.class)
	private List<TeamsMapEventsEntity> listOfEvents;
	
	private boolean startBonus;
	private float activity;
	private float posX;
	private float posY;

	public Long getRegionId() {
		return regionId;
	}

	public void setRegionId(Long regionId) {
		this.regionId = regionId;
	}
	
	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	
	public TeamsEntity getTeamOwner() {
		return teamOwner;
	}

	public void setTeamOwner(TeamsEntity teamOwner) {
		this.teamOwner = teamOwner;
	}
	
	public List<TeamsMapEventsEntity> getListOfEvents() {
		return listOfEvents;
	}

	public TeamsEntity getTeamPrevOwner() {
		return teamPrevOwner;
	}

	public void setTeamPrevOwner(TeamsEntity teamPrevOwner) {
		this.teamPrevOwner = teamPrevOwner;
	}
	
	public boolean getStartBonus() {
		return startBonus;
	}

	public void setStartBonus(boolean startBonus) {
		this.startBonus = startBonus;
	}

	public float getActivity() {
		return activity;
	}

	public void setActivity(float activity) {
		this.activity = activity;
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
