/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid.fields;

import com.avtoticket.client.ui.ProvidesValue;
import com.avtoticket.client.ui.grid.FieldValidator;
import com.avtoticket.client.ui.grid.Footer;
import com.avtoticket.client.ui.grid.GridUtil.FieldTypes;
import com.avtoticket.client.ui.grid.MenuHeader;
import com.avtoticket.client.ui.grid.View;
import com.avtoticket.client.ui.grid.cells.BooleanCell;
import com.avtoticket.client.ui.grid.cells.HighlightedEditCell;
import com.avtoticket.client.ui.grid.cells.LongEditCell;
import com.avtoticket.client.ui.grid.cells.TextAreaCell;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Where;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * @param <B> - тип билдера
 * @param <T> - тип поля
 * @param <C> - тип контекста (модели данных из которой берётся поле)
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 02.09.2012 0:57:19
 */
public class FieldGenericBuilder<B extends FieldGenericBuilder<B, T, C>, T, C extends BaseModel> extends Field<T, C> {

	private FieldTypes ftype = null;
	private String modelKey = null;
	private ProvidesValue<T, C> valueProvider = null;
	private String width = null;
	private String hint = null;
	private FieldValidator<C> validator = null;
	private HorizontalAlignmentConstant ha = null;
	private Boolean isEditable = false;
	private Boolean isSortable = false;
	private Boolean isSortByDesc = false;
	private Boolean isSortByDefault = false;
	private Boolean isShowInGrid = true;
	private Boolean isShowInEditor = false;
	private Boolean isRequire = false;
	private Boolean hasFilter = false;
	private Where filter = null;
	private Header<?> header = null;
	private Header<?> footer = null;
	private View<C> grid;
	private Column<C, T> column;

	/**
	 * Запрещаем создание экземпляров данного класса
	 */
	FieldGenericBuilder() { }

	@SuppressWarnings("unchecked")
	private B getThis() {
		return (B) this;
	}

	@Override
	public FieldTypes getType() {
		return ftype;
	}

	protected B type(FieldTypes ftype) {
		this.ftype = ftype;
		return getThis();
	}

	@Override
	public String getModelKey() {
		return modelKey;
	}

	public B modelKey(String modelKey) {
		this.modelKey = modelKey;
		return getThis();
	}

	@Override
	public ProvidesValue<T, C> getValueProvider() {
		return valueProvider;
	}

	public B valueProvider(ProvidesValue<T, C> valueProvider) {
		this.valueProvider = valueProvider;
		return getThis();
	}

	@Override
	public String getCaption() {
		Header<?> h = getHeader();
		return ((h != null) && (h.getValue() != null)) ? String.valueOf(h.getValue()) : null;
	}

	public B caption(String caption) {
		return header(new MenuHeader<C>(getThis(), caption));
	}

	@Override
	public String getWidth() {
		return width;
	}

	public B width(String width) {
		this.width = width;
		return getThis();
	}

	public B width(int width) {
		return width(width + "px");
	}

	@Override
	public String getHint() {
		return hint;
	}

	public B hint(String hint) {
		this.hint = hint;
		return getThis();
	}

	@Override
	public Boolean isEditable() {
		return isEditable;
	}

	public B editable() {
		isEditable = true;
		return getThis();
	}

	public B editable(boolean isEditable) {
		this.isEditable = isEditable;
		return getThis();
	}

	@Override
	public Boolean isSortable() {
		return isSortable;
	}

	public B sortable() {
		isSortable = true;
		return getThis();
	}

	@Override
	public Boolean isSortByDesc() {
		return isSortByDesc;
	}

	public B desc() {
		isSortByDesc = true;
		return getThis();
	}

	@Override
	public Boolean isSortByDefault() {
		return isSortByDefault;
	}

	public B sortByDefault() {
		isSortByDefault = true;
		return sortable();
	}

	@Override
	public Boolean isShowInGrid() {
		return isShowInGrid;
	}

	public B notShowInGrid() {
		isShowInGrid = false;
		return getThis();
	}

	@Override
	public Boolean isShowInEditor() {
		return isShowInEditor;
	}

	public B showInEditor() {
		isShowInEditor = true;
		return getThis();
	}

	@Override
	public Boolean isRequire() {
		return isRequire;
	}

	public B require() {
		return require(true);
	}

	public B require(boolean isRequire) {
		this.isRequire = isRequire;
		return getThis();
	}

	@Override
	public Boolean hasFilter() {
		return hasFilter;
	}

	public B filter() {
		hasFilter = true;
		return getThis();
	}

	@Override
	public Where getFilter() {
		return filter;
	}

	public B filter(Where where) {
		filter = where;
		return filter();
	}

	@Override
	public FieldValidator<C> getValidator() {
		return validator;
	}

	public B validator(FieldValidator<C> validator) {
		this.validator = validator;
		return getThis();
	}

	@Override
	public HorizontalAlignmentConstant getHorizontalAlignment() {
		return ha;
	}

	public B hAlign(HorizontalAlignmentConstant hAlign) {
		ha = hAlign;
		return getThis();
	}

	public B hAlignCenter() {
		return hAlign(HasHorizontalAlignment.ALIGN_CENTER);
	}

	public B hAlignRight() {
		return hAlign(HasHorizontalAlignment.ALIGN_RIGHT);
	}

	@Override
	public Header<?> getFooter() {
		return footer;
	}

	public B footer(Footer<?> footer) {
		this.footer = footer;
		return getThis();
	}

	@Override
	public Header<?> getHeader() {
		return header;
	}

	public B header(Header<?> header) {
		this.header = header;
		return getThis();
	}

	@Override
	public View<C> getGrid() {
		return grid;
	}

	protected B to(View<C> grid) {
		this.grid = grid;
		return getThis();
	}

	@Override
	public Column<C, T> getColumn() {
		return column;
	}

	protected void setColumn(Column<C, T> column) {
		this.column = column;
	}

	@SuppressWarnings("unchecked")
	protected Cell<T> createCell() {
		switch (getType()) {
		case LONG:
			return (Cell<T>) (isEditable() ? new LongEditCell(getGrid()) : new TextCell());
		case TEXTAREA:
			return (Cell<T>) (isEditable() ? new TextAreaCell() : new TextCell());
		case CUSTOM_FORMAT:
		case PASSWORD:
		case TEXT:
			return (Cell<T>) (isEditable() ? new HighlightedEditCell() : new TextCell());
		case BOOLEAN:
			return (Cell<T>) new BooleanCell(!isEditable());
		default:
			return null;
		}
	}

	protected Column<C, T> createColumn() {
		if (isShowInGrid())
			setColumn(new Column<C, T>(createCell()) {
				@Override
				public T getValue(C object) {
					return (getValueProvider() != null) ? getValueProvider().getValue(object) : null;
				}
			});
		return getColumn();
	}

	static boolean isNull(Object obj) {
		return (obj == null) || ((obj instanceof String) && ((String) obj).isEmpty());
	}

	static boolean isEquals(Object o1, Object o2) {
		boolean o1Null = isNull(o1);
		boolean o2Null = isNull(o2);
		return (o1Null && o2Null) || (!o1Null && o1.equals(o2))/* || (!o1Null && (o1 instanceof Double) && MathUtil.equalDouble((Double) o1, (Double) o2))*/;
	}

	@Override
	public void addToGrid() {
		addTo(getGrid());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addTo(final View<C> grid) {
		if (grid != null) {
			to(grid);
			Column<C, Object> column = (Column<C, Object>) createColumn();
			if (column != null) {
				column.setVerticalAlignment(Column.ALIGN_MIDDLE);
				if (getHorizontalAlignment() != null)
					column.setHorizontalAlignment(getHorizontalAlignment());

				if (isEditable())
					column.setFieldUpdater(new FieldUpdater<C, Object>() {
						@Override
						public void update(int index, final C object, Object value) {
							switch (getType()) {
							case LONG:
								if (value != null) {
									String str = value.toString();
									value = !str.isEmpty() ? Long.valueOf(str) : null;
								}
								break;
							case FLOAT:
								Double oldValue = object.getDoubleProp(getModelKey());
								NumberFormat floatFormat = NumberFormat.getFormat("0.#");
								if (value != null) {
									try {
										value = floatFormat.parse(value.toString());
									} catch (NumberFormatException e) {
										return;
									}
									if ((oldValue == null && value == null) || (oldValue != null && value != null) && floatFormat.format(oldValue).equals(floatFormat.format((Double) value)))
										return;
								}
								break;
							case CURRENCY:
								oldValue = object.getDoubleProp(getModelKey());
								NumberFormat currencyFormat = NumberFormat.getFormat("#,##0.00");
								if (value != null) {
									try {
										value = currencyFormat.parse(value.toString());
									} catch (NumberFormatException e) {
										return;
									}
									if ((oldValue == null && value == null) || (oldValue != null && value != null) && currencyFormat.format(oldValue).equals(currencyFormat.format((Double) value)))
										return;
								}
								break;
							default:
								break;
							}
							if (!isEquals(object.get(getModelKey()), value))
								grid.onUpdate(getThis(), object, value);
						};
					});

				column.setSortable(isSortable());
				column.setDefaultSortAscending(!isSortByDesc());
				grid.addField(getThis());
				if (isSortByDefault())
					grid.getColumnSortList().push(column);
			}
		}
	}

}