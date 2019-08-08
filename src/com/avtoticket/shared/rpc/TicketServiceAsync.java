/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.shared.rpc;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.PageContainer;
import com.avtoticket.shared.models.UUID;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.DocType;
import com.avtoticket.shared.models.core.Help;
import com.avtoticket.shared.models.core.HelpType;
import com.avtoticket.shared.models.core.Nationality;
import com.avtoticket.shared.models.core.News;
import com.avtoticket.shared.models.core.Passage;
import com.avtoticket.shared.models.core.Requisite;
import com.avtoticket.shared.models.core.Station;
import com.avtoticket.shared.models.core.Ticket;
import com.avtoticket.shared.models.core.TicketStatus;
import com.avtoticket.shared.models.core.User;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 13 дек. 2015 г. 22:48:58
 */
public interface TicketServiceAsync {

	void getNews(String locale, int limit, int offset, AsyncCallback<PageContainer<News>> callback);

	void getStations(AsyncCallback<Map<Station, List<Station>>> callback);

	void getMapStations(AsyncCallback<Map<String, List<Station>>> callback);

	void login(String login, String password, boolean remember, AsyncCallback<Boolean> callback);

	void logout(AsyncCallback<Void> callback);

	void getInitData(AsyncCallback<BaseModel> callback);

	void touch(AsyncCallback<Date> callback);

	void regUser(String login, String pass, AsyncCallback<Void> callback);

	void restorePassword(String login, AsyncCallback<Void> callback);

	void isLoginFree(String login, AsyncCallback<Boolean> callback);

	void updateUser(User user, AsyncCallback<Void> callback);

	void changePassword(String oldpass, String newpass, AsyncCallback<Boolean> callback);

	void getPassages(Long depId, Date date, Long destId, AsyncCallback<List<Passage>> callback);

	void getPassage(Long id, Long depId, Long destId, String depDate, AsyncCallback<Passage> callback);

	void getNationalities(AsyncCallback<List<Nationality>> callback);

	void getDocTypes(AsyncCallback<List<DocType>> callback);

	void getTickets(TicketStatus status, int limit, int offset, AsyncCallback<PageContainer<Ticket>> callback);

	void getTicketCounts(AsyncCallback<Map<TicketStatus, Long>> callback);

	void setProp(String name, String value, AsyncCallback<Void> callback);

	void getHelp(Long id, AsyncCallback<Help> callback);

	void getHelp(String locale, AsyncCallback<Map<HelpType, List<Help>>> callback);

	void buyTicket(List<User> usrs, Long id, Long depId, Long destId, Date depDate, AsyncCallback<String> callback);

	void getTicketsByHash(String hash, AsyncCallback<List<Ticket>> callback);

	<T extends BaseModel> void getKeyValueObjects(String className, AsyncCallback<List<T>> callback);

	<T extends BaseModel> void getPagedModels(String className, List<Long> forDel, Where where,
			String sortColumn, AsyncCallback<PageContainer<T>> callback);

	void saveModel(BaseModel obj, AsyncCallback<Long> callback);

	void delModel(BaseModel model, AsyncCallback<Void> callback);

	void generateApiToken(AsyncCallback<UUID> callback);

	void getRequisites(AsyncCallback<List<Requisite>> callback);

	void saveRequisite(Requisite req, AsyncCallback<Requisite> callback);

	void getRequisites(List<Long> forDel, Where where, String sortColumn,
			AsyncCallback<PageContainer<Requisite>> callback);

}