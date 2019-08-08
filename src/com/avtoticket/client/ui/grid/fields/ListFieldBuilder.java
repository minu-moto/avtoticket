/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui.grid.fields;

import com.avtoticket.client.ui.grid.cells.ListCell;
import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.cell.client.Cell;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 28.07.2014 13:52:57
 */
public class ListFieldBuilder<T, C extends BaseModel> extends ListFieldGenericBuilder<ListFieldBuilder<T, C>, Long, T, C> {

	@Override
	protected Cell<Long> createCell() {
		ListCell<Long, T, C> lc = new ListCell<Long, T, C>(getGrid(), isEditable(), !isRequire(), getListFilter());
		if (getListProvider() != null)
			getListProvider().addDataDisplay(lc);
		return lc;
	}

}