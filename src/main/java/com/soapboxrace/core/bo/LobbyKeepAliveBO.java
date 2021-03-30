package com.soapboxrace.core.bo;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import com.soapboxrace.core.dao.LobbyDAO;
import com.soapboxrace.core.jpa.LobbyEntity;
import com.soapboxrace.core.jpa.LobbyEntrantEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppChat;

@Startup
@Singleton
public class LobbyKeepAliveBO {

	@EJB
	private LobbyDAO lobbyDao;

	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private LobbyBO lobbyBO;
	
	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;
	
	@EJB
	private MatchmakingBO matchmakingBO;
	
	@Resource
    private TimerService timerService;

	public void searchPriorityTimer(Long personaId, int carClassHash, int raceFilter, boolean isSClassFilterActive, int priorityTimer) {
		TimerConfig timerConfig = new TimerConfig(null, false); // Must be not-persistent
	    String[] infoArray = new String[4];
	    infoArray[0] = personaId.toString();
	    infoArray[1] = String.valueOf(carClassHash);
	    infoArray[2] = String.valueOf(raceFilter);
	    infoArray[3] = String.valueOf(isSClassFilterActive);
	    
	    timerConfig.setInfo(infoArray);
	    timerService.createSingleActionTimer(priorityTimer, timerConfig);
	    // System.out.println("### searchPriorityTimer is started for " + personaId);
	}
	
	@Timeout
	public void searchPriorityTimeout(Timer timer) {
		String[] infoArray = (String[]) timer.getInfo();
		int searchStage = 3;
		Long personaId = Long.valueOf(infoArray[0]);
		// System.out.println("### searchPriorityTimeout for " + personaId);
		if (matchmakingBO.isPlayerOnMMSearch(personaId)) { // Don't let the timer do stuff if player already quits the Race Now search
			openFireSoapBoxCli.send(XmppChat.createSystemMessage("### No lobbies to fit with Priority Class Group, looking for all lobbies..."), personaId);
			lobbyBO.joinFastLobby(personaId, Integer.parseInt(infoArray[1]), Integer.parseInt(infoArray[2]), 
					Boolean.valueOf(infoArray[3]), searchStage); // personaId, carClassHash, raceFilter, isSClassFilterActive
		}
	}
	
	// What reason for this? Is this necessary for lobbies?
//	@Schedule(second = "*/20", minute = "*", hour = "*", persistent = false)
//	public void run() {
//		List<LobbyEntity> findAllOpen = lobbyDao.findAllOpen();
//		if (findAllOpen != null) {
//			for (LobbyEntity lobbyEntity : findAllOpen) {
//				List<LobbyEntrantEntity> entrants = lobbyEntity.getEntrants();
//				if (entrants != null) {
//					for (LobbyEntrantEntity lobbyEntrantEntity : entrants) {
//						lobbyBO.sendJoinMsg(lobbyEntrantEntity.getPersona().getPersonaId(), entrants);
//					}
//				}
//			}
//		}
//	}

}
