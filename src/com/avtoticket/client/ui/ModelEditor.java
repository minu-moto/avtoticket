/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.avtoticket.client.ui.grid.FieldValidator;
import com.avtoticket.client.ui.grid.GridUtil.FieldTypes;
import com.avtoticket.client.ui.grid.fields.EnumFieldBuilder;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.client.ui.grid.fields.ListFieldBuilder;
import com.avtoticket.shared.models.BaseModel;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.client.DoubleRenderer;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.ValueBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 02.09.2012 0:03:25
 */
public class ModelEditor<T extends BaseModel> extends FlexTable implements Editor<T> {

	public static interface Style extends CssResource {
		String CSS_PATH = "editor.css";

		String atEditor();

		String atEditorCells();

		String atEditorLabel();

		String atEditorField();

		String atEditorFieldCell();
	}

	public static interface FieldStyle extends CssResource {
		String CSS_PATH = "textBox.css";

		String atField();

		String atInvalidField();
	}

	public static interface Resources extends ClientBundle {
		@Source(Style.CSS_PATH)
	    Style editorStyle();

		@Source(FieldStyle.CSS_PATH)
		FieldStyle fieldStyle();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	public static final Style CSS = RESOURCES.editorStyle();
	private static final FieldStyle fieldCSS = RESOURCES.fieldStyle();

	protected List<Field<?, T>> fieldsInfo = new ArrayList<Field<?, T>>();
	private Map<Field<?, T>, HasValue<?>> fields = new HashMap<Field<?, T>, HasValue<?>>();
	private T model;
	protected KeyDownHandler onEnterPressed = new KeyDownHandler() {
		@Override
		public void onKeyDown(KeyDownEvent event) {
			int keyCode = event.getNativeKeyCode();
			if (keyCode == 13)
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						onEnterKeyDown();
					}
				});
			else if (keyCode == 27)
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						onEscKeyDown();
					}
				});
		}
	};

	public ModelEditor() {
		CSS.ensureInjected();
		fieldCSS.ensureInjected();
		addStyleName(CSS.atEditor());
		getColumnFormatter().addStyleName(0, CSS.atEditorLabel());
		getColumnFormatter().addStyleName(1, CSS.atEditorField());
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		for (Field<?, T> ci : fieldsInfo) {
			Object field = fields.get(ci);
			if (field instanceof FocusWidget)
				((FocusWidget) field).setEnabled(isEnabled && ci.isEditable());
			else
				((UIObject) field).getElement().setPropertyBoolean("disabled", !(isEnabled && ci.isEditable()));
		}
	}

	public void removeField(String modelKey) {
		int size = fieldsInfo.size();
		for (int idx = 0; idx < size; idx++)
			if (fieldsInfo.get(idx).getModelKey().equals(modelKey)) {
				removeRow(idx);
				fieldsInfo.remove(idx);
				fields.remove(fieldsInfo.get(idx));
				break;
			}
	}

	public void removeAllFields() {
		removeAllRows();
		fieldsInfo.clear();
		fields.clear();
	}

	@SuppressWarnings("unchecked")
	public void addField(final Field<?, T> ci) {
		if (ci.isShowInEditor()) {
			int size = fieldsInfo.size();
			setHTML(size, 0, ((ci.getCaption() != null) && !ci.getCaption().isEmpty()) ? (ci.getCaption() + ":") : "");
			switch (ci.getType()) {
			case TEXTAREA:
				TextArea ta = new TextArea();
				fields.put(ci, ta);
				setWidget(size, 1, ta);
				break;
			case TEXT:
				TextBox tb = new TextBox();
				fields.put(ci, tb);
				setWidget(size, 1, tb);
				break;
			case FLOAT:
				DoubleTextBox dtb = new DoubleTextBox();
				fields.put(ci, dtb);
				setWidget(size, 1, dtb);
				break;
			case CURRENCY:
				DoubleTextBox dtb1 = new DoubleTextBox(new DoubleRenderer() {
					private NumberFormat formatter = NumberFormat.getFormat("#,##0.00");
					@Override
					public String render(Double number) {
						return (number != null) ? formatter.format(number) : "";
					}
				});
				fields.put(ci, dtb1);
				setWidget(size, 1, dtb1);
				break;
			case LONG:
				LongTextBox ltb = new LongTextBox();
				fields.put(ci, ltb);
				setWidget(size, 1, ltb);
				break;
			case PASSWORD:
				PasswordTextBox ptb = new PasswordTextBox();
				fields.put(ci, ptb);
				setWidget(size, 1, ptb);
				break;
			case LIST:
				ListFieldBuilder<?, T> lfi = (ListFieldBuilder<?, T>) ci;
				@SuppressWarnings("rawtypes")
				ModelListBox lb = new ModelListBox(!ci.isRequire());
				if (lfi.getListProvider() != null)
					lfi.getListProvider().addDataDisplay(lb);
				fields.put(ci, lb);
				setWidget(size, 1, lb);
				break;
			case BOOLEAN:
				CheckBox cb = new CheckBox();
				fields.put(ci, cb);
				setWidget(size, 1, cb);
				break;
			case DATE:
				DateBox db = new DateTextBox();
				fields.put(ci, db);
				setWidget(size, 1, db);
				break;
			case ENUM:
				EnumFieldBuilder<?, T> efi = (EnumFieldBuilder<?, T>) ci;
				@SuppressWarnings("rawtypes")
				EnumListBox<?> en = new EnumListBox(!ci.isRequire(), efi.getEnumClass());
				fields.put(ci, en);
				setWidget(size, 1, en);
				break;
			case IMG:
			case LINK:
			case BUTTON:
			case CUSTOM_FORMAT:
				break;
			}
			if ((getCellCount(size) > 1) && (getWidget(size, 1) != null)) {
				final UIObject field = getWidget(size, 1);
				field.addStyleName(fieldCSS.atField());
				if ((ci.getGrid() == null) && (ci.getWidth() != null) && !ci.getWidth().isEmpty())
					field.setWidth(ci.getWidth());
				if (field instanceof FocusWidget) {
					((FocusWidget) field).setEnabled(ci.isEditable());
					if (!(field instanceof TextArea))
						((FocusWidget) field).addKeyDownHandler(onEnterPressed);
				} else
					field.getElement().setPropertyBoolean("disabled", !ci.isEditable());
				if (field instanceof HasValueChangeHandlers)
					((HasValueChangeHandlers<Object>) field).addValueChangeHandler(new ValueChangeHandler<Object>() {
						@Override
						public void onValueChange(ValueChangeEvent<Object> event) {
							refresh();
							ModelEditor.this.onValueChange((HasValue<?>) event.getSource(), ci.getModelKey());
						}
					});
			}
			CellFormatter cf = getCellFormatter();
			cf.setHorizontalAlignment(size, 0, HasHorizontalAlignment.ALIGN_RIGHT);
			cf.setHorizontalAlignment(size, 1, (ci.getType() != FieldTypes.BOOLEAN) ? HasHorizontalAlignment.ALIGN_LEFT : HasHorizontalAlignment.ALIGN_CENTER);
			cf.getElement(size, 1).setClassName(CSS.atEditorFieldCell());

			fieldsInfo.add(ci);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void edit(T model) {
		this.model = model;
		if (model == null)
			return;
		for (Field ci : fieldsInfo) {
			HasValue f = fields.get(ci);
			if (f != null)
				if (f instanceof BaseListBox)
					f.setValue(String.valueOf(model.get(ci.getModelKey())));
				else
					f.setValue(model.get(ci.getModelKey()));
		}
		validate(true);
	}

	/**
	 * Валидация введённых значений
	 *
	 * @param clear - если флаг установлен в true, то валидация не производится и все поля считаются корректными, по сути происходит очистка полей от ошибок при предыдущих валидациях
	 * @return true - если все значения на форме корректны, иначе - false
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
		    value = "DM_CONVERT_CASE", 
		    justification = "No GWT emulation of String#toLowerCase(Locale)")
	private boolean validate(boolean clear) {
		boolean ret = true;
		StringBuilder sb = new StringBuilder();
		for (Field<?, T> ci : fieldsInfo) {
			boolean isValid = true;
			@SuppressWarnings("rawtypes")
			HasValue f = fields.get(ci);
			if (f == null) {
				isValid = false;
				sb.append("Поле '").append(ci.getCaption()).append("' не найдено!\n");
			} else {
				UIObject uio = (UIObject) f;
				boolean enabled = !uio.getElement().getPropertyBoolean("disabled");
				if (!clear) {
					FieldValidator<T> fv = ci.getValidator();
					if ((fv != null) && !fv.isValidValue(f.getValue(), fields, sb)) {
						isValid = false;
						sb.append("\n");
					}
					if ((f instanceof ValueBox) && (f.getValue() == null) && (((ValueBox<?>) f).getText() != null) && !((ValueBox<?>) f).getText().isEmpty()) {
						isValid = false;
						sb.append("Не правильно задано значение поля '").append(ci.getCaption()).append("'!\n");
					} else if ((f instanceof DateBox) && (f.getValue() == null) && ((Boolean) ((Widget) f).getLayoutData() == Boolean.TRUE)) {
						isValid = false;
						sb.append("Поле '").append(ci.getCaption()).append("' должно содержать дату в формате dd.mm.yyyy\n");
					} else if (ci.isRequire() && enabled && ((f.getValue() == null) || f.getValue().toString().isEmpty())) {
						isValid = false;
						sb.append("Поле '").append(ci.getCaption()).append("' не может быть пустым!\n");
					}
				}
				uio.setStyleName(fieldCSS.atInvalidField(), !isValid);
				if (ret && !isValid)
					if (f instanceof FocusWidget)
						((FocusWidget) f).setFocus(true);
					else if (f instanceof DateBox)
						((DateBox) f).setFocus(true);
			}
			ret &= isValid;
		}
		onError(ret ? null : sb.toString());
		return ret;
	}

	/**
	 * Заполнение редактируемой модели значениями с формы, либо вывод сообщения об ошибке ввода
	 *
	 * @return true - если все значения корректны и успешно записаны в модель, иначе - false
	 */
	@Override
	public boolean flush() {
		if (validate(false)) {
			for (Field<?, T> ci : fieldsInfo)
				model.set(ci.getModelKey(), fields.get(ci).getValue());
			return true;
		} else
			return false;
	}

	public void refresh() {
		for (Field<?, T> ci : fieldsInfo) {
			model.set(ci.getModelKey(), fields.get(ci).getValue());
			if (ci instanceof ListFieldBuilder) {
				@SuppressWarnings("unchecked")
				ListFieldBuilder<?, T> lfi = (ListFieldBuilder<?, T>) ci;
				if (lfi.getListProvider() != null)
					lfi.getListProvider().refresh();
			}
		}
	}

	@Override
	public T getModel() {
		return model;
	}

	protected Map<Field<?, T>, HasValue<?>> getFields() {
		return fields;
	}

	public HasValue<?> getField(String key) {
		for (Field<?, T> ci : fieldsInfo)
			if (ci.getModelKey().equals(key))
				return fields.get(ci);
		return null;
	}

	protected void onError(String message) { }

	protected void onValueChange(HasValue<?> source, String fieldName) {}

	protected void onEnterKeyDown() {}

	protected void onEscKeyDown() { }

}