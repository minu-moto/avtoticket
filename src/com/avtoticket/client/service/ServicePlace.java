/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.service;

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
public class ServicePlace extends TilePlace {

	public static final String NAME = "service";
	public static final ServicePlace INSTANCE = new ServicePlace();

	public interface Style extends CssResource {
		String CSS_PATH = "service.css";

		String atServiceBig();

		String atServiceLabel();

		String atServiceMedium();

		String atServiceSmall();

		String atServiceQuestions();

		String atServiceContent();
	}

	public interface Resources extends ClientBundle {

		ImageResource serviceBig();

		ImageResource serviceMedium();

		ImageResource serviceSmall();

		@Source(Style.CSS_PATH)
	    Style serviceStyle();
	}

	public interface Strings extends Constants {
		@DefaultStringValue("Сервис")
		String caption();

		@DefaultStringValue("У Вас есть вопросы?")
		String questions();

		@DefaultStringValue("<span style='color: #2373ec; font-size: 18px'>Сервис</span><br><br><br>"
				+ "У Вас возникли проблемы по работе<br>с <span style='color: #2373ec'>AVTOTICKET.COM</span>?<br><br><br>"
				+ "Мы рады Вам помочь!<br><br><br><span style='color: #2373ec'>КРАСНОДАР<br>ПРИВОКЗАЛЬНАЯ ПЛОЩАДЬ, 5<br>"
				+ "8 (989) 280-00-34<br>8 (989) 280-00-35<br>e-mail: oazis-ticket@mail.ru</span>")
		String content();
	}

	private final Resources RESOURCES = GWT.create(Resources.class);
	private final Style CSS = RESOURCES.serviceStyle();
	private final Strings STRINGS = GWT.create(Strings.class);

	private final Activity bigActivity;
	private final Activity mediumActivity;
	private final Activity smallActivity;

	private ServicePlace() {
		CSS.ensureInjected();
		bigActivity = new AbstractActivity() {
			private ServiceBig serviceBig = new ServiceBig(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(serviceBig);
			}
		};
		mediumActivity = new AbstractActivity() {
			private ServiceMedium serviceMedium = new ServiceMedium(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(serviceMedium);
			}
		};
		smallActivity = new AbstractActivity() {
			private ServiceSmall serviceSmall = new ServiceSmall(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(serviceSmall);
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