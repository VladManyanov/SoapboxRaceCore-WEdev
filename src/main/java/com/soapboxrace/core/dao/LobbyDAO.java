package com.soapboxrace.core.dao;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.LobbyEntity;
import com.soapboxrace.core.jpa.RewardDropEntity;

@Stateless
public class LobbyDAO extends BaseDAO<LobbyEntity> {

	@EJB
	private ParameterBO parameterBO;
	
	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public LobbyEntity findById(Long id) {
		LobbyEntity lobbyEntity = entityManager.find(LobbyEntity.class, id);
		return lobbyEntity;
	}

	// Search for Race Now, takes 3 tries, depending on car class and lobbies variety
	public List<LobbyEntity> findAllMPLobbies(int carClassHash, int raceFilter, int searchStage, boolean isSClassFilterActive) {
		System.out.println("### findAllMPLobbies, searchStage: " + searchStage);
		Date dateNow = new Date();
		Date datePast = new Date(dateNow.getTime() - (parameterBO.getIntParam("LOBBY_TIME") - 8000)); // Don't count the last 8 seconds of lobby life-time
		
		TypedQuery<LobbyEntity> query = entityManager.createQuery(getSqlLobbySearch(raceFilter, searchStage, carClassHash, isSClassFilterActive), LobbyEntity.class);
		query.setParameter("dateTime1", datePast);
		query.setParameter("dateTime2", dateNow);
		if (searchStage == 1) {
			query.setParameter("carClassHash", carClassHash); // carClassHash will be requested only when finding class-restricted races
		}
		
		if (query.getResultList().isEmpty() && searchStage == 1) {
			System.out.println("### going to searchStage 2");
			findAllMPLobbies(carClassHash, raceFilter, 2, isSClassFilterActive); 
			// 1 to 2 - Repeat the search without strict class restriction, to class groups priority
			// 2 to 3 - Wait for priority timeout, and repeat the search for all existing lobbies
			// 3 - No lobbies at all, wait for new lobbies on Queue MM
		}
		System.out.println("### lobbyDAO finished, searchStage " + searchStage);
		return query.getResultList();
	}
	
	public String getSqlLobbySearch(int raceFilter, int searchStage, int carClassHash, boolean isSClassFilterActive) {
		StringBuilder searchQuery = new StringBuilder();
		searchQuery.append("SELECT obj FROM LobbyEntity obj "); // SELECT command
		searchQuery.append("WHERE obj.started = false AND (obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.lobbyDateTimeStart = null)"); // Lobby should be available to search
		searchQuery.append("AND obj.isPrivate = false AND obj.event.searchAvailable = true "); // Not private, and the event itself should be allowed for search
		searchQuery.append(getSqlClassFilter(searchStage, carClassHash, isSClassFilterActive)); // Class restriction
		searchQuery.append(getSqlRaceFilter(raceFilter)); // Event type action
		searchQuery.append("ORDER BY obj.lobbyDateTimeStart ASC "); // Order lobbies by start time
		return searchQuery.toString();
	}
	
	public String getSqlClassFilter(int searchStage, int carClassHash, boolean isSClassFilterActive) {
		String append = "";
		switch (searchStage) {
		case 1:
			append = "AND obj.event.carClassHash = :carClassHash ";
			break;
		case 2: // TODO This one could be re-worked
			switch (carClassHash) {
			case -2142411446: // S Class group
				append = "AND obj.event.carClassHash = -2142411446 ";
				break;
			case -405837480:
			case -406473455: // A-B Classes group
				append = "AND (obj.event.carClassHash = -405837480) OR (obj.event.carClassHash = -406473455) ";
				break;
			case 1866825865:
			case 415909161:
			case 872416321: // C-D-E Classes group
				append = "AND (obj.event.carClassHash = 1866825865) OR (obj.event.carClassHash = 415909161) OR (obj.event.carClassHash = 872416321) ";
				break;
			}
			break;
		case 3:
			append = "AND obj.event.carClassHash = 607077938 ";
			if (carClassHash == -2142411446 && isSClassFilterActive) { // Search for races, which is hosted by S-Class drivers
				append = "AND obj.carClassHash = -2142411446 ";
			}
			break;
		}
		return append;
	}
	
	public String getSqlRaceFilter(int raceFilter) {
		String append = "";
		switch (raceFilter) {
		case 1: // Sprint & Circuit
			append = "AND (obj.event.eventModeId = 4 or obj.event.eventModeId = 9) ";
			break;
		case 2: // Drag
			append = "AND obj.event.eventModeId = 19 ";
			break;
		case 3: // All Races
			append = "AND (obj.event.eventModeId = 4 or obj.event.eventModeId = 9 or obj.event.eventModeId = 19) ";
			break;
		case 4: // Team Escape
			append = "AND (obj.event.eventModeId = 24 or obj.event.eventModeId = 100) ";
			break;
		default: // No filter
			append = "";
			break;
		}
		return append;
	}
	
	// Matchmaking information lobbies search
	public List<LobbyEntity> findAllOpen() {
		Date dateNow = new Date();
		Date datePast = new Date(dateNow.getTime() - (parameterBO.getIntParam("LOBBY_TIME") - 8000)); // Don't count the last 8 seconds of lobby life-time

		TypedQuery<LobbyEntity> query = entityManager.createNamedQuery("LobbyEntity.findAllOpen", LobbyEntity.class);
		query.setParameter("dateTime1", datePast);
		query.setParameter("dateTime2", dateNow);
		return query.getResultList();
	}

	// Search for event selected on World Map
	public List<LobbyEntity> findByEventStarted(int eventId) {
		Date dateNow = new Date();
		Date datePast = new Date(dateNow.getTime() - (parameterBO.getIntParam("LOBBY_TIME") - 8000)); // Don't count the last 8 seconds of lobby life-time
		EventEntity eventEntity = new EventEntity();
		eventEntity.setId(eventId);

		TypedQuery<LobbyEntity> query = entityManager.createNamedQuery("LobbyEntity.findByEventStarted", LobbyEntity.class);
		query.setParameter("event", eventEntity);
		query.setParameter("dateTime1", datePast);
		query.setParameter("dateTime2", dateNow);
		return query.getResultList();
	}

	// Search for private mode
	public LobbyEntity findByEventAndPersona(int eventId, Long personaId) {
		Date dateNow = new Date();
		Date datePast = new Date(dateNow.getTime() - (parameterBO.getIntParam("LOBBY_TIME") - 8000)); // Don't count the last 8 seconds of lobby life-time
		EventEntity eventEntity = new EventEntity();
		eventEntity.setId(eventId);

		TypedQuery<LobbyEntity> query = entityManager.createNamedQuery("LobbyEntity.findByEventAndPersona", LobbyEntity.class);
		query.setParameter("event", eventEntity);
		query.setParameter("dateTime1", datePast);
		query.setParameter("dateTime2", dateNow);
		query.setParameter("personaId", personaId);

		List<LobbyEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}
	
	// Search for specific player lobbies
	public LobbyEntity findByHosterPersona(Long personaId) {
		TypedQuery<LobbyEntity> query = entityManager.createNamedQuery("LobbyEntity.findByHosterPersona", LobbyEntity.class);
		query.setParameter("personaId", personaId);

		List<LobbyEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}
	
	public boolean isThisLobbyReserved(Long id) {
		TypedQuery<LobbyEntity> query = entityManager.createNamedQuery("LobbyEntity.isThisLobbyReserved", LobbyEntity.class);
		query.setParameter("id", id);

		List<LobbyEntity> resultList = query.getResultList();
		return (resultList != null && !resultList.isEmpty() ) ? false : true;
	}
	
	public void deleteAll() {
		Query query = entityManager.createNamedQuery("LobbyEntity.deleteAll");
		query.executeUpdate();
	}
}
