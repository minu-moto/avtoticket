/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.ui;

import com.avtoticket.client.menu.RouteSelector;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.Waiter;
import com.avtoticket.shared.models.PageContainer;
import com.avtoticket.shared.models.core.Ticket;
import com.avtoticket.shared.models.core.TicketStatus;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 20 янв. 2016 г. 0:02:18
 */
public class TicketsPanel extends FlowPanel {

	public static interface Style extends CssResource {
		String CSS_PATH = "tickets.css";

		String atTickets();

		String atTicketsList();

		String atTicketsHeader();

		String atTicketsRepeat();

		String atTicketsDelete();

		String atTicketsRow();

		String atTicketsLabel();

		String atTicketsValue();

		String atTicketsWaiter();

		String atTicketsPager();

		String atTicketsPagerCount();

		String atTicketsPagerPrev();

		String atTicketsPagerNext();

		String atTicketsPagerDisabledBtn();
	}

	public static interface Resources extends ClientBundle {

		ImageResource waiter();

		@Source(Style.CSS_PATH)
		@Import({BaseListBox.Style.class, CellList.Style.class})
	    Style ticketsStyle();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	public static final Style CSS = RESOURCES.ticketsStyle();

	public static final ProvidesKey<Ticket> keyProvider = item -> (item != null) ? item.getId() : null;

	private CellList<Ticket> list;

	public static TicketsPanel getInstance(TicketStatus status, boolean hasPaymentRepeat) {
		TicketCell cell = new TicketCell();
		TicketsPanel ret = new TicketsPanel(cell, new AsyncDataProvider<Ticket>(keyProvider) {
			@Override
			protected void onRangeChanged(HasData<Ticket> display) {
				Range range = display.getVisibleRange();
				RPC.getTS().getTickets(status, range.getLength(), range.getStart(), new AsyncCallback<PageContainer<Ticket>>() {
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
		if (hasPaymentRepeat) {
			cell.setRepeatText("Продолжить оплату");
			cell.setRepeatUrlSupplier(Ticket::getPaymentOrderURL);
		} else
			cell.setRepeatClickHandler(ticket -> RouteSelector.search(ticket.getFrom(), ticket.getTo()));
		cell.setDeleteClickHandler(ticket -> {
			if (Window.confirm("Действительно хотите удалить билет №" + ticket.getTktNumber() + "?")) {
				ticket.setHidden(true);
				Waiter.start();
				RPC.getTS().saveModel(ticket, new AsyncCallback<Long>() {
					@Override
					public void onFailure(Throwable caught) {
						Waiter.stop();
						Window.alert(caught.getMessage());
					}

					@Override
					public void onSuccess(Long result) {
						Waiter.stop();
						ret.refresh();
					}
				});
			}
		});
		return ret;
	}

	public TicketsPanel(Cell<Ticket> cell, AbstractDataProvider<Ticket> dataProvider) {
		addStyleName(CSS.atTickets());

		list = new CellList<Ticket>(cell, keyProvider);
		list.addStyleName(CSS.atTicketsList());

		TicketPager pager = new TicketPager(CSS);
		pager.setDisplay(list);
		pager.setPageSize(3);

		list.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		list.setEmptyListWidget(new Label("нет данных для отображения"));
		FlowPanel waiter = new FlowPanel();
		waiter.addStyleName(CSS.atTicketsWaiter());
		list.setLoadingIndicator(waiter);

		if (dataProvider != null)
			dataProvider.addDataDisplay(list);

		add(pager);
		add(list);
	}

	public void refresh() {
		list.setVisibleRangeAndClearData(list.getVisibleRange(), true);
	}

}