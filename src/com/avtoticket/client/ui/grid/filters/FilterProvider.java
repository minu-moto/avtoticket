/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui.grid.filters;

import java.util.Collection;

import com.avtoticket.shared.models.Where;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 06.10.2014 15:38:26
 */
public interface FilterProvider<T> {

	public Where getFilter(Collection<T> items);

}