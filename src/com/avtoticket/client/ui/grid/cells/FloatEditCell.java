/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui.grid.cells;

import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 21.01.2014 18:35:12
 */
public class FloatEditCell<C extends BaseModel> extends HighlightedEditCell {

	private static String lastRendered;

	public FloatEditCell() {
		super(new SafeHtmlRenderer<String>() {
			@Override
			public SafeHtml render(String object) {
				lastRendered = object;
				return (object == null) ? SafeHtmlUtils.EMPTY_SAFE_HTML : SafeHtmlUtils.fromString(object);
			}

			@Override
			public void render(String object, SafeHtmlBuilder builder) {
				builder.append(SafeHtmlUtils.fromString(object));
			}
		});
	}

	@Override
	protected void blur(Element parent) {
		super.blur(parent);
		try {
			parent.getParentElement().getStyle().clearBackgroundColor();
			InputElement input = parent.getFirstChild().cast();
			NumberFormat.getFormat("0.#").parse(input.getValue());
		} catch (NumberFormatException e) {
			parent.getParentElement().getStyle().setBackgroundColor("#FFDDDD");
		}
	}

	public String getRenderedValue(Context context, String value) {
		lastRendered = null;
		render(context, value, new SafeHtmlBuilder());
		return lastRendered;
	}

}