/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.news;

import java.util.Date;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.RowCountChangeEvent;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 2 янв. 2016 г. 21:01:28
 */
public class NewsPlace extends VerticalPanel {

	public interface Style extends CssResource {
		String CSS_PATH = "news.css";

		String atNewsList();

		String atNewsEmpty();

		String atNewsImgPreview();

		String atNewsTitle();

		String atNewsContent();

		String atNewsFlexContainer();

		String atNewsCellsContainer();

		String atNewsSelectedItem();

		String atNewsExpanded();

		String atNewsFooter();

		String atNewsVK();

		String atNewsFB();

		String atNewsOK();

		String atNewsMore();

		String atNewsWaiter();

		String atNewsCaption();

		String atNewsBack();

		String atNewsLayout();
	}

	public interface Resources extends ClientBundle {

		ImageResource vk();

		ImageResource fb();

		ImageResource ok();

		ImageResource waiter();

		@Source(Style.CSS_PATH)
		@Import(CellList.Style.class)
	    Style loginStyle();
	}

	@DefaultLocale("ru")
	public interface Strings extends Messages {
		@DefaultMessage("Наши новости")
		String news();

		@DefaultMessage("Назад к списку")
		String back();

		@DefaultMessage("ещё новости")
		String more();

		@DefaultMessage("Нет новостей")
		String empty();

		@DefaultMessage("Опубликовано {0,date:tz=$tz,dd.MM.yyyy}")
		String posted(Date date, TimeZone tz);
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	private static final Style CSS = RESOURCES.loginStyle();
	private static final Strings STRINGS = GWT.create(Strings.class);

	private NewsList newsList;
	private FlowPanel waiter;
	private ShowMorePager pager;

	public NewsPlace() {
		CSS.ensureInjected();
		addStyleName(CSS.atNewsLayout());

		Label caption = new Label(STRINGS.news());
		caption.addStyleName(CSS.atNewsCaption());
		add(caption);

		Anchor btnBack = new Anchor(STRINGS.back());
		btnBack.addStyleName(CSS.atNewsBack());
		btnBack.setVisible(false);
		btnBack.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				newsList.collapse();
				btnBack.setVisible(false);
				caption.setVisible(true);
				RowCountChangeEvent.fire(newsList, newsList.getRowCount(), newsList.isRowCountExact());	// при сворачивании обновляем видимость пагинатора
			}
		});
		add(btnBack);

		newsList = new NewsList(CSS, STRINGS) {
			@Override
			protected void onLoadingStateChanged(LoadingState state) {
				waiter.setVisible(state == LoadingState.PARTIALLY_LOADED);
				super.onLoadingStateChanged(state);
			}

			@Override
			public void expand(int idx) {
				super.expand(idx);
				btnBack.setVisible(true);
				caption.setVisible(false);
				pager.setVisible(false);
			}
		};
		add(newsList);

		waiter = new FlowPanel();
		waiter.addStyleName(CSS.atNewsWaiter());
		waiter.setVisible(false);
		add(waiter);

		pager = new ShowMorePager(CSS, STRINGS);
		pager.addStyleName(CSS.atNewsMore());
		pager.setDisplay(newsList);
		add(pager);
	}

}