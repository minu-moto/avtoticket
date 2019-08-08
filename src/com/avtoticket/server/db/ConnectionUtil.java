/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.SerializationException;
import com.jolbox.bonecp.BoneCPDataSource;

/**
 * @author Minu <<a href='minu-moto@mail.ru'>minu-moto@mail.ru</a>>
 * @since 14.02.2012 9:13:15
 */
public class ConnectionUtil {

	private static Logger logger = LoggerFactory.getLogger(ConnectionUtil.class.getName());

	private static final List<Connection> cons = new ArrayList<Connection>();
	private static final Map<String, DataSource> dss = new HashMap<String, DataSource>();
	public static final String DEFAULT_DATASOURCE = "AVTOTICKET";

	static {
		try {
			// подгружаем драйверы СУБД
			DriverManager.registerDriver(new Driver());
		} catch (SQLException e) {
			logger.error("Почему-то не найден драйвер для PostgreSQL");
		}
	}

	/**
	 * Возвращает экземпляр источника данных по его имени. Метод помечен как
	 * синхронный, чтоб одновременно не создались несколько экземпляров
	 * 
	 * @param dsName
	 *            - имя датасорса
	 * @return экземпляр датасорса
	 * @throws SerializationException
	 */
	public static synchronized BoneCPDataSource getDataSource(String dsName)
			throws SerializationException {
		BoneCPDataSource ds = (BoneCPDataSource) dss.get(dsName);
		if (ds == null)
			try {
				Context initialContext = new InitialContext();
				ds = (BoneCPDataSource) initialContext.lookup("java:comp/env/jdbc/" + dsName);
				if (ds != null) {
					Properties prop = new Properties();
					prop.setProperty("loginTimeout", "2");
					prop.setProperty("user", ds.getUsername());
					prop.setProperty("password", ds.getPassword());
					ds.setDriverProperties(prop);
					dss.put(dsName, ds);
				} else {
					String err = "Соединение с БД не установлено, но и процесс прошел без ошибок\nдля " + dsName;
					logger.error(err);
					throw new SerializationException(err);
				}
			} catch (Exception e) {
				String err = "Ошибка настройки соединения с БД\nдля " + dsName;
				logger.error(err, e);
				throw new SerializationException(err, e);
			}
		return ds;
	}

	/**
	 * Получить дефолтный коннект к базе
	 * 
	 * @return экземпляр коннекта
	 * @throws SerializationException
	 */
	public static Connection getConnection() throws SerializationException {
		return getConnection(DEFAULT_DATASOURCE);
	}

	/**
	 * Получить коннект к базе
	 * 
	 * @param dsName
	 *            - имя датасорса
	 * @return экземпляр коннекта
	 * @throws SerializationException
	 */
	public static synchronized Connection getConnection(String dsName)
			throws SerializationException {
		Connection con = null;
		try {
			DataSource ds = getDataSource(dsName);
			con = ds.getConnection();
			if (con != null) {
				con.setAutoCommit(false);
				cons.add(con);
			}
		} catch (Exception e) {
			logger.error("Не удалось установить соединение с БД для " + dsName, e);
			throw new SerializationException("Не удалось установить соединение с БД", e);
		}
		return con;
	}

	/**
	 * Освободить ресурсы
	 * 
	 * @param rset
	 *            - результат выполнения запроса
	 * @param stmt
	 *            - препарированный запрос
	 * @param con
	 *            - коннект к базе
	 * @throws SerializationException
	 */
	public static void releaseResources(ResultSet rset, Statement stmt, Connection con) throws SerializationException {
		if (rset != null)
			try {
				rset.close();
			} catch (Exception e) {
				logger.error("Ошибка освобождения резалтсета", e);
			}
		if (stmt != null)
			try {
				stmt.close();
			} catch (Exception e) {
				logger.error("Ошибка освобождения стейтмента", e);
			}
		closeConnection(con);
	}

	/**
	 * Закрыть коннект к базе
	 * 
	 * @param con
	 *            - коннект к базе
	 * @throws SerializationException
	 */
	private static synchronized void closeConnection(Connection con) throws SerializationException {
		try {
			if ((con != null) && !con.isClosed()) {
				con.commit();
				con.close();
			}
			cons.remove(con);
		} catch (Exception e) {
			logger.error("Ошибка освобождения соединения к БД", e);
			throw new SerializationException("Ошибка освобождения соединения к БД", e);
		}
	}

	/**
	 * Грохнуть все созданные датасорсы вместе с подключениями
	 * 
	 * @throws Exception
	 */
	public static synchronized void destroyDataSources() throws Exception {
		int relSize = cons.size();
		while (cons.size() > 0)
			closeConnection(cons.get(0));
		if (relSize > 0)
			logger.error("Освобождено [ " + relSize + " ] коннектов !!!!!!!!!!");
//		if (!dss.isEmpty()) {
//			Set<BoneCP> pools = new HashSet<BoneCP>();
//			for (BoneCPDataSource ds : dss.values()) {
//				BoneCP pool = ds.getPool();
//				if (!pools.contains(pool))
//					pools.add(pool);
//			}
//			dss.clear();
//			for (BoneCP pool : pools)
//				pool.shutdown();
//		}
		dss.clear();
	}

}