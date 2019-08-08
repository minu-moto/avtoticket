/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 26.12.2012 13:02:40
 */
public interface TabItem extends IsWidget {

	public String getCaption();

	public void refresh();

	public void onActivate();

	public void onDeactivate();

}