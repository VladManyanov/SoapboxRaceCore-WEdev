package com.soapboxrace.core.dao;

import java.math.BigInteger;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.soapboxrace.core.dao.util.BaseDAO;
import com.soapboxrace.core.jpa.EventEntity;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.RecordsEntity;
import com.soapboxrace.core.jpa.UserEntity;

@Stateless
public class RecordsDAO extends BaseDAO<RecordsEntity> {

	@PersistenceContext
	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public RecordsEntity findById(Long id) {
		return entityManager.find(RecordsEntity.class, id);
	}

	// Search for the existing player record during event finish
	public RecordsEntity findCurrentRace(EventEntity event, UserEntity user, boolean powerUps, int carClassHash) {
		TypedQuery<RecordsEntity> query = entityManager.createNamedQuery("RecordsEntity.findCurrentRace", RecordsEntity.class);
		query.setParameter("event", event);
		query.setParameter("user", user);
		query.setParameter("powerUps", powerUps);
		query.setParameter("carClassHash", carClassHash);

		List<RecordsEntity> resultList = query.getResultList();
		return !resultList.isEmpty() ? resultList.get(0) : null;
	}
	
	// Take all un-checked records to check for actual car version
	public List<RecordsEntity> checkAllRecords() {
		entityManager.setFlushMode(FlushModeType.COMMIT);
		TypedQuery<RecordsEntity> query = entityManager.createNamedQuery("RecordsEntity.checkAllRecords", RecordsEntity.class);
		query.setMaxResults(50000);

		List<RecordsEntity> resultList = query.getResultList();
		return resultList;
	}
	
	// Un-check all records, so they can be obsolete-checked again
	public void uncheckAllRecords() {
		Query query = entityManager.createNamedQuery("RecordsEntity.uncheckAllRecords");
		query.executeUpdate();
	}

	public RecordsEntity getWRRecord(EventEntity event, boolean powerUps, int carClassHash, Long timeMS) {
		TypedQuery<RecordsEntity> query = entityManager.createNamedQuery("RecordsEntity.calcRecordPlace", RecordsEntity.class);
		query.setParameter("event", event);
		query.setParameter("powerUps", powerUps);
		query.setParameter("carClassHash", carClassHash);
		query.setParameter("timeMS", timeMS);
		query.setMaxResults(1);

		List<RecordsEntity> resultList = query.getResultList();
		return resultList.get(0);
	}
	
	public BigInteger countRecordPlace(int eventId, boolean powerUps, int carClassHash, Long timeMS) {
		Query query = entityManager.createNativeQuery(
			"SELECT Count(*) from records WHERE eventId = "+eventId+" and powerUps = "+powerUps+" and carClassHash = "+carClassHash
					+ "and timeMS < "+timeMS+" and userBan = false and isObsolete = false "
		);
		BigInteger count;
		@SuppressWarnings("unchecked")
		List<BigInteger> List = query.getResultList();
		count = List.get(0);
		count = count.add(BigInteger.valueOf(1));
		return count; // 0 means 1st place
	}
	
	public BigInteger countRecords(int eventId, boolean powerUps, int carClassHash, boolean oldRecords) {
		StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT Count(*) from records WHERE eventId = "+eventId+" and powerUps = "+powerUps+" and carClassHash = "+carClassHash+" and userBan = false ");
		if (!oldRecords) { // If true - display all record types
			sqlQuery.append("AND isObsolete = false "); 
		}
		
		Query query = entityManager.createNativeQuery(sqlQuery.toString());
		@SuppressWarnings("unchecked")
		List<BigInteger> List = query.getResultList();
		if (List.isEmpty())
			return new BigInteger("0");
		else return List.get(0);
	}
	
	public BigInteger countRecordsAll(int eventId, boolean powerUps, boolean oldRecords) {
		StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT Count(*) from records WHERE eventId = "+eventId+" AND powerUps = "+powerUps+" AND userBan = false ");
		if (!oldRecords) { // If true - display all record types
			sqlQuery.append("AND isObsolete = false "); 
		}
		
		Query query = entityManager.createNativeQuery(sqlQuery.toString());
		@SuppressWarnings("unchecked")
		List<BigInteger> List = query.getResultList();
		if (List.isEmpty())
			return new BigInteger("0");
		else return List.get(0);
	}
	
	public BigInteger countRecordsPersona(int eventId, boolean powerUps, Long userId) {
		Query query = entityManager.createNativeQuery(
			"SELECT Count(*) from records WHERE eventId = "+eventId+" and userId ="+userId+" and powerUps = "+powerUps);
		@SuppressWarnings("unchecked")
		List<BigInteger> List = query.getResultList();
		if (List.isEmpty())
			return new BigInteger("0");
		else return List.get(0);
	}
	
	public BigInteger countRecordsByCar(int eventId, boolean powerUps, int carPhysicsHash, boolean oldRecords) {
		StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT Count(*) from records WHERE eventId = "+eventId+" and powerUps = "+powerUps+" and carPhysicsHash = "+carPhysicsHash+" and userBan = false ");
		if (!oldRecords) { // If true - display all record types
			sqlQuery.append("AND isObsolete = false "); 
		}
		
		Query query = entityManager.createNativeQuery(sqlQuery.toString());
		@SuppressWarnings("unchecked")
		List<BigInteger> List = query.getResultList();
		if (List.isEmpty())
			return new BigInteger("0");
		else return List.get(0);
	}
	
	// If some server admin did a manual player unban via DB, and forgot to uncheck the userBan field for him, this player should know about it
	public BigInteger countBannedRecords(Long userId) {
		Query query = entityManager.createNativeQuery(
			"SELECT Count(*) from records WHERE userId = "+userId+" and userBan = true");
		BigInteger count;
		@SuppressWarnings("unchecked")
		List<BigInteger> List = query.getResultList();
		count = List.get(0);
		return count; 
	}
	
	public void changeRecordsNickname(PersonaEntity personaEntity) {
		Query createQuery = entityManager.createQuery("UPDATE RecordsEntity obj SET obj.playerName = :playerName WHERE obj.persona = :persona");
		createQuery.setParameter("persona", personaEntity);
		createQuery.setParameter("playerName", personaEntity.getName());
		createQuery.executeUpdate();
	}
	
	public void banRecords(UserEntity user) {
		Query createQuery = entityManager.createQuery("UPDATE RecordsEntity obj SET obj.userBan = true WHERE obj.user = :user");
		createQuery.setParameter("user", user);
		createQuery.executeUpdate();
	}
	
	// When user has deleted one of his personas
	public void deletePersonaRecords(PersonaEntity personaEntity) {
		Query createQuery = entityManager.createQuery("DELETE RecordsEntity obj WHERE obj.persona = :persona");
		createQuery.setParameter("persona", personaEntity);
		createQuery.executeUpdate();
	}
	
	public void unbanRecords(UserEntity user) {
		Query createQuery = entityManager.createQuery("UPDATE RecordsEntity obj SET obj.userBan = false WHERE obj.user = :user");
		createQuery.setParameter("user", user);
		createQuery.executeUpdate();
	}
	
	/**
	 * Получить список лучших заездов по времени в классе
	 * @param eventid - номер трассы
	 * @param powerups - наличие бонусов (true/false)
	 * @param carclasshash - номер класса машин
	 * @param oldrecords - отображение устаревших рекордов (true/false)
	 * @param page - Номер страницы
	 * @param onPage - Сколько позиций на странице
	 * @author Vadimka, Hypercycle
	 */
	@SuppressWarnings("unchecked")
	public List<RecordsEntity> statsEventClass(EventEntity event, boolean powerups, int carClassHash, boolean oldRecords, int page, int onPage) {
		StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT obj FROM RecordsEntity obj ");
		sqlQuery.append("WHERE obj.event = :event ");
		sqlQuery.append("AND obj.powerUps = :powerUps "); 
		sqlQuery.append("AND obj.carClassHash = :carClassHash "); 
		sqlQuery.append("AND obj.userBan = false "); 
		if (!oldRecords) { // If true - display all record types
			sqlQuery.append("AND obj.isObsolete = false "); 
		}
		sqlQuery.append("ORDER BY obj.timeMS "); 
		
		Query query = entityManager.createQuery(sqlQuery.toString());
		query.setParameter("event", event);
		query.setParameter("powerUps", powerups);
		query.setParameter("carClassHash", carClassHash);
		query.setFirstResult((page-1) * onPage);
		query.setMaxResults(onPage);
		List<RecordsEntity> list = query.getResultList();
		return list;
	}
	
	/**
	 * Получить список лучших заездов по времени
	 * @param eventid - номер трассы
	 * @param powerups - наличие бонусов (true/false)
	 * @param oldrecords - отображение устаревших рекордов (true/false)
	 * @param page - Номер страницы
	 * @param onPage - Сколько позиций на странице
	 * @author Vadimka, Hypercycle
	 */
	@SuppressWarnings("unchecked")
	public List<RecordsEntity> statsEventAll(EventEntity event, boolean powerups, boolean oldRecords, int page, int onPage) {
		StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT obj FROM RecordsEntity obj ");
		sqlQuery.append("WHERE obj.event = :event ");
		sqlQuery.append("AND obj.powerUps = :powerUps "); 
		sqlQuery.append("AND obj.userBan = false "); 
		if (!oldRecords) { // If true - display all record types
			sqlQuery.append("AND obj.isObsolete = false "); 
		}
		sqlQuery.append("ORDER BY obj.timeMS "); 
		
		Query query = entityManager.createQuery(sqlQuery.toString());
		query.setParameter("event", event);
		query.setParameter("powerUps", powerups);
		query.setFirstResult((page-1) * onPage);
		query.setMaxResults(onPage);
		List<RecordsEntity> list = query.getResultList();
		return list;
	}
	
	/**
	 * Получить список лучших заездов во всех вариациях трассы. Фильтрация по имени профиля
	 * @param eventid - номер трассы
	 * @param userId - номер аккаунта игрока
	 * @param page - Номер страницы
	 * @param onPage - Сколько позиций на странице
	 * @author Vadimka, Hypercycle
	 */
	public List<RecordsEntity> statsEventPersona(EventEntity event, boolean powerups, UserEntity userEntity) {
		TypedQuery<RecordsEntity> query = entityManager.createNamedQuery("RecordsEntity.statsEventPersona", RecordsEntity.class);
		query.setParameter("event", event);
		query.setParameter("powerUps", powerups);
		query.setParameter("user", userEntity);
		return query.getResultList();
	}
	
	/**
	 * Получить список лучших заездов во всех вариациях трассы. Фильтрация по модели автомобиля
	 * @param eventid - номер трассы
	 * @param carPhysicsHash - хэш модель автомобиля
	 * @param oldrecords - отображение устаревших рекордов (true/false)
	 * @param page - Номер страницы
	 * @param onPage - Сколько позиций на странице
	 * @author Hypercycle
	 */
	@SuppressWarnings("unchecked")
	public List<RecordsEntity> statsEventCar(EventEntity event, boolean powerups, int carPhysicsHash, boolean oldRecords, int page, int onPage) {
		StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT obj FROM RecordsEntity obj ");
		sqlQuery.append("AND obj.userBan = false ");
		sqlQuery.append("AND obj.powerUps = :powerUps "); 
		sqlQuery.append("AND obj.carPhysicsHash = :carPhysicsHash "); 
		if (!oldRecords) { // If true - display all record types
			sqlQuery.append("AND obj.isObsolete = false "); 
		}
		sqlQuery.append("ORDER BY obj.timeMS "); 
		
		Query query = entityManager.createQuery(sqlQuery.toString());
		query.setParameter("event", event);
		query.setParameter("powerUps", powerups);
		query.setParameter("carPhysicsHash", carPhysicsHash);
		query.setFirstResult((page-1) * onPage);
		query.setMaxResults(onPage);
		List<RecordsEntity> list = query.getResultList();
		return list;
	}
}
