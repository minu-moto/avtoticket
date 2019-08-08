/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.news;

import com.avtoticket.client.news.NewsPlace.Strings;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.shared.models.PageContainer;
import com.avtoticket.shared.models.core.News;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 23 дек. 2015 г. 21:58:34
 */
public class NewsList extends CellList<News> {

//	private static final Logger logger = Logger.getLogger(NewsList.class.getName());

	private static ProvidesKey<News> keyProvider = new ProvidesKey<News>() {
		@Override
		public Object getKey(News item) {
			return (item != null) ? item.getId() : null;
		}
	};
	private SingleSelectionModel<News> ssm = new SingleSelectionModel<News>(keyProvider);
	private AsyncDataProvider<News> dataProvider = new AsyncDataProvider<News>(keyProvider) {
		@Override
		protected void onRangeChanged(HasData<News> display) {
			Range range = display.getVisibleRange();
			RPC.getTS().getNews(LocaleInfo.getCurrentLocale().getLocaleName(), range.getLength(), range.getStart(), new AsyncCallback<PageContainer<News>>() {
				@Override
				public void onSuccess(PageContainer<News> result) {
					updateRowCount(result.getItemsCount(), true);
					updateRowData(range.getStart(), result.getPage());
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							oldScroll = Math.max((result.getItemsCount() - 4) * 128, 0);
							getElement().setScrollTop(oldScroll);
						}
					});
				}

				@Override
				public void onFailure(Throwable caught) {
					Window.alert(caught.getMessage());
				}
			});
		}
	};
	private int oldScroll = 0;
	private Element clone;
	private boolean loading = false;
	private com.avtoticket.client.news.NewsPlace.Style CSS;

	public NewsList(com.avtoticket.client.news.NewsPlace.Style CSS, Strings STRINGS) {
		super(new NewsCell(CSS, STRINGS), keyProvider);
		this.CSS = CSS;

		addDomHandler(new ScrollHandler() {
			@Override
			public void onScroll(ScrollEvent event) {
				if (!isExpanded()) {
					int newScroll = getElement().getScrollTop();
					oldScroll = Math.round(1.0f * newScroll / 128) * 128;
					getElement().setScrollTop(oldScroll);
				}
			}
		}, ScrollEvent.getType());
		addStyleName(CSS.atNewsList());
		setPageSize(4);

		Label lblEmpty = new Label(STRINGS.empty());
		lblEmpty.addStyleName(CSS.atNewsEmpty());
		setEmptyListWidget(lblEmpty);

		FlowPanel waiter = new FlowPanel();
		waiter.addStyleName(CSS.atNewsWaiter());
		setLoadingIndicator(waiter);

		dataProvider.addDataDisplay(this);
		getChildContainer().addClassName(CSS.atNewsCellsContainer());

		setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		setSelectionModel(ssm, new DefaultSelectionEventManager<News>(null) {
			@Override
			protected void handleSelectionEvent(CellPreviewEvent<News> event,
					SelectAction action, SelectionModel<? super News> selectionModel) {
				NativeEvent nativeEvent = event.getNativeEvent();
			    String type = nativeEvent.getType();
				if (!loading && BrowserEvents.CLICK.equals(type))
					expand(event.getIndex());
			}
		});
	}

	public void expand(int idx) {
		Element list = getElement();
		Element selected = getChildElement(idx);
		int top = selected.getOffsetTop() - list.getScrollTop();
		int left = selected.getOffsetLeft() - list.getScrollLeft();

		clone = selected.cloneNode(true).<Element> cast();
		clone.addClassName(CSS.atNewsSelectedItem());
		clone.getStyle().setPosition(Position.ABSOLUTE);
		clone.getStyle().setLeft(left, Unit.PX);
		clone.getStyle().setTop(top, Unit.PX);
		clone.removeAttribute("__idx");
		list.appendChild(clone);
		list.getStyle().setOverflow(Overflow.AUTO);
		getChildContainer().getStyle().setDisplay(Display.NONE);
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				clone.addClassName(CSS.atNewsExpanded());
			}
		});
	}

	public void collapse() {
		if (clone != null) {
			clone.removeFromParent();
			clone = null;
			getChildContainer().getStyle().clearDisplay();
			getElement().setScrollTop(oldScroll);
			getElement().getStyle().clearOverflow();
		}
	}

	public boolean isExpanded() {
		return clone != null;
	}

	@Override
	protected void onLoadingStateChanged(LoadingState state) {
		loading = (state != LoadingState.LOADED);
		super.onLoadingStateChanged(state);
	}

}