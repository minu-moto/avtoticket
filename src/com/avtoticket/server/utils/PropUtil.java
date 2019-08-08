package com.avtoticket.server.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropUtil {

	private static Logger logger = LoggerFactory.getLogger(PropUtil.class);
	private static Properties masterProps;
	private static Properties slaveProps;

	public static String getProp(String key) {
		return getProp(key, false);
	}

	/** Fil читаем настройку, сначала читаем из conf/core.properties потом, если не найдена пробуем из варки
	 * 
	 * @param key - ключ
	 * @param forseReload - перечитать файл при каждом запросе
	 * @return
	 */
	public static String getProp(String key, boolean forseReload) {
		String val = getMasterPropFile(forseReload).getProperty(key);
		if ((val == null) || val.isEmpty())
			val = getSlavePropFile().getProperty(key);
		return val;
	}

	public static Boolean getBooleanProp(String key, boolean forseReload) {
		return "true".equalsIgnoreCase(getProp(key, forseReload));
	}

	private static Integer getIntProp(String key) {
		Integer ret = null;
		try {
			String val = getProp(key);
			if ((val != null) && !val.isEmpty())
				ret = Integer.valueOf(val);
		} catch (Exception e) {
			logger.error("Произошла ошибка при получении параметров приложения", e);
		}
		return ret;
	}

	public static Properties getSlavePropFile() {
		if (slaveProps == null) {
			slaveProps = new Properties(); 
			try (InputStream is = PropUtil.class.getResourceAsStream("/core.properties")) {
				if (is == null)
					logger.error("Файл настроек приложения не найден");
				else
					slaveProps.load(is);
			} catch (IOException e) { 
				logger.error("Ошибка при работе с файлом настроек приложения", e);
			}
		}
		return slaveProps;
	}

	public static Properties getMasterPropFile(boolean forceReload) {
		if (masterProps == null || forceReload) {
			masterProps = new Properties(); 
			try {
				File pFile = new File(System.getProperty("catalina.home") + "/conf/core.properties");
				if (pFile.exists())
					masterProps.load(new FileInputStream(pFile));
			} catch (IOException e) { 
				logger.error("Ошибка при работе с файлом настроек приложения", e);
			}
		}
		return masterProps;
	}

	public static String getSmtpHost() {
		return getProp("smtp.host.name");
	}

	public static Integer getSmtpPort() {
		return getIntProp("smtp.host.port");
	}

	public static String getSmtpUser() {
		return getProp("smtp.auth.user");
	}

	public static String getSmtpPassword() {
		return getProp("smtp.auth.password");
	}

	public static String getMailFromAddr() {
		return getProp("smtp.addr.from");
	}

	public static String getSmppHost() {
		return getProp("smpp.host.name");
	}

	public static Integer getSmppPort() {
		return getIntProp("smpp.host.port");
	}

	public static String getSmppUser() {
		return getProp("smpp.auth.user");
	}

	public static String getSmppPassword() {
		return getProp("smpp.auth.password");
	}

	public static String getSmsFromAddr() {
		return getProp("smpp.addr.from");
	}

	public static String getStavrApiLogin() {
		return getProp("stavr.api.login");
	}

	public static String getStavrApiPassword() {
		return getProp("stavr.api.password");
	}

	public static String getStavrApiEndpoint() {
		return getProp("stavr.api.endpoint");
	}

	public static Boolean isProduction() {
		return getBooleanProp("server.production", false);
	}

}