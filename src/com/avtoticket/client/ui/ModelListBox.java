/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui;

import java.io.IOException;

import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.text.shared.Renderer;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 14.09.2012 11:14:54
 */
public class ModelListBox<T extends BaseModel> extends BaseListBox<T> {

	public ModelListBox(boolean allowNull) {
		super(allowNull,
				new ProvidesValue<String, T>() {
					@Override
					public String getValue(T item) {
						return (item != null) ? item.getDisplayField() : null;
					}
				}, 
				new Renderer<T>() {
					@Override
					public String render(T model) {
						return (model != null) ? model.getDisplayField() : "";
					}
		
					@Override
					public void render(T model, Appendable appendable) throws IOException {
						appendable.append(render(model));
				}
		});
	}

}