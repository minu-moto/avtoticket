/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.mobile;

import com.avtoticket.client.tiles.TilePlace;
import com.avtoticket.client.utils.TileSize;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 дек. 2015 г. 19:08:25
 */
public class MobilePlace extends TilePlace {

	public static final String NAME = "mobile";
	public static final MobilePlace INSTANCE = new MobilePlace();

	public interface Style extends CssResource {
		String CSS_PATH = "mobile.css";

		String atMobileBig();

		String atMobileLabel();

		String atMobileMedium();

		String atMobileSmall();

		String atMobileGoogleBtn();

		String atMobileAppleBtn();

		String atMobileCaption();

		String atMobileApple();

		String atMobileAndroid();
	}

	public interface Resources extends ClientBundle {

		ImageResource mobileBig();

		ImageResource mobileMedium();

		ImageResource mobileSmall();

		@Source(Style.CSS_PATH)
	    Style mobileStyle();
	}

	public interface Strings extends Constants {
		@DefaultStringValue("Мобильные приложения")
		String caption();

		@DefaultStringValue("<span style='color: #218bf0'>AVTOTICKET.COM</span> всегда с Вами. <span style='color: #c94e9b'>Нет, навсегда с Вами!</span><br>Просто. Удобно. Всегда под рукой.")
		String tileCaption();

		@DefaultStringValue("Загрузить бесплатное приложение AVTOTICKET на iPhone или iPad в AppStore")
		String apple();

		@DefaultStringValue("Загрузить бесплатное приложение AVTOTICKET на Android - планшет или смартфон")
		String android();

		@DefaultStringValue("установить")
		String install();
	}

	private final Resources RESOURCES = GWT.create(Resources.class);
	private final Style CSS = RESOURCES.mobileStyle();
	private final Strings STRINGS = GWT.create(Strings.class);

	private final Activity bigActivity;
	private final Activity mediumActivity;
	private final Activity smallActivity;

	private MobilePlace() {
		CSS.ensureInjected();
		bigActivity = new AbstractActivity() {
			private MobileBig mobileBig = new MobileBig(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(mobileBig);
			}
		};
		mediumActivity = new AbstractActivity() {
			private MobileMedium mobileMedium = new MobileMedium(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(mobileMedium);
			}
		};
		smallActivity = new AbstractActivity() {
			private MobileSmall mobileSmall = new MobileSmall(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(mobileSmall);
			}
		};
	}

	@Override
	public Activity getActivity(Object param) {
		if (param == null)
			return null;
		switch ((TileSize) param) {
		case BIG:
			return bigActivity;
		case MEDIUM:
			return mediumActivity;
		case SMALL:
			return smallActivity;
		default:
			return null;
		}
	}

	@Override
	public String toString() {
		return NAME;
	}

}