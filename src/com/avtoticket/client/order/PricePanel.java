/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.order;

import java.util.ArrayList;
import java.util.List;

import com.avtoticket.client.order.OrderPlace.Style;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.SessionUtil;
import com.avtoticket.client.utils.Waiter;
import com.avtoticket.shared.models.core.Passage;
import com.avtoticket.shared.models.core.User;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 24 янв. 2016 г. 21:25:57
 */
public class PricePanel extends FlexTable implements ValueChangeHandler<Void> {

	private static final NumberFormat format = NumberFormat.getFormat("#,##0.00 р");

	private Passage psg;
	private TextBox edEmail = new TextBox();

	public PricePanel(Style CSS, Passage psg, TicketsPanel tickets) {
		this.psg = psg;
		addStyleName(CSS.atOrderPricePanel());
		tickets.addValueChangeHandler(this);

		setText(0, 0, "РАСЧЁТ СТОИМОСТИ");
		setText(1, 0, "Тип билета:");
		setText(1, 1, "Полный");
		setText(1, 2, "Детский");
		setText(1, 3, "Багажный");
		setText(2, 0, "Количество:");
		setText(2, 1, "0");
		setText(2, 2, "0");
		setText(2, 3, "0");
		setText(3, 0, "Сумма:");
		setText(3, 1, format.format(0.0));
		setText(3, 2, format.format(0.0));
		setText(3, 3, format.format(0.0));
		setText(3, 4, format.format(0.0));

		Label btnPay = new Label();
		btnPay.addStyleName(CSS.atOrderPricePayBtn());
		btnPay.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (SessionUtil.isServiceMode() && !SessionUtil.isAdmin())
					Window.alert("На портале ведутся технические работы. Покупка билетов временно невозможна.");
				else {
					List<User> users = new ArrayList<User>();
					for (TicketEditor editor : tickets.getTickets()) {
						User u = editor.getUser();
						if (u != null)
							users.add(u);
						else {
							Window.alert("Заполнены не все поля. Проверьте правильность заполнения даных и повторите попытку");
							tickets.selectTab(editor);
							return;
						}
					}
					users.get(0).setLogin(edEmail.getValue());	// первый пользователь считается покупателем
					if (Window.confirm("Уважаемый клиент! Вы приняли решение купить электронный билет с помощью avtoticket.com. Вам необходимо оплатить билет в течении 20 минут! В случае неоплаты заказ билета будет снят!")) {
						Waiter.start();
						RPC.getTS().buyTicket(users, psg.getId(), psg.getDepId(), psg.getDestId(), psg.getDeparture(), new AsyncCallback<String>() {
							@Override
							public void onSuccess(String result) {
								Waiter.stop();
								if (result != null)
									Window.Location.assign(result);
							}
							
							@Override
							public void onFailure(Throwable caught) {
								Waiter.stop();
								Window.alert(caught.getMessage());
							}
						});
					}
				}
			}
		});
		setWidget(3, 5, btnPay);

		edEmail.getElement().getStyle().setMarginRight(18, Unit.PX);
		edEmail.getElement().setAttribute("placeholder", "E-mail");
		edEmail.addStyleName(CSS.atOrderTicketField());
		setWidget(1, 4, edEmail);

		FlexCellFormatter formatter = getFlexCellFormatter();
		formatter.addStyleName(0, 0, CSS.atOrderTitle());
		formatter.addStyleName(1, 0, CSS.atOrderLabel());
		formatter.addStyleName(1, 1, CSS.atOrderPriceValue());
		formatter.addStyleName(1, 2, CSS.atOrderPriceValue());
		formatter.addStyleName(1, 3, CSS.atOrderPriceValue());
		formatter.addStyleName(2, 0, CSS.atOrderLabel());
		formatter.addStyleName(2, 1, CSS.atOrderPriceValue());
		formatter.addStyleName(2, 2, CSS.atOrderPriceValue());
		formatter.addStyleName(2, 3, CSS.atOrderPriceValue());
		formatter.addStyleName(3, 0, CSS.atOrderLabel());
		formatter.addStyleName(3, 1, CSS.atOrderPriceValue());
		formatter.addStyleName(3, 2, CSS.atOrderPriceValue());
		formatter.addStyleName(3, 3, CSS.atOrderPriceValue());
		formatter.addStyleName(3, 4, CSS.atOrderPriceValue());
		formatter.setColSpan(1, 4, 2);
		formatter.setRowSpan(1, 4, 2);
		formatter.setHorizontalAlignment(1, 4, HasHorizontalAlignment.ALIGN_RIGHT);

		ColumnFormatter colFormatter = getColumnFormatter();
		colFormatter.setWidth(0, "210px");
		colFormatter.setWidth(1, "140px");
		colFormatter.setWidth(2, "180px");
		colFormatter.setWidth(3, "190px");
		colFormatter.setWidth(4, "250px");
		colFormatter.setWidth(5, "100%");
	}

	@Override
	public void onValueChange(ValueChangeEvent<Void> event) {
		List<TicketEditor> tickets = ((TicketsPanel) event.getSource()).getTickets();
		int adult = 0;
		int child = 0;
		int baggage = 0;
		for (TicketEditor ticket : tickets) {
			if (ticket.isChild())
				child++;
			else
				adult++;
			baggage += ticket.getBags();
		}

		setText(2, 1, String.valueOf(adult));
		setText(2, 2, String.valueOf(child));
		setText(2, 3, String.valueOf(baggage));

		double adultSum = psg.getSumm() * adult / 100.0;
		double childSum = psg.getChldSumm() * child / 100.0;
		double bagSum = psg.getBagSumm() * baggage / 100.0;

		setText(3, 1, format.format(adultSum));
		setText(3, 2, format.format(childSum));
		setText(3, 3, format.format(bagSum));
		setText(3, 4, format.format(adultSum + childSum + bagSum));

		User usr = SessionUtil.getUser();
		if (usr != null)
			edEmail.setValue(usr.getLogin());
	}

}