/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client;

import com.avtoticket.client.Avtoticket.Strings;
import com.avtoticket.client.Avtoticket.Style;
import com.avtoticket.client.about.AboutPlace;
import com.avtoticket.client.articles.ArticlePlace;
import com.avtoticket.client.news.NewsPlace;
import com.avtoticket.client.tiles.Tiles;
import com.avtoticket.client.utils.ATPlace;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 19 дек. 2015 г. 23:04:11
 */
public class BodyPanel extends FlowPanel {

	private static final EventBus eventBus = new SimpleEventBus();
	private static final PlaceController placeController = new PlaceController(eventBus);

	private final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(new AppPlaceHistoryMapper());
	private final ActivityManager appActivityManager = new ActivityManager(new ActivityMapper() {
		@Override
		public Activity getActivity(Place place) {
			if (place instanceof ATPlace) {
				ATPlace atPlace = (ATPlace) place;
				return atPlace.getActivity();
			}
			return null;
		}
	}, eventBus);

	private NewsPlace newsPanel = new NewsPlace();
	private SimplePanel appPanel = new SimplePanel();

	public BodyPanel(Style CSS, Strings STRINGS) {
		addStyleName(CSS.atBody());

		newsPanel.getElement().getStyle().setFloat(Float.RIGHT);
		add(newsPanel);
		add(appPanel);
		add(ArticlePlace.ARTICLE_PANEL);

		Anchor stavropol = new Anchor(STRINGS.stavropol(), "https://stavbilet26.ru");
		stavropol.addStyleName(CSS.atStavr());
		stavropol.setTarget("_blank");
		add(stavropol);

		Tiles.init(eventBus);
		appActivityManager.setDisplay(appPanel);
		historyHandler.register(placeController, eventBus, AboutPlace.INSTANCE);
		historyHandler.handleCurrentHistory();
	}

	public static void goTo(Place place) {
		placeController.goTo(place);
	}

}