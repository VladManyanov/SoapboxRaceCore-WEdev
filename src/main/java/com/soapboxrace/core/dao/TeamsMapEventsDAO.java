package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.jpa.TeamsMapEventsEntity;
import com.soapboxrace.core.jpa.TeamsRegionsEntity;

@Stateless
public class TeamsMapEventsDAO extends BaseDAO<TeamsEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public TeamsMapEventsEntity findByEventId(Long id) {
		return entityManager.find(TeamsMapEventsEntity.class, id);
	}
	
	public List<TeamsMapEventsEntity> findByRegion(TeamsRegionsEntity teamsRegionsEntity) {
		TypedQuery<TeamsMapEventsEntity> query = entityManager.createNamedQuery("TeamsMapEventsEntity.findByRegion", TeamsMapEventsEntity.class);
		query.setParameter("region", teamsRegionsEntity);
		return query.getResultList();
	}
}
