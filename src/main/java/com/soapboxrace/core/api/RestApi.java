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
import com.soapboxrace.jaxb.http.ChangePassword;

/**
 * API статистики
 * @author Vadimka
 */
@Path("RestApi")
public class RestApi {
	
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
	@Produces(MediaType.APPLICATION_XML)
	public Response apiAuth(@NotNull @QueryParam("key") String key) {
		String ipAddress = sr.getRemoteAddr();
		boolean isKeyVaild = bo.isKeyVaild(key, ipAddress);
		return Response.ok(bo.getAPIAuth(ipAddress, key, isKeyVaild)).build();
	}
	
	// ===================== Страницы =======================
	
	/**
	 * Страница вывода основной статистики в едином запросе
	 * @param onPage - сколько позиций вывести на странице (для суб-запросов)
	 */
	@GET
	@Path("GetMainPageStats")
	@Produces(MediaType.APPLICATION_XML)
	public Response mainPageStats(@NotNull @QueryParam("onpage") int onpage, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getMainPageStats(onpage)).build();
	}
	/**
	 * Страница вывода топа по количеству очков
	 * @param onPage - сколько позиций вывести на странице
	 */
	@GET
	@Path("GetTopScore")
	@Produces(MediaType.APPLICATION_XML)
	public Response topScores(@NotNull @QueryParam("onpage") int onpage, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getTopScores(onpage)).build();
	}
	/**
	 * Страница вывода топа по количеству пройденых заездов
	 * @param onPage - сколько позиций вывести на странице
	 */
	@GET
	@Path("GetTopRacers")
	@Produces(MediaType.APPLICATION_XML)
	public Response topRaces(@NotNull @QueryParam("onpage") int onPage, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getTopRacers(onPage)).build();
	}
	/**
	 * Страница вывода топа по цепочке сборка кристалов
	 * @param onPage - сколько позиций вывести на странице
	 */
	@GET
	@Path("GetTopTreasureHunt")
	@Produces(MediaType.APPLICATION_XML)
	public Response topTreasureHunt(@NotNull @QueryParam("onpage") int onPage, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.TopProfileTreasureHunt(onPage)).build();
	}
	/**
	 * Страница популярных заездов
	 */
	@GET
	@Path("GetPopularRaces")
	@Produces(MediaType.APPLICATION_XML)
	public Response mostPopularRace(@NotNull @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.MostPopularRaces()).build();
	}
	/**
	 * Страница популярных классов машин
	 * @param onPage - сколько позиций вывести на странице
	 */
	@GET
	@Path("GetPopularCarClasses")
	@Produces(MediaType.APPLICATION_XML)
	public Response mostPupularCarClass(@NotNull @QueryParam("onpage") int onPage, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getPopularCarClass(onPage)).build();
	}
	/**
	 * Страница популярных иконок профиля
	 * @param onPage - сколько позиций вывести на странице
	 */
	@GET
	@Path("GetPopularProfileIcons")
	@Produces(MediaType.APPLICATION_XML)
	public Response mostPupularProfileIcon(@NotNull @QueryParam("onpage") int onPage, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getPopularProfileIcons(onPage)).build();
	}
	/**
	 * Страница популярных имен машин
	 * @param onPage - сколько позиций вывести на странице
	 */
	@GET
	@Path("GetPopularCarName")
	@Produces(MediaType.APPLICATION_XML)
	public Response mostPopularCarName(@NotNull @QueryParam("onpage") int onPage, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getTopCarName(onPage)).build();
	}
	/**
	 * Страница Трассы
	 */
	@GET
	@Path("TopTimeOnEvent")
	@Produces(MediaType.APPLICATION_XML)
	public Response racesEvent(@NotNull @QueryParam("eventid") int eventid, @QueryParam("powerups") boolean powerups, @QueryParam("carclass") String carclass, 
			@QueryParam("oldrecords") boolean oldRecords, @QueryParam("page") int page, @QueryParam("onpage") int onPage, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getTopTimeRace(eventid, powerups, carclass, "", oldRecords, page, onPage)).build();
	}
	/**
	 * Страница Трассы для профиля
	 */
	@GET
	@Path("TopTimeRacesByPersona")
	@Produces(MediaType.APPLICATION_XML)
	public Response racesPersona(@NotNull @QueryParam("eventid") int eventid, @QueryParam("powerups") boolean powerups, @QueryParam("personaname") String personaname, 
			@QueryParam("oldrecords") boolean oldRecords, @QueryParam("page") int page, @QueryParam("onpage") int onPage, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
            String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
            return Response.temporaryRedirect(URI.create(accessDenied)).build();
        }
        return Response.ok(bo.getTopTimeRaceByPersona(eventid, powerups, personaname, true, page, onPage)).build();
    }
	/**
	 * Страница Трассы, вывод рекордов по модели автомобиля
	 */
	@GET
	@Path("TopTimeOnEventByCar")
	@Produces(MediaType.APPLICATION_XML)
	public Response racesEventByCar(@NotNull @QueryParam("eventid") int eventid, @QueryParam("powerups") boolean powerups, @QueryParam("carmodel") String carmodel, 
			@QueryParam("oldrecords") boolean oldRecords, @QueryParam("page") int page, @QueryParam("onpage") int onPage, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		if (carmodel == null) {carmodel = "";}
		return Response.ok(bo.getTopTimeRace(eventid, powerups, "all", carmodel, oldRecords, page, onPage)).build();
	}
	/**
	 * Страница Трассы
	 */
	@GET
	@Path("Events")
	@Produces(MediaType.APPLICATION_XML)
	public Response races(@NotNull @QueryParam("all") boolean all, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getRaces(all)).build();
	}
	/**
	 * Информация о поиске лобби в Быстрой Гонке (Race Now)
	 */
	@GET
	@Path("MatchmakingWebStats")
	@Produces(MediaType.APPLICATION_XML)
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
	@Produces(MediaType.APPLICATION_XML)
	public Response serverStats(@NotNull @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getServerStats()).build();
	}
	/**
	 * Данные о карте Командных Гонок и территориях
	 */
	@GET
	@Path("TeamsMap")
	@Produces(MediaType.APPLICATION_XML)
	public Response teamsMap(@NotNull @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getTeamsMapInfo()).build();
	}
	/**
	 * Страница Списка профилей
	 */
	@GET
	@Path("Personas")
	@Produces(MediaType.APPLICATION_XML)
	public Response personas(@NotNull @QueryParam("page") int page,@QueryParam("onpage") int onPage, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getPersonas(page, onPage)).build();
	}
	/**
	 * Страница Профиля
	 */
	@GET
	@Path("Persona")
	@Produces(MediaType.APPLICATION_XML)
	public Response personas(@NotNull @QueryParam("personaName") String personaName, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getPersonaInfo(personaName)).build();
	}
	/**
	 * Страница Профиля
	 */
	@GET
	@Path("PersonaPremium")
	@Produces(MediaType.APPLICATION_XML)
	public Response personasPremium(@NotNull 
				@QueryParam("personaName") String personaName,
				@QueryParam("email") String email,
				@QueryParam("password") String password, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getPersonaPremiumInfo(personaName, password, email)).build();
	}
	/**
	 * Изменение пароля
	 */
	@GET
	@Path("ChangePassword")
	@Produces(MediaType.APPLICATION_XML)
	public Response changePassword(@NotNull @QueryParam("email") String email, @QueryParam("password") String password, 
			@QueryParam("newPassword") String newPassword, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		ChangePassword obj = bo.changePassword(email, password, newPassword);
		if (obj.isOk()) return Response.ok(obj).build();
		return Response.serverError().entity(obj).build();
	}
	/**
	 * Удалить рекорд
	 */
	@GET
	@Path("DeleteRecord")
	@Produces(MediaType.APPLICATION_XML)
	public Response deleteRecord(@NotNull @QueryParam("id") Long id, @QueryParam("reason") String reason, @QueryParam("token") String token) {
		if (!apiTokenDAO.verifyToken(token, sr.getRemoteAddr())) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.deleteRecord(id, reason)).build();
	}
}
