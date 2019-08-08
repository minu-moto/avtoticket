/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.about;

import com.avtoticket.client.about.AboutPlace.Strings;
import com.avtoticket.client.about.AboutPlace.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 дек. 2015 г. 21:37:27
 */
public class AboutBig extends FlowPanel {

	public AboutBig(Style CSS, Strings STRINGS) {
		HTML content = new HTML(STRINGS.content());
		content.addStyleName(CSS.atAboutBig());
		add(content);

		Label label = new Label(STRINGS.caption());
		label.addStyleName(CSS.atAboutLabel());
		add(label);
	}

}