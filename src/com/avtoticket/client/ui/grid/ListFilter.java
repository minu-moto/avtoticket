/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid;

import com.avtoticket.shared.models.BaseModel;

/**
 * Фильтр записей для выпадающих списков в гриде.
 * Иногда требуется отображать различное содержимое списка
 * в зависимости от контекста. Именно в таких случаях на помощь
 * приходит фильтр! Загружаем в {@link ModelListDataProvider} все записи,
 * а отображаем только те, для которых метод {@link #accept()} даёт добро.
 * 
 * @param <T> - класс фильтруемых объектов
 * @param <C> - класс объектов контекста
 * 
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 15.02.2013 20:54:04
 */
public interface ListFilter<T, C extends BaseModel> {

	/**
	 * Определяет принять данный объект или отфильтровать
	 * 
	 * @param option - проверяемый объект
	 * @param context - контекст проверяемого объекта
	 * @return <code>true</code>, если объект проходит фильтрацию, иначе - <code>false</code>
	 */
	public boolean accept(T option, C context);

}