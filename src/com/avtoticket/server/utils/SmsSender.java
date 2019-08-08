package com.avtoticket.server.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.OptionalParameter.Tag;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.shared.models.core.Sms;

import org.jsmpp.bean.OptionalParameter.OctetString;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 25.07.2014 22:30:20
 */
public class SmsSender {

	private static Logger logger = LoggerFactory.getLogger(SmsSender.class);

	private static final TypeOfNumber SENDER_TON = TypeOfNumber.ALPHANUMERIC;
	private static final NumberingPlanIndicator SENDER_NPI = NumberingPlanIndicator.ISDN;

	private static final CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();

	private static boolean isPureAscii(String text) {
		return asciiEncoder.canEncode(text);
	}

	private static SMPPSession session;
	/** Слушатель для СМС подтверждения */
	private static MessageReceiverListener listener = new MessageReceiverListener() {
		@Override
		public DataSmResult onAcceptDataSm(DataSm dataSm, Session source) throws ProcessRequestException {
			logger.info("+++++++++ onAcceptDataSm " + dataSm);
			return null;
		}

		@Override
		public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
			String msg = new String(deliverSm.getShortMessage());
			logger.info("onAcceptDeliverSm - " + msg);
			String id = msg.substring(msg.indexOf(" id:") + 4);
			id = id.substring(0, id.indexOf(' '));
			if (!id.isEmpty()) {
				String stat = msg.substring(msg.indexOf(" stat:") + 6);
				stat = stat.substring(0, stat.indexOf(' '));
				Long stat_code =
						"DELIVRD".equalsIgnoreCase(stat) ? Sms.Status_Delivered :
						("UNDELIV".equalsIgnoreCase(stat) ? Sms.Status_Undelivered :
						("EXPIRED".equalsIgnoreCase(stat) ? Sms.Status_Expired : Sms.Status_Error));
				logger.info("sms.update_status(777, '" + id + "', " + stat_code + ", '" + msg + "'::text)");
//				try {
//					DB.execFunc("sms.update_status(777, '" + id + "', " + stat_code + ", '" + msg + "'::text)");
//				} catch (Exception e) {
//					logger.error("Возникла ошибка при обновлении статуса сообщения", e);
//				}
			}
		}

		@Override
		public void onAcceptAlertNotification(AlertNotification alertNotification) {
			logger.info("+++++++++ onAcceptAlertNotification " + alertNotification);
		}
    };

	public static synchronized void sendMessage(Long uid, String phone, String message) throws Exception {
		sendMessage(uid, phone, message, isPureAscii(message));
	}

	/**
	 * Отправить смс
	 * 
	 * @param recipient - получатель, телефонный номер в формате (7xxxnnnnnnn)
	 * @param message - тело сообщения
	 * @param numberType - тип номера-отправителя, false если циферный (интернациональный), true если цифробуквенный
	 * @param langType - true если латиница, false если не латиница
	 * @return идентификаторы сообщений, если что-то пошло не так - то строка с расшифровкой
	 * @throws Exception 
	 */
	private static String sendMessage(Long uid, String recipient, String message, Boolean langType) throws Exception {
		if ((recipient == null) || recipient.isEmpty() || (message == null))
			return null;
		if ((session == null) || (session.getSessionState() == SessionState.CLOSED))
			connectAndBind();

        try {
//    		Address[] rec = new Address[1];
//    		rec[0] = new Address(TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.ISDN, recip);
//            SubmitMultiResult res = session.submitMultiple("cpa", SENDER_TON, SENDER_NPI, SENDER, rec,
//              		new ESMClass(), (byte) 0, (byte) 1, null, null, new RegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE),
//              		ReplaceIfPresentFlag.DEFAULT, new GeneralDataCoding(langType ? Alphabet.ALPHA_DEFAULT : Alphabet.ALPHA_UCS2, MessageClass.CLASS1, false),
//              		(byte) 0, msg.getBytes("UTF-8"), new OptionalParameter[0]);
//            String id = res.getMessageId();

//    		QuerySmResult res = session.queryShortMessage("2173549", SENDER_TON, SENDER_NPI, SENDER);
//    		String id = res.getFinalDate();

            String id = session.submitShortMessage("cpa", SENDER_TON, SENDER_NPI, PropUtil.getSmsFromAddr(), 
        			TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.ISDN, recipient, new ESMClass(), (byte) 0, (byte) 1, null, 
        			null, new RegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE), (byte) 0, 
        			new GeneralDataCoding(langType ? Alphabet.ALPHA_DEFAULT : Alphabet.ALPHA_UCS2, MessageClass.CLASS1, false), (byte) 0,
        			"".getBytes("UTF-8"), new OctetString(Tag.MESSAGE_PAYLOAD.code(), message, StandardCharsets.UTF_8.name()));
            logger.info("Message for " + recipient + " submitted, message_id is " + id);

            Sms sms = new Sms();
            sms.setSenderId(uid);
            sms.setPhone(recipient);
            sms.setMsg(message);
            sms.setSendDate(new Date());
            sms.setStatusCode(Sms.Status_Sended);
            sms.setSmsId(id);
            sms.setIsLatin(langType);
            int sub;
            if (langType) {
            	if (message.length() <= 160)
            		sub = 1;
            	else
            		sub = (message.length() - 1) / 153 + 1;
            } else {
            	if (message.length() <= 70)
            		sub = 1;
            	else
            		sub = (message.length() - 1) / 67 + 1;
            }
            sms.setSubSmsCount((long) sub);
            logger.info(sms.toString());
//            DB.save(sms, uid);
            return id;

//          QuerySmResult sm = session.queryShortMessage("439279700", TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.ISDN, "79282227029");
//          MessageState ms = sm.getMessageState();
//          log.info(ms.name());
//          log.info(sm.getErrorCode());
        } catch (PDUException e) {
            logger.error("Invalid PDU parameter", e);
            throw new Exception("Invalid PDU parameter");
        } catch (ResponseTimeoutException e) {
            logger.error("Response timeout", e);
            throw new Exception("Response timeout");
        } catch (InvalidResponseException e) {
            logger.error("Receive invalid respose", e);
            throw new Exception("Receive invalid respose");
        } catch (NegativeResponseException e) {
        	switch (e.getCommandStatus()) {
			case 0x0b:
				logger.error("Неправильный номер получателя", e);
	            throw new Exception("Неправильный номер получателя");
			case 0x0d:
				logger.error("Ошибка при подключении к шлюзу", e);
	            throw new Exception("Ошибка при подключении к шлюзу");
			case 0x0e:
				logger.error("Неправильный пароль", e);
	            throw new Exception("Неправильный пароль");
			case 0x0f:
				logger.error("Неправильный логин", e);
	            throw new Exception("Неправильный логин");
			default:
	            logger.error("Receive negative response", e);
	            throw new Exception(e.getMessage());
			}
        } catch (IOException e) {
            logger.error("IO error occur", e);
            throw new Exception("IO error occur");
        } catch (Exception e) {
            logger.error("Something error occur", e);
            throw e;
		}
	}

	private static void connectAndBind() throws Exception {
		session = new SMPPSession();
        session.setTransactionTimer(20000);
        session.setMessageReceiverListener(listener);
		try {
			session.connectAndBind(PropUtil.getSmppHost(), PropUtil.getSmppPort(),
					new BindParameter(BindType.BIND_TRX, PropUtil.getSmppUser(), PropUtil.getSmppPassword(), "cpa", SENDER_TON, SENDER_NPI, null));
		} catch (IOException e) {
			throw new Exception("Ошибка при подключении к шлюзу", e);
		}
	}

	public synchronized static void stop() {
		if ((session != null) && (session.getSessionState() != SessionState.CLOSED))
			session.unbindAndClose();
	}

}