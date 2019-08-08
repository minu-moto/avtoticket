/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.partners;

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
public class PartnersPlace extends TilePlace {

	public static final String NAME = "partners";
	public static final PartnersPlace INSTANCE = new PartnersPlace();

	public interface Style extends CssResource {
		String CSS_PATH = "partners.css";

		String atPartnersBig();

		String atPartnersLabel();

		String atPartnersMedium();

		String atPartnersSmall();
	}

	public interface Resources extends ClientBundle {

		ImageResource partnersBig();

		ImageResource partnersMedium();

		ImageResource partnersSmall();

		@Source(Style.CSS_PATH)
	    Style partnersStyle();
	}

	public interface Strings extends Constants {
		@DefaultStringValue("Партнеры")
		String caption();
	}

	private final Resources RESOURCES = GWT.create(Resources.class);
	private final Style CSS = RESOURCES.partnersStyle();
	private final Strings STRINGS = GWT.create(Strings.class);

	private final Activity bigActivity;
	private final Activity mediumActivity;
	private final Activity smallActivity;

	private PartnersPlace() {
		CSS.ensureInjected();
		bigActivity = new AbstractActivity() {
			private PartnersBig partnersBig = new PartnersBig(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(partnersBig);
			}
		};
		mediumActivity = new AbstractActivity() {
			private PartnersMedium partnersMedium = new PartnersMedium(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(partnersMedium);
			}
		};
		smallActivity = new AbstractActivity() {
			private PartnersSmall partnersSmall = new PartnersSmall(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(partnersSmall);
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