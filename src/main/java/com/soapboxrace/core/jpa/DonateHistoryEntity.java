package com.soapboxrace.core.jpa;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "DONATEHISTORY")
@NamedQueries({ //
@NamedQuery(name = "DonateHistoryEntity.findByUser", //
		query = "SELECT obj FROM DonateHistoryEntity obj WHERE obj.user = :user ")
})
public class DonateHistoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "USERID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_DONATEHISTORY_USER"))
	private UserEntity user;
	
	@ManyToOne
	@JoinColumn(name = "PERSONAID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_DONATEHISTORY_PERSONA"))
	private PersonaEntity persona;
	
	private float currencyAmount;
	private String optionType;
	private String transactionId;
	private LocalDateTime date;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}
	
	public float getCurrencyAmount() {
		return currencyAmount;
	}

	public void setCurrencyAmount(float currencyAmount) {
		this.currencyAmount = currencyAmount;
	}
	
	public String getOptionType() {
		return optionType;
	}

	public void setOptionType(String optionType) {
		this.optionType = optionType;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public PersonaEntity getPersona() {
		return persona;
	}

	public void setPersona(PersonaEntity persona) {
		this.persona = persona;
	}
	
	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

}