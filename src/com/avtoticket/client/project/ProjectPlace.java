/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.project;

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
public class ProjectPlace extends TilePlace {

	public static final String NAME = "project";
	public static final ProjectPlace INSTANCE = new ProjectPlace();

	public interface Style extends CssResource {
		String CSS_PATH = "project.css";

		String atProjectBig();

		String atProjectLabel();

		String atProjectMedium();

		String atProjectSmall();
	}

	public interface Resources extends ClientBundle {

		ImageResource projectSmall();

		ImageResource projectMedium();

		@Source(Style.CSS_PATH)
	    Style projectStyle();
	}

	public interface Strings extends Constants {
		@DefaultStringValue("О проекте")
		String caption();

		@DefaultStringValue("<p style='font-size: 18px; color: #2373ec;'>О проекте AVTOTICKET.COM</p>"
				+ "<br>"
				+ "AVTOTICKET.COM - это первый и единственный в Краснодарском крае онлайн-сервис по приобретению "
				+ "билетов на автобусы междугороднего и международного сообщения. Мы рады предоставить пользователям "
				+ "удобный и простой инструмент, позволяющий быстро и просто купить билет, сэкономив свое время ожидания в очередях."
				+ "<br><br>"
				+ "WEB-сервис AVTOTICKET располагает широким набором инструментов покупки билетов. Учитывая значимую "
				+ "динамику использования смартфонов и планшетов в наше время, мы разработали мобильные приложения "
				+ "\"Avtoticket\" для пользователей операционных систем Android OS и IOS. Бесплатно установить "
				+ "приложения можно через Google Play и AppStore.")
		String content();
	}

	private final Resources RESOURCES = GWT.create(Resources.class);
	private final Style CSS = RESOURCES.projectStyle();
	private final Strings STRINGS = GWT.create(Strings.class);

	private final Activity bigActivity;
	private final Activity mediumActivity;
	private final Activity smallActivity;

	private ProjectPlace() {
		CSS.ensureInjected();
		bigActivity = new AbstractActivity() {
			private ProjectBig projectBig = new ProjectBig(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(projectBig);
			}
		};
		mediumActivity = new AbstractActivity() {
			private ProjectMedium projectMedium = new ProjectMedium(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(projectMedium);
			}
		};
		smallActivity = new AbstractActivity() {
			private ProjectSmall projectSmall = new ProjectSmall(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(projectSmall);
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