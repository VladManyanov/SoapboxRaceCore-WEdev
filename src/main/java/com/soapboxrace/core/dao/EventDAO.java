package com.soapboxrace.core.dao;

import java.math.BigInteger;
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

@Stateless
public class EventDAO extends BaseDAO<EventEntity> {

	@EJB
	private ParameterBO parameterBO;
	
	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EventEntity findById(int id) {
		return entityManager.find(EventEntity.class, id);
	}
	
	public EventEntity findByIdDetached(int id) {
		EventEntity event = entityManager.find(EventEntity.class, id);
		entityManager.detach(event);
		return event;
	}

	public List<EventEntity> findAllStats() {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findAllStats", EventEntity.class);
		return query.getResultList();
	}
	/**
	 * Все включенные эвенты
	 * @author Vadimka
	 */
	public List<EventEntity> findAllEnabledStats() {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findAllEnabledStats", EventEntity.class);
		query.setParameter("rotation", parameterBO.getIntParam("ROTATIONID"));
		return query.getResultList();
	}
	/**
	 * Все включенные эвенты (вкл. погони)
	 * @author Vadimka
	 */
	public List<EventEntity> findAllSearchEnabled(int carClassHash) {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findAllSearchEnabled", EventEntity.class);
		query.setParameter("rotation", parameterBO.getIntParam("ROTATIONID"));
		query.setParameter("carClassHash", carClassHash);
		return query.getResultList();
	}
	public List<EventEntity> findRacesSearchEnabled(int carClassHash) {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findRacesSearchEnabled", EventEntity.class);
		query.setParameter("rotation", parameterBO.getIntParam("ROTATIONID"));
		query.setParameter("carClassHash", carClassHash);
		return query.getResultList();
	}
	public List<EventEntity> findAllRacesSearchEnabled(int carClassHash) {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findAllRacesSearchEnabled", EventEntity.class);
		query.setParameter("rotation", parameterBO.getIntParam("ROTATIONID"));
		query.setParameter("carClassHash", carClassHash);
		return query.getResultList();
	}
	public List<EventEntity> findTESearchEnabled(int carClassHash) {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findTESearchEnabled", EventEntity.class);
		query.setParameter("rotation", parameterBO.getIntParam("ROTATIONID"));
		query.setParameter("carClassHash", carClassHash);
		return query.getResultList();
	}
	public List<EventEntity> findDragSearchEnabled(int carClassHash) {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findDragSearchEnabled", EventEntity.class);
		query.setParameter("rotation", parameterBO.getIntParam("ROTATIONID"));
		query.setParameter("carClassHash", carClassHash);
		return query.getResultList();
	}
	/**
	 * Количество всех эвентов
	 * @author Vadimka
	 */
	// Deprecated
	public BigInteger countAll(boolean all, int rotation) {
        String sqlQ = "SELECT Count(*) cout FROM event WHERE isenabled = true AND rotation <> 999 AND statsVisible = true";
        if (!all)
            sqlQ += " AND rotation = "+rotation;
        Query query = entityManager.createNativeQuery(sqlQ);
        query.setMaxResults(1);
        @SuppressWarnings("unchecked")
        List<BigInteger> list = query.getResultList();
        if (list.isEmpty()) 
            return new BigInteger("0");
        else
            return list.get(0);
    }
	
	public BigInteger countBestTime(int eventid) {
        Query query = entityManager.createNativeQuery("SELECT Count(*) cout FROM event_data d, customcar cc, persona p "
                + "WHERE d.carid = cc.id AND p.id = d.personaid AND d.eventid = "+eventid);
        query.setMaxResults(1);
        @SuppressWarnings("unchecked")
        List<BigInteger> list = query.getResultList();
        if (list.isEmpty())
            return new BigInteger("0");
        else return list.get(0);
    }
	
	public List<EventEntity> findByLevel(int level) {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findByLevel", EventEntity.class);
		query.setParameter("level", level);
		return query.getResultList();
	}
	
	public List<EventEntity> findByRotation(int level) {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findByRotation", EventEntity.class);
		query.setParameter("level", level);
		query.setParameter("rotation", parameterBO.getIntParam("ROTATIONID"));
		return query.getResultList();
	}
	
	public List<EventEntity> findByRotationBase(int level) {
		TypedQuery<EventEntity> query = entityManager.createNamedQuery("EventEntity.findByRotationBase", EventEntity.class);
		query.setParameter("level", level);
		query.setParameter("rotation", parameterBO.getIntParam("ROTATIONID"));
		return query.getResultList();
	}

}
