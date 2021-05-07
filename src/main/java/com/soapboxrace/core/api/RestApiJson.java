package com.soapboxrace.core.api;

import java.net.URI;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.RestApiBO;
import com.soapboxrace.core.dao.APITokenDAO;

/**
 * API статистики с выдачей в формате JSON
 * @author Vadimka, Hypercycle
 */
@Path("RestApi/Json")
public class RestApiJson {
	
	// =============== Утилиты выборки ==============
	
	@EJB
	private RestApiBO bo;

	@EJB
	private ParameterBO parameterBO;
	
	@EJB
	private APITokenDAO apiTokenDAO;
	
	@Context
	private HttpServletRequest sr;
	
	// ===================== Начальная авторизация =======================

	/**
	 * Выдача токена для доступа к API
	 * @param key - постоянный ключ
	 */
	@GET
	@Path("apiAuth")
	@Produces(MediaType.APPLICATION_JSON)
	public Response apiAuth(@NotNull @QueryParam("key") String key) {
		String ipAddress = sr.getRemoteAddr();
		boolean isKeyVaild = bo.isKeyVaild(key, ipAddress);
		return Response.ok(bo.getAPIAuth(ipAddress, key, isKeyVaild)).build();
	}
	
	// ===================== Страницы =======================
	
	/**
	 * Информация о поиске лобби в Быстрой Гонке (Race Now)
	 */
	@GET
	@Path("MatchmakingWebStats")
	@Produces(MediaType.APPLICATION_JSON)
	public Response mmWebStats(@NotNull @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getMatchmakingWebStats()).build();
	}
	
	/**
	 * Общая информация о сервере и событиях
	 */
	@GET
	@Path("ServerStats")
	@Produces(MediaType.APPLICATION_JSON)
	public Response serverStats(@NotNull @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getServerStats()).build();
	}
	
	/**
	 * Информация об событии сообщества
	 */
	@GET
	@Path("CommunityEventStats")
	@Produces(MediaType.APPLICATION_JSON)
	public Response communityEventStats(@NotNull @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getCommunityEventStats()).build();
	}
}
