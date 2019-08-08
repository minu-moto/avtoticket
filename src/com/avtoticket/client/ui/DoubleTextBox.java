/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui;

import java.text.ParseException;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBox;

/**
 * Текстовое поле, предназначенное для ввода и отображения вещественных чисел
 * в стандартном формате приложения "#,##0.#". В случае некорректного ввода
 * подсвечивается стилем .ab-invalid-field.
 *
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 19.11.2012 13:54:20
 */
public class DoubleTextBox extends ValueBox<Double> {

	public static interface Style extends CssResource {
		String CSS_PATH = "textBox.css";

		String atField();

		String atInvalidField();
	}

	public static interface Resources extends ClientBundle {
		@Source(Style.CSS_PATH)
	    Style fieldStyle();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	private static final Style CSS = RESOURCES.fieldStyle();
	private static NumberFormat formatter = NumberFormat.getFormat("0.#");

	private boolean highlighted = false;
	private Double value = null;
	private Renderer<Double> renderer;

	/**
	 * Новый экземпляр вещественного поля.
	 */
	public DoubleTextBox() {
		this(new AbstractRenderer<Double>() {
			@Override
			public String render(Double number) {
				return (number != null) ? formatter.format(number) : "";
			}
		});
	}

	public DoubleTextBox(Renderer<Double> dr) {
		super(Document.get().createTextInputElement(), dr, new Parser<Double>() {
			@Override
			public Double parse(CharSequence text) throws ParseException {
				try {
					return formatter.parse(String.valueOf(text));
				} catch (NumberFormatException e) {
					return null;
				}
			}
		});
		CSS.ensureInjected();
		renderer = dr;
		addValueChangeHandler(new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> event) {
				getValue();
			}
		});
		setStyleName("gwt-TextBox");
		addStyleName(CSS.atField());
	}

	@Override
	public Double getValue() {
		String text = getText().trim().replace('\u0020', '\u00A0');		// заменяем обычные пробелы на неразрывные
		setText(text);
		Double ret = super.getValue();
		if ((ret == null) && !text.isEmpty()) {
			highlighted = true;
			addStyleName(CSS.atInvalidField());
			setFocus(true);
			selectAll();
		} else {
			if (highlighted) {
				removeStyleName(CSS.atInvalidField());
				highlighted = false;
			}
			// если значение изменилось, то запоминаем новое, иначе выдаём на выход старое запомненное значение
			// (оно может иметь большую точность, чем то что пользователь видит на экране)
			if (!renderer.render(value).equals(text))
				value = ret;
			else
				ret = value;
		}
		return ret;
	}

	@Override
	public void setValue(Double value, boolean fireEvents) {
		super.setValue(value, fireEvents);
		this.value = value;
	}

	/**
	 * Очистка поля
	 */
	public void clear() {
		setText("");
		if (highlighted) {
			removeStyleName(CSS.atInvalidField());
			highlighted = false;
		}
	}

}