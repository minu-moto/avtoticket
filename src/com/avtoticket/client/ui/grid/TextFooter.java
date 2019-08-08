/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui.grid;

import com.google.gwt.cell.client.TextCell;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 26.03.2014 23:28:41
 */
public class TextFooter extends Footer<String> {

	private String text;

	public TextFooter() {
		super(new TextCell());
	}

	public TextFooter(String text) {
		this();
	    this.text = text;
	}

	@Override
	public String getValue() {
		return text;
	}

}