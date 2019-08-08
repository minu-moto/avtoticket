/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.about;

import com.avtoticket.client.tiles.TilePlace;
import com.avtoticket.client.utils.TileSize;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 дек. 2015 г. 19:08:25
 */
public class AboutPlace extends TilePlace {

	public static final String NAME = "about";
	public static final AboutPlace INSTANCE = new AboutPlace();

	public interface Style extends CssResource {
		String CSS_PATH = "about.css";

		String atAboutBig();

		String atAboutLabel();

		String atAboutMedium();

		String atAboutSmall();
	}

	public interface Resources extends ClientBundle {
		@Source(Style.CSS_PATH)
	    Style aboutStyle();
	}

	public interface Strings extends Constants {
		@DefaultStringValue("О компании")
		String caption();

		@DefaultStringValue("<p style='font-size: 18px; color: #232323;'>Дорогие друзья!</p>"
				+ "<p style='font-size: 14px; color: #232323; margin-top: 40px; text-align: justify;'>"
				+ "Компания “ОАЗИС” является инвестором и оператором проекта AVTOTICKET.COM "
				+ "На протяжении двух лет мы работали над тем, чтобы предоставить современные "
				+ "условия и инструменты покупки билетов для клиентов ОАО АФ “Кубаньпассажир"
				+ "автосервис”. Детально проработав предпочтения пользователей, мы создали "
				+ "уникальный продукт, способный изменить представления клиентов о процедурах "
				+ "и механизмах покупки билетов.</p>"
				+ "<p style='font-size: 14px; color: #232323; margin-top: 36px;'>Спасибо за доверие к нашему проекту!</p>")
		String content();
	}

	private final Resources RESOURCES = GWT.create(Resources.class);
	private final Style CSS = RESOURCES.aboutStyle();
	private final Strings STRINGS = GWT.create(Strings.class);

	private final Activity bigActivity;
	private final Activity mediumActivity;
	private final Activity smallActivity;

	private AboutPlace() {
		CSS.ensureInjected();
		bigActivity = new AbstractActivity() {
			private AboutBig aboutBig = new AboutBig(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(aboutBig);
			}
		};
		mediumActivity = new AbstractActivity() {
			private AboutMedium aboutMedium = new AboutMedium(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(aboutMedium);
			}
		};
		smallActivity = new AbstractActivity() {
			private AboutSmall aboutSmall = new AboutSmall(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(aboutSmall);
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