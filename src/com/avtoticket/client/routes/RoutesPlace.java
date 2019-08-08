/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.routes;

import java.util.Date;

import com.avtoticket.client.menu.RouteSelector;
import com.avtoticket.client.utils.ATPlace;
import com.avtoticket.shared.utils.DateUtil;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 10 янв. 2016 г. 0:22:44
 */
public class RoutesPlace extends ATPlace {

	public static final String NAME = "routes";
	private static final RoutesGrid grid = new RoutesGrid();
	private static final DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MM.yyyy");

	private Long depId;
	private Long destId;
	private Date date;

	private final Activity ticketsActivity = new AbstractActivity() {
		@Override
		public void start(AcceptsOneWidget panel, EventBus eventBus) {
			RouteSelector.setParams(depId, destId, date);
			grid.refresh(depId, destId, date);
			panel.setWidget(grid);
		}
	};

	public static RoutesPlace getInstance(String token) {
		String[] params = token.split(PARAM_SEPARATOR);
		Long depId = null;
		Long destId = null;
		Date date = null;
		if (params.length > 1)
			try {
				depId = Long.valueOf(params[1]);
			} catch (Exception ignored) {
			}
		if (params.length > 2)
			try {
				destId = Long.valueOf(params[2]);
			} catch (Exception ignored) {
			}
		if (params.length > 3)
			try {
				date = DateUtil.localToMsk(dateFormat.parse(params[3]));
			} catch (Exception ignored) {
			}
		return new RoutesPlace(depId, destId, date);
	}

	public RoutesPlace(Long depId, Long destId, Date date) {
		this.depId = depId;
		this.destId = destId;
		this.date = date;
	}

	@Override
	public Activity getActivity(Object param) {
		return ticketsActivity;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder(NAME);
		if ((depId != null) && (destId != null) && (date != null))
			ret = ret.append(PARAM_SEPARATOR).append(depId)
					.append(PARAM_SEPARATOR).append(destId)
					.append(PARAM_SEPARATOR).append(dateFormat.format(date, DateUtil.getMSKTimeZone()));
		return ret.toString();
	}

}