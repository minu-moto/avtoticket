package com.avtoticket.server.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.ConnectionUtil;
import com.avtoticket.server.db.DB;
import com.avtoticket.server.db.stored.StoredProcs;
import com.avtoticket.server.jcr.JCRUtil;
import com.avtoticket.server.utils.PropUtil;
import com.avtoticket.server.utils.SchedullerUtil;
import com.avtoticket.server.utils.SmsSender;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.User;

public class UtilServlet extends ExtHttpServlet {

	private static final long serialVersionUID = 2229172667499787421L;

	private static Logger logger = LoggerFactory.getLogger(UtilServlet.class);

	private static String root;

	/**
	 * @return абсолютный путь к корневой папке приложения
	 */
	public static String getRootPath() {
		return root;
	}

	@Override
	public void init() throws ServletException {
		super.init();

		root = getServletContext().getRealPath("/");
		if (!root.endsWith("/") && !root.endsWith("\\"))
			root += '/';

//		if (ServerDetector.isProduction())
			JCRUtil.init();
//		SerializerUtil.setSerializer(new SerializerImpl());
		DB.init();
		SchedullerUtil.start();

		String ver = PropUtil.getProp("build.number");
		String date = PropUtil.getProp("build.date");
		boolean prntDate = (date != null) && !date.isEmpty();
		logger.info("System info:"
				+ "\r\nApp: Avtoticket v2.0" + ((!ver.isEmpty()) ? "." + ver : "") + (prntDate ? " " + date : "")
				/*+ "\r\nSrv: " + SystemInfo.getServerInfo()
				+ "\r\nJRE: " + SystemInfo.getJavaVersion()*/
				+ "\r\nJCR: " + JCRUtil.getVersion()/*
				+ "\r\nDB: " + SystemInfo.getDbInfo()
				+ "\r\nOS: " + SystemInfo.getOSName() + " v" + SystemInfo.getOSVersion()
				+ "\r\nCPU: " + SystemInfo.getArch() + " x" + SystemInfo.getAvailableProcessors()
				+ "\r\nMem: " + SerializerUtil.formatTraff(SystemInfo.getTotalPhysicalMemorySize())*/);

	    for (File root : File.listRoots())
	    	logger.info("\r\nFile system root: " + root.getAbsolutePath()
	    			+ "\r\nTotal space: " + root.getTotalSpace()
	    			+ "\r\nFree space: " + root.getFreeSpace()
	    			+ "\r\nUsable space: " + root.getUsableSpace());
		logger.info("all done");
	}

	@Override
	public void destroy() {
		JCRUtil.destroy();
		try {
			ConnectionUtil.destroyDataSources();
		} catch (Exception e) {
			logger.error("", e);
		}
		try {
			SchedullerUtil.stop();
		} catch (Exception e) {
			logger.error("", e);
		}
		try {
			SmsSender.stop();
		} catch (Exception e) {
			logger.error("", e);
		}
		super.destroy();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String act = req.getParameter("act");
		if ("sync_stations".equals(act))
			SchedullerUtil.regetStations();
		else if ("sync_stav".equals(act))
			SchedullerUtil.syncStav26();
		else if ("happybirthday".equals(act))
			try {
				String login = req.getParameter("login");
				User user = DB.getModel(User.class, Where.equals(User.LOGIN, login));
				String theme = StoredProcs.core.getProp("birthday_theme");
				String mail = StoredProcs.core.getProp("birthday_mail");
				SchedullerUtil.doSend(user, theme, mail);
			} catch (Exception e) {
				logger.error("", e);
			}
		else if ("sms".equals(act))
			try {
				SmsSender.sendMessage(1L, req.getParameter("phone"), "привет");
			} catch (Exception e) {
				logger.error("", e);
			}
		else if ("version".equals(act))
			responseHtml(resp, PropUtil.getProp("build.number"));
//		} else if("strrndtst".equals(act)){
//			try {
//				String[] ss = new String[10];
//				for (int i = 0; i<10; i++)
//					ss[i]=RandomStringUtils.randomAlphanumeric(3+i%3).toLowerCase();
//				log.info("*** before ***");
//				for (String s: ss)
//					log.info(s);
//				Arrays.sort(ss);
//				log.info("*** after ***");
//				for (String s: ss)
//					log.info(s);
//				log.info("*** end ***");
//			} catch (Exception e) {
//				log.error("", e);
//			}
//		} else if ("soaptest".equals(act)){
//			
//			try {
//				URL url = new URL("http://www.avtoticket.com/ueksoap/ueksoap?wsdl");
//				QName qname = new QName("http://uec.server.ticket.bp.ru/", "TicketsServiceImplService");
//				Service cardsrv = Service.create(url, qname);
//				Tickets tkts = cardsrv.getPort(Tickets.class);
//				responseText(resp, tkts.getAuthUrl("testauth"));
//			} catch (Exception e) {
//				log.error("", e);
//			}
//			
//			
//		} else if ("sendmetestemailplease".equals(act)){
//			String addr = req.getParameter("addr");
//			if (addr!=null && !addr.isEmpty())
//				try {
//					MailUtil.sendMessage(addr, "test", "ololo");
//				} catch (SerializationException e) {
//					responseText(resp, e.getLocalizedMessage());
//				}
//		}
	}

}