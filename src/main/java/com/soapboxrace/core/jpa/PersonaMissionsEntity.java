package com.soapboxrace.core.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PERSONA_MISSIONS")
public class PersonaMissionsEntity {

	@Id
	private Long personaId;
	
	private int cEventGoalProgress;
	private String pursuitGoal;
	private int pursuitGoalProgress;
	
	public Long getPersonaId() {
		return personaId;
	}

	public void setPersonaId(Long personaId) {
		this.personaId = personaId;
	}
	
	public int getCEventGoalProgress() {
		return cEventGoalProgress;
	}

	public void setCEventGoalProgress(int cEventGoalProgress) {
		this.cEventGoalProgress = cEventGoalProgress;
	}
	
	public String getPursuitGoal() {
		return pursuitGoal;
	}

	public void setPursuitGoal(String pursuitGoal) {
		this.pursuitGoal = pursuitGoal;
	}
	
	public int getPursuitGoalProgress() {
		return pursuitGoalProgress;
	}

	public void setPursuitGoalProgress(int pursuitGoalProgress) {
		this.pursuitGoalProgress = pursuitGoalProgress;
	}
	
}
