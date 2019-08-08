/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.map;

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
public class MapPlace extends TilePlace {

	public static final String NAME = "map";
	public static final MapPlace INSTANCE = new MapPlace();

	public interface Style extends CssResource {
		String CSS_PATH = "map.css";

		String atMapBig();

		String atMapLabel();

		String atMapMedium();

		String atMapSmall();

		String atMapLocalitySelector();

		String atMapLocalityLabel();
	}

	public interface Resources extends ClientBundle {

		ImageResource map();

		ImageResource mapMedium();

		@Source(Style.CSS_PATH)
	    Style mapStyle();
	}

	public interface Strings extends Constants {
		@DefaultStringValue("На карте")
		String caption();

		@DefaultStringValue("Укажите город для поиска автовокзалов")
		String searchLabel();

		@DefaultStringValue("Список пуст")
		String empty();

		@DefaultStringValue("Все города")
		String all();
	}

	private final Resources RESOURCES = GWT.create(Resources.class);
	private final Style CSS = RESOURCES.mapStyle();
	private final Strings STRINGS = GWT.create(Strings.class);

	private final Activity bigActivity;
	private final Activity mediumActivity;
	private final Activity smallActivity;

	private MapPlace() {
		CSS.ensureInjected();
		bigActivity = new AbstractActivity() {
			private MapBig mapBig = new MapBig(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(mapBig);
			}
		};
		mediumActivity = new AbstractActivity() {
			private MapMedium mapMedium = new MapMedium(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(mapMedium);
			}
		};
		smallActivity = new AbstractActivity() {
			private MapSmall mapSmall = new MapSmall(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(mapSmall);
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