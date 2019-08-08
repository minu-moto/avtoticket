/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui.grid.cells;

import com.avtoticket.client.ui.LongTextBox;
import com.avtoticket.client.ui.grid.View;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;

/**
 * Целочисленная ячейка таблицы.
 * 
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 27.09.2012 23:03:14
 */
public class LongEditCell extends HighlightedEditCell {

	private View<?> view;

	public LongEditCell() {
		super();
	}

	public LongEditCell(boolean enableHighlight) {
		super(enableHighlight);
	}

	public LongEditCell(View<?> view) {
		this();
		setView(view);
	}

	public void setView(View<?> view) {
		this.view = view;
	}

	@Override
	public void onBrowserEvent(final Context context, final Element parent,
			final String value, NativeEvent event,
			final ValueUpdater<String> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		if (isEditing(context, parent, value)) {
			String type = event.getType();
			boolean enterPressed = "keyup".equals(type)
					&& (event.getKeyCode() == KeyCodes.KEY_ENTER);
			if ("click".equals(type) || enterPressed) {
				final Element input = parent.getFirstChildElement();
				final EventListener oldListener = DOM.getEventListener(input);
				if (oldListener == null) {
					DOM.setEventListener(input, new EventListener() {
						@Override
						public void onBrowserEvent(Event event) {
							// адовый костыль, при навешивании нативного event'а на input почему-то перестаёт приходить событие onBlur
							// вследствии чего ячейка не выходит из режима редактирования, помогает такой вот ручной вызов
							if ((event.getTypeInt() == Event.ONBLUR) && (view != null))
								view.onBrowserEvent(event);
							if ((event.getTypeInt() == Event.ONKEYDOWN)
									&& !LongTextBox.filterNumbers(
											event.getKeyCode(),
											event.getCtrlKey(),
											event.getShiftKey(),
											event.getAltKey(),
											event.getMetaKey()))
								event.preventDefault();
						}
					});
					DOM.sinkEvents(input, Event.ONBLUR | Event.ONKEYDOWN);
				}
			}
		}
	}

}