package com.soapboxrace.core.dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.TeamsHistoryEntity;

@Stateless
public class TeamsHistoryDAO extends BaseDAO<TeamsHistoryEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public TeamsHistoryEntity findById(Long id) {
		return entityManager.find(TeamsHistoryEntity.class, id);
	}

	public Long getTopTeamIdFromPreviousSeason(int currentSeason) {
		currentSeason--;
		TypedQuery<TeamsHistoryEntity> query = entityManager.createNamedQuery("TeamsHistoryEntity.getTopTeamIdFromPreviousSeason", TeamsHistoryEntity.class);
		query.setParameter("season", currentSeason);
		if (currentSeason < 1 || query.getResultList().isEmpty()) {
			return 0L;
		}
		return query.getResultList().get(0).getTeamId();
	}
	
}
