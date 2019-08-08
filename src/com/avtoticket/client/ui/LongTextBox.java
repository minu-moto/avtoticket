/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.LongBox;

/**
 * Текстовое поле, предназначенное для ввода и отображения целых чисел. В случае
 * некорректного ввода подсвечивается стилем .ab-invalid-field.
 * 
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 24.11.2012 18:59:00
 */
public class LongTextBox extends LongBox {

//	private static final Logger logger = Logger.getLogger(LongTextBox.class.getName());

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

	private boolean highlighted = false;

	/**
	 * Фильтр нечисловых клавиш
	 * (М_ВводЧисел)
	 * 
	 * @param cc
	 *            - код нажатой клавиши
	 * @param ctrlKey
	 *            - нажата ли клавиша Ctrl
	 * @param shiftKey
	 *            - нажата ли клавиша Shift
	 * @param altKey
	 *            - нажата ли клавиша Alt
	 * @param metaKey
	 *            - нажата ли клавиша ???
	 * @return true - если код нажатой клавиши соответствует цифре, либо
	 *         допустимой управляющей клавише, иначе - false
	 */
	public static boolean filterNumbers(int cc, boolean ctrlKey,
			boolean shiftKey, boolean altKey, boolean metaKey) {
		// запрещаем комбинации с insert и delete
		// (ctrl+insert, shift+delete, shift+insert, итд)
		boolean spec_ins_del = (altKey || metaKey || shiftKey || ctrlKey)
				&& ((cc == KeyCodes.KEY_DELETE) || (cc == KeyCodes.KEY_INSERT));
		// запрещаем ctrl+v
		boolean ctrl_v = ctrlKey && (cc == KeyCodes.KEY_V);
		// запрещаем все комбинации с шифтом, кроме выделения текста
		boolean shift = shiftKey && (cc != KeyCodes.KEY_RIGHT)
				&& (cc != KeyCodes.KEY_LEFT) && (cc != KeyCodes.KEY_HOME)
				&& (cc != KeyCodes.KEY_END);
		// если не нажата ни одна спец.клавиша, то
		// запрещаем всё, кроме цифр и управляющих клавиш
		boolean chars = !altKey && !metaKey && !shiftKey && !ctrlKey
				&& (cc != KeyCodes.KEY_DELETE) && (cc != KeyCodes.KEY_BACKSPACE)
				&& (cc != KeyCodes.KEY_END) && (cc != KeyCodes.KEY_ENTER)
				&& (cc != KeyCodes.KEY_ESCAPE) && (cc != KeyCodes.KEY_HOME)
				&& (cc != KeyCodes.KEY_PAGEDOWN) && (cc != KeyCodes.KEY_PAGEUP)
				&& (cc != KeyCodes.KEY_TAB) && !KeyCodes.isArrowKey(cc)
				&& (cc != KeyCodes.KEY_INSERT) && ((cc == 0)
						|| ((cc >= 0x20) && (cc < KeyCodes.KEY_ZERO))
						|| ((cc > KeyCodes.KEY_NINE) && (cc < KeyCodes.KEY_NUM_ZERO))
						|| (cc > KeyCodes.KEY_NUM_NINE));
		return !(spec_ins_del || ctrl_v || shift || chars);
	}

	/**
	 * Новый экземпляр целочисленного поля
	 */
	public LongTextBox() {
		super();
		CSS.ensureInjected();
		addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (!filterNumbers(event.getNativeKeyCode(),
						event.isControlKeyDown(), event.isShiftKeyDown(),
						event.isAltKeyDown(), event.isMetaKeyDown()))
					event.preventDefault();
			}
		});
		addValueChangeHandler(new ValueChangeHandler<Long>() {
			@Override
			public void onValueChange(ValueChangeEvent<Long> event) {
				getValue();
			}
		});
		setStyleName("gwt-TextBox");
		addStyleName(CSS.atField());
	}

	@Override
	public Long getValue() {
		String text = getText().trim().replace('\u0020', '\u00A0');		// заменяем обычные пробелы на неразрывные
		setText(text);
		Long ret = super.getValue();
		if ((ret == null) && !text.isEmpty()) {
			highlighted = true;
			addStyleName(CSS.atInvalidField());
			setFocus(true);
			selectAll();
		} else if (highlighted) {
			removeStyleName(CSS.atInvalidField());
			highlighted = false;
		}
		return ret;
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