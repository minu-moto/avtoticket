/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.Header;

/**
 * Подвал столбца таблицы с отключенным обработчиком сортировки
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 26.03.2014 23:13:38
 */
public abstract class Footer<H> extends Header<H> {

	public Footer(Cell<H> cell) {
		super(cell);
	}

	@Override
	public boolean onPreviewColumnSortEvent(Context context, Element elem, NativeEvent event) {
		return false;
	}

}