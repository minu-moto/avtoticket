/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.server.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.DB;
import com.avtoticket.server.db.stored.StoredProcs;
import com.avtoticket.server.rpc.TicketServiceImpl;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.DocType;
import com.avtoticket.shared.models.core.Gender;
import com.avtoticket.shared.models.core.Nationality;
import com.avtoticket.shared.models.core.Passage;
import com.avtoticket.shared.models.core.Station;
import com.avtoticket.shared.models.core.Ticket;
import com.avtoticket.shared.models.core.TicketStatus;
import com.avtoticket.shared.models.core.User;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.HttpHeaders;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 13 дек. 2015 г. 22:57:49
 */
public class OutUtil {

	private static Logger logger = LoggerFactory.getLogger(OutUtil.class);

	private static Cache<String, User> msession = CacheBuilder
			.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
	public static final Cache<String, Passage> psgcache = CacheBuilder
			.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();

	public static OutWrapper<String> register(HttpServletRequest req) {
		try {
			String login = req.getParameter("login");
			String pass = req.getParameter("pass");
			if (StoredProcs.core.isLoginFree(login)) {
				if (!CommonServerUtils.isValidEmail(login))
					return new OutWrapper<String>(22, "Неправильно указан E-mail");
				String code = RandomStringUtils.randomNumeric(6);
				StoredProcs.core.registerUser(login, pass, code);
				UserUtil.sendRegConfirm(req, login, pass, code);
				return new OutWrapper<String>("Регистрация прошла успешно. В ближайшее время на указанный адрес будет отправлено письмо с дальнейшими инструкциями.");
			} else
				return new OutWrapper<String>(22, "Указанный логин уже используется в системе");
		} catch (Exception e) {
			logger.error("", e);
			return new OutWrapper<String>(1, "Внутренняя ошибка сервера");
		}
	}

	public static OutWrapper<String> restore(HttpServletRequest req) {
		try {
			String login = req.getParameter("login");
			UserUtil.restorePassword(req, login);
			return new OutWrapper<String>("Пароль выслан на указанный адрес электронной почты");
		} catch (SerializationException e) {
			return new OutWrapper<String>(4, e.getMessage());
		} catch (Exception e) {
			logger.error("", e);
			return new OutWrapper<String>(1, "Внутренняя ошибка сервера");
		}
	}

	public static OutWrapper<String> changePass(HttpServletRequest req) {
		try {
			if (check(req)) {
				touch(req);
				String ssid = req.getParameter("ssid");
				String login = msession.getIfPresent(ssid).getLogin();
				String oldpass = req.getParameter("oldpass");
				String newpass = req.getParameter("newpass");
				if (StoredProcs.core.changePassword(login, oldpass, newpass))
					return new OutWrapper<String>("Пароль успешно изменен");
				else
					return new OutWrapper<String>(5, "Не удалось изменить пароль");
			} else
				return new OutWrapper<String>(3, "Сессия просрочена, необходима авторизация");
		} catch (Exception e) {
			logger.error("", e);
			return new OutWrapper<String>(1, "Внутренняя ошибка сервера");
		}
		
	}

	public static OutWrapper<String> login(HttpServletRequest req, HttpServletResponse resp) {
		try {
			String login = req.getParameter("login");
			String pass = req.getParameter("pass");
			Long uid = StoredProcs.core.getUser(login, pass);
			if (uid == null)
				return new OutWrapper<String>(2, "Неправильное сочетание логина и пароля");
			String ssid = RandomStringUtils.random(16, true, true);
			while (msession.getIfPresent(ssid) != null)
				ssid = RandomStringUtils.random(16, true, true);
			msession.put(ssid, DB.getModel(User.class, uid));
			UserUtil.login(login, pass, false, req, resp);
			return new OutWrapper<String>(ssid);
		} catch (Exception e) {
			logger.error("", e);
			return new OutWrapper<String>(1, "Внутренняя ошибка сервера");
		}
	}

	private static void touch(HttpServletRequest req) {
		String ssid = req.getParameter("ssid");
		if ((ssid != null) && !ssid.isEmpty())
			msession.getIfPresent(ssid);
	}

	private static boolean check(HttpServletRequest req) {
		String ssid = req.getParameter("ssid");
		if ((ssid != null) && !ssid.isEmpty())
			return msession.getIfPresent(ssid) != null;
		else
			return false;
	}

	public static OutWrapper<User> getUser(HttpServletRequest req) {
		if (check(req)) {
			String ssid = req.getParameter("ssid");
			touch(req);
			User ret = new User();
			ret.fill(msession.getIfPresent(ssid));
			ret.remove(User.IS_ADMIN);
			ret.remove(User.REG_DATE);
			if (ret.getBirthday() != null)
				ret.set("birthdate", ret.getBirthday());
			ret.remove(User.B_DATE);
			ret.remove(User.CLASS_NAME);
			ret.set(User.GENDER, (ret.getGender() == Gender.MALE) ? "м" : (ret.getGender() == Gender.FEMALE) ? "ж" : "");
			if (ret.getVdate() != null)
				ret.set(User.V_DATE, Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(ret.getVdate())));
			return new OutWrapper<User>(ret);
		} else
			return new OutWrapper<User>(3, "Сессия просрочена, необходима авторизация");
	}

	public static OutWrapper<String> setUser(HttpServletRequest req) {
		if (check(req)) {
			try {
				String ssid = req.getParameter("ssid");
				String firstname = req.getParameter("fname");
				String lastname = req.getParameter("lname");
				String middlename = req.getParameter("mname");
				String number = req.getParameter("number");
				String serial = req.getParameter("serial");
				SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
				Date bdate = df.parse(req.getParameter("bdate"));
				Date vdate = df.parse(req.getParameter("vdate"));
				String bplace = req.getParameter("bplace");
				String phone = req.getParameter("phone");
				String gender = req.getParameter("pol");
				User usr = msession.getIfPresent(ssid);
				Long doctype = Long.parseLong(req.getParameter("doctype"));
				Long grajd = Long.parseLong(req.getParameter("grajd"));
				usr.setFirstName(firstname);
				usr.setLastName(lastname);
				usr.setMiddleName(middlename);
				usr.setPaspNumber(number);
				usr.setPaspSeriya(serial);
				usr.setBirthday(bdate);
				usr.setBplace(bplace);
				usr.setPhone(phone);
				usr.setDocType(doctype);
				DocType dt = DB.getModel(DocType.class, doctype);
				usr.setDocument((dt != null) ? dt.getDisplayField() : "");
				usr.setGrajd(grajd);
				Nationality n = DB.getModel(Nationality.class, grajd);
				usr.setGrajdName((n != null) ? n.getDisplayField() : "");
				usr.setVdate(vdate);
				usr.setGender("м".equalsIgnoreCase(gender) ? Gender.MALE : ("ж".equalsIgnoreCase(gender) ? Gender.FEMALE : null));
				DB.save(usr, usr.getId());

				msession.put(ssid, usr);
				return new OutWrapper<String>("Обновление данных пользователя прошло успешно");
			} catch (Exception e) {
				logger.error("", e);
				return new OutWrapper<String>(1, "Внутренняя ошибка сервера");
			}
		} else {
			return new OutWrapper<String>(3, "Сессия просрочена, необходима авторизация");
		}
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

	public static OutWrapper<List<Station>> getDest(HttpServletRequest req) {
		try {
			touch(req);
			Long dep = Long.valueOf(req.getParameter("dep"));
			Map<Station, List<Station>> stations = TicketServiceImpl.getStationsBase();
			List<Station> ret = null;
			for (Station s : stations.keySet())
				if (dep.equals(s.getId())) {
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
			return new OutWrapper<List<Station>>(ret);
		} catch (Exception e) {
			logger.error("", e);
			return new OutWrapper<List<Station>>(1, "Внутренняя ошибка сервера");
		}
	}

	public static OutWrapper<List<Station>> getDep(HttpServletRequest req) {
		try {
			touch(req);
			List<Station> ret = DB.getKeyValueList(Station.class, new String[] {Station.ID, Station.NAME}, Where.equals(Station.IS_DEPARTURE_POINT, true), null);
			ret.forEach(s -> {
				s.remove(Station.VALUE_FIELD);
				s.remove(Station.DISPLAY_FIELD);
				s.remove(Station.CLASS_NAME);
			});
			return new OutWrapper<List<Station>>(ret);
		} catch (Exception e) {
			logger.error("", e);
			return new OutWrapper<List<Station>>(1, "Внутренняя ошибка сервера");
		}
	}

	public static OutWrapper<List<Passage>> getPassages(HttpServletRequest req, HttpSession session) {
		try {
			touch(req);
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
			Long depId = Long.valueOf(req.getParameter("dep"));
			Long destId = Long.valueOf(req.getParameter("dest"));
			Date date = df.parse(req.getParameter("date"));
			if (date.getTime() < df.parse(df.format(new Date())).getTime())
				return new OutWrapper<List<Passage>>(11, "Некорректная дата");
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
				psgcache.put(psg.getId() + "_" + df.format(psg.getDeparture()) + "_" + session.getId(), psg);

				p.set(Passage.DEPARTURE + "_date", p.getDeparture());
				p.set(Passage.ARRIVAL + "_date", p.getArrival());
				List<Long> slist = new ArrayList<Long>();
				if (p.getSaleSeatList() != null)
					for (BaseModel seat : p.getSaleSeatList())
						if ((seat != null) && !seat.isEmpty())
							slist.add(seat.getLongProp("name"));
				p.setSaleSeats(slist);
				p.remove(Passage.DEPARTURE);
				p.remove(Passage.ARRIVAL);
				p.remove(Passage.SALE_SEAT_LIST);
				p.remove(Passage.CLASS_NAME);
			}
			return new OutWrapper<List<Passage>>(ret);
		} catch (Exception e) {
			logger.error("", e);
			return new OutWrapper<List<Passage>>(1, "Внутренняя ошибка сервера");
		}
	}

	public static OutWrapper<String> sellTicket(HttpServletRequest req, HttpSession session) {
		try {
			touch(req);
			String ssid = req.getParameter("ssid");
			String firstname = req.getParameter("fname");
			String lastname = req.getParameter("lname");
			String middlename = req.getParameter("mname");
			String number = req.getParameter("number");
			String serial = req.getParameter("serial");
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
			Date bdate = req.getParameter("bdate") == null ? null : df.parse(req.getParameter("bdate"));
			Date vdate = req.getParameter("vdate") == null ? null : df.parse(req.getParameter("vdate"));
			String bplace = req.getParameter("bplace");
			String phone = req.getParameter("phone");
			Long doctype = Long.parseLong(req.getParameter("doctype"));
			Long grajd = Long.parseLong(req.getParameter("grajd"));
			String gender = req.getParameter("gender");
			User usr = new User();
			if (check(req))
				usr.fill(msession.getIfPresent(ssid));
			if ((firstname != null) && !firstname.isEmpty())
				usr.setFirstName(firstname);
			if ((lastname != null) && !lastname.isEmpty())
				usr.setLastName(lastname);
			if ((middlename != null) && !middlename.isEmpty())
				usr.setMiddleName(middlename);
			if ((number != null) && !number.isEmpty())
				usr.setPaspNumber(number);
			if ((serial != null) && !serial.isEmpty())
				usr.setPaspSeriya(serial);
			if (bdate != null)
				usr.setBirthday(bdate);
			if ((bplace != null) && !bplace.isEmpty())
				usr.setBplace(bplace);
			if ((phone != null) && !phone.isEmpty())
				usr.setPhone(phone);
			if (vdate != null)
				usr.setVdate(vdate);
			if (doctype != null)
				usr.setDocType(doctype);
			if (grajd != null)
				usr.setGrajd(grajd);
			if ((gender != null) && !gender.isEmpty())
				usr.setGender("м".equalsIgnoreCase(gender) ? Gender.MALE : ("ж".equalsIgnoreCase(gender) ? Gender.FEMALE : null));
			Long psgid = Long.parseLong(req.getParameter("psgid"));
			String date = req.getParameter("date");
			Passage psg = psgcache.getIfPresent(psgid + "_" + date + "_" + session.getId());
			if ((psg == null) || psg.isEmpty()) {
				logger.error("no passage found for: " + psgid + "_" + date);
				return new OutWrapper<String>(30, "Сессия покупки билета истекла. Повторите поиск рейса.");
			}
			Long tkt = 1L;//req.getParameter("tkt")==null?0:Long.parseLong(req.getParameter("tkt"));
			Long chtkt = 0L;//req.getParameter("chtkt")==null?0:Long.parseLong(req.getParameter("chtkt"));
			Long bags = req.getParameter("bags") == null ? 0 : Long.parseLong(req.getParameter("bags"));
			usr.setBags(bags);
			if (tkt + chtkt == 0) {
				logger.warn("0 seats requested");
				return new OutWrapper<String>(40, "");
			}
//			String url = AvProtocolUtil.buyTicket(tkt, chtkt, bags, usr, ip, psg);
//			String url = AvProtocolUtil.buyTicketUec(usr, psg, bags);
			String url = TicketServiceImpl.buyTickets(req, req.getHeader(HttpHeaders.USER_AGENT), usr, Arrays.asList(usr), psg, true);
			return new OutWrapper<String>(url);
		} catch (Exception e) {
			logger.error("", e);
			return new OutWrapper<String>(1, "Внутренняя ошибка сервера");
		}
	}

	public static OutWrapper<List<DocType>> getDocTypes(HttpServletRequest req) {
		try {
			touch(req);
			List<DocType> ret = DB.getModels(DocType.class, new Where(), DocType.ID);
			ret.forEach(dt -> {
				dt.remove(Nationality.DISPLAY_FIELD);
				dt.remove(Nationality.CLASS_NAME);
			});
			return new OutWrapper<List<DocType>>(ret);
		} catch (Exception e) {
			logger.error("",e);
			return new OutWrapper<List<DocType>>(1, "Внутренняя ошибка сервера");
		}
	}

	public static OutWrapper<List<Nationality>> getGrajd(HttpServletRequest req) {
		try {
			touch(req);
			List<Nationality> ret = DB.getModels(Nationality.class, new Where(), Nationality.MAIN + " desc, " + Nationality.NAME);
			ret.forEach(n -> {
				n.remove(Nationality.DISPLAY_FIELD);
				n.remove(Nationality.CLASS_NAME);
			});
			return new OutWrapper<List<Nationality>>(ret);
		} catch (Exception e) {
			logger.error("", e);
			return new OutWrapper<List<Nationality>>(1, "Внутренняя ошибка сервера");
		}
	}

	public static OutWrapper<List<Ticket>> getUserTickets(HttpServletRequest req) {
		try {
			touch(req);
			if (!check(req))
				return new OutWrapper<List<Ticket>>(3, "Сессия просрочена, необходима авторизация");
			String ssid = req.getParameter("ssid");
			Long usrid = msession.getIfPresent(ssid).getId();
			return new OutWrapper<List<Ticket>>(StoredProcs.core.getTickets(usrid, TicketStatus.SOLD, null));
		} catch (Exception e) {
			logger.error("", e);
			return new OutWrapper<List<Ticket>>(1, "Внутренняя ошибка сервера");
		}
	}

	public static OutWrapper<List<Station>> getStations(HttpServletRequest req) {
		try {
			touch(req);
			return new OutWrapper<List<Station>>(DB.getModels(Station.class, Where.isNotNull(Station.LAT).andIsNotNull(Station.LNG), null, false, false));
		} catch (Exception e) {
			logger.error("", e);
			return new OutWrapper<List<Station>>(1, "Внутренняя ошибка сервера");
		}
	}

	public static OutWrapper<List<Ticket>> getHistory(HttpServletRequest req) {
		OutWrapper<List<Ticket>> ret = getUserTickets(req);
		String limit = req.getParameter("limit");
		int lim = 15;
		try {
			lim = Integer.parseInt(limit);
		} catch (NumberFormatException ignored) { }
		if (ret.getContent() != null)
			ret.setContent(ret.getContent().stream().limit(lim).map(t -> {
				Ticket newT = new Ticket();
				newT.set("fromname", t.getFrom());
				newT.setFromId(t.getFromId());
				newT.set("toname", t.getTo());
				newT.setToId(t.getToId());
				newT.setSellTime(t.getSellTime());
				return newT;
			}).collect(Collectors.toList()));
		return ret;
	}

}