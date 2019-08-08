/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.shared.models.core;

import com.avtoticket.shared.models.EnumType;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 3 окт. 2016 г. 19:54:57
 */
@EnumType("core.locales")
public enum Locales {

	RU("Русский"),
	DE("Deutsche");

	private String text;

	private Locales(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}

}