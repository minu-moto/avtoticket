/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid.cells;

import java.io.IOException;

import com.avtoticket.client.ui.ProvidesValue;
import com.avtoticket.client.ui.grid.ListFilter;
import com.avtoticket.client.ui.grid.View;
import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.text.shared.Renderer;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 15.08.2013 10:35:42
 */
public class EnumCell<E extends Enum<E>, C extends BaseModel> extends ListCell<E, E, C> {

	public EnumCell(View<C> grid, boolean editable, boolean allowNull, ListFilter<E, C> filter) {
		super(grid, editable, allowNull, new ProvidesKey<E>() {
			@Override
			public String getKey(E item) {
				return (item != null) ? item.name() : null;
			}
		}, new ProvidesValue<E, E>() {
			@Override
			public E getValue(E item) {
				return item;
			}
		}, new Renderer<E>() {
			@Override
			public String render(E object) {
				return (object != null) ? object.toString() : "";
			}

			@Override
			public void render(E object, Appendable appendable) throws IOException {
				appendable.append(render(object));
			}
		}, filter);
	}

}