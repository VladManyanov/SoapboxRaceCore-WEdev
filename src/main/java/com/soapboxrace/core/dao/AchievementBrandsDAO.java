package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.AchievementBrandsEntity;

@Stateless
public class AchievementBrandsDAO extends BaseDAO<AchievementBrandsEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public AchievementBrandsEntity findById(Long personaId) {
		return entityManager.find(AchievementBrandsEntity.class, personaId);
	}
	
	public AchievementBrandsEntity findByPersona(Long personaId) {
		TypedQuery<AchievementBrandsEntity> query = entityManager.createNamedQuery("AchievementBrandsEntity.findByPersonaId", AchievementBrandsEntity.class);
		query.setParameter("personaId", personaId);
		List<AchievementBrandsEntity> resultList = query.getResultList();
		if (resultList == null || resultList.isEmpty()) {
			AchievementBrandsEntity achievementBrandsEntity = new AchievementBrandsEntity();
			achievementBrandsEntity.setPersonaId(personaId);
			insert(achievementBrandsEntity);
			return achievementBrandsEntity;
		}
		return resultList.get(0);
	}

}
