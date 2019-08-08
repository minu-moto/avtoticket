/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.carrier;

import com.avtoticket.client.carrier.CarrierPlace.Style;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 12 янв. 2016 г. 22:17:12
 */
public class CarrierPanel extends TabLayoutPanel {

	private Style CSS;
	private PassportEditor passportEditor;
	private RequisiteEditor requisiteEditor;
	private FlowPanel routeEditor;

	public CarrierPanel(Style CSS) {
		super(2.5, Unit.EM);
		this.CSS = CSS;
		setAnimationDuration(200);
		addStyleName(CSS.atCarrierPanel());

		passportEditor = new PassportEditor(CSS);
		passportEditor.addClickHandler(event -> {
			if (requisiteEditor == null)
				buildRequisiteEditor();
			CarrierPanel.this.selectTab(1);
		});
		add(passportEditor, "Паспорт");

//		add(new FlowPanel(), "Учётная запись");
//		add(new FlowPanel(), "Реквизиты");
//		add(new FlowPanel(), "Рейсы");
//		add(new FlowPanel(), "Кассиры");
	}

	private void buildRequisiteEditor() {
		if (requisiteEditor == null) {
			requisiteEditor = new RequisiteEditor(CSS);
			requisiteEditor.addClickHandler(event -> {
				if (routeEditor == null) {
					routeEditor = new FlowPanel();
					add(routeEditor, "Рейсы");
				}
				CarrierPanel.this.selectTab(2);
			});
			add(requisiteEditor, "Реквизиты");
		}
	}

}