/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui.grid.cells;

import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 16.09.2012 14:22:55
 */
public class ButtonCell<T extends BaseModel> extends ActionCell<T> {

	interface Template extends SafeHtmlTemplates {
		@Template("<button type=\"button\" tabindex=\"-1\" class=\"gwt-Button {0}\">{1}</button>")
		SafeHtml button(String className, String text);
	}

	private static final Template template = GWT.create(Template.class);

	private final SafeHtml btn;

	/**
	 * @param text
	 * @param delegate
	 */
	public ButtonCell(String text, ActionCell.Delegate<T> delegate) {
		super(text, delegate);
		btn = template.button("", text);	// TODO стиль
	}

	@Override
	public void render(Context context, T value, SafeHtmlBuilder sb) {
		sb.append(btn);
	}

}