/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.utils;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 7 янв. 2016 г. 0:13:19
 */
@FunctionalInterface
public interface DefaultCallback<T> extends AsyncCallback<T> {

	@Override
	default void onFailure(Throwable caught) {
		Window.alert(caught.getMessage());
	}

}