package com.soapboxrace.jaxb.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Общий запрос для основных элементов статистики
 * @author Vadimka, Hypercycle
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MainPageStats", propOrder = {
	"topprofilescoredata",
	"topprofileracesdata",
	"topprofiletreasurehuntdata",
	"mostpopularracesdata",
	"arrayofcarclasshashdata",
	"arrayofprofileicondata",
	"arrayofcarnametopdata"
})
public class MainPageStats {
	@XmlElement(name = "TopProfileScoreData")
	protected TopProfileScore topprofilescoredata;
	@XmlElement(name = "TopProfileRacesData")
	protected TopProfileRaces topprofileracesdata;
	@XmlElement(name = "TopProfileTreasureHuntData")
	protected TopProfileTreasureHunt topprofiletreasurehuntdata;
	@XmlElement(name = "MostPopularRacesData")
	protected MostPopularRaces mostpopularracesdata;
	@XmlElement(name = "ArrayOfCarClassHashData")
	protected ArrayOfCarClassHash arrayofcarclasshashdata;
	@XmlElement(name = "ArrayOfProfileIconData")
	protected ArrayOfProfileIcon arrayofprofileicondata;
	@XmlElement(name = "ArrayOfCarNameTopData")
	protected ArrayOfCarNameTop arrayofcarnametopdata;
	
	public TopProfileScore getTopProfileScoreData() {
		return topprofilescoredata;
	}

	public void setTopProfileScoreData(TopProfileScore topprofilescoredata) {
		this.topprofilescoredata = topprofilescoredata;
	}
	//
	public TopProfileRaces getTopProfileRacesData() {
		return topprofileracesdata;
	}

	public void setTopProfileRacesData(TopProfileRaces topprofileracesdata) {
		this.topprofileracesdata = topprofileracesdata;
	}
	//
	public TopProfileTreasureHunt getTopProfileTreasureHuntData() {
		return topprofiletreasurehuntdata;
	}

	public void setTopProfileTreasureHuntData(TopProfileTreasureHunt topprofiletreasurehuntdata) {
		this.topprofiletreasurehuntdata = topprofiletreasurehuntdata;
	}
	//
	public MostPopularRaces getMostPopularRacesData() {
		return mostpopularracesdata;
	}

	public void setMostPopularRacesData(MostPopularRaces mostpopularracesdata) {
		this.mostpopularracesdata = mostpopularracesdata;
	}
	//
	public ArrayOfCarClassHash getArrayOfCarClassHashData() {
		return arrayofcarclasshashdata;
	}

	public void setArrayOfCarClassHashData(ArrayOfCarClassHash arrayofcarclasshashdata) {
		this.arrayofcarclasshashdata = arrayofcarclasshashdata;
	}
	//
	public ArrayOfProfileIcon getArrayOfProfileIconData() {
		return arrayofprofileicondata;
	}

	public void setArrayOfProfileIconData(ArrayOfProfileIcon arrayofprofileicondata) {
		this.arrayofprofileicondata = arrayofprofileicondata;
	}
	//
	public ArrayOfCarNameTop getArrayOfCarNameTopData() {
		return arrayofcarnametopdata;
	}

	public void setArrayOfCarNameTopData(ArrayOfCarNameTop arrayofcarnametopdata) {
		this.arrayofcarnametopdata = arrayofcarnametopdata;
	}
}
