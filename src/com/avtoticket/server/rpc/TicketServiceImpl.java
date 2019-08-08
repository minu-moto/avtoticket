/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.server.rpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static java.util.stream.Collectors.*;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.DB;
import com.avtoticket.server.db.stored.StoredProcs;
import com.avtoticket.server.utils.KpasProtocolUtil;
import com.avtoticket.server.utils.PropUtil;
import com.avtoticket.server.utils.CommonServerUtils;
import com.avtoticket.server.utils.StavrProtocolUtil;
import com.avtoticket.server.utils.StavrProtocolUtil.StProtocolException;
import com.avtoticket.server.utils.UserUtil;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.PageContainer;
import com.avtoticket.shared.models.UUID;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.Bill;
import com.avtoticket.shared.models.core.DocType;
import com.avtoticket.shared.models.core.Help;
import com.avtoticket.shared.models.core.HelpType;
import com.avtoticket.shared.models.core.Locales;
import com.avtoticket.shared.models.core.Nationality;
import com.avtoticket.shared.models.core.News;
import com.avtoticket.shared.models.core.Passage;
import com.avtoticket.shared.models.core.PassageType;
import com.avtoticket.shared.models.core.Requisite;
import com.avtoticket.shared.models.core.Station;
import com.avtoticket.shared.models.core.StationLink;
import com.avtoticket.shared.models.core.Ticket;
import com.avtoticket.shared.models.core.TicketStatus;
import com.avtoticket.shared.models.core.User;
import com.avtoticket.shared.rpc.TicketService;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.HttpHeaders;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import ru.abyss.acquiring.soap.AcquiringUtil;
import ru.paymentgate.engine.webservices.merchant.RegisterOrderResponse;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 13 дек. 2015 г. 22:57:49
 */
public class TicketServiceImpl extends RemoteServiceServlet implements TicketService {

	private static final long serialVersionUID = -7447583364084957784L;

	private static Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class.getName());

	private static final int MAX_ERRORS = 10;
	/** Количество соседних мест в автобусе */
	private static final int ADJACENT_SEATS = 2;

	private static class PassageUniqKey {
		private long id;
		private long depId;
		private long destId;
		private String depDate;
		private String sessionId;

		public PassageUniqKey(Passage psg, String sessionId) {
			this(psg.getId(), psg.getDepId(), psg.getDestId(), new SimpleDateFormat("yyyyMMddHHmm").format(psg.getDeparture()), sessionId);
			psg.set("cache_key", toString());
		}

		public PassageUniqKey(long id, long depId, long destId, String depDate, String sessionId) {
			this.id = id;
			this.depId = depId;
			this.destId = destId;
			this.depDate = depDate;
			this.sessionId = sessionId;
		}

		@Override
		public int hashCode() {
			return (int) (depId + destId + id) + depDate.hashCode() + sessionId.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PassageUniqKey) {
				PassageUniqKey key = (PassageUniqKey) obj;
				return (key.depId == this.depId) && (key.destId == this.destId)
						&& (key.id == this.id) && key.depDate.equals(this.depDate) && key.sessionId.equals(this.sessionId);
			}
				return false;
		}

		@Override
		public String toString() {
			return DigestUtils.md5Hex(id + ";" + depId + ";" + destId + ";" + depDate + ";" + sessionId);
		}
	}

	private static Cache<String, Passage> passages = CacheBuilder
			.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();

	@Override
	public PageContainer<News> getNews(String locale, int limit, int offset) throws SerializationException {
		try {
			return DB.getPagedModels(News.class,
					Where.equals(News.LOCALE, Locales.valueOf(locale.toUpperCase())).limit(limit).offset(offset),
					News.DATE_CREATE + " desc", true);
		} catch (Exception e) {
			logger.error("Ошибка при получении новостей", e);
			throw new SerializationException("Ошибка при получении новостей");
		}
	}

	public static Map<Station, List<Station>> getStationsBase() throws SerializationException {
		try {
			// список станций
			List<Station> stations = DB.getModels(Station.class);
			if (stations == null)
				return null;
			// индексируем станции
			Map<Long, Station> stationsMap = stations.stream().collect(toMap(Station::getId, Function.identity()));
			// находим пункты отправления
			Set<Long> depIds = stations.stream().filter(Station::isDeparturePoint).map(Station::getId).collect(toSet());
			// список маршрутов
			List<StationLink> links = DB.getModels(StationLink.class, new Where(true).andEquals(StationLink.DEP_ID, depIds).andNotLess(StationLink.DATEOP, new Date()), false);
			// группируем маршруты по пунктам отправления/прибытия, собираем даты рейсов в список
			Map<Long, Map<Long, List<Date>>> linksMap = links.stream().collect(groupingBy(StationLink::getDepId, groupingBy(StationLink::getDestId, mapping(StationLink::getDateOp, toList()))));

			return linksMap.entrySet().stream().collect(toMap(e -> stationsMap.get(e.getKey()), e -> {
				return e.getValue().entrySet().stream().map(se -> {
					Station s = new Station();
					s.fill(stationsMap.get(se.getKey()));
					Collections.sort(se.getValue());
					s.setDepDates(se.getValue());
					return s;
				}).collect(toList());
			}));
		} catch (Exception e) {
			logger.error("Ошибка при получении автостанций", e);
			throw new SerializationException("Ошибка при получении автостанций");
		}
	}

	@Override
	public Map<Station, List<Station>> getStations() throws SerializationException {
		return getStationsBase();
	}

	@Override
	public Boolean login(String login, String password, boolean remember) throws SerializationException {
		return UserUtil.login(login, password, remember, getThreadLocalRequest(), getThreadLocalResponse());
	}

	@Override
	public void logout() throws SerializationException {
		UserUtil.logout(getThreadLocalRequest(), getThreadLocalResponse());
	}

	@Override
	public BaseModel getInitData() throws SerializationException {
		BaseModel ret = new BaseModel();
		User user = UserUtil.getUserFromSession(getThreadLocalRequest());
		ret.set("user", user);
		ret.set("server_timestamp", new Date());
		try {
			ret.set("service_mode", StoredProcs.core.getProp("service_mode"));
			ret.set("birthday_theme", StoredProcs.core.getProp("birthday_theme"));
			ret.set("birthday_mail", StoredProcs.core.getProp("birthday_mail"));
			ret.set("adult_price_formula", StoredProcs.core.getProp("adult_price_formula"));
			ret.set("child_price_formula", StoredProcs.core.getProp("child_price_formula"));
			ret.set("bag_price_formula", StoredProcs.core.getProp("bag_price_formula"));
			ret.set("stav_adult_price_formula", StoredProcs.core.getProp("stav_adult_price_formula"));
			ret.set("stav_child_price_formula", StoredProcs.core.getProp("stav_child_price_formula"));
			ret.set("stav_bag_price_formula", StoredProcs.core.getProp("stav_bag_price_formula"));
			ret.set("childhood", StoredProcs.core.getProp("childhood"));
			ret.set("production", PropUtil.isProduction());
			try {
				ret.set("sale_period", Long.valueOf(StoredProcs.core.getProp("sale_period")));
			} catch (Exception e) {
				ret.set("sale_period", 12L);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return ret;
	}

	@Override
	public Date touch() throws SerializationException {
		if (UserUtil.isUserInSession(getThreadLocalRequest()))
			return new Date();
		else
			return null;
	}

	@Override
	public Map<String, List<Station>> getMapStations() throws SerializationException {
		try {
			List<Station> ss = DB.getModels(Station.class, Where.isNotNull(Station.LAT).andIsNotNull(Station.LNG), false);
			if (ss == null)
				return null;
			return ss.stream().collect(groupingBy(s -> s.getNaspunkt().isEmpty() ? s.getName() : s.getNaspunkt()));
		} catch (Exception e) {
			logger.error("Ошибка при получении автостанций", e);
			throw new SerializationException("Ошибка при получении новостей");
		}
	}

	@Override
	public void regUser(String login, String pass) throws SerializationException {
		if (!CommonServerUtils.isValidEmail(login))
			throw new SerializationException("Неправильно указан E-mail");
		try {
			String code = RandomStringUtils.randomNumeric(6);
			StoredProcs.core.registerUser(login, pass, code);
			UserUtil.sendRegConfirm(getThreadLocalRequest(), login, pass, code);
		} catch (Exception e) {
			logger.error("", e);
			throw new SerializationException("При регистрации нового пользователя произошла ошибка");
		}
	}

	@Override
	public void restorePassword(String login) throws SerializationException {
		UserUtil.restorePassword(getThreadLocalRequest(), login);
	}

	@Override
	public Boolean isLoginFree(String login) throws SerializationException {
		try {
			return StoredProcs.core.isLoginFree(login);
		} catch (Exception e) {
			logger.error("", e);
			throw new SerializationException("При проверке уникальности логина произошла ошибка");
		}
	}

	@Override
	public void updateUser(User user) throws SerializationException {
		try {
			User usr = UserUtil.getUserFromSession(getThreadLocalRequest());
			Long id = usr.getId();

			User saveModel = new User();
			saveModel.fill(usr);
			saveModel.fill(user);
			saveModel.setId(id);
			DB.save(saveModel, id);
			UserUtil.cacheUser(saveModel);
		} catch (Exception e) {
			logger.error("Ошибка при сохранении данных", e);
			throw new SerializationException("Ошибка при сохранении данных");
		}
	}

	@Override
	public Boolean changePassword(String oldpass, String newpass) throws SerializationException {
		try {
			User user = UserUtil.getUserFromSession(getThreadLocalRequest());
			return StoredProcs.core.changePassword(user.getLogin(), oldpass, newpass);
		} catch (Exception e) {
			logger.error("При изменении пароля пользователя произошла ошибка.", e);
			throw new SerializationException("При изменении пароля пользователя произошла ошибка.");
		}
	}

	public static List<Passage> getPassages(Long depId, Date date, Long destId, String sessionId) throws SerializationException {
		try {
			Station dep = DB.getModel(Station.class, depId);
			Station dest = DB.getModel(Station.class, destId);
			if ((dep == null) || (dest == null))
				return null;

			List<Passage> ret;
			if ((dep.getHost() != null) && !dep.getHost().isEmpty())
				ret = KpasProtocolUtil.getPassages(depId, date, destId);
			else
				ret = StavrProtocolUtil.getPassages(dep.getStavId(), date, dest.getStavId());
			if (ret != null) {
				LocalDateTime now = LocalDateTime.now();
				try {
					now = now.plusHours(Long.parseLong(StoredProcs.core.getProp("sale_period")));
				} catch (Exception e) {
					now = now.plusHours(12L);
				}
				LocalDateTime threshold = now;
				ret.forEach(p -> {
					p.setDepId(depId);
					p.setDepName(dep.getName());
					p.setDestId(destId);
					p.setDestName(dest.getName());
					p.setSaleAvailable((p.getFreeSeats() > 0) && (p.getTariff() != null) && (p.getTariff() > 0)
							&& (threshold.isBefore(LocalDateTime.ofInstant(p.getDeparture().toInstant(), ZoneId.systemDefault()))));
				});
				passages.putAll(ret.stream().collect(toMap(p -> new PassageUniqKey(p, sessionId).toString(), Function.identity())));
			}
			return ret;
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("", e);
			throw new SerializationException("Не удалось найти подходящие рейсы");
		}
	}

	@Override
	public List<Passage> getPassages(Long depId, Date date, Long destId) throws SerializationException {
		HttpSession s = getThreadLocalRequest().getSession();
		return getPassages(depId, date, destId, s.getId());
	}

	@Override
	public List<Nationality> getNationalities() throws SerializationException {
		try {
			return DB.getModels(Nationality.class, new Where(), Nationality.MAIN + " desc, " + Nationality.NAME);
		} catch (Exception e) {
			logger.error("Ошибка при получении национальностей", e);
			throw new SerializationException("Ошибка при получении национальностей");
		}
	}

	@Override
	public List<DocType> getDocTypes() throws SerializationException {
		try {
			return DB.getModels(DocType.class, new Where(), DocType.ID);
		} catch (Exception e) {
			logger.error("Ошибка при получении документов", e);
			throw new SerializationException("Ошибка при получении документов");
		}
	}

	@Override
	public PageContainer<Ticket> getTickets(TicketStatus status, int limit, int offset) throws SerializationException {
		try {
			PageContainer<Ticket> ret = new PageContainer<Ticket>(new ArrayList<Ticket>());
			Long uid = (status != null) ? UserUtil.getUserIdFromSession(getThreadLocalRequest()) : null;
			List<Ticket> tickets = StoredProcs.core.getTickets(uid, status, null);
			if (tickets == null)
				return ret;
			ret.setItemsCount(tickets.size());
			ret.setPage(new ArrayList<Ticket>(tickets.subList(Math.max(0, Math.min(tickets.size(), offset)),
					Math.max(0, Math.min(tickets.size(), offset + limit)))));
			return ret;
		} catch (Exception e) {
			logger.error("Ошибка при получении билетов", e);
			throw new SerializationException("Ошибка при получении билетов");
		}
	}

	@Override
	public Map<TicketStatus, Long> getTicketCounts() throws SerializationException {
		try {
			List<Ticket> tickets = DB.getModels(Ticket.class);
			return tickets.stream().filter(t -> Objects.nonNull(t.getStatus())).collect(groupingBy(Ticket::getStatus, counting()));
		} catch (Exception e) {
			logger.error("Ошибка при получении билетов", e);
			throw new SerializationException("Ошибка при получении билетов");
		}
	}

	@Override
	public void setProp(String name, String value) throws SerializationException {
		try {
			StoredProcs.core.setProp(name, value);
		} catch (Exception e) {
			logger.error("Ошибка при записи параметров", e);
			throw new SerializationException("Ошибка при записи параметров");
		}
	}

	public static Passage getPassage(String cacheKey) {
		return passages.getIfPresent(cacheKey);
	}

	@Override
	public Passage getPassage(Long id, Long depId, Long destId, String depDate) throws SerializationException {
		HttpSession s = getThreadLocalRequest().getSession();
		return getPassage(new PassageUniqKey(id, depId, destId, depDate, s.getId()).toString());
	}

	@Override
	public Help getHelp(Long id) throws SerializationException {
		try {
			return DB.getModel(Help.class, id);
		} catch (Exception e) {
			logger.error("Ошибка при получении данных", e);
			throw new SerializationException("Ошибка при получении данных");
		}
	}

	@Override
	public Map<HelpType, List<Help>> getHelp(String locale) throws SerializationException {
		try {
			List<Help> helps = DB.getModels(Help.class, Where.equals(Help.LOCALE, Locales.valueOf(locale.toUpperCase())), false);
			return helps.stream().filter(h -> Objects.nonNull(h.getType())).collect(groupingBy(Help::getType));
		} catch (Exception e) {
			logger.error("Ошибка при получении билетов", e);
			throw new SerializationException("Ошибка при получении билетов");
		}
	}

	@Override
	public String buyTicket(List<User> usrs, Long id, Long depId, Long destId, Date depDate) throws SerializationException {
		HttpServletRequest request = getThreadLocalRequest();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
		Passage psg = getPassage(id, depId, destId, format.format(depDate));
		if (psg == null)
			throw new SerializationException("Сессия покупки билета истекла. Повторите поиск рейса.");
		Long userid = UserUtil.getUserIdFromSession(request);
		User usr = (userid == null) ? usrs.get(0) : UserUtil.getUserFromSession(request);
		return buyTickets(request, request.getHeader(HttpHeaders.USER_AGENT), usr, usrs, psg, false);
	}

	/**
	 * Найти группу соседних сидений
	 * 
	 * @param totalSeats - общее количество мест
	 * @param saleSeatList - проданные места
	 * @param seatsCount - необходимое количество мест
	 * @param alignment - выровнять начало группы по номерам реальных соседних сидений в автобусе
	 * @return
	 */
	private static long findAdjacentSeats(Long totalSeats, Set<Long> saleSeatList, int seatsCount, boolean alignment) {
		long ret = 1;
		while (ret + seatsCount < totalSeats) {
			boolean check = true;
			int i = 0;
			for (; i < seatsCount; i++)
				if (saleSeatList.contains(ret + i)) {
					check = false;
					break;
				}
			if (check)
				return ret;
			ret += alignment ? ADJACENT_SEATS : i + 1;
		}
		return 1;
	}

	public static String buyTickets(HttpServletRequest req, String userAgent, User customer, List<User> usrs, Passage psg, boolean isMobile) throws SerializationException {
		return buyTickets(req, userAgent, customer, usrs, psg, isMobile, "/#tickets/", "/");
	}

	public static String buyTickets(HttpServletRequest req, String userAgent, User customer, List<User> usrs, Passage psg, boolean isMobile, String successUrl, String failUrl) throws SerializationException {
		if (usrs.isEmpty())
			throw new SerializationException("Не указанны пассажиры");
		if ((psg.getTariff() == null) || psg.getTariff().equals(0L))
			throw new SerializationException("Не указан тариф");
		try {
			Long adults = 0L;
			Long childs = 0L;
			Long baggage = 0L;
			for (User us : usrs) {
				if (us.isChild() == Boolean.TRUE)
					childs++;
				else
					adults++;
				baggage += us.getBags();
			}
			Bill bill = new Bill();
			bill.setOpDate(new Date());
			bill.setPaymentSystem(1L);
			bill.setSuccessURL(successUrl);
			bill.setFailURL(failUrl);
			if (!customer.getPhone().isEmpty())
				bill.setPhone(customer.getPhone());
			else
				bill.setPhone(usrs.get(0).getPhone());
			bill.setEmail(customer.getLogin());
			String remoteAddr = req.getHeader(HttpHeaders.X_FORWARDED_FOR);
			if (remoteAddr == null)
				remoteAddr = req.getRemoteAddr();
			bill.setUserIp(remoteAddr);
			bill.setUserAgent(userAgent);

			Long amount = psg.getSumm() * adults
						+ psg.getChldSumm() * childs
						+ psg.getBagSumm() * baggage;
			bill.setAmount(amount);
			bill.setStatus(0L);
			bill.setSeats(adults);
			bill.setChilds(childs);
			bill.setBags(baggage);
			bill.setBpStatus(TicketStatus.IN_PROCESSING);
			bill.setUserId(customer.getId());
			DB.save(bill, bill.getUserId());

			StringBuilder comment = new StringBuilder();
			if (!PropUtil.isProduction())
				comment.append("Тестовая покупка ");
			comment.append(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(psg.getDeparture())).append(" ").append(psg.getName()).append(", ")
					.append(psg.getDepName()).append(", ").append(psg.getDestName()).append(", м: ");

			List<Ticket> tktlist = new ArrayList<Ticket>();

			long st = 1;
			long stInGroup = 0;
			Set<Long> seats = new TreeSet<Long>();

			Long komsbor = psg.getKomSbor();
			Long obstrah = psg.getObStrah();
			Long tarif = psg.getTariff();
			Long chtarif = psg.getPrefTariff();
			Long ptarif = (psg.getRaftFlag() && (psg.getRaftTariff() != null)) ? psg.getRaftTariff() : 0L;
			Long ptarifch = (psg.getRaftFlag() && (psg.getRaftTariffCh() != null)) ? psg.getRaftTariffCh() : 0L;

			Set<Long> saleSeatList = new HashSet<Long>();	// для КПАС - это список проданных мест, для Ставрополя - это список доступных мест
			if (psg.getSaleSeatList() != null)
				for (BaseModel seat : psg.getSaleSeatList())
					if (seat.getLongProp("name") != null)
						saleSeatList.add(seat.getLongProp("name"));

			for (User ps : usrs) {
				Ticket tkt = new Ticket();
				boolean ischild = ps.isChild() == Boolean.TRUE;

				tkt.setBaggage(ps.getBags());

				tkt.setUserId(customer.getId());
				tkt.setDocument(ps.getDocType());
				tkt.setSeriya(ps.getPaspSeriya());
				tkt.setNumber(ps.getPaspNumber());
				tkt.setFromId(psg.getDepId());
				tkt.setFrom(psg.getDepName());
				tkt.setToId(psg.getDestId());
				tkt.setTo(psg.getDestName());
				tkt.setNomerVedomosti(psg.getStatement());
				tkt.setVdate(ps.getVdate());

				tkt.setTarif(ischild ? chtarif : tarif);
				tkt.setFactTarif(tarif);

				tkt.setKomSbor(komsbor);
				tkt.setObStrah(obstrah);

				tkt.setPrice(ischild ? psg.getChldSumm() : psg.getSumm());
				tkt.setPriceBag(psg.getBagSumm());

				tkt.setTicketType(ischild ? "д" : "п");

				tkt.setParom(psg.getRaftFlag());
				tkt.setParomTarif(ischild ? ptarifch : ptarif);
				tkt.setFactParomTarif(ptarif);
				tkt.setParomTicketType(ischild ? "д" : "в");

				tkt.setFirstname((ps.getFirstName() != null) ? ps.getFirstName().toUpperCase() : null);
				tkt.setLastname((ps.getLastName() != null) ? ps.getLastName().toUpperCase() : null);
				tkt.setMiddlename((ps.getMiddleName() != null) ? ps.getMiddleName().toUpperCase() : null);

				tkt.setPhone(ps.getPhone());
				tkt.setGrajd(ps.getGrajd());
				tkt.setGender(ps.getGender());

				tkt.setBirthDate(ps.getBirthday());

				tkt.setStatus(TicketStatus.IN_PROCESSING);
				tkt.setBillId(bill.getId());

				tkt.setSellTime(new Date());

				tkt.setDeparture(psg.getDeparture());
				tkt.setArrival(psg.getArrival());

				tkt.setReisId(psg.getId());
				tkt.setReisName(psg.getName());

				tkt.setBag2(0L);
				tkt.setBag1(0L);
				tkt.setBagTarif(0L);

				tkt.setTBTarif(psg.getTBTarif());

				DB.save(tkt, bill.getUserId());

				switch (ps.getBags().intValue()) {
				case 2:
					tkt.setBag2(StoredProcs.core.getBagid());
				case 1:
					tkt.setBag1(StoredProcs.core.getBagid());
					tkt.setBagTarif(psg.getBagTariff());
				}

				if (psg.getPassageType() == PassageType.KPAS) {
					boolean needrepeat = true;
					int errs = 0;
	
					while (needrepeat && (saleSeatList.size() < psg.getTotalSeats())) {
						// поиск группы соседних сидений
						if (stInGroup % ADJACENT_SEATS == 0) {
							int seatsCount = (int) (adults + childs - tktlist.size());
							st = findAdjacentSeats(psg.getTotalSeats(), saleSeatList, Math.min(seatsCount, ADJACENT_SEATS), seatsCount >= ADJACENT_SEATS);
						}

						// поиск одиночных сидений, если группа не найдена
						if (saleSeatList.contains(st)) {
							stInGroup = 0;
							for (long j = st; j <= psg.getTotalSeats(); j++)
								if (!saleSeatList.contains(j)) {
									st = j;
									break;
								}
						}

						tkt.setSeat(st);

						DB.save(tkt, bill.getUserId());
//						if (PropUtil.isProduction())
							needrepeat = !KpasProtocolUtil.saleTicket(tkt, bill.getEmail());
//						else
//							needrepeat = false;
						if (needrepeat) {
							stInGroup = 0;
							saleSeatList.add(st);
							if (++errs >= MAX_ERRORS)
								break;
						}
					}
					if (needrepeat) {
						DB.delete(tkt);
						throw new SerializationException("Не удалось забронировать билеты, попробуйте ещё раз");
					}
					saleSeatList.add(st);
					seats.add(st);
					stInGroup++;
					st++;
				} else {
					// заполняем гражданство и документ
					Nationality nat = DB.getModel(Nationality.class, tkt.getGrajd());
					tkt.setStavNationId(nat.getOksm());
					tkt.setStavDocTypeId(tkt.getDocument());
				}
				tktlist.add(tkt);
			}

			if (psg.getPassageType() == PassageType.STAVAVTO) {
				for (Ticket tkt : tktlist)
					findStavSeat(tktlist, tkt, saleSeatList, seats);

				Pattern p = Pattern.compile("Место № (\\d*) уже недоступно для продажи");
				boolean needrepeat = true;
				while (needrepeat)
					try {
						bill.setStavBookingId(StavrProtocolUtil.booking(tktlist, psg));
						needrepeat = false;
					} catch (StProtocolException e) {
						Matcher m = p.matcher(e.getMessage());
						if (m.find()) {
							Long seat = Long.valueOf(m.group(1));
							seats.remove(seat);
							Optional<Ticket> tkt = tktlist.stream().filter(t -> Objects.equals(t.getSeat(), seat)).findFirst();
							if (tkt.isPresent()) {
								// поиск нового места
								findStavSeat(tktlist, tkt.get(), saleSeatList, seats);
							} else
								throw new SerializationException("Не удалось забронировать билеты, попробуйте ещё раз");
						} else
							throw e;
					}
				DB.save(tktlist, bill.getUserId());
			}

			if ((psg.getTBTarif() != null) && !psg.getTBTarif().equals(0L))
				comment.append("б/м");
			else
				comment.append(seats.stream().map(Object::toString).collect(Collectors.joining(",")));
			comment.append(" б: ").append(baggage);
			bill.setComment(comment.toString()); // TODO возможно необходимо будет добавить ссылку на страницу с электронным билетом

			String callbackUrl = req.getScheme() + "://" + req.getServerName() + ((req.getServerPort() != 80) ? ":" + req.getServerPort() : "") + "/alfaservlet";
			RegisterOrderResponse resp = AcquiringUtil.registerOrder(bill.getId().toString(),
					bill.getAmount(), callbackUrl, callbackUrl, bill.getComment(), bill.getEmail().trim().isEmpty() ? null : bill.getEmail(), null);	// TODO isMobile
			bill.setPaymentOrderURL(resp.getFormUrl());
			DB.save(bill, bill.getUserId());
			return resp.getFormUrl();
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("", e);
			throw new SerializationException("При попытке покупки билета произошла ошибка");
		}
	}

	private static void findStavSeat(List<Ticket> tktlist, Ticket tkt, Set<Long> saleSeatList, Set<Long> seats) throws Exception {
		Optional<Long> min = saleSeatList.stream().min(Long::compareTo);
		if (!min.isPresent()) {
			DB.delete(tktlist);
			throw new SerializationException("Не удалось забронировать билеты, попробуйте ещё раз");
		}
		Long seat = min.get();
		tkt.setSeat(seat);
		saleSeatList.remove(seat);
		seats.add(seat);
	}

	@Override
	public List<Ticket> getTicketsByHash(String hash) throws SerializationException {
		try {
			return StoredProcs.core.getTickets(null, null, hash);
		} catch (Exception e) {
			logger.error("Ошибка при получении билетов", e);
			throw new SerializationException("Ошибка при получении билетов");
		}
	}

	@Override
	public <T extends BaseModel> PageContainer<T> getPagedModels(String className, List<Long> forDel, Where where,
			String sortColumn) throws SerializationException {
		try {
			@SuppressWarnings("unchecked")
			Class<T> clazz = (Class<T>) Class.forName(className);
			if (forDel != null)
				DB.del(clazz, forDel);
			return DB.getPagedModels(clazz, where, sortColumn, true);
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Возникла ошибка при получении данных", e);
			throw new SerializationException("Возникла ошибка при получении данных");
		}
	}

	@Override
	public <T extends BaseModel> Long saveModel(T model) throws SerializationException {
		User currUser = UserUtil.getUserFromSession(getThreadLocalRequest());
		try {
			return DB.save(model, (currUser != null) ? currUser.getId() : -1L);
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Возникла ошибка при сохранении данных", e);
			throw new SerializationException("Возникла ошибка при сохранении данных");
		}
	}

	@Override
	public <T extends BaseModel> void delModel(T model) throws SerializationException {
		try {
			DB.delete(model);
		} catch (Exception e) {
			logger.error("Возникла ошибка при удалении данных", e);
			throw new SerializationException("Возникла ошибка при удалении данных");
		}
	}

	@Override
	public <T extends BaseModel> List<T> getKeyValueObjects(String className) throws SerializationException {
		try {
			@SuppressWarnings("unchecked")
			Class<T> clazz = (Class<T>) Class.forName(className);
			return DB.getKeyValueList(clazz, null, null, null);
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Возникла ошибка при получении данных", e);
			throw new SerializationException("Возникла ошибка при получении данных");
		}
	}

	@Override
	public UUID generateApiToken() throws SerializationException {
		User user = UserUtil.getUserFromSession(getThreadLocalRequest());
		try {
			UUID ret = new UUID(java.util.UUID.randomUUID().toString());
			user.setApiToken(ret);
			DB.save(user, user.getId());
			return ret;
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Возникла ошибка при создании API-токена", e);
			throw new SerializationException("Возникла ошибка при создании API-токена");
		}
	}

	@Override
	public List<Requisite> getRequisites() throws SerializationException {
		User user = UserUtil.getUserFromSession(getThreadLocalRequest());
		if (user == null)
			return null;
		try {
			return DB.getModels(Requisite.class, Where.equals(Requisite.TUSER_ID, user.getId()), false);
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Возникла ошибка при получении данных", e);
			throw new SerializationException("Возникла ошибка при получении данных");
		}
	}

	@Override
	public PageContainer<Requisite> getRequisites(List<Long> forDel, Where where, String sortColumn)
			throws SerializationException {
		User user = UserUtil.getUserFromSession(getThreadLocalRequest());
		if (user == null)
			return null;
		try {
			if (forDel != null)
				DB.del(Requisite.class, forDel);
			return DB.getPagedModels(Requisite.class, where.andEquals(Requisite.TUSER_ID, user.getId()), sortColumn, true);
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Возникла ошибка при получении данных", e);
			throw new SerializationException("Возникла ошибка при получении данных");
		}
	}

	@Override
	public Requisite saveRequisite(Requisite req) throws SerializationException {
		User user = UserUtil.getUserFromSession(getThreadLocalRequest());
		if (user == null)
			throw new SerializationException("Авторизуйтесь для сохранения шаблонов");
		try {
			req.setUserId(user.getId());
			Long id = DB.save(req, user.getId());
			return DB.getModel(Requisite.class, id);
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Возникла ошибка при сохранении данных", e);
			throw new SerializationException("Возникла ошибка при сохранении данных");
		}
	}

}