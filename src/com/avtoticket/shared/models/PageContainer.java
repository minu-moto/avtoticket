/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.shared.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Minu <<a href='minu-moto@mail.ru'>minu-moto@mail.ru</a>>
 * @since 04.04.2012 16:27:54
 */
public class PageContainer<T extends BaseModel> implements Serializable {

	private static final long serialVersionUID = 5116004492151436591L;

	/**
	 * Общее количество элементов
	 */
	private int itemsCount;
	/**
	 * Список элементов на странице
	 */
	private List<T> page;

	public PageContainer() {
		this(null);
	}

	public PageContainer(Collection<T> c) {
		page = (c != null) ? new ArrayList<T>(c) : null;
		itemsCount = (page != null) ? page.size() : 0;
	}

	public int getItemsCount() {
		return itemsCount;
	}
	public void setItemsCount(int itemsCount) {
		this.itemsCount = itemsCount;
	}

	public List<T> getPage() {
		return page;
	}
	public void setPage(List<T> page) {
		this.page = page;
	}

}