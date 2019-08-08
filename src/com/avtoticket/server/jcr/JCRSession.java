/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.server.jcr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.SerializationException;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 23.03.2012 16:18:30
 */
public class JCRSession {

	private static Logger logger = LoggerFactory.getLogger(JCRSession.class.getName());

	private Session session = null;
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private Lock readLock = lock.readLock();
	private Lock writeLock = lock.writeLock();

	/**
	 * Сессия доступа в репозиторий
	 * 
	 * @param repository
	 *            - сам репозиторий, с которым создаётся сессия
	 */
	public JCRSession(Repository repository) {
		// логинимся в репозиторий
		try {
			session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()), "default");
		} catch (Exception e) {
			logger.error("Ошибка при создании сессии OAK", e);
		}
	}

	/**
	 * Закрывает кроличью сессию
	 */
	public void closeSession() {
		lockWrite();
		try {
			if (session != null) {
				session.save();
				session.logout();
				while (session.isLive()) {
					logger.error("ААААААААААААА! Кернел паник!!! Сессия осталась жива " + session);
					Thread.sleep(6000);
					session.logout();
				}
				logger.info("Сессия репозитория закрыта");
			}
		} catch (Exception e) {
			logger.error("Ошибка при завершении сессии", e);
		} finally {
			unlockWrite();
		}
	}

	public void refresh() {
		try {
			session.refresh(true);
		} catch (RepositoryException e) {
			logger.error("Ошибка при обновлении сессии", e);
		}
	}

	/**
	 * Заблокировать сессию для изменений
	 */
	public void lockWrite() {
		writeLock.lock();
	}

	/**
	 * Разблокировать сессию после записи
	 */
	public void unlockWrite() {
		writeLock.unlock();
	}

	/**
	 * Заблокировать сессию для чтения
	 */
	public void lockRead() {
		readLock.lock();
	}

	/**
	 * Разблокировать сессию после чтения
	 */
	public void unlockRead() {
		readLock.unlock();
	}

	/**
	 * Сохранить изменения в сессии
	 */
	public void save() throws Exception {
		if (session != null)
			session.save();
	}

	/**
	 * Удалить элемент
	 * 
	 * @param nodeId
	 *            - идентификатор узла
	 * @return true - если всё хорошо
	 * @throws Exception
	 */
	public Boolean delNode(String nodeId) {
		if ((session == null) || (nodeId == null) || nodeId.isEmpty())
			return false;
		lockRead();
		try {
			Node node = session.getNodeByIdentifier(nodeId);
			if (node != null) {
				unlockRead();
				// переключаемся на запись
				lockWrite();
				try {
					// повторная проверка, тк. узел мог быть удалён пока мы получали доступ на запись
					node = session.getNodeByIdentifier(nodeId);
					if (node != null) {
						node.remove();
						save();
					}
					return true;
				} finally {
					lockRead();
					unlockWrite();
				}
			} else
				return false;
		} catch (Exception e) {
			logger.error("Возникла ошибка при удалении элемента из хранилища", e);
			return false;
		} finally {
			unlockRead();
		}
	}

	/**
	 * Удалить элементы
	 *
	 * @param nodeIds
	 *            - идентификаторы удаляемых узлов
	 * @throws Exception
	 */
	public void delNodes(List<String> nodeIds) throws Exception {
		if ((session == null) || (nodeIds == null) || nodeIds.isEmpty())
			return;
		lockWrite();
		try {
			for (String id : nodeIds) {
				Node node = session.getNodeByIdentifier(id);
				if (node != null)
					node.remove();
			}
			save();
		} catch (Exception e) {
			logger.error("Возникла ошибка при удалении элементов из хранилища", e);
			throw e;
		} finally {
			unlockWrite();
		}
	}

	/**
	 * Получить элемент из репозитория
	 * 
	 * @param nodeId
	 *            - ID элемента
	 * @return искомый элемент репозитория
	 * @throws Exception
	 */
	public Node getNode(String nodeId) throws Exception {
		if ((session == null) || (nodeId == null) || nodeId.isEmpty())
			return null;
		Node ret = null;
		lockRead();
		try {
			ret = session.getNodeByIdentifier(nodeId);
		} catch (ItemNotFoundException e) {
			throw new SerializationException("Элемент " + nodeId + " не найден");
		} catch (Exception e) {
			logger.error("Возникла ошибка при получении элемента из хранилища", e);
			throw new Exception("Возникла ошибка при получении элемента из хранилища");
		} finally {
			unlockRead();
		}
		return ret;
	}

	/**
	 * Получить корневой элемент репозитория
	 * 
	 * @return корневой элемент репозитория
	 * @throws Exception
	 */
	public Node getRootNode() throws Exception {
		if (session == null)
			return null;
		Node ret = null;
		lockRead();
		try {
			ret = session.getRootNode();
		} catch (Exception e) {
			logger.error("Возникла ошибка при получении элемента из хранилища", e);
			throw new Exception("Возникла ошибка при получении элемента из хранилища");
		} finally {
			unlockRead();
		}
		return ret;
	}

	public List<Node> execQuery(String query, Long offset, Long limit) throws Exception {
		List<Node> ret = new ArrayList<Node>();
		try {
			if (session == null)
				return null;
			Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
			if (limit != null)
				q.setLimit(limit);
			if (offset != null)
				q.setOffset(offset);
        	QueryResult result = q.execute();
        	for (RowIterator it = result.getRows(); it.hasNext(); )
        		ret.add(it.nextRow().getNode());
		} catch (Exception e) {
			logger.error("Возникла ошибка при выполнении запроса к хранилищу данных", e);
			throw new Exception("Возникла ошибка при выполнении запроса к хранилищу данных");
		}
		return ret;
	}

	public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
		return session.getWorkspace().getNamespaceRegistry();
	}

	public NodeTypeManager getNodeTypeManager() throws RepositoryException {
		return session.getWorkspace().getNodeTypeManager();
	}

	public ValueFactory getValueFactory() throws RepositoryException {
		return session.getValueFactory();
	}

}