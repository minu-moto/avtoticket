/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.avtoticket.client.order.OrderPlace.Style;
import com.avtoticket.client.utils.SessionUtil;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 24 янв. 2016 г. 20:20:37
 */
public class TicketsPanel extends TabLayoutPanel implements HasValueChangeHandlers<Void> {

	private final Style CSS;

	private List<TicketEditor> tickets = new ArrayList<TicketEditor>();
	private List<Label> captions = new ArrayList<Label>();

	public TicketsPanel(Style CSS, Date departure, boolean showBags) {
		super(36, Unit.PX);
		this.CSS = CSS;
		setAnimationDuration(200);
		addStyleName(CSS.atOrderTickets());
		addStyleName(CSS.atOrderTicketsUnclosable());

		addTicket(departure, showBags);
		tickets.get(0).setUser(SessionUtil.getUser());
	}

	private void addTicket(Date departure, boolean showBags) {
		int i = getWidgetCount() + 1;
		TicketEditor ticket = new TicketEditor(CSS, departure, showBags) {
			@Override
			protected void onAddTicketClick() {
				for (TicketEditor editor : tickets)
					if (editor.getUser() == null) {
						Window.alert("Заполнены не все поля. Проверьте правильность заполнения данных и повторите попытку");
						return;
					}
				addTicket(departure, showBags);
				TicketsPanel.this.removeStyleName(CSS.atOrderTicketsUnclosable());
			}

			@Override
			protected void onValueChange() {
				ValueChangeEvent.fire(TicketsPanel.this, null);
			}
		};

		FlowPanel tab = new FlowPanel();
		Label caption = new Label("БИЛЕТ " + i);
		captions.add(caption);
		tab.add(caption);
		FlowPanel btnClose = new FlowPanel();
		btnClose.addStyleName(CSS.atOrderTicketClose());
		btnClose.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				event.preventDefault();
				remove(ticket);
				tickets.remove(ticket);
				captions.remove(caption);
				if (tickets.size() == 1)
					addStyleName(CSS.atOrderTicketsUnclosable());
				for (int i = 1; i <= captions.size(); i++)
					captions.get(i - 1).setText("БИЛЕТ " + i);

				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						ValueChangeEvent.fire(TicketsPanel.this, null);
					}
				});
			}
		}, ClickEvent.getType());
		tab.add(btnClose);

		add(ticket, tab);
		selectTab(i - 1);
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				ValueChangeEvent.fire(TicketsPanel.this, null);
			}
		});
		tickets.add(ticket);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Void> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	public List<TicketEditor> getTickets() {
		return tickets;
	}

}