package com.soapboxrace.core.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
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
import javax.persistence.Transient;

@Entity
@Table(name = "LOBBY")
@NamedQueries({ //
		@NamedQuery(name = "LobbyEntity.findAll", query = "SELECT obj FROM UserEntity obj"), //
		@NamedQuery(name = "LobbyEntity.findAllOpen", //
				query = "SELECT obj FROM LobbyEntity obj WHERE obj.isPrivate = false AND obj.started = false AND ((obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.isReserved = false)) "), //
		
		@NamedQuery(name = "LobbyEntity.findByEventStarted", query = "SELECT obj FROM LobbyEntity obj WHERE obj.event = :event AND obj.started = false AND ((obj.lobbyDateTimeStart between :dateTime1 AND :dateTime2) OR (obj.isReserved = false)) AND obj.isPrivate = false "), //
		@NamedQuery(name = "LobbyEntity.findByEventAndPersona", query = "SELECT obj FROM LobbyEntity obj WHERE obj.started = false AND obj.event = :event AND ((obj.lobbyDateTimeStart between :dateTime1 AND :dateTime2) OR (obj.isReserved = falsel)) AND obj.isPrivate = true AND obj.personaId = :personaId "), //
		@NamedQuery(name = "LobbyEntity.findByHosterPersona", query = "SELECT obj FROM LobbyEntity obj WHERE obj.started = false AND obj.personaId = :personaId "), //
		@NamedQuery(name = "LobbyEntity.isThisLobbyReserved", query = "SELECT obj FROM LobbyEntity obj WHERE obj.started = false AND obj.id = :id AND obj.isReserved = false "), //
		@NamedQuery(name = "LobbyEntity.deleteAll", query = "DELETE FROM LobbyEntity obj") //
})
public class LobbyEntity {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "EVENTID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_LOBBY_EVENT"))
	private EventEntity event;

	@OneToMany(mappedBy = "lobby", targetEntity = LobbyEntrantEntity.class, cascade = CascadeType.MERGE)
	private List<LobbyEntrantEntity> entrants;

	private Date lobbyDateTimeStart;

	private Boolean isPrivate;

	private Long personaId;
	
	private Long team1Id;
	
	private Long team2Id;
	
	private Boolean isReserved;
	
	private int carClassHash;
	
	private Boolean started;

	@Transient
	private Long lobbyCountdownInMilliseconds = 45000L;

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

	public List<LobbyEntrantEntity> getEntrants() {
		return entrants;
	}

	public void setEntrants(List<LobbyEntrantEntity> entrants) {
		this.entrants = entrants;
	}

	public Date getLobbyDateTimeStart() {
		return lobbyDateTimeStart;
	}

	public void setLobbyDateTimeStart(Date lobbyDateTimeStart) {
		this.lobbyDateTimeStart = lobbyDateTimeStart;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}

	public void setIsPrivate(Boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public Long getPersonaId() {
		return personaId;
	}

	public void setPersonaId(Long personaId) {
		this.personaId = personaId;
	}
	
	public Long getTeam1Id() {
		return team1Id;
	}

	public void setTeam1Id(Long team1Id) {
		this.team1Id = team1Id;
	}
	
	public Long getTeam2Id() {
		return team2Id;
	}

	public void setTeam2Id(Long team2Id) {
		this.team2Id = team2Id;
	}
	
	public Boolean isReserved() {
		return isReserved;
	}

	public void setIsReserved(Boolean isReserved) {
		this.isReserved = isReserved;
	}
	
	public int getCarClassHash() {
		return carClassHash;
	}

	public void setCarClassHash(int carClassHash) {
		this.carClassHash = carClassHash;
	}
	
	public Boolean isStarted() {
		return started;
	}

	public void setStarted(Boolean started) {
		this.started = started;
	}

	public boolean add(LobbyEntrantEntity e) {
		if (entrants == null) {
			entrants = new ArrayList<>();
		}
		return entrants.add(e);
	}

}
