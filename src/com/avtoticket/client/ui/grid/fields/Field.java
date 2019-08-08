/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid.fields;

import java.util.Date;

import com.avtoticket.client.ui.ModelListDataProvider;
import com.avtoticket.client.ui.ProvidesValue;
import com.avtoticket.client.ui.grid.FieldValidator;
import com.avtoticket.client.ui.grid.GridUtil.FieldTypes;
import com.avtoticket.client.ui.grid.View;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Where;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * @param <T> - тип поля
 * @param <C> - тип контекста (модели данных из которой берётся поле)
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 05.10.2013 15:16:03
 */
public abstract class Field<T, C extends BaseModel> {

	public abstract FieldTypes getType();

	public abstract String getModelKey();

	public abstract ProvidesValue<T, C> getValueProvider();

	public abstract String getCaption();

	public abstract String getWidth();

	public abstract String getHint();

	public abstract Boolean isEditable();

	public abstract Boolean isSortable();

	public abstract Boolean isSortByDesc();

	public abstract Boolean isSortByDefault();

	public abstract Boolean isShowInGrid();

	public abstract Boolean isShowInEditor();

	public abstract Boolean isRequire();

	public abstract Boolean hasFilter();

	public abstract Where getFilter();

	public abstract FieldValidator<C> getValidator();

	public abstract HorizontalAlignmentConstant getHorizontalAlignment();

	public abstract Header<?> getFooter();

	public abstract Header<?> getHeader();

	public abstract Column<C, ?> getColumn();

	public abstract View<C> getGrid();

	public abstract void addTo(View<C> grid);

	public abstract void addToGrid();

	public static <C extends BaseModel> FieldBuilder<String, C> asTextTo(View<C> grid) {
		final FieldBuilder<String, C> ret = new FieldBuilder<String, C>().type(FieldTypes.TEXT).to(grid);
		return ret.valueProvider(new ProvidesValue<String, C>() {
			@Override
			public String getValue(C item) {
				return (item != null) ? item.getStringProp(ret.getModelKey()) : null;
			}
		});
	}

	public static <C extends BaseModel> FieldBuilder<String, C> asTextAreaTo(View<C> grid) {
		final FieldBuilder<String, C> ret = new FieldBuilder<String, C>().type(FieldTypes.TEXTAREA).to(grid);
		return ret.valueProvider(new ProvidesValue<String, C>() {
			@Override
			public String getValue(C item) {
				return (item != null) ? item.getStringProp(ret.getModelKey()) : null;
			}
		});
	}

	public static <C extends BaseModel> FieldBuilder<String, C> asPasswordTo(View<C> grid) {
		final FieldBuilder<String, C> ret = new FieldBuilder<String, C>().type(FieldTypes.PASSWORD).to(grid);
		return ret.valueProvider(new ProvidesValue<String, C>() {
			@Override
			public String getValue(C item) {
				return (item != null) ? item.getStringProp(ret.getModelKey()) : null;
			}
		});
	}

	public static <C extends BaseModel> FieldBuilder<String, C> asLongTo(View<C> grid) {
		final FieldBuilder<String, C> ret = new FieldBuilder<String, C>().type(FieldTypes.LONG).to(grid);
		NumberFormat formatter = NumberFormat.getFormat("0.#");
		return ret.valueProvider(new ProvidesValue<String, C>() {
			@Override
			public String getValue(C item) {
				Double d = (item != null) ? item.getDoubleProp(ret.getModelKey()) : null;
				return (d != null) ? formatter.format(d) : "";
			}
		});
	}

	public static <C extends BaseModel> DateFieldBuilder<C> asDateTo(View<C> grid) {
		final DateFieldBuilder<C> ret = new DateFieldBuilder<C>().type(FieldTypes.DATE).to(grid).desc().format("dd.MM.yyyy");
		return ret.valueProvider(new ProvidesValue<Date, C>() {
			@Override
			public Date getValue(C item) {
				return (item != null) ? item.getDateProp(ret.getModelKey()) : null;
			}
		});
	}

	public static <C extends BaseModel> NumberFormatFieldBuilder<C> asFloatTo(View<C> grid) {
		final NumberFormatFieldBuilder<C> ret = new NumberFormatFieldBuilder<C>().type(FieldTypes.FLOAT).to(grid).hAlign(HasHorizontalAlignment.ALIGN_RIGHT).format("0.#");
		return ret.valueProvider(new ProvidesValue<String, C>() {
			@Override
			public String getValue(C item) {
				if (ret.getFormatter() != null)
					return ret.getFormatter().format(item);
				else {
					Object obj = (item != null) ? item.get(ret.getModelKey()) : null;
					return (obj != null) ? obj.toString() : "";
				}
			}
		});
	}

	public static <C extends BaseModel> NumberFormatFieldBuilder<C> asCurrencyTo(View<C> grid) {
		final NumberFormatFieldBuilder<C> ret = new NumberFormatFieldBuilder<C>().type(FieldTypes.CURRENCY).to(grid).hAlign(HasHorizontalAlignment.ALIGN_RIGHT).format("#,##0.00");
		return ret.valueProvider(new ProvidesValue<String, C>() {
			@Override
			public String getValue(C item) {
				if (ret.getFormatter() != null)
					return ret.getFormatter().format(item);
				else {
					Object obj = (item != null) ? item.get(ret.getModelKey()) : null;
					return (obj != null) ? obj.toString() : "";
				}
			}
		});
	}

	public static <C extends BaseModel> FieldBuilder<Boolean, C> asBooleanTo(View<C> grid) {
		final FieldBuilder<Boolean, C> ret = new FieldBuilder<Boolean, C>().type(FieldTypes.BOOLEAN).to(grid).hAlign(HasHorizontalAlignment.ALIGN_CENTER);
		return ret.valueProvider(new ProvidesValue<Boolean, C>() {
			@Override
			public Boolean getValue(C item) {
				return (item != null) ? item.getBooleanProp(ret.getModelKey()) : null;
			}
		});
	}

	public static <C extends BaseModel> ButtonFieldBuilder<C> asButtonTo(View<C> grid) {
		return new ButtonFieldBuilder<C>().type(FieldTypes.BUTTON).to(grid).hAlign(HasHorizontalAlignment.ALIGN_CENTER).valueProvider(new ProvidesValue<C, C>() {
			@Override
			public C getValue(C item) {
				return item;
			}
		});
	}

	public static <C extends BaseModel> LinkFieldBuilder<C> asLinkTo(View<C> grid) {
		return new LinkFieldBuilder<C>().type(FieldTypes.LINK).to(grid).valueProvider(new ProvidesValue<C, C>() {
			@Override
			public C getValue(C item) {
				return item;
			}
		});
	}

	public static <C extends BaseModel> CustomFormatFieldBuilder<String, C> asCustomFormatTo(View<C> grid) {
		final CustomFormatFieldBuilder<String, C> ret = new CustomFormatFieldBuilder<String, C>().type(FieldTypes.CUSTOM_FORMAT).to(grid);
		return ret.valueProvider(new ProvidesValue<String, C>() {
			@Override
			public String getValue(C item) {
				if (ret.getFormatter() != null)
					return ret.getFormatter().format(item);
				else {
					Object obj = (item != null) ? item.get(ret.getModelKey()) : null;
					return (obj != null) ? obj.toString() : "";
				}
			}
		});
	}

	public static <C extends BaseModel> NumberFormatFieldBuilder<C> asNumberFormatTo(View<C> grid) {
		final NumberFormatFieldBuilder<C> ret = new NumberFormatFieldBuilder<C>().type(FieldTypes.CUSTOM_FORMAT).to(grid);
		return ret.valueProvider(new ProvidesValue<String, C>() {
			@Override
			public String getValue(C item) {
				if (ret.getFormatter() != null)
					return ret.getFormatter().format(item);
				else {
					Object obj = (item != null) ? item.get(ret.getModelKey()) : null;
					return (obj != null) ? obj.toString() : "";
				}
			}
		});
	}

	public static <T extends BaseModel, C extends BaseModel> ListFieldBuilder<T, C> asListTo(View<C> grid, Class<T> clazz) {
		return Field.<T, C> asListTo(grid).provider(new ModelListDataProvider<T>(clazz));
	}

	public static <T extends BaseModel, C extends BaseModel> ListFieldBuilder<T, C> asListTo(View<C> grid) {
		final ListFieldBuilder<T, C> ret = new ListFieldBuilder<T, C>().type(FieldTypes.LIST).to(grid);
		return ret.valueProvider(new ProvidesValue<Long, C>() {
			@Override
			public Long getValue(C item) {
				return (item != null) ? item.getLongProp(ret.getModelKey()) : null;
			}
		});
	}

	public static <E extends Enum<E>, C extends BaseModel> EnumFieldBuilder<E, C> asEnumTo(View<C> grid, Class<E> clazz) {
		return Field.<E, C> asEnumTo(grid).enumClass(clazz);
	}

	public static <E extends Enum<E>, C extends BaseModel> EnumFieldBuilder<E, C> asEnumTo(View<C> grid) {
		final EnumFieldBuilder<E, C> ret = new EnumFieldBuilder<E, C>().type(FieldTypes.ENUM).to(grid);
		return ret.valueProvider(new ProvidesValue<E, C>() {
			@Override
			public E getValue(C item) {
				return (item != null) ? item.<E> getEnumProp(ret.getModelKey()) : null;
			}
		});
	}

	public static <T, C extends BaseModel> ImageFieldBuilder<T, C> asImgTo(View<C> grid) {
		return new ImageFieldBuilder<T, C>().type(FieldTypes.IMG).to(grid);
	}

}