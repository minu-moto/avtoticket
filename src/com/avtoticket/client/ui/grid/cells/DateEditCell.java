/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui.grid.cells;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

import java.util.Date;

import com.avtoticket.client.ui.DatePicker;
import com.avtoticket.client.ui.grid.Grid;
import com.avtoticket.shared.utils.DateUtil;
import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.DatePickerCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

/**
 * Копия GWT'шного {@link DatePickerCell} с некоторыми модификациями
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 22.01.2014 14:04:02
 */
public class DateEditCell extends AbstractEditableCell<Date, Date> {

	private static final int ESCAPE = 27;

	private final DatePicker datePicker;
	private final DateTimeFormat format;
	private int offsetX = 10;
	private int offsetY = 10;
	private Object lastKey;
	private Element lastParent;
	private int lastIndex;
	private int lastColumn;
	private Date lastValue;
	private PopupPanel panel;
	private final SafeHtmlRenderer<String> renderer;
	private ValueUpdater<Date> valueUpdater;

	/**
	 * Constructs a new DatePickerCell that uses the given date/time format and
	 * {@link SafeHtmlRenderer}.
	 * 
	 * @param format
	 *            a {@link DateTimeFormat} instance
	 */
	public DateEditCell(DateTimeFormat format) {
		super(CLICK, KEYDOWN);
		if (format == null)
			throw new IllegalArgumentException("format == null");
		this.format = format;
		this.renderer = SimpleSafeHtmlRenderer.getInstance();

		this.datePicker = new DatePicker();
		this.panel = new PopupPanel(true, true) {
			@Override
			protected void onPreviewNativeEvent(NativePreviewEvent event) {
				if (Event.ONKEYUP == event.getTypeInt())
					if (event.getNativeEvent().getKeyCode() == ESCAPE) {
						// Dismiss when escape is pressed
						panel.hide();
					}
			}
		};
		panel.getElement().getStyle().setZIndex(6);
		panel.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				lastKey = null;
				lastValue = null;
				lastIndex = -1;
				lastColumn = -1;
				if (lastParent != null) {
					if (!event.isAutoClosed()) {
						// Refocus on the containing cell after the user selects a
						// value, but
						// not if the popup is auto closed.
						lastParent.focus();
					}
					lastParent.getParentElement().removeClassName(Grid.CSS.atEditCell());
				}
				lastParent = null;
			}
		});
		panel.add(datePicker);

		// Hide the panel and call valueUpdater.update when a date is selected
		datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				// Remember the values before hiding the popup.
				Element cellParent = lastParent;
				Date oldValue = lastValue;
				Object key = lastKey;
				int index = lastIndex;
				int column = lastColumn;
				panel.hide();

				// Update the cell and value updater.
				Date date = event.getValue();
				setViewData(key, date);
				setValue(new Context(index, column, key), cellParent, oldValue);
				if (valueUpdater != null)
					valueUpdater.update(date);
			}
		});
	}

	@Override
	public boolean isEditing(Context context, Element parent, Date value) {
		return (lastKey != null) && lastKey.equals(context.getKey());
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, Date value,
			NativeEvent event, ValueUpdater<Date> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		if (CLICK.equals(event.getType()))
			onEnterKeyDown(context, parent, value, event, valueUpdater);
	}

	@Override
	public void render(Context context, Date value, SafeHtmlBuilder sb) {
		// Get the view data.
		Object key = context.getKey();
		Date viewData = getViewData(key);
		if (viewData != null && viewData.equals(value)) {
			clearViewData(key);
			viewData = null;
		}

		String s = null;
		if (viewData != null)
			s = format.format(viewData, DateUtil.getMSKTimeZone());
		else if (value != null)
			s = format.format(value, DateUtil.getMSKTimeZone());
		if (s != null)
			sb.append(renderer.render(s));
	}

	@Override
	protected void onEnterKeyDown(Context context, Element parent, Date value,
			NativeEvent event, ValueUpdater<Date> valueUpdater) {
		lastKey = context.getKey();
		lastParent = parent;
		lastValue = value;
		this.lastIndex = context.getIndex();
		this.lastColumn = context.getColumn();
		this.valueUpdater = valueUpdater;

		Date viewData = getViewData(lastKey);
		Date date = (viewData == null) ? lastValue : viewData;
		if (date == null)
			date = DateUtil.localToMsk(new Date());
		datePicker.setCurrentMonth(date);
		datePicker.setValue(date);
		lastParent.getParentElement().addClassName(Grid.CSS.atEditCell());
		panel.setPopupPositionAndShow(new PositionCallback() {
			@Override
			public void setPosition(int offsetWidth, int offsetHeight) {
				panel.setPopupPosition(lastParent.getAbsoluteLeft() + offsetX,
						lastParent.getAbsoluteTop() + offsetY);
			}
		});
	}

}