/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid.fields;

import com.avtoticket.shared.models.BaseModel;

/**
 * @param <T> - тип поля
 * @param <C> - тип контекста (модели данных из которой берётся поле)
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 05.10.2013 21:51:21
 */
public class FieldBuilder<T, C extends BaseModel> extends FieldGenericBuilder<FieldBuilder<T, C>, T, C> { }