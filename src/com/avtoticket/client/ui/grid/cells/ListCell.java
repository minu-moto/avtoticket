/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui.grid.cells;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.avtoticket.client.ui.ProvidesValue;
import com.avtoticket.client.ui.grid.ListFilter;
import com.avtoticket.client.ui.grid.View;
import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.SelectionModel;

/**
 * @param <K> - тип уникального ключа, для доступа к записям
 * @param <T> - класс объектов, представляющих записи в списке
 * @param <C> - класс объектов контекста, представляющих записи в таблице, которой принадлежит данная ячейка
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 04.09.2012 15:38:06
 */
public class ListCell<K, T, C extends BaseModel> extends AbstractInputCell<K, String> implements HasData<T> {

	interface Template extends SafeHtmlTemplates {
		@Template("<select id=\"{0}\" tabindex=\"-1\" style=\"height: 18px; width: 100%; font-size: 12px; background: inherit; border: none;\">")
		SafeHtml list(String id);

		@Template("<option value=\"{0}\">{1}</option>")
		SafeHtml deselected(String value, String display);

		@Template("<option value=\"{0}\" selected=\"selected\">{1}</option>")
		SafeHtml selected(String value, String display);
	}

	public interface ProvidesKey<T> {
		String getKey(T item);
	}

	private final static Template template = GWT.create(Template.class);

	private boolean editable;
	private boolean loading = true;
	private boolean allowNull;
	private int rowCount = 0;
	private Map<Integer, Map<String, T>> keyToModel = new HashMap<Integer, Map<String, T>>();
	private List<? extends T> options;
	private View<C> grid;
	private ListFilter<T, C> filter = null;
	private final ProvidesKey<T> keyProvider;
	private final ProvidesValue<K, T> valueProvider;
	private final Renderer<T> renderer;

	/**
	 * @param grid - родительский объект ячейки
	 * @param editable - <code>true</code>, если разрешено изменение значения ячейки, иначе - в ячейке будет
	 * 						отрисован статичный текст без возможности выбора
	 * @param allowNull - <code>true</code>, если ячейка с выпадающим списком позволяет выбирать пустые значения
	 * @param keyProvider - источник ключей для записей. Ключи будут записаны в атрибут <code>value</code> элементов
	 * 						выпадающего списка, и должны быть уникальны в пределах одной ячейки с учётом фильтра.
	 * @param valueProvider - источник значений для записей. Значение ячейки устанавливается родительским объектом ячейки.
	 * 						Если ячейка изменилась, то новое значение передаётся в valueUpdater, если он задан.
	 * 						В простейшем случае {@link #valueProvider} выдаёт те же значения, что и {@link #keyProvider}.
	 * @param renderer - визуализатор записей. Генерирует текстовое представление данных, понятное пользователю.
	 */
	public ListCell(View<C> grid, boolean editable, boolean allowNull, ProvidesKey<T> keyProvider, ProvidesValue<K, T> valueProvider, Renderer<T> renderer, ListFilter<T, C> filter) {
		super(BrowserEvents.CHANGE);
		this.renderer = renderer;
		this.valueProvider = valueProvider;
		this.keyProvider = keyProvider;
		this.allowNull = allowNull;
		this.editable = editable;
		this.grid = grid;
		this.filter = filter;
	}

	public ListCell(View<C> grid, boolean editable, boolean allowNull) {
		this(grid, editable, allowNull, new ProvidesKey<T>() {
			@Override
			public String getKey(T item) {
				return (item != null) ? String.valueOf(((BaseModel) item).getValueField()) : null;
			}
		}, new ProvidesValue<K, T>() {
			@SuppressWarnings("unchecked")
			@Override
			public K getValue(T item) {
				return (item != null) ? (K) ((BaseModel) item).getValueField() : null;
			}
		}, new Renderer<T>() {
			@Override
			public String render(T object) {
				return (object == null) ? "" : ((BaseModel) object).getDisplayField();
			}

			@Override
			public void render(T object, Appendable appendable) throws IOException {
				appendable.append(render(object));
			}
		}, null);
	}

	public ListCell(View<C> grid, boolean editable, boolean allowNull, ListFilter<T, C> filter) {
		this(grid, editable, allowNull);
		setFilter(filter);
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, K value,
			NativeEvent event, ValueUpdater<K> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		String type = event.getType();
		if ("change".equals(type)) {
			Object key = context.getKey();
			SelectElement select = getInputElement(parent).cast();
			T newValue = getSelectedModel(context.getIndex(), select.getValue());
			setViewData(key, renderer.render(newValue));
			K val = valueProvider.getValue(newValue);
			finishEditing(parent, val, key, valueUpdater);
			if (valueUpdater != null)
				valueUpdater.update(val);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void render(Context context, K value, SafeHtmlBuilder sb) {
		if (loading)
			if (editable) {
				sb.append(template.list(""));
				sb.append(template.selected("0", "Загрузка..."));
				sb.appendHtmlConstant("</select>");
			} else
				sb.append(SafeHtmlUtils.fromString("Загрузка..."));
		else {
			keyToModel.remove(context.getIndex());
			Map<String, T> k2m = new HashMap<String, T>();
			keyToModel.put(context.getIndex(), k2m);

			final String id = DOM.createUniqueId();
			BaseModel contextModel = grid.getVisibleItem(context.getIndex() - grid.getVisibleRange().getStart());
			if (editable)
				sb.append(template.list(id));
			if (allowNull) {
				k2m.put("", null);
				if (editable)
					sb.append(template.deselected("", ""));
			}
			if (options != null) {
				boolean selected = false;
				for (T option : options) {
					String key = keyProvider.getKey(option);
					if ((filter == null) || ((ListFilter<T, BaseModel>) filter).accept(option, contextModel)) {
						if (k2m.containsKey(key))
							throw new IllegalArgumentException("Повторяющееся значение: " + key);
						k2m.put(key, option);
						if (Objects.equals(valueProvider.getValue(option), value)) {
							if (editable)
								sb.append(template.selected(key, renderer.render(option)));
							else
								sb.append(SafeHtmlUtils.fromString(renderer.render(option)));
							selected = true;
						} else if (editable)
							sb.append(template.deselected(key, renderer.render(option)));
					}
				}
				if (!selected)
					// костыль для выпадающего списка: во всех браузерах кроме файрфокса
					// список без выбранного элемента после атача автоматически сбрасывается на первый элемент
					Scheduler.get().scheduleFinally(new ScheduledCommand() {
						@Override
						public void execute() {
							Element select = DOM.getElementById(id);
							if (select != null)
								select.<SelectElement> cast().setSelectedIndex(-1);
						}
					});
			}
			if (editable)
				sb.appendHtmlConstant("</select>");
		}
	}

	@Override
	public boolean isEditing(Context context, Element parent, K value) {
		return false;
	}

	private T getSelectedModel(int idx, String key) {
		Map<String, T> k2m = keyToModel.get(idx);
		return (k2m != null) ? k2m.get(key) : null;
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
	public void fireEvent(GwtEvent<?> event) {

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
		return null;
	}

	@Override
	public void setRowData(int start, List<? extends T> values) {
		options = values;
		loading = (values.size() == 1) && ("Загрузка...".equalsIgnoreCase(renderer.render(values.get(0))));
		if (grid != null)
			grid.redraw();
	}

	@Override
	public void setSelectionModel(SelectionModel<? super T> selectionModel) {

	}

	@Override
	public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {

	}

	public void setView(View<C> grid) {
		this.grid = grid;
	}

	public void setFilter(ListFilter<T, C> filter) {
		this.filter = filter;
	}

}