/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.shared.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 16.10.2013 18:35:32
 */
public class WhereExpression<T> implements IsSerializable {

	/**
	 * Проверяемое условие: больше, меньше, равно, соответствует
	 */
	public enum Condition {
		EQUALS(" = "),
		LESS(" < "),
		GREATER(" > "),
		LIKE(" ~~* ");

		private String sql;

		private Condition(String sql) {
			this.sql = sql;
		}

		@Override
		public String toString() {
			return sql;
		}
	}

	/**
	 * Операция объединения условий: и, или, не, и не, или не
	 */
	public enum Operation {
		OR(" or "),
		AND(" and "),
		NOT("not "),
		OR_NOT(" or not "),
		AND_NOT(" and not ");

		private String sql;

		private Operation(String sql) {
			this.sql = sql;
		}

		@Override
		public String toString() {
			return sql;
		}
	}

	private Operation op;
	private Where subExpression;
	private String fieldName;
	private Condition type;
	private T value;

	public Operation getOp() {
		return op;
	}
	public void setOp(Operation op) {
		this.op = op;
	}

	public Where getSubExpression() {
		return subExpression;
	}
	public void setSubExpression(Where subCondition) {
		this.subExpression = subCondition;
	}

	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Condition getType() {
		return type;
	}
	public void setType(Condition type) {
		this.type = type;
	}

	public T getValue() {
		return value;
	}
	public void setValue(T value) {
		this.value = value;
	}

	public String compile(List<Object> params, boolean trimTime) {
		StringBuilder ret = new StringBuilder();

		if (getOp() != null)
			ret.append(getOp());
		if (getSubExpression() != null) {
			String sub = getSubExpression().compile(null, params, false, false);
			if ((sub != null) && !sub.isEmpty())
				ret.append("(").append(sub).append(")");
			else
				return "";
		} else switch (getType()) {
			case EQUALS:
				if (getValue() == null)
					ret.append(getFieldName()).append(" is null");
				else if (getValue() instanceof Boolean) {
					if (Boolean.FALSE.equals(getValue()))
						ret.append("not ");
					ret.append(getFieldName());
				} else {
					if (getValue() instanceof Double)
						ret.append("eq(").append(getFieldName()).append(", ?)");
					else if (getValue() instanceof Date) {
						ret.append(getFieldName());
						if (trimTime)
							ret.append("::DATE");
						ret.append(" = ?");
						if (trimTime)
							ret.append("::DATE");
					} else if (getValue() instanceof Collection)
						ret.append(getFieldName()).append(" = any(?)");
					else
						ret.append(getFieldName()).append(" = ?");

					if (params != null)
						params.add(getValue());
				}
				break;

			case LIKE:
			case LESS:
			case GREATER:
				if (getValue() == null)
					throw new IllegalArgumentException("'" + getType() + "' compare with null condition value");
				boolean tt = trimTime && (getValue() instanceof Date);
				ret.append(getFieldName());
				if (tt)
					ret.append("::DATE");
				ret.append(getType());
				ret.append("?");
				if (tt)
					ret.append("::DATE");
				if (params != null)
					params.add(getValue());
				break;
			}
		return ret.toString();
	}

	@Override
	public String toString() {
		List<Object> params = new ArrayList<Object>();
		String ret = compile(params, false);
		return ret + " with params " + params;
	}

}