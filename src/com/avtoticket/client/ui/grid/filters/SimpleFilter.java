/*
 * Copyright Бездна (c) 2015.
 */
package com.avtoticket.client.ui.grid.filters;

import com.avtoticket.client.ui.grid.filters.FilterChangeEvent.Handler;
import com.avtoticket.shared.models.Where;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 18.02.2015 16:40:58
 */
public class SimpleFilter implements Filter {

	private Handler handler;

	@Override
	public HandlerRegistration addFilterChangeHandler(Handler handler) {
		this.handler = handler;
		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				SimpleFilter.this.handler = null;
			}
		};
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		if ((handler != null) && (event != null) && (event instanceof FilterChangeEvent))
			handler.onFilterChange((FilterChangeEvent) event);
	}

	@Override
	public Where getFilter() {
		return null;
	}

	@Override
	public void clearFilter() {
		clearFilter(false);
	}

	@Override
	public void clearFilter(boolean fireEvent) {
		
	}

}