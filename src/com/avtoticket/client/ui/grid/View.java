/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid;

import java.util.List;

import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.client.ui.grid.filters.Filter;
import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.HasData;

/**
 * Интерфейс визуального компонента, который отображает содержимое {@link DataProvider источника данных}
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 27.05.2013 11:22:03
 */
public interface View<T extends BaseModel> extends IsWidget, HasVisibility, HasData<T>, EventListener, Filter {

	public void redraw();

	public void refresh();

	public String getSortColumn();

	public ColumnSortList getColumnSortList();

	public void setVisible(int col, boolean visible);

	public void addField(Field<?, T> ci);

	public Field<?, T> getField(int col);

	public Field<?, T> getField(String name);

	public List<Field<?, T>> getFields();

	public void onUpdate(Field<?, T> ci, T object, final Object value);

	@Override
	public List<T> getVisibleItems();

	public int getPageSize();

	public void addFilter(Filter filter);

}