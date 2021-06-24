package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.TeamsEntity;
import com.soapboxrace.core.jpa.TeamsMatchesEntity;

@Stateless
public class TeamsMatchesDAO extends BaseDAO<TeamsMatchesEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public TeamsMatchesEntity findById(Long id) {
		return entityManager.find(TeamsMatchesEntity.class, id);
	}
	
	public List<TeamsMatchesEntity> findInvitesByTeam(TeamsEntity teamsEntity) {
		TypedQuery<TeamsMatchesEntity> query = entityManager.createNamedQuery("TeamsMatchesEntity.findInviteByTeam", TeamsMatchesEntity.class);
		query.setParameter("team2", teamsEntity);
		return query.getResultList();
	}
}
