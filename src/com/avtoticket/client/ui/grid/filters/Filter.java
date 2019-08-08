/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui.grid.filters;

import com.avtoticket.client.ui.grid.filters.FilterChangeEvent.HasFilterChangeHandlers;
import com.avtoticket.shared.models.Where;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 18.09.2014 18:56:00
 */
public interface Filter extends HasFilterChangeHandlers {

	public Where getFilter();

	public void clearFilter();

	public void clearFilter(boolean fireEvent);

}