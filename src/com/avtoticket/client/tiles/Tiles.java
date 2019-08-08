/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.tiles;

import com.avtoticket.client.about.AboutPlace;
import com.avtoticket.client.articles.ArticlePlace;
import com.avtoticket.client.map.MapPlace;
import com.avtoticket.client.mobile.MobilePlace;
import com.avtoticket.client.partners.PartnersPlace;
import com.avtoticket.client.project.ProjectPlace;
import com.avtoticket.client.reference.ReferencePlace;
import com.avtoticket.client.service.ServicePlace;
import com.avtoticket.client.utils.TileSize;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TableLayout;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 9 янв. 2016 г. 23:16:05
 */
public class Tiles extends FlexTable {

	private static Tiles INSTANCE;
	public static final Activity TILES_ACTIVITY = new AbstractActivity() {
		@Override
		public void start(AcceptsOneWidget panel, com.google.gwt.event.shared.EventBus eventBus) {
			panel.setWidget(INSTANCE);
		}
	};

	public static void init(EventBus eventBus) {
		INSTANCE = new Tiles(eventBus);
	}

	private TilePlace prevPlace;

	private final ActivityManager bigPanelManager;
	private final ActivityManager mediumPanelManager;
	private final ActivityManager smallPanel1Manager;
	private final ActivityManager smallPanel2Manager;
	private final ActivityManager smallPanel3Manager;
	private final ActivityManager smallPanel4Manager;
	private final ActivityManager smallPanel5Manager;

	private SimplePanel bigPanel = new SimplePanel();
	public SimplePanel mediumPanel = new SimplePanel();
	public SimplePanel smallPanel1 = new SimplePanel();
	public SimplePanel smallPanel2 = new SimplePanel();
	public SimplePanel smallPanel3 = new SimplePanel();
	public SimplePanel smallPanel4 = new SimplePanel();
	public SimplePanel smallPanel5 = new SimplePanel();

	private ActivityManager getTileManager(TilePlace defaultPlace, TileSize tileSize, EventBus eventBus) {
		return new ActivityManager(new ActivityMapper() {
			private TilePlace currentPlace = defaultPlace;

			@Override
			public Activity getActivity(Place place) {
				if (currentPlace == place)
					currentPlace = prevPlace;
				return currentPlace.getActivity(tileSize);
			}
		}, eventBus);
	}

	private Tiles(EventBus eventBus) {
		INSTANCE = this;
		setWidth("1270px");
		getElement().getStyle().setTableLayout(TableLayout.FIXED);

		bigPanelManager = new ActivityManager(new ActivityMapper() {
			private TilePlace currentPlace = AboutPlace.INSTANCE;

			@Override
			public Activity getActivity(Place place) {
				if (place instanceof ArticlePlace) {
					expandBigTile();
					return ((ArticlePlace) place).getActivity(null);
				} else {
					if (place instanceof TilePlace) {
						collapseBigTile();
						prevPlace = currentPlace;
						currentPlace = (TilePlace) place;
					}
					return currentPlace.getActivity(TileSize.BIG);
				}
			}
		}, eventBus);
		mediumPanelManager = getTileManager(MobilePlace.INSTANCE, TileSize.MEDIUM, eventBus);
		smallPanel1Manager = getTileManager(MapPlace.INSTANCE, TileSize.SMALL, eventBus);
		smallPanel2Manager = getTileManager(ServicePlace.INSTANCE, TileSize.SMALL, eventBus);
		smallPanel3Manager = getTileManager(ReferencePlace.INSTANCE, TileSize.SMALL, eventBus);
		smallPanel4Manager = getTileManager(ProjectPlace.INSTANCE, TileSize.SMALL, eventBus);
		smallPanel5Manager = getTileManager(PartnersPlace.INSTANCE, TileSize.SMALL, eventBus);

		setCellSpacing(0);
		setCellPadding(0);
		setWidget(0, 0, bigPanel);
		setWidget(0, 1, smallPanel1);
		setWidget(0, 2, smallPanel2);
		setWidget(0, 3, smallPanel3);
		setWidget(1, 0, smallPanel4);
		setWidget(2, 0, smallPanel5);
		setWidget(1, 1, mediumPanel);

		smallPanel1.getElement().getStyle().setOverflow(Overflow.HIDDEN);
		smallPanel4.getElement().getStyle().setOverflow(Overflow.HIDDEN);
		smallPanel5.getElement().getStyle().setOverflow(Overflow.HIDDEN);

		FlexCellFormatter formatter = getFlexCellFormatter();
		formatter.setRowSpan(0, 0, 3);
		formatter.setRowSpan(1, 1, 2);
		formatter.setColSpan(1, 1, 2);
		formatter.getElement(0, 2).getStyle().setPaddingRight(30, Unit.PX);
		formatter.getElement(0, 0).getStyle().setProperty("paddingRight", "30px");

		ColumnFormatter cf = getColumnFormatter();
		cf.setWidth(0, "100%");
		cf.setWidth(1, "212px");
		cf.setWidth(2, "212px");
		cf.setWidth(3, "182px");

		bigPanelManager.setDisplay(bigPanel);
		mediumPanelManager.setDisplay(mediumPanel);
		smallPanel1Manager.setDisplay(smallPanel1);
		smallPanel2Manager.setDisplay(smallPanel2);
		smallPanel3Manager.setDisplay(smallPanel3);
		smallPanel4Manager.setDisplay(smallPanel4);
		smallPanel5Manager.setDisplay(smallPanel5);
	}

	private void collapseBigTile() {
		getColumnFormatter().setWidth(1, "212px");
	}

	private void expandBigTile() {
		getColumnFormatter().setWidth(1, "0px");
	}

}