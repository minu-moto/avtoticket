/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.shared.models.core;

/**
 * Модель файла с изображением
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 22.04.2014 18:26:11
 */
public class ImageFile extends RepositoryItem {

	private static final long serialVersionUID = -6008000056998896129L;

	public static final transient String MIX_IMAGE_FILE = "avt:imagefile";

	/** Ширина изображения */
	public static final String WIDTH = "avt:width";
	/** Высота изображения */
	public static final String HEIGHT = "avt:height";

	/**
	 * Новый экземпляр модели
	 */
	public ImageFile() {
		super(ImageFile.class.getName());
	}

	/**
	 * @return ширина изображения
	 */
	public Long getWidth() {
		return getLongProp(WIDTH);
	}
	public void setWidth(Long width) {
		set(WIDTH, width);
	}

	/**
	 * @return высота изображения
	 */
	public Long getHeight() {
		return getLongProp(HEIGHT);
	}
	public void setHeight(Long height) {
		set(HEIGHT, height);
	}

}