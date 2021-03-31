package com.soapboxrace.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.LobbyEntity;
import com.soapboxrace.core.jpa.LobbyEntrantEntity;
import com.soapboxrace.core.jpa.PersonaEntity;

@Stateless
public class LobbyEntrantDAO extends BaseDAO<LobbyEntrantEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public LobbyEntrantEntity findById(Long id) {
		return entityManager.find(LobbyEntrantEntity.class, id);
	}

	public void deleteByPersona(PersonaEntity personaEntity) {
		Query query = entityManager.createNamedQuery("LobbyEntrantEntity.deleteByPersona");
		query.setParameter("persona", personaEntity);
		query.executeUpdate();
	}
	
	public void deleteByLobby(LobbyEntity lobbyEntity) {
		Query query = entityManager.createNamedQuery("LobbyEntrantEntity.deleteByLobby");
		query.setParameter("lobby", lobbyEntity);
		query.executeUpdate();
	}
	
	public void deleteAll() {
		Query query = entityManager.createNamedQuery("LobbyEntrantEntity.deleteAll");
		query.executeUpdate();
	}
	
	public boolean isLobbyEmpty(LobbyEntity lobbyEntity) {
		TypedQuery<LobbyEntrantEntity> query = entityManager.createNamedQuery("LobbyEntrantEntity.isLobbyEmpty", LobbyEntrantEntity.class);
		query.setParameter("lobby", lobbyEntity);
		return !query.getResultList().isEmpty() ? false : true;
	}
	
	public int getPlayerCount(LobbyEntity lobbyEntity) { // Use the same query as "isLobbyEmpty"
		TypedQuery<LobbyEntrantEntity> query = entityManager.createNamedQuery("LobbyEntrantEntity.isLobbyEmpty", LobbyEntrantEntity.class);
		query.setParameter("lobby", lobbyEntity);
		List<LobbyEntrantEntity> list = query.getResultList();
		return (list != null && !list.isEmpty() ) ? list.size() : 0;
	}
}
