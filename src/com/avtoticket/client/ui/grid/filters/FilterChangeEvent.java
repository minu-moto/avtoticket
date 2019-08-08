/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui.grid.filters;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 30.09.2014 14:55:45
 */
public class FilterChangeEvent extends GwtEvent<FilterChangeEvent.Handler> {

	public static interface Handler extends EventHandler {

		public void onFilterChange(FilterChangeEvent event);

	}

	public static interface HasFilterChangeHandlers extends HasHandlers {

		  /**
		   * Adds a {@link FilterChangeEvent} handler.
		   * 
		   * @param handler the handler
		   * @return the registration for the event
		   */
		  public HandlerRegistration addFilterChangeHandler(Handler handler);

	}

	/**
	 * Handler type.
	 */
	private static Type<Handler> TYPE;

	/**
	 * Fires a value change event on all registered handlers in the handler
	 * manager. If no such handlers exist, this method will do nothing.
	 * 
	 * @param <T>
	 *            the old value type
	 * @param handlerSource
	 *            the source of the handlers
	 */
	public static void fire(HasFilterChangeHandlers handlerSource) {
		fire(handlerSource, null);
	}

	/**
	 * Fires a value change event on all registered handlers in the handler
	 * manager. If no such handlers exist, this method will do nothing.
	 * 
	 * @param <T>
	 *            the old value type
	 * @param handlerSource
	 *            the source of the handlers
	 * @param eventSource
	 *            источник события
	 */
	public static void fire(HasFilterChangeHandlers handlerSource, Object eventSource) {
		if (TYPE != null) {
			FilterChangeEvent event = new FilterChangeEvent(eventSource);
			handlerSource.fireEvent(event);
		}
	}

	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<Handler> getType() {
		if (TYPE == null)
			TYPE = new Type<Handler>();
		return TYPE;
	}

	private Object eventSource;

	protected FilterChangeEvent() {
		this(null);
	}

	protected FilterChangeEvent(Object eventSource) {
		this.eventSource = eventSource;
	}

	public Object getEventSource() {
		return eventSource;
	}

	@Override
	public final Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onFilterChange(this);
	}

}