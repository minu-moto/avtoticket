/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.mobile;

import com.avtoticket.client.mobile.MobilePlace.Strings;
import com.avtoticket.client.mobile.MobilePlace.Style;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 дек. 2015 г. 21:37:27
 */
public class MobileBig extends FlowPanel {

	private FlowPanel content = new FlowPanel();

	public MobileBig(Style CSS, Strings STRINGS) {
		HTML caption = new HTML(STRINGS.tileCaption());
		caption.addStyleName(CSS.atMobileCaption());
		content.add(caption);

		Label apple = new Label(STRINGS.apple());
		apple.addStyleName(CSS.atMobileApple());
		content.add(apple);

		Label android = new Label(STRINGS.android());
		android.addStyleName(CSS.atMobileAndroid());
		content.add(android);

		Anchor btnApple = new Anchor(STRINGS.install(), "https://itunes.apple.com/ru/app/avtoticket/id1000556173?mt=8", "_blank");
		btnApple.addStyleName(CSS.atMobileAppleBtn());
		content.add(btnApple);

		Anchor btnGoogle = new Anchor(STRINGS.install(), "https://play.google.com/store/apps/details?id=com.avtoticket.app", "_blank");
		btnGoogle.addStyleName(CSS.atMobileGoogleBtn());
		content.add(btnGoogle);

		content.addStyleName(CSS.atMobileBig());
		add(content);

		Label label = new Label(STRINGS.caption());
		label.addStyleName(CSS.atMobileLabel());
		add(label);
	}

}