/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.admin;

import com.avtoticket.client.ui.DBTable;
import com.avtoticket.client.ui.ImageBtn;
import com.avtoticket.client.ui.grid.fields.CustomFormatField.Formatter;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.shared.models.core.Bill;
import com.avtoticket.shared.models.core.TicketStatus;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 11 февр. 2016 г. 23:08:25
 */
public class BillsTable extends DBTable<Bill, Long> {

	public BillsTable() {
		super("Платежи", new Bill());
	}

	@Override
	protected void buildButtons() {
		super.buildButtons();
		((ImageBtn) btnAdd).setEnabled(false);
		btnDelete.setEnabled(false);
	}

	@Override
	protected void buildTableColumns() {
		addColumn(Field.asDateTo(grid).modelKey(Bill.OP_DATE).caption("Дата").width(80).sortByDefault());
		addColumn(Field.asTextTo(grid).modelKey(Bill.PHONE).caption("Телефон").width(140).sortable());
		addColumn(Field.asTextTo(grid).modelKey(Bill.EMAIL).caption("E-mail").width(160).sortable());
		addColumn(Field.asCurrencyTo(grid).modelKey(Bill.AMOUNT).caption("Сумма").width(80).sortable()
				.formatter(new Formatter<Bill>() {
					private NumberFormat format = NumberFormat.getFormat("#,##0.00");

					@Override
					public String format(Bill context) {
						if (context.getAmount() == null)
							return "";
						return format.format(context.getAmount() / 100.0);
					}
				}));
		addColumn(Field.asTextTo(grid).modelKey(Bill.COMMENT).caption("Коментарий").width("100%").sortable());
		addColumn(Field.asEnumTo(grid, TicketStatus.class).modelKey(Bill.BP_STATUS).caption("Статус").width(100).sortable());
	}

}