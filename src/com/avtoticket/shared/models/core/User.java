/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.shared.models.core;

import java.util.Date;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.UUID;
import com.avtoticket.shared.models.View;

/**
 * Модель пользователя
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 23 нояб. 2015 г. 22:39:55
 */
@View("core.user")
@Table("core.tuser")
public class User extends BaseModel {

	private static final long serialVersionUID = -3707490128887872798L;

	@TableField
	public static final transient String LOGIN = "login";
	@TableField
	public static final transient String PHONE = "phone";
	@TableField
	public static final transient String FIRSTNAME = "firstname";
	@TableField
	public static final transient String LASTNAME = "lastname";
	@TableField
	public static final transient String MIDDLENAME = "middlename";
	@TableField
	public static final transient String PASP_SERIYA = "paspseriya";
	@TableField
	public static final transient String PASP_NUMBER = "paspnumber";
	@TableField
	public static final transient String B_DATE = "bdate";
	@TableField
	public static final transient String B_PLACE = "bplace";
	@TableField
	public static final transient String IS_ADMIN = "isadmin";
	@TableField
	public static final transient String IS_PRINT_TICKET = "isprintticket";
	@TableField
	public static final transient String REG_DATE = "regdate";
	@TableField
	public static final transient String DOCTYPE = "doctype";
	@TableField
	public static final transient String V_DATE = "vdate";
	@TableField
	public static final transient String SNILS = "snils";
	@TableField
	public static final transient String GENDER = "gender";
	@TableField
	public static final transient String GRAJD = "grajd";
	@TableField
	public static final transient String AVATAR_ID = "avatar_id";
	@TableField
	public static final transient String API_TOKEN = "api_token";

	public static final transient String DOCUMENT = "document";
	public static final transient String GRAJDNAME = "grajdname";
	public static final transient String BAGS = "bags";
	public static final transient String IS_CHILD = "is_child";
//	public static final transient String PASSWORD = "password";

	public User() {
		this(User.class.getName());
	}

	public User(String className) {
		super(className);
	}

	public String getLogin() {
		return getStringProp(LOGIN);
	}
	public void setLogin(String val) {
		set(LOGIN, val);
	}

	public String getPhone() {
		return getStringProp(PHONE);
	}
	public void setPhone(String val) {
		set(PHONE, val);
	}

	public String getFirstName() {
		return getStringProp(FIRSTNAME);
	}
	public void setFirstName(String val) {
		set(FIRSTNAME, val);
	}

	public String getLastName() {
		return getStringProp(LASTNAME);
	}
	public void setLastName(String val) {
		set(LASTNAME, val);
	}

	public String getMiddleName() {
		return getStringProp(MIDDLENAME);
	}
	public void setMiddleName(String val) {
		set(MIDDLENAME, val);
	}

	public String getPaspSeriya() {
		return getStringProp(PASP_SERIYA);
	}
	public void setPaspSeriya(String val) {
		set(PASP_SERIYA, val);
	}

	public String getPaspNumber() {
		return getStringProp(PASP_NUMBER);
	}
	public void setPaspNumber(String val) {
		set(PASP_NUMBER, val);
	}

	public Date getBirthday() {
		return getDateProp(B_DATE);
	}
	public void setBirthday(Date val) {
		set(B_DATE, val);
	}

	public String getBplace() {
		return getStringProp(B_PLACE);
	}
	public void setBplace(String val) {
		set(B_PLACE, val);
	}

	public Boolean isAdmin() {
		return getBooleanProp(IS_ADMIN);
	}
	public void setIsAdmin(Boolean val) {
		put(IS_ADMIN, val);
	}

	public Boolean isPrintTicket() {
		return getBooleanProp(IS_PRINT_TICKET);
	}
	public void setPrintTicket(Boolean val) {
		put(IS_PRINT_TICKET, val);
	}

	public Date getRegdate() {
		return getDateProp(REG_DATE);
	}
	public void setRegdate(Date val) {
		set(REG_DATE, val);
	}

	public Long getDocType() {
		return getLongProp(DOCTYPE);
	}
	public void setDocType(Long val) {
		put(DOCTYPE, val);
	}

	public Date getVdate() {
		return getDateProp(V_DATE);
	}
	public void setVdate(Date val) {
		set(V_DATE, val);
	}

	public void setSnils(String val) {
		set(SNILS, val);
	}
	public String getSnils() {
		return getStringProp(SNILS);
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

	public String getAvatarId() {
		return getStringProp(AVATAR_ID);
	}
	public void setAvatarId(String id) {
		put(AVATAR_ID, id);
	}

	/**
	 * @return ключ доступа к программному интерфейсу системы
	 */
	public UUID getApiToken() {
		return (UUID) get(API_TOKEN);
	}
	public void setApiToken(UUID apiToken) {
		set(API_TOKEN, apiToken);
	}

	public String getDocument() {
		return getStringProp(DOCUMENT);
	}
	public void setDocument(String val) {
		put(DOCUMENT, val);
	}

	public String getGrajdName() {
		return getStringProp(GRAJDNAME);
	}
	public void setGrajdName(String val) {
		put(GRAJDNAME, val);
	}

	public Long getBags() {
		return getLongProp(BAGS);
	}
	public void setBags(Long bags) {
		set(BAGS, bags);
	}

	public Boolean isChild() {
		return getBooleanProp(IS_CHILD);
	}
	public void setIsChild(Boolean isChild) {
		set(IS_CHILD, isChild);
	}

}