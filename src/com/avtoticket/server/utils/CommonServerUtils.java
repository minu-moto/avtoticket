/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.server.utils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.avtoticket.shared.models.BaseModel;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 14.02.2012 14:09:25
 */
public class CommonServerUtils {

//	private static final Logger logger = LoggerFactory.getLogger(CommonServerUtils.class.getName());

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@"
			+ "([a-z0-9]([-a-z0-9]{0,61}[a-z0-9])?\\.)*(aero|arpa|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel|[a-z][a-z])$";
	private static Pattern pattern = Pattern.compile(EMAIL_PATTERN);

	/**
	 * Фильтр e-mail адресов
	 * 
	 * @param email
	 *            - строка с адресом
	 * @return true, если адрес является правильным имейлом, false - иначе
	 */
	public static Boolean isValidEmail(String email) {
		return pattern.matcher(email).matches();
	}

	/**
	 * Проверяет параметры запроса на содержание какой-либо полезной информации
	 * 
	 * @param params
	 *            - параметры запроса или хранимой процедуры
	 * @return true - если все параметры пустые, false - иначе
	 */
	public static Boolean isEmptyParams(Object[] params) {
		for (Object item : params)
			if ((item != null) && 
					((!(item instanceof List) && !String.valueOf(item).isEmpty()) || 
							((item instanceof List) && (((List<?>) item).size() > 0) && (((List<?>) item).get(0) != null))))
				return false;
		return true;
	}

	/**
	 * Склеивает параметры запроса в строку через запятую. Если в запросе учавствовала функция с паролем, то пароль затирается звёздочками
	 *
	 * @param sql
	 * 			  - запрос
	 * @param params
	 *            - массив объектов
	 * @return строковое представление параметров
	 */
	public static String joinToStr(String sql, Object[] params) {
		if ((sql != null) && sql.contains("core.login"))
			return joinToStr(new Object[] {params[0], "***"});
		else
			return joinToStr(params);
	}

	/**
	 * Склеивает массив объектов в строку через запятую
	 *
	 * @param params
	 *            - массив объектов
	 * @return строковое представление параметров
	 */
	public static String joinToStr(Object[] params) {
		return joinToStr(params, ", ");
	}

	/**
	 * Склеивает массив объектов в строку через разделитель
	 *
	 * @param params
	 *            - массив объектов
	 * @param separator
	 * 			  - разделитель
	 * @return строковое представление параметров
	 */
	public static String joinToStr(Object[] params, String separator) {
		int max = 50;
		StringBuilder ret = new StringBuilder();
		if (params != null)
			for (Object param : params)
				if (param instanceof Collection) {
					Collection<?> lst = (Collection<?>) param;
					if ((max > 0) && (lst.size() > max)) {
						ret.append(separator).append('[');
						lst.stream().limit(max).forEach(obj -> ret.append(obj).append(separator));
						ret.append(" ... ").append(lst.size()).append(" items]");
					} else
						ret.append(separator).append(param);
				} else if ((param != null) && (param instanceof Float)) {
					float v = (Float) param;	// для вещественных чисел с целыми значениями отрезаем дробную часть
					if (v == (int) v)
						ret.append(separator).append((int) v);
					else
						ret.append(separator).append(v);
				} else if ((param != null) && (param instanceof Double)) {
					double v = (Double) param;	// для вещественных чисел с целыми значениями отрезаем дробную часть
					if (v == (long) v)
						ret.append(separator).append((long) v);
					else
						ret.append(separator).append(v);
				} else if ((param != null) && (param instanceof Enum)) {
					ret.append(separator).append(((Enum<?>) param).name());
				} else
					ret.append(separator).append(param);
		int sepLength = separator.length();
		return (ret.length() > sepLength) ? ret.substring(sepLength) : "";
	}

	@SuppressWarnings("unchecked")
	public static <K extends Object, V extends BaseModel> Map<K, V> createModelMap(String keyField, List<V> models) {
		if (models == null)
			return null;
		Map<K, V> ret = new LinkedHashMap<K, V>();
		for (V model : models)
			ret.put((K) model.get(keyField), model);
		return ret;
	}

}