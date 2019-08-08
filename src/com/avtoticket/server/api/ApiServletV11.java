/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.server.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.DB;
import com.avtoticket.server.db.stored.StoredProcs;
import com.avtoticket.server.rpc.TicketServiceImpl;
import com.avtoticket.server.servlet.ExtHttpServlet;
import com.avtoticket.server.utils.KpasProtocolUtil;
import com.avtoticket.server.utils.CommonServerUtils;
import com.avtoticket.server.utils.UserUtil;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.DocType;
import com.avtoticket.shared.models.core.Gender;
import com.avtoticket.shared.models.core.Nationality;
import com.avtoticket.shared.models.core.Passage;
import com.avtoticket.shared.models.core.Station;
import com.avtoticket.shared.models.core.Ticket;
import com.avtoticket.shared.models.core.User;
import com.avtoticket.shared.utils.DateUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 24 июля 2016 г. 1:46:16
 */
public class ApiServletV11 extends ExtHttpServlet {

	private static final long serialVersionUID = -3804917311407491620L;

	private static Logger logger = LoggerFactory.getLogger(ApiServletV11.class);

	public static final Cache<String, Passage> psgcache = CacheBuilder
			.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();

	private static long parseLong(HttpServletRequest req, String name) throws ApiException {
		String num = req.getParameter(name);
		try {
			return Long.parseLong(num);
		} catch (Exception e) {
			throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат целого числа - " + num);
		}
	}

	private static Long parseLongOrNull(HttpServletRequest req, String name) throws ApiException {
		String num = req.getParameter(name);
		if ((num == null) || num.isEmpty())
			return null;
		try {
			return Long.parseLong(num);
		} catch (Exception e) {
			throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат целого числа - " + num);
		}
	}

	private static Date parseDate(HttpServletRequest req, String name) throws ApiException {
		String date = req.getParameter(name);
		try {
			return new SimpleDateFormat("dd.MM.yyyy").parse(date);
		} catch (Exception e) {
			throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат даты - " + date);
		}
	}

	private static Date parseDateOrNull(HttpServletRequest req, String name) throws ApiException {
		String date = req.getParameter(name);
		if ((date == null) || date.isEmpty())
			return null;
		try {
			return new SimpleDateFormat("dd.MM.yyyy").parse(date);
		} catch (Exception e) {
			throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат даты - " + date);
		}
	}

	private static List<DocType> getDocTypes() throws Exception {
		List<DocType> ret = DB.getModels(DocType.class, new Where(), DocType.ID);
		ret.forEach(dt -> {
			dt.remove(Nationality.DISPLAY_FIELD);
			dt.remove(Nationality.CLASS_NAME);
		});
		return ret;
	}

	private static List<Nationality> getNations() throws Exception {
		List<Nationality> ret = DB.getModels(Nationality.class, new Where(), Nationality.MAIN + " desc, " + Nationality.NAME);
		ret.forEach(n -> {
			n.remove(Nationality.MAIN);
			n.remove(Nationality.DISPLAY_FIELD);
			n.remove(Nationality.CLASS_NAME);
		});
		return ret;
	}

	private static List<Station> getDeps() throws Exception {
		List<Station> ret = DB.getKeyValueList(Station.class, new String[] {Station.ID, Station.NAME}, Where.equals(Station.IS_DEPARTURE_POINT, true), null);
		ret.forEach(s -> {
			s.remove(Station.VALUE_FIELD);
			s.remove(Station.DISPLAY_FIELD);
			s.remove(Station.CLASS_NAME);
		});
		return ret;
	}

	private static List<Station> getDests(Long depId) throws Exception {
		Map<Station, List<Station>> stations = TicketServiceImpl.getStationsBase();
		List<Station> ret = null;
		for (Station s : stations.keySet())
			if (depId.equals(s.getId())) {
				ret = stations.get(s);
				ret.forEach(st -> {
					st.remove(Station.KPAS_ID);
					st.remove(Station.STAV_ID);
					st.remove(Station.VALUE_FIELD);
					st.remove(Station.DISPLAY_FIELD);
					st.remove(Station.CLASS_NAME);
					st.remove(Station.ADDRESS);
					st.remove(Station.LAT);
					st.remove(Station.LNG);
					st.remove(Station.NASPUNKT);
					st.remove(Station.NASPUNKT_ID);
					st.remove(Station.IS_DEPARTURE_POINT);
				});
				break;
			}
		return ret;
	}

	private static List<Passage> getPassages(Long depId, Long destId, Date date) throws Exception {
		if (date.getTime() + 24 * 60 * 60 * 1000 <= new Date().getTime())
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Указана прошедшая дата");
		List<Passage> ret = KpasProtocolUtil.getPassages(depId, date, destId);
		Station dep = DB.getModel(Station.class, depId);
		Station dest = DB.getModel(Station.class, destId);
		for (Passage p : ret) {
			Passage psg = new Passage();
			psg.fill(p);
			psg.setDepId(depId);
			psg.setDepName(dep.getName());
			psg.setDestId(destId);
			psg.setDestName(dest.getName());
			psgcache.put(psg.getId() + "_" + new SimpleDateFormat("dd.MM.yyyy").format(psg.getDeparture()), psg);

//			p.set(Passage.DEPARTURE + "_date", p.getDeparture());
//			p.set(Passage.ARRIVAL + "_date", p.getArrival());
			p.set("is_transit", Objects.equals(p.getTranzit(), 1L));
			p.set("is_raft", p.getRaftFlag());
			p.set("total_seats", p.getTotalSeats());
			p.set("free_seats", p.getFreeSeats());
			p.set("bus_mark", p.getBusMark());
			p.set("bus_type", p.getBusType());
			p.remove("elementName");
			p.remove(Passage.RAFT_FLAG);
			p.remove(Passage.BUS_TYPE);
			p.remove(Passage.BUS_MARK);
			p.remove(Passage.FREE_SEATS);
			p.remove(Passage.TOTAL_SEATS);
			p.remove(Passage.TRANZIT);
//			p.remove(Passage.DEPARTURE);
//			p.remove(Passage.ARRIVAL);
			p.remove(Passage.SALE_SEAT_LIST_LONG);
			p.remove(Passage.SALE_SEAT_LIST);
			p.remove(Passage.CLASS_NAME);
			p.remove(Passage.TARIFF);
			p.remove(Passage.PREF_TARIFF);
			p.remove(Passage.BAG_TARIFF);
			p.remove(Passage.RAFT_TARIFF);
			p.remove(Passage.RAFT_TARIFF_CH);
			p.remove(Passage.OB_STRAH);
			p.remove(Passage.KOM_SBOR);
			p.remove(Passage.TBTARIF);
		}
		return ret;
	}

	private static void cleanTicket(Ticket t) {
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		t.set("price", t.getAmount());
		t.set(Ticket.BIRTH_DATE, df.format(t.getBirthDate()));
		t.set("doc_issue", df.format(t.getVdate()));
		t.set("is_child", "д".equalsIgnoreCase(t.getTicketType()));
		t.set("is_baggage", t.isBaggage());
		t.set(Ticket.DOCUMENT, t.getDocTypeName());
		t.set(Ticket.ID, t.getTktNumber());
		t.set("doc_series", t.getSeriya());
		t.set("doc_number", t.getNumber());
		t.set("name", t.getReisName());
		t.set("sale_time", t.getSellTime());
		t.set("statement", t.getNomerVedomosti());
		t.set(Ticket.SEAT, t.isBaggage() ? "багажное"
				: (((t.getTBTarif() != null) && (t.getTBTarif() > 0L)) ? "б/м" : String.valueOf(t.getSeat())));
		t.remove(Ticket.CLASS_NAME);
		t.remove(Ticket.VALUE_FIELD);
		t.remove(Ticket.DOCTYPENAME);
		t.remove(Ticket.TBTARIF);
		t.remove(Ticket.KOM_SBOR);
		t.remove(Ticket.OB_STRAH);
		t.remove(Ticket.TARIF);
		t.remove(Ticket.TARIF_FACT);
		t.remove(Ticket.FROM_ID);
		t.remove(Ticket.TO_ID);
		t.remove(Ticket.PAROM);
		t.remove(Ticket.PAROM_TARIF);
		t.remove(Ticket.PAROM_TARIF_FACT);
		t.remove(Ticket.PAROM_TICKET_TYPE);
		t.remove(Ticket.BILL_ID);
		t.remove("ustarif");
		t.remove(Ticket.GRAJD);
		t.remove(Ticket.BAGTARIF);
		t.remove(Ticket.BAG1);
		t.remove(Ticket.BAG2);
		t.remove(Ticket.TICKET_TYPE);
		t.remove(Ticket.TKTNUMBER);
		t.remove(Ticket.SERIYA);
		t.remove(Ticket.NUMBER);
		t.remove(Ticket.REIS_NAME);
		t.remove(Ticket.ISBAGGAGE);
		t.remove(Ticket.V_DATE);
		t.remove(Ticket.SELL_TIME);
		t.remove(Ticket.NOMER_VEDOMOSTI);
		t.remove(Ticket.USER_ID);
		t.remove(Ticket.PRICE_BAG);
		t.remove(Ticket.AMOUNT);
	}

	private static List<Ticket> getTickets(Long userId, String hash) throws Exception {
		List<Ticket> ret = StoredProcs.core.getTickets(userId, null, hash);
		ret.forEach(ApiServletV11::cleanTicket);
		return ret;
	}

	private static String registerUser(HttpServletRequest req, String login, String lastname, String firstname, String middlename,
			Long docId, String docSeries, String docNumber, Date docIssue, String phone, Date birthdate, String gender, Long nationId) throws Exception {
		Gender sex = null;
		if ((gender != null) && !gender.isEmpty())
			try {
				sex = Gender.valueOf(gender.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Некорректный запрос - значение перечисления '" + gender + "' не существует", e);
			}
		if ((login == null) || login.isEmpty())
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный запрос - не указан логин пользователя");
		if (!CommonServerUtils.isValidEmail(login))
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный запрос - неправильный формат логина");
		if (!StoredProcs.core.isLoginFree(login))
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Пользователь с таким логином уже существует");
		if ((docId != null) && (DB.getModel(DocType.class, docId) == null))
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Документ с идентификатором " + docId + " не существует");
		if ((nationId != null) && (DB.getModel(Nationality.class, nationId) == null))
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Национальность с идентификатором " + nationId + " не существует");

		// регистрация пользователя
		String pass = RandomStringUtils.randomAlphanumeric(8);
		if (!UserUtil.regUser(req, login, pass))
			throw new ApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "При регистрации нового пользователя произошла ошибка");

		User user = StoredProcs.core.getUser(login);
		user.setLastName(lastname);
		user.setFirstName(firstname);
		user.setMiddleName(middlename);
		user.setPaspSeriya(docSeries);
		user.setPaspNumber(docNumber);
		user.setVdate(docIssue);
		user.setPhone(phone);
		user.setGender(sex);
		user.setBirthday(birthdate);
		user.setGrajd(nationId);
		user.setDocType(docId);
		user.setApiToken(new com.avtoticket.shared.models.UUID(UUID.randomUUID().toString()));
		DB.save(user, -1L);

		return user.getApiToken().toString();
	}

	private static User getUser(String login, String password) throws Exception {
		Long uid = StoredProcs.core.getUser(login, password);
		if (uid == null)
			throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "Пользователь с такими реквизитами не найден");
		User user = DB.getModel(User.class, uid);

		if (user.getApiToken() == null) {
			user.setApiToken(new com.avtoticket.shared.models.UUID(UUID.randomUUID().toString()));
			DB.save(user, -1L);
		}
		user.set(User.API_TOKEN, user.getApiToken().toString());
		user.set(User.DOCUMENT, user.getDocType());
		user.set("nationality", user.getGrajd());
		user.set("birthdate", user.getBirthday());
		if (!user.getPaspSeriya().isEmpty())
			user.set("doc_series", user.getPaspSeriya());
		if (!user.getPaspNumber().isEmpty())
			user.set("doc_number", user.getPaspNumber());
		user.set("doc_issue", user.getVdate());
		user.remove(User.ID);
		user.remove(User.CLASS_NAME);
		user.remove(User.GRAJDNAME);
		user.remove(User.REG_DATE);
		user.remove(User.IS_ADMIN);
		user.remove(User.GRAJD);
		user.remove(User.DOCTYPE);
		user.remove(User.B_DATE);
		user.remove(User.V_DATE);
		user.remove(User.PASP_SERIYA);
		user.remove(User.PASP_NUMBER);
		user.remove(User.VALUE_FIELD);
		return user;
	}

	private static String auth(HttpServletRequest req, HttpServletResponse resp, String apiToken) throws Exception {
		UUID token = null;
		try {
			token = UUID.fromString(apiToken);
		} catch (Exception e) {
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный ключ доступа", e);
		}
		User user = DB.getModel(User.class, Where.equals(User.API_TOKEN, token));
		if (user == null)
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный ключ доступа");

		Long currentId = UserUtil.getUserIdFromSession(req);
		if (!Objects.equals(currentId, user.getId())) {
			UserUtil.logout(req, resp);
			UserUtil.login(user.getId(), false, req, resp);
		}

		return "/";
	}

	private static BaseModel getTickets(String apiToken, Long offset, Long limit) throws Exception {
		UUID token = null;
		try {
			token = UUID.fromString(apiToken);
		} catch (Exception e) {
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный ключ доступа", e);
		}
		User user = DB.getModel(User.class, Where.equals(User.API_TOKEN, token));
		if (user == null)
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный ключ доступа");

		BaseModel ret = new BaseModel();
		ret.remove(BaseModel.CLASS_NAME);
		List<Ticket> tickets = StoredProcs.core.getTickets(user.getId(), null, null);
		if (tickets == null)
			return ret;
		ret.set("items_count", tickets.size());
		List<Ticket> tkts = tickets.subList(Math.max(0, Math.min(tickets.size(), (offset != null) ? offset.intValue() : 0)),
				Math.max(0, Math.min(tickets.size(), ((offset != null) ? offset.intValue() : 0) + ((limit != null) ? limit.intValue() : tickets.size()))));
		tkts.forEach(ApiServletV11::cleanTicket);
		ret.set("items", tkts);
		return ret;
	}

	private static String sellTicket(HttpServletRequest req, String userAgent, Long psgid, Date date, String successUrl, String failUrl, String usrs) throws Exception {
		Passage psg = psgcache.getIfPresent(psgid + "_" + new SimpleDateFormat("dd.MM.yyyy").format(date));
		if (psg == null)
			throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "Не найден указанный рейс. Повторите поиск.");
		Gson gson = new GsonBuilder().setDateFormat("dd.MM.yyyy").create();
		List<User> users = gson.fromJson(usrs, new TypeToken<List<User>>() {
		}.getType());
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		for (User u : users) {
			String gender = u.getStringProp(User.GENDER).trim();
			if (gender.isEmpty())
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Не указан пол пассажира");
			else
				try {
					u.setGender(Gender.valueOf(gender.toUpperCase()));
				} catch (IllegalArgumentException e) {
					throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Некорректный запрос - значение перечисления '" + gender + "' не существует", e);
				}

			u.setBirthday(df.parse(u.getStringProp("birthdate")));
			u.setDocType(u.getLongProp("document"));
			u.setPaspSeriya(u.getStringProp("doc_series"));
			u.setPaspNumber(u.getStringProp("doc_number"));
			u.setVdate(df.parse(u.getStringProp("doc_issue")));
			u.setGrajd(u.getLongProp("nationality"));
			if (u.getBags() == null)
				u.setBags(0L);
			else
				u.setBags(u.getBags());	// преобразуем в long

			if (u.getLastName().trim().isEmpty())
				throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Не указана фамилия пассажира");
			if (u.getFirstName().trim().isEmpty())
				throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Не указано имя пассажира");
			if (u.getMiddleName().trim().isEmpty())
				throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Не указано отчество пассажира");
			if (u.getBirthday() == null)
				throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Не указан день рождения пассажира");
			if (u.getDocType() == null)
				throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Не указан документ пассажира");
			if (u.getPaspSeriya().trim().isEmpty())
				throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Не указана серия документа");
			if (u.getPaspNumber().trim().isEmpty())
				throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Не указан номер документа");
			if (u.getVdate() == null)
				throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Не указана дата выдачи документа");
			if (u.getPhone().trim().isEmpty())
				throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Не указан телефон пассажира");
			if (u.getGrajd() == null)
				throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Не указано гражданство пассажира");

			u.setIsChild(DateUtil.getAge(u.getBirthday(), psg.getDeparture()) <= Integer.parseInt(StoredProcs.core.getProp("childhood")));
		}
		return TicketServiceImpl.buyTickets(req, userAgent, users.get(0), users, psg, true);
	}

	private static void dispatch(HttpServletRequest req, HttpServletResponse resp) {
		String act = null;
		Object ret = null;
		try {
			req.setCharacterEncoding("UTF-8");
			String uri = req.getRequestURI();
			logger.debug(uri);

			act = uri.substring(uri.lastIndexOf('/') + 1);
			switch (act) {
			case "docs":
				ret = getDocTypes();
				break;
			case "nations":
				ret = getNations();
				break;
			case "deps":
				ret = getDeps();
				break;
			case "dests":
				ret = getDests(parseLong(req, "dep_id"));
				break;
			case "passages":
				ret = getPassages(parseLong(req, "dep_id"), parseLong(req, "dest_id"), parseDate(req, "date"));
				break;
			case "orders":
				if ("GET".equalsIgnoreCase(req.getMethod()))
					ret = getTickets(null, req.getParameter("id"));
				else {
					String redirect = sellTicket(req, req.getHeader(HttpHeaders.USER_AGENT), parseLong(req, "id"), parseDate(req, "date"), req.getParameter("success_url"), req.getParameter("fail_url"), req.getParameter("passengers"));
					logger.debug(redirect);
					try {
						resp.setCharacterEncoding("UTF-8");
						resp.sendRedirect(redirect);
						resp.flushBuffer();
					} catch (IOException e) {
						logger.warn(e.getClass() + ": " + e.getMessage());
					}
					return;
				}
				break;
			case "users":
				if ("GET".equalsIgnoreCase(req.getMethod()))
					ret = getUser(req.getParameter("login"), req.getParameter("password"));
				else {
					String token = registerUser(req, req.getParameter(User.LOGIN), req.getParameter(User.LASTNAME), req.getParameter(User.FIRSTNAME),
							req.getParameter(User.MIDDLENAME), parseLongOrNull(req, "document"), req.getParameter("doc_series"), req.getParameter("doc_number"),
							parseDateOrNull(req, "doc_issue"), req.getParameter(User.PHONE), parseDateOrNull(req, "birthdate"), req.getParameter(User.GENDER),
							parseLongOrNull(req, "nationality"));
					logger.debug(token);
					try {
						resp.setCharacterEncoding("UTF-8");
						responseHtml(resp, token);
						resp.flushBuffer();
					} catch (IOException e) {
						logger.warn(e.getClass() + ": " + e.getMessage());
					}
					return;
				}
				break;
			case "tickets":
				ret = getTickets(req.getParameter(User.API_TOKEN), parseLongOrNull(req, "offset"), parseLongOrNull(req, "limit"));
				break;
			case "sessions":
				String redirect = auth(req, resp, req.getParameter(User.API_TOKEN));
				logger.debug(redirect);
				try {
					resp.setCharacterEncoding("UTF-8");
					resp.sendRedirect(redirect);
					resp.flushBuffer();
				} catch (IOException e) {
					logger.warn(e.getClass() + ": " + e.getMessage());
				}
				return;
			default:
				throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "Entity not found - " + uri);
			}

			logger.debug((ret != null) ? ret.toString() : "");
			Gson gson = new GsonBuilder().setDateFormat(("dests".equals(act) || "users".equals(act)) ? "dd.MM.yyyy" : "dd.MM.yyyy HH:mm z").create();
			responseJson(resp, gson.toJson(ret));
		} catch (ApiException e) {
			logger.error(String.valueOf(e.getErrorCode()), e);
			try {
				resp.setCharacterEncoding("UTF-8");
				resp.sendError(e.getErrorCode(), e.getMessage());
				resp.flushBuffer();
			} catch (IOException e1) {
				logger.warn(e1.getClass() + ": " + e1.getMessage());
			}
		} catch (Exception e) {
			logger.error("Внутренняя ошибка сервера", e);
			try {
				resp.setCharacterEncoding("UTF-8");
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
				resp.flushBuffer();
			} catch (IOException e1) {
				logger.warn(e1.getClass() + ": " + e1.getMessage());
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		dispatch(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		dispatch(req, resp);
	}

}