/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.admin;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.avtoticket.client.ui.DBTable;
import com.avtoticket.client.ui.RichTextToolbar;
import com.avtoticket.client.ui.grid.Grid;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.client.ui.grid.filters.Filter;
import com.avtoticket.client.ui.grid.filters.FilterChangeEvent;
import com.avtoticket.client.utils.Waiter;
import com.avtoticket.shared.models.core.Locales;
import com.avtoticket.shared.models.core.News;
import com.avtoticket.shared.utils.DateUtil;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 13 февр. 2016 г. 21:55:25
 */
public class NewsTable extends DBTable<News, Long> {

	private SingleSelectionModel<News> ssm = new SingleSelectionModel<News>(modelKeyProvider);
	private RichTextArea textArea = new RichTextArea();
	private TextBox edCaption = new TextBox();
	private TextBox edPicture = new TextBox();
	private AdminPlace.Style CSS;

	private BlurHandler blurHandler = new BlurHandler() {
		@Override
		public void onBlur(BlurEvent event) {
			News news = ssm.getSelectedObject();
			if (news != null) {
				boolean isDirty = false;
				if (!Objects.equals(news.getTitle(), edCaption.getValue())) {
					news.setTitle(edCaption.getValue());
					isDirty = true;
				}
				if (!Objects.equals(news.getImage(), edPicture.getValue())) {
					news.setImage(edPicture.getValue());
					isDirty = true;
				}
				if (!Objects.equals(news.getDescription(), textArea.getHTML())) {
					news.setDescription(textArea.getHTML());
					isDirty = true;
				}
				if (isDirty) {
					Waiter.start();
					save(news, new AsyncCallback<News>() {
						@Override
						public void onFailure(Throwable caught) {
							Waiter.stop();
						}

						@Override
						public void onSuccess(News result) {
							Waiter.stop();
							grid.redraw();
						}
					});
				}
			}
		}
	};

	public NewsTable(AdminPlace.Style css) {
		super("Новости", new News());
		getElement().getStyle().setPaddingRight(700, Unit.PX);
		CSS = css;
		edCaption.addBlurHandler(blurHandler);
		edPicture.addBlurHandler(blurHandler);
		textArea.addBlurHandler(blurHandler);
	}

	@Override
	protected void onAddClick() {
		News news = new News();
		news.setDateCreate(DateUtil.localToMsk(new Date()));
		try {
			news.setLocale(Locales.valueOf(LocaleInfo.getCurrentLocale().getLocaleName().toUpperCase()));
		} catch (Exception e) {
			news.setLocale(Locales.RU);
		}
		onCreate(news);
	}

	@Override
	protected void onCreated(News newModel) {
		super.onCreated(newModel);
		ssm.setSelected(newModel, true);
	}

	@Override
	protected void onDeleted(List<News> deleted) {
		super.onDeleted(deleted);
		ssm.clear();
	}

	@Override
	protected void build() {
		super.build();

		Label lbCaption = new Label("Название");
		Label lbPicture = new Label("Ссылка на заглавное изображение");

		lbCaption.addStyleName(CSS.atAdminNewsCaptionLbl());
		add(lbCaption);
		edCaption.addStyleName(CSS.atAdminNewsCaption());
		add(edCaption);
		lbPicture.addStyleName(CSS.atAdminNewsPictureLbl());
		add(lbPicture);
		edPicture.addStyleName(CSS.atAdminNewsPicture());
		add(edPicture);

		FlowPanel richPanel = new FlowPanel();
		RichTextToolbar toolbar = new RichTextToolbar(textArea);
		richPanel.addStyleName(CSS.atAdminNewsRichEdit());
		textArea.addStyleName(CSS.atAdminNewsRichArea());
		toolbar.addStyleName(CSS.atAdminNewsRichToolbar());
		richPanel.add(toolbar);
		richPanel.add(textArea);
		add(richPanel);
	}

	@Override
	protected void buildGrid() {
		// создаём таблицу, указываем ей откуда брать уникальные ключи моделей
		Grid<News> grid = new Grid<News>(modelKeyProvider, false) {
			@Override
			public void onUpdate(final Field<?, News> ci, final News object, final Object value) {
				NewsTable.this.onUpdate(ci, object, value);
			}
		};
		super.grid = grid;
		grid.setSelectionModel(ssm);
		grid.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		grid.setSkipRowHoverFloatElementCheck(false);
		grid.setSkipRowHoverStyleUpdate(false);
		grid.setSkipRowHoverCheck(false);

		ssm.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				News news = ssm.getSelectedObject();
				if (news != null) {
					textArea.setHTML(news.getDescription());
					edCaption.setValue(news.getTitle());
					edPicture.setValue(news.getImage());
				} else {
					textArea.setHTML((String) null);
					edCaption.setValue(null);
					edPicture.setValue(null);
				}
			}
		});

		grid.addFilterChangeHandler(new FilterChangeEvent.Handler() {
			@Override
			public void onFilterChange(FilterChangeEvent event) {
				if (btnClearFilter != null)
					btnClearFilter.setEnabled(!grid.getFilter().isEmpty());
			}
		});

		if (pager != null) {
			pager.setDisplay(grid);
			pager.setPageSize(22);
		}

		for (Filter f : filters)
			grid.addFilter(f);
		filters.clear();
		add(grid);
	}

	@Override
	protected void buildTableColumns() {
		addColumn(Field.asDateTo(grid).modelKey(News.DATE_CREATE).caption("Дата").width(80).sortByDefault());
		addColumn(Field.asTextTo(grid).modelKey(News.TITLE).caption("Название").width("100%").sortable());
		addColumn(Field.asEnumTo(grid, Locales.class).modelKey(News.LOCALE).caption("Язык").width(64).sortable());
//		addColumn(Field.asTextAreaTo(grid).modelKey(News.DESCRIPTION).width("100%").sortable().editable());
	}

}