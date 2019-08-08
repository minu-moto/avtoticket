/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.news;

import com.avtoticket.client.news.NewsPlace.Strings;
import com.avtoticket.client.news.NewsPlace.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.Anchor;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 2 янв. 2016 г. 20:44:21
 */
public class ShowMorePager extends AbstractPager implements ClickHandler {

	private static final int DEFAULT_INCREMENT = 4;

	public ShowMorePager(Style CSS, Strings STRINGS) {
		Anchor btnMore = new Anchor(STRINGS.more());
		btnMore.addStyleName(CSS.atNewsMore());
		btnMore.addClickHandler(this);
		initWidget(btnMore);
	}

	@Override
	protected void onRangeOrRowCountChanged() {
		setVisible(hasNextPage());
	}

	@Override
	public void onClick(ClickEvent event) {
		setPageSize(getDisplay().getVisibleRange().getLength() + DEFAULT_INCREMENT);
	}

}