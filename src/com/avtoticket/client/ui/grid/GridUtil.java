/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid;

import java.util.List;

import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.client.ui.grid.fields.ListFieldBuilder;
import com.avtoticket.shared.models.BaseModel;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SetSelectionModel;

/**
 * Общая логика для {@link Grid} и {@link Grid2}. Унаследовать это добро не получается, так как они происходят от разных предков.
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 15.08.2013 17:54:15
 */
public final class GridUtil {

	public enum FieldTypes {
		TEXT, TEXTAREA, LINK, PASSWORD, LONG, FLOAT, CURRENCY, DATE, LIST, BOOLEAN, BUTTON, CUSTOM_FORMAT, ENUM, IMG
	}

	public static interface Style extends CssResource {
		String CSS_PATH = "body.css";

		String atEmpty();

		String atCheckboxSelectionHeader();
	}

	public static interface Resources extends ClientBundle {
		ImageResource progress();

		@Source(Style.CSS_PATH)
	    Style bodyStyle();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	private static final Style CSS = RESOURCES.bodyStyle();

	/**
	 * Запрещаем создание экземпляров данного класса
	 */
	private GridUtil() { }

	static <T extends BaseModel> T getSelectedRowValue(AbstractCellTable<T> grid) {
		int row = grid.getKeyboardSelectedRow();
		return ((row >= 0) && (row < grid.getVisibleItemCount())) ? grid.getVisibleItems().get(row) : null;
	}

	static <T extends BaseModel> void setKeyboardSelectionEnabled(AbstractCellTable<T> grid, boolean isEnabled) {
		grid.setKeyboardSelectionPolicy(isEnabled ? KeyboardSelectionPolicy.ENABLED : KeyboardSelectionPolicy.DISABLED);
		grid.setSkipRowHoverFloatElementCheck(!isEnabled);
		grid.setSkipRowHoverStyleUpdate(!isEnabled);
		grid.setSkipRowHoverCheck(!isEnabled);
	}

	static <T extends BaseModel> void setHeaderText(AbstractCellTable<T> grid, int idx, String text) {
		Header<?> header = grid.getHeader(idx);
		if (header instanceof MenuHeader) {
			((MenuHeader<?>) header).setValue(text);
			grid.redrawHeaders();
		}
	}

	@SuppressWarnings("unchecked")
	static <T extends BaseModel> void buildGrid(final AbstractCellTable<T> grid) {
		CSS.ensureInjected();
		grid.setPageSize(Integer.MAX_VALUE);
		grid.setLoadingIndicator(new Image(RESOURCES.progress()));
		grid.setHeaderBuilder(new CustomHeaderBuilder<T>((View<T>) grid));
		grid.setTableBuilder(new CustomTableBuilder<T>(grid));
		setKeyboardSelectionEnabled(grid, false);

		// создаём обработчик сортировки списка
		grid.addColumnSortHandler(new AsyncHandler(grid));

		// сообщение для пустого списка
		Label emptyLabel = new Label("<Список пуст>");
		emptyLabel.addStyleName(CSS.atEmpty());
		grid.setEmptyTableWidget(emptyLabel);
	}

	/**
	 * Создание колонки чекбоксов для выбора элементов
	 */
	public static <T extends BaseModel> void addCheckboxes(final AbstractCellTable<T> grid) {
		Column<T, Boolean> column = new Column<T, Boolean>(new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(T object) {
				return grid.getSelectionModel().isSelected(object);
			}
		};

		column.setVerticalAlignment(Column.ALIGN_MIDDLE);
		column.setHorizontalAlignment(Column.ALIGN_CENTER);

		column.setFieldUpdater(new FieldUpdater<T, Boolean>() {
			@Override
			public void update(int index, final T object, Boolean value) {
				grid.getSelectionModel().setSelected(object, value);
			}
		});

		Header<Boolean> header = null;
		if (grid.getSelectionModel() instanceof MultiSelectionModel) {
			header = new Header<Boolean>(new CheckboxCell(true, false)) {
				@Override
				public Boolean getValue() {
					int c = grid.getVisibleItemCount();
					Boolean ret = true;
					for (int i = 0; ret && (i < c); i++)
						ret &= grid.getSelectionModel().isSelected(grid.getVisibleItem(i));
					return ret;
				}
			};
			header.setUpdater(new ValueUpdater<Boolean>() {
				@Override
				public void update(Boolean value) {
					if (value)
						for (T item : grid.getVisibleItems())
							grid.getSelectionModel().setSelected(item, value);
					else {
						@SuppressWarnings("unchecked")
						SetSelectionModel<T> selectionModel = (SetSelectionModel<T>) grid.getSelectionModel();
						selectionModel.clear();
					}
				}
			});
			CSS.ensureInjected();
			header.setHeaderStyleNames(CSS.atCheckboxSelectionHeader());
		}

		grid.setColumnWidth(column, "26px");
		grid.addColumn(column, header);
	}

	@SuppressWarnings("unchecked")
	static <T extends BaseModel> void refresh(AbstractCellTable<T> grid, List<Field<?, T>> columns) {
		grid.setVisibleRangeAndClearData(grid.getVisibleRange(), true);
		for (Field<?, T> ci : columns)
			if ((ci instanceof ListFieldBuilder) && (((ListFieldBuilder<?, T>) ci).getListProvider() != null))
				((ListFieldBuilder<?, T>) ci).getListProvider().reload();
	}

	@SuppressWarnings("unchecked")
	static <T extends BaseModel> String getSortColumn(AbstractCellTable<T> grid, List<Field<?, T>> columns) {
		StringBuilder ret = new StringBuilder();
		ColumnSortList sortList = grid.getColumnSortList();
		for (int i = 0; i < sortList.size(); i++) {
			ColumnSortInfo sortInfo = sortList.get(i);
			int colIdx = grid.getColumnIndex((Column<T, ?>) sortInfo.getColumn());
			ret.append(", ").append(columns.get(colIdx).getModelKey()).append(sortInfo.isAscending() ? " asc" : " desc");
		}
		return (ret.length() <= 0) ? null : ret.substring(2);
	}

	public static <T extends BaseModel> void sortColumn(View<T> grid, Column<T, ?> column) {
		ColumnSortList sortList = grid.getColumnSortList();
        sortList.push(column);
        ColumnSortEvent.fire(grid, sortList);
	}

	public static <T extends BaseModel> void sortColumn(View<T> grid, Column<T, ?> column, boolean ascending) {
		ColumnSortList sortList = grid.getColumnSortList();
        sortList.push(new ColumnSortList.ColumnSortInfo(column, ascending));
        ColumnSortEvent.fire(grid, sortList);
	}

}