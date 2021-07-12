package com.soapboxrace.core.jpa;

import java.time.LocalDateTime;

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
@Table(name = "TEAMS_TOKENS")
@NamedQueries({ //
	@NamedQuery(name = "TeamsTokensEntity.findByAffectedEventId", query = "SELECT obj FROM TeamsTokensEntity obj WHERE obj.tokenValue2 = :tokenValue2 AND obj.activated = true"), //
	@NamedQuery(name = "TeamsTokensEntity.findAllByTeam", query = "SELECT obj FROM TeamsTokensEntity obj WHERE obj.activated = true AND obj.teamOwner = :teamOwner"), //
	
	@NamedQuery(name = "TeamsTokensEntity.lookForIncomeToken", query = "SELECT obj FROM TeamsTokensEntity obj WHERE obj.activated = true AND obj.tokenType = 'INCOME' AND obj.teamOwner = :teamOwner"), //
	@NamedQuery(name = "TeamsTokensEntity.lookForTHKeeperToken", query = "SELECT obj FROM TeamsTokensEntity obj WHERE obj.activated = true AND obj.tokenType = 'TREASURE_KEEPER' AND obj.teamOwner = :teamOwner"), //
	@NamedQuery(name = "TeamsTokensEntity.lookForNeutralZoneToken", query = "SELECT obj FROM TeamsTokensEntity obj WHERE obj.activated = true AND obj.tokenType = 'NEUTRAL_ZONE' AND obj.tokenValue2 = :tokenValue2") //
})
public class TeamsTokensEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "TEAMOWNERID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "TEAMS_TOKENS_FK"))
	private TeamsEntity teamOwner;
	
	private String tokenType;
	private boolean activated;
	private LocalDateTime activationDate;
	
	private int tokenValue; // Token effect value
	private int tokenValue2; // Event ID, if token should be applied for specific event

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public TeamsEntity getTeamOwner() {
		return teamOwner;
	}

	public void setTeamOwner(TeamsEntity teamOwner) {
		this.teamOwner = teamOwner;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
	
	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}
	
	public int getTokenValue() {
		return tokenValue;
	}

	public void setTokenValue(int tokenValue) {
		this.tokenValue = tokenValue;
	}
	
	public int getTokenValue2() {
		return tokenValue2;
	}

	public void setTokenValue2(int tokenValue2) {
		this.tokenValue2 = tokenValue2;
	}
	
	public LocalDateTime getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(LocalDateTime activationDate) {
		this.activationDate = activationDate;
	}

}
