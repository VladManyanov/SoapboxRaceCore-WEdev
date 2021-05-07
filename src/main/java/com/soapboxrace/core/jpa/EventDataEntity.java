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
@Table(name = "EVENT_DATA")
@NamedQueries({ //
		@NamedQuery(name = "EventDataEntity.findByPersona", query = "SELECT obj FROM EventDataEntity obj WHERE obj.personaId = :personaId"), //
		@NamedQuery(name = "EventDataEntity.getRacers", query = "SELECT obj FROM EventDataEntity obj WHERE obj.eventSessionId = :eventSessionId"), //
		@NamedQuery(name = "EventDataEntity.getRacersRanked", query = "SELECT obj FROM EventDataEntity obj WHERE obj.eventSessionId = :eventSessionId AND obj.finishReason <> 0 AND obj.rank <> 0 ORDER BY obj.serverEventDuration ASC"), //
		@NamedQuery(name = "EventDataEntity.getRacersTEFinished", query = "SELECT obj FROM EventDataEntity obj WHERE obj.eventSessionId = :eventSessionId AND obj.finishReason = 22 AND obj.rank <> 0"), //
		@NamedQuery(name = "EventDataEntity.findByPersonaAndType", query = "SELECT obj FROM EventDataEntity obj WHERE obj.personaId = :personaId AND obj.eventModeId = :eventModeId"), //
		@NamedQuery(name = "EventDataEntity.findByPersonaAndEventSessionId", query = "SELECT obj FROM EventDataEntity obj WHERE obj.personaId = :personaId AND obj.eventSessionId = :eventSessionId") //
})
public class EventDataEntity {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "EVENTID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_EVENTDATA_EVENT"))
	private EventEntity event;

	private Long personaId;
	private int eventModeId;
	private Long eventSessionId;

	// ArbitrationPacket variables
	private long alternateEventDurationInMilliseconds;
	private long carId;
	private long eventDurationInMilliseconds;
	private long serverEventDuration;
	private int finishReason;
	private long hacksDetected;
	private int rank;

	// Other ArbitrationPacket Global variables
	protected long longestJumpDurationInMilliseconds;
	protected long sumOfJumpsDurationInMilliseconds;
	protected float topSpeed;
	protected float avgSpeed;

	// RouteArbitrationPacket variables
	protected long bestLapDurationInMilliseconds;
	protected float fractionCompleted;
	protected int numberOfCollisions;
	protected int perfectStart;

	// PursuitArbitrationPacket and TeamEscapeArbitrationPacket Global variable
	protected int copsDeployed;
	protected int copsDisabled;
	protected int copsRammed;
	protected int costToState;
	protected int infractions;
	protected int roadBlocksDodged;
	protected int spikeStripsDodged;

	// PursuitArbitrationPacket variable
	protected float heat;

	// TeamEscapeArbitrationPacket variables
	protected int bustedCount;
	protected float distanceToFinish;
	
	// Arbitration packet check
	protected boolean arbitration;
	protected boolean isSingle;
	protected boolean speedBugChance; // after 2 hours of playing, NFSW's time system can glitch sometimes, giving a possible player advantage
	// so server will save this value is player was logged for 2 hours and more
	protected int carVersion;
	protected LocalDateTime date;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public EventEntity getEvent() {
		return event;
	}

	public void setEvent(EventEntity event) {
		this.event = event;
	}

	public Long getPersonaId() {
		return personaId;
	}

	public void setPersonaId(Long value) {
		this.personaId = value;
	}

	public int getEventModeId() {
		return eventModeId;
	}

	public void setEventModeId(int value) {
		this.eventModeId = value;
	}

	public Long getEventSessionId() {
		return eventSessionId;
	}

	public void setEventSessionId(Long value) {
		this.eventSessionId = value;
	}

	// ArbitrationPacket functions
	public long getAlternateEventDurationInMilliseconds() {
		return alternateEventDurationInMilliseconds;
	}

	public void setAlternateEventDurationInMilliseconds(long value) {
		this.alternateEventDurationInMilliseconds = value;
	}

	public long getCarId() {
		return carId;
	}

	public void setCarId(long value) {
		this.carId = value;
	}

	public long getEventDurationInMilliseconds() {
		return eventDurationInMilliseconds;
	}

	public void setEventDurationInMilliseconds(long value) {
		this.eventDurationInMilliseconds = value;
	}
	
	public long getServerEventDuration() {
		return serverEventDuration;
	}

	public void setServerEventDuration(long value) {
		this.serverEventDuration = value;
	}

	public int getFinishReason() {
		return finishReason;
	}

	public void setFinishReason(int value) {
		this.finishReason = value;
	}

	public long getHacksDetected() {
		return hacksDetected;
	}

	public void setHacksDetected(long value) {
		this.hacksDetected = value;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int value) {
		this.rank = value;
	}

	// OtherArbitrationPacket Global functions
	public long getLongestJumpDurationInMilliseconds() {
		return longestJumpDurationInMilliseconds;
	}

	public void setLongestJumpDurationInMilliseconds(long value) {
		this.longestJumpDurationInMilliseconds = value;
	}

	public long getSumOfJumpsDurationInMilliseconds() {
		return sumOfJumpsDurationInMilliseconds;
	}

	public void setSumOfJumpsDurationInMilliseconds(long value) {
		this.sumOfJumpsDurationInMilliseconds = value;
	}

	public float getTopSpeed() {
		return topSpeed;
	}

	public void setTopSpeed(float value) {
		this.topSpeed = value;
	}
	
	public float getAvgSpeed() {
		return avgSpeed;
	}

	public void setAvgSpeed(float value) {
		this.avgSpeed = value;
	}

	// RouteArbitrationPacket functions
	public long getBestLapDurationInMilliseconds() {
		return bestLapDurationInMilliseconds;
	}

	public void setBestLapDurationInMilliseconds(long value) {
		this.bestLapDurationInMilliseconds = value;
	}

	public float getFractionCompleted() {
		return fractionCompleted;
	}

	public void setFractionCompleted(float value) {
		this.fractionCompleted = value;
	}

	public int getNumberOfCollisions() {
		return numberOfCollisions;
	}

	public void setNumberOfCollisions(int value) {
		this.numberOfCollisions = value;
	}

	public int getPerfectStart() {
		return perfectStart;
	}

	public void setPerfectStart(int value) {
		this.perfectStart = value;
	}

	// PursuitArbitrationPacket and TeamEscapeArbitrationPacket Global functions
	public int getCopsDeployed() {
		return copsDeployed;
	}

	public void setCopsDeployed(int value) {
		this.copsDeployed = value;
	}

	public int getCopsDisabled() {
		return copsDisabled;
	}

	public void setCopsDisabled(int value) {
		this.copsDisabled = value;
	}

	public int getCopsRammed() {
		return copsRammed;
	}

	public void setCopsRammed(int value) {
		this.copsRammed = value;
	}

	public int getCostToState() {
		return costToState;
	}

	public void setCostToState(int value) {
		this.costToState = value;
	}

	public int getInfractions() {
		return infractions;
	}

	public void setInfractions(int value) {
		this.infractions = value;
	}

	public int getRoadBlocksDodged() {
		return roadBlocksDodged;
	}

	public void setRoadBlocksDodged(int value) {
		this.roadBlocksDodged = value;
	}

	public int getSpikeStripsDodged() {
		return spikeStripsDodged;
	}

	public void setSpikeStripsDodged(int value) {
		this.spikeStripsDodged = value;
	}

	// PursuitArbitrationPacket function
	public float getHeat() {
		return heat;
	}

	public void setHeat(float value) {
		this.heat = value;
	}

	// TeamEscapeArbitrationPacket functions
	public int getBustedCount() {
		return bustedCount;
	}

	public void setBustedCount(int value) {
		this.bustedCount = value;
	}

	public float getDistanceToFinish() {
		return distanceToFinish;
	}

	public void setDistanceToFinish(float value) {
		this.distanceToFinish = value;
	}
	
	public boolean getArbitration() {
		return arbitration;
	}

	public void setArbitration(boolean value) {
		this.arbitration = value;
	}
	
	public boolean getIsSingle() {
		return isSingle;
	}

	public void setIsSingle(boolean value) {
		this.isSingle = value;
	}
	
	public boolean getSpeedBugChance() {
		return speedBugChance;
	}

	public void setSpeedBugChance(boolean value) {
		this.speedBugChance = value;
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

}
