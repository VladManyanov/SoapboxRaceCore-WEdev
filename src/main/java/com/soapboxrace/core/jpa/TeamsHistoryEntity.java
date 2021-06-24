package com.soapboxrace.core.jpa;

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
import javax.persistence.Table;

@Entity
@Table(name = "TEAMSHISTORY")
@NamedQueries({ //
		@NamedQuery(name = "TeamsHistoryEntity.findBySeason", query = "SELECT obj FROM TeamsHistoryEntity obj WHERE obj.season = :season"), //
		@NamedQuery(name = "TeamsHistoryEntity.getTopTeamIdFromPreviousSeason", query = "SELECT obj FROM TeamsHistoryEntity obj WHERE obj.season = :season ORDER BY points DESC") //
})
public class TeamsHistoryEntity {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long teamId;
	private int points;
	private int season;

	@ManyToOne
	@JoinColumn(name = "TEAMID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "TEAMS_PERSONA_FK"))
	private TeamsEntity team;

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}
	
	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}
	
	public TeamsEntity getTeam() {
		return team;
	}

	public void setTeam(TeamsEntity team) {
		this.team = team;
	}
}
