package com.soapboxrace.core.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.ProductEntity;
import com.soapboxrace.core.jpa.RewardDropEntity;

@Stateless
public class RewardDropDAO extends BaseDAO<RewardDropEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public RewardDropEntity findById(Long id) {
		return entityManager.find(RewardDropEntity.class, id);
	}

	public List<RewardDropEntity> getRewardDrops(Long dropGroupId, short numberOfRewards, boolean isCardPack) {
		List<RewardDropEntity> rewardDropList = new ArrayList<>();
		StringBuilder sqlWhere = new StringBuilder();
		sqlWhere.append(" WHERE obj.dropGroupId = :dropGroupId ");

		StringBuilder sqlCount = new StringBuilder();
		sqlCount.append("SELECT COUNT(*) FROM RewardDropEntity obj ");
		sqlCount.append(sqlWhere.toString());

		Query countQuery = entityManager.createQuery(sqlCount.toString());
		countQuery.setParameter("dropGroupId", dropGroupId);
		Long count = (Long) countQuery.getSingleResult();

		StringBuilder sqlProduct = new StringBuilder();
		sqlProduct.append("SELECT obj FROM RewardDropEntity obj");
		sqlProduct.append(sqlWhere.toString());

		TypedQuery<RewardDropEntity> rewardDropQuery = entityManager.createQuery(sqlProduct.toString(), RewardDropEntity.class);
		rewardDropQuery.setParameter("dropGroupId", dropGroupId);

		int number = count.intValue();
		Random random = new Random();
		rewardDropQuery.setMaxResults(1);
		if (isCardPack) { // Card Pack gives you a random set of items
			int max = Math.max(1, numberOfRewards);
			for (int i = 0; i < max; i++) {
				number = random.nextInt(count.intValue());
				rewardDropQuery.setFirstResult(number);
				rewardDropList.add(rewardDropQuery.getSingleResult());
			}
		} else { // Give the defined items
			int max = Math.max(1, numberOfRewards);
			for (int i = 0; i < max; i++) {
				rewardDropQuery.setFirstResult(i);
				rewardDropList.add(rewardDropQuery.getSingleResult());
			}
		}
		return rewardDropList;
	}
	
	public List<ProductEntity> getBundleDrops(String productType) {
		// 5-Stars Performance Parts, 4-Stars Skillmods and the Percentage Performance Parts
		if (productType.contentEquals("POWERPACK")) {
			Query countQuery = entityManager.createQuery("SELECT COUNT(*) FROM ProductEntity obj WHERE obj.isDropableMode = 2 OR (obj.stars = 5 AND obj.longDescription LIKE '%tuned%')");
			Long count = (Long) countQuery.getSingleResult();
			List<ProductEntity> productList = new ArrayList<>();
			TypedQuery<ProductEntity> productQuery = entityManager.createQuery("SELECT obj FROM ProductEntity obj WHERE obj.isDropableMode = 2 OR (obj.stars = 5 AND obj.longDescription LIKE '%tuned%')", ProductEntity.class);
			
			int number = count.intValue();
			Random random = new Random();
			productQuery.setMaxResults(1);
			int cardAmount = 5;
			
			for (int i = 0; i < cardAmount; i++) {
				number = random.nextInt(count.intValue());
				productQuery.setFirstResult(number);
				ProductEntity productEntity = productQuery.getSingleResult();
				productList.add(productEntity);
			}
			return productList;
		}
		else {
			Query countQuery = entityManager.createQuery("SELECT COUNT(*) FROM ProductEntity obj WHERE obj.productType = :productType AND obj.isDropableMode <> 0");
			countQuery.setParameter("productType", productType);
			Long count = (Long) countQuery.getSingleResult();
			
			List<ProductEntity> productList = new ArrayList<>();
			TypedQuery<ProductEntity> productQuery = entityManager.createQuery("SELECT obj FROM ProductEntity obj WHERE obj.productType = :productType AND obj.isDropableMode <> 0", ProductEntity.class);
			productQuery.setParameter("productType", productType);
			
			int number = count.intValue();
			Random random = new Random();
			productQuery.setMaxResults(1);
			
			boolean isPackReduced = random.nextBoolean();
			int cardAmount = (!isPackReduced ? 5 : 3);
			
			int max = Math.max(1, cardAmount);
			for (int i = 0; i < max; i++) {
				number = random.nextInt(count.intValue());
				productQuery.setFirstResult(number);
				productList.add(productQuery.getSingleResult());
			}
			return productList;
		}
	}
}
