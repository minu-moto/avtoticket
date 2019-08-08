/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid.fields;

import com.avtoticket.shared.models.BaseModel;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 14.01.2013 14:47:01
 */
public class CustomFormatField<B extends CustomFormatField<B, T, C>, T, C extends BaseModel>
		extends FieldGenericBuilder<B, T, C> {

	/**
	 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
	 * @since 14.01.2013 14:45:51
	 */
	public interface Formatter<C extends BaseModel> {

		public String format(C context);

	}

	private Formatter<C> formatter = null;

	public Formatter<C> getFormatter() {
		return formatter;
	}
	@SuppressWarnings("unchecked")
	public B formatter(Formatter<C> formatter) {
		this.formatter = formatter;
		return (B) this;
	}

}