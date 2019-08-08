/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.shared.models;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 21.05.2014 17:07:44
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StoredProc {

	/**
	 * @return название хранимки в БД, с которой связан аннотированный метод
	 */
	String value();

}