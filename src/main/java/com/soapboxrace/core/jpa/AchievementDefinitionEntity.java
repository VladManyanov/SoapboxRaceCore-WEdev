package com.soapboxrace.core.jpa;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ACHIEVEMENT_DEFINITION")
public class AchievementDefinitionEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@OneToOne
	@JoinColumn(name = "badgeDefinitionId", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_ACHDEF_BADGEDEF"))
	private BadgeDefinitionEntity badgeDefinition;

	@Column(name = "canProgress")
	private boolean canProgress;

	@Column(name = "currentValue")
	private Long currentValue;

	@Column(name = "isVisible")
	private boolean isVisible;

	@Column(name = "progressText")
	private String progressText;

	@OneToMany(mappedBy = "achievementDefinition", fetch = FetchType.EAGER, targetEntity = AchievementRankEntity.class)
	private List<AchievementRankEntity> ranks;

	@Column(name = "statConversion")
	private String statConversion;

	@Column(name = "friendlyidentifier")
	private String friendlyidentifier;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean visible) {
		this.isVisible = visible;
	}

	public String getProgressText() {
		return progressText;
	}

	public void setProgressText(String progressText) {
		this.progressText = progressText;
	}

	public BadgeDefinitionEntity getBadgeDefinition() {
		return badgeDefinition;
	}

	public void setBadgeDefinition(BadgeDefinitionEntity badgeDefinition) {
		this.badgeDefinition = badgeDefinition;
	}

	public List<AchievementRankEntity> getRanks() {
		return ranks;
	}

	public void setRanks(List<AchievementRankEntity> ranks) {
		this.ranks = ranks;
	}

	public String getStatConversion() {
		return statConversion;
	}

	public void setStatConversion(String statConversion) {
		this.statConversion = statConversion;
	}

	public boolean isCanProgress() {
		return canProgress;
	}

	public void setCanProgress(boolean canProgress) {
		this.canProgress = canProgress;
	}

	public Long getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(Long currentValue) {
		this.currentValue = currentValue;
	}

	public String getFriendlyidentifier() {
		return friendlyidentifier;
	}

	public void setFriendlyidentifier(String friendlyidentifier) {
		this.friendlyidentifier = friendlyidentifier;
	}
	
}
