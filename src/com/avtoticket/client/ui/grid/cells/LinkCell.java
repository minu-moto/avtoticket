/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid.cells;

import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.text.shared.SafeHtmlRenderer;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 08.03.2013 11:42:47
 */
public class LinkCell<T extends BaseModel> extends ActionCell<T> {

	public interface Template extends SafeHtmlTemplates {
		@Template("<a href=\"javascript:;\">{0}</a>")
		SafeHtml link(String text);

		@Template("<a href=\"{0}\">{1}</a>")
		SafeHtml link(SafeUri href, String text);
	}

	public static final Template template = GWT.create(Template.class);

	private SafeHtmlRenderer<T> htmlRenderer;
	private final Delegate<T> delegate;

	/**
	 * @param text - текст ссылки
	 * @param delegate - действие по нажатию
	 */
	public LinkCell(final String text, ActionCell.Delegate<T> delegate) {
		this(new SafeHtmlRenderer<T>() {
			@Override
			public SafeHtml render(T context) {
				return template.link(text);
			}

			@Override
			public void render(T object, SafeHtmlBuilder builder) {
				builder.append(render(object));
			}
		}, delegate);
	}

	/**
	 * @param htmlRenderer
	 * @param delegate - действие по нажатию
	 */
	public LinkCell(SafeHtmlRenderer<T> htmlRenderer, ActionCell.Delegate<T> delegate) {
		super("", delegate);
		this.delegate = delegate;
		this.htmlRenderer = htmlRenderer;
	}

	@Override
	public void render(Context context, T value, SafeHtmlBuilder sb) {
		sb.append(htmlRenderer.render(value));
	}

	@Override
	protected void onEnterKeyDown(Context context, Element parent, T value, NativeEvent event, ValueUpdater<T> valueUpdater) {
		if (delegate != null)
			delegate.execute(value);
	}

}