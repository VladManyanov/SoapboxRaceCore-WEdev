package com.soapboxrace.jaxb.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Информация об событии сообщества
 * @author Hypercycle
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfCommunityEventInfo", propOrder = {
	"ceventdescription",
	"targetgoal",
	"finishdate",
	"reward",
	"trackarray"
})
public class ArrayOfCommunityEventInfo {
	@XmlElement(name = "CEventDescription")
	private String ceventdescription;
	@XmlElement(name = "TargetGoal")
	private int targetgoal;
	@XmlElement(name = "FinishDate")
	private String finishdate;
	@XmlElement(name = "Reward")
	private String reward;
	@XmlElement(name = "TrackArray")
	private List<TrackInfo> trackarray;
	
	
	public String getCEventDescription() {
		return ceventdescription;
	}
	public void setCEventDescription(String ceventdescription) {
		this.ceventdescription = ceventdescription;
	}
	
	public int getTargetGoal() {
		return targetgoal;
	}
	public void setTargetGoal(int targetgoal) {
		this.targetgoal = targetgoal;
	}
	
	public String getFinishDate() {
		return finishdate;
	}
	public void setFinishDate(String finishdate) {
		this.finishdate = finishdate;
	}
	
	public String getReward() {
		return reward;
	}
	public void setReward(String reward) {
		this.reward = reward;
	}
	
	public ArrayOfCommunityEventInfo() {
		trackarray = new ArrayList<TrackInfo>();
	}
	public void add(String trackName, int trackCounter) {
		trackarray.add(new TrackInfo(trackName, trackCounter));
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "TrackInfo", propOrder = {
		"TRACKNAME",
		"TRACKCOUNTER",
	})
	public static class TrackInfo {
		@XmlElement(name = "TrackName")
		private String TRACKNAME;
		@XmlElement(name = "TrackCounter")
		private int TRACKCOUNTER;
		
		protected TrackInfo (String trackName, int trackCounter) {
			TRACKNAME = trackName;
			TRACKCOUNTER = trackCounter;
		}
	}
}
