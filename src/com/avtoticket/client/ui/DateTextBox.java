/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.ui;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.avtoticket.shared.utils.DateUtil;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarModel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.user.datepicker.client.DefaultCalendarView;
import com.google.gwt.user.datepicker.client.DefaultMonthSelector;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 16 янв. 2016 г. 22:04:52
 */
public class DateTextBox extends DateBox {

	public DateTextBox() {
		this("dd.MM.yyyy");
	}

	public DateTextBox(String format) {
		super(new DatePicker(new DefaultMonthSelector(), new DefaultCalendarView(), new CalendarModel() {
			@Override
			protected DateTimeFormat getDayOfWeekFormatter() {
				return DateTimeFormat.getFormat("ccc");
			}

			@Override
			protected boolean isMonthBeforeYear() {
				return false;
			}
		}) {
			@SuppressWarnings("deprecation")
			private final int TO_YEAR = new Date().getYear();
			private final int FROM_YEAR = TO_YEAR - 100;
			private boolean shift;

			@Override
			public int getVisibleYearCount() {						// костыль, позволяющий задавать диапазон отображаемых годов
				@SuppressWarnings("deprecation")
				int currentYear = getCurrentMonth().getYear();
				int ret = shift ? 2 * Math.max(TO_YEAR - currentYear, 0) : (2 * Math.max(currentYear - FROM_YEAR, 0) + 1);
				shift = !shift;
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						if (shift) {
							shift = false;
							setVisibleYearCount(100);
						}
						shift = false;
					}
				});
				return ret;
			}
		}, null, new DefaultFormat(DateTimeFormat.getFormat(format)));
	}

	// КОСТЫЛЬ корректируем разницу в часовых поясах на сервере и на клиенте

	@Override
	public Date getValue() {
		Date val = super.getValue();
		if (val != null)
			return DateUtil.localToMsk(val);
		else
			return null;
	}

	@Override
	public void setValue(Date date, boolean fireEvents) {
		if (date != null) {
			@SuppressWarnings("deprecation")
			long diff = TimeUnit.MINUTES.toMillis(date.getTimezoneOffset() - DateUtil.getMSKTimeZone().getOffset(date));
		    super.setValue(new Date(date.getTime() + diff), fireEvents);
		} else
			super.setValue(null, fireEvents);
	}

}