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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.SerializationException;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 13 дек. 2015 г. 22:50:17
 */
@RemoteServiceRelativePath("rpc")
public interface TicketService extends RemoteService {

	public PageContainer<News> getNews(String locale, int limit, int offset) throws SerializationException;

	public Map<Station, List<Station>> getStations() throws SerializationException;

	public Map<String, List<Station>> getMapStations() throws SerializationException;

	public Boolean login(String login, String password, boolean remember) throws SerializationException;

	public void logout() throws SerializationException;

	public BaseModel getInitData() throws SerializationException;

	public Date touch() throws SerializationException;

	public void regUser(String login, String pass) throws SerializationException;

	public void restorePassword(String login) throws SerializationException;

	public Boolean isLoginFree(String login) throws SerializationException;

	public void updateUser(User user) throws SerializationException;

	public Boolean changePassword(String oldpass, String newpass) throws SerializationException;

	public List<Passage> getPassages(Long depId, Date date, Long destId) throws SerializationException;

	public Passage getPassage(Long id, Long depId, Long destId, String depDate) throws SerializationException;

	public List<Nationality> getNationalities() throws SerializationException;

	public List<DocType> getDocTypes() throws SerializationException;

	public List<Requisite> getRequisites() throws SerializationException;

	public PageContainer<Requisite> getRequisites(List<Long> forDel, Where where, String sortColumn) throws SerializationException;

	public Requisite saveRequisite(Requisite req) throws SerializationException;

	public PageContainer<Ticket> getTickets(TicketStatus status, int limit, int offset) throws SerializationException;

	public Map<TicketStatus, Long> getTicketCounts() throws SerializationException;

	public void setProp(String name, String value) throws SerializationException;

	public Map<HelpType, List<Help>> getHelp(String locale) throws SerializationException;

	public Help getHelp(Long id) throws SerializationException;

	public String buyTicket(List<User> usrs, Long id, Long depId, Long destId, Date depDate) throws SerializationException;

	public List<Ticket> getTicketsByHash(String hash) throws SerializationException;

	public <T extends BaseModel> List<T> getKeyValueObjects(String className) throws SerializationException;

	public <T extends BaseModel> PageContainer<T> getPagedModels(String className, List<Long> forDel, Where where, String sortColumn) throws SerializationException;

	public <T extends BaseModel> Long saveModel(T obj) throws SerializationException;

	public <T extends BaseModel> void delModel(T model) throws SerializationException;

	public UUID generateApiToken() throws SerializationException;

}