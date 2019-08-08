/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.admin;

import com.avtoticket.client.admin.AdminPlace.Style;
import com.avtoticket.client.ui.TabItem;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.TabLayoutPanel;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 11 февр. 2016 г. 22:59:22
 */
public class AdminPanel extends TabLayoutPanel {

	public AdminPanel(Style CSS) {
		super(2.5, Unit.EM);
		setAnimationDuration(200);
		addStyleName(CSS.atAdminPanel());

		addTab(new UsersTable());
		addTab(new BillsTable());
		addTab(new TicketsTable());
		addTab(new NasPunktsTable());
		addTab(new NewsTable(CSS));
		addTab(new StationsTable());
		addTab(new SalePanel(CSS));
		addTab(new BirthdayTab(CSS));
	}

	private void addTab(TabItem tab) {
		add(tab, tab.getCaption());
	}

}