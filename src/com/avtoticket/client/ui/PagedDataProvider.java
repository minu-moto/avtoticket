/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.avtoticket.client.ui.grid.DataChangeEvent;
import com.avtoticket.client.ui.grid.DataChangeEvent.Handler;
import com.avtoticket.client.ui.grid.DataProvider;
import com.avtoticket.client.ui.grid.View;
import com.avtoticket.client.ui.grid.filters.FilterChangeEvent;
import com.avtoticket.client.utils.DefaultCallback;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.PageContainer;
import com.avtoticket.shared.models.Where;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 13.06.2012 12:40:56
 */
public class PagedDataProvider<T extends BaseModel, K extends Object> extends AsyncDataProvider<T> implements DataProvider<T, K> {

	protected Class<T> modelsClass;
	protected View<T> view = null;
	private List<K> forDel = null;
	private DefaultCallback<Void> forDelCallback = null;

	public PagedDataProvider(Class<T> clazz, ProvidesKey<T> keyProvider, final View<T> view) {
		super(keyProvider);
		this.modelsClass = clazz;
		this.view = view;
		view.addFilterChangeHandler(new FilterChangeEvent.Handler() {
			@Override
			public void onFilterChange(FilterChangeEvent event) {
				view.refresh();
			}
		});
	}

	@Override
	protected void onRangeChanged(final HasData<T> display) {
		Range r = display.getVisibleRange();
		Where where = (view != null) ? view.getFilter() : null;
		if (where == null)
			where = new Where();
		where.offset(r.getStart()).limit(r.getLength());
		final DefaultCallback<Void> forDelCallback = this.forDelCallback;
		getObjects(modelsClass.getName(), forDel, where, (view != null) ? view.getSortColumn() : null, new AsyncCallback<PageContainer<T>>() {
			@Override
			public void onFailure(Throwable caught) {
				setData(display, null);
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(PageContainer<T> result) {
				setData(display, result);
				if (forDelCallback != null)
					forDelCallback.onSuccess(null);
			}
		});
		this.forDelCallback = null;
		this.forDel = null;
	}

	@SuppressWarnings("unchecked")
	protected void getObjects(String className, List<K> forDel,
			Where where, String sortColumn, AsyncCallback<PageContainer<T>> callback) {
		RPC.getTS().getPagedModels(className, (List<Long>) forDel, where, sortColumn, callback);
	}

	public void setData(HasData<T> display, PageContainer<T> data) {
		Range r = display.getVisibleRange();
		if ((data != null) && (data.getPage() != null)) {
			updateRowCount(data.getItemsCount(), true);
			updateRowData(display, r.getStart(), data.getPage());
		} else {
			updateRowCount(0, data != null);
			updateRowData(display, r.getStart(), new ArrayList<T>());
		}
	}

	@Override
	public void removeItems(List<K> ids, DefaultCallback<Void> callback) {
		forDel = ids;
		forDelCallback = callback;
		if (view != null)
			view.refresh();
	}

	@Override
	public void updateCount(int count, boolean exact) {
		super.updateRowCount(count, exact);
	}

	@Override
	public void updateData(int start, List<T> values) {
		super.updateRowData(start, values);
	}

	@Override
	public HandlerRegistration addDataChangeHandler(Handler handler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		// TODO Auto-generated method stub
	}

	@Override
	public Where getFilter(Object target) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearFilter() {
		// TODO Auto-generated method stub
	}

	@Override
	public void clearFilter(boolean fireEvent) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDataChange(DataChangeEvent event) {
		// TODO Auto-generated method stub
	}

}