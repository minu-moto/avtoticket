/*
 * Copyright MinuSoft (c) 2015.
 */
package com.avtoticket.client.utils;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 27 мая 2015 г. 22:52:31
 */
public class Waiter extends PopupPanel {

	private static Waiter INSTANCE = new Waiter();

	public Waiter() {
		super(false, true);
		setAnimationEnabled(false);
		setGlassEnabled(true);
		getGlassElement().getStyle().setOpacity(0);
		getGlassElement().getStyle().setCursor(Cursor.WAIT);
		setPopupPosition(-100, -100);
	}

	public static void start() {
		INSTANCE.show();
	}

	public static void stop() {
		INSTANCE.hide();
	}

}