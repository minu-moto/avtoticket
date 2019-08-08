/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui.grid;

import java.util.ArrayList;
import java.util.List;

import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.client.ui.grid.filters.Filter;
import com.avtoticket.client.ui.grid.filters.FilterChangeEvent;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Where;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 09.06.2012 10:42:13
 */
public class Grid<T extends BaseModel> extends CellTable<T> implements View<T> {

	public static interface Style extends CellTable.Style {
		String CSS_PATH = "table.css";

		String atEditCell();
	}

	public static interface Resources extends CellTable.Resources {
		@Override
	    @Source(Style.CSS_PATH)
	    Style cellTableStyle();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	public static final Style CSS = RESOURCES.cellTableStyle();

	private List<Field<?, T>> columns = new ArrayList<Field<?, T>>();
	private List<Filter> filters = new ArrayList<Filter>();

	public Grid() {
		super();
		buildGrid();
	}

	public Grid(ProvidesKey<T> mkp, boolean checkboxes) {
		super(20, RESOURCES, mkp);
		buildGrid();

		if (checkboxes) {
			buildSelectionModel();
			columns.add(null);
			addCheckboxes();
		}
	}

	public T getSelectedRowValue() {
		return GridUtil.getSelectedRowValue(this);
	}

	public void setKeyboardSelectionEnabled(boolean isEnabled) {
		GridUtil.setKeyboardSelectionEnabled(this, isEnabled);
	}

	public void setHeaderText(int idx, String text) {
		GridUtil.setHeaderText(this, idx, text);
	}

	protected void buildSelectionModel() {
		setSelectionModel(new MultiSelectionModel<T>(getKeyProvider()), DefaultSelectionEventManager.<T> createCheckboxManager(columns.size()));
	}

	protected void buildGrid() {
		GridUtil.buildGrid(this);
	}

	protected void addCheckboxes() {
		GridUtil.addCheckboxes(this);
	}

	@Override
	public void addField(Field<?, T> ci) {
		columns.add(ci);
		addColumn(ci.getColumn(), ci.getHeader(), ci.getFooter());
		setColumnWidth(ci.getColumn(), ci.getWidth());
	}

	@Override
	public Field<?, T> getField(int col) {
		return columns.get(col);
	}

	@Override
	public Field<?, T> getField(String name) {
		if (name == null)
			return null;
		for (Field<?, T> column : columns)
			if ((column != null) && name.equalsIgnoreCase(column.getModelKey()))
				return column;
		return null;
	}

	@Override
	public List<Field<?, T>> getFields() {
		return columns;
	}

	@Override
	public void refresh() {
		GridUtil.refresh(this, columns);
	}

	@Override
	public String getSortColumn() {
		return GridUtil.getSortColumn(this, columns);
	}

	@Override
	public void onUpdate(Field<?, T> ci, T object, final Object value) {}

	@Override
	public void clearFilter() {
		clearFilter(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clearFilter(boolean fireEvent) {
		for (Field<?, T> fld : columns)
			if ((fld != null) && fld.hasFilter() && (fld.getFilter() != null) && (fld.getHeader() != null) && (fld.getHeader() instanceof MenuHeader))
				((MenuHeader<T>) fld.getHeader()).clearFilter();
		for (Filter flt : filters)
			flt.clearFilter(false);
		if (fireEvent)
			FilterChangeEvent.fire(this);
	}

	@Override
	public void addFilter(Filter filter) {
		filters.add(filter);
		filter.addFilterChangeHandler(new FilterChangeEvent.Handler() {
			@Override
			public void onFilterChange(final FilterChangeEvent event) {
				// реагируем не сразу, даём отдуплиться компоненту-источнику события
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						FilterChangeEvent.fire(Grid.this, event.getEventSource());
					}
				});
			}
		});
	}

	@Override
	public HandlerRegistration addFilterChangeHandler(FilterChangeEvent.Handler handler) {
		return addHandler(handler, FilterChangeEvent.getType());
	}

	@Override
	public Where getFilter() {
		Where ret = new Where();
		for (Field<?, T> fld : columns)
			if ((fld != null) && (fld.getFilter() != null))
				ret.andWhere(fld.getFilter());
		for (Filter flt : filters) {
			Where fltWhere;
			if ((flt != null) && ((fltWhere = flt.getFilter()) != null))
				ret.andWhere(fltWhere);
		}
		return ret;
	}

	@Override
	public void setVisible(int col, boolean visible) {
		if (visible)
			removeColumnStyleName(col, "atHidden");
		else
			addColumnStyleName(col, "atHidden");
		Header<?> header = getHeader(col);
		if (header != null)
			header.setHeaderStyleNames(visible ? "" : "atHidden");		// TODO возможно затирание уже установленных стилей
		Column<T, ?> column = getColumn(col);
		if (column != null)
			column.setCellStyleNames(visible ? "" : "atHidden");
		header = getFooter(col);
		if (header != null)
			header.setHeaderStyleNames(visible ? "" : "atHidden");
	}

}