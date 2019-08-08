/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui.grid;

import java.util.List;

import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 02.10.2014 15:10:48
 */
public class TotalSumFooter<T extends BaseModel> extends TextFooter {

	private View<T> grid;
	private String field;
	private NumberFormat numberFormat;

	public TotalSumFooter(View<T> grid, String field, String format) {
		this.grid = grid;
		this.field = field;
		this.numberFormat = NumberFormat.getFormat(format);
	}

	@Override
	public String getValue() {
		List<T> items = grid.getVisibleItems();
		if ((items == null) || items.isEmpty())
			return "\u00A0";
		else {
			double sum = 0.0;
			for (T item : items) {
				Object val = item.get(field);
				if ((val != null) && (val instanceof Number))
					sum += ((Number) val).doubleValue();
			}
			return numberFormat.format(sum);
		}
	}

}