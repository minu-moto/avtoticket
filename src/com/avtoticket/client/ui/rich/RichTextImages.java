/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui.rich;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 21.04.2014 23:05:06
 */
public interface RichTextImages extends ClientBundle {

	ImageResource backColors();

	ImageResource fonts();

	ImageResource fontSizes();

	ImageResource foreColors();

	ImageResource bold();

	ImageResource createLink();

	ImageResource hr();

	ImageResource indent();

	ImageResource insertImage();

	ImageResource italic();

	ImageResource justifyCenter();

	ImageResource justifyLeft();

	ImageResource justifyRight();

	ImageResource ol();

	ImageResource outdent();

	ImageResource removeFormat();

	ImageResource removeLink();

	ImageResource strikeThrough();

	ImageResource subscript();

	ImageResource superscript();

	ImageResource ul();

	ImageResource underline();

}