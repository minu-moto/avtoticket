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
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.DB;
import com.avtoticket.server.db.stored.StoredProcs;
import com.avtoticket.server.rpc.TicketServiceImpl;
import com.avtoticket.server.servlet.ExtHttpServlet;
import com.avtoticket.server.utils.KpasProtocolUtil;
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
public class ApiServletV10 extends ExtHttpServlet {

	private static final long serialVersionUID = -3804917311407491620L;

	private static Logger logger = LoggerFactory.getLogger(ApiServletV10.class);

	public static final Cache<String, Passage> psgcache = CacheBuilder
			.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();

	private static long parseLong(HttpServletRequest req, String name) throws ApiException {
		String num = req.getParameter(name);
		try {
			return Long.parseLong(num);
		} catch (Exception e) {
			throw new ApiException(2, "Неверный формат целого числа - " + num);
		}
	}

	private static Date parseDate(HttpServletRequest req, String name) throws ApiException {
		String date = req.getParameter(name);
		try {
			return new SimpleDateFormat("dd.MM.yyyy").parse(date);
		} catch (Exception e) {
			throw new ApiException(3, "Неверный формат даты - " + date);
		}
	}

	private static OutWrapper<List<DocType>> getDocTypes() throws Exception {
		List<DocType> ret = DB.getModels(DocType.class, new Where(), DocType.ID);
		ret.forEach(dt -> {
			dt.remove(Nationality.DISPLAY_FIELD);
			dt.remove(Nationality.CLASS_NAME);
		});
		return new OutWrapper<List<DocType>>(ret);
	}

	private static OutWrapper<List<Nationality>> getNations() throws Exception {
		List<Nationality> ret = DB.getModels(Nationality.class, new Where(), Nationality.MAIN + " desc, " + Nationality.NAME);
		ret.forEach(n -> {
			n.remove(Nationality.MAIN);
			n.remove(Nationality.DISPLAY_FIELD);
			n.remove(Nationality.CLASS_NAME);
		});
		return new OutWrapper<List<Nationality>>(ret);
	}

	private static OutWrapper<List<Station>> getDeps() throws Exception {
		List<Station> ret = DB.getKeyValueList(Station.class, new String[] {Station.ID, Station.NAME}, Where.equals(Station.IS_DEPARTURE_POINT, true), null);
		ret.forEach(s -> {
			s.remove(Station.VALUE_FIELD);
			s.remove(Station.DISPLAY_FIELD);
			s.remove(Station.CLASS_NAME);
		});
		return new OutWrapper<List<Station>>(ret);
	}

	private static OutWrapper<List<Station>> getDests(Long depId) throws Exception {
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
		return new OutWrapper<List<Station>>(ret);
	}

	private static OutWrapper<List<Passage>> getPassages(Long depId, Long destId, Date date) throws Exception {
		if (date.getTime() + 24 * 60 * 60 * 1000 <= new Date().getTime())
			throw new ApiException(4, "Указана прошедшая дата");
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
			p.set("price0", p.getSumm());
			p.set("price1", p.getSumm() + p.getBagSumm());
			p.set("price2", p.getSumm() + p.getBagSumm() * 2);
			p.set("child_price0", p.getChldSumm());
			p.set("child_price1", p.getChldSumm() + p.getBagSumm());
			p.set("child_price2", p.getChldSumm() + p.getBagSumm() * 2);
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
		return new OutWrapper<List<Passage>>(ret);
	}

	private static OutWrapper<List<Ticket>> getTickets(String hash) throws Exception {
		List<Ticket> ret = StoredProcs.core.getTickets(null, null, hash);
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		ret.forEach(n -> {
			n.set("price", n.getAmount());
			n.set(Ticket.BIRTH_DATE, df.format(n.getBirthDate()));
			n.set("doc_issue", df.format(n.getVdate()));
			n.set("is_child", "д".equalsIgnoreCase(n.getTicketType()));
			n.set("is_baggage", n.isBaggage());
			n.set(Ticket.DOCUMENT, n.getDocTypeName());
			n.set(Ticket.ID, n.getTktNumber());
			n.set("doc_series", n.getSeriya());
			n.set("doc_number", n.getNumber());
			n.set("name", n.getReisName());
			n.set("sale_time", n.getSellTime());
			n.set("statement", n.getNomerVedomosti());
			n.set(Ticket.SEAT, n.isBaggage() ? "багажное"
					: (((n.getTBTarif() != null) && (n.getTBTarif() > 0L)) ? "б/м" : String.valueOf(n.getSeat())));
			n.remove(Ticket.CLASS_NAME);
			n.remove(Ticket.VALUE_FIELD);
			n.remove(Ticket.DOCTYPENAME);
			n.remove(Ticket.TBTARIF);
			n.remove(Ticket.KOM_SBOR);
			n.remove(Ticket.OB_STRAH);
			n.remove(Ticket.TARIF);
			n.remove(Ticket.TARIF_FACT);
			n.remove(Ticket.FROM_ID);
			n.remove(Ticket.TO_ID);
			n.remove(Ticket.PAROM);
			n.remove(Ticket.PAROM_TARIF);
			n.remove(Ticket.PAROM_TARIF_FACT);
			n.remove(Ticket.PAROM_TICKET_TYPE);
			n.remove(Ticket.BILL_ID);
			n.remove("ustarif");
			n.remove(Ticket.GRAJD);
			n.remove(Ticket.BAGTARIF);
			n.remove(Ticket.BAG1);
			n.remove(Ticket.BAG2);
			n.remove(Ticket.TICKET_TYPE);
			n.remove(Ticket.TKTNUMBER);
			n.remove(Ticket.SERIYA);
			n.remove(Ticket.NUMBER);
			n.remove(Ticket.REIS_NAME);
			n.remove(Ticket.ISBAGGAGE);
			n.remove(Ticket.V_DATE);
			n.remove(Ticket.SELL_TIME);
			n.remove(Ticket.NOMER_VEDOMOSTI);
		});
		return new OutWrapper<List<Ticket>>(ret);
	}

	private static OutWrapper<String> sellTicket(HttpServletRequest req, String userAgent, Long psgid, Date date, String successUrl, String failUrl, String usrs) throws Exception {
		Passage psg = psgcache.getIfPresent(psgid + "_" + new SimpleDateFormat("dd.MM.yyyy").format(date));
		if (psg == null)
			throw new ApiException(6, "Не найден указанный рейс. Повторите поиск.");
		Gson gson = new GsonBuilder().setDateFormat("dd.MM.yyyy").create();
		List<User> users = gson.fromJson(usrs, new TypeToken<List<User>>() {
		}.getType());
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		for (User u : users) {
			if (u.getStringProp(User.GENDER).trim().isEmpty())
				throw new ApiException(7, "Не указан пол пассажира");

			u.setBirthday(df.parse(u.getStringProp("birthdate")));
			u.setGender(Gender.valueOf(u.getStringProp(User.GENDER)));
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
				throw new ApiException(7, "Не указана фамилия пассажира");
			if (u.getFirstName().trim().isEmpty())
				throw new ApiException(7, "Не указано имя пассажира");
			if (u.getMiddleName().trim().isEmpty())
				throw new ApiException(7, "Не указано отчество пассажира");
			if (u.getBirthday() == null)
				throw new ApiException(7, "Не указан день рождения пассажира");
			if (u.getDocType() == null)
				throw new ApiException(7, "Не указан документ пассажира");
			if (u.getPaspSeriya().trim().isEmpty())
				throw new ApiException(7, "Не указана серия документа");
			if (u.getPaspNumber().trim().isEmpty())
				throw new ApiException(7, "Не указан номер документа");
			if (u.getVdate() == null)
				throw new ApiException(7, "Не указана дата выдачи документа");
			if (u.getPhone().trim().isEmpty())
				throw new ApiException(7, "Не указан телефон пассажира");
			if (u.getGrajd() == null)
				throw new ApiException(7, "Не указано гражданство пассажира");

			u.setIsChild(DateUtil.getAge(u.getBirthday(), psg.getDeparture()) < Integer.parseInt(StoredProcs.core.getProp("childhood")));
		}
		return new OutWrapper<String>(TicketServiceImpl.buyTickets(req, userAgent, users.get(0), users, psg, true));
	}

	private static void dispatch(HttpServletRequest req, HttpServletResponse resp) {
		String act = null;
		OutWrapper<?> ret = null;
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
				ret = getTickets(req.getParameter("id"));
				break;
			case "sell":
				ret = sellTicket(req, req.getHeader(HttpHeaders.USER_AGENT), parseLong(req, "id"), parseDate(req, "date"), req.getParameter("success_url"), req.getParameter("fail_url"), req.getParameter("passengers"));
				break;
			default:
				throw new ApiException(5, "Некорректный запрос" + " - " + uri);
			}
		} catch (ApiException e) {
			ret = new OutWrapper<String>(e.getErrorCode(), e.getMessage());
		} catch (Exception e) {
			logger.error("Внутренняя ошибка сервера", e);
			ret = new OutWrapper<String>(1, "Внутренняя ошибка сервера");
		}

		logger.debug((ret != null) ? ret.toString() : "");
		Gson gson = new GsonBuilder().setDateFormat("dests".equals(act) ? "dd.MM.yyyy" : "dd.MM.yyyy HH:mm z").create();
		responseJson(resp, gson.toJson(ret));
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