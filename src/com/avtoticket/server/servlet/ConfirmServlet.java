/*
 * Copyright Avtoticket (c) 2018.
 */
package com.avtoticket.server.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.stored.StoredProcs;
import com.avtoticket.server.utils.UserUtil;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 6 февр. 2018 г. 21:42:44
 */
public class ConfirmServlet extends ExtHttpServlet {

	private static final long serialVersionUID = 4168303659862948222L;

	private static Logger logger = LoggerFactory.getLogger(ConfirmServlet.class.getName());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String login = req.getParameter("login");
		String code = req.getParameter("code");
		String returnUrl = "<a href='" + req.getScheme() + "://" + req.getServerName()
			+ ((req.getServerPort() != 80) ? ":" + req.getServerPort() : "")
			+ "'>Вернуться на сайт</a>";
		if ((login != null) && !login.isEmpty()
				&& (code != null) && !code.isEmpty()) {
			try {
				Long userId = StoredProcs.core.confirmRegistration(login, code);
				if (userId != null) {
					if (userId >= 0) {
						UserUtil.login(userId, false, req, resp);
						response(resp, "Аккаунт успешно активирован", returnUrl);
					} else
						response(resp, "Ссылка устарела, попробуйте ещё раз", returnUrl);
				} else
					resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (Exception e) {
				logger.error("", e);
				response(resp, "Произошла ошибка при подтверждении данных", returnUrl);
			}
		} else
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

}