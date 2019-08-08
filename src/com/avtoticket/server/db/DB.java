/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.server.db;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.postgresql.util.PGobject;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.stored.StoredProcs;
import com.avtoticket.server.utils.CastUtil;
import com.avtoticket.server.utils.CommonServerUtils;
import com.avtoticket.server.utils.ModelMappingUtil;
import com.avtoticket.server.utils.ModelMappingUtil.TableDef;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.PageContainer;
import com.avtoticket.shared.models.StoredProc;
import com.avtoticket.shared.models.UUID;
import com.avtoticket.shared.models.Where;
import com.google.common.base.CaseFormat;
import com.google.gwt.user.client.rpc.SerializationException;

/**
 * @author Minu <<a href='minu-moto@mail.ru'>minu-moto@mail.ru</a>>
 * @since 14.02.2012 10:31:15
 */
public class DB {

	private static Logger logger = LoggerFactory.getLogger(DB.class.getName());

	private static final Long funcTimeout = logger.isDebugEnabled() ? 100L : 1000L;

	/**
	 * Контейнер для результатов запросов
	 */
	private static class ResultContainer<T> {

		private T content = null;

		public T getContent() {
			return content;
		}

		public void setContent(T content) {
			this.content = content;
		}

	}

	/**
	 * Обратный вызов от исполнителя запросов
	 */
	private interface QueryCallback<T> {

		public void onEnd(T ret) throws Exception;

	}

	/**
	 * Формирует на основе эксепшена от БД новый эксепшен с содержательным описанием ошибки
	 * 
	 * @param e - эксепшен, возникший при обращении БД
	 * @throws Exception
	 */
	private static void formatUserMessage(PSQLException e) throws Exception {
		if ((e.getMessage() != null) && e.getMessage().contains("tsupplier_prices_slave_uniq_idx"))
			throw new SerializationException("Такой поставщик материала уже существует!");
		else if ((e.getMessage() != null) && e.getMessage().contains("tparents_slave_uniq_idx"))
			throw new SerializationException("Такой родитель уже существует!");
		else if ((e.getMessage() != null) && e.getMessage().contains("tconstruct_elements_uniq_idx"))
			throw new SerializationException("Такой элемент конструирования уже существует!");
		else if ((e.getMessage() != null) && e.getMessage().contains("telements_uniq_idx"))
			throw new SerializationException("Такой элемент уже существует!");
		else if ((e.getMessage() != null) && e.getMessage().contains("tconstructs_uniq_idx"))
			throw new SerializationException("Такой конструктив уже существует!");
		else if ((e.getMessage() != null) && e.getMessage().contains("tuserroles_uniq_idx"))
			throw new SerializationException("У пользователя уже есть эта роль!");
		else if ((e.getMessage() != null) && e.getMessage().contains("tusers_login_uniq_idx"))
			throw new SerializationException("Пользователь с таким логином уже существует!");
		else if ((e.getMessage() != null) && e.getMessage().contains("tdocument_requests_invoice_material_uniq_idx"))
			throw new SerializationException("Такой материал уже существует!");
		else if ((e.getMessage() != null) && e.getMessage().contains("troles_uniq_sign_idx"))
			throw new SerializationException("Метка роли должна быть уникальна!");
		else if ((e.getMessage() != null) && e.getMessage().contains("\"tuser_id\" нарушает ограничение NOT NULL"))
			throw new SerializationException("Необходимо указать пользователя!");
		else if ((e.getMessage() != null) && e.getMessage().contains("\"trole_id\" нарушает ограничение NOT NULL"))
			throw new SerializationException("Необходимо указать роль!");
		else if ((e.getMessage() != null) && e.getMessage().contains("tparent_source_pkey"))
			throw new SerializationException("Родительский элемент уже существует");
		else if ((e.getMessage() != null) && e.getMessage().contains("tconnections_uniq_idx"))
			throw new SerializationException("Соединение уже существует");
		else if ((e.getMessage() != null) && e.getMessage().contains("trole_modules_uniq_idx"))
			throw new SerializationException("У роли уже есть этот модуль!");
		else if ((e.getMessage() != null) && e.getMessage().contains("Ошибка при обращении к БД\n  Подсказка: "))
			throw new SerializationException(e.getMessage().substring(e.getMessage().indexOf("Подсказка:") + 11));
	}

	/**
	 * Заполнить запрос значениями параметров
	 * 
	 * @param con
	 *            - подключение к БД
	 * @param stmt
	 *            - препарированный запрос
	 * @param params
	 *            - список параметров
	 * @param type
	 *            - тип возвращаемого значения
	 * @param retColumn
	 *            - номер параметра с результатом выполнения запроса
	 * @throws SQLException
	 */
	private static void fillParams(Connection con, PreparedStatement stmt,
			Object[] params, int type, int retColumn) throws SQLException {
		boolean isSQLQuery = !(stmt instanceof CallableStatement);
		if ((type != Types.NULL) && (retColumn != 0) && !isSQLQuery)
			((CallableStatement) stmt).registerOutParameter(retColumn, type);
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				Object o = params[i];
				int col = i + 1;
				col += ((retColumn == 0) || (col < retColumn) || isSQLQuery) ? 0 : 1;
				if (o == null)
					stmt.setObject(col, null);
				if (o instanceof Collection)
					stmt.setArray(col, (con.getMetaData()
							.getConnection()).createArrayOf("bigint", ((Collection<?>) o).toArray()));
				else if (o instanceof Date)
					stmt.setTimestamp(col, new java.sql.Timestamp(((Date) o).getTime()));
				else if (o instanceof InputStream)
					stmt.setBinaryStream(col, (InputStream) o);
				else if (o instanceof Enum) {
					PGobject enumObject = new PGobject();
					String t = ModelMappingUtil.getEnumTypeName(o.getClass());
					enumObject.setType(t);
					enumObject.setValue(((Enum<?>) o).name());
					stmt.setObject(col, enumObject);
				} else if (o instanceof UUID) {
					java.util.UUID uuid = java.util.UUID.fromString(o.toString());
					stmt.setObject(col, uuid);
				} else if (o instanceof BaseModel) {
					PGobject json = new PGobject();
					json.setType("json");
					json.setValue(/*SerializerUtil.serialize(o).toString()*/null);	// TODO
					stmt.setObject(col, json);
				} else
					stmt.setObject(col, o);
			}
		}
	}

	/**
	 * Принимает все необходимые данные, выполняет запрос, возвращает результат.
	 * При этом замеряется время выполнения запроса и извлечения результатов.
	 * Это самая низкоуровневая функция выполнения запросов, все остальные функции 
	 * так или иначе работают через неё.
	 * 
	 * @param type
	 *            - тип получаемого значения
	 * @param func
	 *            - вызываемая функция
	 * @param params
	 *            - список параметров функции, если есть
	 * @param retColumn
	 *            - номер параметра с результатом выполнения функции
	 * @param limit
	 *            - максимальное количество получаемых объектов (null - не ограниченно)
	 * @param callback
	 *            - внешняя функция извлечения результатов
	 * @return результат выполнения функции
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static <T> void queryCore(int type, String sql, boolean isFunc, Object[] params,
			int retColumn, Integer limit, QueryCallback<T> callback)
			throws Exception {
		Date std = new Date();
		Connection con = ConnectionUtil.getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = isFunc ? con.prepareCall(sql) : con.prepareStatement(sql);
			fillParams(con, stmt, params, type, retColumn);
			if (limit != null)
				stmt.setMaxRows(limit);
			stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
			stmt.setFetchSize(128);
			stmt.execute();
			Long tm = new Date().getTime() - std.getTime();
			if (tm > funcTimeout)
				logger.warn("sql   " + tm + " ms " + sql + " with params ["
						+ CommonServerUtils.joinToStr(sql, params) + "]");
			std = new Date();

			if (isFunc)
				callback.onEnd((T) CastUtil.castPostgreTypes((CallableStatement) stmt, type, retColumn));
			else if (type == Types.OTHER) {
				rs = stmt.getResultSet();
				callback.onEnd((T) rs);
			} else {
				rs = stmt.getResultSet();
				if ((rs != null) && rs.next())
					callback.onEnd((T) CastUtil.castPostgreTypes(rs, type, 1));
				else
					callback.onEnd((T) null);
			}

			tm = new Date().getTime() - std.getTime();
			if (tm > funcTimeout)
				logger.warn("fetch " + tm + " ms " + sql + " with params ["
						+ CommonServerUtils.joinToStr(sql, params) + "]");
		} finally {
			ConnectionUtil.releaseResources(rs, stmt, con);
		}
	}

	/**
	 * Получить результат выполнения функции
	 * 
	 * @param type
	 *            - тип получаемого значения
	 * @param sql
	 *            - запрос
	 * @param isFunc
	 * 			  - true - если в запросе вызывается хранимая процедура
	 * @param params
	 *            - список параметров функции, если есть
	 * @param retColumn
	 *            - номер параметра с результатом выполнения функции
	 * @param limit
	 *            - максимальное количество получаемых объектов (null - не
	 *            ограниченно)
	 * @return результат выполнения функции
	 * @throws Exception
	 */
	private static <T> T getValue(int type, String sql, boolean isFunc, Object[] params,
			int retColumn, Integer limit) throws Exception {
		final ResultContainer<T> result = new ResultContainer<T>();
		try {
			queryCore(type, sql, isFunc, params, retColumn, limit, new QueryCallback<T>() {
				@Override
				public void onEnd(T ret) throws Exception {
					result.setContent(ret);
				};
			});
		} catch (PSQLException e) {
			formatUserMessage(e);
			logger.error(sql + " with params [" + CommonServerUtils.joinToStr(sql, params) + "]", e);
			throw new Exception("Ошибка при обращении к БД");
		} catch (Exception e) {
			logger.error(sql + " with params [" + CommonServerUtils.joinToStr(sql, params) + "]", e);
			throw new Exception("Ошибка при обращении к БД");
		}
		return result.getContent();
	}

	/**
	 * Получить результат выполнения функции в виде списка моделей
	 * 
	 * @param clazz
	 *            - класс модели данных
	 * @param func
	 *            - функция
	 * @param params
	 *            - список параметров процедуры, если есть
	 * @param limit
	 *            - максимальное количество получаемых объектов (null - не
	 *            ограниченно)
	 * @param attDetails
	 *            - подтягивать главные и зависимые модели или нет
	 * @return результат выполнения хранимой процедуры
	 * @throws Exception
	 */
	private static <T extends BaseModel> List<T> getModels(final Class<T> clazz,
			String sql, boolean isFunc, Object[] params, Integer limit, final Boolean attDetails)
			throws Exception {
		final ResultContainer<List<T>> result = new ResultContainer<List<T>>();
		try {
			queryCore(Types.OTHER, sql, isFunc, params, 1, limit, new QueryCallback<ResultSet>() {
				@Override
				public void onEnd(ResultSet ret) throws Exception {
					try {
						result.setContent(CastUtil.buildModels(clazz, ret));
					} catch (Exception e) {
						logger.error("Возникла ошибка при получении моделей из базы.", e);
						throw new Exception("Возникла ошибка при получении моделей из базы.");
					} finally {
						if ((ret != null) && !ret.isClosed())
							ret.close();
					}
				};
			});
			if (attDetails) {
				TableDef td = ModelMappingUtil.getTableDef(clazz);
				CastUtil.attachSubModels(result.getContent(), td.masters, td.details);
			}
		} catch (PSQLException e) {
			formatUserMessage(e);
			logger.error(sql + " with params [" + CommonServerUtils.joinToStr(sql, params) + "]", e);
			throw new Exception("Ошибка при обращении к БД");
		} catch (Exception e) {
			logger.error(sql + " with params [" + CommonServerUtils.joinToStr(sql, params) + "]", e);
			throw new Exception("Ошибка при обращении к БД");
		}
		return result.getContent();
	}

	/**
	 * Получить из БД объекты, возвращаемые SQL запросом
	 * 
	 * @param clazz
	 *            - класс модели данных
	 * @param sql
	 *            - SQL запрос
	 * @param params
	 *            - список параметров запроса, если есть
	 * @param attDetails
	 *            - подтягивать зависимые модели или нет
	 * @return результат запроса в виде списка моделей
	 * @throws Exception
	 */
	private static <T extends BaseModel> List<T> getModelsSQL(Class<T> clazz,
			String sql, Object[] params, Boolean attDetails) throws Exception {
		return getModels(clazz, sql, false, params, null, attDetails);
	}

	/**
	 * Получить из БД объекты, возвращаемые хранимой процедурой
	 * 
	 * @param clazz
	 *            - класс модели данных
	 * @param func
	 *            - прототип хранимой процедуры/функции
	 * @param params
	 *            - список параметров процедуры, если есть
	 * @param limit
	 *            - максимальное количество получаемых объектов (null - не
	 *            ограниченно)
	 * @param attDetails
	 *            - подтягивать зависимые модели или нет
	 * @return результат выполнения хранимой процедуры в виде списка моделей
	 * @throws Exception
	 */
	private static <T extends BaseModel> List<T> getModelsFunc(Class<T> clazz,
			String func, Object[] params, Integer limit, Boolean attDetails) throws Exception {
		return getModels(clazz, "{ ? = call " + func + " }", true, params, limit, attDetails);
	}



	/* ==== Различные обёртки для получения и записи данных =========================== */

	/**
	 * Получить из БД объект по его идентификатору
	 * 
	 * @param clazz
	 *            - класс модели данных
	 * @param id
	 *            - идентификатор записи
	 * @return результат запроса в виде модели данных
	 * @throws Exception
	 */
	public static <T extends BaseModel> T getModel(Class<T> clazz, Long id) throws Exception {
		return getModel(clazz, id, true);
	}

	/**
	 * Получить из БД объект по его идентификатору
	 * 
	 * @param clazz
	 *            - класс модели данных
	 * @param id
	 *            - идентификатор записи
	 * @param attDetails 
	 * 			  - подтягивать зависимые модели или нет
	 * @return результат запроса в виде модели данных
	 * @throws Exception
	 */
	public static <T extends BaseModel> T getModel(Class<T> clazz, Long id, Boolean attDetails) throws Exception {
		if (id != null) {
			String sql = new StringBuilder().append("select * from ").append(ModelMappingUtil.getViewName(clazz))
					.append(" where ").append(BaseModel.ID).append("=?").toString();
			List<T> ret = getModelsSQL(clazz, sql, new Object[] { id }, attDetails);
			return ((ret != null) && !ret.isEmpty()) ? ret.get(0) : null;
		} else
			return null;
	}

	/**
	 * Получить из БД объекты, содержащие только поля DISPLAY_FIELD и VALUE_FIELD
	 * 
	 * @param clazz
	 *            - класс модели данных
	 * @param extFields
	 * 			  - список дополнительных полей
	 * @return результат запроса в виде списка DISPLAY_FIELD;VALUE_FIELD
	 * @throws Exception
	 */
	public static <T extends BaseModel> List<T> getKeyValueList(Class<T> clazz, String[] extFields, Where where, String[] orderFields) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(BaseModel.DISPLAY_FIELD).append(", ").append(BaseModel.VALUE_FIELD);
		if (extFields != null)
			sb.append(", ").append(CommonServerUtils.joinToStr(extFields));
		List<Object> params = new ArrayList<Object>();
		sb.append(" from ").append(ModelMappingUtil.getViewName(clazz));
		if (where != null)
			sb.append(where.compile(null, params));
		sb.append(" order by ");
		if (orderFields != null)
			sb.append(CommonServerUtils.joinToStr(orderFields));
		else
			sb.append(BaseModel.DISPLAY_FIELD).append(", ").append(BaseModel.VALUE_FIELD);
		return getModelsSQL(clazz, sb.toString(), params.toArray(), false);
	}

	/**
	 * Получить из БД объекты по их идентификаторам
	 * 
	 * @param clazz
	 *            - класс модели данных
	 * @param ids
	 *            - список идентификаторов нужных записей, если равен null, то
	 *            возвращаются все записи
	 * @param attDetails 
	 * 			  - подтягивать зависимые модели или нет
	 * @return результат запроса в виде списка моделей
	 * @throws Exception
	 */
	public static <T extends BaseModel> List<T> getModels(Class<T> clazz, Collection<?> ids, Boolean attDetails) throws Exception {
		if (ids != null)
			if (ids.isEmpty())
				return null;
			else
				return getModelsSQL(clazz,
						"select * from " + ModelMappingUtil.getViewName(clazz) + " where " + BaseModel.ID + " = ANY(?)",
						new Object[] { ids }, attDetails);
		else
			return getModelsSQL(clazz,
					"select * from " + ModelMappingUtil.getViewName(clazz), null, attDetails);
	}

	/**
	 * Получить из БД объекты по их идентификаторам
	 * 
	 * @param clazz
	 *            - класс модели данных
	 * @param ids
	 *            - список идентификаторов нужных записей, если равен null, то
	 *            возвращаются все записи
	 * @return результат запроса в виде списка моделей
	 * @throws Exception
	 */
	public static <T extends BaseModel> List<T> getModels(Class<T> clazz, Collection<?> ids) throws Exception {
		return getModels(clazz, ids, true);
	}

	public static <T extends BaseModel> T getModel(Class<T> clazz, Where where) throws Exception {
		List<T> ret = getModels(clazz, where, false);
		return ((ret != null) && !ret.isEmpty()) ? ret.get(0) : null;
	}

	public static <T extends BaseModel> List<T> getModels(Class<T> clazz, Where where, String sortColumns) throws Exception {
		return getModels(clazz, where, sortColumns, true, false);
	}

	public static <T extends BaseModel> List<T> getModels(Class<T> clazz, Where where, Boolean only) throws Exception {
		return getModels(clazz, where, null, true, only);
	}

	public static <T extends BaseModel> List<T> getModels(Class<T> clazz, Where where, String sortColumns, Boolean attDetails, Boolean only) throws Exception {
		if (where != null) {
			List<Object> params = new ArrayList<Object>();
			if (only)
				return getModelsSQL(clazz, "select * from only " + ModelMappingUtil.getTableName(clazz)
						+ where.compile(sortColumns, params), params.toArray(), attDetails);
			else
				return getModelsSQL(clazz, "select * from " + ModelMappingUtil.getViewName(clazz)
							+ where.compile(sortColumns, params), params.toArray(), attDetails);
		} else
			return getModels(clazz, (List<?>) null, attDetails);
	}

	/**
	 * Получить из БД все объекты данного класса
	 * 
	 * @param clazz
	 *            - класс модели данных
	 * @return результат запроса в виде списка моделей
	 * @throws Exception
	 */
	public static <T extends BaseModel> List<T> getModels(Class<T> clazz) throws Exception {
		return getModels(clazz, (List<?>) null);
	}

	/**
	 * Получить из БД страницу с объектами данного класса
	 * 
	 * @param clazz
	 *            - класс модели данных
	 * @param where 
	 * 			  - условия запроса
	 * @param sortColumn
	 * 			  - столбец для сортировки
	 * @return результат запроса в виде списка моделей
	 * @throws Exception
	 */
	public static <T extends BaseModel> PageContainer<T> getPagedModels(Class<T> clazz, Where where, String sortColumn, Boolean attDetails) throws Exception {
		PageContainer<T> ret = new PageContainer<T>();
		String view = ModelMappingUtil.getViewName(clazz);
		// получаем общее количество объектов
		List<Object> params = new ArrayList<Object>();
		Long count = getValue(Types.NUMERIC, "select count(1) from " + view + ((where != null) ? where.compile(null, params, true, false) : ""), false, params.toArray(), 1, null);
		ret.setItemsCount(count.intValue());
//		if ((startIndex < 0) || ((count > 0) && (startIndex >= count)))
//			throw new SerializationException("Указанная страница выходит за границы допустимого диапазона");
		// получаем указанную страницу объектов
		if (where == null)
			where = new Where();
		params.clear();
		ret.setPage(getModelsSQL(clazz, "select * from " + view + where.compile(sortColumn, params), params.toArray(), attDetails));
		return ret;
	}

	/**
	 * Получить из БД страницу с объектами данного класса
	 * 
	 * @param clazz
	 *            - класс модели данных
	 * @param func
	 * 			  - имя табличной функции
	 * @param where 
	 * 			  - условия запроса
	 * @param sortColumn
	 * 			  - столбец для сортировки
	 * @param params
	 * 			  - параметры табличной функции
	 * @return результат запроса в виде списка моделей
	 * @throws Exception
	 */
	public static <T extends BaseModel> PageContainer<T> getPagedModels(Class<T> clazz, String func, Where where, String sortColumn, Object... params) throws Exception {
		PageContainer<T> ret = new PageContainer<T>();
		// получаем общее количество объектов
		List<Object> param = new ArrayList<Object>(Arrays.asList(params));
		Long count = getValue(Types.NUMERIC, "select count(1) from " + func + ((where != null) ? where.compile(null, param, true, false) : ""), false, param.toArray(), 1, null);
		ret.setItemsCount(count.intValue());
		// получаем указанную страницу объектов
		if (where == null)
			where = new Where();
		param = new ArrayList<Object>(Arrays.asList(params));
		ret.setPage(getModelsSQL(clazz, "select * from " + func + where.compile(sortColumn, param), param.toArray(), true));
		return ret;
	}

	/**
	 * Получить из БД объекты, возвращаемые хранимой процедурой
	 * 
	 * @param clazz
	 *            - класс модели данных
	 * @param func
	 *            - прототип хранимой функции
	 * @param params
	 *            - список параметров процедуры, если есть
	 * @return результат выполнения хранимой процедуры в виде списка моделей
	 * @throws Exception
	 */
	public static <T extends BaseModel> List<T> getModels(Class<T> clazz,
			String func, Object... params) throws Exception {
		return getModelsFunc(clazz, func, params, null, true);
	}

	/**
	 * Получить из БД объект, возвращаемый хранимой процедурой
	 * 
	 * @param clazz
	 *            - класс модели данных
	 * @param func
	 *            - прототип хранимой функции
	 * @param params
	 *            - список параметров процедуры, если есть
	 * @return результат выполнения хранимой процедуры в виде модели данных
	 * @throws Exception
	 */
	public static <T extends BaseModel> T getModel(Class<T> clazz, String func,
			Object... params) throws Exception {
		List<T> models = getModelsFunc(clazz, func, params, 1, true);
		return ((models != null) && !models.isEmpty()) ? models.get(0) : null;
	}

	/**
	 * Получить значение List из хранимки в БД
	 * 
	 * @param func
	 *            - прототип хранимой функции
	 * @param params
	 *            - список параметров функции, если есть
	 * @return результат запроса
	 * @throws Exception
	 */
	public static <T> List<T> getList(String func, Object... params) throws Exception {
		return getValue(Types.ARRAY, "{ ? = call " + func + " }", true, params, 1, 1);
	}

	/**
	 * Получить значение Boolean из хранимки в БД
	 * 
	 * @param func
	 *            - прототип хранимой функции
	 * @param params
	 *            - список параметров функции, если есть
	 * @return результат запроса
	 * @throws Exception
	 */
	public static Boolean getBoolean(String func, Object... params) throws Exception {
		return getValue(Types.BOOLEAN, "{ ? = call " + func + " }", true, params, 1, 1);
	}

	/**
	 * Получить значение Date из хранимки в БД
	 * 
	 * @param func
	 *            - прототип хранимой функции
	 * @param params
	 *            - список параметров функции, если есть
	 * @return результат запроса Date
	 * @throws Exception
	 */
	public static Date getDate(String func, Object... params) throws Exception {
		return getValue(Types.TIMESTAMP, "{ ? = call " + func + " }", true, params, 1, 1);
	}

	/**
	 * Получить значение Long из хранимки в БД
	 * 
	 * @param func
	 *            - прототип хранимой функции
	 * @param params
	 *            - список параметров функции, если есть
	 * @return результат запроса Long
	 * @throws Exception
	 */
	public static Long getLong(String func, Object... params) throws Exception {
		return getValue(Types.BIGINT, "{ ? = call " + func + " }", true, params, 1, 1);
	}

	/**
	 * Получить следующий по порядку номер записи
	 * 
	 * @param clazz
	 * @param where
	 * @return следующий по порядку номер записи
	 * @throws Exception
	 */
	public static <T extends BaseModel> Long getNextNpp(Class<T> clazz, Where where) throws Exception {
		List<Object> params = new ArrayList<Object>();
		Long ret = getValue(Types.BIGINT, "select max(npp) from " + ModelMappingUtil.getViewName(clazz)
				+ ((where != null) ? where.compile(null, params) : ""), false, params.toArray(), 0, 1);
		return ((ret != null) ? ret : 0) + 1;
	}

	/**
	 * Получить значение Double из хранимки в БД
	 * 
	 * @param func
	 *            - прототип хранимой функции
	 * @param params
	 *            - список параметров функции, если есть
	 * @return результат запроса Double
	 * @throws Exception
	 */
	public static Double getDouble(String func, Object... params) throws Exception {
		return getValue(Types.DOUBLE, "{ ? = call " + func + " }", true, params, 1, 1);
	}

	/**
	 * Получить значение String из хранимки в БД
	 * 
	 * @param func
	 *            - прототип хранимой функции
	 * @param params
	 *            - список параметров функции, если есть
	 * @return результат запроса String
	 * @throws Exception
	 */
	public static String getString(String func, Object... params) throws Exception {
		return getValue(Types.VARCHAR, "{ ? = call " + func + " }", true, params, 1, 1);
	}

	/**
	 * Получить значение Enum из хранимки в БД
	 * 
	 * @param func
	 *            - прототип хранимой функции
	 * @param params
	 *            - список параметров функции, если есть
	 * @return результат запроса Enum
	 * @throws Exception
	 */
	public static <T extends Enum<?>> T getEnum(String func, Object... params) throws Exception {
		return getValue(Types.OTHER, "{ ? = call " + func + " }", true, params, 1, 1);
	}

	/**
	 * Вызвать хранимую процедуру
	 * 
	 * @param prc
	 *            - прототип хранимой процедуры
	 * @param params
	 *            - список параметров процедуры, если есть
	 * @throws Exception
	 */
	public static void execProc(String prc, Object... params) throws Exception {
		getValue(Types.NULL, "{ call " + prc + " }", true, params, 0, 0);
	}

	/**
	 * Выполнить запрос
	 * 
	 * @param sql
	 *            - запрос
	 * @param params
	 *            - список параметров запроса, если есть
	 * @throws Exception
	 */
	public static void execQuery(String sql, Object... params) throws Exception {
		getValue(Types.NULL, sql, false, params, 0, 0);
	}

	/**
	 * Создаёт объект в БД если id модели нулевой, иначе обновляет запись с
	 * данным id
	 * 
	 * @param model
	 *            - объект для сохранения
	 * @param userId
	 * 			  - идентификатор пользователя, производящего изменения
	 * @return id последней изменённой записи
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BaseModel> Long save(T model, Long userId) throws Exception {
		if (model == null)
			return null;
		Class<T> clazz = (Class<T>) model.getClass();
		if (BaseModel.class.equals(clazz))
			if ((model.getClassName() != null) && !model.getClassName().isEmpty())
				try {
					clazz = (Class<T>) Class.forName(model.getClassName());
				} catch (Exception e) {
					logger.error("Попытка сохранить модель неизвестного класса", e);
					throw new Exception(e);
				}
			else
				return null;
		StringBuilder sql = null;
		TableDef pers = ModelMappingUtil.getTableDef(clazz);
		if ((pers.tableName == null) || (pers.fields == null))
			throw new Exception("Не найдена таблица модели " + clazz.getName());
		List<Object> params = new ArrayList<Object>(pers.fields.size());
		if (model.getId() == null) {
			StringBuilder fieldSql = new StringBuilder(512);
			StringBuilder valueSql = new StringBuilder(128);

			for (String field : pers.fields) {
				Object val = model.get(field);
				if (val != null) {
					fieldSql.append(", ").append(field);
					valueSql.append(", ?");
					params.add(val);
				}
			}
			if ((fieldSql.length() > 2) && (valueSql.length() > 2))
				sql = new StringBuilder(1024).append("insert into ")
						.append(pers.tableName).append(" (").append(fieldSql.substring(2))
						.append(") values (").append(valueSql.substring(2)).append(")");
		} else {
			sql = new StringBuilder(1024);
			for (String field : pers.fields) {
				Object val = model.get(field);
				if ((field.charAt(0) >= '0') && (field.charAt(0) <= '9'))
					sql.append(", \"").append(field).append("\"=?");
				else
					sql.append(", ").append(field).append("=?");
				params.add(val);
			}
			sql = new StringBuilder(1024).append("update ")
					.append(pers.tableName).append(" set ").append(sql.substring(2)).append(" where id=?");
			params.add(model.getId());
		}
		Long ret = null;
		if (sql != null) {
			Long id = getValue(Types.NUMERIC, 
					sql.append(" returning id").toString(), false,
					params.toArray(), 0, 1);
			model.setId(id);
			ret = id;
		}
		return ret;
	}

	/**
	 * Сохраняет список моделей в БД. 
	 * Создаёт новую запись если id модели нулевой, иначе обновляет запись с заданным id.
	 * 
	 * @param models
	 *            - список моделей для сохранения
	 * @param userId
	 * 			  - идентификатор пользователя, производящего изменения
	 * @return список идентификаторов каждой из сохранённых моделей
	 * @throws Exception
	 */
	public static <T extends BaseModel> List<Long> save(List<T> models, Long userId) throws Exception {
		if ((models == null) || models.isEmpty())
			return null;
		List<Long> ret = new ArrayList<Long>();
		for (T model : models)
			ret.add(save(model, userId));
		return ret;
	}

	/**
	 * Удаляет объекты из БД
	 * 
	 * @param clazz
	 *            - класс удаляемого объекта
	 * @param ids
	 *            - идентификаторы записей
	 * @param userId
	 * 			  - идентификатор пользователя, производящего удаление
	 * @throws Exception
	 */
	public static <T extends BaseModel> void del(Class<T> clazz, List<Long> ids) throws Exception {
		execQuery("delete from " + ModelMappingUtil.getTableName(clazz) + " where " + BaseModel.ID + "=ANY(?)", ids);
	}

	public static <T extends BaseModel> void delete(T model) throws Exception {
		del(model.getClass(), Arrays.asList(model.getId()));
	}

	public static <T extends BaseModel> void delete(List<T> models) throws Exception {
		if ((models == null) || models.isEmpty())
			return;
		List<Long> ids = new ArrayList<Long>();
		for (T model : models)
			ids.add(model.getId());
		del(models.get(0).getClass(), ids);
	}

	public static <T extends BaseModel> void writeBlob(Class<T> clazz, String fname, Long id, InputStream is) throws Exception {
		execQuery("UPDATE " + ModelMappingUtil.getTableName(clazz) + 
					" SET " + fname + "=? WHERE " + BaseModel.ID + "=?", is, id);
	}

	private static void setFinalStatic(Field field, Object newValue) throws Exception {
		field.setAccessible(true);

		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(null, newValue);
	}

	public static void init() {
		ModelMappingUtil.init();
		try {
			for (final Field f : StoredProcs.class.getFields()) {
				Class<?> clazz = f.getType();
				if (clazz.isInterface())
					setFinalStatic(f, Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] { clazz }, new InvocationHandler() {
						@SuppressWarnings("unchecked")
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							String spName;
							StoredProc sp = method.getAnnotation(StoredProc.class);
							Class<?> clazz = method.getReturnType();
							if ((sp != null) && (sp.value() != null))
								spName = sp.value();
							else {
								StringBuilder mname = new StringBuilder(f.getName()).append(".")
										.append(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, method.getName())).append("(");
								if (args != null) {
									if (args.length > 0)
										mname.append("?");
									for (int i = 1; i < args.length; i++)
										mname.append(", ?");
								}
								mname.append(")");
								spName = mname.toString();
							}
							if (BaseModel.class.isAssignableFrom(clazz))
								return DB.getModel((Class<BaseModel>) method.getReturnType(), spName, args);
							else if (Void.class.isAssignableFrom(clazz) || Void.TYPE.isAssignableFrom(clazz))
								DB.execProc(spName, args);
							else if (Long.class.isAssignableFrom(clazz) || Long.TYPE.isAssignableFrom(clazz))
								return DB.getLong(spName, args);
							else if (Boolean.class.isAssignableFrom(clazz) || Boolean.TYPE.isAssignableFrom(clazz))
								return DB.getBoolean(spName, args);
							else if (String.class.isAssignableFrom(clazz))
								return DB.getString(spName, args);
							else if (List.class.isAssignableFrom(clazz)) {
								ParameterizedType t = (ParameterizedType) method.getGenericReturnType();
								if (t.getActualTypeArguments().length > 0) {
									Class<?> typeArg = (Class<?>) t.getActualTypeArguments()[0];
									if (BaseModel.class.isAssignableFrom(typeArg))
										return DB.getModels((Class<BaseModel>) typeArg, spName, args);
									else
										return DB.getList(spName, args);
								}
							}
							return null;
						}
					}));
			}
		} catch (Exception e) {
			logger.error("Ошибка при инициализации ORM", e);
		}
	}

}