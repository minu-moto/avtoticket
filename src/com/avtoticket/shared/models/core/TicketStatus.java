/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.shared.models.core;

import com.avtoticket.shared.models.EnumType;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 19 янв. 2016 г. 21:51:13
 */
@EnumType("core.ticket_status")
public enum TicketStatus {

	IN_PROCESSING("Забронирован"),
	RESERVED("Зарезервирован"),
	SOLD("Оплачен"),
	CANCELED("Отменён"),
	ERROR("Ошибка");

	private String text;

	private TicketStatus(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}

}