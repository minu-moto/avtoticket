/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.tickets;

import java.util.List;

import com.avtoticket.client.ui.TicketCell;
import com.avtoticket.client.ui.TicketsPanel;
import com.avtoticket.client.utils.ATPlace;
import com.avtoticket.client.utils.DefaultCallback;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.SessionUtil;
import com.avtoticket.shared.models.core.Ticket;
import com.avtoticket.shared.models.core.User;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 2 февр. 2016 г. 22:40:24
 */
public class TicketsPlace extends ATPlace {

	public static final String NAME = "tickets";

	public static TicketsPlace getInstance(String token) {
		String[] params = token.split(PARAM_SEPARATOR);
		String hash = null;
		if (params.length > 1)
			hash = params[1];
		return new TicketsPlace(hash);
	}

	private String hash;
	private Button btnPrint;
	private Button btnPrintPos;

	public TicketsPlace(String hash) {
		this.hash = hash;
		btnPrint = new Button("Печать билета");
		btnPrint.addClickHandler(event ->
			Window.open("/report?format=pdf&sign=tickets&hash=" + hash, null, null));
		btnPrint.getElement().getStyle().setPosition(Position.ABSOLUTE);
		btnPrint.getElement().getStyle().setTop(0, Unit.PX);

		btnPrintPos = new Button("Печать чека");
		btnPrintPos.addClickHandler(event ->
			Window.open("/report?format=pdf&sign=tickets_pos&hash=" + hash, null, null));
		btnPrintPos.getElement().getStyle().setPosition(Position.ABSOLUTE);
		btnPrintPos.getElement().getStyle().setTop(0, Unit.PX);
		btnPrintPos.getElement().getStyle().setLeft(130, Unit.PX);
	}

	@Override
	public Activity getActivity(Object param) {
		return new AbstractActivity() {
			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				TicketsPanel ticketsPanel = new TicketsPanel(new TicketCell(), new AsyncDataProvider<Ticket>(TicketsPanel.keyProvider) {

					private List<Ticket> tickets;

					@Override
					protected void onRangeChanged(HasData<Ticket> display) {
						if (tickets != null) {
							Range range = display.getVisibleRange();
							updateRowCount(tickets.size(), true);
							updateRowData(range.getStart(), tickets.subList(Math.max(0, Math.min(tickets.size(), range.getStart())),
									Math.max(0, Math.min(tickets.size(), range.getStart() + range.getLength()))));
						} else
							RPC.getTS().getTicketsByHash(hash, (DefaultCallback<List<Ticket>>) result -> {
								tickets = result;
								onRangeChanged(display);
							});
					}
				});
				ticketsPanel.add(btnPrint);
				User user = SessionUtil.getUser();
				if ((user != null) && (user.isPrintTicket() == Boolean.TRUE))
					ticketsPanel.add(btnPrintPos);
				panel.setWidget(ticketsPanel);
			}
		};
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder(NAME);
		if (hash != null)
			ret = ret.append(PARAM_SEPARATOR).append(hash);
		return ret.toString();
	}

}