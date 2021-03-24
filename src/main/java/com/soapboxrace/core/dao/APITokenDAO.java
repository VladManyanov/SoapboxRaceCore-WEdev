package com.soapboxrace.core.dao;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.APITokenEntity;

@Stateless
public class APITokenDAO extends BaseDAO<APITokenEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public boolean verifyToken(String token, URI uri) {
		TypedQuery<APITokenEntity> query = entityManager.createNamedQuery("APITokenEntity.findByToken", APITokenEntity.class);
		query.setParameter("token", token);

		String ipAddress = uri.getHost();
		List<APITokenEntity> resultList = query.getResultList();
		if (resultList == null || resultList.isEmpty()) {
			System.out.println("### API Token " + token + " does not exist on DB, user IP: " + ipAddress);
			return false;
		}
		APITokenEntity result = resultList.get(0);
		
		if (result.isDisabled()) {
			System.out.println("### API Token ID " + result.getId() + " has been requested, but already disabled, User IP: " + ipAddress + ".");
			return false;
		}
		if (!result.getIPAddress().contentEquals(ipAddress)) {return disableToken(result, 2, ipAddress);}
		if (LocalDateTime.now().isAfter((result.getCreatedTime().plusDays(1)))) {return disableToken(result, 3, ipAddress);}
		return true;
	}
	
	// Reason: 0 = Token does not exist, 1 - Token is disabled, 2 - Creator's IP and receiver IP address does not equal, 3 - Token is expired
	public boolean disableToken(APITokenEntity apiTokenEntity, int reason, String details) {
		apiTokenEntity.setDisabled(true);
		update(apiTokenEntity);
		System.out.println("### API Token ID " + apiTokenEntity.getId() + " has been disabled, reason: " + reason + ", details: " + details + ".");
		return false;
	}

}