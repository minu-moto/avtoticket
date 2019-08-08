/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.ui;

import com.avtoticket.client.ui.TicketsPanel.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 янв. 2016 г. 23:17:05
 */
public class TicketPager extends AbstractPager {

	private FlowPanel layout = new FlowPanel();
	private FlowPanel prevBtn = new FlowPanel();
	private FlowPanel nextBtn = new FlowPanel();
	private Label lblCount = new Label();
	private final Style CSS;

	public TicketPager(Style CSS) {
		this.CSS = CSS;
		layout.addStyleName(CSS.atTicketsPager());
		initWidget(layout);

		prevBtn.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				previousPage();
			}
		}, ClickEvent.getType());
		prevBtn.addStyleName(CSS.atTicketsPagerPrev());
		layout.add(prevBtn);

		nextBtn.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				nextPage();
			}
		}, ClickEvent.getType());
		nextBtn.addStyleName(CSS.atTicketsPagerNext());
		layout.add(nextBtn);

		lblCount.addStyleName(CSS.atTicketsPagerCount());
		layout.add(lblCount);
	}

	protected String createText() {
		NumberFormat formatter = NumberFormat.getFormat("#,###");
		HasRows display = getDisplay();
		Range range = display.getVisibleRange();
		int pageStart = range.getStart() + 1;
		int pageSize = range.getLength();
		int dataSize = display.getRowCount();
		int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
		endIndex = Math.max(pageStart, endIndex);
		boolean exact = display.isRowCountExact();
		return formatter.format(pageStart) + "-" + formatter.format(endIndex) + (exact ? " из " : " из более ")
				+ formatter.format(dataSize);
	}

	@Override
	public void setPageSize(int pageSize) {
		super.setPageSize(pageSize);
	}

	@Override
	protected void onRangeOrRowCountChanged() {
		HasRows display = getDisplay();
		lblCount.setText(createText());

	    setPrevPageButtonsDisabled(!hasPreviousPage());

	    if (isRangeLimited() || !display.isRowCountExact())
	    	setNextPageButtonsDisabled(!hasNextPage());
	}

	private void setNextPageButtonsDisabled(boolean b) {
		nextBtn.setStyleName(CSS.atTicketsPagerDisabledBtn(), b);
	}

	private void setPrevPageButtonsDisabled(boolean b) {
		prevBtn.setStyleName(CSS.atTicketsPagerDisabledBtn(), b);
	}

}