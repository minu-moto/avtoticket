/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui.grid;

import java.util.Map;

import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.user.client.ui.HasValue;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 16.09.2012 22:19:51
 */
public interface FieldValidator <T extends BaseModel> {

	public Boolean isValidValue(Object value, Map<Field<?, T>, HasValue<?>> fields, StringBuilder errorMessage);

}