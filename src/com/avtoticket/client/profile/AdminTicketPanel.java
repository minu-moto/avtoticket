/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.profile;

import java.util.Map;

import com.avtoticket.client.ui.TicketsPanel;
import com.avtoticket.client.utils.DefaultCallback;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.shared.models.PageContainer;
import com.avtoticket.shared.models.core.Ticket;
import com.avtoticket.shared.models.core.TicketStatus;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 24 янв. 2016 г. 0:35:06
 */
public class AdminTicketPanel extends TicketsPanel {

	public AdminTicketPanel(ProfilePlace.Style CSS) {
		super(new AdminTicketCell(), new AsyncDataProvider<Ticket>(keyProvider) {
			@Override
			protected void onRangeChanged(HasData<Ticket> display) {
				Range range = display.getVisibleRange();
				RPC.getTS().getTickets(null, range.getLength(), range.getStart(), new AsyncCallback<PageContainer<Ticket>>() {
					@Override
					public void onSuccess(PageContainer<Ticket> result) {
						updateRowCount(result.getItemsCount(), true);
						updateRowData(range.getStart(), result.getPage());
					}

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}
				});
			}
		});

		RPC.getTS().getTicketCounts((DefaultCallback<Map<TicketStatus, Long>>) counts -> {
			FlexTable table = new FlexTable();
			table.setText(0, 0, TicketStatus.SOLD + ":");
			table.setText(0, 1, String.valueOf(counts.get(TicketStatus.SOLD)));
			table.setText(0, 2, TicketStatus.IN_PROCESSING + ":");
			table.setText(0, 3, String.valueOf(counts.get(TicketStatus.IN_PROCESSING)));

			CellFormatter formatter = table.getCellFormatter();
			formatter.setStyleName(0, 0, CSS.atProfileTicketCountsLabel());
			formatter.setStyleName(0, 1, CSS.atProfileTicketCountsValue());
			formatter.setStyleName(0, 2, CSS.atProfileTicketCountsLabel());
			formatter.setStyleName(0, 3, CSS.atProfileTicketCountsValue());
			table.addStyleName(CSS.atProfileTicketCounts());
			add(table);
		});
	}

}