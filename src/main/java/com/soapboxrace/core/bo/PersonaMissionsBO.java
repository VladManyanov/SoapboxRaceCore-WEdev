package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.dao.PersonaMissionsDAO;
import com.soapboxrace.core.jpa.PersonaEntity;
import com.soapboxrace.core.jpa.PersonaMissionsEntity;
import com.soapboxrace.jaxb.http.TeamEscapeArbitrationPacket;

@Stateless
public class PersonaMissionsBO {

	@EJB
	private PersonaDAO personaDao;

	@EJB
	private PersonaMissionsDAO personaMissionsDAO;
	
	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private AchievementsBO achievementsBO;

	public PersonaMissionsEntity getPersonaMissions(Long personaId) {
		PersonaMissionsEntity personaMissionsEntity = personaMissionsDAO.findById(personaId);
		if (personaMissionsEntity == null) {
			personaMissionsEntity = createPersonaMissionsEntry(personaId);
		}
		return personaMissionsEntity;
	}
	
	public PersonaMissionsEntity createPersonaMissionsEntry(Long personaId) {
		PersonaMissionsEntity personaMissionsEntity = new PersonaMissionsEntity();
		personaMissionsEntity.setPersonaId(personaId);
		personaMissionsEntity.setCEventGoalProgress(0);
		personaMissionsEntity.setPursuitGoal(null);
		personaMissionsEntity.setPursuitGoalProgress(0);
		personaMissionsDAO.insert(personaMissionsEntity);
		return personaMissionsEntity;
	}
	
	public void teamEscapeCEvent(int playersAmount, TeamEscapeArbitrationPacket teamEscapeArbitrationPacket, PersonaEntity personaEntity,
			boolean arbitration) {
		int cEventType = parameterBO.getIntParam("CEVENT_ACHIEVEMENTID");
		if (cEventType != 0) {
			PersonaMissionsEntity personaMissionsEntity = getPersonaMissions(personaEntity.getPersonaId());
			if (playersAmount > 1 && teamEscapeArbitrationPacket.getFinishReason() == 22 && arbitration) {
				personaMissionsEntity.setCEventGoalProgress(personaMissionsEntity.getCEventGoalProgress() + teamEscapeArbitrationPacket.getCopsDisabled());
				personaMissionsDAO.update(personaMissionsEntity);
				achievementsBO.applyCEventAchievement(personaEntity, personaMissionsEntity, cEventType);
			}
		}
	}
}
