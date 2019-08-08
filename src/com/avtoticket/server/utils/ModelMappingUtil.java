/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.server.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Detail;
import com.avtoticket.shared.models.EnumType;
import com.avtoticket.shared.models.Master;
import com.avtoticket.shared.models.PrivateTableField;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.View;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 25.09.2012 15:18:30
 */
public class ModelMappingUtil {

	private static Logger logger = LoggerFactory.getLogger(ModelMappingUtil.class.getName());

	/**
	 * Описание связанной модели
	 */
	public static class LinkedDef {
		/** Имя поля, в котором будет храниться связанная модель */
		private String fieldName;
		/** Класс главной модели */
		private Class<? extends BaseModel> modelClass;
		/** Имя поля, в котором хранится идентификатор связанной модели */
		private String idKey;
		/** Цеплять вложенные модели или нет */
		private Boolean attDetails;

		/**
		 * @return имя поля, в котором будет храниться связанная модель
		 */
		public String getFieldName() {
			return fieldName;
		}

		/**
		 * @return класс главной модели
		 */
		public Class<? extends BaseModel> getModelClass() {
			return modelClass;
		}

		/**
		 * @return имя поля, в котором хранится идентификатор связанной модели
		 */
		public String getIdKey() {
			return idKey;
		}

		/**
		 * @return цеплять вложенные модели или нет
		 */
		public Boolean isAttDetails() {
			return attDetails;
		}
	}

	/**
	 * Описание таблицы БД с которой связана модель
	 */
	public static class TableDef {
		/** Имя таблицы, с которой связана модель */
		public String tableName;
		/** Имя вьюхи, с которой связана модель */
		public String viewName;
		/** Список полей */
		public List<String> fields;
		/** Список главных моделей */
		public List<LinkedDef> masters;
		/** Список зависимых моделей */
		public List<LinkedDef> details;

//		public String getIdKey(String fieldName) {
//			for (MasterDef def : masters)
//				if (def.fieldName.equalsIgnoreCase(fieldName))
//					return def.idKey;
//			return null;
//		}
	}

	private static Map<String, TableDef> mappingCache;
	private static Map<String, Class<?>> enumCache;

	@SuppressWarnings("unchecked")
	private static void readFields(Class<?> clazz, List<String> fields, List<LinkedDef> masters, List<LinkedDef> details, boolean readPrivate) {
		for (Field f : clazz.getDeclaredFields()) {
			if (f.isAnnotationPresent(TableField.class) || (readPrivate && f.isAnnotationPresent(PrivateTableField.class))) {
				f.setAccessible(true);
				Object value = null;
				try {
					value = f.get(clazz);
				} catch (Exception e) {
					logger.error("", e);
				}
				if (value != null)
					fields.add(value.toString());
			}
			if (f.isAnnotationPresent(Master.class)) {
				f.setAccessible(true);
				LinkedDef master = null;
				try {
					Master mas = f.getAnnotation(Master.class);
					master = new LinkedDef();
					master.fieldName = f.get(clazz).toString();
					master.modelClass = (Class<? extends BaseModel>) mas.clazz();
					master.idKey = mas.key();
					master.attDetails = mas.attDetails();
				} catch (Exception e) {
					logger.error("", e);
				}
				if (master != null)
					masters.add(master);
			}
			if (f.isAnnotationPresent(Detail.class)) {
				f.setAccessible(true);
				LinkedDef detail = null;
				try {
					Detail det = f.getAnnotation(Detail.class);
					detail = new LinkedDef();
					detail.fieldName = f.get(clazz).toString();
					detail.modelClass = (Class<? extends BaseModel>) det.clazz();
					detail.idKey = det.key();
					detail.attDetails = det.attDetails();
				} catch (Exception e) {
					logger.error("", e);
				}
				if (detail != null)
					details.add(detail);
			}
		}
	}
	
	/**
	 * Получить описание таблицы БД, с которой связан класс модели
	 * 
	 * @param clazz
	 *            - класс модели
	 * @return объект с именем таблицы, именем вьюхи и описанием полей модели
	 *         данных
	 * @throws Exception
	 */
	public static <T extends BaseModel> TableDef getTableDef(Class<T> clazz) {
		TableDef ret = (mappingCache != null) ? mappingCache.get(clazz.getName()) : null;
		if (ret == null) {
			ret = new TableDef();
			Table ta = clazz.getAnnotation(Table.class);
			View va = clazz.getAnnotation(View.class);
			if ((ta != null) || (va != null)) {
				if (va != null)
					ret.viewName = va.value();
				if (ta != null)
					ret.tableName = ta.value();
				ret.fields = new ArrayList<String>();
				ret.masters = new ArrayList<LinkedDef>();
				ret.details = new ArrayList<LinkedDef>();
				Class<? super T> superClass = clazz;
				readFields(superClass, ret.fields, ret.masters, ret.details, true);
				while (!(superClass = superClass.getSuperclass()).equals(BaseModel.class))
					readFields(superClass, ret.fields, ret.masters, ret.details, false);
			}
			if (mappingCache != null)
				mappingCache.put(clazz.getName(), ret);
		}
		return ret;
	}

	/**
	 * Получить название таблицы в БД
	 *
	 * @param clazz
	 *            - класс модели
	 * @return название таблицы в БД, соответствующей модели данных
	 * @throws Exception
	 */
	public static <T extends BaseModel> String getTableName(Class<T> clazz) throws Exception {
		TableDef pers = getTableDef(clazz);
		if (pers.tableName != null)
			return pers.tableName;
		else
			throw new Exception("Не найдена таблица модели " + clazz.getName());
	}

	/**
	 * Получить название вьюхи в БД
	 *
	 * @param clazz
	 *            - класс модели
	 * @return название вьюхи в БД, соответствующей модели данных
	 * @throws Exception
	 */
	public static <T extends BaseModel> String getViewName(Class<T> clazz) throws Exception {
		TableDef pers = getTableDef(clazz);
		if (pers.viewName != null)
			return pers.viewName;
		else
			throw new Exception("Не найдена вьюха модели " + clazz.getName());
	}

	/**
	 * Получить класс перечислимого типа по его имени в базе
	 * 
	 * @param name - имя перечислимого типа в БД
	 * @return класс перечислимого типа
	 */
	public static Class<?> getEnumType(String name) {
		return (enumCache != null) ? enumCache.get(name) : null;
	}

	public static String getEnumTypeName(Class<?> clazz) {
		EnumType ea = clazz.getAnnotation(EnumType.class);
		return (ea != null) ? ea.value() : null;
	}

	public static void init() {
		mappingCache = new HashMap<String, TableDef>();
		enumCache = new HashMap<String, Class<?>>();

		Set<Class<?>> enums = new Reflections(BaseModel.getRootPackage()).getTypesAnnotatedWith(EnumType.class);
		enums.forEach(clazz -> {
			String name = getEnumTypeName(clazz);
			int idx = name.indexOf('.');
			enumCache.put(name, clazz);
			if (idx < 0)
				enumCache.put("\"" + name + "\"", clazz);
			else
				enumCache.put("\"" + name.substring(0, idx) + "\".\"" + name.substring(idx + 1) + "\"", clazz);
		});
	}

}