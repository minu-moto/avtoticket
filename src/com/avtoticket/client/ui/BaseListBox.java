/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle.MultiWordSuggestion;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 21.08.2014 14:30:40
 */
public class BaseListBox<T> extends SuggestBox implements HasData<T> {

//	private static final Logger logger = Logger.getLogger(BaseListBox.class.getName());

	@ImportedWithPrefix("listbox")
	public static interface Style extends CssResource {
		String CSS_PATH = "list.css";

		String atList();

		String atListSuggestions();
	}

	public static interface Resources extends ClientBundle {

		ImageResource arrow();

		@Source(Style.CSS_PATH)
	    Style listStyle();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	private static final Style CSS = RESOURCES.listStyle();

	private static class SuggestionList extends DefaultSuggestionDisplay {
		private Widget suggestionList;

		@Override
		protected Widget decorateSuggestionList(Widget suggestionList) {
			CSS.ensureInjected();
			this.suggestionList = suggestionList;
			suggestionList.addStyleName(CSS.atListSuggestions());
			return super.decorateSuggestionList(suggestionList);
		}

		@Override
		protected void showSuggestions(SuggestBox suggestBox, Collection<? extends Suggestion> suggestions,
				boolean isDisplayStringHTML, boolean isAutoSelectEnabled, SuggestionCallback callback) {
			super.showSuggestions(suggestBox, suggestions, isDisplayStringHTML, isAutoSelectEnabled, callback);
			suggestionList.getElement().getStyle().setProperty("minWidth", suggestBox.getOffsetWidth() - 8 + "px");
		}
	}

	private static final String WAITER = "Загрузка...";

	private final Map<String, T> valueToModel = new HashMap<String, T>();
	private final boolean allowNull;
	private int rowCount = 0;
	private final ProvidesValue<String, T> valueProvider;
	private final Renderer<T> renderer;

	private MultiWordSuggestOracle suggestOracle;
	private TextBox valueBox;

	public BaseListBox(boolean allowNull, final ProvidesValue<String, T> valueProvider, Renderer<T> renderer) {
		super(new MultiWordSuggestOracle(), new TextBox(), new SuggestionList());
		this.allowNull = allowNull;
		this.valueProvider = valueProvider;
		this.renderer = renderer;

		valueBox = (TextBox) getValueBox();
		suggestOracle = (MultiWordSuggestOracle) getSuggestOracle();

		setLimit(9999);
		addStyleName(CSS.atList());
		getElement().<InputElement> cast().setReadOnly(true);
		setAutoSelectEnabled(false);
		setText(WAITER);
		addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				//setValue(valueProvider.getValue(indexToModel.get(getSelectedIndex())), true);
			}
		});
		addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showSuggestionList();
				setFocus(true);
			}
		}, ClickEvent.getType());

		suggestOracle.setDefaultSuggestionsFromText(Arrays.asList(WAITER));
	}

	@Override
	public String getText() {
		// выбор пункта отправления должен работать как выпадающий список
		// поэтому скрываем от оракула текущий текст, чтоб он всегда выдавал все варианты выбора
		return "";
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		super.setValue(value, fireEvents);
		if (fireEvents && (getSelectedModel() != null))
			SelectionEvent.fire(this, new MultiWordSuggestion(value, SafeHtmlUtils.htmlEscape(value)));
	}

	public boolean isAllowNull() {
		return allowNull;
	}

	public T getSelectedModel() {
		return valueToModel.get(valueBox.getValue());
	}

	public void setAcceptableValues(List<? extends T> values) {
		if (WAITER.equals(valueBox.getValue()))
			setText(null);
		valueToModel.clear();
		suggestOracle.clear();

		List<String> vals = new ArrayList<String>();
		if (allowNull) {
			valueToModel.put("", null);
			vals.add("");
		}
		if (values != null)
			for (T option : values) {
				String val = valueProvider.getValue(option);
				if (valueToModel.containsKey(val))
					throw new IllegalArgumentException("Повторяющееся значение: " + val);
				vals.add(renderer.render(option));
				valueToModel.put(val, option);
			}

		suggestOracle.setDefaultSuggestionsFromText(vals);
	}

	@Override
	public HandlerRegistration addRangeChangeHandler(RangeChangeEvent.Handler handler) {
		return null;
	}

	@Override
	public HandlerRegistration addRowCountChangeHandler(RowCountChangeEvent.Handler handler) {
		return null;
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public Range getVisibleRange() {
		return new Range(0, getRowCount());
	}

	@Override
	public boolean isRowCountExact() {
		return true;
	}

	@Override
	public void setRowCount(int count) {
		setRowCount(count, true);
	}

	@Override
	public void setRowCount(int count, boolean isExact) {
		rowCount = count;
	}

	@Override
	public void setVisibleRange(int start, int length) {

	}

	@Override
	public void setVisibleRange(Range range) {

	}

	@Override
	public HandlerRegistration addCellPreviewHandler(CellPreviewEvent.Handler<T> handler) {
		return null;
	}

	@Override
	public SelectionModel<? super T> getSelectionModel() {
		return null;
	}

	@Override
	public T getVisibleItem(int indexOnPage) {
		return null;
	}

	@Override
	public int getVisibleItemCount() {
		return rowCount;
	}

	@Override
	public Iterable<T> getVisibleItems() {
		return valueToModel.values();
	}

	@Override
	public void setRowData(int start, List<? extends T> values) {
		setAcceptableValues(values);	// TODO не учитывается start
	}

	@Override
	public void setSelectionModel(SelectionModel<? super T> selectionModel) {

	}

	@Override
	public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {

	}

}