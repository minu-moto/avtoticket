/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.server.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.RandomStringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.DB;
import com.avtoticket.server.db.stored.StoredProcs;
import com.avtoticket.shared.models.UUID;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.SavedSession;
import com.avtoticket.shared.models.core.User;
import com.google.common.net.HttpHeaders;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.Util;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 13 дек. 2015 г. 22:57:49
 */
public class UserUtil {

	private static Logger log = LoggerFactory.getLogger(UserUtil.class);
	private static Map<Long, User> users = new HashMap<Long, User>();
	private static final String USER_ID = "userid";
	public static final String COOKIE_NAME = "remember";
	public static final int COOKIE_AGE = 2592000;

	private static boolean login(User user, boolean remember, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		if (user != null) {
			HttpSession ses = req.getSession();
			ses.setAttribute(USER_ID, user.getId());
			cacheUser(user);

			if (remember) {
		        String uuid = java.util.UUID.randomUUID().toString();
				SavedSession ss = new SavedSession();
				String remoteAddr = req.getHeader(HttpHeaders.X_FORWARDED_FOR);
				if (remoteAddr == null)
					remoteAddr = req.getRemoteAddr();
				ss.setUserIp(remoteAddr);
	        	ss.setUserAgent(req.getHeader(HttpHeaders.USER_AGENT));
				ss.setLastLogin(new Date());
				ss.setUserId(user.getId());
				ss.setSessionId(new UUID(uuid));
				DB.save(ss, user.getId());

				Cookie cookie = new Cookie(COOKIE_NAME, uuid);
				cookie.setPath("/");
				cookie.setMaxAge(COOKIE_AGE);
				cookie.setHttpOnly(true);
				resp.addCookie(cookie);
		    } else {
//		        List<Remember> rem = DB.getModels(Remember.class, Where.equals(Remember.USER_ID, user.getId()), false);
//		        DB.delete(rem);

				Cookie cookie = new Cookie(COOKIE_NAME, "");
				cookie.setPath("/");
				cookie.setMaxAge(0);
				cookie.setHttpOnly(true);
				resp.addCookie(cookie);
		    }

			return true;
		} else
			return false;
	}

	public static boolean login(String login, String password, boolean remember, HttpServletRequest req, HttpServletResponse resp) throws SerializationException {
		try {
			return login(StoredProcs.core.getUser(login, password), remember, req, resp);
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			log.error("", e);
			throw new SerializationException(e);
		}
	}

	public static boolean login(Long userId, boolean remember, HttpServletRequest req, HttpServletResponse resp) throws SerializationException {
		try {
			return login(DB.getModel(User.class, userId), remember, req, resp);
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			log.error("", e);
			throw new SerializationException(e);
		}
	}

	public static void logout(HttpServletRequest req, HttpServletResponse resp) {
		HttpSession ses = req.getSession();
		Long uid = getUserIdFromSession(ses);
		if (uid != null) {
			users.remove(uid);
			ses.removeAttribute(USER_ID);

			Cookie cookie = Util.getCookie(req, COOKIE_NAME, true);
			if (cookie != null)
				try {
					SavedSession ss = DB.getModel(SavedSession.class, Where.equals(SavedSession.SESSION_ID, new UUID(cookie.getValue())));
					if (ss != null)
						DB.delete(ss);
					cookie.setValue("");
					cookie.setPath("/");
					cookie.setMaxAge(0);
					cookie.setHttpOnly(true);
					resp.addCookie(cookie);
				} catch (Exception e) {
					log.error("", e);
				}
		}
	}

	public static void cacheUser(User user) {
		if (user != null)
			users.put(user.getId(), user);
	}

	public static User getUserFromSession(HttpServletRequest req) {
		User user = null;
		try {
			if (req != null) {
				HttpSession ses = req.getSession();
				if (ses != null) {
					Long userId = (Long) ses.getAttribute(USER_ID);
					if (userId != null) {
						user = users.get(userId);
						if (user == null) {
							user = DB.getModel(User.class, userId);
							if (user != null)
								cacheUser(user);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return user;
	}

	public static Long getUserIdFromSession(HttpSession ses) {
		return (ses != null) ? (Long) ses.getAttribute(USER_ID) : null;
	}

	public static Long getUserIdFromSession(HttpServletRequest req) {
		return (req != null) ? getUserIdFromSession(req.getSession()) : null;
	}

	public static boolean isUserInSession(HttpServletRequest req) {
		return getUserIdFromSession(req) != null;
	}

	public static Boolean regUser(HttpServletRequest req, String login, String pass) throws SerializationException {
		try {
			StoredProcs.core.registerUser(login, pass, "000000");
			Long confirm = StoredProcs.core.confirmRegistration(login, "000000");
			if ((confirm != null) && (confirm >= 0)) {
				String domain = req.getScheme() + "://" + req.getServerName() + ((req.getServerPort() != 80) ? ":" + req.getServerPort() : "") + "/";
				MailUtil.sendMessage(login, "Регистрация нового пользователя", "Регистрация прошла успешно.<br>" +
						"логин: " + login + "<br>пароль: " + pass + "<br>" + domain);
				return true;
			} else
				throw new Exception("Ошибка при регистрации пользователя " + login);
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	public static void restorePassword(HttpServletRequest req, String email) throws SerializationException {
		try {
			User user = StoredProcs.core.getUser(email);
			if ((user != null) && CommonServerUtils.isValidEmail(email)) {
				String pass = RandomStringUtils.randomAlphanumeric(8);
				String code = RandomStringUtils.randomNumeric(6);
				StoredProcs.core.registerUser(email, pass, code);
				sendNewPassword(req, user, pass, code);
			} else
				throw new SerializationException("Пользователь с указанным E-mail не найден.");
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			log.error("", e);
			throw new SerializationException("При попытке восстановления пароля произошла ошибка");
		}
	}

	public static void sendRegConfirm(HttpServletRequest req, String login, String pswd, String code) throws SerializationException {
		String domain = req.getScheme() + "://" + req.getServerName() + ((req.getServerPort() != 80) ? ":" + req.getServerPort() : "") + "/";
		String url = domain + "confirm?login=" + login + "&code=" + code;
		MailUtil.sendMessage(login, "Регистрация в Avtoticket.com",
				"Здравствуйте!<br><br>"
						+ "Ваш почтовый адрес был использован при регистрации в "
						+ "<a href='" + domain + "'>Avtoticket.com</a>. Параметры учётной записи таковы:<br>"
						+ "<br>" + "E-mail:&nbsp;<b>" + StringEscapeUtils.escapeHtml(login) + "</b><br>"
						+ "Пароль:&nbsp;<b>" + StringEscapeUtils.escapeHtml(pswd) + "</b><br>" + "<br>"
						+ "Ваша учётная запись ещё не активна. Для активации перейдите по следующей ссылке:<br>"
						+ "<br>" + "<a href='" + url + "'>" + StringEscapeUtils.escapeHtml(url) + "</a><br><br>"
//						+ "Или введите код <b>" + StringEscapeUtils.escapeHtml(code) + "</b> в мобильном приложении.<br><br>"
						+ "Не забывайте свой пароль: он хранится в нашей базе в зашифрованном виде, и мы не сможем Вам его выслать. "
						+ "Если Вы всё же забудете пароль, то сможете запросить новый, который придётся активировать таким же образом, как и вашу учётную запись.<br>"
						+ "<br>"
						+ "Мы благодарим Вас за потраченное время и желаем приятного пользования нашей системой.<br>"
						+ "<br>" + "<span style='color: #5E6061'><b><i>С уважением," + "<br>"
						+ "команда Avtoticket.com</i></b></span>");
	}

	private static void sendNewPassword(HttpServletRequest req, User user, String pswd, String code) throws SerializationException {
		String domain = req.getScheme() + "://" + req.getServerName() + ((req.getServerPort() != 80) ? ":" + req.getServerPort() : "") + "/";
		String url = domain + "confirm?login=" + user.getLogin() + "&code=" + code;
		MailUtil.sendMessage(user.getLogin(), "Восстановление пароля в Avtoticket.com",
				"Здравствуйте"
						+ StringEscapeUtils.escapeHtml(
								!user.getFirstName().isEmpty()
										? " " + user.getFirstName() + (!user.getMiddleName().isEmpty() ? " " + user.getMiddleName() : "")
										: "")
						+ "!<br><br>Ваш почтовый адрес был использован для восстановления пароля в "
						+ "<a href='" + domain + "'>Avtoticket.com</a><br>"
						+ "Новый пароль: <b>" + StringEscapeUtils.escapeHtml(pswd) + "</b><br><br>"
						+ "Прежде чем использовать новый пароль, Вы должны его активировать. Для этого перейдите по следующей ссылке:<br>"
						+ "<a href='" + url + "'>" + StringEscapeUtils.escapeHtml(url) + "</a><br>"
//						+ "Или введите код <b>" + StringEscapeUtils.escapeHtml(code) + "</b> в мобильном приложении.<br>"
						+ "<br>В дальнейшем Вы сможете сменить этот пароль на странице настроек в личном кабинете.<br>"
						+ "Если Вы не запрашивали новый пароль, то не обращайте внимания на это письмо.<br>"
						+ "<br>" + "<span style='color: #5E6061'><b><i>С уважением," + "<br>"
						+ "команда Avtoticket.com</i></b></span>");
	}

}