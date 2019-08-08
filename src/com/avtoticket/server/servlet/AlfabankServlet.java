/*
 * Copyright Avtoticket (c) 2017.
 */
package com.avtoticket.server.servlet;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsmpp.extra.SessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.db.DB;
import com.avtoticket.server.db.stored.StoredProcs;
import com.avtoticket.server.utils.KpasProtocolUtil;
import com.avtoticket.server.utils.MailUtil;
import com.avtoticket.server.utils.PropUtil;
import com.avtoticket.server.utils.SmsSender;
import com.avtoticket.server.utils.StavrProtocolUtil;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.Bill;
import com.avtoticket.shared.models.core.Ticket;
import com.avtoticket.shared.models.core.TicketStatus;

import ru.abyss.acquiring.soap.AbstractAcquiringServlet;
import ru.abyss.acquiring.soap.AcquiringUtil;
import ru.paymentgate.engine.webservices.merchant.GetOrderStatusExtendedResponse;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 02 окт. 2017 г. 00:18:08
 */
public class AlfabankServlet extends AbstractAcquiringServlet {

	private static final long serialVersionUID = 5244411147841638090L;

	private static Logger logger = LoggerFactory.getLogger(AlfabankServlet.class.getName());

	private static final int RETRY_PERIOD = 300000;
	private static final int RETRY_COUNT = 24;
	private static final Object SYNC_BILL = new Object();

	private boolean confirm(HttpServletRequest req, Long id, String hash) throws Exception {
		boolean success = true;

		Bill bill = DB.getModel(Bill.class, id);
		List<Ticket> tickets = DB.getModels(Ticket.class, Where.equals(Ticket.BILL_ID, id), false);
		if (PropUtil.isProduction()) {
			if (bill.getStavBookingId().isEmpty()) {
				for (Ticket t : tickets)
					if (t.getStatus() != TicketStatus.SOLD) {
						boolean ret = KpasProtocolUtil.confirmSale(t, bill.getEmail());
						success &= ret;
						t.setStatus(ret ? TicketStatus.SOLD : TicketStatus.ERROR);
						DB.save(t, -1L);
					}
			} else {
				List<Ticket> ret = StavrProtocolUtil.pay(bill.getStavBookingId());
				for (Ticket t : ret) {
					Optional<Ticket> tkt = tickets.stream().filter(r -> Objects.equals(r.getSeat(), t.getSeat())).findFirst();
					if (tkt.isPresent()) {
						tkt.get().setStavTicketNumber(t.getStavNumberTicket());
						String[] bags = t.getStavBaggageNumber().split("\\^");
						if ((bags != null) && (bags.length > 0)) {
							if (!bags[0].trim().isEmpty())
								tkt.get().setStavBag1(bags[0]);
							if ((bags.length > 1) && !bags[1].trim().isEmpty())
								tkt.get().setStavBag2(bags[1]);
						}
					}
				}
				tickets.forEach(t -> t.setStatus(TicketStatus.SOLD));
				DB.save(tickets, -1L);
			}
		}

		if (success) {
			if (bill != null) {
				bill.setBpStatus(TicketStatus.SOLD);
				DB.save(bill, -1L);

				logger.info("Оповещение на мыло '" + bill.getEmail() + "'");
				if (bill.getEmail().contains("@")) {
					String domain = req.getScheme() + "://" + req.getServerName() + ((req.getServerPort() != 80) ? ":" + req.getServerPort() : "");
					String ticketUrl = domain + "/#tickets/" + hash;
					String printUrl = domain + "/report?format=pdf&sign=tickets&hash=" + hash;
					MailUtil.sendMessage(bill.getEmail(), "Купленный билет",
						"Купленный билет: <a href='" + ticketUrl + "'>" + ticketUrl + "</a>"
								+ "<br><br>Печать билета: <a href='" + printUrl + "'>" + printUrl + "</a>");
				}

				logger.info("Оповещение на телефон " + bill.getPhone());
				StringBuilder phone = new StringBuilder();
				for (char c : bill.getPhone().toCharArray())
					if ((c >= '0') && (c <= '9'))
						phone.append(c);
				if (phone.length() == 10)
					phone.insert(0, "+7");
				if ((phone.length() == 11) && ((phone.charAt(0) == '7') || (phone.charAt(0) == '8'))) {
					phone.deleteCharAt(0);
					phone.insert(0, "+7");
				}
				if ((phone.length() == 12) && phone.toString().startsWith("+7"))
					try {
						if (!tickets.isEmpty()) {
							SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
							Set<Long> seats = new TreeSet<Long>();
							for (Ticket t : tickets)
								if ((t.getTBTarif() == null) || t.getTBTarif().equals(0L))
									seats.add(t.getSeat());
							Ticket tkt = tickets.get(0);

							StringBuilder text = new StringBuilder("Рейс: ")
									.append(tkt.getFrom())
									.append("-")
									.append(tkt.getTo())
									.append("\nМесто: ");
							if (seats.isEmpty())
								text.append("б/м");
							else {
								for (Long s : seats)
									text.append(s).append(",");
								text.delete(text.length() - 1, text.length());
							}
							text.append("\nОтправление: ")
								.append(formatter.format(tkt.getDeparture()))
								.append("\nПрибытие: ")
								.append(formatter.format(tkt.getArrival()))
								.append("\nСчастливого пути!");

							SmsSender.sendMessage(id, phone.toString(), text.toString());
						}
					} catch (Exception e) {
						logger.error("Ошибка при отправке смс (" + id + "; " + bill.getPhone() + ")", e);
					}
				else
					logger.warn("Неверный формат телефона " + bill.getPhone());
			}
		}

		return success;
	}

	private String getSalt(String text) {
		try {
			return StoredProcs.core.saltmd5(text);
		} catch (Exception e) {
			logger.error("", e);
			return "";
		}
	}

	private TimerTask createTask(HttpServletRequest req, Long id, String hash) {
		return new TimerTask() {
			private int ticks = 0;

			@Override
			public void run() {
				ticks++;
				try {
					boolean success = confirm(req, id, hash);
					logger.info("rbresult process bill: " + id + "; recheck status: " + success);
					if (success)
						cancel();
					else if (ticks >= RETRY_COUNT)
						throw new Exception("Превышено количество попыток обращения к вокзалу");
				} catch (Exception e) {
					if (ticks >= RETRY_COUNT) {
						cancel();

						String ticketUrl = "http://www.avtoticket.com/#tickets/" + hash;
						String printUrl = "http://www.avtoticket.com/report?format=pdf&sign=tickets&hash=" + hash;
						try {
							Bill bill = DB.getModel(Bill.class, id);
							String msg = "Билет: <a href='" + ticketUrl + "'>" + ticketUrl + "</a>"
									+ "<br><br>Печать билета: <a href='" + printUrl + "'>" + printUrl + "</a>"
									+ "<br><br>Выполнялись запросы:";
							if (bill.getStavBookingId().isEmpty())
								for (Ticket t : DB.getModels(Ticket.class, Where.equals(Ticket.BILL_ID, id), false))
									msg += "<br>" + ((t.getStatus() == TicketStatus.IN_PROCESSING) ? "Ожидает: "
											: ((t.getStatus() == TicketStatus.SOLD) ? "Успешно: "
											: ((t.getStatus() == TicketStatus.ERROR) ? "Ошибка: " : String.valueOf(t.getStatus()) + ": ")))
										+ KpasProtocolUtil.getSaleUrl(t, KpasProtocolUtil.ACTION_SALE_TICKET, bill.getEmail());
							else
								msg += "<br>" + "Ошибка: " + StavrProtocolUtil.getPayUrl(bill.getStavBookingId());
							MailUtil.sendMessage(new String[] {"af_kpas@mail.ru", "minu-moto@mail.ru"}, "Ошибка подтверждения покупки билета", msg);

							bill.setBpStatus(TicketStatus.ERROR);
							DB.save(bill, -1L);
						} catch (Exception e1) {
							logger.error("", e1);
						}
					}
					logger.error("", e);
				}
			}
		};
	}

	@Override
	protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, String orderId, GetOrderStatusExtendedResponse status) {
		if (!PropUtil.isProduction())
			try {
				AcquiringUtil.reverseOrder(orderId, null);
				logger.info("rbresult process bill: " + status.getOrderNumber() + "; cancel: " + status.getAmount());
			} catch (Exception e) {
				logger.error("Возникла ошибка при отмене оплаты", e);
			}

		Long lsumm = status.getAmount();
		String sid = status.getOrderNumber();
		String hash = getSalt(sid);
		Long id = Long.valueOf(sid);
		TimerTask retryTask = createTask(req, id, hash);

		try {
			Bill bill;
			synchronized (SYNC_BILL) {
				bill = DB.getModel(Bill.class, id);
				if ((bill == null) || !Objects.equals(bill.getAmount(), lsumm)) {
					if (bill != null) {
						bill.setBpStatus(TicketStatus.ERROR);
						DB.save(bill, -1L);
					}
					logger.info("rbresult process bill: " + id + "; error: wrong operation");
					response(resp, 500, "text/html", "ERR:wrong operation");
					return;
				}
				if (bill.getBpStatus() != TicketStatus.IN_PROCESSING) {
					resp.sendRedirect(bill.getSuccessURL() + hash);
					return;
				} else {
					bill.setBpStatus(TicketStatus.RESERVED);
					DB.save(bill, -1L);
				}
			}

			logger.info("rbresult process bill: " + id + "; summ: " + lsumm);
			boolean success = confirm(req, id, hash);
			logger.info("rbresult process bill: " + id + "; status: " + success);
			if (!success)
				new Timer().schedule(retryTask, RETRY_PERIOD, RETRY_PERIOD);
			resp.sendRedirect(bill.getSuccessURL() + hash);
		} catch (Exception e) {
			new Timer().schedule(retryTask, RETRY_PERIOD, RETRY_PERIOD);
			logger.error("ID билета: " + sid, e);
		}
	}

	@Override
	protected void onFailure(HttpServletRequest req, HttpServletResponse resp, String orderId, String message, GetOrderStatusExtendedResponse status) {
		logger.info("Платёж отклонён: orderId=" + orderId);
		try {
			Bill bill;
			synchronized (SYNC_BILL) {
				bill = DB.getModel(Bill.class, Long.valueOf(status.getOrderNumber()));
				bill.setBpStatus(TicketStatus.CANCELED);
				DB.save(bill, -1L);
			}
			response(resp, "Платёж отклонён", "<a href='" + bill.getFailURL() + "'>Вернуться на сайт</a>");
		} catch (Exception e) {
			logger.error("", e);
			response(resp, "Платёж отклонён", e.getMessage() + "<br><a href='" + req.getScheme() + "://" + req.getServerName() + ((req.getServerPort() != 80) ? ":" + req.getServerPort() : "") + "'>Вернуться на сайт</a>");
		}
	}

}