/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui.grid.fields;

import com.avtoticket.client.ui.ModelListDataProvider;
import com.avtoticket.client.ui.grid.ListFilter;
import com.avtoticket.shared.models.BaseModel;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 16.09.2012 16:48:15
 */
public class ListFieldGenericBuilder<B extends ListFieldGenericBuilder<B, K, T, C>, K, T, C extends BaseModel> extends FieldGenericBuilder<B, K, C> {

	private ModelListDataProvider<T> listProvider;
	private ListFilter<T, C> filter = null;

	public ModelListDataProvider<T> getListProvider() {
		return listProvider;
	}
	@SuppressWarnings("unchecked")
	public B provider(ModelListDataProvider<T> listProvider) {
		this.listProvider = listProvider;
		return (B) this;
	}

	public ListFilter<T, C> getListFilter() {
		return filter;
	}
	public ListFieldGenericBuilder<B, K, T, C> listFilter(ListFilter<T, C> filter) {
		this.filter = filter;
		return this;
	}

}