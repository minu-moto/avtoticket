/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.server.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
import com.avtoticket.server.utils.CommonServerUtils;
import com.avtoticket.server.utils.UserUtil;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.DocType;
import com.avtoticket.shared.models.core.Gender;
import com.avtoticket.shared.models.core.Nationality;
import com.avtoticket.shared.models.core.Passage;
import com.avtoticket.shared.models.core.Requisite;
import com.avtoticket.shared.models.core.SavedSession;
import com.avtoticket.shared.models.core.Station;
import com.avtoticket.shared.models.core.Ticket;
import com.avtoticket.shared.models.core.TicketStatus;
import com.avtoticket.shared.models.core.User;
import com.avtoticket.shared.utils.DateUtil;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 24 июля 2016 г. 1:46:16
 */
public class ApiServletV12 extends ExtHttpServlet {

	private static final long serialVersionUID = -3804917311407491620L;

	private static Logger logger = LoggerFactory.getLogger(ApiServletV12.class);

	private static final String GET = "GET";
	private static final String PUT = "PUT";
	private static final String POST = "POST";
	private static final String DELETE = "DELETE";
	private static final byte[] SECRET_KEY = "?T7e.)[cCzst7W[4".getBytes(StandardCharsets.UTF_8);

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
			dt.remove(DocType.VALUE_FIELD);
			dt.remove(DocType.DISPLAY_FIELD);
			dt.remove(DocType.CLASS_NAME);
		});
		return ret;
	}

	private static List<Nationality> getNations() throws Exception {
		List<Nationality> ret = DB.getModels(Nationality.class, new Where(), Nationality.MAIN + " desc, " + Nationality.NAME);
		ret.forEach(n -> {
			n.remove(Nationality.MAIN);
			n.remove(Nationality.VALUE_FIELD);
			n.remove(Nationality.DISPLAY_FIELD);
			n.remove(Nationality.CLASS_NAME);
			n.remove(Nationality.OKSM);
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

	private static boolean checkDatesOrder(List<Date> dates) {
		if ((dates == null) || dates.isEmpty())
			return false;
		Date date = dates.get(0);
		for (int i = 1; i < dates.size(); i++) {
			if (dates.get(i).getTime() - date.getTime() != 24 * 60 * 60 * 1000)
				return false;
			date = dates.get(i);
		}
		return true;
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
					if (checkDatesOrder(st.getDepDates())) {
						st.setRegularTo(st.getDepDates().get(st.getDepDates().size() - 1));
						st.remove(Station.DEP_DATES);
					}
				});
				break;
			}
		return ret;
	}

	private static List<Passage> getPassages(String sessionId, Long depId, Long destId, Date date) throws Exception {
		if (date.getTime() + 24 * 60 * 60 * 1000 <= new Date().getTime())
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Указана прошедшая дата");
		List<Passage> ret = TicketServiceImpl.getPassages(depId, date, destId, sessionId);
		Station dep = DB.getModel(Station.class, depId);
		Station dest = DB.getModel(Station.class, destId);
		Long childhood;
		try {
			childhood = Long.parseLong(StoredProcs.core.getProp("childhood"));
		} catch (Exception e) {
			childhood = null;
		}
		for (Passage p : ret) {
			Passage psg = new Passage();
			psg.fill(p);
			psg.setDepId(depId);
			psg.setDepName(dep.getName());
			psg.setDestId(destId);
			psg.setDestName(dest.getName());

//			p.set(Passage.DEPARTURE + "_date", p.getDeparture());
//			p.set(Passage.ARRIVAL + "_date", p.getArrival());
			p.set("is_transit", Objects.equals(p.getTranzit(), 1L));
			p.set("is_raft", p.getRaftFlag());
			p.set("total_seats", p.getTotalSeats());
			p.set("free_seats", p.getFreeSeats());
			p.set("bus_mark", p.getBusMark());
			p.set("bus_type", p.getBusType());
			p.set("childhood", childhood);
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
		t.set(Ticket.BIRTH_DATE, (t.getBirthDate() != null) ? df.format(t.getBirthDate()) : "");
		t.set("doc_issue", (t.getVdate() != null) ? df.format(t.getVdate()) : "");
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
		if (hash == null)
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный запрос - не указан идентификатор заказа");
		List<Ticket> ret = StoredProcs.core.getTickets(userId, null, hash);
		ret.forEach(ApiServletV12::cleanTicket);
		return ret;
	}

	private static String registerUser(HttpServletRequest req, String login, String lastname, String firstname, String middlename,
			Long docId, String docSeries, String docNumber, Date docIssue, String phone, Date birthdate, String gender, Long nationId, String ua, String ip) throws Exception {
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
		if (!UserUtil.regUser(req, login, pass))	// 	TODO подтверждение почты
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

		return auth(login, pass, ua, ip);
	}

	private static SavedSession getSession(String token) throws Exception {
		return getSession(token, true);
	}

	private static SavedSession getSession(String token, boolean throwException) throws Exception {
		UUID sessId = null;
		try {
			Jws<Claims> jwt = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
			Claims cl = jwt.getBody();
			sessId = UUID.fromString(cl.getId());
		} catch (Exception e) {
			if (throwException)
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный ключ доступа", e);
			else
				return null;
		}
		SavedSession sess = DB.getModel(SavedSession.class, Where.equals(SavedSession.SESSION_ID, sessId));
		if ((sess == null) && throwException)
			throw new ApiException(HttpServletResponse.SC_UNAUTHORIZED, "Сессия пользователя не найдена, требуется авторизация");
		return sess;
	}

	private static User getUser(String token) throws Exception {
		SavedSession sess = getSession(token);
		User user = DB.getModel(User.class, sess.getUserId());

		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		user.set(User.DOCUMENT, user.getDocType());
		user.set("nationality", user.getGrajd());
		user.set("birthdate", (user.getBirthday() != null) ? df.format(user.getBirthday()) : null);
		if (!user.getPaspSeriya().isEmpty())
			user.set("doc_series", user.getPaspSeriya());
		if (!user.getPaspNumber().isEmpty())
			user.set("doc_number", user.getPaspNumber());
		user.set("doc_issue", (user.getVdate() != null) ? df.format(user.getVdate()) : null);
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
		user.remove(User.API_TOKEN);
		return user;
	}

	private static void editUser(String token, Collection<String> params, String lastname, String firstname, String middlename,
			Long docId, String docSeries, String docNumber, Date docIssue, String phone, Date birthdate, Gender gender, Long nationId,
			String password, String oldPass) throws Exception {
		SavedSession sess = getSession(token);
		User user = DB.getModel(User.class, sess.getUserId());

		if (params.contains(User.LASTNAME))
			user.setLastName(lastname);
		if (params.contains(User.FIRSTNAME))
			user.setFirstName(firstname);
		if (params.contains(User.MIDDLENAME))
			user.setMiddleName(middlename);
		if (params.contains("document"))
			user.setDocType(docId);
		if (params.contains("doc_series"))
			user.setPaspSeriya(docSeries);
		if (params.contains("doc_number"))
			user.setPaspNumber(docNumber);
		if (params.contains("doc_issue"))
			user.setVdate(docIssue);
		if (params.contains(User.PHONE))
			user.setPhone(phone);
		if (params.contains("birthdate"))
			user.setBirthday(birthdate);
		if (params.contains(User.GENDER))
			user.setGender(gender);
		if (params.contains("nationality"))
			user.setGrajd(nationId);
		if (params.contains("password")) {
			if ((password == null) || password.isEmpty())
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Пароль не может быть пустым");
			if (!params.contains("current_password"))
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Не указан текущий пароль");
			if (!StoredProcs.core.changePassword(user.getLogin(), oldPass, password))
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Не верно указан текущий пароль");
		}
		DB.save(user, -1L);
	}

	private static List<Requisite> getRequisites(String token) throws Exception {
		SavedSession sess = getSession(token);
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		List<Requisite> ret = DB.getModels(Requisite.class, Where.equals(Requisite.TUSER_ID, sess.getUserId()), Requisite.DISPLAY_FIELD);
		ret.forEach(r -> {
			r.set(Requisite.DOCUMENT, r.getDocType());
			r.set("nationality", r.getGrajd());
			if (r.getBirthday() != null)
				r.set("birthdate", df.format(r.getBirthday()));
			if (!r.getPaspSeriya().isEmpty())
				r.set("doc_series", r.getPaspSeriya());
			if (!r.getPaspNumber().isEmpty())
				r.set("doc_number", r.getPaspNumber());
			if (r.getVdate() != null)
				r.set("doc_issue", df.format(r.getVdate()));
			r.remove(Requisite.TUSER_ID);
			r.remove(Requisite.CLASS_NAME);
			r.remove(Requisite.GRAJDNAME);
			r.remove(Requisite.GRAJD);
			r.remove(Requisite.DOCTYPE);
			r.remove(Requisite.B_DATE);
			r.remove(Requisite.V_DATE);
			r.remove(Requisite.PASP_SERIYA);
			r.remove(Requisite.PASP_NUMBER);
			r.remove(Requisite.VALUE_FIELD);
			r.remove(Requisite.DISPLAY_FIELD);
		});
		return ret;
	}

	private static void editRequisites(String token, Long id, Collection<String> params, String lastname, String firstname, String middlename,
			Long docId, String docSeries, String docNumber, Date docIssue, String phone, Date birthdate, Gender gender, Long nationId) throws Exception {
		SavedSession sess = getSession(token);
		Requisite req = DB.getModel(Requisite.class, Where.equals(Requisite.ID, id).andEquals(Requisite.TUSER_ID, sess.getUserId()));
		if (req == null)
			throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "Шаблон #" + id + " не найден");

		if (params.contains(Requisite.LASTNAME))
			req.setLastName(lastname);
		if (params.contains(Requisite.FIRSTNAME))
			req.setFirstName(firstname);
		if (params.contains(Requisite.MIDDLENAME))
			req.setMiddleName(middlename);
		if (params.contains("document"))
			req.setDocType(docId);
		if (params.contains("doc_series"))
			req.setPaspSeriya(docSeries);
		if (params.contains("doc_number"))
			req.setPaspNumber(docNumber);
		if (params.contains("doc_issue"))
			req.setVdate(docIssue);
		if (params.contains(Requisite.PHONE))
			req.setPhone(phone);
		if (params.contains("birthdate"))
			req.setBirthday(birthdate);
		if (params.contains(Requisite.GENDER))
			req.setGender(gender);
		if (params.contains("nationality"))
			req.setGrajd(nationId);
		DB.save(req, -1L);
	}

	private static void deleteRequisites(String token, Long id) throws Exception {
		SavedSession sess = getSession(token);
		Requisite req = DB.getModel(Requisite.class, Where.equals(Requisite.ID, id).andEquals(Requisite.TUSER_ID, sess.getUserId()));
		if (req == null)
			throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "Шаблон #" + id + " не найден");
		DB.delete(req);
	}

	private static void addRequisites(String token, String lastname, String firstname, String middlename,
			Long docId, String docSeries, String docNumber, Date docIssue, String phone, Date birthdate, String gender, Long nationId) throws Exception {
		SavedSession sess = getSession(token);

		if ((lastname == null) || lastname.isEmpty())
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный запрос - не указана фамилия пассажира");
		if ((firstname == null) || firstname.isEmpty())
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный запрос - не указано имя пассажира");
		if ((middlename == null) || middlename.isEmpty())
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный запрос - не указано отчество пассажира");
		if ((phone == null) || phone.isEmpty())
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный запрос - не указан телефон пассажира");
		if (birthdate == null)
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный запрос - не указана дата рождения пассажира");
		if (nationId == null)
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный запрос - не указана национальность пассажира");
		if (docId == null)
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный запрос - не указан тип документа");
		if ((docSeries == null) || docSeries.isEmpty())
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный запрос - не указана серия документа");
		if ((docNumber == null) || docNumber.isEmpty())
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный запрос - не указан номер документа");
		if (docIssue == null)
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Некорректный запрос - не указана дата выдачи документа");
		Gender sex = null;
		if ((gender != null) && !gender.isEmpty())
			try {
				sex = Gender.valueOf(gender.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Некорректный запрос - значение перечисления '" + gender + "' не существует", e);
			}

		Requisite req = new Requisite();
		req.setUserId(sess.getUserId());
		req.setBirthday(birthdate);
		req.setDocType(docId);
		req.setLastName(lastname);
		req.setFirstName(firstname);
		req.setMiddleName(middlename);
		req.setGender(sex);
		req.setGrajd(nationId);
		req.setPaspNumber(docNumber);
		req.setPaspSeriya(docSeries);
		req.setPhone(phone);
		req.setVdate(docIssue);
		DB.save(req, -1L);
	}

	private static String auth(String login, String password, String ua, String ip) throws Exception {
		Long uid = StoredProcs.core.getUser(login, password);
		if (uid == null)
			throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "Пользователь с такими реквизитами не найден");

		String uuid = UUID.randomUUID().toString();
		SavedSession ss = new SavedSession();
		ss.setLastLogin(new Date());
		ss.setUserId(uid);
		ss.setSessionId(new com.avtoticket.shared.models.UUID(uuid));
		ss.setUserAgent(ua);
		ss.setUserIp(ip);
		DB.save(ss, uid);

		return Jwts.builder()
			.setId(uuid)
			.setIssuedAt(ss.getLastLogin())
		  	.signWith(SignatureAlgorithm.HS512, SECRET_KEY)
		  	.compact();
	}

	private static BaseModel getTickets(String token, TicketStatus status, Long offset, Long limit) throws Exception {
		SavedSession sess = getSession(token);

		BaseModel ret = new BaseModel();
		ret.remove(BaseModel.CLASS_NAME);
		List<Ticket> tickets = StoredProcs.core.getTickets(sess.getUserId(), status, null);
		if (tickets == null)
			return ret;
		ret.set("items_count", tickets.size());
		List<Ticket> tkts = tickets.subList(Math.max(0, Math.min(tickets.size(), (offset != null) ? offset.intValue() : 0)),
				Math.max(0, Math.min(tickets.size(), ((offset != null) ? offset.intValue() : 0) + ((limit != null) ? limit.intValue() : tickets.size()))));
		tkts.forEach(ApiServletV12::cleanTicket);
		ret.set("items", tkts);
		return ret;
	}

	private static void deleteTicket(String token, Long id) throws Exception {
		SavedSession sess = getSession(token);

		Ticket ticket = DB.getModel(Ticket.class, Where.equals(Ticket.ID, id)
				.andEquals(Ticket.USER_ID, sess.getUserId())
				.andWhere(Where.equals(Ticket.IS_HIDDEN, false).orIsNull(Ticket.IS_HIDDEN)));
		if (ticket == null)
			throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "Билет #" + id + " не найден");
		ticket.setHidden(true);
		DB.save(ticket, sess.getUserId());
	}

	private static String sellTicket(HttpServletRequest req, String userAgent, String token, String cacheKey, String successUrl, String failUrl, String usrs) throws Exception {
		SavedSession sess = getSession(token, false);
		User customer = null;
		if (sess != null)
			customer = DB.getModel(User.class, sess.getUserId());

		Long childhood;
		try {
			childhood = Long.parseLong(StoredProcs.core.getProp("childhood"));
		} catch (Exception e) {
			childhood = null;
		}
		Passage psg = TicketServiceImpl.getPassage(cacheKey);
		if (psg == null)
			throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "Не найден указанный рейс. Повторите поиск.");
		Gson gson = new GsonBuilder().setDateFormat("dd.MM.yyyy").create();
		List<User> users = gson.fromJson(usrs, new TypeToken<List<User>>() {
		}.getType());
		if ((users == null) || users.isEmpty())
			throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Список пассажиров пуст");
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
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Не указана фамилия пассажира");
			if (u.getFirstName().trim().isEmpty())
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Не указано имя пассажира");
			if (u.getMiddleName().trim().isEmpty())
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Не указано отчество пассажира");
			if (u.getBirthday() == null)
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Не указан день рождения пассажира");
			if (u.getDocType() == null)
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Не указан документ пассажира");
			if (u.getPaspSeriya().trim().isEmpty())
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Не указана серия документа");
			if (u.getPaspNumber().trim().isEmpty())
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Не указан номер документа");
			if (u.getVdate() == null)
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Не указана дата выдачи документа");
			if (u.getPhone().trim().isEmpty())
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Не указан телефон пассажира");
			if (u.getGrajd() == null)
				throw new ApiException(HttpServletResponse.SC_FORBIDDEN, "Не указано гражданство пассажира");

			u.setIsChild((childhood != null) && (DateUtil.getAge(u.getBirthday(), psg.getDeparture(), 2) <= childhood));
		}
		return TicketServiceImpl.buyTickets(req, userAgent, (customer != null) ? customer : users.get(0), users, psg, true);
	}

	private static void dispatch(HttpServletRequest req, HttpServletResponse resp) {
		String act = null;
		Object ret = null;
		try {
			req.setCharacterEncoding("UTF-8");
			resp.setCharacterEncoding("UTF-8");
			String uri = req.getRequestURI();
			logger.debug(req.getMethod() + " " + uri);

			act = uri.substring(uri.lastIndexOf('/') + 1);
			switch (act) {
			case "docs":
				if (GET.equalsIgnoreCase(req.getMethod()))
					ret = getDocTypes();
				else
					throw new ApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Метод недоступен для данного ресурса");
				break;
			case "nations":
				if (GET.equalsIgnoreCase(req.getMethod()))
					ret = getNations();
				else
					throw new ApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Метод недоступен для данного ресурса");
				break;
			case "deps":
				if (GET.equalsIgnoreCase(req.getMethod()))
					ret = getDeps();
				else
					throw new ApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Метод недоступен для данного ресурса");
				break;
			case "dests":
				if (GET.equalsIgnoreCase(req.getMethod()))
					ret = getDests(parseLong(req, "dep_id"));
				else
					throw new ApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Метод недоступен для данного ресурса");
				break;
			case "passages":
				if (GET.equalsIgnoreCase(req.getMethod()))
					ret = getPassages(req.getSession().getId(), parseLong(req, "dep_id"), parseLong(req, "dest_id"), parseDate(req, "date"));
				else
					throw new ApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Метод недоступен для данного ресурса");
				break;
			case "orders":
				if (GET.equalsIgnoreCase(req.getMethod()))
					ret = getTickets(null, req.getParameter("id"));
				else if (POST.equalsIgnoreCase(req.getMethod())) {
					String redirect = sellTicket(req, req.getHeader(HttpHeaders.USER_AGENT), req.getParameter("access_token"), req.getParameter("cache_key"),
							req.getParameter("success_url"), req.getParameter("fail_url"), req.getParameter("passengers"));
					logger.debug(redirect);
					try {
						resp.sendRedirect(redirect);
						resp.flushBuffer();
					} catch (IOException e) {
						logger.warn(e.getClass() + ": " + e.getMessage());
					}
					return;
				} else
					throw new ApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Метод недоступен для данного ресурса");
				break;
			case "user":
				if (GET.equalsIgnoreCase(req.getMethod()))
					ret = getUser(req.getParameter("access_token"));
				else if (POST.equalsIgnoreCase(req.getMethod())) {
					String remoteAddr = req.getHeader(HttpHeaders.X_FORWARDED_FOR);
					if (remoteAddr == null)
						remoteAddr = req.getRemoteAddr();
					ret = registerUser(req, req.getParameter(User.LOGIN), req.getParameter(User.LASTNAME), req.getParameter(User.FIRSTNAME),
							req.getParameter(User.MIDDLENAME), parseLongOrNull(req, "document"), req.getParameter("doc_series"), req.getParameter("doc_number"),
							parseDateOrNull(req, "doc_issue"), req.getParameter(User.PHONE), parseDateOrNull(req, "birthdate"), req.getParameter(User.GENDER),
							parseLongOrNull(req, "nationality"), req.getHeader(HttpHeaders.USER_AGENT), remoteAddr);
				} else if (PUT.equalsIgnoreCase(req.getMethod())) {
					Collection<String> params = Collections.list(req.getParameterNames());
					Gender sex = null;
					if (params.contains(User.GENDER))
						try {
							sex = Gender.valueOf(req.getParameter(User.GENDER).toUpperCase());
						} catch (IllegalArgumentException e) {
							throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Некорректный запрос - значение перечисления '" + req.getParameter(User.GENDER) + "' не существует", e);
						}
					editUser(req.getParameter("access_token"), params, req.getParameter(User.LASTNAME),
							req.getParameter(User.FIRSTNAME), req.getParameter(User.MIDDLENAME), parseLongOrNull(req, "document"), req.getParameter("doc_series"),
							req.getParameter("doc_number"), parseDateOrNull(req, "doc_issue"), req.getParameter(User.PHONE), parseDateOrNull(req, "birthdate"),
							sex, parseLongOrNull(req, "nationality"), req.getParameter("password"), req.getParameter("current_password"));
				} else
					throw new ApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Метод недоступен для данного ресурса");
				break;
			case "tickets":
				if (GET.equalsIgnoreCase(req.getMethod())) {
					Collection<String> params = Collections.list(req.getParameterNames());
					TicketStatus status = null;
					if (params.contains(Ticket.TICKET_STATUS))
						try {
							status = TicketStatus.valueOf(req.getParameter(Ticket.TICKET_STATUS).toUpperCase());
						} catch (IllegalArgumentException e) {
							throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Некорректный запрос - значение перечисления '" + req.getParameter(Ticket.TICKET_STATUS) + "' не существует", e);
						}
					ret = getTickets(req.getParameter("access_token"), status, parseLongOrNull(req, "offset"), parseLongOrNull(req, "limit"));
				} else if (DELETE.equalsIgnoreCase(req.getMethod())) {
					deleteTicket(req.getParameter("access_token"), parseLongOrNull(req, Ticket.ID));
				} else
					throw new ApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Метод недоступен для данного ресурса");
				break;
			case "sessions":
				if (GET.equalsIgnoreCase(req.getMethod())) {
					String remoteAddr = req.getHeader(HttpHeaders.X_FORWARDED_FOR);
					if (remoteAddr == null)
						remoteAddr = req.getRemoteAddr();
					ret = auth(req.getParameter("login"), req.getParameter("password"), req.getHeader(HttpHeaders.USER_AGENT), remoteAddr);
				} else if (DELETE.equalsIgnoreCase(req.getMethod())) {
					SavedSession sess = getSession(req.getParameter("access_token"));
					DB.delete(sess);
				} else
					throw new ApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Метод недоступен для данного ресурса");
				break;
			case "templates":
				if (GET.equalsIgnoreCase(req.getMethod()))
					ret = getRequisites(req.getParameter("access_token"));
				else if (POST.equalsIgnoreCase(req.getMethod())) {
					addRequisites(req.getParameter("access_token"), req.getParameter(Requisite.LASTNAME), req.getParameter(Requisite.FIRSTNAME),
							req.getParameter(Requisite.MIDDLENAME), parseLongOrNull(req, "document"), req.getParameter("doc_series"), req.getParameter("doc_number"),
							parseDateOrNull(req, "doc_issue"), req.getParameter(Requisite.PHONE), parseDateOrNull(req, "birthdate"), req.getParameter(Requisite.GENDER),
							parseLongOrNull(req, "nationality"));
					responseJson(resp, HttpServletResponse.SC_CREATED, "");
					return;
				} else if (DELETE.equalsIgnoreCase(req.getMethod()))
					deleteRequisites(req.getParameter("access_token"), parseLongOrNull(req, Requisite.ID));
				else if (PUT.equalsIgnoreCase(req.getMethod())) {
					Collection<String> params = Collections.list(req.getParameterNames());
					Gender sex = null;
					if (params.contains(Requisite.GENDER))
						try {
							sex = Gender.valueOf(req.getParameter(Requisite.GENDER).toUpperCase());
						} catch (IllegalArgumentException e) {
							throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Некорректный запрос - значение перечисления '" + req.getParameter(Requisite.GENDER) + "' не существует", e);
						}
					editRequisites(req.getParameter("access_token"), parseLongOrNull(req, Requisite.ID), params, req.getParameter(Requisite.LASTNAME),
							req.getParameter(Requisite.FIRSTNAME), req.getParameter(Requisite.MIDDLENAME), parseLongOrNull(req, "document"), req.getParameter("doc_series"),
							req.getParameter("doc_number"), parseDateOrNull(req, "doc_issue"), req.getParameter(Requisite.PHONE), parseDateOrNull(req, "birthdate"),
							sex, parseLongOrNull(req, "nationality"));
					
				} else
					throw new ApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Метод недоступен для данного ресурса");
				break;
			default:
				throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "Entity not found - " + uri);
			}

			logger.debug((ret != null) ? ret.toString() : "");
			if ((ret != null) && (ret instanceof String))
				responseHtml(resp, (String) ret);
			else if (ret != null) {
				Gson gson = new GsonBuilder().setDateFormat(("dests".equals(act) || "users".equals(act)) ? "dd.MM.yyyy" : "dd.MM.yyyy HH:mm z").create();
				responseJson(resp, gson.toJson(ret));
			} else
				responseHtml(resp, "");
		} catch (ApiException e) {
			logger.error(String.valueOf(e.getErrorCode()), e);
			try {
				resp.sendError(e.getErrorCode(), e.getMessage());
				resp.flushBuffer();
			} catch (IOException e1) {
				logger.warn(e1.getClass() + ": " + e1.getMessage());
			}
		} catch (Exception e) {
			logger.error("Внутренняя ошибка сервера", e);
			try {
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

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		dispatch(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		dispatch(req, resp);
	}

}