/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.jcr.JCRUtil;
import com.avtoticket.shared.models.core.ImageFile;

import com.google.gwt.user.client.rpc.SerializationException;

/**
 * @author Minu <<a href='minu-moto@mail.ru'>minu-moto@mail.ru</a>>
 * @since 12.12.2012 17:51:39
 */
public class IOServlet extends ExtHttpServlet {

	private static final long serialVersionUID = 8408385159290283664L;

	private static Logger logger = LoggerFactory.getLogger(IOServlet.class.getName());

	private static final long repoTimeout = logger.isDebugEnabled() ? 100 : 1000;

	private Long getLongValue(String val, Long def) {
		try {
			return Long.valueOf(val);
		} catch (Exception e) {
			return def;
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String fileTitle = "";
		try {
			long time = new Date().getTime();
			String id = req.getParameter("id");
			Long w = getLongValue(req.getParameter("width"), null);
			Long h = getLongValue(req.getParameter("height"), null);
			ImageFile img = JCRUtil.getImage(id, w, h);
			if (img != null) {
				fileTitle = img.getTitle();
				try (InputStream cont = JCRUtil.getContent(img.getIdent())) {
					time = new Date().getTime() - time;
					if (time >= repoTimeout)
						logger.warn("repository " + time + "ms params: " + w + "x" + h + " id=" + id);
					responseFile(req, resp, "image/jpg", fileTitle, cont, (img.getSize() != null) ? img.getSize().intValue() : null);
				}
			} else
				throw new Exception("Изображение не найдено");
		} catch (NumberFormatException e) {
			response(resp, "", "Неправильно заданы параметры запроса");
		} catch (SerializationException e) {
			response(resp, fileTitle, e.getMessage());
		} catch (Exception e) {
        	logger.error((!fileTitle.isEmpty() ? fileTitle + ": " : "") + e.getMessage(), e);
        	response(resp, fileTitle, e.getMessage());
        }
	}

}