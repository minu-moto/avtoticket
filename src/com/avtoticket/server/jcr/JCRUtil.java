/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.server.jcr;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.jcr.Binary;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.jcr.version.OnParentVersionAction;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.segment.SegmentNodeStore;
import org.apache.jackrabbit.oak.plugins.segment.file.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avtoticket.server.servlet.UtilServlet;
import com.avtoticket.server.utils.CastUtil;
import com.avtoticket.shared.models.core.ImageFile;
import com.avtoticket.shared.models.core.RepositoryItem;

/**
 * Follow the JackRabbit
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 23.03.2012 16:18:30
 */
public class JCRUtil {

	private static Logger logger = LoggerFactory.getLogger(JCRUtil.class.getName());

	private static final long jcrTimeout = logger.isDebugEnabled() ? 100 : 1000;

	public static final String REP_PREFIX = "avt";
	public static final String REP_NAMESPACE = "http://www.avtoticket.com/";

	/** Воркспейс для изображений в новостях */
	public static final String WORKSPACE_IMAGES = "images";
	/** Воркспейс для архивов с экспортированными заказами */
	public static final String WORKSPACE_EXPORTS = "exports";
	private static final String REPOSITORY_PATH = "repository/";
	private static FileStore fs;
	private static JCRSession session;
	private static JackrabbitRepository repository = null;

	/**
	 * Инициализация репозитория
	 */
	public static synchronized void init() {
		if (repository != null) {
			logger.error("Репозиторий уже инициализирован!");
			return;
		}
		try {
			String root = UtilServlet.getRootPath() + "../";	// для отладки под jetty создаём репозиторий в корне проекта
			if (root.indexOf("webapps") >= 0)					// для tomcat создаём репозиторий в папке с самим сервером
				root += "../";

//			OakFileDataStore fds = new OakFileDataStore();
//			fds.setPath("");
			fs = FileStore.builder(new File(root + REPOSITORY_PATH)).build();
			SegmentNodeStore ns = SegmentNodeStore.builder(fs).build();
			Oak oak = new Oak(ns);
			Jcr jcr = new Jcr(oak);
			repository = (JackrabbitRepository) jcr.createRepository();

			session = new JCRSession(repository);

			registerCustomNodeTypes(session);
		} catch (Exception e) {
			logger.error("Ошибка при инициализации репозитория", e);
		}
	}

	/**
	 * Завершение работы репозитория, очистка памяти
	 */
	public static void destroy() {
		logger.info("Завершаем работу репозитория");
		if (session != null) {
			logger.info("Закрываем сессию");
			session.closeSession();
			session = null;
		}
		if (repository != null) {
			logger.info("Закрываем репозиторий");
			repository.shutdown();
			logger.info("Репозиторий закрыт");
			repository = null;
		}
		if (fs != null) {
			logger.info("Закрываем файловое хранилище");
			fs.close();
			logger.info("Хранилище закрыто");
			fs = null;
		}
	}

	public static String getVersion() {
		return (repository != null) ? repository.getDescriptor(Repository.REP_NAME_DESC) + " " + repository.getDescriptor(Repository.REP_VERSION_DESC) : "";
	}

	private static void registerCustomNodeTypes(JCRSession session) {
		try {
			NamespaceRegistry nr = session.getNamespaceRegistry();
			try {
				nr.getURI(JCRUtil.REP_PREFIX);
			} catch (NamespaceException e) {
				nr.registerNamespace(JCRUtil.REP_PREFIX, JCRUtil.REP_NAMESPACE);
			}

			registerContentSize(session);
			registerImageFile(session);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private static void registerContentSize(JCRSession session) throws Exception {
		NodeTypeManager manager = session.getNodeTypeManager();
		if (manager.hasNodeType(RepositoryItem.MIX_CONTENT_SIZE))
			return;

        NodeTypeTemplate ntt = manager.createNodeTypeTemplate();
        ntt.setName(RepositoryItem.MIX_CONTENT_SIZE);
        ntt.setMixin(true);
        ntt.setOrderableChildNodes(false);

        @SuppressWarnings("unchecked")
		List<PropertyDefinitionTemplate> pdts = ntt.getPropertyDefinitionTemplates();
        pdts.add(createProperty(manager, RepositoryItem.SIZE, PropertyType.LONG));

        manager.registerNodeType(ntt, false);
	}

	private static void registerImageFile(JCRSession session) throws Exception {
		NodeTypeManager manager = session.getNodeTypeManager();
		if (manager.hasNodeType(ImageFile.MIX_IMAGE_FILE))
			return;

		NodeTypeTemplate ntt = manager.createNodeTypeTemplate();
        ntt.setName(ImageFile.MIX_IMAGE_FILE);
        ntt.setMixin(true);
        ntt.setOrderableChildNodes(false);

		@SuppressWarnings("unchecked")
		List<PropertyDefinitionTemplate> pdts = ntt.getPropertyDefinitionTemplates();
        pdts.add(createProperty(manager, ImageFile.WIDTH, PropertyType.LONG));
        pdts.add(createProperty(manager, ImageFile.HEIGHT, PropertyType.LONG));

        NodeDefinitionTemplate ndt = manager.createNodeDefinitionTemplate();
        ndt.setAutoCreated(false);
        ndt.setMandatory(false);
        ndt.setOnParentVersion(OnParentVersionAction.COPY);
        ndt.setRequiredPrimaryTypeNames(new String[] {NodeType.NT_RESOURCE});
        ndt.setName("*");
        ndt.setProtected(false);
        ndt.setSameNameSiblings(false);
        @SuppressWarnings("unchecked")
		List<NodeDefinitionTemplate> ndts = ntt.getNodeDefinitionTemplates();
        ndts.add(ndt);

        manager.registerNodeType(ntt, false);
	}

	private static PropertyDefinitionTemplate createProperty(NodeTypeManager manager, String name, int type) throws Exception {
		PropertyDefinitionTemplate ret = manager.createPropertyDefinitionTemplate();
		ret.setName(name);
		ret.setRequiredType(type);
		ret.setAutoCreated(false);
		ret.setMandatory(false);
		ret.setOnParentVersion(OnParentVersionAction.COPY);
		ret.setProtected(false);
		ret.setMultiple(false);
		return ret;
	}
	
	/**
	 * Получить сессию доступа к репозиторию
	 * 
	 * @param workspace - название воркспейса, сессию к которому хотим получить
	 * @return сессия доступа к репозиторию
	 * @throws Exception
	 */
	private static JCRSession getSession() throws Exception {
		return session;
	}

	/**
	 * Получить дату последнего изменения файла
	 * 
	 * @param file - файл
	 * @return дата последнего изменения
	 * @throws Exception
	 */
	public static Date getLastModified(Node file) throws Exception {
		Calendar c = JcrUtils.getLastModified(file);
		return (c != null) ? c.getTime() : null;
	}

	/**
	 * Проверить наличие заданного элемента, если его не существует, то создать
	 * 
	 * @param workspace
	 * 			  - название воркспейса, в котором ищем/создаём каталог
	 * @param parentId
	 *            - идентификатор просматриваемой папки (null для корневой папки)
	 * @param nodeName
	 *            - имя элемента
	 * @param descr
	 *            - описание
	 * @return новый или существующий узел
	 * @throws Exception
	 */
	private static Node getOrCreateFolderImpl(String parentId, String nodeName, String descr) throws Exception {
		JCRSession sess = getSession();

		// пробуем найти папку
		sess.lockRead();
		Node parent;
		try {
			if (parentId == null)
				parent = sess.getRootNode();
			else
				parent = sess.getNode(parentId);
			if (parent == null)
				return null;
			else if (parent.hasNode(nodeName))
				return parent.getNode(nodeName);
		} finally {
			sess.unlockRead();
		}

		// если папка не существует, то создаём её
		Node fld = null;
		sess.lockWrite();
		try {
			long time = new Date().getTime();
			fld = JcrUtils.getOrAddFolder(parent, nodeName);
			fld.addMixin(NodeType.MIX_TITLE);
			fld.setProperty(Property.JCR_TITLE, nodeName);
			if (descr != null)
				fld.setProperty(Property.JCR_DESCRIPTION, descr);
			long newTime = new Date().getTime();
			if (newTime - time >= jcrTimeout)
				logger.warn("repository getOrCreateFolderImpl getOrAddFolder " + (newTime - time) + "ms");
			time = newTime;
			sess.save();
			newTime = new Date().getTime();
			if (newTime - time >= jcrTimeout)
				logger.warn("repository getOrCreateFolderImpl save " + (newTime - time) + "ms");
		} finally {
			sess.unlockWrite();
		}
		return fld;
	}
	
//	/**
//	 * Проверить наличие заданного элемента, если его не существует, то создать
//	 * 
//	 * @param parentId
//	 *            - идентификатор просматриваемой папки (null для корневой папки)
//	 * @param nodeName
//	 *            - имя элемента
//	 * @param descr
//	 *            - описание
//	 * @return модель нового или существующего узла
//	 * @throws Exception
//	 */
//	public static RepositoryItem getOrCreateFolder(String workspace, String parentId, String nodeName, String descr) throws Exception {
//		return CastUtil.node2model(new RepositoryItem(), getOrCreateFolderImpl(workspace, parentId, nodeName, descr));
//	}

	/**
	 * Добавить файл
	 * 
	 * @param parentId
	 *            - идентификатор папки, в которую будет добавлен элемент (null
	 *            для корневой папки)
	 * @param nodeName
	 *            - имя файла
	 * @param content
	 *            - бинарный поток
	 * @param descr
	 *            - описание
	 * @return новый файл, либо null в случае неудачи
	 * @throws Exception
	 */
	private static Node addOrUpdateFileImpl(String parentId, 
			String nodeName, String title, String descr, InputStream content) throws Exception {
		JCRSession sess = getSession();
		Node parent;
		sess.lockRead();
		try {
			if (parentId == null)
				parent = sess.getRootNode();
			else
				parent = sess.getNode(parentId);
		} finally {
			sess.unlockRead();
		}
		if (parent == null)
			return null;

		Node fl = null;
		sess.lockWrite();
		try {
			fl = JcrUtils.putFile(parent, nodeName, "", content);
			fl.addMixin(RepositoryItem.MIX_CONTENT_SIZE);
			fl.setProperty(RepositoryItem.SIZE, getContentSize(fl));
			fl.addMixin(NodeType.MIX_TITLE);
			fl.setProperty(Property.JCR_TITLE, title);
			if (descr != null)
				fl.setProperty(Property.JCR_DESCRIPTION, descr);
			sess.save();
		} finally {
			sess.unlockWrite();
		}
		return fl;
	}

	/**
	 * Добавить или обновить файл
	 * 
	 * @param parentId
	 *            - идентификатор папки, в которую будет добавлен элемент (null
	 *            для корневой папки)
	 * @param nodeName
	 *            - имя файла
	 * @param content
	 *            - бинарный поток
	 * @param descr
	 *            - описание
	 * @return модель нового файла, либо null в случае неудачи
	 * @throws Exception
	 */
	public static RepositoryItem addOrUpdateFile(String parentId, 
			String nodeName, String title, String descr, InputStream content) throws Exception {
		return CastUtil.node2model(new RepositoryItem(), addOrUpdateFileImpl(parentId, nodeName, title, descr, content));
	}

	/**
	 * Удалить элемент
	 * 
	 * @param nodeId
	 *            - идентификатор узла
	 * @return true - если всё хорошо
	 * @throws Exception
	 */
	public static Boolean delNode(String nodeId) throws Exception {
		return getSession().delNode(nodeId);
	}

	/**
	 * Удалить элементы
	 * 
	 * @param nodeIds
	 *            - идентификаторы удаляемых узлов
	 * @throws Exception
	 */
	public static void delNodes(List<String> nodeIds) throws Exception {
		getSession().delNodes(nodeIds);
	}

	/**
	 * Получить элемент по идентификатору
	 * 
	 * @param nodeId
	 *            - ID элемента
	 * @return искомый элемент репозитория
	 * @throws Exception
	 */
	public static RepositoryItem getNode(String nodeId) throws Exception {
		return CastUtil.node2model(new RepositoryItem(), getSession().getNode(nodeId));
	}

	/**
	 * Получить элемент по имени
	 * 
	 * @param parentId
	 * 			  - идентификатор просматриваемой папки (null - корень)
	 * @param name
	 *            - название элемента
	 * @return искомый элемент репозитория
	 * @throws Exception
	 */
	public static RepositoryItem getNode(String parentId, String name) throws Exception {
		Node nd;
		JCRSession sess = getSession();
		sess.lockRead();
		try {
			if (parentId == null)
				nd = sess.getRootNode();
			else
				nd = sess.getNode(parentId);
			if (nd != null)
				nd = nd.getNode(name);
		} finally {
			sess.unlockRead();
		}
		return CastUtil.node2model(new RepositoryItem(), nd);
	}

	/**
	 * Получить содержимое папки
	 * 
	 * @param nodeId
	 *            - идентификатор просматриваемой папки (null для корневой папки)
	 * @param nodeType
	 *            - тип получаемых элементов (null для всех элементов, пустой
	 *            для всех файлов)
	 * @return список вложенных элементов
	 * @throws Exception
	 */
	public static List<RepositoryItem> getNodes(String nodeId, String nodeType) {
		List<RepositoryItem> ret = null;
		try {
			Node node;
			JCRSession sess = getSession();
			sess.lockRead();
			try {
				if (nodeId == null)
					node = sess.getRootNode();
				else
					node = sess.getNode(nodeId);
				if (node == null)
					return null;
				if ((nodeType != null) && nodeType.isEmpty())
					nodeType = NodeType.NT_FILE;
	
				NodeIterator nodes = node.getNodes();
				ret = new ArrayList<RepositoryItem>();
				while (nodes.hasNext()) {
					Node nd = nodes.nextNode();
					if ((nodeType == null) || nd.isNodeType(nodeType))
						ret.add(CastUtil.node2model(new RepositoryItem(), nd));
				}
			} finally {
				sess.unlockRead();
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return ret;
	}

	/**
	 * Получить содержимое файла
	 * 
	 * @param nodeId
	 *            - ID элемента
	 * @return бинарный поток с содержимым файла
	 * @throws Exception
	 */
	public static InputStream getContent(String nodeId) throws Exception {
		if ((nodeId == null) || nodeId.isEmpty())
			return null;
		JCRSession sess = getSession();
		sess.lockRead();
		session.refresh();
		try {
			Node node = sess.getNode(nodeId);
			return (node != null) ? JcrUtils.readFile(node) : null;
		} catch (Exception e) {
			logger.error("Возникла ошибка при получении содержимого файла", e);
			throw new Exception("Возникла ошибка при получении содержимого файла");
		} finally {
			sess.unlockRead();
		}
	}

	/**
	 * Получить размер файла в байтах
	 * 
	 * @param file - файл
	 * @return размер файла в байтах
	 * @throws Exception
	 */
	private static long getContentSize(Node file) throws Exception {
        if (file.hasProperty(Property.JCR_DATA)) {
            Property data = file.getProperty(Property.JCR_DATA);
            Binary binary = data.getBinary();
            try {
            	return binary.getSize();
            } finally {
            	binary.dispose();
            }
        } else if (file.hasNode(Node.JCR_CONTENT)) {
            return getContentSize(file.getNode(Node.JCR_CONTENT));
        } else
        	return 0;
    }

	/**
	 * Выполняет масштабирование картинки до заданных размеров, сохраняя пропорции.
	 * Если указанные размеры больше размера картинки, никаких действий не выполняется.
	 * 
	 * @param inp - поток с исходным изображением
	 * @param toWidth - новая ширина
	 * @param toHeight - новая высота
	 * @param fileTitle - название изображения (для логов)
	 * @return поток с отмасштабированным изображением
	 */
	private static InputStream scaleImage(Node img, int toWidth, int toHeight) {
		// грузим картинку из потока
		BufferedImage inputImage = null;
		try (InputStream imgContent = JcrUtils.readFile(img)) {
			inputImage = ImageIO.read(imgContent);
		} catch (IOException | RepositoryException e) {
			logger.error("Image loading error", e);
		}
		if (inputImage == null)
			return null;

		int width = inputImage.getWidth(); 
		int height = inputImage.getHeight();

		if ((toWidth == 0) || (toHeight == 0)) {
			// отслеживаем нулевые размеры
			width = 0;
			height = 0;
		} else if ((width <= toWidth) && (height <= toHeight))
			// если картинка меньше требуемой, то выплёвываем её без изменений
			try {
				return JcrUtils.readFile(img);
			} catch (RepositoryException e) {
				logger.error("Image loading error", e);
			}
		else
			// если картинка больше требуемых размеров, то смотрим на пропорции
			if (toWidth * height > toHeight * width) {
				// приводим либо по высоте
				width = (width * toHeight) / height;
				height = toHeight;
			} else {
				// либо по ширине
				height = (height * toWidth) / width;
				width = toWidth;
			}

		// нулевые размеры ява не понимает :)
		if ((width <= 0) || (height <= 0)) {
			height = 1;
			width = 1;
		}

		BufferedImage outputImage = new BufferedImage(width, height, inputImage.getType());
		outputImage.getGraphics().drawImage(inputImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);

		try (ByteArrayOutputStream os = new ByteArrayOutputStream(1 << 16)) {
			ImageIO.write(outputImage, "png", os);
			return new ByteArrayInputStream(os.toByteArray());
		} catch (IOException e) {
			logger.error("Image writing error", e);
		}
		return null;
	}

	/**
	 * Получить изображение из репозитория (содержит встроенный механизм скейлинга и кеширования превьюшек)
	 * 
	 * @param nodeId - ID изображения
	 * @param width - желаемая ширина изображения
	 * @param height - желаемая высота изображения
	 * @return бинарный поток с изображением
	 * @throws Exception
	 */
	public static ImageFile getImage(String nodeId, Long width, Long height) throws Exception {
		JCRSession session = getSession();
		session.lockRead();
		session.refresh();
    	try {
			Node img = session.getNode(nodeId);
			if ((img != null) && img.hasProperty(ImageFile.WIDTH) && img.hasProperty(ImageFile.HEIGHT)) {
				ImageFile ret = new ImageFile();
				ret.setTitle(img.getProperty(ImageFile.TITLE).getString());
				long originW = img.getProperty(ImageFile.WIDTH).getLong();
				long originH = img.getProperty(ImageFile.HEIGHT).getLong();
				// нормализуем размеры, они не могут быть больше оригинала
				if (width == null)
					width = originW;
				width = Math.min(width, originW);
				if (height == null)
					height = originH;
				height = Math.min(height, originH);

				ret.setWidth(width);
				ret.setHeight(height);
				// если размеры совпадают с исходной картинкой, то сразу выплёвываем её
				if ((width.longValue() == originW) && (height.longValue() == originH))
					return CastUtil.node2model(ret, img);
				// иначе - ищем сохранённую уменьшенную версию (превью)
				String nodeName = width + "x" + height;
				if (img.hasNode(nodeName)) {
					Node content = img.getNode(nodeName);
					ret.setSize(getContentSize(content));
					return CastUtil.node2model(ret, content);
				}

			    // если превьюшка не найдена, то мы должны её создать
			    // переключаемся на запись
			    session.unlockRead();
			    session.lockWrite();
			    try {
			    	// повторяем процедуру поиска, т.к. пока мы получали разрешение на запись, ситуация могла кардинально измениться
			    	if (img.hasNode(nodeName)) {
						Node content = img.getNode(nodeName);
						ret.setSize(getContentSize(content));
						return CastUtil.node2model(ret, content);
					}
		    		try (InputStream scaled = scaleImage(img, width.intValue(), height.intValue())) {
				    	if (scaled != null) {
				    		Binary binary = session.getValueFactory().createBinary(scaled);
					        try {
					        	Node content = img.addNode(nodeName, NodeType.NT_RESOURCE);
					            content.setProperty(Property.JCR_LAST_MODIFIED, Calendar.getInstance());
					            content.setProperty(Property.JCR_DATA, binary);
					            session.save();

					            ret.setSize(getContentSize(content));
					    		return CastUtil.node2model(ret, content);
					        } finally {
					            binary.dispose();
					        }
				    	} else
					    	return null;
		    		}
				} finally {
					session.lockRead();
					session.unlockWrite();
				}
			}
		} catch(Exception e) {
			logger.error("Возникла ошибка при получении изображения", e);
			throw new Exception("Возникла ошибка при получении изображения");
		} finally {
			session.unlockRead();
		}
		return null;
	}

///////////// Методы предметной области //////////////////////////////////////////////////////////////////////////

	/**
	 * Создать новый файл
	 * 
	 * @param fid
	 * @param name
	 * @param descr
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public static RepositoryItem putFile(Long fid, String name, String descr, InputStream content) throws Exception {
		if (fid == null)
			throw new Exception("Не указан завод, для которого скачиваются заказы");
		JCRSession sess = getSession();
		Node file;
		sess.lockWrite();
		try {
			Node folder = getOrCreateFolderImpl(null, fid.toString(), null);
			if (folder == null)
				throw new Exception("Не удалось сохранить архив с заказами");

			file = JcrUtils.getOrCreateUniqueByPath(folder, name, NodeType.NT_FILE);
			file = addOrUpdateFileImpl(folder.getIdentifier(), file.getName(), name, descr, content);

			sess.save();
		} finally {
			sess.unlockWrite();
		}
		return CastUtil.node2model(new RepositoryItem(), file);
	}

	public static ImageFile putImage(String name, String descr, String url) throws IOException, Exception {
		try {
			BufferedImage img = ImageIO.read(new URL(url));
			if (img != null) {
				JCRSession sess = getSession();
				sess.lockWrite();
				sess.refresh();
				try (ByteArrayOutputStream os = new ByteArrayOutputStream(1 << 16)) {
					ImageIO.write(img, "jpg", os);
					try (InputStream is = new ByteArrayInputStream(os.toByteArray())) {
						Node file = addOrUpdateFileImpl(null, name, name, descr, is);
						file.addMixin(ImageFile.MIX_IMAGE_FILE);
			            file.setProperty(ImageFile.WIDTH, img.getWidth());
			            file.setProperty(ImageFile.HEIGHT, img.getHeight());
						sess.save();
						return CastUtil.node2model(new ImageFile(), file);
					}
				} catch (IOException e) {
					logger.error("Image writing error", e);
					throw new Exception("Image writing error");
				} finally {
					sess.unlockWrite();
				}
			} else
				throw new Exception("Файл повреждён или имеет неизвестный формат");
		} catch (IOException e) {
			logger.error("Файл повреждён или имеет неизвестный формат", e);
			throw new Exception("Файл повреждён или имеет неизвестный формат");
		} catch (Exception e) {
			logger.error("Возникла ошибка при загрузке изображения", e);
			throw new Exception("Возникла ошибка при загрузке изображения");
		}
	}

}