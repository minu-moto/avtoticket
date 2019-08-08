/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui.grid.cells;

import static com.google.gwt.dom.client.BrowserEvents.BLUR;
import static com.google.gwt.dom.client.BrowserEvents.KEYUP;

import com.avtoticket.client.ui.grid.Grid;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 16.04.2014 12:12:44
 */
public class TextAreaCell extends EditTextCell {

	interface Template extends SafeHtmlTemplates {
		@Template("<textarea class=\"{0}\" tabindex=\"-1\">{1}</textarea>")
		SafeHtml textarea(String className, String value);
	}

	private final static Template template = GWT.create(Template.class);

	private final SafeHtmlRenderer<String> renderer = SimpleSafeHtmlRenderer.getInstance();

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		Object viewData = getViewData(context.getKey());
		if (viewData != null)
			sb.append(template.textarea("", value));	// TODO
		else if ((value != null) && (value.trim().length() > 0))
			sb.append(renderer.render((value.length() > 200) ?
					value.substring(0, 100) + " ... " + value.substring(value.length() - 100) : value));
		else
			sb.appendHtmlConstant("\u00A0");
	}

	@Override
	protected void edit(Context context, Element parent, String value) {
		super.edit(context, parent, value);
		parent.getParentElement().addClassName(Grid.CSS.atEditCell());
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, String value,
			NativeEvent event, ValueUpdater<String> valueUpdater) {
		boolean handled = false;
		if (isEditing(context, parent, value))
			handled = editEvent(context, parent, event, value, valueUpdater);
		if (!handled)
			super.onBrowserEvent(context, parent, value, event, valueUpdater);
	}

	private boolean editEvent(Context context, Element parent,
			NativeEvent event, String value, ValueUpdater<String> valueUpdater) {
		boolean prevent = false;
		String type = event.getType();
		if (KEYUP.equals(type)) {
			int keyCode = event.getKeyCode();
			prevent = (keyCode == KeyCodes.KEY_ENTER);
			if (keyCode == KeyCodes.KEY_ESCAPE)
				blur(parent);
		} else if (BLUR.equals(type)) {
			EventTarget eventTarget = event.getEventTarget();
			if (Element.is(eventTarget)) {
				Element target = Element.as(eventTarget);
				if (TextAreaElement.TAG.equalsIgnoreCase(target.getTagName())) {
					blur(parent);
					commit(context, parent, valueUpdater);
				}
			}
		}
		return prevent;
	}

	protected void blur(Element parent) {
		parent.getParentElement().removeClassName(Grid.CSS.atEditCell());
	}

	private void commit(Context context, Element parent, ValueUpdater<String> valueUpdater) {
		TextAreaElement input = parent.getFirstChild().cast();
	    String value = input.getValue();
		clearViewData(context.getKey());
		setValue(context, parent, value);
		if (valueUpdater != null)
			valueUpdater.update(value);
	}

}