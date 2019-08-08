/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.admin;

import com.avtoticket.client.utils.ATPlace;
import com.avtoticket.client.utils.SessionUtil;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 23 янв. 2016 г. 23:15:14
 */
public class AdminPlace extends ATPlace {

	public static final String NAME = "admin";
	public static final AdminPlace INSTANCE = new AdminPlace();

	public interface Style extends CssResource {
		String CSS_PATH = "admin.css";

		String atAdminPanel();

		String atAdminNewsRichEdit();

		String atAdminNewsRichArea();

		String atAdminNewsRichToolbar();

		String atAdminNewsCaptionLbl();

		String atAdminNewsCaption();

		String atAdminNewsPictureLbl();

		String atAdminNewsPicture();

		String atAdminBirthdayCaptionLbl();

		String atAdminBirthdayCaption();

		String atAdminBirthdayRichEdit();

		String atAdminBirthdayRichArea();

		String atAdminServiceMode();

		String atAdminServiceModeHint();

		String atAdminServiceModeButton();

		String atAdminSalePanelLayout();

		String atAdminFormulasGrid();
	}

	public interface Resources extends ClientBundle {
		@Source(Style.CSS_PATH)
	    Style adminStyle();
	}

	private final Resources RESOURCES = GWT.create(Resources.class);
	private final Style CSS = RESOURCES.adminStyle();

	private AdminPanel profilePanel;
	private final Activity adminActivity = new AbstractActivity() {
		@Override
		public void start(AcceptsOneWidget panel, EventBus eventBus) {
			if ((SessionUtil.getUser() != null) && (SessionUtil.getUser().isAdmin() == Boolean.TRUE)) {
				if (profilePanel == null)
					profilePanel = new AdminPanel(CSS);
				panel.setWidget(profilePanel);
			} else
				panel.setWidget(new Label("Доступ закрыт"));
		}
	};

	@Override
	public Activity getActivity(Object param) {
		return adminActivity;
	}

	private AdminPlace() {
		CSS.ensureInjected();
	}

	@Override
	public String toString() {
		return NAME;
	}

}