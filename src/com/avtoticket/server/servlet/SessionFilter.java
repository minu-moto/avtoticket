/*
 * Copyright Avtoticket (c) 2018.
 */
package com.avtoticket.server.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.DB;
import com.avtoticket.server.utils.UserUtil;
import com.avtoticket.shared.models.UUID;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.SavedSession;
import com.google.common.net.HttpHeaders;
import com.google.gwt.user.server.Util;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 4 марта 2018 г. 16:05:32
 */
public class SessionFilter implements Filter {

	private static Logger logger = LoggerFactory.getLogger(SessionFilter.class);

	@Override
	public void destroy() { }

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		Long userId = UserUtil.getUserIdFromSession(request);

		if (userId == null) {
			Cookie cookie = Util.getCookie(request, UserUtil.COOKIE_NAME, true);
		    String uuid = (cookie != null) ? cookie.getValue() : null;

		    if ((uuid != null) && !uuid.isEmpty())
		    	try {
			    	SavedSession ss = DB.getModel(SavedSession.class, Where.equals(SavedSession.SESSION_ID, new UUID(uuid)));
			    	userId = (ss != null) ? ss.getUserId() : null;

			        if (userId != null) {
			        	UserUtil.login(userId, false, request, response);

			        	String remoteAddr = request.getHeader(HttpHeaders.X_FORWARDED_FOR);
						if (remoteAddr == null)
							remoteAddr = req.getRemoteAddr();
						ss.setUserIp(remoteAddr);
			        	ss.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
			        	ss.setLastLogin(new Date());
			        	DB.save(ss, userId);

			        	// extends cookie age
						cookie = new Cookie(UserUtil.COOKIE_NAME, uuid);
						cookie.setPath("/");
						cookie.setMaxAge(UserUtil.COOKIE_AGE);
						cookie.setHttpOnly(true);
						response.addCookie(cookie);
			        } else {
			        	// remove cookie
			        	cookie = new Cookie(UserUtil.COOKIE_NAME, "");
						cookie.setPath("/");
						cookie.setMaxAge(0);
						cookie.setHttpOnly(true);
						response.addCookie(cookie);
			        }
			    } catch (Exception e) {
			    	logger.error("", e);
			    }
		}

		chain.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException { }

}