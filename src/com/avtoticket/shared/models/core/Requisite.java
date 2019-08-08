/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.shared.models.core;

import java.util.Date;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.View;

/**
 * Модель шаблона билета
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 23 нояб. 2015 г. 22:39:55
 */
@View("core.requisites")
@Table("core.trequisites")
public class Requisite extends BaseModel {

	private static final long serialVersionUID = -3707490128887872798L;

	@TableField
	public static final transient String TUSER_ID = "tuser_id";
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
	public static final transient String DOCTYPE = "doctype";
	@TableField
	public static final transient String V_DATE = "vdate";
	@TableField
	public static final transient String GENDER = "gender";
	@TableField
	public static final transient String GRAJD = "grajd";

	public static final transient String DOCUMENT = "document";
	public static final transient String GRAJDNAME = "grajdname";

	public Requisite() {
		this(Requisite.class.getName());
	}

	public Requisite(String className) {
		super(className);
	}

	public Long getUserId() {
		return getLongProp(TUSER_ID);
	}
	public void setUserId(Long id) {
		set(TUSER_ID, id);
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

}