/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.server.utils;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.SerializationException;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 29.03.2013 15:18:30
 */
public class MailUtil {

	private static final Logger logger = LoggerFactory.getLogger(MailUtil.class.getName());

	private static final Authenticator authenticator = new Authenticator() {
		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(
					PropUtil.getSmtpUser(), PropUtil.getSmtpPassword());
		}
	};

	/**
	 * Разослать спам по адресам
	 * 
	 * @param auth
	 *            - параметры авторизации
	 * @param host
	 *            - хост SMTP
	 * @param port
	 *            - порт SMTP
	 * @param sysFrom
	 *            - поле "От кого", часто должно соответствовать почтовому
	 *            адресу, иначе не даст отправить
	 * @param recipients
	 *            - получатели, массив email адресов
	 * @param subject
	 *            - тема письма
	 * @param message
	 *            - текст письма
	 * @return true в случае успешного завершения, false - иначе
	 * @throws SerializationException
	 */
	private static Boolean sendMessage(Authenticator auth, String host, Integer port,
			String sysFrom, String[] recipients, String subject, String message)
			throws SerializationException {
		try {
			Properties props = new Properties();
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.ssl.enable", true);
			props.put("mail.smtp.socketFactory.port", port);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.fallback", "false");
			props.put("mail.debug", "false");

			Session session = Session.getInstance(props, auth);

			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(sysFrom));

			int i = 0;
			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (String recipient : recipients)
				addressTo[i++] = new InternetAddress(recipient);
			msg.setRecipients(Message.RecipientType.TO, addressTo);

			msg.setSubject(subject, StandardCharsets.UTF_8.name());
			msg.setContent(message, "text/html; charset=utf-8");
			Transport.send(msg);
			logger.info("Отправлено письмо для " + CommonServerUtils.joinToStr(recipients) + " (" + subject + ")");
			return true;
		} catch (MessagingException e) {
			logger.error("", e);
			return false;
		}
	}

	public static Boolean sendMessage(String[] recipients, String subject,
			String message) throws SerializationException {
		return sendMessage(authenticator, PropUtil.getSmtpHost(), PropUtil.getSmtpPort(),
				PropUtil.getMailFromAddr(), recipients, subject, message);
	}

	public static Boolean sendMessage(String recipient, String subject,
			String message) throws SerializationException {
		return sendMessage(new String[] { recipient }, subject, message);
	}

}