/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.shared.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.avtoticket.shared.models.WhereExpression.Condition;
import com.avtoticket.shared.models.WhereExpression.Operation;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Построитель условий запроса
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 16.10.2013 17:29:21
 */
public class Where implements IsSerializable {

	private List<WhereExpression<?>> expression = new ArrayList<WhereExpression<?>>();
	private Long offset;
	private Long limit;
	private boolean trimTime;

	/** Точность вычислений */
	public static final double EPS = 0.01;

	/**
	 * Два вещественных числа считаем равными тогда и только тогда,
	 * когда либо они оба равны <code>null</code>, либо их разность меньше {@link #EPS эпсилон}.<br>
	 * Сравнение не чисел (NaN) и бесконечностей (±Infinity) с чем либо считается заведомо ложным.
	 * 
	 * @param a - вещественное число для сравнения
	 * @param b - вещественное число для сравнения
	 * @return <code>true</code> - если a и b равны<br>
	 * 			<code>false</code> - если a и b не равны, либо хотябы одно из них имеет значение NaN или ±Infinity
	 */
	public static boolean equalDouble(Double a, Double b) {
		return !((a == null) ^ (b == null)) && ((a == null) || (Math.abs(a - b) <= EPS));
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
		    value = "UUF_UNUSED_FIELD", 
		    justification = "белый список для SerializationPolicy")
	private WhereExpression<BaseModel> we;

	public static Where equals(String field, Object value) {
		return new Where().appendExpression(null, field, Condition.EQUALS, value);
	}

	public static Where notEquals(String field, Object value) {
		return new Where().appendExpression(Operation.NOT, field, Condition.EQUALS, value);
	}

	public static Where less(String field, Object value) {
		return new Where().appendExpression(null, field, Condition.LESS, value);
	}

	public static Where notLess(String field, Object value) {
		return new Where().appendExpression(Operation.NOT, field, Condition.LESS, value);
	}

	public static Where greater(String field, Object value) {
		return new Where().appendExpression(null, field, Condition.GREATER, value);
	}

	public static Where notGreater(String field, Object value) {
		return new Where().appendExpression(Operation.NOT, field, Condition.GREATER, value);
	}

	public static Where like(String field, Object value) {
		return new Where().appendExpression(null, field, Condition.LIKE, value);
	}

	public static Where notLike(String field, Object value) {
		return new Where().appendExpression(Operation.NOT, field, Condition.LIKE, value);
	}

	public static Where isNull(String field) {
		return new Where().appendExpression(null, field, Condition.EQUALS, null);
	}

	public static Where isNotNull(String field) {
		return new Where().appendExpression(Operation.NOT, field, Condition.EQUALS, null);
	}

	public static Where where(Where where) {
		return new Where().appendExpression(null, where);
	}

	public static Where whereNot(Where where) {
		return new Where().appendExpression(Operation.NOT, where);
	}

	public static Where false_() {
		return new Where().appendExpression(null, null, Condition.EQUALS, 0L);
	}

	public static Where offset_(Integer offset) {
		return offset_((offset != null) ? offset.longValue() : null);
	}

	public static Where offset_(Long offset) {
		return new Where().offset(offset);
	}

	public static Where limit_(Integer limit) {
		return limit_((limit != null) ? limit.longValue() : null);
	}

	public static Where limit_(Long limit) {
		return new Where().limit(limit);
	}

	public Where() {
		this(false);
	}

	public Where(boolean trimTime) {
		this.trimTime = trimTime;
	}

	public Where andEquals(String field, Object value) {
		return appendExpression(Operation.AND, field, Condition.EQUALS, value);
	}

	public Where orEquals(String field, Object value) {
		return appendExpression(Operation.OR, field, Condition.EQUALS, value);
	}

	public Where andNotEquals(String field, Object value) {
		return appendExpression(Operation.AND_NOT, field, Condition.EQUALS, value);
	}

	public Where orNotEquals(String field, Object value) {
		return appendExpression(Operation.OR_NOT, field, Condition.EQUALS, value);
	}

	public Where andLess(String field, Object value) {
		return appendExpression(Operation.AND, field, Condition.LESS, value);
	}

	public Where orLess(String field, Object value) {
		return appendExpression(Operation.OR, field, Condition.LESS, value);
	}

	public Where andNotLess(String field, Object value) {
		return appendExpression(Operation.AND_NOT, field, Condition.LESS, value);
	}

	public Where orNotLess(String field, Object value) {
		return appendExpression(Operation.OR_NOT, field, Condition.LESS, value);
	}

	public Where andGreater(String field, Object value) {
		return appendExpression(Operation.AND, field, Condition.GREATER, value);
	}

	public Where orGreater(String field, Object value) {
		return appendExpression(Operation.OR, field, Condition.GREATER, value);
	}

	public Where andNotGreater(String field, Object value) {
		return appendExpression(Operation.AND_NOT, field, Condition.GREATER, value);
	}

	public Where orNotGreater(String field, Object value) {
		return appendExpression(Operation.OR_NOT, field, Condition.GREATER, value);
	}

	public Where andLike(String field, Object value) {
		return appendExpression(Operation.AND, field, Condition.LIKE, value);
	}

	public Where orLike(String field, Object value) {
		return appendExpression(Operation.OR, field, Condition.LIKE, value);
	}

	public Where andNotLike(String field, Object value) {
		return appendExpression(Operation.AND_NOT, field, Condition.LIKE, value);
	}

	public Where orNotLike(String field, Object value) {
		return appendExpression(Operation.OR_NOT, field, Condition.LIKE, value);
	}

	public Where andIsNull(String field) {
		return andEquals(field, null);
	}

	public Where orIsNull(String field) {
		return orEquals(field, null);
	}

	public Where andIsNotNull(String field) {
		return andNotEquals(field, null);
	}

	public Where orIsNotNull(String field) {
		return orNotEquals(field, null);
	}

	public Where andWhere(Where where) {
		return appendExpression(Operation.AND, where);
	}

	public Where orWhere(Where where) {
		return appendExpression(Operation.OR, where);
	}

	public Where andWhereNot(Where where) {
		return appendExpression(Operation.AND_NOT, where);
	}

	public Where orWhereNot(Where where) {
		return appendExpression(Operation.OR_NOT, where);
	}

	public Where offset(Integer offset) {
		return offset((offset != null) ? offset.longValue() : null);
	}

	public Where offset(Long offset) {
		this.offset = offset;
		return this;
	}

	public Where limit(Integer limit) {
		return limit((limit != null) ? limit.longValue() : null);
	}

	public Where limit(Long limit) {
		this.limit = limit;
		return this;
	}

	public String compile(String sort, List<Object> params) {
		return compile(sort, params, true, true);
	}

	public boolean isEmpty() {
		return expression.size() <= 0;
	}

	public String compile(String sort, List<Object> params, boolean appendWhere, boolean appendLimit) {
		StringBuilder ret = new StringBuilder();
		if (!isEmpty() && appendWhere)
			ret.append(" where ");
//		expression.stream().map(cond -> cond.compile(params, trimTime)).forEachOrdered(ret::append);	TODO
		for (WhereExpression<?> cond : expression)
			ret.append(cond.compile(params, trimTime));
		if (sort != null && !sort.isEmpty())
			ret.append(" order by ").append(sort.replace("_id ", "_" + BaseModel.DISPLAY_FIELD + " ")).append(", ").append(BaseModel.ID);
		if (appendLimit) {
			if ((limit != null) && (limit >= 0)) {
				ret.append(" limit ?");
				if (params != null)
					params.add(limit);
			}
			if ((offset != null) && (offset >= 0)) {
				ret.append(" offset ?");
				if (params != null)
					params.add(offset);
			}
		}
		return ret.toString();
	}

	private Where appendExpression(Operation op, String field, Condition type, Object value) {
		WhereExpression<Object> cond = appendExpression(op);
		cond.setFieldName(field);
		cond.setType(type);
		cond.setValue(value);
		return this;
	}

	private Where appendExpression(Operation op, Where where) {
		if ((where != null) && !where.isEmpty()) {
			WhereExpression<Object> cond = appendExpression(op);
			cond.setSubExpression(where);
		}
		return this;
	}

	private WhereExpression<Object> appendExpression(Operation op) {
		WhereExpression<Object> cond = new WhereExpression<Object>();
		if (!isEmpty() || (op == Operation.NOT))
			cond.setOp(op);
		else if ((op == Operation.AND_NOT) || (op == Operation.OR_NOT))
			cond.setOp(Operation.NOT);
		expression.add(cond);
		return cond;
	}

	@Override
	public String toString() {
		List<Object> params = new ArrayList<Object>();
		String ret = compile(null, params);
		return ret + " with params " + params;
	}

	public void clear() {
		expression.clear();
		limit = null;
		offset = null;
	}

	public List<WhereExpression<?>> getExpression() {
		return expression;
	}

	public Long getOffset() {
		return offset;
	}

	public Long getLimit() {
		return limit;
	}

	@SuppressWarnings("deprecation")
	private Date trimTime(Date date) {
		long msec = date.getTime();
		int offset = (int) (msec % 1000);
	    // Normalize if time is before epoch
	    if (offset < 0)
	     	offset += 1000;
		Date newDate = new Date();
	    newDate.setTime(msec - offset);
	    newDate.setHours(0);
	    newDate.setMinutes(0);
	    newDate.setSeconds(0);
	    return newDate;
	}

	@SuppressWarnings("unchecked")
	public boolean check(BaseModel model) {
		boolean ret = true;
		for (WhereExpression<?> expr : getExpression()) {
			boolean tmp = false;

			if (expr.getSubExpression() != null)
				tmp = expr.getSubExpression().check(model);
			else {
				Object val1;
				if (expr.getFieldName() != null)
					if (expr.getFieldName().startsWith("trim(")) {
						val1 = model.get(expr.getFieldName().substring(5, expr.getFieldName().length() - 1));
						if ((val1 != null) && (val1 instanceof String))
							val1 = ((String) val1).trim();
						else
							val1 = "";
					} else
						val1 = model.get(expr.getFieldName());
				else
					val1 = null;
				Object val2 = expr.getValue();

				if (trimTime) {
					if ((val1 != null) && (val1 instanceof Date))
						val1 = trimTime((Date) val1);
					if ((val2 != null) && (val2 instanceof Date))
						val2 = trimTime((Date) val2);
				}

				switch (expr.getType()) {
				case EQUALS:
					if ((val2 != null) && (val2 instanceof Collection))
						tmp = ((Collection<?>) val2).contains(val1);
					else if ((val1 == null) && (val2 != null)) {
						Operation op = expr.getOp();	// сравнение любого значения с null'ом всегда даёт false
						tmp = (op == Operation.NOT) || (op == Operation.AND_NOT) || (op == Operation.OR_NOT);
					} else if ((val2 != null) && (val2 instanceof Double))
						tmp = equalDouble((Double) val1, (Double) val2);
					else
						tmp = Objects.equals(val1, val2);
					break;
				case GREATER:
					if ((val1 != null) && (val2 != null))
						tmp = ((Comparable<Object>) val1).compareTo(val2) > 0;
					else
						tmp = false;
					break;
				case LESS:
					if ((val1 != null) && (val2 != null))
						tmp = ((Comparable<Object>) val1).compareTo(val2) < 0;
					else
						tmp = false;
					break;
				case LIKE:
					String s1 = (String) (((val1 != null) && (val1 instanceof String)) ? val1 : null);
					String s2 = (String) (((val2 != null) && (val2 instanceof String)) ? val2 : null);
					if ((s1 != null) && (s2 != null)) {
						StringBuilder exp = new StringBuilder("^");
						final char escapeChar = '\\';
						String replaceChars = "\\^$*+-?.|()[]{}";
						// преобразуем выражение оператора like в регулярку
					    for (int i = 0; i < s2.length(); i++) {
					    	char ch = s2.charAt(i);
					    	switch (ch) {
							case '_':
								// один любой символ
					    		exp.append(".");
								break;
							case '%':
								// любое количество любых символов
					    		exp.append(".*");
								break;
							case escapeChar:
					    		i++;
					    		// экранированный символ просто берём и добавляем как есть
					    		if (i < s2.length())
					    			ch = s2.charAt(i);
							default:
						    	if (replaceChars.indexOf(ch) >= 0)
						    		exp.append(escapeChar);
						    	exp.append(ch);
								break;
							}
					    }
					    exp.append("$");
						RegExp regexp = RegExp.compile(exp.toString(), "i");
						tmp = regexp.test(s1);
					}
					break;
				}
			}

			if (expr.getOp() == null)
				ret = tmp;
			else
				switch (expr.getOp()) {
				case AND:
					ret &= tmp;
					break;
				case AND_NOT:
					ret &= !tmp;
					break;
				case NOT:
					ret = !tmp;
					break;
				case OR:
					ret |= tmp;
					break;
				case OR_NOT:
					ret |= !tmp;
					break;
				}
		}
		return ret;
	}

}