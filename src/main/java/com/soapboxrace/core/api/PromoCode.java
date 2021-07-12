package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.PromoCodeBO;
import com.soapboxrace.core.bo.SalesBO;
import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.PersonaEntity;

@Path("/PromoCode")
public class PromoCode {

	@EJB
	private PromoCodeBO bo;

	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private SalesBO salesBO;
	
	@EJB
	private PersonaDAO personaDAO;

	@POST
	@Path("/createPromoCode")
	@Produces(MediaType.TEXT_HTML)
	public String createPromoCode(@FormParam("token") String token, @FormParam("codeType") String codeType) {
		if (parameterBO.getStrParam("TOKEN_PROMOCODE").equals(token) && codeType != null) {
			return bo.createPromoCode(codeType);
		}
		if (parameterBO.getStrParam("TOKEN_PROMOCODE").equals(token) && codeType == null) {
			return "ERROR: Code type is not selected, please try again";
		}
		return "ERROR: invalid token (not a staff? quit right now, hacker)";
	}

	@POST
	@Path("/usePromoCode")
	@Produces(MediaType.TEXT_HTML)
	public String usePromoCode(@FormParam("promoCode") String promoCode, @FormParam("email") String email, @FormParam("password") String password, @FormParam("nickname") String nickname, @FormParam("token") String token) {
		if (token != null && (promoCode.isEmpty() || nickname.isEmpty())) {
			return "ERROR: empty nickname or code";
		}
		if (token == null && (promoCode.isEmpty() || email.isEmpty() || password.isEmpty() || nickname.isEmpty())) {
			return "ERROR: empty email, password, nickname or code";
		}
		if (token != null && !parameterBO.getStrParam("TOKEN_PROMOCODE").equals(token)) {
			return "ERROR: invalid token (not a staff? quit right now, hacker)";
		}
		return bo.usePromoCode(promoCode, email, password, nickname, token);
	}
	
//	@POST
//	@Path("/processDonateRequest")
//	@Produces(MediaType.TEXT_HTML)
//	public String processDonateRequest(@FormParam("optionType") String optionType, @FormParam("email") String currencyAmountStr, @FormParam("password") String transactionId, @FormParam("nickname") String nickname, @FormParam("token") String webToken) {
//		// FIXME Доделать
//		PersonaEntity personaEntity = personaDAO.findByNameIgnoreCase(nickname);
//		float currencyAmount = Float.parseFloat(currencyAmountStr);
//		return bo.processDonateRequest(currencyAmount, transactionId, personaEntity, optionType);
//	}
	
	@POST
	@Path("/useDebug")
	@Produces(MediaType.TEXT_HTML)
	public String useDebug(@FormParam("adminToken") String adminToken, @FormParam("premiumType") String premiumType, @FormParam("extraMoney") String extraMoney, @FormParam("nickname") String nickname, @FormParam("timeYear") String timeYear, @FormParam("timeMonth") String timeMonth, @FormParam("timeDay") String timeDay) {
		if (adminToken != null && !parameterBO.getStrParam("TOKEN_ADMIN").equals(adminToken)) {
			return "ERROR: invalid token";
		}
		return bo.useDebug(premiumType, extraMoney, nickname, timeYear, timeMonth, timeDay);
	}
	
	@POST
	@Path("/saleGen")
	@Produces(MediaType.TEXT_HTML)
	public String saleGen(@FormParam("saleManagerToken") String saleManagerToken, @FormParam("saleCar1") String saleCar1, @FormParam("saleCar2") String saleCar2, @FormParam("saleCar3") String saleCar3, @FormParam("saleCar4") String saleCar4, @FormParam("saleName") String saleName) {
		if (saleManagerToken != null && !parameterBO.getStrParam("TOKEN_SALEMANAGER").equals(saleManagerToken)) {
			return "ERROR: invalid token";
		}
		return salesBO.saleGen(saleName, saleCar1, saleCar2, saleCar3, saleCar4);
	}
}
