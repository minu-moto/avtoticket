/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 16.07.2014 12:18:21
 */
public interface ProvidesValue<K, T> {

	K getValue(T item);

}