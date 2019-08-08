/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.partners;

import com.avtoticket.client.partners.PartnersPlace.Strings;
import com.avtoticket.client.partners.PartnersPlace.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 дек. 2015 г. 21:37:27
 */
public class PartnersBig extends FlowPanel {

	private FlowPanel content = new FlowPanel();

	public PartnersBig(Style CSS, Strings STRINGS) {
		content.addStyleName(CSS.atPartnersBig());
		add(content);

		Label label = new Label(STRINGS.caption());
		label.addStyleName(CSS.atPartnersLabel());
		add(label);
	}

}