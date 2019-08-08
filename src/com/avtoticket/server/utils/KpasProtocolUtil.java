package com.avtoticket.server.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.DB;
import com.avtoticket.server.db.stored.StoredProcs;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.Formula;
import com.avtoticket.shared.models.core.Gender;
import com.avtoticket.shared.models.core.Passage;
import com.avtoticket.shared.models.core.PassageType;
import com.avtoticket.shared.models.core.Station;
import com.avtoticket.shared.models.core.Ticket;

import com.google.gwt.user.client.rpc.SerializationException;

import com.udojava.evalex.Expression;

public class KpasProtocolUtil {

	private static Logger logger = LoggerFactory.getLogger(KpasProtocolUtil.class);

	private static class AvProtocolException extends Exception {

		private static final long serialVersionUID = 632025572466594071L;

		public AvProtocolException(String msg) {
			super(msg);
		}

	}

	// private static final String HOST =
	// "http://www.kpas.ru/cgi-bin/rsale.cgi";
	public static final int DATES_RANGE = 31;
	public static final String ACTION_GET_DESTINATIONS = "dest";
	public static final String ACTION_GET_PASSAGES = "reis";
	public static final String ACTION_SELL_TICKET = "sell";
	public static final String ACTION_SALE_TICKET = "sale";
	private static final String ENCODING = "KOI8-R";

	private static String getHost(Long depid) throws Exception {
		Station server = DB.getModel(Station.class, depid);
		return (server != null) ? "http://" + server.getHost() + "/cgi-bin/rsale.cgi" : null;
	}

	private static String getXml(String url) throws SerializationException {
		try {
			return IOUtils.toString(new URL(url), Charset.forName(ENCODING));
		} catch (MalformedURLException e) {
			logger.error("Произошла ошибка при получении данных " + url, e);
			throw new SerializationException("Внутренняя ошибка сервера");
		} catch (IOException e) {
			logger.error("Произошла ошибка ввода/вывода " + url, e);
			throw new SerializationException("Внутренняя ошибка сервера");
		} catch (Exception e) {
			logger.error(url, e);
			throw new SerializationException("Внутренняя ошибка сервера");
		}
	}

	public static List<Station> getDestinations(Date date, Long depid) throws SerializationException {
		String url = "";
		try {
			if (depid == null)
				return null;
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
			url = getHost(depid) + "?act=" + ACTION_GET_DESTINATIONS + "&date=" + dateFormat.format(date);
			String xml;
			xml = getXml(url);
			return getObjects(Station.class, xml);
		} catch (AvProtocolException e) {
			logger.warn("Ошибка при получении пунктов назначения\n" + url + "\n" + e.getMessage());
			throw new SerializationException("Невозможно получить пункты назначения");
		} catch (Exception e) {
			logger.error("Ошибка при получении пунктов назначения", e);
			throw new SerializationException("Невозможно получить пункты назначения");
		}
	}

	private static void fillExpression(Expression expr, Passage psg) {
		expr.setVariable("tarif", (psg.getTariff() != null) ? BigDecimal.valueOf(psg.getTariff() / 100.0) : BigDecimal.ZERO)
			.and("child_tarif", (psg.getPrefTariff() != null) ? BigDecimal.valueOf(psg.getPrefTariff() / 100.0) : BigDecimal.ZERO)
			.and("bag_tarif", (psg.getBagTariff() != null) ? BigDecimal.valueOf(psg.getBagTariff() / 100.0) : BigDecimal.ZERO)
			.and("kom_sbor", (psg.getKomSbor() != null) ? BigDecimal.valueOf(psg.getKomSbor() / 100.0) : BigDecimal.ZERO)
			.and("ob_strah", (psg.getObStrah() != null) ? BigDecimal.valueOf(psg.getObStrah() / 100.0) : BigDecimal.ZERO)
			.and("raft_tarif", (psg.getRaftFlag() == Boolean.TRUE) ? BigDecimal.valueOf(psg.getRaftTariff() / 100.0) : BigDecimal.ZERO)
			.and("transit", (psg.getTBTarif() != null) ? BigDecimal.valueOf(psg.getTBTarif() / 100.0) : BigDecimal.ZERO);
	}

	public static List<Passage> getPassages(Long depId, Date date, Long destId) throws SerializationException {
		String url = "";
		try {
			if (depId == null)
				return null;
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy");
			Station dest = DB.getModel(Station.class, destId);
			url = getHost(depId) + "?act=" + ACTION_GET_PASSAGES + "&date=" + df.format(date) + "&kpp=" + dest.getKpasId() + "";
			String xml;
			xml = getXml(url);
			List<Passage> ret = getObjects(Passage.class, xml);
			SimpleDateFormat incomingDateFormat = new SimpleDateFormat("ddMMyy HH:mm");
			Expression ap = new Expression(StoredProcs.core.getProp("adult_price_formula"));
			Expression cp = new Expression(StoredProcs.core.getProp("child_price_formula"));
			Expression bp = new Expression(StoredProcs.core.getProp("bag_price_formula"));

			Expression apExpr;
			Expression cpExpr;
			Expression bpExpr;
			for (Passage psg : ret) {
				psg.setDeparture(incomingDateFormat.parse(psg.getStringProp(Passage.DEPARTURE)));
				psg.setArrival(incomingDateFormat.parse(psg.getStringProp(Passage.ARRIVAL)));

				Formula f = DB.getModel(Formula.class, Where.equals(Formula.PASSAGE_ID, psg.getId()));
				if (f != null) {
					apExpr = new Expression(f.getAdultPriceFormula());
					cpExpr = new Expression(f.getChildPriceFormula());
					bpExpr = new Expression(f.getBagPriceFormula());
				} else {
					apExpr = ap;
					cpExpr = cp;
					bpExpr = bp;
				}

				if ((psg.getTariff() == null) || psg.getTariff().equals(0L))
					psg.setSumm(0L);
				else {
					fillExpression(apExpr, psg);
					psg.setSumm(apExpr.eval().multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).longValue());
				}

				if ((psg.getPrefTariff() == null) || psg.getPrefTariff().equals(0L))
					psg.setChldSumm(0L);
				else {
					fillExpression(cpExpr, psg);
					psg.setChldSumm(cpExpr.eval().multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).longValue());
				}

				if ((psg.getBagTariff() == null) || psg.getBagTariff().equals(0L))
					psg.setBagSumm(0L);
				else {
					fillExpression(bpExpr, psg);
					psg.setBagSumm(bpExpr.eval().multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).longValue());
				}
				psg.setPassageType(PassageType.KPAS);
			}
			return ret;
		} catch (AvProtocolException e) {
			logger.warn("Ошибка при получении рейсов\n" + url + "\n" + e.getMessage());
			throw new SerializationException("Невозможно получить рейсы");
		} catch (Exception e) {
			logger.error(url);
			logger.error("Ошибка при получении рейсов", e);
			throw new SerializationException("Невозможно получить рейсы");
		}
	}

//	private static <T extends BaseModel> T getObject(Class<T> clazz, String value) throws Exception {
//		try {
//			Document doc = DocumentHelper.parseText(value);
//			Element root = (Element) doc.getRootElement();
//			if (!"error".equals(((Element) root.elements().get(0)).getName())) {
//				return parseElement(clazz, root);
//			} else
//				throw new AvProtocolException(((Element) root.elements().get(0)).attributeValue("code"));
//		} catch (DocumentException e) {
//			logger.error("Ошибка разбора документа", e);
//			throw new SerializationException("Неверный формат данных");
//		}
//	}

	@SuppressWarnings("unchecked")
	private static <T extends BaseModel> List<T> getObjects(Class<T> clazz, String value) throws Exception {
		try {
			List<T> ret = new ArrayList<T>();
			Document doc = DocumentHelper.parseText(value);
			Element root = (Element) doc.getRootElement();
			if (root != null && root.elements() != null && !root.elements().isEmpty()) {
				if (!"error".equals(((Element) root.elements().get(0)).getName())) {
					for (Element el : (List<Element>) root.elements()) {
						T model = parseElement(clazz, el);
						ret.add(model);
					}
				} else
					throw new AvProtocolException(((Element) root.elements().get(0)).attributeValue("code"));
			}
			return ret;
		} catch (DocumentException e) {
			logger.error("Ошибка разбора документа", e);
			throw new SerializationException("Неверный формат данных");
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends BaseModel> T parseElement(Class<T> clazz, Element root) throws Exception {
		try {
			T bm = clazz.newInstance();
			bm.set("elementName", root.getName());
			for (int i = 0; i < root.attributeCount(); i++) {
				String attn = root.attribute(i).getName();
				String attv = root.attributeValue(attn);
				bm.set(attn, attv);
			}
			if ((root.elements() != null) && !root.elements().isEmpty())
				for (Element el : (List<Element>) root.elements()) {
					if (el.getName().contains("list") && el.elements().size() > 0) {
						List<BaseModel> lst = new ArrayList<BaseModel>();
						for (Element subel : (List<Element>) el.elements())
							lst.add(parseElement(BaseModel.class, subel));
						bm.set(el.getName(), lst);
					} else if (el.attributes().size() > 0 || el.elements().size() > 0) {
						bm.set(el.getName(), parseElement(BaseModel.class, el));
					} else {
						if (el.getName().contains("rafttariff") && !el.getTextTrim().contains("."))
							bm.set(el.getName(), el.getTextTrim() + "00");
						else
							bm.set(el.getName(), el.getTextTrim().replace(".", ""));
					}
				}
			else {
				bm.put(Station.NAME, root.getTextTrim());
				bm.put(BaseModel.DISPLAY_FIELD, root.getTextTrim());
			}

			return bm;
		} catch (Exception e) {
			logger.error("", e);
			throw new Exception(e);
		}
	}

	private static String toMoney(Long money) {
		return money / 100 + "." + (money % 100 < 10 ? "0" : "") + money % 100;
	}

	private static String getUrlParams(Ticket ticket, String act, String email) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd H:m");
		Station to = DB.getModel(Station.class, ticket.getToId());
		String ret = "?act=" + act
				+ "&nved=" + ticket.getNomerVedomosti()
				+ "&mesto=" + ticket.getSeat()
				+ "&nbilet=" + ticket.getId()
				+ "&tbilet=" + timeFormat.format(ticket.getSellTime())
				+ "&kpp=" + to.getKpasId()
				+ "&tarif=" + toMoney(ticket.getTarif())
				+ "&tariff=" + toMoney(ticket.getFactTarif())
				+ "&komsb=" + toMoney(ticket.getKomSbor())
				+ "&obstr=" + toMoney(ticket.getObStrah())
				+ "&tipb=" + URLEncoder.encode(ticket.getTicketType(), ENCODING)
				+ "&parom=" + (ticket.getParom() ? "1" : "0")
				+ "&tarifp=" + toMoney(ticket.getParomTarif())
				+ "&tariffp=" + toMoney(ticket.getFactParomTarif())
				+ "&tipbp=" + URLEncoder.encode(ticket.getParomTicketType(), ENCODING)
				+ "&tip=" + /* URLEncoder.encode( */ticket.getDocument()/* , ENCODING) */
				+ "&ser=" + URLEncoder.encode(ticket.getSeriya().trim(), ENCODING)
				+ "&nom=" + URLEncoder.encode(ticket.getNumber().trim(), ENCODING)
				+ "&fam=" + URLEncoder.encode(ticket.getLastname(), ENCODING)
				+ "&im=" + URLEncoder.encode(ticket.getFirstname(), ENCODING)
				+ "&otch=" + URLEncoder.encode(ticket.getMiddlename(), ENCODING)
				+ "&d_birth=" + ((ticket.getBirthDate() != null) ? dateFormat.format(ticket.getBirthDate()) : "")
				+ "&nbiletg1=" + (ticket.getBag1() == null ? 0L : ticket.getBag1())
				+ "&nbiletg2=" + (ticket.getBag2() == null ? 0L : ticket.getBag2())
				+ "&tarifg=" + toMoney((ticket.getBagTarif() == null) ? 0L : ticket.getBagTarif())
				+ "&tbtarif=" + toMoney((ticket.getTBTarif() == null) ? 0L : ticket.getTBTarif())
				+ "&ustarif=" + toMoney(ticket.getPrice() - ticket.getTarif() - ticket.getKomSbor() - ticket.getObStrah() - ((ticket.getParom() == Boolean.TRUE) ? ticket.getParomTarif() : 0L) - ticket.getTBTarif()
						+ (ticket.getPriceBag() - ticket.getBagTarif()) * ticket.getBaggage())
				+ "&grajd=" + ticket.getGrajd()
				+ "&pol=" + URLEncoder.encode((ticket.getGender() == Gender.MALE) ? "м" : ((ticket.getGender() == Gender.FEMALE) ? "ж" : ""), ENCODING)
				+ "&tel=" + URLEncoder.encode(ticket.getPhone(), ENCODING)
				+ "&email=" + URLEncoder.encode(email, ENCODING);
		return ret.replace(" ", "%20");
	}

	public static String getSaleUrl(Ticket ticket, String act, String email) throws Exception {
		return getHost(ticket.getFromId()) + getUrlParams(ticket, act, email);
	}

	private static boolean sale(Ticket ticket, String act, String email) throws Exception {
		try {
			String url = getSaleUrl(ticket, act, email);
			logger.info(url);
			String xml = getXml(url);
			logger.info(xml);
			Document doc = DocumentHelper.parseText(xml);
			Element root = doc.getRootElement();
			return root.element("error") == null;
		} catch (Exception e) {
			logger.error("", e);
			throw e;
		}
	}

	public static boolean saleTicket(Ticket ticket, String email) throws Exception {
		return sale(ticket, ACTION_SELL_TICKET, email);
	}

	public static boolean confirmSale(Ticket ticket, String email) throws Exception {
		return sale(ticket, ACTION_SALE_TICKET, email);
	}

}