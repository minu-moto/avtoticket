/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.carrier;

import com.avtoticket.client.utils.ATPlace;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 6 сент. 2016 г. 23:43:31
 */
public class CarrierPlace extends ATPlace {

	public static final String NAME = "carrier";
	public static final CarrierPlace INSTANCE = new CarrierPlace();

	public interface Style extends CssResource {
		String CSS_PATH = "carrier.css";

		String atCarrierPanel();

		String atCarrierPassport();

		String atCarrierPassportCaption();

		String atCarrierPassportNextBtn();

		String atCarrierRequisite();

		String atCarrierRequisiteCaption();

		String atCarrierRequisiteNextBtn();
	}

	public interface Resources extends ClientBundle {
		@Source(Style.CSS_PATH)
	    Style carrierStyle();
	}

	private final Resources RESOURCES = GWT.create(Resources.class);
	private final Style CSS = RESOURCES.carrierStyle();

//	private CarrierPanel carrierPanel;
	private final Activity carrierActivity = new AbstractActivity() {
		@Override
		public void start(AcceptsOneWidget panel, EventBus eventBus) {
//			if (carrierPanel == null)
//				carrierPanel = new CarrierPanel(CSS);
			panel.setWidget(new CarrierPanel(CSS));
		}
	};

	@Override
	public Activity getActivity(Object param) {
		return carrierActivity;
	}

	private CarrierPlace() {
		CSS.ensureInjected();
	}

	@Override
	public String toString() {
		return NAME;
	}

}