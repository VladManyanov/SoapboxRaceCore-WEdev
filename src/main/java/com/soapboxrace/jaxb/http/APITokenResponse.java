package com.soapboxrace.jaxb.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Give the API access token and error code response
 * @author Hypercycle
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "APITokenResponse", propOrder = {
	"token",
	"errorcode",
})
public class APITokenResponse {
	@XmlElement(name = "Token")
	private String token;
	@XmlElement(name = "ErrorCode")
	private int errorcode;
	
	/**
	 * Set the token variable
	 * @param token
	 */
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	/**
	 * Set the ErrorCode variable
	 * @param errorcode
	 */
	public int getErrorCode() {
		return errorcode;
	}
	public void setErrorCode(int errorcode) {
		this.errorcode = errorcode;
	}
}
