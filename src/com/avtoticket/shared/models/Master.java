/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.shared.models;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ссылка на главную таблицу
 * 
 * @author Minu <<a href='minu-moto@mail.ru'>minu-moto@mail.ru</a>>
 * @since 12.03.2012 13:09:46
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Master {

	/**
	 * @return имя класса, на который ссылается поле
	 */
	Class<?> clazz();

	/**
	 * @return название поля содержащего ссылку на другую таблицу
	 */
	String key();

	/**
	 * @return цеплять вложенные модели или нет
	 */
	boolean attDetails() default true;

}