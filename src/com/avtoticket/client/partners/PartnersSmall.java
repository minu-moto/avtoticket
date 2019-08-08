/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.partners;

import com.avtoticket.client.partners.PartnersPlace.Strings;
import com.avtoticket.client.partners.PartnersPlace.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 дек. 2015 г. 21:37:27
 */
public class PartnersSmall extends FlowPanel {

	private FlowPanel content = new FlowPanel();

	public PartnersSmall(Style CSS, Strings STRINGS) {
		content.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.Location.assign("#" + PartnersPlace.NAME);
			}
		}, ClickEvent.getType());
		content.addStyleName(CSS.atPartnersSmall());
		add(content);

		Label label = new Label(STRINGS.caption());
		label.addStyleName(CSS.atPartnersLabel());
		add(label);
	}

}