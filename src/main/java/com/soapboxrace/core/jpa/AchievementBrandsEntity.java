package com.soapboxrace.core.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "ACHIEVEMENT_BRANDS")
@NamedQueries({ //
	@NamedQuery(name = "AchievementBrandsEntity.findByPersonaId", query = "SELECT obj FROM AchievementBrandsEntity obj where obj.personaId = :personaId") })

public class AchievementBrandsEntity {

	@Id
	private Long personaId;
	
	// Original achievement set
	private int alfaRomeoWins;
	private int astonMartinWins;
	private int audiWins;
	private int bentleyWins;
	private int bmwWins;
	private int cadillacWins;
	private int caterhamWins;
	private int chevroletWins;
	private int chryslerWins;
	private int dodgeWins;
	private int fordWins;
	private int fordShelbyWins;
	private int hummerWins;
	private int infinitiWins;
	private int jaguarWins;
	private int jeepWins;
	private int koenigseggWins;
	private int lamborghiniWins;
	private int lanciaWins;
	private int lexusWins;
	private int lotusWins;
	private int marussiaWins;
	private int mazdaWins;
	private int mclarenWins;
	private int mercedesBenzWins;
	private int mitsubishiWins;
	private int nissanWins;
	private int paganiWins;
	private int plymouthWins;
	private int pontiacWins;
	private int porscheWins;
	private int renaultWins;
	private int scionWins;
	private int shelbyWins;
	private int subaruWins;
	private int toyotaWins;
	private int vauxhallWins;
	private int volkswagenWins;
	
	// Additional brands
	private int bugattiWins;
	private int ferrariWins;
	private int fiatWins;
	private int hondaWins;
	private int maseratiWins;
	private int nfsWins;
	private int smartWins;
	private int teslaWins;
	private int flankerWins;
	private int buickWins;
	private int polestarWins;
	private int arielWins;
	
	public Long getPersonaId() {
		return personaId;
	}

	public void setPersonaId(Long personaId) {
		this.personaId = personaId;
	}

	public int getAlfaRomeoWins() {
		return alfaRomeoWins;
	}

	public void setAlfaRomeoWins(int alfaRomeoWins) {
		this.alfaRomeoWins = alfaRomeoWins;
	}
	
	public int getAstonMartinWins() {
		return astonMartinWins;
	}

	public void setAstonMartinWins(int astonMartinWins) {
		this.astonMartinWins = astonMartinWins;
	}
	
	public int getAudiWins() {
		return audiWins;
	}

	public void setAudiWins(int audiWins) {
		this.audiWins = audiWins;
	}
	
	public int getBentleyWins() {
		return bentleyWins;
	}

	public void setBentleyWins(int bentleyWins) {
		this.bentleyWins = bentleyWins;
	}
	
	public int getBMWWins() {
		return bmwWins;
	}

	public void setBMWWins(int bmwWins) {
		this.bmwWins = bmwWins;
	}
	
	public int getCadillacWins() {
		return cadillacWins;
	}

	public void setCadillacWins(int cadillacWins) {
		this.cadillacWins = cadillacWins;
	}
	
	public int getCaterhamWins() {
		return caterhamWins;
	}

	public void setCaterhamWins(int caterhamWins) {
		this.caterhamWins = caterhamWins;
	}
	
	public int getChevroletWins() {
		return chevroletWins;
	}

	public void setChevroletWins(int chevroletWins) {
		this.chevroletWins = chevroletWins;
	}
	
	public int getChryslerWins() {
		return chryslerWins;
	}

	public void setChryslerWins(int chryslerWins) {
		this.chryslerWins = chryslerWins;
	}
	
	public int getDodgeWins() {
		return dodgeWins;
	}

	public void setDodgeWins(int dodgeWins) {
		this.dodgeWins = dodgeWins;
	}
	
	public int getFordWins() {
		return fordWins;
	}

	public void setFordWins(int fordWins) {
		this.fordWins = fordWins;
	}
	
	public int getFordShelbyWins() {
		return fordShelbyWins;
	}

	public void setFordShelbyWins(int fordShelbyWins) {
		this.fordShelbyWins = fordShelbyWins;
	}
	
	public int getHummerWins() {
		return hummerWins;
	}

	public void setHummerWins(int hummerWins) {
		this.hummerWins = hummerWins;
	}
	
	public int getInfinitiWins() {
		return infinitiWins;
	}

	public void setInfinitiWins(int infinitiWins) {
		this.infinitiWins = infinitiWins;
	}
	
	public int getJaguarWins() {
		return jaguarWins;
	}

	public void setJaguarWins(int jaguarWins) {
		this.jaguarWins = jaguarWins;
	}
	
	public int getJeepWins() {
		return jeepWins;
	}

	public void setJeepWins(int jeepWins) {
		this.jeepWins = jeepWins;
	}
	
	public int getKoenigseggWins() {
		return koenigseggWins;
	}

	public void setKoenigseggWins(int koenigseggWins) {
		this.koenigseggWins = koenigseggWins;
	}
	
	public int getLamborghiniWins() {
		return lamborghiniWins;
	}

	public void setLamborghiniWins(int lamborghiniWins) {
		this.lamborghiniWins = lamborghiniWins;
	}
	
	public int getLanciaWins() {
		return lanciaWins;
	}

	public void setLanciaWins(int lanciaWins) {
		this.lanciaWins = lanciaWins;
	}
	
	public int getLexusWins() {
		return lexusWins;
	}

	public void setLexusWins(int lexusWins) {
		this.lexusWins = lexusWins;
	}
	
	public int getLotusWins() {
		return lotusWins;
	}

	public void setLotusWins(int lotusWins) {
		this.lotusWins = lotusWins;
	}
	
	public int getMarussiaWins() {
		return marussiaWins;
	}

	public void setMarussiaWins(int marussiaWins) {
		this.marussiaWins = marussiaWins;
	}
	
	public int getMazdaWins() {
		return mazdaWins;
	}

	public void setMazdaWins(int mazdaWins) {
		this.mazdaWins = mazdaWins;
	}
	
	public int getMclarenWins() {
		return mclarenWins;
	}

	public void setMclarenWins(int mclarenWins) {
		this.mclarenWins = mclarenWins;
	}
	
	public int getMercedesBenzWins() {
		return mercedesBenzWins;
	}

	public void setMercedesBenzWins(int mercedesBenzWins) {
		this.mercedesBenzWins = mercedesBenzWins;
	}
	
	public int getMitsubishiWins() {
		return mitsubishiWins;
	}

	public void setMitsubishiWins(int mitsubishiWins) {
		this.mitsubishiWins = mitsubishiWins;
	}
	
	public int getNissanWins() {
		return nissanWins;
	}

	public void setNissanWins(int nissanWins) {
		this.nissanWins = nissanWins;
	}
	
	public int getPaganiWins() {
		return paganiWins;
	}

	public void setPaganiWins(int paganiWins) {
		this.paganiWins = paganiWins;
	}
	
	public int getPlymouthWins() {
		return plymouthWins;
	}

	public void setPlymouthWins(int plymouthWins) {
		this.plymouthWins = plymouthWins;
	}
	
	public int getPontiacWins() {
		return pontiacWins;
	}

	public void setPontiacWins(int pontiacWins) {
		this.pontiacWins = pontiacWins;
	}
	
	public int getPorscheWins() {
		return porscheWins;
	}

	public void setPorscheWins(int porscheWins) {
		this.porscheWins = porscheWins;
	}
	
	public int getRenaultWins() {
		return renaultWins;
	}

	public void setRenaultWins(int renaultWins) {
		this.renaultWins = renaultWins;
	}
	
	public int getScionWins() {
		return scionWins;
	}

	public void setScionWins(int scionWins) {
		this.scionWins = scionWins;
	}
	
	public int getShelbyWins() {
		return shelbyWins;
	}

	public void setShelbyWins(int shelbyWins) {
		this.shelbyWins = shelbyWins;
	}
	
	public int getSubaruWins() {
		return subaruWins;
	}

	public void setSubaruWins(int subaruWins) {
		this.subaruWins = subaruWins;
	}
	
	public int getToyotaWins() {
		return toyotaWins;
	}

	public void setToyotaWins(int toyotaWins) {
		this.toyotaWins = toyotaWins;
	}
	
	public int getVauxhallWins() {
		return vauxhallWins;
	}

	public void setVauxhallWins(int vauxhallWins) {
		this.vauxhallWins = vauxhallWins;
	}
	
	public int getVolkswagenWins() {
		return volkswagenWins;
	}

	public void setVolkswagenWins(int volkswagenWins) {
		this.volkswagenWins = volkswagenWins;
	}
	
	//
	
	public int getBugattiWins() {
		return bugattiWins;
	}

	public void setBugattiWins(int bugattiWins) {
		this.bugattiWins = bugattiWins;
	}
	
	public int getFerrariWins() {
		return ferrariWins;
	}

	public void setFerrariWins(int ferrariWins) {
		this.ferrariWins = ferrariWins;
	}
	
	public int getFiatWins() {
		return fiatWins;
	}

	public void setFiatWins(int fiatWins) {
		this.fiatWins = fiatWins;
	}
	
	public int getHondaWins() {
		return hondaWins;
	}

	public void setHondaWins(int hondaWins) {
		this.hondaWins = hondaWins;
	}
	
	public int getMaseratiWins() {
		return maseratiWins;
	}

	public void setMaseratiWins(int maseratiWins) {
		this.maseratiWins = maseratiWins;
	}
	
	public int getNFSWins() {
		return nfsWins;
	}

	public void setNFSWins(int nfsWins) {
		this.nfsWins = nfsWins;
	}
	
	public int getSmartWins() {
		return smartWins;
	}

	public void setSmartWins(int smartWins) {
		this.smartWins = smartWins;
	}
	
	public int getTeslaWins() {
		return teslaWins;
	}

	public void setTeslaWins(int teslaWins) {
		this.teslaWins = teslaWins;
	}
	
	public int getFlankerWins() {
		return flankerWins;
	}

	public void setFlankerWins(int flankerWins) {
		this.flankerWins = flankerWins;
	}
	
	public int getBuickWins() {
		return buickWins;
	}

	public void setBuickWins(int buickWins) {
		this.buickWins = buickWins;
	}
	
	public int getPolestarWins() {
		return polestarWins;
	}

	public void setPolestarWins(int polestarWins) {
		this.polestarWins = polestarWins;
	}
	
	public int getArielWins() {
		return arielWins;
	}

	public void setArielWins(int arielWins) {
		this.arielWins = arielWins;
	}
	
}
