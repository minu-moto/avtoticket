/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid;

import java.util.List;

import com.avtoticket.client.ui.grid.DataChangeEvent.HasDataChangeHandlers;
import com.avtoticket.client.utils.DefaultCallback;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Where;
import com.google.gwt.view.client.HasData;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 27.05.2013 11:28:40
 */
public interface DataProvider<T extends BaseModel, K extends Object> extends HasDataChangeHandlers, DataChangeEvent.Handler {

	public void addDataDisplay(final HasData<T> display);

	public void updateCount(int count, boolean exact);

	public void updateData(int start, List<T> values);

	public void removeItems(List<K> ids, DefaultCallback<Void> callback);

	public Where getFilter(Object target);

	public void clearFilter();

	public void clearFilter(boolean fireEvent);

}