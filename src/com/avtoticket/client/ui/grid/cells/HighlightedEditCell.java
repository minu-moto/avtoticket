/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid.cells;

import static com.google.gwt.dom.client.BrowserEvents.BLUR;
import static com.google.gwt.dom.client.BrowserEvents.KEYUP;

import com.avtoticket.client.ui.grid.Grid;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.text.shared.SafeHtmlRenderer;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 11.07.2013 15:38:22
 */
public class HighlightedEditCell extends EditTextCell {

	private boolean enableHighlight;

	public HighlightedEditCell() {
		this(true);
	}

	public HighlightedEditCell(boolean enableHighlight) {
		this.enableHighlight = enableHighlight;
	}

	public HighlightedEditCell(SafeHtmlRenderer<String> renderer) {
		super(renderer);
		this.enableHighlight = true;
	}

	@Override
	protected void edit(Context context, Element parent, String value) {
		super.edit(context, parent, value);
		if (enableHighlight)
			parent.getParentElement().addClassName(Grid.CSS.atEditCell());
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, String value,
			NativeEvent event, ValueUpdater<String> valueUpdater) {
		if (enableHighlight && isEditing(context, parent, value))
			editEvent(parent, event, value);
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
	}

	private void editEvent(Element parent, NativeEvent event, String value) {
		String type = event.getType();
		if (KEYUP.equals(type)) {
			int keyCode = event.getKeyCode();
			if ((keyCode == KeyCodes.KEY_ENTER) || (keyCode == KeyCodes.KEY_ESCAPE))
				blur(parent);
		} else if (BLUR.equals(type)) {
			EventTarget eventTarget = event.getEventTarget();
			if (Element.is(eventTarget)) {
				Element target = Element.as(eventTarget);
				if (InputElement.TAG.equalsIgnoreCase(target.getTagName()))
					blur(parent);
			}
		}
	}

	protected void blur(Element parent) {
		parent.getParentElement().removeClassName(Grid.CSS.atEditCell());
	}

}