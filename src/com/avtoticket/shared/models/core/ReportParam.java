/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.shared.models.core;

import com.avtoticket.shared.models.BaseModel;

/**
 * Модель параметра отчёта
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 18.07.2013 16:40:14
 */
public class ReportParam extends BaseModel {

	private static final long serialVersionUID = -36129600281044660L;

	/** Название */
	public static final transient String NAME = "name";
	/** Описание */
	public static final transient String DESCR = "descr";
	/** Тип */
	public static final transient String TYPE = "type";
	/** Значение по умолчанию */
	public static final transient String DEFAULT = "default";
	/** Запросить значение параметра у пользователя */
	public static final transient String IS_FOR_PROMPTING = "is_for_prompting";

	/**
	 * Новый экземпляр модели
	 */
	public ReportParam() {
		super(ReportParam.class.getName());
	}

	/**
	 * @return название
	 */
	public String getName() {
		return getStringProp(NAME);
	}
	public void setName(String name) {
		set(NAME, name);
	}

	/**
	 * @return описание
	 */
	public String getDescr() {
		return getStringProp(DESCR);
	}
	public void setDescr(String descr) {
		set(DESCR, descr);
	}

	/**
	 * @return тип
	 */
	public String getType() {
		return getStringProp(TYPE);
	}
	public void setType(String type) {
		set(TYPE, type);
	}

	/**
	 * @return значение по умолчанию
	 */
	public String getDefault() {
		return getStringProp(DEFAULT);
	}
	public void setDefault(String def) {
		set(DEFAULT, def);
	}

	/**
	 * @return запросить значение параметра у пользователя
	 */
	public Boolean isForPrompting() {
		return getBooleanProp(IS_FOR_PROMPTING);
	}
	public void setForPrompting(Boolean isForPrompting) {
		set(IS_FOR_PROMPTING, isForPrompting);
	}

}