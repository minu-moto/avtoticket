/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid.fields;

import com.avtoticket.client.ui.grid.GridUtil.FieldTypes;
import com.avtoticket.client.ui.grid.cells.FloatEditCell;
import com.avtoticket.shared.models.BaseModel;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.Column;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 04.10.2013 22:11:49
 */
public class NumberFormatFieldBuilder<C extends BaseModel> extends CustomFormatField<NumberFormatFieldBuilder<C>, String, C> {

	public NumberFormatFieldBuilder<C> format(String fmt) {
		final NumberFormat format = NumberFormat.getFormat(fmt);
		return formatter(new Formatter<C>() {
			@Override
			public String format(C context) {
				Object val = (context != null) ? context.get(getModelKey()) : null;
				return ((val != null) && (val instanceof Number)) ? format.format((Number) val) : "";
			}
		});
	}

	public NumberFormatFieldBuilder<C> format(final NumberFormat fmt) {
		return formatter(new Formatter<C>() {
			@Override
			public String format(C context) {
				Object val = (context != null) ? context.get(getModelKey()) : null;
				return ((val != null) && (val instanceof Number)) ? fmt.format((Number) val) : "";
			}
		});
	}

	@Override
	protected Column<C, String> createColumn() {
		if (isShowInGrid())
			if ((getType() == FieldTypes.CURRENCY) || (getType() == FieldTypes.FLOAT)) {
				final FloatEditCell<C> fc = isEditable() ? new FloatEditCell<C>() : null;
				setColumn(new Column<C, String>(isEditable() ? fc : new TextCell()) {
					@Override
					public String getValue(C object) {
						return (getValueProvider() != null) ? getValueProvider().getValue(object) : null;
					}

					@Override
					public String getCellStyleNames(Context context, C object) {
						String ret = super.getCellStyleNames(context, object);
						if (fc != null)
							try {
								NumberFormat.getFormat("0.#").parse(fc.getRenderedValue(context, getValue(object)));
							} catch (NumberFormatException e) {
								ret = ((ret != null) ? ret + " " : "") + "errorNumberFormat";
							} catch (Exception e) {
								
							}
						return ret;
					}
				});
			} else
				return super.createColumn();
		return getColumn();
	}

}