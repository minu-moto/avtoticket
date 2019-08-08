/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid;

import com.avtoticket.client.ui.DateTextBox;
import com.avtoticket.client.ui.ModelEditor;
import com.avtoticket.client.ui.grid.GridUtil.FieldTypes;
import com.avtoticket.client.ui.grid.fields.EnumFieldBuilder;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.client.ui.grid.fields.ListFieldBuilder;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.WhereExpression;
import com.avtoticket.shared.models.WhereExpression.Condition;
import com.avtoticket.shared.models.WhereExpression.Operation;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.UIObject;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 29.10.2013 18:29:41
 */
public class FilterEditor extends ModelEditor<BaseModel> {

	private enum Oper {
		AND("и"),
		OR("или");

		private String text;

		private Oper(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	private enum Cond {
		EQUALS("="),
		GREATER(">"),
		LESS("<"),
		NOT_EQUALS("<>"),
		NOT_LESS(">="),
		NOT_GREATER("<="),
		EMPTY("пустое"),
		NOT_EMPTY("не пустое");

		private String text;

		private Cond(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	private enum TextCond {
		LIKE("с любой частью поля"),
		NOT_LIKE("не содержит"),
		EQUALS("поле целиком"),
		STARTS("начинается с"),
		ENDS("заканчивается на"),
		EMPTY("пустое поле"),
		NOT_EMPTY("не пустое поле");

		private String text;

		private TextCond(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	private enum ListCond {
		EQUALS("равно"),
		NOT_EQUALS("не равно");

		private String text;

		private ListCond(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	private BaseModel filterModel;
	private Field<?, ?> info;
	private ColumnMenu<?> cm;

	/**
	 * Разделение контекста фильтров. У панели фильтра другой контекст, по сравнению с записями в таблице.
	 * На данный момент контекст панели фильтра не используется и равен <code>null</code>.
	 * 
	 * @param filter - фильтр списков таблицы
	 * @return фильтр списков панели фильтров
	 */
	private <T, C extends BaseModel> ListFilter<T, BaseModel> separateContext(final ListFilter<T, C> filter) {
		return new ListFilter<T, BaseModel>() {
			@Override
			public boolean accept(T option, BaseModel context) {
				return (filter == null) || filter.accept(option, null);
			}
		};
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public FilterEditor(Field<?, ?> info, ColumnMenu<?> cm) {
		this.cm = cm;
		this.info = info;
		filterModel = new BaseModel();
		addStyleName(MenuHeaderCell.CSS.atFilterEditor());
		String key;
		int idx = 1;
		if (info.getFilter() != null)
			// когда фильтр задан изначально - парсим его и строим на его основе форму
			for (WhereExpression<?> expr : info.getFilter().getExpression()) {
				if (!info.getModelKey().equals(expr.getFieldName()))
					throw new IllegalArgumentException("Filter on field '" + info.getModelKey() + "' contains expression with another field '" + expr.getFieldName() + "'");
				if (expr.getSubExpression() != null)
					throw new IllegalArgumentException("Filter on field '" + info.getModelKey() + "' contains subexpression");
				switch (info.getType()) {
				case TEXT:
				case TEXTAREA:
					if ((expr.getValue() == null) || !(expr.getValue() instanceof String))
						throw new IllegalArgumentException("Filter on field '" + info.getModelKey() + "' contains not suitable string value '" + String.valueOf(expr.getValue()) + "'");
					if ((expr.getType() != Condition.LIKE) && (expr.getType() != Condition.EQUALS))
						throw new IllegalArgumentException("Filter on field '" + info.getModelKey() + "' contains not suitable condition '" + expr.getType() + "'");
					if (idx > 1) {
						key = "op" + idx++;
						addField(Field.<Oper, BaseModel> asEnumTo(null).modelKey(key).width("50px").enumClass(Oper.class).editable().showInEditor());
						filterModel.set(key, (expr.getOp() == Operation.AND) ? Oper.AND : (expr.getOp() == Operation.OR) ? Oper.OR : null);
					}
					key = "con" + idx++;
					addField(Field.<TextCond, BaseModel> asEnumTo(null).modelKey(key).caption("Совпадение").enumClass(TextCond.class).editable().showInEditor().require());
					boolean not = (expr.getOp() == Operation.AND_NOT) || (expr.getOp() == Operation.OR_NOT) || (expr.getOp() == Operation.NOT);
					String st = (String) expr.getValue();
					if (expr.getType() == Condition.LIKE)
						if ((st.startsWith("%") || st.startsWith("\\%")) && st.endsWith("%")) {
							filterModel.set(key, not ? TextCond.NOT_LIKE : TextCond.LIKE);
							st = st.substring(1, st.length() - 1);
						} else if (st.startsWith("%") || st.startsWith("\\%")) {
							filterModel.set(key, TextCond.ENDS);
							st = st.substring(1);
						} else if (st.endsWith("%")) {
							filterModel.set(key, TextCond.STARTS);
							st = st.substring(0, st.length() - 1);
						} else
							filterModel.set(key, TextCond.EQUALS);
					else if (st.isEmpty())
						filterModel.set(key, not ? TextCond.NOT_EMPTY : TextCond.EMPTY);

					key = "val" + idx++;
					addField(Field.asTextTo(null).modelKey(key).caption("Образец").editable().showInEditor().require());
					filterModel.set(key, st);
					break;

				case BOOLEAN:
					if ((expr.getType() != Condition.EQUALS))
						throw new IllegalArgumentException("Filter on field '" + info.getModelKey() + "' contains not suitable condition '" + expr.getType() + "'");
					if (idx > 1)
						throw new IllegalArgumentException("Filter on field '" + info.getModelKey() + "' contains more then one expressions");
					key = "val" + idx++;
					addField(Field.asBooleanTo(null).modelKey(key).caption("Образец").editable().showInEditor().require());
					filterModel.set(key, expr.getValue());
					break;

				case DATE:
				case LONG:
				case FLOAT:
				case CURRENCY:
					if (expr.getValue() == null)
						throw new IllegalArgumentException("Filter on field '" + info.getModelKey() + "' contains null value");
					if (idx > 1) {
						key = "op" + idx++;
						addField(Field.<Oper, BaseModel> asEnumTo(null).modelKey(key).width("50px").enumClass(Oper.class).editable().showInEditor());
						filterModel.set(key, ((expr.getOp() == Operation.AND) || (expr.getOp() == Operation.AND_NOT)) ? Oper.AND :
								((expr.getOp() == Operation.OR) || (expr.getOp() == Operation.OR_NOT)) ? Oper.OR : null);
					}
					key = "con" + idx++;
					addField(Field.<Cond, BaseModel> asEnumTo(null).modelKey(key).caption("Условие").width("50px").enumClass(Cond.class).editable().showInEditor().require());
					not = (expr.getOp() == Operation.AND_NOT) || (expr.getOp() == Operation.OR_NOT) || (expr.getOp() == Operation.NOT);
					if (expr.getType() == Condition.EQUALS) {
						if (expr.getValue() != null)
							filterModel.set(key, not ? Cond.NOT_EQUALS : Cond.EQUALS);
						else
							filterModel.set(key, not ? Cond.NOT_EMPTY : Cond.EMPTY);
					} else if (expr.getType() == Condition.GREATER)
						filterModel.set(key, not ? Cond.NOT_GREATER : Cond.GREATER);
					else if (expr.getType() == Condition.LESS)
						filterModel.set(key, not ? Cond.NOT_LESS : Cond.LESS);

					key = "val" + idx++;
					switch (info.getType()) {
					case DATE:
						addField(Field.asDateTo(null).modelKey(key).caption("Значение").editable().showInEditor().require());
						DateTextBox dtb = (DateTextBox) getField(key);
						cm.addAutoHidePartner(dtb.getDatePicker().getElement());
						break;
					case LONG:
						addField(Field.asLongTo(null).modelKey(key).caption("Значение").editable().showInEditor().require());
						break;
					case FLOAT:
						addField(Field.asFloatTo(null).modelKey(key).caption("Значение").editable().showInEditor().require());
						break;
					case CURRENCY:
						addField(Field.asCurrencyTo(null).modelKey(key).caption("Значение").editable().showInEditor().require());
						break;

					default:
						break;
					}
					filterModel.set(key, expr.getValue());
					break;

				case LIST:
				case ENUM:
					if (expr.getType() != Condition.EQUALS)
						throw new IllegalArgumentException("Filter on field '" + info.getModelKey() + "' contains not suitable condition '" + expr.getType() + "'");
					if (idx > 1) {
						key = "op" + idx++;
						addField(Field.<Oper, BaseModel> asEnumTo(null).modelKey(key).width("50px").enumClass(Oper.class).editable().showInEditor());
						filterModel.set(key, ((expr.getOp() == Operation.AND) || (expr.getOp() == Operation.AND_NOT)) ? Oper.AND :
								((expr.getOp() == Operation.OR) || (expr.getOp() == Operation.OR_NOT)) ? Oper.OR : null);
					}
					key = "con" + idx++;
					addField(Field.<ListCond, BaseModel> asEnumTo(null).modelKey(key).caption("Условие")
							.width("50px").enumClass(ListCond.class).editable().showInEditor().require());
					not = (expr.getOp() == Operation.AND_NOT) || (expr.getOp() == Operation.OR_NOT) || (expr.getOp() == Operation.NOT);
					filterModel.set(key, not ? ListCond.NOT_EQUALS : ListCond.EQUALS);

					key = "val" + idx++;
					if (info.getType() == FieldTypes.LIST)
						addField(Field.asListTo(null).modelKey(key).caption("Значение").editable().showInEditor()
								.provider(((ListFieldBuilder<BaseModel, ?>) info).getListProvider())
								.listFilter(separateContext(((ListFieldBuilder<BaseModel, BaseModel>) info).getListFilter())).require(info.isRequire()));
					else
						addField(Field.asEnumTo(null).modelKey(key).caption("Значение").editable().showInEditor()
								.enumClass(((EnumFieldBuilder) info).getEnumClass())
								.listFilter(separateContext(((EnumFieldBuilder) info).getListFilter())).require(info.isRequire()));
					filterModel.set(key, expr.getValue());
					break;

				default:
					break;
				}
			}
		else if ((info.getType() == FieldTypes.TEXT) || (info.getType() == FieldTypes.TEXTAREA)) {
			key = "con" + idx++;
			addField(Field.<TextCond, BaseModel> asEnumTo(null).modelKey(key).caption("Совпадение").enumClass(TextCond.class).editable().showInEditor().require());
			filterModel.set(key, TextCond.LIKE);
			addField(Field.asTextTo(null).modelKey("val" + idx++).caption("Образец").editable().showInEditor().require());
		} else if ((info.getType() == FieldTypes.DATE) || (info.getType() == FieldTypes.LONG) || (info.getType() == FieldTypes.FLOAT) || (info.getType() == FieldTypes.CURRENCY)) {
			key = "con" + idx++;
			addField(Field.<Cond, BaseModel> asEnumTo(null).modelKey(key).caption("Условие").enumClass(Cond.class).editable().showInEditor().require());
			filterModel.set(key, Cond.EQUALS);
			key = "val" + idx++;
			switch (info.getType()) {
			case DATE:
				addField(Field.asDateTo(null).modelKey(key).caption("Значение").editable().showInEditor().require());
				DateTextBox dtb = (DateTextBox) getField(key);
				cm.addAutoHidePartner(dtb.getDatePicker().getElement());
				break;
			case LONG:
				addField(Field.asLongTo(null).modelKey(key).caption("Значение").editable().showInEditor().require());
				break;
			case FLOAT:
				addField(Field.asFloatTo(null).modelKey(key).caption("Значение").editable().showInEditor().require());
				break;
			case CURRENCY:
				addField(Field.asCurrencyTo(null).modelKey(key).caption("Значение").editable().showInEditor().require());
				break;

			default:
				break;
			}
		} else if (info.getType() == FieldTypes.BOOLEAN)
			addField(Field.asBooleanTo(null).modelKey("val" + idx++).caption("Образец").editable().showInEditor().require());
		else if ((info.getType() == FieldTypes.LIST) || (info.getType() == FieldTypes.ENUM)) {
			key = "con" + idx++;
			addField(Field.<ListCond, BaseModel> asEnumTo(null).modelKey(key).caption("Условие").enumClass(ListCond.class).editable().showInEditor().require());
			filterModel.set(key, ListCond.EQUALS);
			if (info.getType() == FieldTypes.LIST)
				addField(Field.asListTo(null).modelKey("val" + idx++).caption("Значение").editable().showInEditor()
						.provider(((ListFieldBuilder<BaseModel, ?>) info).getListProvider())
						.listFilter(separateContext(((ListFieldBuilder<BaseModel, BaseModel>) info).getListFilter())).require(info.isRequire()));
			else
				addField(Field.asEnumTo(null).modelKey("val" + idx++).caption("Значение").editable().showInEditor()
						.enumClass(((EnumFieldBuilder) info).getEnumClass())
						.listFilter(separateContext(((EnumFieldBuilder) info).getListFilter())).require(info.isRequire()));
		}

		if ((idx <= 12) && (info.getType() != FieldTypes.BOOLEAN))
			addField(Field.<Oper, BaseModel> asEnumTo(null).modelKey("op" + idx).width("50px").enumClass(Oper.class).editable().showInEditor());
		edit(filterModel);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	protected void onValueChange(HasValue<?> source, String fieldName) {
		if (fieldName.startsWith("op")) {
			int fldIdx;
			int size = fieldsInfo.size();
			for (fldIdx = 0; (fldIdx < size) && !fieldName.equals(fieldsInfo.get(fldIdx).getModelKey()); fldIdx++) ;
			Oper op = filterModel.getEnumProp(fieldName);
			if ((op != null) && (fldIdx == size - 1)) {
				// если выбрано И/ИЛИ, то создаём три новых поля
				if (size > 1)
					fldIdx = Integer.parseInt(fieldName.substring(2)) + 1;
				else
					fldIdx = 1;
				String key = "con" + fldIdx++;
				switch (info.getType()) {
				case TEXT:
				case TEXTAREA:
					addField(Field.<TextCond, BaseModel> asEnumTo(null).modelKey(key).caption("Совпадение").enumClass(TextCond.class).editable().showInEditor().require());
					((HasValue<TextCond>) getField(key)).setValue(TextCond.LIKE);
		    		addField(Field.asTextTo(null).modelKey("val" + fldIdx++).caption("Образец").editable().showInEditor().require());
		    		break;
				case DATE:
				case LONG:
				case FLOAT:
				case CURRENCY:
		    		addField(Field.<Cond, BaseModel> asEnumTo(null).modelKey(key).caption("Условие").enumClass(Cond.class).editable().showInEditor().require());
					((HasValue<Cond>) getField(key)).setValue(Cond.EQUALS);
					key = "val" + fldIdx++;
					switch (info.getType()) {
					case DATE:
						addField(Field.asDateTo(null).modelKey(key).caption("Значение").editable().showInEditor().require());
						DateTextBox dtb = (DateTextBox) getField(key);
						cm.addAutoHidePartner(dtb.getDatePicker().getElement());
						break;
					case LONG:
						addField(Field.asLongTo(null).modelKey(key).caption("Значение").editable().showInEditor().require());
						break;
					case FLOAT:
						addField(Field.asFloatTo(null).modelKey(key).caption("Значение").editable().showInEditor().require());
						break;
					case CURRENCY:
						addField(Field.asCurrencyTo(null).modelKey(key).caption("Значение").editable().showInEditor().require());
						break;

					default:
						break;
					}
		    		break;
				case LIST:
				case ENUM:
		    		addField(Field.<ListCond, BaseModel> asEnumTo(null).modelKey(key).caption("Условие").enumClass(ListCond.class).editable().showInEditor().require());
					((HasValue<ListCond>) getField(key)).setValue(ListCond.EQUALS);
					key = "val" + fldIdx++;
					if (info.getType() == FieldTypes.LIST)
						addField(Field.asListTo(null).modelKey(key).caption("Значение").editable().showInEditor()
								.provider(((ListFieldBuilder<BaseModel, ?>) info).getListProvider())
								.listFilter(separateContext(((ListFieldBuilder<BaseModel, BaseModel>) info).getListFilter())).require(info.isRequire()));
					else
						addField(Field.asEnumTo(null).modelKey(key).caption("Значение").editable().showInEditor()
								.enumClass(((EnumFieldBuilder) info).getEnumClass())
								.listFilter(separateContext(((EnumFieldBuilder) info).getListFilter())).require(info.isRequire()));
		    		break;
		    	default:
		    		break;
				}
				if (size < 12)
	    			addField(Field.<Oper, BaseModel> asEnumTo(null).modelKey("op" + fldIdx).width("50px").enumClass(Oper.class).editable().showInEditor());
			} else if ((op == null) && (fldIdx < size - 1)) {
				// если выбрана пустота, то удаляем текущее поле и следующие два за ним
				removeField(fieldsInfo.get(fldIdx + 2).getModelKey());
				removeField(fieldsInfo.get(fldIdx + 1).getModelKey());
				if (fldIdx != size - 3)
					removeField(fieldName);
				size = fieldsInfo.size();
				if ((size < 12) && (fieldsInfo.get(size - 1).getModelKey().startsWith("val"))) {
					fldIdx = Integer.parseInt(fieldsInfo.get(size - 1).getModelKey().substring(3)) + 1;
					addField(Field.<Oper, BaseModel> asEnumTo(null).modelKey("op" + fldIdx).width("50px").enumClass(Oper.class).editable().showInEditor());
				}
			}
		} else if (fieldName.startsWith("con") && ((info.getType() == FieldTypes.DATE)
				|| (info.getType() == FieldTypes.LONG)
				|| (info.getType() == FieldTypes.FLOAT)
				|| (info.getType() == FieldTypes.CURRENCY))) {
			int fldIdx;
			int size = fieldsInfo.size();
			for (fldIdx = 0; (fldIdx < size) && !fieldName.equals(fieldsInfo.get(fldIdx).getModelKey()); fldIdx++) ;
			if (fldIdx < size - 1) {
				HasValue<?> fld = getField(fieldsInfo.get(fldIdx + 1).getModelKey());
				final UIObject field = (UIObject) fld;
				Cond c = filterModel.getEnumProp(fieldName);
				boolean enabled = (c != Cond.EMPTY) && (c != Cond.NOT_EMPTY);
				if (!enabled)
					fld.setValue(null);
				if (field instanceof FocusWidget)
					((FocusWidget) field).setEnabled(enabled);
				else
					field.getElement().setPropertyBoolean("disabled", !enabled);
			}
		} else if (fieldName.startsWith("con") && ((info.getType() == FieldTypes.TEXT) || (info.getType() == FieldTypes.TEXTAREA))) {
			int fldIdx;
			int size = fieldsInfo.size();
			for (fldIdx = 0; (fldIdx < size) && !fieldName.equals(fieldsInfo.get(fldIdx).getModelKey()); fldIdx++) ;
			if (fldIdx < size - 1) {
				HasValue<?> fld = getField(fieldsInfo.get(fldIdx + 1).getModelKey());
				final UIObject field = (UIObject) fld;
				TextCond tc = filterModel.getEnumProp(fieldName);
				boolean enabled = (tc != TextCond.EMPTY) && (tc != TextCond.NOT_EMPTY);
				if (!enabled)
					fld.setValue(null);
				if (field instanceof FocusWidget)
					((FocusWidget) field).setEnabled(enabled);
				else
					field.getElement().setPropertyBoolean("disabled", !enabled);
			}
		}
	}

	public Where getFilter() {
		if (fieldsInfo.size() <= 0)
			return null;
		Where ret = null;
		if ((info.getType() == FieldTypes.TEXT) || (info.getType() == FieldTypes.TEXTAREA)) {
			TextCond cond = (TextCond) getField(fieldsInfo.get(0).getModelKey()).getValue();
			String val = (String) getField(fieldsInfo.get(1).getModelKey()).getValue();
			if (((val == null) || val.isEmpty()) && (cond != TextCond.EMPTY) && (cond != TextCond.NOT_EMPTY))
				return null;
			String mk = info.getModelKey();
			val = val.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");	// экранируем спецсимволы
			switch (cond) {
			case LIKE:
				ret = Where.like(mk, "%" + val + "%");
				break;

			case NOT_LIKE:
				ret = Where.notLike(mk, "%" + val + "%");
				break;

			case STARTS:
				ret = Where.like(mk, val + "%");
				break;

			case ENDS:
				ret = Where.like(mk, "%" + val);
				break;

			case EQUALS:
				ret = Where.like(mk, val);
				break;

			case EMPTY:
				ret = Where.where(Where.equals("trim(" + mk + ")", "").orIsNull(mk));
				break;

			case NOT_EMPTY:
				ret = Where.notEquals("trim(" + mk + ")", "");
				break;
			}
			for (int i = 3; i < fieldsInfo.size(); i += 3) {
				Oper op = (Oper) getField(fieldsInfo.get(i - 1).getModelKey()).getValue();
				cond = (TextCond) getField(fieldsInfo.get(i).getModelKey()).getValue();
				val = (String) getField(fieldsInfo.get(i + 1).getModelKey()).getValue();
				if (((op == null) || (val == null) || val.isEmpty()) && (cond != TextCond.EMPTY) && (cond != TextCond.NOT_EMPTY))
					continue;
				val = val.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");	// экранируем спецсимволы
				switch (cond) {
				case LIKE:
					if (op == Oper.AND)
						ret.andLike(mk, "%" + val + "%");
					else
						ret.orLike(mk, "%" + val + "%");
					break;

				case NOT_LIKE:
					if (op == Oper.AND)
						ret.andNotLike(mk, "%" + val + "%");
					else
						ret.orNotLike(mk, "%" + val + "%");
					break;

				case STARTS:
					if (op == Oper.AND)
						ret.andLike(mk, val + "%");
					else
						ret.orLike(mk, val + "%");
					break;

				case ENDS:
					if (op == Oper.AND)
						ret.andLike(mk, "%" + val);
					else
						ret.orLike(mk, "%" + val);
					break;

				case EQUALS:
					if (op == Oper.AND)
						ret.andLike(mk, val);
					else
						ret.orLike(mk, val);
					break;

				case EMPTY:
					if (op == Oper.AND)
						ret.andWhere(Where.equals("trim(" + mk + ")", "").orIsNull(mk));
					else
						ret.orWhere(Where.equals("trim(" + mk + ")", "").orIsNull(mk));
					break;

				case NOT_EMPTY:
					if (op == Oper.AND)
						ret.andNotEquals("trim(" + mk + ")", "");
					else
						ret.orNotEquals("trim(" + mk + ")", "");
					break;
				}
			}
		}
		if ((info.getType() == FieldTypes.DATE) || (info.getType() == FieldTypes.LONG) || (info.getType() == FieldTypes.FLOAT) || (info.getType() == FieldTypes.CURRENCY)) {
			String key = info.getModelKey();
			ret = new Where(true);
			for (int i = 0; i < fieldsInfo.size(); i += 3) {
				Oper op = (i > 0) ? (Oper) getField(fieldsInfo.get(i - 1).getModelKey()).getValue() : Oper.AND;
				Cond cond = (Cond) getField(fieldsInfo.get(i).getModelKey()).getValue();
				Object val = getField(fieldsInfo.get(i + 1).getModelKey()).getValue();
				if ((op != null)/* && (val != null)*/)
					switch (cond) {
					case EQUALS:
						if (op == Oper.AND)
							ret.andEquals(key, val);
						else
							ret.orEquals(key, val);
						break;

					case NOT_EQUALS:
						if (op == Oper.AND)
							ret.andNotEquals(key, val);
						else
							ret.orNotEquals(key, val);
						break;

					case GREATER:
						if (op == Oper.AND)
							ret.andGreater(key, val);
						else
							ret.orGreater(key, val);
						break;

					case NOT_GREATER:
						if (op == Oper.AND)
							ret.andNotGreater(key, val);
						else
							ret.orNotGreater(key, val);
						break;

					case LESS:
						if (op == Oper.AND)
							ret.andLess(key, val);
						else
							ret.orLess(key, val);
						break;

					case NOT_LESS:
						if (op == Oper.AND)
							ret.andNotLess(key, val);
						else
							ret.orNotLess(key, val);
						break;

					case EMPTY:
						if (op == Oper.AND)
							ret.andIsNull(key);
						else
							ret.orIsNull(key);
						break;

					case NOT_EMPTY:
						if (op == Oper.AND)
							ret.andIsNotNull(key);
						else
							ret.orIsNotNull(key);
						break;
					}
			}
		}
		if (info.getType() == FieldTypes.BOOLEAN) {
			Boolean val = (Boolean) getField(fieldsInfo.get(0).getModelKey()).getValue();
			if (val == null)
				return null;
			ret = Where.equals(info.getModelKey(), val);
		}
		if ((info.getType() == FieldTypes.LIST) || (info.getType() == FieldTypes.ENUM)) {
			Object val = getField(fieldsInfo.get(1).getModelKey()).getValue();
			ListCond cond = (ListCond) getField(fieldsInfo.get(0).getModelKey()).getValue();
			String mk = info.getModelKey();
			switch (cond) {
			case EQUALS:
				ret = Where.equals(mk, val);
				break;

			case NOT_EQUALS:
				ret = Where.notEquals(mk, val);
				break;
			}
			for (int i = 3; i < fieldsInfo.size(); i += 3) {
				Oper op = (Oper) getField(fieldsInfo.get(i - 1).getModelKey()).getValue();
				cond = (ListCond) getField(fieldsInfo.get(i).getModelKey()).getValue();
				val = getField(fieldsInfo.get(i + 1).getModelKey()).getValue();
				if (op != null)
					switch (cond) {
					case EQUALS:
						if (op == Oper.AND)
							ret.andEquals(mk, val);
						else
							ret.orEquals(mk, val);
						break;

					case NOT_EQUALS:
						if (op == Oper.AND)
							ret.andNotEquals(mk, val);
						else
							ret.orNotEquals(mk, val);
						break;
					}
			}
		}
		return ret;
	}

}