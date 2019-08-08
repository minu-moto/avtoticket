/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Image;

/**
 * Кнопка на основе картинки из ресурсов
 * 
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 22.12.2012 20:17:32
 */
public class ImageBtn extends Image implements HasEnabled {

	@ImportedWithPrefix("atImageBtn")
	public static interface Style extends CssResource {
		String CSS_PATH = "imageButton.css";

		String atImgBtn();

		String atImgBtnDisabled();
	}

	public static interface Resources extends ClientBundle {
		@Source(Style.CSS_PATH)
	    Style imageBtnStyle();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	public static final Style CSS = RESOURCES.imageBtnStyle();

	private boolean isEnabled = true;

	public static ImageBtn wrap(Element element, ImageResource icon, String text, ClickHandler handler) {
		if (element == null)
			return null;
		assert Document.get().getBody().isOrHasChild(element);

		ImageBtn image = new ImageBtn(element, icon, text, handler);

		image.onAttach();
//		RootPanel.detachOnWindowClose(image);

		return image;
	}

	/**
	 * Новый экземпляр графической кнопки
	 * 
	 * @param icon
	 *            - ресурс с изображением кнопки
	 * @param text
	 *            - альтернативный текст кнопки, он же всплывающая подсказка
	 * @param handler
	 *            - обработчик нажатия
	 */
	public ImageBtn(ImageResource icon, String text, ClickHandler handler) {
		CSS.ensureInjected();
		setResource(icon);
		addStyleName(CSS.atImgBtn());
		if (text != null) {
			setAltText(text);
			setTitle(text);
		}
		if (handler != null)
			addClickHandler(handler);
	}

	protected ImageBtn(Element element, ImageResource icon, String text, ClickHandler handler) {
		super(element);
		CSS.ensureInjected();
		setResource(icon);
		addStyleName(CSS.atImgBtn());
		if (text != null) {
			setAltText(text);
			setTitle(text);
		}
		if (handler != null)
			addClickHandler(handler);
	}

	/**
	 * Новый экземпляр графической кнопки
	 * 
	 * @param icon
	 *            - ресурс с изображением кнопки
	 * @param text
	 *            - альтернативный текст кнопки, он же всплывающая подсказка
	 */
	public ImageBtn(ImageResource icon, String text) {
		this(icon, text, null);
	}

	/**
	 * Новый экземпляр графической кнопки
	 * 
	 * @param icon
	 *            - ресурс с изображением кнопки
	 * @param handler
	 *            - обработчик нажатия
	 */
	public ImageBtn(ImageResource icon, ClickHandler handler) {
		this(icon, null, handler);
	}

	/**
	 * Новый экземпляр графической кнопки
	 * 
	 * @param icon
	 *            - ресурс с изображением кнопки
	 */
	public ImageBtn(ImageResource icon) {
		this(icon, (String) null);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
		setStyleName(CSS.atImgBtnDisabled(), !isEnabled);
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public HandlerRegistration addClickHandler(final ClickHandler handler) {
		if (handler == null)
			throw new NullPointerException("Cannot add a null handler");
		return super.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isEnabled)
					handler.onClick(event);
			}
		});
	}

	public final void click() {
	    fireEvent(new ClickEvent() { });
	}

}