package com.soapboxrace.core.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "TOKEN_SESSION")
@NamedQueries({ //
		@NamedQuery(name = "TokenSessionEntity.findByUserId", query = "SELECT obj FROM TokenSessionEntity obj WHERE obj.userId = :userId"), //
		@NamedQuery(name = "TokenSessionEntity.findByActivePersonaId", query = "SELECT obj FROM TokenSessionEntity obj WHERE obj.activePersonaId = :activePersonaId"), //
		@NamedQuery(name = "TokenSessionEntity.findBySecurityToken", query = "SELECT obj FROM TokenSessionEntity obj WHERE obj.securityToken = :securityToken"), //
		@NamedQuery(name = "TokenSessionEntity.deleteByUserId", query = "DELETE FROM TokenSessionEntity obj WHERE obj.userId = :userId"), //
		@NamedQuery(name = "TokenSessionEntity.updateRelayCrytoTicket", //
				query = "UPDATE TokenSessionEntity obj " // 
						+ "SET obj.relayCryptoTicket = :relayCryptoTicket WHERE obj.activePersonaId = :personaId"), //
		@NamedQuery(name = "TokenSessionEntity.updateLobbyId", //
				query = "UPDATE TokenSessionEntity obj " 
						+ "SET obj.activeLobbyId = :activeLobbyId WHERE obj.activePersonaId = :personaId"), //
		@NamedQuery(name = "TokenSessionEntity.getUsersOnlineCount", query = "SELECT Count(obj) FROM TokenSessionEntity obj WHERE obj.expirationDate >= NOW() AND obj.isLoggedIn = true"), //
		@NamedQuery(name = "TokenSessionEntity.isUserNotOnline", query = "SELECT obj FROM TokenSessionEntity obj WHERE obj.userId = :userId AND obj.expirationDate >= NOW() AND obj.isLoggedIn = true"), //
		@NamedQuery(name = "TokenSessionEntity.getUsersOnlineList", query = "SELECT obj FROM TokenSessionEntity obj WHERE obj.expirationDate >= NOW() AND obj.isLoggedIn = true") //
})
public class TokenSessionEntity {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String securityToken;

	@Column(unique = true)
	private Long userId;

	private Date expirationDate;

	private Long activePersonaId;

	private String relayCryptoTicket;

	private Long activeLobbyId;

	private boolean premium = false;

	private String clientHostIp;
	
	private boolean isLoggedIn;
	
	private Long teamId;
	
	private int searchEventId;
	
	private boolean mapHostedEvent;

	public String getSecurityToken() {
		return securityToken;
	}

	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Long getActivePersonaId() {
		return activePersonaId;
	}

	public void setActivePersonaId(Long activePersonaId) {
		this.activePersonaId = activePersonaId;
	}

	public String getRelayCryptoTicket() {
		return relayCryptoTicket;
	}

	public void setRelayCryptoTicket(String relayCryptoTicket) {
		this.relayCryptoTicket = relayCryptoTicket;
	}

	public Long getActiveLobbyId() {
		return activeLobbyId;
	}

	public void setActiveLobbyId(Long activeLobbyId) {
		this.activeLobbyId = activeLobbyId;
	}

	public boolean isPremium() {
		return premium;
	}

	public void setPremium(boolean premium) {
		this.premium = premium;
	}

	public String getClientHostIp() {
		return clientHostIp;
	}

	public void setClientHostIp(String clientHostIp) {
		this.clientHostIp = clientHostIp;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public boolean getIsLoggedIn() {
		return isLoggedIn;
	}

	public void setIsLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}
	
	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}
	
	public int getSearchEventId() {
		return searchEventId;
	}

	public void setSearchEventId(int searchEventId) {
		this.searchEventId = searchEventId;
	}
	
	public boolean isMapHostedEvent() {
		return mapHostedEvent;
	}

	public void setMapHostedEvent(boolean mapHostedEvent) {
		this.mapHostedEvent = mapHostedEvent;
	}

}
