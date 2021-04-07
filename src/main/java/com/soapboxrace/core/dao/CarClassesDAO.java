package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import javax.persistence.FlushModeType;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.CarClassesEntity;

@Stateless
public class CarClassesDAO extends BaseDAO<CarClassesEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public CarClassesEntity findById(String id) {
		return entityManager.find(CarClassesEntity.class, id);
	}

	public CarClassesEntity findByHash(int hash) {
		entityManager.setFlushMode(FlushModeType.COMMIT);
		TypedQuery<CarClassesEntity> query = entityManager.createNamedQuery("CarClassesEntity.findByHash", CarClassesEntity.class);
		query.setParameter("hash", hash);
		return query.getSingleResult();
	}
	
	public CarClassesEntity findByProductId(String productId) {
		TypedQuery<CarClassesEntity> query = entityManager.createNamedQuery("CarClassesEntity.findByProductId", CarClassesEntity.class);
		query.setParameter("productId", productId);
		return query.getSingleResult();
	}
	
	public CarClassesEntity findByStoreName(String customCarName) {
		TypedQuery<CarClassesEntity> query = entityManager.createQuery("SELECT obj FROM CarClassesEntity obj WHERE obj.storeName = :storeName", CarClassesEntity.class);
		query.setParameter("storeName", customCarName);
		return query.getSingleResult();
	}
	
	public CarClassesEntity findByRecordsCarName(String recordCarName) {
		TypedQuery<CarClassesEntity> query = entityManager.createQuery("SELECT obj FROM CarClassesEntity obj WHERE obj.modelSmall = :modelSmall", CarClassesEntity.class);
		query.setParameter("modelSmall", recordCarName);
		return query.getSingleResult();
	}
	
	public List<CarClassesEntity> findByLootboxType(int lootboxType) {
		TypedQuery<CarClassesEntity> query = entityManager.createQuery("SELECT obj FROM CarClassesEntity obj WHERE obj.lootboxType = :lootboxType", CarClassesEntity.class);
		query.setParameter("lootboxType", lootboxType);
		return query.getResultList();
	}
	
	public String getFullCarName(String carName) {
		Query query = entityManager.createNativeQuery(
			"SELECT full_name from car_classes WHERE store_name = '"+carName+"'");
		@SuppressWarnings("unchecked")
		List<String> fullCarName = query.getResultList();
		if (fullCarName.isEmpty()) {
			return "temp350";
		}
		return fullCarName.get(0); 
	}
}
