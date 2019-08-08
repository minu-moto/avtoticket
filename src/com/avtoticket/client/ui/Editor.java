/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui;

import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 06.03.2014 14:52:58
 */
public interface Editor<T extends BaseModel> extends IsWidget {

	public void edit(T model);

	public boolean flush();

	public T getModel();

	public void setEnabled(boolean isEnabled);

}