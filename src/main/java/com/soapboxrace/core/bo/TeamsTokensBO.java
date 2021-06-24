package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.TeamsDAO;
import com.soapboxrace.core.dao.TeamsTokensDAO;
import com.soapboxrace.core.jpa.TeamsTokensEntity;

@Stateless
public class TeamsTokensBO {

	@EJB
	private TeamsDAO teamsDAO;
	
	@EJB
	private TeamsTokensDAO teamsTokensDAO;

	public void saveToken(Long teamId, Long personaId, String tokenType) {
		// Когда игрок получил жетон с карты наград, нужно перейти в этот класс и сохранить жетон в БД
		TeamsTokensEntity teamsTokensEntity = new TeamsTokensEntity();
		teamsTokensEntity.setTeamOwner(teamsDAO.findById(teamId));
		teamsTokensEntity.setTokenType(tokenType);
		teamsTokensEntity.setTokenValue(0); // Сделать
		teamsTokensEntity.setTokenValue2(0); // Сделать
		teamsTokensEntity.setActivated(false);
		teamsTokensDAO.insert(teamsTokensEntity);
		// Оповещение в Дискорде об получении жетона (без указания типа)
	}
	
	public void activateTokenEffects() {
		// Применить эффект жетона (для трассы или подсчёте очков)
	}
	
	public void activateTokenByType(TeamsTokensEntity teamsTokensEntity) {
		// а где
		teamsTokensEntity.setActivated(true);
		teamsTokensDAO.update(teamsTokensEntity);
	}
}
