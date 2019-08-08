/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.service;

import com.avtoticket.client.service.ServicePlace.Strings;
import com.avtoticket.client.service.ServicePlace.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 22 дек. 2015 г. 22:06:12
 */
public class ServiceBig extends FlowPanel {

	private FlowPanel content = new FlowPanel();

	public ServiceBig(Style CSS, Strings STRINGS) {
		content.addStyleName(CSS.atServiceBig());
		add(content);

		HTML text = new HTML(STRINGS.content());
		text.addStyleName(CSS.atServiceContent());
		content.add(text);

		Label label = new Label(STRINGS.caption());
		label.addStyleName(CSS.atServiceLabel());
		add(label);
	}

}