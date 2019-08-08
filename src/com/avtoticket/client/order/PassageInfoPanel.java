/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.order;

import java.util.Date;

import com.avtoticket.client.order.OrderPlace.Style;
import com.avtoticket.shared.models.core.Passage;
import com.avtoticket.shared.utils.DateUtil;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 24 янв. 2016 г. 19:47:09
 */
public class PassageInfoPanel extends AbsolutePanel {

	private DateTimeFormat dateFormat = DateTimeFormat.getFormat("HH:mm dd MMMM");
	private DateTimeFormat timeFormat = DateTimeFormat.getFormat("H ч. mm мин.");

	private Label nameValue = new Label();
	private Label departureValue = new Label();
	private Label arrivalValue = new Label();
	private Label freeSeatsValue = new Label();
	private Label selectPlaceValue = new Label();
	private Label timeValue = new Label();

	public PassageInfoPanel(Style CSS) {
		addStyleName(CSS.atOrderPassageInfo());

		Label title = new Label("ИНФОРМАЦИЯ О РЕЙСЕ");
		title.addStyleName(CSS.atOrderTitle());
		add(title);

		Label name = new Label("Рейс:");
		name.addStyleName(CSS.atOrderLabel());
		add(name, 352, 0);

		nameValue.addStyleName(CSS.atOrderValue());
		add(nameValue, 402, 0);

		Label departure = new Label("Выезд:");
		departure.addStyleName(CSS.atOrderLabel());
		add(departure, 0, 40);

		departureValue.addStyleName(CSS.atOrderValue());
		add(departureValue, 63, 40);

		Label arrival = new Label("Прибытие:");
		arrival.addStyleName(CSS.atOrderLabel());
		add(arrival, 0, 80);

		arrivalValue.addStyleName(CSS.atOrderValue());
		add(arrivalValue, 94, 80);

		Label freeSeats = new Label("Свободных мест:");
		freeSeats.addStyleName(CSS.atOrderLabel());
		add(freeSeats, 460, 40);

		freeSeatsValue.addStyleName(CSS.atOrderValue());
		add(freeSeatsValue, 609, 40);

		Label selectPlace = new Label("Выбор мест:");
		selectPlace.addStyleName(CSS.atOrderLabel());
		add(selectPlace, 460, 80);

		selectPlaceValue.addStyleName(CSS.atOrderValue());
		add(selectPlaceValue, 569, 80);

		Label time = new Label("Время в пути:");
		time.addStyleName(CSS.atOrderLabel());
		add(time, 800, 0);

		timeValue.addStyleName(CSS.atOrderValue());
		add(timeValue, 920, 0);
	}

	public void setPassage(Passage psg) {
		nameValue.setText(psg.getId() + " " + psg.getName());
		departureValue.setText(dateFormat.format(psg.getDeparture(), DateUtil.getMSKTimeZone()) + " " + psg.getDepName());
		arrivalValue.setText(dateFormat.format(psg.getArrival(), DateUtil.getMSKTimeZone()) + " " + psg.getDestName());
		freeSeatsValue.setText(psg.getFreeSeats() + ((psg.getLongProp(Passage.TOTAL_SEATS) != null) ? " из " + psg.getTotalSeats() : ""));
		selectPlaceValue.setText((((psg.getTranzit() != null) && (psg.getTranzit() > 0)) ? "транизитный рейс; " : "")
				+ (((psg.getTranzit() != null) && (psg.getTranzit() != 0)) ? "билеты без указания мест" : "места назначаются автоматически"));
		timeValue.setText(timeFormat.format(new Date(psg.getArrival().getTime() - psg.getDeparture().getTime()), TimeZone.createTimeZone(0)));
	}

}