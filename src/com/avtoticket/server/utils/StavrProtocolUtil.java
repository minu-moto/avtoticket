/*
 * Copyright Avtoticket (c) 2018.
 */
package com.avtoticket.server.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.stored.StoredProcs;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.core.DocType;
import com.avtoticket.shared.models.core.Gender;
import com.avtoticket.shared.models.core.Nationality;
import com.avtoticket.shared.models.core.Passage;
import com.avtoticket.shared.models.core.PassageType;
import com.avtoticket.shared.models.core.Station;
import com.avtoticket.shared.models.core.Ticket;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gwt.user.client.rpc.SerializationException;
import com.udojava.evalex.Expression;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 29 авг. 2018 г. 21:50:48
 */
public class StavrProtocolUtil {

	private static Logger logger = LoggerFactory.getLogger(StavrProtocolUtil.class);

	public static class StProtocolException extends Exception {

		private static final long serialVersionUID = 632025572466594071L;

		private int code;

		public StProtocolException(int code, String msg) {
			super(msg);
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}

	private static class Response<T extends BaseModel> {
		public int error;
		public String error_description;
		public List<T> result;
	}

	private static class Param {
		public String key;
		public Object value;

		public Param(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		public static Param set(String key, Object value) {
			return new Param(key, value);
		}
	}

	private static String URL = PropUtil.getStavrApiEndpoint();
	private static String LOGIN = PropUtil.getStavrApiLogin();
	private static String SKEY = PropUtil.getStavrApiPassword();
	private static final Charset ENCODING = StandardCharsets.UTF_8;

	private static final String COMMAND_FROM = "from";
	private static final String COMMAND_TO = "to";
	private static final String COMMAND_NATIONALITY = "nationality";
	private static final String COMMAND_TYPE_DOC = "type_doc";
	private static final String COMMAND_TRIP = "trip";
	private static final String COMMAND_BOOKING = "booking";
	private static final String COMMAND_PAY = "pay";

	public static final int DATES_RANGE = 5;

	private static String getJson(String url) throws SerializationException {
		try {
			return IOUtils.toString(new URL(url), ENCODING);
		} catch (MalformedURLException e) {
			logger.error("Произошла ошибка при получении данных " + url, e);
			throw new SerializationException("Внутренняя ошибка сервера");
		} catch (IOException e) {
			logger.error("Произошла ошибка ввода/вывода " + url, e);
			throw new SerializationException("Внутренняя ошибка сервера");
		} catch (Exception e) {
			logger.error(url, e);
			throw new SerializationException("Внутренняя ошибка сервера");
		}
	}

	private static <T extends BaseModel> List<T> getObjects(String command, Class<T> contentClass, Param... params) throws Exception {
		ObjectMapper mapper = new ObjectMapper().setDateFormat(new SimpleDateFormat("dd.MM.yyyy"));
		JavaType type = mapper.getTypeFactory().constructParametricType(Response.class, contentClass);

		String request = URL + "?user=" + LOGIN + "&skey=" + SKEY + "&command=" + command;
		if ((params != null) && (params.length > 0))
			request += "&" + Stream.of(params).map(p -> p.key + "=" + p.value).collect(Collectors.joining("&"));
		String json = getJson(request);

		Response<T> resp = mapper.readValue(json, type);
		if ((resp.error > 0) && (resp.error != 10)) {	// 10 - Запрос не вернул значений
			throw new StProtocolException(resp.error, resp.error_description);
		} else
			return resp.result;
	}

	public static List<Nationality> getNationalities() throws Exception {
		List<Nationality> res = getObjects(COMMAND_NATIONALITY, Nationality.class);
		if (res != null)
			res.forEach(n -> n.setOksm(n.getSourceStavId()));
		return res;
	}

	public static List<DocType> getDocTypes() throws Exception {
		List<DocType> res = getObjects(COMMAND_TYPE_DOC, DocType.class);
		if (res != null)
			res.forEach(dt -> dt.setId(dt.getSourceStavId()));
		return res;
	}

	public static List<Station> getFromStations() throws Exception {
		List<Station> res = getObjects(COMMAND_FROM, Station.class);
		if (res != null)
			res.forEach(s -> {
				s.setDeparturePoint(true);
				s.setStavId(s.getSourceStavFromId());
			});
		return res;
	}

	public static List<Station> getToStations(String fromId) throws Exception {
		List<Station> res = getObjects(COMMAND_TO, Station.class, Param.set("id_from", fromId));
		if (res != null)
			res.forEach(s -> s.setStavId(s.getSourceStavToId()));
		return res;
	}

	private static void fillExpression(Expression expr, Passage psg) {
		expr.setVariable("tarif", (psg.getDoubleProp(Passage.FULL_TICKET_PRICE) != null) ? BigDecimal.valueOf(psg.getDoubleProp(Passage.FULL_TICKET_PRICE)) : BigDecimal.ZERO)
			.and("child_tarif", (psg.getDoubleProp(Passage.CHILD_TICKET_PRICE) != null) ? BigDecimal.valueOf(psg.getDoubleProp(Passage.CHILD_TICKET_PRICE)) : BigDecimal.ZERO)
			.and("bag_tarif", (psg.getDoubleProp(Passage.BAGGAGE_PRICE) != null) ? BigDecimal.valueOf(psg.getDoubleProp(Passage.BAGGAGE_PRICE)) : BigDecimal.ZERO)
			.and("stav_markup_tarif", (psg.getDoubleProp(Passage.MARKUP_TICKET_PRICE) != null) ? BigDecimal.valueOf(psg.getDoubleProp(Passage.MARKUP_TICKET_PRICE)) : BigDecimal.ZERO)
			.and("stav_markup_child_tarif", (psg.getDoubleProp(Passage.MARKUP_CHILD_PRICE) != null) ? BigDecimal.valueOf(psg.getDoubleProp(Passage.MARKUP_CHILD_PRICE)) : BigDecimal.ZERO)
			.and("stav_markup_bag_tarif", (psg.getDoubleProp(Passage.MARKUP_BAGGAGE_PRICE) != null) ? BigDecimal.valueOf(psg.getDoubleProp(Passage.MARKUP_BAGGAGE_PRICE)) : BigDecimal.ZERO)
			.and("stav_booking_price", (psg.getDoubleProp(Passage.BOOKING_PRICE) != null) ? BigDecimal.valueOf(psg.getDoubleProp(Passage.BOOKING_PRICE)) : BigDecimal.ZERO);
	}

	public static List<Passage> getPassages(String depId, Date date, String destId) throws Exception {
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		List<Passage> res = getObjects(COMMAND_TRIP, Passage.class,
				Param.set("id_from", depId),
				Param.set("id_to", destId),
				Param.set("date_trip", df.format(date)));
		if (res != null) {
			SimpleDateFormat tripFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			Expression ap = new Expression(StoredProcs.core.getProp("stav_adult_price_formula"));
			Expression cp = new Expression(StoredProcs.core.getProp("stav_child_price_formula"));
			Expression bp = new Expression(StoredProcs.core.getProp("stav_bag_price_formula"));
			res = res.stream().map(p -> {
				Passage ret = new Passage();
				ret.setId(p.getLongProp(Passage.ID_TRIP));
				ret.setName(p.getStringProp(Passage.NAME_TRIP));
				ret.setFreeSeats(p.getLongProp(Passage.COUNT_AVAILABLE_SEATS_TRIP));
				ret.setStavDepId(depId);
				ret.setStavDestId(destId);
				try {
					ret.setDeparture(tripFormat.parse(p.getStringProp(Passage.DATE_TRIP) + " " + p.getStringProp(Passage.TIME_TRIP)));
					ret.setArrival(tripFormat.parse(p.getStringProp(Passage.DATE_ARRIVAL_TRIP) + " " + p.getStringProp(Passage.TIME_ARRIVAL_TRIP)));
				} catch (ParseException e) {
					logger.error("", e);
				}
				ret.setTariff(Math.round(p.getDoubleProp(Passage.FULL_TICKET_PRICE) * 100));
				ret.setPrefTariff(Math.round(p.getDoubleProp(Passage.CHILD_TICKET_PRICE) * 100));
				ret.setBagTariff(Math.round(p.getDoubleProp(Passage.BAGGAGE_PRICE) * 100));

				fillExpression(ap, p);
				ret.setSumm(ap.eval().multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).longValue());
				fillExpression(cp, p);
				ret.setChldSumm(cp.eval().multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).longValue());
				fillExpression(bp, p);
				ret.setBagSumm(bp.eval().multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).longValue());

				// запоминаем исходную цену билетов
				ret.set(Passage.TOTAL_FULL_TICKET_PRICE, Math.round(p.getDoubleProp(Passage.TOTAL_FULL_TICKET_PRICE) * 100));
				ret.set(Passage.TOTAL_CHILD_TICKET_PRICE, Math.round(p.getDoubleProp(Passage.TOTAL_CHILD_TICKET_PRICE) * 100));
				ret.set(Passage.TOTAL_BAGGAGE_PRICE, Math.round(p.getDoubleProp(Passage.TOTAL_BAGGAGE_PRICE) * 100));

				String[] seats = p.getStringProp(Passage.SEATS_TRIP).split("\\^");
				ret.setSaleSeatList(Stream.of(seats).map(s -> {
					BaseModel bm = new BaseModel();
					bm.set("name", s);
					return bm;
				}).collect(Collectors.toList()));
				ret.setPassageType(PassageType.STAVAVTO);
				return ret;
			}).collect(Collectors.toList());
		}
		return res;
	}

	private static String toMoney(Long money) {
		return money / 100 + "." + (money % 100 < 10 ? "0" : "") + money % 100;
	}

	public static String booking(List<Ticket> tickets, Passage psg) throws Exception {
		if (tickets.size() > 4)
			throw new StProtocolException(-1, "Возможно бронирование максимум 4 билетов за один раз");
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
		List<Param> params = new ArrayList<Param>(Arrays.asList(
				Param.set("date_trip", df.format(psg.getDeparture())),
				Param.set("time_trip", tf.format(psg.getDeparture())),
				Param.set("id_trip", psg.getId()),
				Param.set("id_from", psg.getStavDepId()),
				Param.set("id_to", psg.getStavDestId())));
		for (int i = 0; i < tickets.size(); ) {
			Ticket ticket = tickets.get(i);
			i++;
			params.add(Param.set("surname" + i, URLEncoder.encode(ticket.getLastname(), ENCODING.name())));
			params.add(Param.set("first_name" + i, URLEncoder.encode(ticket.getFirstname(), ENCODING.name())));
			params.add(Param.set("middle_name" + i, URLEncoder.encode(ticket.getMiddlename(), ENCODING.name())));
			params.add(Param.set("sex" + i, (ticket.getGender() == Gender.MALE) ? "1" : ((ticket.getGender() == Gender.FEMALE) ? "2" : "")));
			params.add(Param.set("category" + i, "п".equalsIgnoreCase(ticket.getTicketType()) ? "1" : ("д".equalsIgnoreCase(ticket.getTicketType()) ? "2" : "")));
			params.add(Param.set("dob" + i, df.format(ticket.getBirthDate())));
			params.add(Param.set("nationality" + i, URLEncoder.encode(ticket.getStavNationId(), ENCODING.name())));
			params.add(Param.set("number_of_baggage" + i, ticket.getBaggage()));
			params.add(Param.set("type_doc" + i, ticket.getStavDocTypeId()));
			params.add(Param.set("value_doc" + i, URLEncoder.encode(ticket.getSeriya().trim() + " " + ticket.getNumber().trim(), ENCODING.name())));
			params.add(Param.set("seat" + i, ticket.getSeat()));
			params.add(Param.set("total_ticket_price" + i, toMoney(psg.getLongProp("п".equalsIgnoreCase(ticket.getTicketType()) ? Passage.TOTAL_FULL_TICKET_PRICE : Passage.TOTAL_CHILD_TICKET_PRICE))));
			params.add(Param.set("total_baggage_price" + i, toMoney((ticket.getBaggage() > 0) ? psg.getLongProp(Passage.TOTAL_BAGGAGE_PRICE) : 0L)));
		}
		List<BaseModel> res = getObjects(COMMAND_BOOKING, BaseModel.class, params.toArray(new Param[params.size()]));
		if ((res != null) && !res.isEmpty())
			return res.get(0).getStringProp("id_booking");
		else
			return null;
	}

	public static String getPayUrl(String bookingId) {
		return URL + "?user=" + LOGIN + "&skey=" + SKEY + "&command=" + COMMAND_PAY + "&id_booking=" + bookingId;
	}

	public static List<Ticket> pay(String bookingId) throws Exception {
		List<Ticket> res = getObjects(COMMAND_PAY, Ticket.class, Param.set("id_booking", bookingId));
		if (res != null)
			;
		return res;
	}

}