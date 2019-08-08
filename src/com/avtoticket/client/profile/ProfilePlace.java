/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.profile;

import com.avtoticket.client.ui.BaseListBox;
import com.avtoticket.client.utils.ATPlace;
import com.avtoticket.client.utils.SessionUtil;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 10 янв. 2016 г. 0:22:44
 */
public class ProfilePlace extends ATPlace {

	public static final String NAME = "profile";
	public static final ProfilePlace INSTANCE = new ProfilePlace();

	public static interface Style extends CssResource {
		String CSS_PATH = "profile.css";

		String atProfilePanel();

		String atProfilePanelAdmin();

		String atProfileAccountPanel();

		String atProfileAvatar();

		String atProfileLastName();

		String atProfileFirstName();

		String atProfileAge();

		String atProfileAccountLabel();

		String atProfileAccountValue();

		String atProfileAccountEditBtn();

		String atProfileAccountField();

		String atProfileAccountEditorTitle();

		String atProfileTicketCounts();

		String atProfileTicketCountsLabel();

		String atProfileTicketCountsValue();
	}

	public static interface Resources extends ClientBundle {

		ImageResource placeholder();

		@Source(Style.CSS_PATH)
		@Import({BaseListBox.Style.class})
	    Style profileStyle();
	}

	private final Resources RESOURCES = GWT.create(Resources.class);
	private final Style CSS = RESOURCES.profileStyle();

	private ProfilePanel profilePanel;
	private final Activity profileActivity = new AbstractActivity() {
		@Override
		public void start(AcceptsOneWidget panel, EventBus eventBus) {
			if (SessionUtil.getUser() != null) {
				if (profilePanel == null)
					profilePanel = new ProfilePanel(CSS);
				panel.setWidget(profilePanel);
			} else
				panel.setWidget(new Label("Для просмотра профиля Вы должны авторизоваться"));
		}
	};

	public ProfilePlace() {
		CSS.ensureInjected();
	}

	@Override
	public Activity getActivity(Object param) {
		return profileActivity;
	}

	@Override
	public String toString() {
		return NAME;
	}

}