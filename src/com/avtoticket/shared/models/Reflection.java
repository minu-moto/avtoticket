/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.shared.models;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 27.12.2012 17:29:33
 */
public interface Reflection {

	public <T extends BaseModel> T instantiate(String className);

}