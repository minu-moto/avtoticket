/*
 * Copyright Avtoticket (c) 2018.
 */
package com.avtoticket.server.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.DB;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.Passage;
import com.avtoticket.shared.models.core.Station;
import com.avtoticket.shared.models.core.StationLink;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 19 сент. 2018 г. 22:37:26
 */
public class SyncStavThread extends Thread {

	private static Logger logger = LoggerFactory.getLogger(SyncStavThread.class);

	public static final int THREADS = 20;

	private Station dep;
	private Instant date;
	private List<Station> dests;
	private Map<String, Station> stIdMap;
	private Map<String, Station> stNameMap;
	private Function<Station, String> nameMapper;

	public SyncStavThread(Station dep, Instant date, List<Station> dests, Map<String, Station> stNameMap, Map<String, Station> stIdMap, Function<Station, String> nameMapper) {
		this.dep = dep;
		this.date = date;
		this.dests = dests;
		this.stIdMap = stIdMap;
		this.stNameMap = stNameMap;
		this.nameMapper = nameMapper;
	}

	private void updateStavStation(Station newStation) throws Exception {
		Station exists = stIdMap.get(newStation.getStavId());
		if (exists != null)
			if (nameMapper.apply(exists).equalsIgnoreCase(newStation.getName().replaceAll(" ", ""))) {
				// если станция совпадает по id и названию, то ничего не делаем
				newStation.setId(exists.getId());
				return;
			} else {
				// если станция уже существует и не совпадает по названию, то обнуляем связь и создаём новую станцию
				logger.info("Отвязываем станцию '" + exists.getName() + "'");
				stIdMap.remove(exists.getStavId());
				exists.setStavId(null);
				DB.save(exists, -1L);
			}
		// ищем станцию с таким же названием
		exists = stNameMap.get(newStation.getName().replaceAll(" ", "").toLowerCase());
		if (exists != null) {
			// если станция с таким названием уже есть, то устанавливаем связь со Ставрополем
			logger.info("Обновляем станцию '" + exists.getName() + "'");
			stIdMap.put(newStation.getStavId(), exists);
			exists.setStavId(newStation.getStavId());
			if (exists.getAddress().isEmpty())
				exists.setAddress(newStation.getAddress());
			exists.setDeparturePoint((exists.isDeparturePoint() == Boolean.TRUE) || (newStation.isDeparturePoint() == Boolean.TRUE));
			DB.save(exists, -1L);
			newStation.setId(exists.getId());
		} else {
			// иначе создаём новую станцию
			logger.info("Добавляем новую станцию '" + newStation.getName() + "'");
			stIdMap.put(newStation.getStavId(), newStation);
			stNameMap.put(newStation.getName().replaceAll(" ", "").toLowerCase(), newStation);
			DB.save(newStation, -1L);
		}
	}

	@Override
	public void run() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());
		try {
			logger.info("Станция отправления '" + dep.getName() + "' " + formatter.format(date));
			// обновляем точки отправления
			updateStavStation(dep);
			// обновляем точки прибытия
			for (Station dest : dests) {
				updateStavStation(dest);

				// обновляем рейсы
				logger.debug("Станция прибытия '" + dest.getName() + "' " + formatter.format(date));
				try {
					// очищаем старые рейсы
					List<StationLink> sls = DB.getModels(StationLink.class, Where
							.equals(StationLink.DEP_ID, dep.getId())
							.andEquals(StationLink.DATEOP, Date.from(date))
							.andEquals(StationLink.DEST_ID, dest.getId()), false);
					DB.delete(sls);
					// получаем новые рейсы от вокзала
					List<Passage> psg = StavrProtocolUtil.getPassages(dep.getStavId(), Date.from(date), dest.getStavId());
					if ((psg != null) && !psg.isEmpty()) {
						StationLink sl = new StationLink();
						sl.setDateOp(Date.from(date));
						sl.setDepId(dep.getId());
						sl.setDestId(dest.getId());
						DB.save(sl, -1L);
					}
				} catch (Exception e) {
					logger.error(dep.getName(), e);
				}
			}
		} catch (Exception e) {
			logger.error("syncStav26 " + getName() + " exception", e);
		}
	}

}