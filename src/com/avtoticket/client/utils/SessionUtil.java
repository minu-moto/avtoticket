package com.avtoticket.client.utils;

import java.util.Date;

import com.avtoticket.shared.models.core.User;

public class SessionUtil {

	private static Session instance;

	public static void setSession(Session sessionImpl) {
		instance = sessionImpl;
	}

	private static Session getInstance() {
		if (instance == null)
			instance = new Session();
		return instance;
	}

	public static void init(DefaultCallback<User> cback) {
		getInstance().init(cback);
	}

	public static User getUser() {
		return getInstance().getUser();
	}

	public static String getUserLogin() {
		User user = getUser();
		return (user != null) ? user.getLogin() : null;
	}

	public static void doLogin(String login, String password, boolean remember) {
		getInstance().doLogin(login, password, remember);
	}

	public static void reGetData(DefaultCallback<Boolean> callback) {
		getInstance().reGetData(callback);
	}

	public static boolean isLocal() {
		return getInstance().isLocal();
	}

	public static Date getTimestamp() {
		return getInstance().getTimestamp();
	}

	public static boolean isAdmin() {
		User usr = getUser();
		return (usr != null) && (usr.isAdmin() == Boolean.TRUE);
	}

	public static boolean isServiceMode() {
		return getInstance().isServiceMode() == Boolean.TRUE;
	}

	public static String getBirthdayTheme() {
		return getInstance().getBirthdayTheme();
	}

	public static String getBirthdayMail() {
		return getInstance().getBirthdayMail();
	}

	public static Long getSalePeriod() {
		return getInstance().getSalePeriod();
	}

	public static int getChildhood() {
		Long ret = getInstance().getChildhood();
		return (ret != null) ? ret.intValue() : 0;
	}

	public static String getAdultPriceFormula() {
		return getInstance().getAdultPriceFormula();
	}

	public static String getChildPriceFormula() {
		return getInstance().getChildPriceFormula();
	}

	public static String getBagPriceFormula() {
		return getInstance().getBagPriceFormula();
	}

	public static String getStavAdultPriceFormula() {
		return getInstance().getStavAdultPriceFormula();
	}

	public static String getStavChildPriceFormula() {
		return getInstance().getStavChildPriceFormula();
	}

	public static String getStavBagPriceFormula() {
		return getInstance().getStavBagPriceFormula();
	}

	public static boolean isProduction() {
		return getInstance().isProduction();
	}

}