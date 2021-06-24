package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.TeamsRegionsEntity;

@Stateless
public class TeamsRegionsDAO extends BaseDAO<TeamsRegionsEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public TeamsRegionsEntity findById(Long id) {
		return entityManager.find(TeamsRegionsEntity.class, id);
	}
	
	public List<TeamsRegionsEntity> findAllRegions() {
		TypedQuery<TeamsRegionsEntity> query = entityManager.createNamedQuery("TeamsRegionsEntity.findAllRegions", TeamsRegionsEntity.class);
		return query.getResultList();
	}
}
