/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.utils;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.place.shared.Place;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 дек. 2015 г. 19:33:24
 */
public abstract class ATPlace extends Place {

	public static final String PARAM_SEPARATOR = "/";

	public Activity getActivity() {
		return getActivity(null);
	}

	public abstract Activity getActivity(Object param);

}