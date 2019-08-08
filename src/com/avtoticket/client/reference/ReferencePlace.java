/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.reference;

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
public class ReferencePlace extends TilePlace {

	public static final String NAME = "reference";
	public static final ReferencePlace INSTANCE = new ReferencePlace();

	public interface Style extends CssResource {
		String CSS_PATH = "reference.css";

		String atReferenceBig();

		String atReferenceLabel();

		String atReferenceMedium();

		String atReferenceSmall();

		String atReferenceCaption();

		String atReferenceCells();

		String atReferenceCell();

		String atReferenceCellHeader();

		String atReferenceFieldLeft();

		String atReferenceFieldRight();
	}

	public interface Resources extends ClientBundle {

		ImageResource referenceSmall();

		ImageResource referenceMedium();

		@Source(Style.CSS_PATH)
	    Style referenceStyle();
	}

	public interface Strings extends Constants {
		@DefaultStringValue("Справочные")
		String caption();
	}

	private final Resources RESOURCES = GWT.create(Resources.class);
	private final Style CSS = RESOURCES.referenceStyle();
	private final Strings STRINGS = GWT.create(Strings.class);

	private final Activity bigActivity;
	private final Activity mediumActivity;
	private final Activity smallActivity;

	private ReferencePlace() {
		CSS.ensureInjected();
		bigActivity = new AbstractActivity() {
			private ReferenceBig referenceBig = new ReferenceBig(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(referenceBig);
			}
		};
		mediumActivity = new AbstractActivity() {
			private ReferenceMedium referenceMedium = new ReferenceMedium(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(referenceMedium);
			}
		};
		smallActivity = new AbstractActivity() {
			private ReferenceSmall referenceSmall = new ReferenceSmall(CSS, STRINGS);

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(referenceSmall);
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