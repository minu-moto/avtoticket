/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.shared.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.TimeZone;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 13.02.2012 13:26:25
 */
public class DateUtil {

	private static TimeZone TZ_MSK;

	@SuppressWarnings("deprecation")
	public static int getAge(Date birthday, Date now, int offsetDays) {
		now = new Date(now.getTime() - TimeUnit.DAYS.toMillis(offsetDays));
		int year = birthday.getYear();
		int month = birthday.getMonth();
		int day = birthday.getDate();

	    int nowMonth = now.getMonth();
	    int nowYear = now.getYear();
	    int result = nowYear - year;

	    if (month > nowMonth)
	        result--;
	    else if (month == nowMonth) {
	        int nowDay = now.getDate();
	        if (day > nowDay)
	            result--;
	    }
	    return result;
	}

	public static int getAge(Date birthday, Date now) {
	    return getAge(birthday, now, 0);
	}

	public static String formatDate(Date date, String format) {
		return DateTimeFormat.getFormat(format).format(date, TZ_MSK);
	}

	public static Date localToMsk(Date date) {
		@SuppressWarnings("deprecation")
		long diff = TimeUnit.MINUTES.toMillis(date.getTimezoneOffset() - TZ_MSK.getOffset(date));
	    return new Date(date.getTime() - diff);
	}

	public static void setMSKTimeZone(TimeZone tz) {
		TZ_MSK = tz;
	}
	public static TimeZone getMSKTimeZone() {
		return TZ_MSK;
	}

}