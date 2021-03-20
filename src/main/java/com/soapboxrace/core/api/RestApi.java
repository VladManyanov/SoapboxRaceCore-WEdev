package com.soapboxrace.core.api;

import java.net.URI;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.soapboxrace.core.bo.ParameterBO;
import com.soapboxrace.core.bo.RestApiBO;
import com.soapboxrace.jaxb.http.ChangePassword;

/**
 * API статистики
 * @author Vadimka
 */
@Path("RestApi")
public class RestApi {
	
	// =============== Утилиты выборки ==============
	
	/**
	 * Выборка по REST API
	 */
	@EJB
	private RestApiBO bo;
	/**
	 * Параметры сервера
	 */
	@EJB
	private ParameterBO parameterBO;
	
	
	// ===================== Страницы =======================
	
	/**
	 * Страница вывода топа по количеству очков
	 * @param onPage - сколько позиций вывести на странице
	 */
	@GET
	@Path("GetTopScore")
	@Produces(MediaType.APPLICATION_XML)
	public Response topScores(@QueryParam("onpage") int onpage, @QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	public Response topRaces(@QueryParam("onpage") int onPage, @QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	public Response topTreasureHunt(@QueryParam("onpage") int onPage, @QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	public Response mostPupularRace(@QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	public Response mostPupularCarClass(@QueryParam("onpage") int onPage, @QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	public Response mostPupularProfileIcon(@QueryParam("onpage") int onPage, @QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	public Response mostPopularCarName(@QueryParam("onpage") int onPage, @QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	public Response racesEvent(@QueryParam("eventid") int eventid, @QueryParam("powerups") boolean powerups, @QueryParam("carclass") String carclass, 
			@QueryParam("oldrecords") boolean oldRecords, @QueryParam("page") int page, @QueryParam("onpage") int onPage, @QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	public Response racesPersona(@QueryParam("eventid") int eventid, @QueryParam("powerups") boolean powerups, @QueryParam("personaname") String personaname, 
			@QueryParam("oldrecords") boolean oldRecords, @QueryParam("page") int page, @QueryParam("onpage") int onPage, @QueryParam("key") String key) {
        if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	public Response racesEventByCar(@QueryParam("eventid") int eventid, @QueryParam("powerups") boolean powerups, @QueryParam("carmodel") String carmodel, 
			@QueryParam("oldrecords") boolean oldRecords, @QueryParam("page") int page, @QueryParam("onpage") int onPage, @QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	public Response races(@QueryParam("all") boolean all, @QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	@Produces(MediaType.APPLICATION_JSON)
	public Response mmWebStats(@QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		return Response.ok(bo.getMatchmakingWebStats()).build();
	}
	/**
	 * Страница Списка профилей
	 */
	@GET
	@Path("Personas")
	@Produces(MediaType.APPLICATION_XML)
	public Response personas(@QueryParam("page") int page,@QueryParam("onpage") int onPage, @QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	public Response personas(@QueryParam("personaName") String personaName, @QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	public Response personasPremium(
				@QueryParam("personaName") String personaName,
				@QueryParam("email") String email,
				@QueryParam("password") String password, @QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
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
	public Response changePassword(@QueryParam("email") String email, @QueryParam("password") String password, 
			@QueryParam("newPassword") String newPassword, @QueryParam("key") String key) {
		if (!parameterBO.getStrParam("RESTAPI_KEY").equals(key)) {
			String accessDenied = parameterBO.getStrParam("RESTAPI_FAILURELINK");
			return Response.temporaryRedirect(URI.create(accessDenied)).build();
		}
		ChangePassword obj = bo.changePassword(email, password, newPassword);
		if (obj.isOk()) return Response.ok(obj).build();
		return Response.serverError().entity(obj).build();
	}
}
