/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.service;

import com.avtoticket.client.service.ServicePlace.Strings;
import com.avtoticket.client.service.ServicePlace.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 22 дек. 2015 г. 22:08:02
 */
public class ServiceSmall extends FlowPanel {

	private FlowPanel content = new FlowPanel();

	public ServiceSmall(Style CSS, Strings STRINGS) {
		content.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.Location.assign("#" + ServicePlace.NAME);
			}
		}, ClickEvent.getType());
		content.addStyleName(CSS.atServiceSmall());
		add(content);

		Label label = new Label(STRINGS.caption());
		label.addStyleName(CSS.atServiceLabel());
		add(label);
	}

}