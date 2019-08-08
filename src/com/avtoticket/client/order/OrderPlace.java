/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.order;

import com.avtoticket.client.menu.RouteSelector;
import com.avtoticket.client.ui.BaseListBox;
import com.avtoticket.client.utils.ATPlace;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.Waiter;
import com.avtoticket.shared.models.core.Passage;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 23 янв. 2016 г. 0:15:43
 */
public class OrderPlace extends ATPlace {

	public static final String NAME = "order";

	public static interface Style extends CssResource {
		String CSS_PATH = "order.css";

		String atOrderPassageInfo();

		String atOrderTitle();

		String atOrderLabel();

		String atOrderValue();

		String atOrderTickets();

		String atOrderPanel();

		String atOrderTicketEditor();

		String atOrderTicketAdd();

		String atOrderTicketTitle();

		String atOrderPricePanel();

		String atOrderPriceValue();

		String atOrderPricePayBtn();

		String atOrderTicketField();

		String atOrderTicketType();

		String atOrderTicketBaggage();

		String atOrderTicketBaggageLbl();

		String atOrderTicketTemplateBtn();

		String atOrderTicketTemplateMenu();

		String atOrderTicketTemplatePopup();

		String atOrderTicketClose();

		String atOrderTicketsUnclosable();
	}

	public static interface Resources extends ClientBundle {

		ImageResource close();

		ImageResource baggage();

		@Source(Style.CSS_PATH)
		@Import(BaseListBox.Style.class)
	    Style orderStyle();
	}

	private final Resources RESOURCES = GWT.create(Resources.class);
	private final Style CSS = RESOURCES.orderStyle();

	private Long depId;
	private Long destId;
	private Long passageId;
	private String depDate;

	private final Activity orderActivity = new AbstractActivity() {
		@Override
		public void start(AcceptsOneWidget panel, EventBus eventBus) {
			if ((depId == null) || (destId == null) || (passageId == null) || (depDate == null))
				panel.setWidget(new Label("Рейс не найден. Повторите поиск."));
			else {
//				panel.setWidget(null);
				Waiter.start();
				RPC.getTS().getPassage(passageId, depId, destId, depDate, new AsyncCallback<Passage>() {
					@Override
					public void onSuccess(Passage result) {
						Waiter.stop();
						if (result == null)
							panel.setWidget(new Label("Рейс не найден. Повторите поиск."));
						else {
							RouteSelector.setParams(result.getDepId(), result.getDestId(), result.getDeparture());
							panel.setWidget(new OrderPanel(CSS, result));
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						Waiter.stop();
						Window.alert(caught.getMessage());
					}
				});
			}
		}
	};

	public static OrderPlace getInstance(String token) {
		String[] params = token.split(PARAM_SEPARATOR);
		Long depId = null;
		if (params.length > 1)
			try {
				depId = Long.valueOf(params[1]);
			} catch (Exception ignored) {
			}
		Long destId = null;
		if (params.length > 2)
			try {
				destId = Long.valueOf(params[2]);
			} catch (Exception ignored) {
			}
		String depDate = null;
		if (params.length > 3)
			depDate = params[3];
		Long passageId = null;
		if (params.length > 4)
			try {
				passageId = Long.valueOf(params[4]);
			} catch (Exception ignored) {
			}
		return new OrderPlace(depId, destId, depDate, passageId);
	}

	public OrderPlace(Long depId, Long destId, String depDate, Long passageId) {
		this.depId = depId;
		this.destId = destId;
		this.passageId = passageId;
		this.depDate = depDate;
		CSS.ensureInjected();
	}

	@Override
	public Activity getActivity(Object param) {
		return orderActivity;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder(NAME);
		ret.append(PARAM_SEPARATOR);
		if (depId != null)
			ret = ret.append(depId);
		ret.append(PARAM_SEPARATOR);
		if (destId != null)
			ret = ret.append(destId);
		ret.append(PARAM_SEPARATOR);
		if (depDate != null)
			ret = ret.append(depDate);
		ret.append(PARAM_SEPARATOR);
		if (passageId != null)
			ret = ret.append(passageId);
		return ret.toString();
	}

}