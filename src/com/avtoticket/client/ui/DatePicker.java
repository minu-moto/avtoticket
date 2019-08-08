/*
 * Copyright Бездна (c) 2015.
 */
package com.avtoticket.client.ui;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarModel;
import com.google.gwt.user.datepicker.client.DefaultCalendarView;
import com.google.gwt.user.datepicker.client.DefaultMonthSelector;
import com.google.gwt.user.datepicker.client.MonthSelector;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 26.01.2015 17:43:54
 */
public class DatePicker extends com.google.gwt.user.datepicker.client.DatePicker {

	public DatePicker() {
		super(new DefaultMonthSelector(), new DefaultCalendarView(), new CalendarModel() {
			@Override
			protected DateTimeFormat getDayOfWeekFormatter() {
				return DateTimeFormat.getFormat("ccc");
			}
		});
	}

	public MonthSelector getSelector() {
		return getMonthSelector();
	}

	public void refresh() {
		refreshAll();
	}

}