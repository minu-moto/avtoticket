/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.shared.models.core;

import com.avtoticket.shared.models.EnumType;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 15 янв. 2016 г. 14:13:18
 */
@EnumType("core.gender")
public enum Gender {

	MALE("Мужской"),
	FEMALE("Женский");

	private String text;

	private Gender(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}

}