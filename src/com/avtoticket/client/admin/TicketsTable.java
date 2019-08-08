/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.admin;

import com.avtoticket.client.ui.DBTable;
import com.avtoticket.client.ui.ImageBtn;
import com.avtoticket.client.ui.grid.fields.CustomFormatField.Formatter;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.shared.models.core.Ticket;
import com.avtoticket.shared.models.core.TicketStatus;

import com.google.gwt.i18n.client.NumberFormat;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 11 февр. 2016 г. 23:08:25
 */
public class TicketsTable extends DBTable<Ticket, Long> {

	public TicketsTable() {
		super("Билеты", new Ticket());
	}

	@Override
	protected void buildButtons() {
		super.buildButtons();
		((ImageBtn) btnAdd).setEnabled(false);
		btnDelete.setEnabled(false);
	}

	@Override
	protected void buildTableColumns() {
		addColumn(Field.asDateTo(grid).modelKey(Ticket.SELL_TIME).caption("Дата").width(80).sortByDefault());
		addColumn(Field.asTextTo(grid).modelKey(Ticket.SERIYA).caption("Серия пасп.").width(100).sortable());
		addColumn(Field.asTextTo(grid).modelKey(Ticket.NUMBER).caption("Номер пасп.").width(100).sortable());
		addColumn(Field.asTextTo(grid).modelKey(Ticket.PHONE).caption("Телефон").width(140).sortable());
		addColumn(Field.asTextTo(grid).modelKey(Ticket.FROM).caption("Пункт отправления").width("50%").sortable());
		addColumn(Field.asTextTo(grid).modelKey(Ticket.TO).caption("Пункт прибытия").width("50%").sortable());
		addColumn(Field.asLongTo(grid).modelKey(Ticket.NOMER_VEDOMOSTI).caption("Ведомость").width(120).sortable());
		addColumn(Field.asLongTo(grid).modelKey(Ticket.SEAT).caption("Место").width(80).sortable());
		addColumn(Field.asCurrencyTo(grid).modelKey(Ticket.TARIF).caption("Тариф").width(80).sortable()
				.formatter(new Formatter<Ticket>() {
					private NumberFormat format = NumberFormat.getFormat("#,##0.00");

					@Override
					public String format(Ticket context) {
						if (context.getTarif() == null)
							return "";
						return format.format(context.getTarif() / 100.0);
					}
				}));
		addColumn(Field.asCurrencyTo(grid).modelKey(Ticket.TARIF_FACT).caption("Тариф ф.").width(80).sortable()
				.formatter(new Formatter<Ticket>() {
					private NumberFormat format = NumberFormat.getFormat("#,##0.00");

					@Override
					public String format(Ticket context) {
						if (context.getFactTarif() == null)
							return "";
						return format.format(context.getFactTarif() / 100.0);
					}
				}));
		addColumn(Field.asEnumTo(grid, TicketStatus.class).modelKey(Ticket.TICKET_STATUS).caption("Статус").width(100).sortable());
	}

}