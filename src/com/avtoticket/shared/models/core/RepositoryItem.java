/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.shared.models.core;

import java.util.Date;

import com.avtoticket.shared.models.BaseModel;

/**
 * Элемент репозитория (файл/каталог)
 * 
 * @author Minu <<a href='minu-moto@mail.ru'>minu-moto@mail.ru</a>>
 * @since 21.03.2012 17:37:18
 */
public class RepositoryItem extends BaseModel {

	private static final long serialVersionUID = -982694282998065497L;

	public static final transient String MIX_CONTENT_SIZE = "avt:filesize";

	public static final transient String IDENT = "jcr:id";					//NameConstants.JCR_ID
	public static final transient String DATE_CREATE = "jcr:created";		//NameConstants.JCR_CREATED
	public static final transient String NAME = "jcr:name";					//NameConstants.JCR_NAME
	public static final transient String PATH = "jcr:path";					//NameConstants.JCR_PATH
	public static final transient String TYPE = "jcr:primaryType";			//NameConstants.JCR_PRIMARYTYPE
	public static final transient String FOLDER = "nt:folder";				//NameConstants.NT_FOLDER;
	public static final transient String FILE = "nt:file";					//NameConstants.NT_FILE;
	public static final transient String SIZE = "avt:size";
	public static final transient String TITLE = "jcr:title";				//NameConstants.JCR_TITLE;
	public static final transient String DESCRIPTION = "jcr:description";	//NameConstants.JCR_DESCRIPTION

	/**
	 * Новый экземпляр модели
	 */
	public RepositoryItem() {
		super(RepositoryItem.class.getName());
	}

	/**
	 * Новый экземпляр модели
	 * 
	 * @param className - имя класса фактической модели
	 */
	public RepositoryItem(String className) {
		super(className);
	}

	/**
	 * @return идентификатор элемента в репозитории
	 */
	public String getIdent() {
		return getStringProp(IDENT);
	}
	public void setIdent(String val) {
		set(IDENT, val);
	}

	public Date getDateCreate() {
		return getDateProp(DATE_CREATE);
	}
	public void setDateCreate(Date val) {
		set(DATE_CREATE, val);
	}

	/**
	 * @return имя элемента в репозитории
	 */
	public String getName() {
		return getStringProp(NAME);
	}
	public void setName(String val) {
		set(NAME, val);
	}

	/**
	 * @return путь элемента в репозитории
	 */
	public String getPath() {
		return getStringProp(PATH);
	}
	public void setPath(String val) {
		set(PATH, val);
	}

	/**
	 * @return тип элемента
	 */
	public String getType() {
		return getStringProp(TYPE);
	}
	public void setType(String val) {
		set(TYPE, val);
	}
	public Boolean isFolder() {
		return FOLDER.equalsIgnoreCase(getType());
	}
	public Boolean isFile() {
		return FILE.equalsIgnoreCase(getType());
	}

	/**
	 * @return размер содержимого элемента в байтах
	 */
	public Long getSize() {
		return getLongProp(SIZE);
	}
	public void setSize(Long val) {
		set(SIZE, val);
	}

	/**
	 * @return заголовок элемента
	 */
	public String getTitle() {
		return getStringProp(TITLE);
	}
	public void setTitle(String title) {
		set(TITLE, title);
	}

	/**
	 * @return описание элемента
	 */
	public String getDescription() {
		return getStringProp(DESCRIPTION);
	}
	public void setDescription(String descr) {
		set(DESCRIPTION, descr);
	}

}