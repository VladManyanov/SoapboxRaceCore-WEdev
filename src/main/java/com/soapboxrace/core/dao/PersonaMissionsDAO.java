package com.soapboxrace.core.dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.PersonaMissionsEntity;

@Stateless
public class PersonaMissionsDAO extends BaseDAO<PersonaMissionsEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public PersonaMissionsEntity findById(Long personaId) {
		return entityManager.find(PersonaMissionsEntity.class, personaId);
	}
	
}
