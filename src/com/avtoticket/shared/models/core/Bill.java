package com.avtoticket.shared.models.core;

import java.util.Date;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.View;

@View("core.tbills")
@Table("core.tbills")
public class Bill extends BaseModel {

	private static final long serialVersionUID = 8844682665991407718L;

	@TableField
	public static final transient String PAYMENT_SYSTEM = "paymentsystem";
	@TableField
	public static final transient String OP_DATE = "opdate";
	@TableField
	public static final transient String AMOUNT = "amount";
	@TableField
	public static final transient String COMMENT = "coment";
	@TableField
	public static final transient String STATUS = "status";
	@TableField
	public static final transient String SEATS = "seats";
	@TableField
	public static final transient String BAGS = "bags";
	@TableField
	public static final transient String CHILDS = "childs";
	@TableField
	public static final transient String BP_STATUS = "bpstatus";
	@TableField
	public static final transient String USER_ID = "userid";
	@TableField
	public static final transient String PHONE = "phone";
	@TableField
	public static final transient String EMAIL = "email";
	@TableField
	public static final transient String USER_IP = "userip";
	@TableField
	public static final transient String USER_AGENT = "useragent";
	@TableField
	public static final transient String SUCCESS_URL = "success_url";
	@TableField
	public static final transient String FAIL_URL = "fail_url";
	@TableField
	public static final transient String PAYMENT_ORDER_URL = "payment_order_url";
	@TableField
	public static final transient String STAV_BOOKING_ID = "stav_booking_id";

	public Bill() {
		super(Bill.class.getName());
	}

	public Long getPaymentSystem() {
		return getLongProp(PAYMENT_SYSTEM);
	}
	public void setPaymentSystem(Long val) {
		set(PAYMENT_SYSTEM, val);
	}

	public Long getOpDate() {
		return getLongProp(OP_DATE);
	}
	public void setOpDate(Date val) {
		set(OP_DATE, val);
	}

	public Long getAmount() {
		return getLongProp(AMOUNT);
	}
	public void setAmount(Long val) {
		set(AMOUNT, val);
	}

	public String getComment() {
		return getStringProp(COMMENT);
	}
	public void setComment(String val) {
		set(COMMENT, val);
	}

	public Long getStatus() {
		return getLongProp(STATUS);
	}
	public void setStatus(Long val) {
		set(STATUS, val);
	}

	public Long getSeats() {
		return getLongProp(SEATS);
	}
	public void setSeats(Long val) {
		set(SEATS, val);
	}

	public Long getBags() {
		return getLongProp(BAGS);
	}
	public void setBags(Long val) {
		set(BAGS, val);
	}

	public Long getChilds() {
		return getLongProp(CHILDS);
	}
	public void setChilds(Long val) {
		set(CHILDS, val);
	}

	public TicketStatus getBpStatus() {
		return getEnumProp(BP_STATUS);
	}
	public void setBpStatus(TicketStatus val) {
		put(BP_STATUS, val);
	}

	public Long getUserId() {
		return getLongProp(USER_ID);
	}
	public void setUserId(Long val) {
		set(USER_ID, val);
	}

	public String getPhone() {
		return getStringProp(PHONE);
	}
	public void setPhone(String val) {
		set(PHONE, val);
	}

	public String getEmail() {
		return getStringProp(EMAIL);
	}
	public void setEmail(String val) {
		set(EMAIL, val);
	}

	public String getUserIp() {
		return getStringProp(USER_IP);
	}
	public void setUserIp(String val) {
		set(USER_IP, val);
	}

	public String getUserAgent() {
		return getStringProp(USER_AGENT);
	}
	public void setUserAgent(String val) {
		set(USER_AGENT, val);
	}

	public String getSuccessURL() {
		return getStringProp(SUCCESS_URL);
	}
	public void setSuccessURL(String val) {
		set(SUCCESS_URL, val);
	}

	public String getFailURL() {
		return getStringProp(FAIL_URL);
	}
	public void setFailURL(String val) {
		set(FAIL_URL, val);
	}

	public String getPaymentOrderURL() {
		return getStringProp(PAYMENT_ORDER_URL);
	}
	public void setPaymentOrderURL(String orderUrl) {
		set(PAYMENT_ORDER_URL, orderUrl);
	}

	public String getStavBookingId() {
		return getStringProp(STAV_BOOKING_ID);
	}
	public void setStavBookingId(String id) {
		set(STAV_BOOKING_ID, id);
	}

}