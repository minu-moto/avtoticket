/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.about;

import com.avtoticket.client.about.AboutPlace.Strings;
import com.avtoticket.client.about.AboutPlace.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 дек. 2015 г. 21:37:27
 */
public class AboutSmall extends FlowPanel {

	public AboutSmall(Style CSS, Strings STRINGS) {
		Label content = new Label(STRINGS.caption());
		content.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.Location.assign("#" + AboutPlace.NAME);
			}
		}, ClickEvent.getType());
		content.addStyleName(CSS.atAboutSmall());
		add(content);

		Label label = new Label(STRINGS.caption());
		label.addStyleName(CSS.atAboutLabel());
		add(label);
	}

}