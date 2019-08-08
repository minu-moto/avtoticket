/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.profile;

import com.avtoticket.client.profile.ProfilePlace.Style;
import com.avtoticket.client.ui.TicketsPanel;
import com.avtoticket.client.utils.SessionUtil;
import com.avtoticket.shared.models.core.TicketStatus;
import com.avtoticket.shared.models.core.User;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 12 янв. 2016 г. 22:17:12
 */
public class ProfilePanel extends TabLayoutPanel {

	private Account account;
	private AccountEditor accountEditor;
	private RequisitesTable reqTab = new RequisitesTable();

	public ProfilePanel(Style CSS) {
		super(2.5, Unit.EM);
		setAnimationDuration(200);
		addStyleName(CSS.atProfilePanel());

		User user = SessionUtil.getUser();
		if ((user != null) && (user.isAdmin() == Boolean.TRUE))
			addStyleName(CSS.atProfilePanelAdmin());

		SimplePanel accLayout = new SimplePanel();
		account = new Account(CSS) {
			@Override
			public void onEditClick() {
				User user = new User();
				user.fill(SessionUtil.getUser());
				accountEditor.setModel(user);
				accLayout.setWidget(accountEditor);
			}
		};
		accountEditor = new AccountEditor(CSS) {
			@Override
			public void onUpdated() {
				User user = SessionUtil.getUser();
				user.fill(getModel());
				account.fillForm(user);
			}
			@Override
			public void onClose() {
				accLayout.setWidget(account);
			}
		};
		accLayout.setWidget(account);
		account.fillForm(SessionUtil.getUser());

		if ((user != null) && (user.isAdmin() == Boolean.TRUE))
			add(new AdminTicketPanel(CSS), "Все билеты");
		add(accLayout, "Мой профиль");
		add(reqTab, reqTab.getCaption());
		add(TicketsPanel.getInstance(TicketStatus.SOLD, false), "Мои билеты");
		add(TicketsPanel.getInstance(TicketStatus.IN_PROCESSING, true), "Билеты в оформлении");
		add(TicketsPanel.getInstance(TicketStatus.ERROR, false), "Ошибочные билеты");
		add(new ChangePasswordPanel(CSS), "Изменение пароля");
	}

}