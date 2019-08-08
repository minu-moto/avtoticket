/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid.fields;

import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ImageResourceCell;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 05.10.2013 0:51:39
 */
public class ImageFieldBuilder<T, C extends BaseModel> extends FieldGenericBuilder<ImageFieldBuilder<T, C>, T, C> {

	@SuppressWarnings("unchecked")
	@Override
	protected Cell<T> createCell() {
		return (Cell<T>) new ImageResourceCell();
	}

}