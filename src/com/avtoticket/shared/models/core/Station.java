package com.avtoticket.shared.models.core;

import java.util.Date;
import java.util.List;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.View;

/**
 * Модель автостанции
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 23 нояб. 2015 г. 22:39:55
 */
@View("core.stations")
@Table("core.tstations")
public class Station extends BaseModel {

	private static final long serialVersionUID = 6769276394553171804L;

	@TableField
	public static final transient String NAME = "name";
	@TableField
	public static final transient String KPAS_ID = "stationid";
	@TableField
	public static final transient String NASPUNKT_ID = "naspunkt_id";
	@TableField
	public static final transient String IS_DEPARTURE_POINT = "is_departure_point";
	@TableField
	public static final transient String ADDRESS = "address";
	@TableField
	public static final transient String LAT = "lat";
	@TableField
	public static final transient String LNG = "lng";
	@TableField
	public static final transient String HOST = "host";
	@TableField
	public static final transient String STAV_ID = "stavid";

	public static final transient String NASPUNKT = "naspunkt_displayfield";
	public static final transient String DEP_DATES = "dep_dates";
	public static final transient String REGULAR_TO = "regular_to";

	public static final transient String ID_FROM = "id_from";
	public static final transient String ID_TO = "id_to";

	public Station() {
		super(Station.class.getName());
	}

	public String getName() {
		return getStringProp(NAME);
	}
	public void setName(String val) {
		set(NAME, val);
	}

	public Long getKpasId() {
		return getLongProp(KPAS_ID);
	}
	public void setKpasId(Long id) {
		set(KPAS_ID, id);
	}

	public Long getNaspunktId() {
		return getLongProp(NASPUNKT_ID);
	}
	public void setNaspunktId(Long val) {
		set(NASPUNKT_ID, val);
	}

	public Boolean isDeparturePoint() {
		return getBooleanProp(IS_DEPARTURE_POINT);
	}
	public void setDeparturePoint(Boolean isDeparturePoint) {
		set(IS_DEPARTURE_POINT, isDeparturePoint);
	}

	public String getAddress() {
		return getStringProp(ADDRESS);
	}
	public void setAddress(String val) {
		set(ADDRESS, val);
	}

	public Double getLatitude() {
		return getDoubleProp(LAT);
	}
	public void setLatitude(Double lat) {
		set(LAT, lat);
	}

	public Double getLongitude() {
		return getDoubleProp(LNG);
	}
	public void setLongitude(Double lng) {
		set(LNG, lng);
	}

	public String getNaspunkt() {
		return getStringProp(NASPUNKT);
	}
	public void setNaspunkt(String naspunkt) {
		set(NASPUNKT, naspunkt);
	}

	public String getHost() {
		return getStringProp(HOST);
	}
	public void setHost(String host) {
		set(HOST, host);
	}

	public List<Date> getDepDates() {
		return getListProp(DEP_DATES);
	}
	public void setDepDates(List<Date> depDates) {
		set(DEP_DATES, depDates);
	}

	public Date getRegularTo() {
		return getDateProp(REGULAR_TO);
	}
	public void setRegularTo(Date regularTo) {
		set(REGULAR_TO, regularTo);
	}

	public String getStavId() {
		return getStringProp(STAV_ID);
	}
	public void setStavId(String id) {
		set(STAV_ID, id);
	}

	public String getSourceStavFromId() {
		return getStringProp(ID_FROM);
	}
	public void setSourceStavFromId(String id) {
		set(ID_FROM, id);
	}

	public String getSourceStavToId() {
		return getStringProp(ID_TO);
	}
	public void setSourceStavToId(String id) {
		set(ID_TO, id);
	}

}