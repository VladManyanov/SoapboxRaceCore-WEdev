package com.soapboxrace.core.dao;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.MostPopularEventEntity;

@Stateless
public class EventDataDAO extends BaseDAO<EventDataEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EventDataEntity findById(Long id) {
		return entityManager.find(EventDataEntity.class, id);
	}
	
	public List<EventDataEntity> findByPersona(Long personaId) {
		TypedQuery<EventDataEntity> query = entityManager.createNamedQuery("EventDataEntity.findByPersona", EventDataEntity.class);
		query.setParameter("personaId", personaId);
		return query.getResultList();
	}
	
	public List<EventDataEntity> findByPersonaAndRaceType(Long personaId, Integer type) {
		TypedQuery<EventDataEntity> query = entityManager.createNamedQuery("EventDataEntity.findByPersonaAndType", EventDataEntity.class);
		query.setParameter("personaId", personaId);
		query.setParameter("eventModeID", type);
		return query.getResultList();
	}
	
	public List<EventDataEntity> getRacers(Long eventSessionId) {
		TypedQuery<EventDataEntity> query = entityManager.createNamedQuery("EventDataEntity.getRacers", EventDataEntity.class);
		query.setParameter("eventSessionId", eventSessionId);
		return query.getResultList();
	}
	
	public List<EventDataEntity> getRacersRanked(Long eventSessionId) {
		TypedQuery<EventDataEntity> query = entityManager.createNamedQuery("EventDataEntity.getRacersRanked", EventDataEntity.class);
		query.setParameter("eventSessionId", eventSessionId);
		return query.getResultList();
	}
	
	public BigInteger countTEFinishers(Long eventSessionId) {
		Query query = entityManager.createNativeQuery("SELECT COUNT(*) " + 
				"FROM event_data " + 
				"WHERE eventSessionId = "+eventSessionId+" AND finishReason = 22 AND rank <> 0");
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<BigInteger> list = query.getResultList();
		BigInteger count = new BigInteger("0");
		if (!list.isEmpty()) count = list.get(0);
		return count;
	}
		
	public EventDataEntity findByPersonaAndEventSessionId(Long personaId, Long eventSessionId) {
		TypedQuery<EventDataEntity> query = entityManager.createNamedQuery("EventDataEntity.findByPersonaAndEventSessionId", EventDataEntity.class);
		query.setParameter("personaId", personaId);
		query.setParameter("eventSessionId", eventSessionId);
		
		List<EventDataEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}
	
	/**
	 * Количество трасс, на которых профиль подозрвался в читерстве
	 * @param personaID - Идентификатор профиля
	 */
	public BigInteger countHackRacesByPersona(Long personaID) {
		Query query = entityManager.createNativeQuery("SELECT COUNT(*) " + 
				"FROM report " + 
				"WHERE abuserpersonaid = "+personaID+" AND (hacksDetected = 8 OR hacksDetected = 40)");
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<BigInteger> list = query.getResultList();
		BigInteger count = new BigInteger("0");
		if (!list.isEmpty()) count = list.get(0);
		return count;
	}
	/**
	 * Самые популярные заезды по 
	 * @param onPage - Из скольки профилей будет состоять топ
	 * @author Vadimka
	 */
	public List<MostPopularEventEntity> findTopRaces() {
		List<MostPopularEventEntity> races = new ArrayList<MostPopularEventEntity>();
		List<MostPopularEventEntity> race;
		TypedQuery<MostPopularEventEntity> query = entityManager.createNamedQuery("MostPopularEventEntity.mostPopular", MostPopularEventEntity.class);
		// Круг
		query.setParameter("mode", 4);
		query.setParameter("count", 1);
		race = query.getResultList();
		if (race != null && !race.isEmpty())
			races.add(query.getResultList().get(0));
		// Спринт
		query.setParameter("mode", 9);
		query.setParameter("count", 1);
		race = query.getResultList();
		if (race != null && !race.isEmpty())
			races.add(query.getResultList().get(0));
		// Драг
		query.setParameter("mode", 19);
		query.setParameter("count", 1);
		race = query.getResultList();
		if (race != null && !race.isEmpty())
			races.add(query.getResultList().get(0));
		// Погоня
		query.setParameter("mode", 12);
		query.setParameter("count", 1);
		race = query.getResultList();
		if (race != null && !race.isEmpty())
			races.add(query.getResultList().get(0));
		// Спасение командой
		query.setParameter("mode", 24);
		query.setParameter("count", 1);
		race = query.getResultList();
		if (race != null && !race.isEmpty())
			races.add(query.getResultList().get(0));
		return races;
	}
	/**
	 * Возвращает запрос
	 * Фильтрация по нику пользователя
	 * @return
	 */
	public BigInteger countBestTimeByPersona(int eventid, String personaName) {
		BigInteger count = new BigInteger("0");
		Query query = entityManager.createNativeQuery("SELECT Count(*) cout FROM event_data d, carslot cs, customcar cc, persona p "
				+ "WHERE d.carid = cs.id AND d.carid = cc.id AND p.id = cs.personaid AND d.eventid = "+eventid+" AND p.name LIKE '"+personaName+"'");
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<BigInteger> list = query.getResultList();
		for (BigInteger objects : list) {
			count = objects;
		}
		return count;
	}
	
	public BigInteger countCEventTrackResults(int eventid, LocalDateTime startTime, int eventModeId) {
		BigInteger count = new BigInteger("0");
		Query query = entityManager.createNativeQuery("SELECT Count(*) FROM event_data WHERE issingle = false AND eventmodeid = "+eventModeId
				+ " AND finishreason = 22 AND arbitration = true AND date > '"+startTime+"' AND eventid = "+eventid);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<BigInteger> list = query.getResultList();
		if (!list.isEmpty()) count = list.get(0);
		return count;
	}

}
