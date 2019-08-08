/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.order;

import com.avtoticket.client.order.OrderPlace.Style;
import com.avtoticket.shared.models.core.Passage;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 24 янв. 2016 г. 20:02:33
 */
public class OrderPanel extends FlowPanel {

	private TicketsPanel tickets;
	private PassageInfoPanel passage;
	private PricePanel price;

	public OrderPanel(Style CSS, Passage p) {
		addStyleName(CSS.atOrderPanel());

		passage = new PassageInfoPanel(CSS);
		tickets = new TicketsPanel(CSS, p.getDeparture(), (p.getBagSumm() != null) && (p.getBagSumm() > 0L));
		price = new PricePanel(CSS, p, tickets);

		add(tickets);
		add(passage);
		add(price);

		passage.setPassage(p);
	}

}