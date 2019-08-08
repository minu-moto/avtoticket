/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid.fields;

import java.util.Date;

import com.avtoticket.client.ui.grid.cells.DateEditCell;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.utils.DateUtil;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.i18n.shared.DateTimeFormat;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 04.10.2013 22:11:49
 */
public class DateFieldBuilder<C extends BaseModel> extends CustomFormatField<DateFieldBuilder<C>, Date, C> {

	private DateTimeFormat format = null;

	public DateFieldBuilder<C> format(String fmt) {
		this.format = DateTimeFormat.getFormat(fmt);
		return formatter(new Formatter<C>() {
			@Override
			public String format(C context) {
				Date val = (context != null) ? context.getDateProp(getModelKey()) : null;
				return (val != null) ? format.format(val, DateUtil.getMSKTimeZone()) : "";
			}
		});
	}

	public DateFieldBuilder<C> format(DateTimeFormat fmt) {
		format = fmt;
		return formatter(new Formatter<C>() {
			@Override
			public String format(C context) {
				Object val = (context != null) ? context.get(getModelKey()) : null;
				return ((val != null) && (val instanceof Date)) ? format.format((Date) val) : "";
			}
		});
	}

	@Override
	protected Cell<Date> createCell() {
		return isEditable() ? new DateEditCell(format) : new DateCell(format, DateUtil.getMSKTimeZone());
	}

}