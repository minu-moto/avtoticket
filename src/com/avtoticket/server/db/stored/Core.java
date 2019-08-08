/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.server.db.stored;

import java.util.Date;
import java.util.List;

import com.avtoticket.shared.models.core.Gender;
import com.avtoticket.shared.models.core.Ticket;
import com.avtoticket.shared.models.core.TicketStatus;
import com.avtoticket.shared.models.core.User;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 23 дек. 2015 г. 22:46:11
 */
public interface Core {

	User getUser(String login) throws Exception;

	Long getUser(String login, String password) throws Exception;

	Boolean isLoginFree(String login) throws Exception;

	void registerUser(String login, String password, String code) throws Exception;

	void registerUser(String login, String password, String code, String lastname, String firstname, String middlename, Long doctypeId,
			String docSeries, String docNumber, Date docIssue, String phone, Date birthdate, Gender gender, Long nationId) throws Exception;

	Long confirmRegistration(String login, String code) throws Exception;

	Boolean changePassword(String login, String oldPassword, String newPassword) throws Exception;

	List<Ticket> getTickets(Long userId, TicketStatus status, String hash) throws Exception;

	void setProp(String name, String value) throws Exception;

	String getProp(String name) throws Exception;

	String saltmd5(String pass) throws Exception;

	Long getBagid() throws Exception;

}