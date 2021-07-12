package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.jpa.TeamsRegionsEntity;
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
	
	public List<TeamsTokensEntity> findAllByTeam(TeamsEntity teamsEntity) {
		TypedQuery<TeamsTokensEntity> query = entityManager.createNamedQuery("TeamsTokensEntity.findAllByTeam", TeamsTokensEntity.class);
		query.setParameter("teamOwner", teamsEntity);
		return query.getResultList().isEmpty() != true ? query.getResultList() : null;
	}
	
	// Token-specified requests
	public TeamsTokensEntity lookForIncomeToken(TeamsEntity teamsEntity) {
		TypedQuery<TeamsTokensEntity> query = entityManager.createNamedQuery("TeamsTokensEntity.lookForIncomeToken", TeamsTokensEntity.class);
		query.setParameter("teamOwner", teamsEntity);
		return query.getResultList().isEmpty() != true ? query.getSingleResult() : null;
	}
	
	public TeamsTokensEntity lookForTHKeeperToken(TeamsEntity teamsEntity) {
		TypedQuery<TeamsTokensEntity> query = entityManager.createNamedQuery("TeamsTokensEntity.lookForTHKeeperToken", TeamsTokensEntity.class);
		query.setParameter("teamOwner", teamsEntity);
		return query.getResultList().isEmpty() != true ? query.getSingleResult() : null;
	}
	
	public TeamsTokensEntity lookForNeutralZoneToken(Long regionId) {
		TypedQuery<TeamsTokensEntity> query = entityManager.createNamedQuery("TeamsTokensEntity.lookForNeutralZoneToken", TeamsTokensEntity.class);
		query.setParameter("tokenValue2", regionId.intValue());
		return query.getResultList().isEmpty() != true ? query.getSingleResult() : null;
	}
}
