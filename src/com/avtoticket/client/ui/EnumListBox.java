/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui;

import java.io.IOException;
import java.util.Arrays;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.view.client.ListDataProvider;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 05.03.2013 0:29:16
 */
public class EnumListBox<E extends Enum<E>> extends BaseListBox<E> {

	private ListDataProvider<E> provider;

	public EnumListBox(boolean allowNull, Class<E> clazz) {
		this(allowNull, clazz, new Renderer<E>() {
			@Override
			public String render(E object) {
				return (object != null) ? object.toString() : "";
			}

			@Override
			public void render(E object, Appendable appendable)
					throws IOException {
				appendable.append(render(object));
			}
		});
	}

	public EnumListBox(boolean allowNull, Class<E> clazz, Renderer<E> renderer) {
		super(allowNull, new ProvidesValue<String, E>() {
			@Override
			public String getValue(E item) {
				return item.toString();
			}
		}, renderer);

		provider = new ListDataProvider<E>(Arrays.asList(((Class<E>) clazz).getEnumConstants()));
		provider.addDataDisplay(this);
	}

	public void refresh() {
		provider.refresh();
	}

	@Override
	public Iterable<E> getVisibleItems() {
		return provider.getList();
	}

}