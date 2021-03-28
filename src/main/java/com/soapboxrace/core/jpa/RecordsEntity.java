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
@Table(name = "RECORDS")
@NamedQueries({ //
@NamedQuery(name = "RecordsEntity.findCurrentRace", //
		query = "SELECT obj FROM RecordsEntity obj WHERE obj.event = :event AND obj.user = :user AND obj.powerUps = :powerUps AND obj.carClassHash = :carClassHash "),//
@NamedQuery(name = "RecordsEntity.calcRecordPlace", //
        query = "SELECT obj FROM RecordsEntity obj "
        		+ "WHERE obj.event = :event "
        		+ "AND obj.powerUps = :powerUps "
        		+ "AND obj.carClassHash = :carClassHash "
        		+ "AND obj.timeMS <= :timeMS "
        		+ "AND obj.userBan = false "
        		+ "AND obj.isObsolete = false "
                + "ORDER BY obj.timeMS "),//

@NamedQuery(name = "RecordsEntity.statsEventPersona", // Note: obsolete personal records is being displayed 
        query = "SELECT obj FROM RecordsEntity obj "
                + "WHERE obj.event = :event "
                + "AND obj.userBan = false "
                + "AND obj.powerUps = :powerUps "
                + "AND obj.user = :user "
                + "ORDER BY obj.timeMS "),//

@NamedQuery(name = "RecordsEntity.checkAllRecords", 
        query = "SELECT obj FROM RecordsEntity obj WHERE obj.userBan = false AND obj.isObsolete = false AND obj.obsoleteChecked = false "),//
@NamedQuery(name = "RecordsEntity.uncheckAllRecords", 
        query = "UPDATE RecordsEntity obj SET obj.obsoleteChecked = false "),//

})
public class RecordsEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private long timeMS;
	private long timeMSAlt;
	private long timeMSSrv;
	private long timeMSOld;
	private long bestLapTimeMS;
	
	private boolean powerUps;
	private boolean perfectStart;
	private boolean isSingle;
	private float topSpeed;
	private float avgSpeed;
	private long airTimeMS;
	
	private int carClassHash;
	private int carPhysicsHash;
	private int carVersion;
	private LocalDateTime date;
	private String playerName;
	private String carName;
	
	private Long eventSessionId;
	private Long eventDataId;
	private int eventModeId;
	private boolean userBan;
	private boolean training;
	private boolean isObsolete;
	private boolean obsoleteChecked;
	
	@ManyToOne
	@JoinColumn(name = "EVENTPOWERUPSID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "RECORDS_EVENT_POWERUPS_FK"))
	private EventPowerupsEntity eventPowerups;
	
	@ManyToOne
	@JoinColumn(name = "EVENTID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "RECORDS_EVENT_FK"))
	private EventEntity event;
	
	@ManyToOne
	@JoinColumn(name = "PERSONAID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "RECORDS_PERSONA_FK"))
	private PersonaEntity persona;
	
	@ManyToOne
	@JoinColumn(name = "USERID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "RECORDS_USER_SB_FK"))
	private UserEntity user;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getTimeMS() {
		return timeMS;
	}

	public void setTimeMS(Long timeMS) {
		this.timeMS = timeMS;
	}
	
	public Long getTimeMSAlt() {
		return timeMSAlt;
	}

	public void setTimeMSAlt(Long timeMSAlt) {
		this.timeMSAlt = timeMSAlt;
	}
	
	public Long getTimeMSSrv() {
		return timeMSSrv;
	}

	public void setTimeMSSrv(Long timeMSSrv) {
		this.timeMSSrv = timeMSSrv;
	}
	
	public Long getTimeMSOld() {
		return timeMSOld;
	}

	public void setTimeMSOld(Long timeMSOld) {
		this.timeMSOld = timeMSOld;
	}
	
	public Long getBestLapTimeMS() {
		return bestLapTimeMS;
	}

	public void setBestLapTimeMS(Long bestLapTimeMS) {
		this.bestLapTimeMS = bestLapTimeMS;
	}
	
	public boolean getPowerUps() {
		return powerUps;
	}

	public void setPowerUps(boolean powerUps) {
		this.powerUps = powerUps;
	}
	
	public boolean getPerfectStart() {
		return perfectStart;
	}

	public void setPerfectStart(boolean perfectStart) {
		this.perfectStart = perfectStart;
	}
	
	public boolean getIsSingle() {
		return isSingle;
	}

	public void setIsSingle(boolean isSingle) {
		this.isSingle = isSingle;
	}
	
	public float getTopSpeed() {
		return topSpeed;
	}

	public void setTopSpeed(float topSpeed) {
		this.topSpeed = topSpeed;
	}
	
	public float getAvgSpeed() {
		return avgSpeed;
	}

	public void setAvgSpeed(float avgSpeed) {
		this.avgSpeed = avgSpeed;
	}
	
	public Long getAirTimeMS() {
		return airTimeMS;
	}

	public void setAirTimeMS(Long airTimeMS) {
		this.airTimeMS = airTimeMS;
	}
	
	public int getCarClassHash() {
		return carClassHash;
	}

	public void setCarClassHash(int carClassHash) {
		this.carClassHash = carClassHash;
	}
	
	public int getCarPhysicsHash() {
		return carPhysicsHash;
	}

	public void setCarPhysicsHash(int carPhysicsHash) {
		this.carPhysicsHash = carPhysicsHash;
	}
	
	public int getCarVersion() {
		return carVersion;
	}

	public void setCarVersion(int carVersion) {
		this.carVersion = carVersion;
	}
	
	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	
	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	public String getCarName() {
		return carName;
	}

	public void setCarName(String carName) {
		this.carName = carName;
	}
	
	public Long getEventSessionId() {
		return eventSessionId;
	}

	public void setEventSessionId(Long eventSessionId) {
		this.eventSessionId = eventSessionId;
	}
	
	public Long getEventDataId() {
		return eventDataId;
	}

	public void setEventDataId(Long eventDataId) {
		this.eventDataId = eventDataId;
	}
	
	public EventPowerupsEntity getEventPowerups() {
		return eventPowerups;
	}

	public void setEventPowerups(EventPowerupsEntity eventPowerups) {
		this.eventPowerups = eventPowerups;
	}
	
	public EventEntity getEvent() {
		return event;
	}

	public void setEvent(EventEntity event) {
		this.event = event;
	}
	
	public int getEventModeId() {
		return eventModeId;
	}

	public void setEventModeId(int eventModeId) {
		this.eventModeId = eventModeId;
	}
	
	public PersonaEntity getPersona() {
		return persona;
	}

	public void setPersona(PersonaEntity persona) {
		this.persona = persona;
	}
	
	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}
	
	public boolean getUserBan() {
		return userBan;
	}

	public void setUserBan(boolean userBan) {
		this.userBan = userBan;
	}
	
	public boolean isTraining() {
		return training;
	}

	public void setIsTraining(boolean training) {
		this.training = training;
	}
	
	public boolean isObsolete() {
		return isObsolete;
	}

	public void setIsObsolete(boolean isObsolete) {
		this.isObsolete = isObsolete;
	}
	
	public boolean ObsoleteChecked() {
		return obsoleteChecked;
	}

	public void setObsoleteChecked(boolean obsoleteChecked) {
		this.obsoleteChecked = obsoleteChecked;
	}
	
}
