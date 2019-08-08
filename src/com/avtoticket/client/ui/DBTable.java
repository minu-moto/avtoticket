/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.avtoticket.client.ui.grid.DataProvider;
import com.avtoticket.client.ui.grid.Grid;
import com.avtoticket.client.ui.grid.ModelEditorDialog;
import com.avtoticket.client.ui.grid.View;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.client.ui.grid.filters.Filter;
import com.avtoticket.client.ui.grid.filters.FilterChangeEvent;
import com.avtoticket.client.utils.DefaultCallback;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.Waiter;
import com.avtoticket.shared.models.BaseModel;

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;

/**
 * Стандартная панель с гридом и элементами управления.
 * 
 * @param <T> - тип записей в гриде
 * @param <K> - тип ключа, однозначно определяющего запись в гриде
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 26.12.2012 13:02:40
 */
public abstract class DBTable<T extends BaseModel, K extends Object> extends FlowPanel implements TabItem {

	public static interface Style extends CssResource {
		String CSS_PATH = "table.css";

		String atTablePager();

		String atTableToolbar();
	}

	public static interface Resources extends ClientBundle {
		ImageResource add();

		ImageResource delete();

		ImageResource refresh();

		ImageResource filterDel();

		@Source(Style.CSS_PATH)
	    Style tableStyle();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	private static final Style CSS = RESOURCES.tableStyle();

	/** Таблица, с записями */
	protected View<T> grid;

	/** Панель инструментов */
	protected AbsolutePanel toolbar = new AbsolutePanel();
	protected Widget btnAdd;
	protected ImageBtn btnDelete;
	protected ImageBtn btnRefresh;
	protected ImageBtn btnClearFilter;

	/** Пагинатор */
	protected SimplePager pager;

	/** Экземпляр модели данных */
	protected T saveModel;

	/** Редактор записей */
	protected ModelEditorDialog<T> editorDialog;

	protected DataProvider<T, K> dataProvider;
	protected final ProvidesKey<T> modelKeyProvider = getModelKeyProvider();

	private String caption;
	private boolean builded = false;

	protected List<Filter> filters = new ArrayList<Filter>();

	/**
	 * @param caption
	 *            - заголовок таба
	 * @param saveModel
	 * 			  - экземпляр модели данных
	 */
	public DBTable(String caption, T saveModel) {
		CSS.ensureInjected();
		this.caption = caption;
		this.saveModel = saveModel;
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		if (!builded) {
			build();
			builded = true;
			if (btnClearFilter != null) {
				for (Field<?, T> fld : grid.getFields())
					if ((fld != null) && fld.hasFilter()) {
						btnClearFilter.setVisible(true);
						break;
					}
				btnClearFilter.setEnabled(!grid.getFilter().isEmpty());
			}
		}
	}

	protected void build() {
		buildEditor();
		buildButtons();
		buildPager();
		buildGrid();
		buildTableColumns();
		buildDataProvider();
	}

	protected void buildEditor() {
		Editor<T> editorPanel = new ModelEditor<T>() {
			@Override
			protected void onEnterKeyDown() {
				editorDialog.onOkClick();
			}

			@Override
			protected void onEscKeyDown() {
				editorDialog.hide();
			}

			@Override
			protected void onError(String message) {
				if (message == null)
					editorDialog.clearState();
				else
					editorDialog.errorMessage(message);
			}
		};
		editorDialog = new ModelEditorDialog<T>(editorPanel);
	}

	protected void addColumn(Field<?, T> field) {
		field.addTo(grid);
		Editor<T> editor = editorDialog.getEditor();
		if ((editor != null) && (editor instanceof ModelEditor))
			((ModelEditor<T>) editor).addField(field);
	}

	protected ProvidesKey<T> getModelKeyProvider() {
		return item -> (item == null) ? null : item.getId();
	}

	@SuppressWarnings("unchecked")
	protected void buildDataProvider() {
		dataProvider = new PagedDataProvider<T, K>((Class<T>) saveModel.getClass(), modelKeyProvider, grid);
		dataProvider.addDataDisplay(grid);
	}

	/**
	 * Инициализация столбцов таблицы
	 */
	protected abstract void buildTableColumns();

	protected void buildButtons() {
		toolbar.addStyleName(CSS.atTableToolbar());

		toolbar.add(createAddButton());

		btnDelete = new ImageBtn(RESOURCES.delete(), "Удалить выбранные", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final List<T> forDel = new ArrayList<T>();
				for (T item : grid.getVisibleItems())
					if (grid.getSelectionModel().isSelected(item))
						forDel.add(item);

				if (forDel.isEmpty())
					Window.alert("Выберите записи, которые нужно удалить");
				else if (Window.confirm("Вы действительно хотите удалить выбранные записи?"))
					onDeleteClick(forDel);
			}
		});
		toolbar.add(btnDelete);

		btnRefresh = new ImageBtn(RESOURCES.refresh(), "Обновить список", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});
		toolbar.add(btnRefresh);

		btnClearFilter = new ImageBtn(RESOURCES.filterDel(), "Сбросить все фильтры", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				grid.clearFilter();
			}
		});
		btnClearFilter.setVisible(false);
		toolbar.add(btnClearFilter);

		add(toolbar);
	}

	protected Widget createAddButton() {
		btnAdd = new ImageBtn(RESOURCES.add(), "Добавить запись", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onAddClick();
			}
		});
		return btnAdd;
	}

	/**
	 * Создание грида
	 */
	protected void buildGrid() {
		// создаём таблицу, указываем ей откуда брать уникальные ключи моделей
		grid = new Grid<T>(modelKeyProvider, true) {
			@Override
			public void onUpdate(final Field<?, T> ci, final T object, final Object value) {
				DBTable.this.onUpdate(ci, object, value);
			}
		};

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

	protected void buildPager() {
		// создаём пагинатор
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true) {
			@Override
			protected String createText() {
				return super.createText().replace("of", "из").replace("over", "более");
			}

			@Override
			public void setPageStart(int index) {
			    if (getDisplay() != null) {
			        Range range = getDisplay().getVisibleRange();
			        int pageSize = range.getLength();
			        if (isRangeLimited() && getDisplay().isRowCountExact()) {
			        	int rc = getDisplay().getRowCount();
			        	if (rc <= index)								// если количество записей меньше текущего индекса
			        		index = ((rc - 1) / pageSize) * pageSize;	// то откатываемся на последнюю страницу
			        }
			        index = Math.max(0, index);
			        if (index != range.getStart())
			        	getDisplay().setVisibleRange(index, pageSize);
			    }
			}
		};
		pager.addStyleName(CSS.atTablePager());

		add(pager);
	}

	@Override
	public String getCaption() {
		return caption;
	}

	protected void onCreate(T model) {
		editorDialog.startProgress();
		save(model, new AsyncCallback<T>() {
			@Override
			public void onFailure(Throwable caught) {
				editorDialog.errorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(T newModel) {
				onCreated(newModel);
				editorDialog.finishProgress();
				editorDialog.hide();
			}
		});
	}

	protected void onCreated(T newModel) {
		int count = grid.getVisibleItemCount();
		dataProvider.updateData(grid.getRowCount(), Arrays.asList(newModel));
		if (count >= grid.getPageSize())
			dataProvider.updateCount(grid.getRowCount() + 1, grid.isRowCountExact());
	}

	protected void onUpdate(final Field<?, T> ci, final T object, Object value) {
		Waiter.start();
		saveModel.clear();
		saveModel.fill(object);
		saveModel.set(ci.getModelKey(), value);
		save(saveModel, new AsyncCallback<T>() {
			@Override
			public void onFailure(Throwable caught) {
				Waiter.stop();
				Window.alert(caught.getMessage());
				if ((ci.getColumn() != null) && (ci.getColumn().getCell() != null) && ci.getColumn().getCell() instanceof AbstractEditableCell)
					((AbstractEditableCell<?, ?>) ci.getColumn().getCell()).clearViewData(modelKeyProvider.getKey(object));
				grid.redraw();
			}

			@Override
			public void onSuccess(T result) {
				Waiter.stop();
				object.fill(result);
				onUpdated(ci, object);
			}
		});
	}

	protected void onUpdated(Field<?, T> ci, T object) { }

	protected void onDeleted(List<T> deleted) { }

	protected void onAddClick() {
		saveModel.clear();
		editorDialog.edit(true, saveModel, new DefaultCallback<T>() {
			@Override
			public void onSuccess(final T ret) {
				onCreate(ret);
			}
		});
	}

	@SuppressWarnings("unchecked")
	protected void onDeleteClick(final List<T> forDel) {
		List<K> dels = new ArrayList<K>();
		for (T item : forDel)
			dels.add((K) modelKeyProvider.getKey(item));
		dataProvider.removeItems(dels, new DefaultCallback<Void>() {
			@Override
			public void onSuccess(Void ret) {
				onDeleted(forDel);
			}
		});
	}

	/**
	 * Сохранение модели в базе
	 *
	 * @param saveModel
	 * @param callback
	 */
	protected void save(T saveModel, AsyncCallback<T> callback) {
		RPC.getTS().saveModel(saveModel, new AsyncCallback<Long>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(Long result) {
				T newModel = saveModel.copy();
				newModel.setId(result);
				callback.onSuccess(newModel);
			}
		});
	}

	@Override
	public void refresh() {
		if (grid != null)
			grid.refresh();
	}

	public void addFilter(Filter filter) {
		if (grid != null)
			grid.addFilter(filter);
		else
			filters.add(filter);
	}

	public View<T> getGrid() {
		return grid;
	}

	@Override
	public void onActivate() {
		
	}

	@Override
	public void onDeactivate() {
		
	}

}