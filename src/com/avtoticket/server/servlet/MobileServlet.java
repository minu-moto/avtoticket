package com.avtoticket.server.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.rpc.TicketServiceImpl;
import com.avtoticket.server.utils.OutUtil;
import com.avtoticket.server.utils.OutWrapper;
import com.avtoticket.shared.models.core.Gender;
import com.avtoticket.shared.models.core.Passage;
import com.avtoticket.shared.models.core.User;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class MobileServlet extends ExtHttpServlet {

	private static Logger logger = LoggerFactory.getLogger(MobileServlet.class);

	private static final long serialVersionUID = 323887998578974327L;
	private boolean debug = false;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String act = req.getParameter("act");
		if ("sell".equalsIgnoreCase(act))
			try {
				HttpSession session = req.getSession();
				User user = null;
				OutWrapper<User> ow = OutUtil.getUser(req);
				if (ow.getErrorCode() == 0)
					user = ow.getContent();
				Long psgid = Long.parseLong(req.getParameter("psgid"));
				String date = req.getParameter("date");
				Passage psg = OutUtil.psgcache.getIfPresent(psgid + "_" + date + "_" + session.getId());
				if (psg == null) {
					logger.error("no passage found for: " + psgid + "_" + date);
					responseJson(resp, toJson(new OutWrapper<String>(30, "Сессия покупки билета истекла. Повторите поиск рейса.")));
					return;
				}
				List<User> users = new Gson().fromJson(req.getParameter("users"), new TypeToken<List<User>>() {
				}.getType());
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				for (User u : users) {
					u.setBags(u.getLongProp("baggage"));
					u.setIsChild(u.getBooleanProp("ischild"));
					u.setBirthday(format.parse(u.getStringProp(User.B_DATE)));
					u.setVdate(format.parse(u.getStringProp(User.V_DATE)));
					u.setGender("м".equalsIgnoreCase(u.getStringProp(User.GENDER)) ? Gender.MALE : ("ж".equalsIgnoreCase(u.getStringProp(User.GENDER)) ? Gender.FEMALE : null));
				}
				responseJson(resp, toJson(new OutWrapper<String>(TicketServiceImpl.buyTickets(req, req.getHeader(HttpHeaders.USER_AGENT),
						(user != null) ? user : users.get(0), users, psg, true))));
			} catch (Exception e) {
				logger.error("", e);
				responseJson(resp, toJson(new OutWrapper<String>(1, "Внутренняя ошибка сервера")));
			}
		else
			doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String act = req.getParameter("act");
		String ret = null;
		HttpSession session = null;
		if (!"login".equals(act))
			session = req.getSession();
		if ("dep".equals(act))
			ret = toJson(OutUtil.getDep(req));
		else if ("dest".equals(act))
			ret = toJson(OutUtil.getDest(req), "dd.MM.yyyy");
		else if ("register".equals(act))
			ret = toJson(OutUtil.register(req));
		else if ("login".equals(act))
			ret = toJson(OutUtil.login(req, resp));
		else if ("getprofile".equals(act))
			ret = toJson(OutUtil.getUser(req));
		else if ("setprofile".equals(act))
			ret = toJson(OutUtil.setUser(req));
		else if ("restorepass".equals(act))
			ret = toJson(OutUtil.restore(req));
		else if ("changepass".equals(act))
			ret = toJson(OutUtil.changePass(req));
		else if ("reis".equals(act))
			ret = toJson(OutUtil.getPassages(req, session));
		else if ("sell".equals(act))
			ret = toJson(OutUtil.sellTicket(req, session));
		else if ("doctypes".equals(act))
			ret = toJson(OutUtil.getDocTypes(req));
		else if ("grajd".equals(act))
			ret = toJson(OutUtil.getGrajd(req));
		else if ("tickets".equals(act))
			ret = toJson(OutUtil.getUserTickets(req));
		else if ("stations".equals(act))
			ret = toJson(OutUtil.getStations(req));
		else if ("history".equals(act))
			ret = toJson(OutUtil.getHistory(req));
		else
			ret = toJson(new OutWrapper<String>(10, "Некорректный запрос" + " - " + act));
		if (debug) {
			logger.info(req.toString());
			logger.info(ret);
		}
		responseJson(resp, ret);
	}

	private <T extends Object> String toJson(OutWrapper<T> out) {
		Gson gs = new Gson();
		return gs.toJson(out);
	}

	private <T extends Object> String toJson(OutWrapper<T> out, String dateFormat) {
		Gson gs = new GsonBuilder().setDateFormat(dateFormat).create();
		return gs.toJson(out);
	}

}