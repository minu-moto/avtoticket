/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.shared.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.avtoticket.shared.models.core.Formula;
import com.avtoticket.shared.models.core.Gender;
import com.avtoticket.shared.models.core.HelpType;
import com.avtoticket.shared.models.core.NasPunkt;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Базовая модель данных
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 13.02.2012 13:26:25
 */
@SuppressWarnings("unused")
public class BaseModel extends HashMap<String, Object> implements IsSerializable {

	private static final long serialVersionUID = 5287312595797502294L;

	// белый список для SerializationPolicy
	private Integer integer_;
	private Long long_;
	private Boolean boolean_;
	private Double double_;
	private Date date_;
	private Map<?, ?> hashMap_;
	private HashSet<?> hs;
	private List<BaseModel> list_;
	private Gender g;
	private HelpType ht;
	private NasPunkt np;
	private Formula f;

	private static final String rootPackage = BaseModel.class.getName().substring(0, BaseModel.class.getName().lastIndexOf('.'));
	private static Reflection refl = null;

	public static final transient String ID = "id";
	public static final transient String VALUE_FIELD = "valuefield";
	public static final transient String DISPLAY_FIELD = "displayfield";
	public static final transient String CLASS_NAME = "class";

	/**
	 * Базовая модель данных (конструктор по умолчанию нужен для RPC)
	 */
	public BaseModel() {
		//setClassName(BaseModel.class.getName());
	}

	public BaseModel(Object value, String display) {
		setValueField(value);
		setDisplayField(display);
	}

	/**
	 * Базовая модель данных
	 * 
	 * @param className - имя класса фактической модели
	 */
	public BaseModel(String className) {
		setClassName(className);
	}

	/**
	 * Получить уникальный идентификатор модели
	 * @return уникальный идентификатор модели
	 */
	public Long getId() {
		return getLongProp(ID);
	}
	public void setId(Long id) {
		set(VALUE_FIELD, id);
		set(ID, id);
	}

	public String getDisplayField() {
		return getStringProp(DISPLAY_FIELD);
	}
	public void setDisplayField(String displayfield) {
		set(DISPLAY_FIELD, displayfield);
	}

	public Object getValueField() {
		return get(VALUE_FIELD);
	}
	public void setValueField(Object value) {
		set(VALUE_FIELD, value);
	}

	public String getClassName() {
		return getFullClassName(getStringProp(CLASS_NAME));
	}
	public void setClassName(String className) {
		if ((className != null) && className.startsWith(rootPackage))
			set(CLASS_NAME, "rt" + className.substring(rootPackage.length()));
		else
			set(CLASS_NAME, className);
	}

	public BaseModel fill(Map<String, Object> model) {
		if (model != null) {
			Object cn = model.get(CLASS_NAME);
			model.remove(CLASS_NAME);
			putAll(model);
			if (cn != null)
				model.put(CLASS_NAME, cn);
		}
		return this;
	}

	public Long getLongProp(String prop) {
		Object obj = get(prop);
		if (obj == null)
			return null;
		else if (obj instanceof Long)
			return (Long) obj;
		else if (obj instanceof Double)
			return ((Double) obj).longValue();
		else if (obj instanceof Date)
			return ((Date) obj).getTime();
		else
			try {
				return Long.valueOf(String.valueOf(obj));
			} catch (Exception e) {
				return null;
			}
	}
    public Long getNullSafeLong(String fieldName) {
        Long ret = getLongProp(fieldName);
        return (ret == null) ? 0L : ret;
    }

	public Integer getIntegerProp(String prop) {
		Object obj = get(prop);
		if (obj == null)
			return null;
		else if (obj instanceof Long)
			return ((Long) obj).intValue();
		else if (obj instanceof Double)
			return ((Double) obj).intValue();
		else
			try {
				return Integer.valueOf(String.valueOf(obj));
			} catch (Exception e) {
				return null;
			}
	}

	public String getStringProp(String prop) {
		Object obj = get(prop);
		if ((obj == null) || "null".equals(obj))
			return "";
		else
			return obj.toString();
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
	public Boolean getBooleanProp(String prop) {
		Object obj = get(prop);
		if (obj == null)
			return null;
		else if (obj instanceof Boolean)
			return (Boolean) obj;
		else if ((obj instanceof Integer) || (obj instanceof Long))
			return !"0".equalsIgnoreCase(String.valueOf(obj));
		else
			return Boolean.valueOf(String.valueOf(obj));
	}

	public Date getDateProp(String prop) {
		Object obj = get(prop);
		if (obj == null)
			return null;
		else if (obj instanceof Date)
			return (Date) obj;
		else if (obj instanceof Long)
			return new Date((Long) obj);
		else if (obj instanceof String)
			return /*SerializerUtil.parseDateTime(String.valueOf(obj))*/null;	// TODO
		else
			return null;
	}

	public Double getDoubleProp(String prop) {
		Object obj = get(prop);
		if (obj == null)
			return null;
		else if (obj instanceof Number)
			return ((Number) obj).doubleValue();
		else if (obj instanceof String)
			return Double.valueOf(obj.toString());
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	public <T extends Enum<?>> T getEnumProp(String prop) {
		Object obj = get(prop);
		if (obj == null)
			return null;
		else if (obj instanceof Enum)
			return (T) obj;
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getListProp(String prop) {
		Object list = get(prop);
		return ((list != null) && (list instanceof List)) ? (List<T>) list : null;
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseModel> T getModelProp(String prop) {
		Object model = get(prop);
		return ((model != null) && (model instanceof BaseModel)) ? (T) model : null;
	}

	@SuppressWarnings("unchecked")
	public <T> Set<T> getSetProp(String prop) {
		Object set = get(prop);
		return ((set != null) && (set instanceof Set)) ? (Set<T>) set : null;
	}

	@SuppressWarnings("unchecked")
	protected <K, V> Map<K, V> getMapProp(String prop) {
		Object map = get(prop);
		return ((map != null) && (map instanceof Map)) ? (Map<K, V>) map : null;
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseModel> T set(String property, Object value) {
		put(property, value);
		return (T) this;
	}

	public Boolean hasProp(String prop) {
		return get(prop) != null;
	}

	/**
	 * Обновить связи моделей. Например присвоить masterId всем detail'ам.
	 * 
	 * @param value - значение, которое надо записать в подчинённые модели
	 * @param detailKey - имя поля, в котором лежит список с подчинёнными моделями
	 * @param referenceKey - имя поля, в которое нужно записать значение
	 */
	protected void updateReferences(Object value, String detailKey, String referenceKey) {
		List<BaseModel> list = getListProp(detailKey);
		if (list != null)
			for (BaseModel item : list)
				item.set(referenceKey, value);
	}

	protected <T extends BaseModel> void appendModel(T model, String detailKey) {
		appendModels(Arrays.asList(model), detailKey);
	}

	/**
	 * Добавить модели к списку. Если списка не существует, то создать и добавить в него модели.
	 * 
	 * @param models - список моделей
	 * @param detailKey - имя поля со списком
	 */
	protected <T extends BaseModel> void appendModels(List<T> models, String detailKey) {
		// берём уже существующие элементы
		List<T> tmp = getListProp(detailKey);
		// если их нет, то создаём пустой список
		if (tmp == null) {
			tmp = new ArrayList<T>();
			set(detailKey, tmp);
		}
		// добавляем новые элементы, если они ещё не добавлены
		for (T model : models)
			if (!tmp.contains(model))
				tmp.add(model);
	}

	/**
	 * Создание точной копии модели со всеми вложенными моделями. GWT плохо дружит с Object.clone, поэтому делаем всё вручную.
	 * 
	 * @return копия модели
	 */
	public <T extends BaseModel> T copy() {
		return copy(true);
	}

	/**
	 * Создание точной копии модели со всеми вложенными моделями. GWT плохо дружит с Object.clone, поэтому делаем всё вручную.
	 * 
	 * @param recursive - если этот параметр равен <code>false</code>, то вложенные модели копироваться не будут, а в текущую модель будет записана ссылка на оригинал
	 * @return копия модели
	 */
	public <T extends BaseModel> T copy(boolean recursive) {
		return (recursive ? this.<T> copy(new HashMap<BaseModel, BaseModel>()) : this.<T> copy(null));
	}

	/**
	 * Создание точной копии модели. GWT плохо дружит с Object.clone, поэтому делаем всё вручную.
	 * 
	 * @param clones
	 * 			  - карта уже скопированных моделей, исключает вероятность зацикливания при копировании вложенных моделей с цикличными ссылками.
	 * 				Если этот параметр равен <code>null</code>, то вложенные модели копироваться не будут, а в текущую модель будут записаны ссылки на них.
	 * @return копия модели
	 */
	private <T extends BaseModel> T copy(Map<BaseModel, BaseModel> clones) {
		String className = ((getClassName() != null) && !getClassName().isEmpty()) ? getClassName() : getClass().getName();
		if ((className == null) || className.isEmpty())
			return null;
		T ret = null;
		if (refl != null)
			ret = refl.instantiate(className);
		if (ret == null)
			return null;

		if (clones != null) {
			clones.put(this, ret);
			for (Entry<String, Object> entry : entrySet())
				ret.set(entry.getKey(), cloneValue(clones, entry.getValue()));
		} else
			ret.fill(this);
		return ret;
	}

	private Object cloneValue(Map<BaseModel, BaseModel> clones, Object obj) {
		if ((obj != null) && (obj instanceof BaseModel)) {
			BaseModel clone = clones.get(obj);
			if (clone == null)
				clone = ((BaseModel) obj).copy(clones);
			return clone;
		} else if ((obj != null) && ((obj instanceof List) || (obj instanceof Set))) {
			Collection<Object> clone = (obj instanceof List) ? new ArrayList<Object>() : new HashSet<Object>();
			for (Object o : (Collection<?>) obj)
				clone.add(cloneValue(clones, o));
			return clone;
		} else
			return obj;
	}

	/**
	 * @return корневой пакет всех моделей данных
	 */
	public static String getRootPackage() {
		return rootPackage;
	}

	/**
	 * @return полное имя класса модели, если оно было сокращено до корневого пакета
	 */
	public static String getFullClassName(String className) {
		return ((className != null) && className.startsWith("rt.")) ? rootPackage + className.substring(2) : className;
	}

	public static Reflection getRefl() {
		return refl;
	}
	public static void setRefl(Reflection refl) {
		BaseModel.refl = refl;
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	@Override
	public int hashCode() {
        int h = 0;
        for (Entry<String, Object> entry : entrySet())
        	if (!(entry.getValue() instanceof List) && !(entry.getValue() instanceof BaseModel))
        		h += entry.hashCode();
        return h;
	}

}