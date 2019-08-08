/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.shared.models.core;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.View;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 26 янв. 2016 г. 22:55:35
 */
@View("core.thelp")
@Table("core.thelp")
public class Help extends BaseModel {

	private static final long serialVersionUID = -6105898340520382060L;

	@TableField
	public static final transient String NAME = "name";
	@TableField
	public static final transient String TEXT = "text";
	@TableField
	public static final transient String TYPE = "type";
	@TableField
	public static final transient String LOCALE = "locale";

	public Help() {
		super(Help.class.getName());
	}

	public String getName() {
		return getStringProp(NAME);
	}
	public void setName(String name) {
		set(NAME, name);
	}

	public String getText() {
		return getStringProp(TEXT);
	}
	public void setText(String text) {
		set(TEXT, text);
	}

	public HelpType getType() {
		return getEnumProp(TYPE);
	}
	public void setType(HelpType type) {
		set(TYPE, type);
	}

	public Locales getLocale() {
		return getEnumProp(LOCALE);
	}
	public void setLocale(Locales locale) {
		set(LOCALE, locale);
	}

}