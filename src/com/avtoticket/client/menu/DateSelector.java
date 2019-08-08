/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.menu;

import java.util.Date;

import com.avtoticket.client.menu.MainMenu.Strings;
import com.avtoticket.client.menu.MainMenu.Style;
import com.avtoticket.shared.utils.DateUtil;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.datepicker.client.CalendarUtil;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 19 дек. 2015 г. 22:09:06
 */
public class DateSelector extends FlowPanel implements HasValue<Date> {

//	private static final Logger logger = Logger.getLogger(DateSelector.class.getName());

	private static final int LEFT = 58;
	private static final int RIGHT = 770;

	private Boolean dragging = false;
	private Date value;
	private int mouseX;
	private int selectorX = 60;
	private Label lblDate;
	private Label lblDateSelector = new Label();
	private FlowPanel dateSelectorLine = new FlowPanel();

	private Date[] dates;
	private int STEP_COUNT = 0;
	private int STEP = Integer.MAX_VALUE;
	private final Strings STRINGS;

	private void onDragStart(int clientX) {
		dragging = true;
		mouseX = clientX;
	}

	public DateSelector(Style CSS, Strings STRINGS) {
		this.STRINGS = STRINGS;
		lblDate = new Label(STRINGS.when());
		lblDate.addStyleName(CSS.atDateSelectorDateLbl());
		add(lblDate);

		dateSelectorLine.addStyleName(CSS.atDateSelectorLine());
		add(dateSelectorLine);

		lblDateSelector.addTouchStartHandler(new TouchStartHandler() {
			@Override
			public void onTouchStart(TouchStartEvent event) {
				if (event.getTouches().length() != 1)
					return;
				event.stopPropagation();
				event.preventDefault();
				onDragStart(event.getTouches().get(0).getClientX());
			}
		});
		lblDateSelector.addTouchMoveHandler(new TouchMoveHandler() {
			@Override
			public void onTouchMove(TouchMoveEvent event) {
				if (dragging) {
	            	int clientX = event.getChangedTouches().get(0).getClientX();
	            	int delta = 1920 * (clientX - mouseX) / Math.min(Window.getClientWidth(), 1920);	// учитываем масштаб
	            	int newX = Math.max(Math.min(selectorX + delta, RIGHT), LEFT);
            		int n = Math.min(Math.round((float) (newX - LEFT) / STEP), STEP_COUNT);
            		value = (dates != null) ? dates[n] : null;
            		lblDateSelector.setText((value != null) ? STRINGS.date(value, (TimeZone) DateUtil.getMSKTimeZone()) : "");
            		lblDateSelector.getElement().getStyle().setLeft(newX, Unit.PX);
    				event.stopPropagation();
            		event.preventDefault();
            	}
			}
		});
		lblDateSelector.addTouchEndHandler(new TouchEndHandler() {
			@Override
			public void onTouchEnd(TouchEndEvent event) {
				if (dragging) {
	            	int clientX = event.getChangedTouches().get(0).getClientX();
	            	int delta = 1920 * (clientX - mouseX) / Math.min(Window.getClientWidth(), 1920);	// учитываем масштаб
	            	int newX = Math.max(Math.min(selectorX + delta, RIGHT), LEFT);
            		int n = Math.min(Math.round((float) (newX - LEFT) / STEP), STEP_COUNT);
            		selectorX = LEFT + STEP * n;
            		lblDateSelector.getElement().getStyle().setLeft(selectorX, Unit.PX);
    				dragging = false;
    				event.stopPropagation();
            		event.preventDefault();
            	}
			}
		});

		lblDateSelector.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				onDragStart(event.getClientX());
			}
		});
		Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
			public void onPreviewNativeEvent(NativePreviewEvent event) {
            	if (dragging) {
	            	NativeEvent ne = event.getNativeEvent();
	            	if (BrowserEvents.MOUSEMOVE.equalsIgnoreCase(ne.getType()) || BrowserEvents.MOUSEUP.equalsIgnoreCase(ne.getType())) {
		            	int delta = 1920 * (ne.getClientX() - mouseX) / Math.min(Window.getClientWidth(), 1920);	// учитываем масштаб
		            	int newX = Math.max(Math.min(selectorX + delta, RIGHT), LEFT);
	            		int n = Math.min(Math.round((float) (newX - LEFT) / STEP), STEP_COUNT);
		            	if (BrowserEvents.MOUSEMOVE.equalsIgnoreCase(ne.getType())) {
		            		value = (dates != null) ? dates[n] : null;
		            		lblDateSelector.setText((value != null) ? STRINGS.date(value, (TimeZone) DateUtil.getMSKTimeZone()) : "");
		            		lblDateSelector.getElement().getStyle().setLeft(newX, Unit.PX);
		            		ne.preventDefault();
		            	}
		            	if (BrowserEvents.MOUSEUP.equalsIgnoreCase(ne.getType())) {
		            		selectorX = LEFT + STEP * n;
		            		lblDateSelector.getElement().getStyle().setLeft(selectorX, Unit.PX);
		    				dragging = false;
		            	}
	            	}
            	}
            }
		});
		lblDateSelector.addStyleName(CSS.atRouteSelectorDateSelector());
		add(lblDateSelector);
	}

	@Override
	public Date getValue() {
		return value;
	}

	@Override
	public void setValue(Date value) {
		setValue(value, false);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public void setValue(Date value, boolean fireEvents) {
		// TODO fireEvents
		if (dates != null)
			if (value != null) {
				for (int n = 0; n < dates.length; n++)
					if (CalendarUtil.isSameDate(value, dates[n])) {
						refreshSelectorPosition(n);
						return;
					}
			} else
				refreshSelectorPosition(0);
	}

	public void setDates(Date[] dates) {
		this.dates = dates;

		STEP_COUNT = dates.length - 1;
		if (STEP_COUNT > 0)
			STEP = (RIGHT - LEFT) / STEP_COUNT;
		else
			STEP = Integer.MAX_VALUE;

		int n = Math.min(Math.round((float) (selectorX - LEFT) / STEP), STEP_COUNT);
		refreshSelectorPosition(n);
	}

	private void refreshSelectorPosition(int n) {
		value = dates[n];
		selectorX = LEFT + STEP * n;
		lblDateSelector.getElement().getStyle().setLeft(selectorX, Unit.PX);
		lblDateSelector.setText(STRINGS.date(value, (TimeZone) DateUtil.getMSKTimeZone()));
	}

	public void reset() {
		refreshSelectorPosition(0);
	}

}