/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui.grid;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 30.09.2014 14:55:45
 */
public class DataChangeEvent extends GwtEvent<DataChangeEvent.Handler> {

	public static interface Handler extends EventHandler {

		public void onDataChange(DataChangeEvent event);

	}

	public static interface HasDataChangeHandlers extends HasHandlers {

		  /**
		   * Adds a {@link DataChangeEvent} handler.
		   * 
		   * @param handler the handler
		   * @return the registration for the event
		   */
		  public HandlerRegistration addDataChangeHandler(Handler handler);

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
	 * @param eventSource
	 *            источник события
	 */
	public static void fire(HasDataChangeHandlers handlerSource) {
		if (TYPE != null) {
			DataChangeEvent event = new DataChangeEvent();
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

	public DataChangeEvent() {
		super();
	}

	public DataChangeEvent(Object source) {
		setSource(source);
	}

	@Override
	public final Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onDataChange(this);
	}

}