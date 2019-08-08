/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.profile;

import com.avtoticket.client.ui.TicketCell;
import com.avtoticket.shared.models.core.Ticket;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 24 янв. 2016 г. 0:44:20
 */
public class AdminTicketCell extends TicketCell {

	@Override
	public void render(Context context, Ticket ticket, SafeHtmlBuilder sb) {
		if (ticket == null)
			return;
		sb.append(templates.header(CSS.atTicketsHeader(), "Рейс: " + ticket.getReisName() + " " + ticket.getReisId()))
			.append(getRow("Номер билета:", ticket.getTktNumber()))
			.append(getRow("Номер заказа:", String.valueOf(ticket.getBillId())))
			.append(getRow("Отправление:", timeFormat.format(ticket.getDeparture())))
			.append(getRow("Прибытие:", timeFormat.format(ticket.getArrival())))
			.append(getRow("Пассажир:", ticket.getLastname() + " " + ticket.getFirstname(), ticket.getMiddlename()))
			.append(getRow("Дата рождения:", (ticket.getBirthDate() != null) ? dateFormat.format(ticket.getBirthDate()) : ""))
			.append(getRow("Документ:", ticket.getDocTypeName(),
					"серия " + ticket.getSeriya() + " номер " + ticket.getNumber()))
			.append(getRow("Телефон:", ticket.getPhone()))
			.append(getRow("Место:", ticket.isBaggage() ? "багажное"
					: (((ticket.getTBTarif() != null) && (ticket.getTBTarif() > 0L)) ? "б/м" : String.valueOf(ticket.getSeat()))))
			.append(getRow("Стоимость:", (ticket.getAmount() != null) ? currencyFormat.format(ticket.getAmount() / 100.0) : ""))
			.append(getRow("Статус:", (ticket.getStatus() != null) ? ticket.getStatus().toString() : ""));
	}

}