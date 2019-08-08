/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid.fields;

import com.avtoticket.client.ui.ModelListDataProvider;
import com.avtoticket.client.ui.grid.cells.EnumCell;
import com.avtoticket.shared.models.BaseModel;

import com.google.gwt.cell.client.Cell;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 15.08.2013 13:39:12
 */
public class EnumFieldBuilder<E extends Enum<E>, C extends BaseModel> extends ListFieldGenericBuilder<EnumFieldBuilder<E, C>, E, E, C> {

	private Class<E> clazz;

	public Class<E> getEnumClass() {
		return clazz;
	}
	public EnumFieldBuilder<E, C> enumClass(Class<E> clazz) {
		this.clazz = clazz;
		return provider(new ModelListDataProvider<E>(clazz));
	}

	@Override
	protected Cell<E> createCell() {
		EnumCell<E, C> ret = new EnumCell<E, C>(getGrid(), isEditable(), !isRequire(), getListFilter());
		if (getListProvider() != null)
			getListProvider().addDataDisplay(ret);
		return ret;
	}

}