
package com.soapboxrace.core.jpa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Table(name = "PERSONA")
@NamedQueries({ //
		@NamedQuery(name = "PersonaEntity.findByName", query = "SELECT obj FROM PersonaEntity obj WHERE obj.name = :name AND obj.isHidden = false"), //
		@NamedQuery(name = "PersonaEntity.findAllHiddenDrivers", query = "SELECT obj FROM PersonaEntity obj WHERE obj.isHidden = true") //
})
public class PersonaEntity {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long personaId;
	private int cash;
	private int iconIndex;
	private int level;
	private String motto;
	private String name;
	private float percentToLevel;
	private double rating;
	private double rep;
	private int repAtCurrentLevel;
	private int score;
	private int curCarIndex = 0;
	private int carSlots = 6;
	private int racesCount;
	private LocalDate dailyRaceDate;
	private boolean isHidden; // Temporarily removed persona
	private boolean ignoreRaces; // Permanent "Ignore the event after being declined on the search" switch
	private int priorityMMTimeout; // Personal Priority Class Group search timeout
	private boolean raceAgain; // Permanent "Does the Race Again lobby invite on the finish appears" switch
	private int seqCSCurrentEvent; // Current Daily Series event (sequential mode only)

	@ManyToOne
	@JoinColumn(name = "USERID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_PERSONA_USER"))
	private UserEntity user;
	
	@ManyToOne
	@JoinColumn(name = "TEAMID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "PERSONA_TEAMS_FK"))
	private TeamsEntity team;

	@OneToMany(mappedBy = "persona", targetEntity = BadgePersonaEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<BadgePersonaEntity> listOfBadges;

	@Column(name = "created")
	private LocalDateTime created;

	public int getCash() {
		return cash;
	}

	public void setCash(int cash) {
		this.cash = cash;
	}

	public int getIconIndex() {
		return iconIndex;
	}

	public void setIconIndex(int iconIndex) {
		this.iconIndex = iconIndex;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getMotto() {
		return motto;
	}

	public void setMotto(String motto) {
		this.motto = motto;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getPercentToLevel() {
		return percentToLevel;
	}

	public void setPercentToLevel(float percentToLevel) {
		this.percentToLevel = percentToLevel;
	}

	public Long getPersonaId() {
		return personaId;
	}

	public void setPersonaId(Long personaId) {
		this.personaId = personaId;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public double getRep() {
		return rep;
	}

	public void setRep(double rep) {
		this.rep = rep;
	}

	public int getRepAtCurrentLevel() {
		return repAtCurrentLevel;
	}

	public void setRepAtCurrentLevel(int repAtCurrentLevel) {
		this.repAtCurrentLevel = repAtCurrentLevel;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}
	
	public TeamsEntity getTeam() {
		return team;
	}

	public void setTeam(TeamsEntity team) {
		this.team = team;
	}

	public int getCurCarIndex() {
		return curCarIndex;
	}

	public void setCurCarIndex(int curCarIndex) {
		this.curCarIndex = curCarIndex;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public int getCarSlots() {
		return carSlots;
	}

	public void setCarSlots(int carSlots) {
		this.carSlots = carSlots;
	}

	public List<BadgePersonaEntity> getListOfBadges() {
		return listOfBadges;
	}

	public void setListOfBadges(List<BadgePersonaEntity> listOfBadges) {
		this.listOfBadges = listOfBadges;
	}
	
	public int getRacesCount() {
		return racesCount;
	}

	public void setRacesCount(int racesCount) {
		this.racesCount = racesCount;
	}
	
	public LocalDate getDailyRaceDate() {
		return dailyRaceDate;
	}

	public void setDailyRaceDate(LocalDate dailyRaceDate) {
		this.dailyRaceDate = dailyRaceDate;
	}
	
	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}
	
	public boolean isIgnoreRaces() {
		return ignoreRaces;
	}

	public void setIgnoreRaces(boolean ignoreRaces) {
		this.ignoreRaces = ignoreRaces;
	}
	
	public int getPriorityMMTimeout() {
		return priorityMMTimeout;
	}

	public void setPriorityMMTimeout(int priorityMMTimeout) {
		this.priorityMMTimeout = priorityMMTimeout;
	}
	
	public boolean getRaceAgain() {
		return raceAgain;
	}

	public void setRaceAgain(boolean raceAgain) {
		this.raceAgain = raceAgain;
	}
	
	public int getSeqCSCurrentEvent() {
		return seqCSCurrentEvent;
	}

	public void setSeqCSCurrentEvent(int seqCSCurrentEvent) {
		this.seqCSCurrentEvent = seqCSCurrentEvent;
	}

}
