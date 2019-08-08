package com.avtoticket.shared.models.core;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.avtoticket.shared.models.BaseModel;

/**
 * Рейс
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 */
public class Passage extends BaseModel {

	private static final long serialVersionUID = -4788531919945228777L;

	public static final transient String NAME = "name";
	public static final transient String STATEMENT = "statement";
	public static final transient String BUS_MARK = "busmark";
	public static final transient String BUS_TYPE = "bustype";
	public static final transient String TOTAL_SEATS = "totalseats";
	public static final transient String FREE_SEATS = "freeseats";
	public static final transient String DEPARTURE = "departure";
	public static final transient String ARRIVAL = "arrival";
	public static final transient String TARIFF = "tariff";
	public static final transient String PREF_TARIFF = "preftariff";
	public static final transient String BAG_TARIFF = "bagtariff";
	public static final transient String RAFT_FLAG = "raftflag";
	public static final transient String RAFT_TARIFF = "rafttariff";
	public static final transient String RAFT_TARIFF_CH = "rafttariffch";
	public static final transient String CATEGORY = "category";
	public static final transient String SALE_SEAT_LIST = "saleseatlist";
	public static final transient String SALE_SEAT_LIST_LONG = SALE_SEAT_LIST + "_long";
	public static final transient String KOM_SBOR = "komsbtariff";
	public static final transient String OB_STRAH = "obstrtariff";
	public static final transient String TRANZIT = "tranzit";
	public static final transient String TBTARIF = "tbtarif";

	public static final transient String DEP_ID = "dep_id";
	public static final transient String STAV_DEP_ID = "stav_dep_id";
	public static final transient String DEP_NAME = "dep_name";
	public static final transient String DEST_ID = "dest_id";
	public static final transient String STAV_DEST_ID = "stav_dest_id";
	public static final transient String DEST_NAME = "dest_name";

	public static final transient String PRICE_ADULT = "price_adult";
	public static final transient String PRICE_CHILD = "price_child";
	public static final transient String PRICE_BAG = "price_bag";

	public static final transient String PASSAGE_TYPE = "passage_type";
	public static final transient String IS_SALE_AVAILABLE = "is_sale_available";

	// Ставрополь
	public static final transient String ID_TRIP = "id_trip";
	public static final transient String NAME_TRIP = "name_trip";
	public static final transient String DATE_TRIP = "date_trip";
	public static final transient String TIME_TRIP = "time_trip";
	public static final transient String DATE_ARRIVAL_TRIP = "date_arrival_trip";
	public static final transient String TIME_ARRIVAL_TRIP = "time_arrival_trip";
	public static final transient String TIME_DURATION_TRIP = "time_duration_trip";
	public static final transient String COUNT_AVAILABLE_SEATS_TRIP = "count_available_seats_trip";
	public static final transient String SEATS_TRIP = "seats_trip";
	public static final transient String FULL_TICKET_PRICE = "full_ticket_price";
	public static final transient String CHILD_TICKET_PRICE = "child_ticket_price";
	public static final transient String BAGGAGE_PRICE = "baggage_price";
	public static final transient String BOOKING_PRICE = "booking_price";
	public static final transient String MARKUP_TICKET_PRICE = "markup_ticket_price";
	public static final transient String MARKUP_CHILD_PRICE = "markup_child_price";
	public static final transient String MARKUP_BAGGAGE_PRICE = "markup_baggage_price";
	public static final transient String TOTAL_FULL_TICKET_PRICE = "total_full_ticket_price";
	public static final transient String TOTAL_CHILD_TICKET_PRICE = "total_child_ticket_price";
	public static final transient String TOTAL_BAGGAGE_PRICE = "total_baggage_price";

	public Passage() {
		super(Passage.class.getName());
	}

	public String getName() {
		return getStringProp(NAME);
	}
	public void setName(String val) {
		set(NAME, val);
	}

	public Long getStatement() {
		return getNullSafeLong(STATEMENT);
	}
	public void setStatement(Long val) {
		set(STATEMENT, val);
	}

	public String getBusMark() {
		return getStringProp(BUS_MARK);
	}
	public void setBusMark(String val) {
		set(BUS_MARK, val);
	}

	public String getBusType() {
		return getStringProp(BUS_TYPE);
	}
	public void setBusType(String val) {
		set(BUS_TYPE, val);
	}

	public Long getTotalSeats() {
		return getNullSafeLong(TOTAL_SEATS);
	}
	public void setTotalSeats(Long val) {
		set(TOTAL_SEATS, val);
	}

	public Long getFreeSeats() {
		return getNullSafeLong(FREE_SEATS);
	}
	public void setFreeSeats(Long val) {
		set(FREE_SEATS, val);
	}

	public Date getDeparture() {
		return getDateProp(DEPARTURE);
	}
	public void setDeparture(Date val) {
		set(DEPARTURE, val);
	}

	public Date getArrival() {
		return getDateProp(ARRIVAL);
	}
	public void setArrival(Date val) {
		set(ARRIVAL, val);
	}

	public Long getTariff() {
		return getNullSafeLong(TARIFF);
	}
	public void setTariff(Long val) {
		set(TARIFF, val);
	}

	public Long getPrefTariff() {
		return getNullSafeLong(PREF_TARIFF);
	}
	public void setPrefTariff(Long val) {
		set(PREF_TARIFF, val);
	}

	public Long getBagTariff() {
		return getNullSafeLong(BAG_TARIFF);
	}
	public void setBagTariff(Long val) {
		set(BAG_TARIFF, val);
	}

	public Boolean getRaftFlag() {
		return Objects.equals(getNullSafeLong(RAFT_FLAG), 1L);
	}
	public void setRaftFlag(Boolean val) {
		set(RAFT_FLAG, val ? 1L : 0);
	}

	public Long getRaftTariff() {
		return getNullSafeLong(RAFT_TARIFF);
	}
	public void setRaftTariff(Long val) {
		set(RAFT_TARIFF, val);
	}

	public Long getRaftTariffCh() {
		return getNullSafeLong(RAFT_TARIFF_CH);
	}
	public void setRaftTariffCh(Long val) {
		set(RAFT_TARIFF_CH, val);
	}

	public String getCategory() {
		return getStringProp(CATEGORY);
	}
	public void setCategory(String val) {
		set(CATEGORY, val);
	}

	public List<BaseModel> getSaleSeatList() {
		return getListProp(SALE_SEAT_LIST);
	}
	public void setSaleSeatList(List<BaseModel> val) {
		set(SALE_SEAT_LIST, val);
	}

	public List<Long> getSaleSeats() {
		return getListProp(SALE_SEAT_LIST_LONG);
	}
	public void setSaleSeats(List<Long> val) {
		set(SALE_SEAT_LIST_LONG, val);
	}

	public Long getKomSbor() {
		return getNullSafeLong(KOM_SBOR);
	}
	public void setKomSbor(Long val) {
		set(KOM_SBOR, val);
	}

	public Long getObStrah() {
		return getNullSafeLong(OB_STRAH);
	}
	public void setObStrah(Long val) {
		set(OB_STRAH, val);
	}

	public Long getTranzit() {
		return getNullSafeLong(TRANZIT);
	}
	public void setTranzit(Long val) {
		set(TRANZIT, val);
	}

	public Long getTBTarif() {
		return getNullSafeLong(TBTARIF);
	}
	public void setTBTarif(Long val) {
		set(TBTARIF, val);
	}

	public Long getDepId() {
		return getLongProp(DEP_ID);
	}
	public void setDepId(Long val) {
		set(DEP_ID, val);
	}

	public String getStavDepId() {
		return getStringProp(STAV_DEP_ID);
	}
	public void setStavDepId(String id) {
		set(STAV_DEP_ID, id);
	}

	public String getDepName() {
		return getStringProp(DEP_NAME);
	}
	public void setDepName(String val) {
		set(DEP_NAME, val);
	}

	public Long getDestId() {
		return getLongProp(DEST_ID);
	}
	public void setDestId(Long val) {
		set(DEST_ID, val);
	}

	public String getStavDestId() {
		return getStringProp(STAV_DEST_ID);
	}
	public void setStavDestId(String id) {
		set(STAV_DEST_ID, id);
	}

	public String getDestName() {
		return getStringProp(DEST_NAME);
	}
	public void setDestName(String val) {
		set(DEST_NAME, val);
	}

	public Long getSumm() {
		return getLongProp(PRICE_ADULT);
	}
	public void setSumm(Long summ) {
		set(PRICE_ADULT, summ);
	}

	public Long getChldSumm() {
		return getLongProp(PRICE_CHILD);
	}
	public void setChldSumm(Long summ) {
		set(PRICE_CHILD, summ);
	}

	public Long getBagSumm() {
		return getLongProp(PRICE_BAG);
	}
	public void setBagSumm(Long summ) {
		set(PRICE_BAG, summ);
	}

	public PassageType getPassageType() {
		return getEnumProp(PASSAGE_TYPE);
	}
	public void setPassageType(PassageType type) {
		set(PASSAGE_TYPE, type);
	}

	public Boolean isSaleAvailable() {
		return getBooleanProp(IS_SALE_AVAILABLE);
	}
	public void setSaleAvailable(Boolean isSaleAvailable) {
		set(IS_SALE_AVAILABLE, isSaleAvailable);
	}

}