/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid;

import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.user.cellview.client.Header;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 21.10.2013 15:44:28
 */
public class MenuHeader<С extends BaseModel> extends Header<String> {

	private String caption;

	public MenuHeader(Field<?, С> info, String caption) {
		super(new MenuHeaderCell<С>(info));
		this.caption = caption;
	}

	@Override
	public String getValue() {
		return caption;
	}

	public void setValue(String caption) {
		this.caption = caption;
	}

	@SuppressWarnings("unchecked")
	public void clearFilter() {
		((MenuHeaderCell<С>) getCell()).clearFilter();
	}

}