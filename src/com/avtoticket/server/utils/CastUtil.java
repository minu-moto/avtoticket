/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.server.utils;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.Property;

import org.postgresql.util.PGobject;

import com.avtoticket.server.db.DB;
import com.avtoticket.server.utils.ModelMappingUtil.LinkedDef;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.ImageFile;
import com.avtoticket.shared.models.core.RepositoryItem;

/**
 * Нетривиальные функции преобразования одних типов данных в другие.
 * 
 * @author Minu <<a href='minu-moto@mail.ru'>minu-moto@mail.ru</a>>
 * @since 21.02.2012 11:11:10
 */
public class CastUtil {

	//private static Logger logger = LoggerFactory.getLogger(CastUtil.class.getName());

	/**
	 * Кастование типов PostgreSQL -> Java
	 *
	 * @param obj - исходный объект
	 * @return 
	 * @throws Exception
	 */
	private static Object cast(Object obj) throws Exception {
		if (obj instanceof PGobject) {
			String type = ((PGobject) obj).getType();
			if ("json".equalsIgnoreCase(type))
				return /*SerializerUtil.deserialize(((PGobject) obj).getValue())*/null;		// TODO
			else {
				Class<?> enumClass = ModelMappingUtil.getEnumType(type);
				if (enumClass != null)
					return enumClass.getMethod("valueOf", String.class).invoke(null, obj.toString());
			}
		} else /*if (obj instanceof Jdbc4Array)
			return new ArrayList<Long>(Arrays.asList((Long[]) ((Jdbc4Array) obj).getArray()));
		else*/ if (obj instanceof Timestamp)
			return new Date(((Timestamp) obj).getTime());
		else if (obj instanceof java.sql.Date)
			return new Date(((Date) obj).getTime());
		else if (obj instanceof UUID)
			return new com.avtoticket.shared.models.UUID(((UUID) obj).toString());
		return obj;
	}

	/**
	 * Преобразование некоторых специфических постгресовских типов в нативные типы явы
	 * 
	 * @param rset
	 *            - набор данных
	 * @param type
	 *            - тип результата
	 * @param column
	 *            - номер колонки в наборе данных
	 * @return скастованное значение
	 * @throws Exception
	 */
	public static Object castPostgreTypes(ResultSet rset, int type, int column) throws Exception {
		if (type != Types.NULL)
			return cast(rset.getObject(column));
		else
			return null;
	}

	/**
	 * Преобразование некоторых специфических постгресовских типов в нативные типы явы
	 * 
	 * @param stmt
	 *            - хранимая процедура, результат выполнения которой надо получить
	 * @param type
	 *            - тип результата
	 * @param column
	 *            - номер колонки с результатом
	 * @return скастованное значение
	 * @throws Exception
	 */
	public static Object castPostgreTypes(CallableStatement stmt, int type, int column) throws Exception {
		if (type != Types.NULL)
			return cast(stmt.getObject(column));
		else
			return null;
	}

	public static <T extends BaseModel> List<T> buildModels(Class<T> clazz, ResultSet rset) throws Exception {
		if ((rset != null) && (!rset.isClosed())) {
			ResultSetMetaData md = rset.getMetaData();
			int count = md.getColumnCount();
			List<T> models = new ArrayList<T>();
			while (rset.next()) {
				T model = clazz.newInstance();
				for (int i = 1; i <= count; i++) {
					Object val = castPostgreTypes(rset, md.getColumnType(i), i);
					if (val != null)
						model.set(md.getColumnName(i).toLowerCase(Locale.getDefault()), val);
				}
				models.add(model);
			}
			return models;
		} else
			//throw new Exception("ResultSet is closed!!!");
			return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends BaseModel> void attachSubModels(List<T> models, List<LinkedDef> masters, List<LinkedDef> details) throws Exception {
		if (((models != null) && !models.isEmpty())
				&& (((masters != null) && !masters.isEmpty())
				|| ((details != null) && !details.isEmpty()))) {
			Map<LinkedDef, Map<Long, BaseModel>> masModels = new HashMap<>();
			Map<LinkedDef, Map<Long, List<BaseModel>>> detModels = new HashMap<>();
			for (T model : models) {
				// группируем идентификаторы главных моделей по классам
				if (masters != null)
					for (LinkedDef master : masters) {
						Long id = model.getLongProp(master.getIdKey());
						if (id != null) {
							Map<Long, BaseModel> ids = masModels.get(master);
							if (ids == null) {
								ids = new HashMap<Long, BaseModel>();
								masModels.put(master, ids);
							}
							ids.put(id, null);
						}
					}

				// группируем идентификаторы подчинённых моделей по описаниям (класс;поле_внешнего_ключа)
				if (details != null)
					for (LinkedDef detail : details) {
						Long id = model.getId();
						if (id != null) {
							Map<Long, List<BaseModel>> ids = detModels.get(detail);
							if (ids == null) {
								ids = new HashMap<Long, List<BaseModel>>();
								detModels.put(detail, ids);
							}
							ids.put(id, null);
						}
					}
			}

			if (masters != null) {
				// извлекаем главные модели пачками
				for (Entry<LinkedDef, Map<Long, BaseModel>> item : masModels.entrySet()) {
					Map<Long, BaseModel> val = item.getValue();
					if (val != null) {
						LinkedDef master = item.getKey();
						if (val.size() != 1) {
							List<BaseModel> mdls = DB.getModels((Class<BaseModel>) master.getModelClass(), val.keySet(), master.isAttDetails());
							for (BaseModel model : mdls)
								val.put(model.getId(), model);
						} else {
							BaseModel bm = DB.getModel((Class<BaseModel>) master.getModelClass(), val.keySet().iterator().next(), master.isAttDetails());
							if (bm != null)
								val.put(bm.getId(), bm);
						}
					}
				}
				// прикрепляем главные модели к результату
				masters.forEach(master ->
					models.forEach(model -> {
						Long id = model.getLongProp(master.getIdKey());
						if (id != null)
							model.set(master.getFieldName(), masModels.get(master).get(id));
					}));
			}

			if (details != null) {
				// извлекаем подчинённые модели пачками
				for (Entry<LinkedDef, Map<Long, List<BaseModel>>> item : detModels.entrySet()) {
					Map<Long, List<BaseModel>> val = item.getValue();
					if (val != null) {
						String idKey = item.getKey().getIdKey();
						Where where = Where.equals(idKey, (val.size() != 1) ? val.keySet() : val.keySet().iterator().next());
						List<BaseModel> mdls = DB.getModels(
								(Class<BaseModel>) item.getKey().getModelClass(), where, idKey, item.getKey().isAttDetails(), false);
						// разрезаем полученный кусок на отрезки с равными detail.idKey
						int i = 0;
						int j = 0;
						Long id = null;
						for (; j < mdls.size(); j++) {
							BaseModel mdl = mdls.get(j);
							if (!mdl.getLongProp(idKey).equals(id)) {
								if (i < j)
									val.put(id, new ArrayList<BaseModel>(mdls.subList(i, j)));
								id = mdl.getLongProp(idKey);
								i = j;
							}
						}
						if (i < j)
							if (i == 0)
								val.put(id, mdls);
							else
								val.put(id, new ArrayList<BaseModel>(mdls.subList(i, j)));
					}
				}
				// прикрепляем подчинённые модели к результату
				for (LinkedDef detail : details) {
					Map<Long, List<BaseModel>> mdls = detModels.get(detail);
					for (BaseModel model : models) {
						Long id = model.getId();
						if (id != null)
							model.set(detail.getFieldName(), mdls.get(id));
					}
				}
			}
		}
	}

	/**
	 * Перегоняет элемент репозитория в привычную всем модель
	 * 
	 * @param node
	 *            - элемент репозитория
	 * @return модель, соответствующая элементу репозитория
	 * @throws Exception
	 */
	public static <T extends RepositoryItem> T node2model(T ret, Node node) throws Exception {
		if (node == null)
			return null;

		ret.setIdent(node.getIdentifier());
		ret.setName(node.getName());
		ret.setPath(node.getPath());
		ret.setType(node.getPrimaryNodeType().getName());
		if (node.hasProperty(Property.JCR_TITLE))
			ret.setTitle(node.getProperty(Property.JCR_TITLE).getString());
		if (node.hasProperty(RepositoryItem.SIZE))
			ret.setSize(node.getProperty(RepositoryItem.SIZE).getLong());
		if (node.hasProperty(Property.JCR_DESCRIPTION))
			ret.setDescription(node.getProperty(Property.JCR_DESCRIPTION).getString());
		if (node.hasProperty(Property.JCR_CREATED))
			ret.setDateCreate(node.getProperty(Property.JCR_CREATED).getDate().getTime());
//		Date lm = JCRUtil.getLastModified(node);
//		if (lm != null)
//			ret.setDateUpdate(lm);

		if (ret instanceof ImageFile) {
			if (node.hasProperty(ImageFile.WIDTH))
				((ImageFile) ret).setWidth(node.getProperty(ImageFile.WIDTH).getLong());
			if (node.hasProperty(ImageFile.HEIGHT))
				((ImageFile) ret).setHeight(node.getProperty(ImageFile.HEIGHT).getLong());
		}

		return ret;
	}

}