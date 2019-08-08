package com.avtoticket.shared.models.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.avtoticket.shared.models.BaseModel;

public class Sms extends BaseModel {

	private static final long serialVersionUID = -5204140058328954963L;

	public static final transient String SENDER_ID = "sender_id";
	public static final transient String PHONE = "phone";
	public static final transient String MSG = "msg";
	public static final transient String SEND_DATE = "senddate";
	public static final transient String STATUS_CODE = "statuscode";
	public static final transient String SMS_ID = "smsid";
	public static final transient String LATIN = "latin";
	public static final transient String SUB_SMS_COUNT = "sub_sms_count";

	public static final Long Status_Sended = 0L;
	public static final Long Status_Undelivered = 1L;
	public static final Long Status_Delivered = 2L;
	public static final Long Status_Expired = 3L;
	public static final Long Status_Error = 4L;
	public static final Long Status_Deleted = 5L;

	@SuppressWarnings("serial")
	private static final Map<Long, String> statusmsg = new HashMap<Long, String>() {{
		put(Status_Error, "Неизвестная ошибка");
		put(Status_Sended, "Сообщение отправлено");
		put(Status_Undelivered, "Сообщение не доставлено");
		put(Status_Delivered, "Сообщение доставлено");
		put(Status_Expired, "Истек срок доставки сообщения");
		put(Status_Deleted, "Сообщение удалено");
	}};

	public Sms() {
		super(Sms.class.getName());
	}

	public void setSenderId(Long val) {
		set(SENDER_ID, val);
	}
	public Long getSenderId() {
		return getLongProp(SENDER_ID);
	}

	public void setPhone(String val) {
		set(PHONE, val);
	}
	public String getPhone() {
		return getStringProp(PHONE);
	}

	public void setMsg(String val) {
		set(MSG, val);
	}
	public String getMsg() {
		return getStringProp(MSG);
	}

	public void setSendDate(Date val) {
		set(SEND_DATE, val);
	}
	public Date getSendDate() {
		return getDateProp(SEND_DATE);
	}

	public void setStatusCode(Long val) {
		set(STATUS_CODE, val);
	}
	public Long getStatusCode() {
		return getLongProp(STATUS_CODE);
	}

	public void setSmsId(String val) {
		set(SMS_ID, val);
	}
	public String getSmsId() {
		return getStringProp(SMS_ID);
	}

	public void setIsLatin(Boolean val) {
		set(LATIN, val);
	}
	public Boolean isLatin() {
		return getBooleanProp(LATIN);
	}

	public void setSubSmsCount(Long val) {
		set(SUB_SMS_COUNT, val);
	}
	public Long getSubSmsCount() {
		return getLongProp(SUB_SMS_COUNT);
	}

	public String getStatusMsg() {
		return ((getStatusCode() != null) && (statusmsg.get(getStatusCode()) != null)) ? statusmsg.get(getStatusCode()) : "";
	}

}