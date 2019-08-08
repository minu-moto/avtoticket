/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.shared.models;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ссылка на подчинённую таблицу
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 21.09.2012 16:37:49
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Detail {

	/**
	 * @return имя класса, который ссылается на эту таблицу
	 */
	Class<?> clazz();

	/**
	 * @return название поля содержащего ссылку на эту таблицу
	 */
	String key();

	/**
	 * @return цеплять вложенные модели или нет
	 */
	boolean attDetails() default true;

}