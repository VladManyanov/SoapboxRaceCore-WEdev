package com.soapboxrace.core.dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.TeamsTokensEntity;

@Stateless
public class TeamsTokensDAO extends BaseDAO<TeamsTokensEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public TeamsTokensEntity findById(Long id) {
		return entityManager.find(TeamsTokensEntity.class, id);
	}
	
	public TeamsTokensEntity findByAffectedEventId(int eventId) {
		TypedQuery<TeamsTokensEntity> query = entityManager.createNamedQuery("TeamsTokensEntity.findByAffectedEventId", TeamsTokensEntity.class);
		query.setParameter("tokenValue2", eventId);
		return query.getResultList().isEmpty() != true ? query.getSingleResult() : null;
	}
}
