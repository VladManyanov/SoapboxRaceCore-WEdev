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
import com.soapboxrace.core.bo.util.CarClassType;
import com.soapboxrace.core.bo.util.EventModeType;
import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.LobbyEntity;

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
	// Note: that createQuery action takes 7 ms just to fetch
	public List<LobbyEntity> findAllMPLobbies(int carClassHash, int raceFilter, int searchStage, boolean isSClassFilterActive) {
		System.out.println("### findAllMPLobbies, searchStage: " + searchStage);
		Date dateNow = new Date();
		Date datePast = new Date(dateNow.getTime() - (parameterBO.getIntParam("LOBBY_TIME") - 8000)); // Don't count the last 8 seconds of lobby life-time
		
		TypedQuery<LobbyEntity> query = entityManager.createQuery(getSqlLobbySearch(raceFilter, searchStage, carClassHash, isSClassFilterActive), LobbyEntity.class);
		System.out.println("### findAllMPLobbies, query prepare end");
		query.setParameter("dateTime1", datePast);
		query.setParameter("dateTime2", dateNow);
		if (searchStage == 1) {
			query.setParameter("carClassHash", carClassHash); // carClassHash will be requested only when finding class-restricted races
		}
		System.out.println("### findAllMPLobbies, prepare");
		
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
		System.out.println("### findAllMPLobbies, getSqlLobbySearch");
		searchQuery.append("SELECT obj FROM LobbyEntity obj "); // SELECT command
		searchQuery.append("WHERE obj.started = false AND (obj.lobbyDateTimeStart between :dateTime1 and :dateTime2) OR (obj.lobbyDateTimeStart = null)"); // Lobby should be available to search
		searchQuery.append("AND obj.isPrivate = false AND obj.event.searchAvailable = true "); // Not private, and the event itself should be allowed for search
		searchQuery.append(getSqlClassFilter(searchStage, carClassHash, isSClassFilterActive)); // Class restriction
		searchQuery.append(getSqlRaceFilter(raceFilter, carClassHash)); // Event type action
		searchQuery.append("ORDER BY obj.lobbyDateTimeStart ASC "); // Order lobbies by start time
		System.out.println("### findAllMPLobbies, getSqlLobbySearch End");
		return searchQuery.toString();
	}
	
	public String getSqlClassFilter(int searchStage, int carClassHash, boolean isSClassFilterActive) {
		String append = "";
		int SClassInt = CarClassType.S_CLASS.getId();
		if (carClassHash != CarClassType.MISC.getId()) { // If player wants to drive Drift-Spec or Traffic car, he just should get open events
			switch (searchStage) {
			case 1:
				append = "AND obj.event.carClassHash = :carClassHash ";
				break;
			case 2: 
				switch (CarClassType.valueOf(carClassHash)) {
				case S_CLASS: // S Class group
					append = "AND obj.event.carClassHash = " + SClassInt + " ";
					break;
				case A_CLASS:
				case B_CLASS: // A-B Classes group
					append = "AND (obj.event.carClassHash = " + CarClassType.A_CLASS.getId() + ") OR (obj.event.carClassHash = " + CarClassType.B_CLASS.getId() + ") ";
					break;
				case C_CLASS:
				case D_CLASS:
				case E_CLASS: // C-D-E Classes group
					append = "AND (obj.event.carClassHash = " + CarClassType.C_CLASS.getId() + ") OR (obj.event.carClassHash = " + CarClassType.D_CLASS.getId() +
					") OR (obj.event.carClassHash = " + CarClassType.E_CLASS.getId() + ") ";
					break;
				default:
					break;
				}
				break;
			case 3:
				append = "AND obj.event.carClassHash = " + CarClassType.OPEN_CLASS.getId() + " ";
				if (carClassHash == SClassInt && isSClassFilterActive) { // Search for races, which is hosted by S-Class drivers
					append = "AND obj.carClassHash = " + SClassInt + " ";
				}
				break;
			}
		}
		else {
			append = "AND obj.event.carClassHash = " + CarClassType.OPEN_CLASS.getId() + " ";
		}
		System.out.println("### findAllMPLobbies, getSqlClassFilter");
		return append;
	}
	
	public String getSqlRaceFilter(int raceFilter, int carClassHash) {
		String append = "";
		if (carClassHash != CarClassType.MISC.getId()) { // If player wants to drive Drift-Spec or Traffic car, he just should avoid Drag events
			switch (raceFilter) {
			case 1: // Sprint & Circuit
				append = "AND (obj.event.eventModeId = " + EventModeType.SPRINT.getId() + " or obj.event.eventModeId = " + EventModeType.CIRCUIT.getId() + ") ";
				break;
			case 2: // Drag
				append = "AND obj.event.eventModeId = " + EventModeType.DRAG.getId() + " ";
				break;
			case 3: // All Races
				append = "AND (obj.event.eventModeId = " + EventModeType.SPRINT.getId() + " or obj.event.eventModeId = " + EventModeType.CIRCUIT.getId() + 
				" or obj.event.eventModeId = " + EventModeType.DRAG.getId() + ") ";
				break;
			case 4: // Team Escape
				append = "AND (obj.event.eventModeId = " + EventModeType.TEAM_ESCAPE.getId() + " or obj.event.eventModeId = " 
				+ EventModeType.INTERCEPTOR.getId() + ") ";
				break;
			default: // No filter
				append = "";
				break;
			}
		}
		else {
			append = "AND obj.event.eventModeId <> " + EventModeType.DRAG.getId() + " ";
		}
		System.out.println("### findAllMPLobbies, getSqlRaceFilter");
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
