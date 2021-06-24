package com.soapboxrace.core.jpa;

import java.time.LocalDateTime;

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
@Table(name = "TEAMS_MATCHES")
@NamedQueries({ //
	@NamedQuery(name = "TeamsMatchesEntity.findInvitesByTeam", query = "SELECT obj FROM TeamsMatchesEntity obj WHERE obj.team2 = :team2") //
})
public class TeamsMatchesEntity {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	
	@ManyToOne
	@JoinColumn(name = "TEAM1ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "TEAMS_MATCHES_FK_1"))
	private TeamsEntity team1;

	@ManyToOne
	@JoinColumn(name = "TEAM2ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "TEAMS_MATCHES_FK_2"))
	private TeamsEntity team2;
	
	private boolean accepted;
	private int eventId1;
	private int eventId2;
	private int eventId3;
	private int eventId4;
	
	private int team1Score;
	private int team2Score;
	
	@ManyToOne
	@JoinColumn(name = "REWARDREGIONID", referencedColumnName = "REGIONID", foreignKey = @ForeignKey(name = "TEAMS_MATCHES_FK"))
	private TeamsRegionsEntity rewardRegionId;
	
	private int forceClassHash;
	private boolean forcePowerups;
	private LocalDateTime creationDate;
	private LocalDateTime finishDate;

	public Long getId() {
		return Id;
	}

	public void setId(Long Id) {
		this.Id = Id;
	}
	
	public TeamsEntity getTeam1() {
		return team1;
	}

	public void setTeam1(TeamsEntity team1) {
		this.team1 = team1;
	}
	
	public TeamsEntity getTeam2() {
		return team2;
	}

	public void setTeam2(TeamsEntity team2) {
		this.team2 = team2;
	}
	
	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
	
	public int getEventId1() {
		return eventId1;
	}

	public void setEventId1(int eventId1) {
		this.eventId1 = eventId1;
	}
	
	public int getEventId2() {
		return eventId2;
	}

	public void setEventId2(int eventId2) {
		this.eventId2 = eventId2;
	}
	
	public int getEventId3() {
		return eventId3;
	}

	public void setEventId3(int eventId3) {
		this.eventId3 = eventId3;
	}
	
	public int getEventId4() {
		return eventId4;
	}

	public void setEventId4(int eventId4) {
		this.eventId4 = eventId4;
	}
	
	public int getTeam1Score() {
		return team1Score;
	}

	public void setTeam1Score(int team1Score) {
		this.team1Score = team1Score;
	}
	
	public int getTeam2Score() {
		return team2Score;
	}

	public void setTeam2Score(int team2Score) {
		this.team2Score = team2Score;
	}
	
	public TeamsRegionsEntity getRewardRegionId() {
		return rewardRegionId;
	}

	public void setRewardRegionId(TeamsRegionsEntity rewardRegionId) {
		this.rewardRegionId = rewardRegionId;
	}
	
	public int getForceClassHash() {
		return forceClassHash;
	}
	
	public void setForceClassHash(int forceClassHash) {
		this.forceClassHash = forceClassHash;
	}
	
	public boolean getForcePowerups() {
		return forcePowerups;
	}
	
	public void setForcePowerups(boolean forcePowerups) {
		this.forcePowerups = forcePowerups;
	}
	
	public LocalDateTime getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}
	
	public LocalDateTime getFinishDate() {
		return finishDate;
	}
	
	public void setFinishDate(LocalDateTime finishDate) {
		this.finishDate = finishDate;
	}

}
