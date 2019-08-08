/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.mobile;

import com.avtoticket.client.mobile.MobilePlace.Strings;
import com.avtoticket.client.mobile.MobilePlace.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 дек. 2015 г. 21:37:27
 */
public class MobileMedium extends FlowPanel {

	private FlowPanel content = new FlowPanel();

	public MobileMedium(Style CSS, Strings STRINGS) {
		content.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.Location.assign("#" + MobilePlace.NAME);
			}
		}, ClickEvent.getType());
		content.addStyleName(CSS.atMobileMedium());
		add(content);

		Label label = new Label(STRINGS.caption());
		label.addStyleName(CSS.atMobileLabel());
		add(label);
	}

}