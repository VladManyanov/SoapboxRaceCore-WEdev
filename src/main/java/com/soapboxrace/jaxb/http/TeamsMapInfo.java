package com.soapboxrace.jaxb.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.soapboxrace.jaxb.http.TeamsMapInfo.MapRegion.MapEvent;

/**
 * Данные карты территории для командных гонок
 * @author Hypercycle
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TeamsMapInfo", propOrder = {
	"season",	
	"toppointsteam",
	"topregionsteam",
	"prevseasonwinner",
	"mapregion"
})
public class TeamsMapInfo {
	@XmlElement(name = "Season")
	private int season;
	@XmlElement(name = "TopPointsTeam")
	private String toppointsteam;
	@XmlElement(name = "TopRegionsTeam")
	private String topregionsteam;
	@XmlElement(name = "PrevSeasonWinner")
	private String prevseasonwinner;
	@XmlElement(name = "MapRegion")
	private List<MapRegion> mapregion;
	
	public int getSeason() {
		return season;
	}
	public void setSeason(int season) {
		this.season = season;
	}
	
	public String getTopPointsTeam() {
		return toppointsteam;
	}
	public void setTopPointsTeam(String toppointsteam) {
		this.toppointsteam = toppointsteam;
	}
	
	public String getTopRegionsTeam() {
		return toppointsteam;
	}
	public void setTopRegionsTeam(String topregionsteam) {
		this.topregionsteam = topregionsteam;
	}
	
	public String getPrevSeasonWinner() {
		return prevseasonwinner;
	}
	public void setPrevSeasonWinner(String prevseasonwinner) {
		this.prevseasonwinner = prevseasonwinner;
	}
	
	public TeamsMapInfo() {
		mapregion = new ArrayList<MapRegion>();
	}
	public void addMapRegion(int regionId, String regionName, String teamOwner, String teamPrevOwner, boolean startBonus,
			float activityLevel, float posX, float posY, List<MapEvent> mapEventList) {
		mapregion.add(new MapRegion(regionId, regionName, teamOwner, teamPrevOwner, startBonus, activityLevel, posX, posY, mapEventList));
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "MapRegion", propOrder = {
		"regionid",
		"regionname",
		"teamowner",
		"teamprevowner",
		"startbonus",
		"activitylevel",
		"posx",
		"posy",
		"mapevent"
	})
	public static class MapRegion {
		@XmlElement(name = "RegionId")
		private int regionid;
		@XmlElement(name = "RegionName")
		private String regionname;
		@XmlElement(name = "TeamOwner")
		private String teamowner;
		@XmlElement(name = "TeamPrevOwner")
		private String teamprevowner;
		@XmlElement(name = "StartBonus")
		private boolean startbonus;
		@XmlElement(name = "ActivityLevel")
		private float activitylevel;
		@XmlElement(name = "PosX")
		private float posx;
		@XmlElement(name = "PosY")
		private float posy;
		@XmlElement(name = "MapEvent")
		private List<MapEvent> mapevent;
		
		protected MapRegion (int regionId, String regionName, String teamOwner, String teamPrevOwner, boolean startBonus,
				float activityLevel, float posX, float posY, List<MapEvent> mapEventList) {
			regionid = regionId;
			regionname = regionName;
			teamowner = teamOwner;
			teamprevowner = teamPrevOwner;
			startbonus = startBonus;
			activitylevel = activityLevel;
			posx = posX;
			posy = posY;
			mapevent = mapEventList;
		}
		
		public void MapRegionEvents() {
			mapevent = new ArrayList<MapEvent>();
		}
		public void addMapEvent(int eventId, String eventName, String teamWinner, String forceCarModel, int winPoints,
				String tokenApplied, String tokenParameter, float posX, float posY) {
			mapevent.add(new MapEvent(eventId, eventName, teamWinner, forceCarModel, winPoints, tokenApplied, 
					tokenParameter, posX, posY));
		}
		
		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "MapEvent", propOrder = {
			"eventid",
			"eventname",
			"teamwinner",
			"forcecarmodel",
			"winpoints",
			"tokenapplied",
			"tokenparameter",
			"posx",
			"posy"
		})
		public static class MapEvent {
			@XmlElement(name = "EventId")
			private int eventid;
			@XmlElement(name = "EventName")
			private String eventname;
			@XmlElement(name = "TeamWinner")
			private String teamwinner;
			@XmlElement(name = "ForceCarModel")
			private String forcecarmodel;
			@XmlElement(name = "WinPoints")
			private int winpoints;
			@XmlElement(name = "TokenApplied")
			private String tokenapplied;
			@XmlElement(name = "TokenParameter")
			private String tokenparameter;
			@XmlElement(name = "PosX")
			private float posx;
			@XmlElement(name = "PosY")
			private float posy;
			
			public MapEvent (int eventId, String eventName, String teamWinner, String forceCarModel, int winPoints,
					String tokenApplied, String tokenParameter, float posX, float posY) {
				eventid = eventId;
				eventname = eventName;
				teamwinner = teamWinner;
				forcecarmodel = forceCarModel;
				winpoints = winPoints;
				tokenapplied = tokenApplied;
				tokenparameter = tokenParameter;
				posx = posX;
				posy = posY;
			}
	    }
    }
}
