package com.avtoticket.shared.models.core;

import java.util.Date;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.View;

@View("core.tickets")
@Table("core.ttickets")
public class Ticket extends BaseModel {

	private static final long serialVersionUID = 6351630313965691576L;

	@TableField
	public static final transient String USER_ID = "userid";
	@TableField
	public static final transient String DOCUMENT = "document";
	@TableField
	public static final transient String NUMBER = "number";
	@TableField
	public static final transient String SERIYA = "seriya";
	@TableField
	public static final transient String FROM_ID = "fromid";
	@TableField
	public static final transient String TO_ID = "toid";
	@TableField
	public static final transient String NOMER_VEDOMOSTI = "nved";
	@TableField
	public static final transient String SEAT = "seat";
	@TableField
	public static final transient String TARIF = "tarif";
	@TableField
	public static final transient String TARIF_FACT = "tariff";
	@TableField
	public static final transient String KOM_SBOR = "komsb";
	@TableField
	public static final transient String OB_STRAH = "obstr";
	@TableField
	public static final transient String TICKET_TYPE = "tipb";
	@TableField
	public static final transient String PAROM = "parom";
	@TableField
	public static final transient String PAROM_TARIF = "tarifp";
	@TableField
	public static final transient String PAROM_TARIF_FACT = "tariffp";
	@TableField
	public static final transient String PAROM_TICKET_TYPE = "tipbp";
	@TableField
	public static final transient String FIRSTNAME = "firstname";
	@TableField
	public static final transient String LASTNAME = "lastname";
	@TableField
	public static final transient String MIDDLENAME = "middlename";
	@TableField
	public static final transient String BIRTH_PLACE = "birthplace";
	@TableField
	public static final transient String TICKET_STATUS = "status";
	@TableField
	public static final transient String BILL_ID = "billid";
	@TableField
	public static final transient String DEPARTURE = "departure";
	@TableField
	public static final transient String ARRIVAL = "arrival";
	@TableField
	public static final transient String REIS_ID = "reisid";
	@TableField
	public static final transient String REIS_NAME = "reisname";
	@TableField
	public static final transient String BIRTH_DATE = "birthdate";
	@TableField
	public static final transient String SELL_TIME = "tbilet";
	@TableField
	public static final transient String PHONE = "phone";
	@TableField
	public static final transient String V_DATE = "vdate";
	@TableField
	public static final transient String BAGGAGE = "baggage";
	@TableField
	public static final transient String BAG1 = "bag1";
	@TableField
	public static final transient String BAG2 = "bag2";
	@TableField
	public static final transient String BAGTARIF = "bagtarif";
	@TableField
	public static final transient String TBTARIF = "tbtarif";
	@TableField
	public static final transient String GRAJD = "grajd";
	@TableField
	public static final transient String GENDER = "gender";
	@TableField
	public static final transient String PRICE = "price";
	@TableField
	public static final transient String PRICE_BAG = "price_bag";
	@TableField
	public static final transient String IS_HIDDEN = "is_hidden";
	@TableField
	public static final transient String STAV_TICKET_NUMBER = "stav_ticket_number";
	@TableField
	public static final transient String STAV_BAG1 = "stav_bag1";
	@TableField
	public static final transient String STAV_BAG2 = "stav_bag2";

	public static final transient String FROM = "from";
	public static final transient String TO = "to";
	public static final transient String TKTNUMBER = "tktnumber";
	public static final transient String ISBAGGAGE = "isbaggage";
	public static final transient String DOCTYPENAME = "doctypename";
	public static final transient String AMOUNT = "amount";
	public static final transient String PAYMENT_ORDER_URL = "payment_order_url";

	public static final transient String STAV_NATION_ID = "stav_nation_id";
	public static final transient String STAV_DOC_TYPE_ID = "stav_doc_type_id";
	public static final transient String STAV_NUMBER_TICKET = "number_ticket";
	public static final transient String STAV_NUMBER_BAGGAGE = "number_baggage";

	public Ticket() {
		super(Ticket.class.getName());
	}

	public String getFirstname() {
		return getStringProp(FIRSTNAME);
	}
	public void setFirstname(String val) {
		put(FIRSTNAME, val);
	}

	public String getLastname() {
		return getStringProp(LASTNAME);
	}
	public void setLastname(String val) {
		put(LASTNAME, val);
	}

	public String getMiddlename() {
		return getStringProp(MIDDLENAME);
	}
	public void setMiddlename(String val) {
		put(MIDDLENAME, val);
	}

	public Long getDocument() {
		return getLongProp(DOCUMENT);
	}
	public void setDocument(Long val) {
		put(DOCUMENT, val);
	}

	public String getSeriya() {
		return getStringProp(SERIYA);
	}
	public void setSeriya(String val) {
		put(SERIYA, val);
	}

	public String getNumber() {
		return getStringProp(NUMBER);
	}
	public void setNumber(String val) {
		put(NUMBER, val);
	}

	public Date getBirthDate() {
		return getDateProp(BIRTH_DATE);
	}
	public void setBirthDate(Date val) {
		put(BIRTH_DATE, val);
	}

	public String getBirthPlace() {
		return getStringProp(BIRTH_PLACE);
	}
	public void setBirthPlace(String val) {
		put(BIRTH_PLACE, val);
	}

	public String getPhone() {
		return getStringProp(PHONE);
	}
	public void setPhone(String val) {
		put(PHONE, val);
	}

	public Long getUserId() {
		return getLongProp(USER_ID);
	}
	public void setUserId(Long val) {
		put(USER_ID, val);
	}

	public Long getFromId() {
		return getLongProp(FROM_ID);
	}
	public void setFromId(Long val) {
		put(FROM_ID, val);
	}

	public Long getToId() {
		return getLongProp(TO_ID);
	}
	public void setToId(Long val) {
		put(TO_ID, val);
	}

	public Long getNomerVedomosti() {
		return getLongProp(NOMER_VEDOMOSTI);
	}
	public void setNomerVedomosti(Long val) {
		put(NOMER_VEDOMOSTI, val);
	}

	public Long getSeat() {
		return getLongProp(SEAT);
	}
	public void setSeat(Long val) {
		put(SEAT, val);
	}

	public Date getSellTime() {
		return getDateProp(SELL_TIME);
	}
	public void setSellTime(Date val) {
		put(SELL_TIME, val);
	}

	public Long getTarif() {
		return getLongProp(TARIF);
	}
	public void setTarif(Long val) {
		put(TARIF, val);
	}

	public Long getFactTarif() {
		return getLongProp(TARIF_FACT);
	}
	public void setFactTarif(Long val) {
		put(TARIF_FACT, val);
	}

	public Long getKomSbor() {
		return getLongProp(KOM_SBOR);
	}
	public void setKomSbor(Long val) {
		put(KOM_SBOR, val);
	}

	public Long getObStrah() {
		return getLongProp(OB_STRAH);
	}
	public void setObStrah(Long val) {
		put(OB_STRAH, val);
	}

	public String getTicketType() {
		return getStringProp(TICKET_TYPE);
	}
	public void setTicketType(String val) {
		put(TICKET_TYPE, val);
	}

	public Boolean getParom() {
		return getBooleanProp(PAROM);
	}
	public void setParom(Boolean val) {
		put(PAROM, val);
	}

	public Long getParomTarif() {
		return getLongProp(PAROM_TARIF);
	}
	public void setParomTarif(Long val) {
		put(PAROM_TARIF, val);
	}

	public Long getFactParomTarif() {
		return getLongProp(PAROM_TARIF_FACT);
	}
	public void setFactParomTarif(Long val) {
		put(PAROM_TARIF_FACT, val);
	}

	public String getParomTicketType() {
		return getStringProp(PAROM_TICKET_TYPE);
	}
	public void setParomTicketType(String val) {
		put(PAROM_TICKET_TYPE, val);
	}

	public TicketStatus getStatus() {
		return getEnumProp(TICKET_STATUS);
	}
	public void setStatus(TicketStatus val) {
		put(TICKET_STATUS, val);
	}

	public Long getBillId() {
		return getLongProp(BILL_ID);
	}
	public void setBillId(Long val) {
		put(BILL_ID, val);
	}

	public Date getDeparture() {
		return getDateProp(DEPARTURE);
	}
	public void setDeparture(Date val) {
		put(DEPARTURE, val);
	}

	public Date getArrival() {
		return getDateProp(ARRIVAL);
	}
	public void setArrival(Date val) {
		put(ARRIVAL, val);
	}

	public Long getReisId() {
		return getLongProp(REIS_ID);
	}
	public void setReisId(Long val) {
		put(REIS_ID, val);
	}

	public String getReisName() {
		return getStringProp(REIS_NAME);
	}
	public void setReisName(String val) {
		put(REIS_NAME, val);
	}

	public Date getVdate() {
		return getDateProp(V_DATE);
	}
	public void setVdate(Date val) {
		set(V_DATE, val);
	}

	public Long getBaggage() {
		Long ret = 0L;
		if ((getBag1() != null) && (getBag1() != 0L))
			ret++;
		if ((getBag2() != null) && (getBag2() != 0L))
			ret++;
		return ret;
	}
	public void setBaggage(Long val) {
		set(BAGGAGE, val);
	}

	public String getFrom() {
		return getStringProp(FROM);
	}
	public void setFrom(String val) {
		set(FROM, val);
	}

	public String getTo() {
		return getStringProp(TO);
	}
	public void setTo(String val) {
		set(TO, val);
	}

	public Long getBag1() {
		return getLongProp(BAG1);
	}
	public void setBag1(Long val) {
		set(BAG1, val);
	}

	public Long getBag2() {
		return getLongProp(BAG2);
	}
	public void setBag2(Long val) {
		set(BAG2, val);
	}

	public Long getBagTarif() {
		return getLongProp(BAGTARIF);
	}
	public void setBagTarif(Long val) {
		set(BAGTARIF, val);
	}

	public Long getTBTarif() {
		return getLongProp(TBTARIF);
	}
	public void setTBTarif(Long val) {
		set(TBTARIF, val);
	}

	public Gender getGender() {
		return getEnumProp(GENDER);
	}
	public void setGender(Gender val) {
		put(GENDER, val);
	}

	public Long getGrajd() {
		return getLongProp(GRAJD);
	}
	public void setGrajd(Long val) {
		put(GRAJD, val);
	}

	public String getTktNumber() {
		return getStringProp(TKTNUMBER);
	}
	public void setTktNumber(String val) {
		set(TKTNUMBER, val);
	}

	public Boolean isBaggage() {
		return getBooleanProp(ISBAGGAGE);
	}
	public void setBaggage(Boolean isBaggage) {
		set(ISBAGGAGE, isBaggage);
	}

	public String getDocTypeName() {
		return getStringProp(DOCTYPENAME);
	}
	public void setDocTypeName(String val) {
		set(DOCTYPENAME, val);
	}

	public Long getPrice() {
		return getLongProp(PRICE);
	}
	public void setPrice(Long price) {
		set(PRICE, price);
	}

	public Long getPriceBag() {
		return getLongProp(PRICE_BAG);
	}
	public void setPriceBag(Long price) {
		set(PRICE_BAG, price);
	}

	public Long getAmount() {
		return getLongProp(AMOUNT);
	}
	public void setAmount(Long amount) {
		set(AMOUNT, amount);
	}

	public Boolean isHidden() {
		return getBooleanProp(IS_HIDDEN);
	}
	public void setHidden(Boolean isHidden) {
		set(IS_HIDDEN, isHidden);
	}

	public String getPaymentOrderURL() {
		return getStringProp(PAYMENT_ORDER_URL);
	}
	public void setPaymentOrderURL(String orderUrl) {
		set(PAYMENT_ORDER_URL, orderUrl);
	}

	public String getStavNationId() {
		return getStringProp(STAV_NATION_ID);
	}
	public void setStavNationId(String id) {
		set(STAV_NATION_ID, id);
	}

	public Long getStavDocTypeId() {
		return getLongProp(STAV_DOC_TYPE_ID);
	}
	public void setStavDocTypeId(Long id) {
		set(STAV_DOC_TYPE_ID, id);
	}

	public String getStavNumberTicket() {
		return getStringProp(STAV_NUMBER_TICKET);
	}
	public void setStavNumberTicket(String num) {
		set(STAV_NUMBER_TICKET, num);
	}

	public String getStavBaggageNumber() {
		return getStringProp(STAV_NUMBER_BAGGAGE);
	}
	public void setStavBaggageNumber(String num) {
		set(STAV_NUMBER_BAGGAGE, num);
	}

	public String getStavTicketNumber() {
		return getStringProp(STAV_TICKET_NUMBER);
	}
	public void setStavTicketNumber(String num) {
		put(STAV_TICKET_NUMBER, num);
	}

	public String getStavBag1() {
		return getStringProp(STAV_BAG1);
	}
	public void setStavBag1(String num) {
		put(STAV_BAG1, num);
	}

	public String getStavBag2() {
		return getStringProp(STAV_BAG2);
	}
	public void setStavBag2(String num) {
		put(STAV_BAG2, num);
	}

}