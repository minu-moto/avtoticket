package com.avtoticket.server.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.HttpHeaders;

/**
 * @author Minu <<a href='minu-moto@mail.ru'>minu-moto@mail.ru</a>>
 * @since 18.02.2012 17:51:39
 */
public class ExtHttpServlet extends HttpServlet {

	private static final long serialVersionUID = 6625622394047916073L;

	private static Logger logger = LoggerFactory.getLogger(ExtHttpServlet.class.getName());

	/**
	 * Отправить запрос на указанный адрес
	 * 
	 * @param url
	 *            - адрес назначения
	 * @return ответ сервера
	 */
	protected static String doRequest(String url) {
		try {
			return IOUtils.toString(new URL(url).openConnection().getInputStream());
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}

	/**
	 * Сформировать ответ в формате HTML
	 * 
	 * @param resp
	 *            - ответ пользователю
	 * @param html
	 *            - содержимое ответа
	 */
	public static void responseHtml(HttpServletResponse resp, String html) {
		response(resp, 200, "text/html", html);
	}

	public static void responseJson(HttpServletResponse resp, String json) {
		responseJson(resp, 200, json);
	}

	public static void responseJson(HttpServletResponse resp, int code, String json) {
		response(resp, code, "application/json", json);
	}

	/**
	 * Сформировать ответ в формате HTML
	 * 
	 * @param resp
	 *            - ответ пользователю
	 * @param code
	 *            - код HTTP ответа
	 * @param html
	 *            - содержимое ответа
	 */
	public static void response(HttpServletResponse resp, int code, String contentType, String html) {
		try {
			if ((resp == null) || resp.isCommitted())
				return;
//			resp.reset();
			resp.setStatus(code);
			resp.setContentType(contentType);
			resp.setCharacterEncoding("UTF-8");
			resp.setHeader("Pragma", "no-cache");
			resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
			resp.setHeader("Expires", "Sat, 26 Jul 1997 05:00:00 GMT");
			if (html != null) {
				byte[] bytes = html.getBytes("UTF-8");
				resp.setContentLength(bytes.length);
				resp.getOutputStream().write(bytes);
			}
			try {
				resp.flushBuffer();
			} catch (IOException e) {
				logger.warn(e.getClass() + ": " + e.getMessage());
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	/**
	 * Сформировать ответ в формате XML
	 * 
	 * @param resp
	 *            - ответ пользователю
	 * @param xml
	 *            - содержимое ответа
	 */
	protected static void responseXML(HttpServletResponse resp, String xml) {
		try {
			if ((resp == null) || resp.isCommitted())
				return;
			resp.reset();
			resp.setContentType("text/xml");
			resp.setCharacterEncoding("UTF-8");
			resp.getOutputStream().write(
					"<?xml version='1.0' encoding='UTF-8'?>\n".getBytes("UTF-8"));
			if (xml != null) {
				byte[] bytes = xml.getBytes("UTF-8");
				resp.setContentLength(bytes.length);
				resp.getOutputStream().write(bytes);
			}
			try {
				resp.flushBuffer();
			} catch (IOException e) {
				logger.warn(e.getClass() + ": " + e.getMessage());
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	/**
	 * Сообщить пользователю о возникшей ошибке
	 * 
	 * @param resp
	 *            - ответ пользователю
	 * @param title
	 *            - заголовок
	 * @param mes
	 *            - текст сообщения об ошибке
	 */
	protected void response(HttpServletResponse resp, String title, String mes) {
		try {
			responseHtml(
					resp,
					"<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><title>"
							+ title
							+ "</title></head><body><center><h1>"
							+ title + "</h1>" + mes + "</center></body></html>");
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	/**
	 * Сформировать имя файла для вставки в заголовок ответа
	 * 
	 * @param req
	 *            - запрос пользователя
	 * @param fileName
	 *            - имя файла
	 * @return
	 */
	protected String getFileName(HttpServletRequest req, String fileName) {
		String enFilename = "filename";
		try {
			fileName = fileName.replaceAll(" ", "_");
			if (req.getHeader(HttpHeaders.USER_AGENT).indexOf("Chrome") != -1
					|| req.getHeader(HttpHeaders.USER_AGENT).indexOf("MSIE") != -1)
				enFilename += "=\"" + new String(fileName.getBytes("Cp1251"), "Cp1252") + "\"";
			else
				enFilename += "*=UTF-8''" + URLEncoder.encode(fileName, "UTF8");
		} catch (Exception e) {
			logger.error("", e);
		}
		return enFilename;
	}

	/**
	 * Перенаправить запрос на указанный адрес
	 * 
	 * @param req
	 *            - запрос
	 * @param urlString
	 *            - адрес назначения
	 * @param resp
	 *            - ответ
	 * @throws IOException
	 */
	protected void redirect(HttpServletRequest req, String urlString, HttpServletResponse resp) throws IOException {
		BufferedInputStream webToProxyBuf = null;
		BufferedOutputStream proxyToClientBuf = null;

		String queryString = req.getQueryString();

		urlString += queryString == null ? "" : "?" + queryString;
		URL url = new URL(urlString);

		// log.info("Fetching >"+url.toString());

		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		String methodName = req.getMethod();
		con.setRequestMethod(methodName);
		con.setDoOutput(true);
		con.setDoInput(true);
		HttpURLConnection.setFollowRedirects(false);
		con.setUseCaches(true);

		for (Enumeration<?> e = req.getHeaderNames(); e.hasMoreElements();) {
			String headerName = e.nextElement().toString();
			con.setRequestProperty(headerName, req.getHeader(headerName));
		}

		con.connect();

		if ("POST".equalsIgnoreCase(methodName)) {
			BufferedInputStream clientToProxyBuf = new BufferedInputStream(
					req.getInputStream());
			BufferedOutputStream proxyToWebBuf = new BufferedOutputStream(
					con.getOutputStream());

			IOUtils.copy(clientToProxyBuf, proxyToWebBuf);

			proxyToWebBuf.flush();
			proxyToWebBuf.close();
			clientToProxyBuf.close();
		}

		resp.setStatus(con.getResponseCode());

		con.getHeaderFields().forEach((key, val) -> {
			if (key != null)
				resp.setHeader(key, val.get(0));
		});

		webToProxyBuf = new BufferedInputStream(con.getInputStream());
		proxyToClientBuf = new BufferedOutputStream(resp.getOutputStream());
		IOUtils.copy(webToProxyBuf, proxyToClientBuf);

		proxyToClientBuf.close();
		webToProxyBuf.close();
		con.disconnect();
	}

	/**
	 * Вернуть пользователю файл
	 * 
	 * @param req
	 *            - запрос
	 * @param resp
	 *            - ответ пользователю
	 * @param cType
	 *            - MIME тип файла
	 * @param fName
	 *            - имя файла
	 * @param fCont
	 *            - содержимое файла
	 * @param fSize
	 *            - размер файла
	 */
	protected void responseFile(HttpServletRequest req, HttpServletResponse resp,
			String cType, String fName, InputStream fCont, Integer fSize) {
		responseFile(req, resp, cType, fName, fCont, fSize, false);
	}

	/**
	 * Вернуть пользователю файл
	 * 
	 * @param req
	 *            - запрос
	 * @param resp
	 *            - ответ пользователю
	 * @param cType
	 *            - MIME тип файла
	 * @param fName
	 *            - имя файла
	 * @param fCont
	 *            - содержимое файла
	 * @param fSize
	 *            - размер файла
	 */
	protected void responseFile(HttpServletRequest req, HttpServletResponse resp,
			String cType, String fName, InputStream fCont, Integer fSize, boolean cache) {
		try {
			resp.resetBuffer();
			resp.setCharacterEncoding("UTF-8");
			if (cache) {
				resp.setHeader("Expires", "0");
				resp.setHeader("Pragma", "public");
				resp.setHeader("Cache-Control", "max-age=3600");
			} else {
				resp.setHeader("Pragma", "no-cache");
				resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
				resp.setHeader("Expires", "Sat, 26 Jul 1997 05:00:00 GMT");
			}
			resp.setContentType(cType);
			if (fName != null)
				resp.setHeader("Content-Disposition",
						"inline; " + getFileName(req, fName));
			try {
				if (fSize != null) {
					resp.setContentLength(fSize);
					if (fCont != null)
						IOUtils.copy(fCont, resp.getOutputStream());
				} else if (fCont != null)
					resp.setContentLength(IOUtils.copy(fCont, resp.getOutputStream()));
				resp.flushBuffer();
			} catch (IOException e) {
				logger.warn(e.getClass() + ": " + e.getMessage());
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	/**
	 * Вернуть пользователю файл
	 * 
	 * @param req
	 *            - запрос
	 * @param resp
	 *            - ответ пользователю
	 * @param cType
	 *            - MIME тип файла
	 * @param fName
	 *            - имя файла
	 * @param fCont
	 *            - содержимое файла
	 */
	protected void responseFile(HttpServletRequest req, HttpServletResponse resp,
			String cType, String fName, String fCont) {
		try {
			byte[] bytes = fCont.getBytes("UTF-8");
			responseFile(req, resp, cType, fName, new ByteArrayInputStream(bytes), bytes.length);
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
		}
	}

}