package com.avtoticket.server.utils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.text.WordUtils;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.DB;
import com.avtoticket.server.db.stored.StoredProcs;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.Bill;
import com.avtoticket.shared.models.core.BirthdayUser;
import com.avtoticket.shared.models.core.Nationality;
import com.avtoticket.shared.models.core.Station;
import com.avtoticket.shared.models.core.StationLink;
import com.avtoticket.shared.models.core.TicketStatus;
import com.avtoticket.shared.models.core.User;
import com.google.gwt.user.client.rpc.SerializationException;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.CronScheduleBuilder.*;

public class SchedullerUtil {

	private static Logger logger = LoggerFactory.getLogger(SchedullerUtil.class);
	private static Scheduler scheduler;
	private static String destCron = "0 0 */6 * * ?";		// каждые 6 часов
	private static String stavCron = "0 0 1/6 * * ?";		// каждые 6 часов
	private static String billCron = "0 */30 * * * ?";		// каждые 30 минут
	private static String birthdayCron = "0 0 12 * * ?";	// каждый день в 12:00

	public static class DestJob implements Job {
		@Override
		public void execute(JobExecutionContext arg0) {
			regetStations();
		}
	}

	public static class StavJob implements Job {
		@Override
		public void execute(JobExecutionContext arg0) {
			syncStav26();
		}
	}

	public static class BillJob implements Job {
		@Override
		public void execute(JobExecutionContext arg0) {
			recheckBills();
		}
	}

	public static class BirthdayJob implements Job {
		@Override
		public void execute(JobExecutionContext arg0) {
			sendHappyBirthday();
		}
	}

	public static void start() {
		if (scheduler == null)
			try {
				scheduler = StdSchedulerFactory.getDefaultScheduler();
				scheduler.start();

				JobDetail jobDest = newJob(DestJob.class).withIdentity("jobDest", "common").build();
				Trigger triggerDest = newTrigger().withIdentity("triggerDest", "common").startNow()
						.withSchedule(cronSchedule(destCron)).build();
				scheduler.scheduleJob(jobDest, triggerDest);

				if (!PropUtil.isProduction()) {
					JobDetail jobStav = newJob(StavJob.class).withIdentity("jobStav", "common").build();
					Trigger triggerStav = newTrigger().withIdentity("triggerStav", "common").startNow()
							.withSchedule(cronSchedule(stavCron)).build();
					scheduler.scheduleJob(jobStav, triggerStav);
				}

				JobDetail jobBill = newJob(BillJob.class).withIdentity("jobBill", "common").build();
				Trigger triggerBill = newTrigger().withIdentity("triggerBill", "common").startNow()
						.withSchedule(cronSchedule(billCron)).build();
				scheduler.scheduleJob(jobBill, triggerBill);

				if (PropUtil.isProduction()) {
					JobDetail jobBirthday = newJob(BirthdayJob.class).withIdentity("jobBirthday", "common").build();
					Trigger triggerBirthday = newTrigger().withIdentity("triggerBirthday", "common").startNow()
							.withSchedule(cronSchedule(birthdayCron)).build();
					scheduler.scheduleJob(jobBirthday, triggerBirthday);
				}
			} catch (Exception e) {
				logger.error("", e);
			}
	}

	public static void stop() {
		if (scheduler != null)
			try {
				scheduler.shutdown();
			} catch (Exception e) {
				logger.error("", e);
			}
	}

	private static Map<String, String> loadNameMap() {
		Map<String, String> ret = new HashMap<String, String>();
		try (Scanner in = new Scanner(SchedullerUtil.class.getResourceAsStream("/StationMapping.txt"), StandardCharsets.UTF_8.name())) {
			while (in.hasNext()) {
				String line = in.nextLine();
				String[] entry = line.split("=");
				ret.put(entry[0].replaceAll(" ", "").toLowerCase(), entry[1]);
			}
		}
		return ret;
	}

	public static void syncStav26() {
		logger.info("Начинаем синхронизацию с вокзалами Ставрополя");
		try {
			// обновляем национальности
			List<Nationality> avtNations = DB.getModels(Nationality.class);
			List<Nationality> stavNations = StavrProtocolUtil.getNationalities();
			BiPredicate<String, String> cmp = (s1, s2) -> s1.replaceAll(" ", "").equalsIgnoreCase(s2.replaceAll(" ", ""));
			if (stavNations != null) {
				for (Nationality n : stavNations) {
					// ищем ранее синхронизированную нацию
					Optional<Nationality> exists = avtNations.stream().filter(an -> Objects.equals(an.getOksm(), n.getOksm())).findFirst();
					if (exists.isPresent())
						if (cmp.test(exists.get().getName(), n.getName()))
							// если нация уже существует и совпадает по названию, то ничего не делаем
							continue;
						else {
							// если нация уже существует и не совпадает по названию, то обнуляем связь и создаём новую нацию
							Nationality e = exists.get();
							logger.info("Отвязываем нацию '" + e.getName() + "'");
							e.setOksm(null);
							DB.save(e, -1L);
						}
					// ищем нацию с таким же названием
					exists = avtNations.stream().filter(an -> cmp.test(an.getName(), n.getName())).findFirst();
					if (exists.isPresent()) {
						// если нация с таким названием уже есть, то устанавливаем связь со Ставрополем
						Nationality e = exists.get();
						logger.info("Обновляем нацию '" + e.getName() + "'");
						e.setOksm(n.getOksm());
						DB.save(e, -1L);
					} else {
						// иначе создаём новую нацию
						logger.info("Добавляем новую нацию '" + n.getName() + "'");
						DB.save(n, -1L);
					}
				}
			}

			// обновляем станции
			List<Station> stations = DB.getModels(Station.class);
			Map<String, String> nameMap = loadNameMap();
			Function<Station, String> nameMapper = s -> {
				String ret = s.getName().replaceAll(" ", "").toLowerCase();
				if (nameMap.containsKey(ret))
					ret = nameMap.get(ret).replaceAll(" ", "").toLowerCase();
				return ret;
			};
			Map<String, Station> stNameMap = stations.stream().collect(Collectors.toConcurrentMap(nameMapper, Function.identity(), (a, b) -> a));
			Map<String, Station> stIdMap = DB.getModels(Station.class).stream().filter(s -> (s.getStavId() != null) && !s.getStavId().isEmpty()).collect(Collectors.toConcurrentMap(Station::getStavId, Function.identity()));
			List<Station> deps = StavrProtocolUtil.getFromStations();

			ExecutorService executor = Executors.newFixedThreadPool(SyncStavThread.THREADS);
		    final List<Future<?>> futures = new ArrayList<>();
		    for (Station dep : deps) {
				List<Station> dests = StavrProtocolUtil.getToStations(dep.getStavId());
				Instant date = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
				for (int i = 0; i < StavrProtocolUtil.DATES_RANGE; i++) {
		            Future<?> future = executor.submit(new SyncStavThread(dep, date, dests, stNameMap, stIdMap, nameMapper));
		            futures.add(future);
					date = date.plus(1, ChronoUnit.DAYS);
				}
		    }
	        for (Future<?> future : futures)
	            future.get();
		} catch (Exception e) {
			logger.error("syncStav26 job exception", e);
		} finally {
			logger.info("Синхронизация окончена");
		}
	}

	public static void regetStations() {
		logger.info("Начинаем синхронизацию с вокзалами");
		try {
			List<Station> deps = DB.getModels(Station.class, Where.equals(Station.IS_DEPARTURE_POINT, true).andIsNotNull(Station.HOST), false);
			Map<Long, Station> oldDests = DB.getModels(Station.class).stream().filter(s -> s.getKpasId() != null).collect(Collectors.toMap(Station::getKpasId, Function.identity()));
			for (Station dep : deps) {
				if (dep.getHost().isEmpty())
					continue;
				logger.info("Станция '" + dep.getName() + "' (" + dep.getHost() + ")");
				Instant date = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
				for (int i = 0; i < KpasProtocolUtil.DATES_RANGE; i++) {
					try {
						// очищаем старые рейсы
						List<StationLink> sls = DB.getModels(StationLink.class, Where
								.equals(StationLink.DEP_ID, dep.getId())
								.andEquals(StationLink.DATEOP, Date.from(date)), false);
						DB.delete(sls);
						// получаем новые рейсы от вокзала
						List<Station> dests = KpasProtocolUtil.getDestinations(Date.from(date), dep.getId());
						for (Station dest : dests) {
							Station destination = oldDests.get(dest.getId());
							if (destination != null) {
								if (!Objects.equals(destination.getName(), dest.getName()))
									logger.info("обновление автостанции " + dest.getName() + "(" + dest.getId() + ")    dep:" + dep.getName());
							} else {
								logger.info("добавление автостанции " + dest.getName() + "(" + dest.getId() + ")    dep:" + dep.getName());
								destination = new Station();
								destination.setKpasId(dest.getId());
								oldDests.put(dest.getId(), destination);
							}
							// обновляем станции
							if (!Objects.equals(destination.getName(), dest.getName())) {
								destination.setName(dest.getName());
								DB.save(destination, -1L);
							}

							// обновляем рейсы
							StationLink sl = new StationLink();
							sl.setDateOp(Date.from(date));
							sl.setDepId(dep.getId());
							sl.setDestId(destination.getId());
							DB.save(sl, -1L);
						}
					} catch (SerializationException e) {
						logger.warn(dep.getName() + ": " + e.getMessage());
					} catch (Exception e) {
						logger.error(dep.getName(), e);
					}
					date = date.plus(1, ChronoUnit.DAYS);
				}
			}
		} catch (Exception e) {
			logger.error("regetStations job exception", e);
		} finally {
			logger.info("Синхронизация окончена");
		}
	}

	public static void recheckBills() {
		try {
			Date timeout = new Date(new Date().getTime() - 115 * 60 * 1000);	// 115 минут назад
			List<Bill> bills = DB.getModels(Bill.class, Where.less(Bill.OP_DATE, timeout).andWhere(Where.equals(Bill.BP_STATUS, TicketStatus.IN_PROCESSING).orEquals(Bill.BP_STATUS, TicketStatus.RESERVED)), false);
			if (bills != null) {
				bills.forEach(b -> b.setBpStatus(TicketStatus.CANCELED));
				DB.save(bills, -1L);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public static void sendHappyBirthday() {
		try {
			String theme = StoredProcs.core.getProp("birthday_theme");
			String mail = StoredProcs.core.getProp("birthday_mail");
			List<BirthdayUser> users = DB.getModels(BirthdayUser.class);
			if (users != null)
				for (BirthdayUser user : users)
					doSend(user, theme, mail);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public static void doSend(User user, String theme, String mail) {
		String email = user.getLogin();
		try {
			if (email.contains("@")) {
				String username = "";
				if (!user.getFirstName().isEmpty()) {
					username = WordUtils.capitalizeFully(user.getFirstName());
					if (!user.getMiddleName().isEmpty())
						username += " " + WordUtils.capitalizeFully(user.getMiddleName());
					username += "!";
					if (user.getGender() != null)
						switch (user.getGender()) {
						case MALE:
							username = "Уважаемый " + username;
							break;
						case FEMALE:
							username = "Уважаемая " + username;
							break;
						}
				}
				MailUtil.sendMessage(email, theme, mail.replace("%username%", username));
			}
		} catch (Exception e) {
			logger.error("Возникла ошибка при отправке поздравления на адрес " + email, e);
		}
	}

}