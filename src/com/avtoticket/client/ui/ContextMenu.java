/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui;

import java.util.Iterator;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 * Блять!!! Эта хуйня родилась только из-за того, что гугл зажопил метод
 * {@link MenuBar#getPopup}. Пришлось писать своё всплывающее меню, с блекджеком и
 * шлюхами... Если гугл откроет доступ к всплывающим менюшкам, то это можно
 * будет спокойно удалить.
 * 
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 09.02.2013 21:10:34
 */
public class ContextMenu extends MenuBar {

	/**
	 * Копия {@link DecoratorPanel} с переопределёнными стилями DEFAULT_ROW_STYLENAMES
	 */
	private static class PopupDecoratorPanel extends SimplePanel {
		/**
		 * Create a new row with a specific style name. The row will contain
		 * three cells (Left, Center, and Right), each prefixed with the
		 * specified style name.
		 * 
		 * This method allows Widgets to reuse the code on a DOM level, without
		 * creating a DecoratorPanel Widget.
		 * 
		 * @param styleName
		 *            the style name
		 * @return the new row {@link Element}
		 */
		static Element createTR(String styleName) {
			Element trElem = DOM.createTR();
			setStyleName(trElem, styleName);
			if (LocaleInfo.getCurrentLocale().isRTL()) {
				DOM.appendChild(trElem, createTD(styleName + "Right"));
				DOM.appendChild(trElem, createTD(styleName + "Center"));
				DOM.appendChild(trElem, createTD(styleName + "Left"));
			} else {
				DOM.appendChild(trElem, createTD(styleName + "Left"));
				DOM.appendChild(trElem, createTD(styleName + "Center"));
				DOM.appendChild(trElem, createTD(styleName + "Right"));
			}
			return trElem;
		}

		/**
		 * Create a new table cell with a specific style name.
		 * 
		 * @param styleName
		 *            the style name
		 * @return the new cell {@link Element}
		 */
		private static Element createTD(String styleName) {
			Element tdElem = DOM.createTD();
			Element inner = DOM.createDiv();
			DOM.appendChild(tdElem, inner);
			setStyleName(tdElem, styleName);
			setStyleName(inner, styleName + "Inner");
			return tdElem;
		}

		/**
		 * The container element at the center of the panel.
		 */
		private Element containerElem;

		/**
		 * The table body element.
		 */
		private Element tbody;

		/**
		 * Creates a new panel using the specified style names to apply to each
		 * row. Each row will contain three cells (Left, Center, and Right). The
		 * Center cell in the containerIndex row will contain the {@link Widget}
		 * .
		 * 
		 * @param rowStyles
		 *            an array of style names to apply to each row
		 * @param containerIndex
		 *            the index of the container row
		 */
		public PopupDecoratorPanel() {
			super(DOM.createTable());

			// Add a tbody
			Element table = getElement();
			tbody = DOM.createTBody();
			DOM.appendChild(table, tbody);
			table.setPropertyInt("cellSpacing", 0);
			table.setPropertyInt("cellPadding", 0);

			// Add each row
			String[] rowStyles = new String[] { "menuPopupTop",
					"menuPopupMiddle", "menuPopupBottom" };
			for (int i = 0; i < rowStyles.length; i++) {
				Element row = createTR(rowStyles[i]);
				DOM.appendChild(tbody, row);
				if (i == 1)
					containerElem = DOM.getFirstChild(DOM.getChild(row, 1));
			}
		}

		@SuppressWarnings("deprecation")
		@Override
		protected com.google.gwt.user.client.Element getContainerElement() {
			return DOM.asOld(containerElem);
		}

		@Override
		protected void onAttach() {
			super.onAttach();
		}

		@Override
		protected void onDetach() {
			super.onDetach();
		}

	}

	/**
	 * Копия {@link DecoratedPopupPanel} с переопределённым decPanel
	 */
	public static class PopupMenuPanel extends PopupPanel {

		public static interface Style extends CssResource {
			String CSS_PATH = "context.css";

			String atContextContent();
		}

		public static interface Resources extends ClientBundle {
			@Source(Style.CSS_PATH)
		    Style contextStyle();
		}

		private static final Resources RESOURCES = GWT.create(Resources.class);
		private static final Style CSS = RESOURCES.contextStyle();

		/**
		 * The panel used to nine box the contents.
		 */
		private PopupDecoratorPanel decPanel;

		/**
		 * Creates an empty decorated popup panel using the specified style
		 * names.
		 * 
		 * @param autoHide
		 *            <code>true</code> if the popup should be automatically
		 *            hidden when the user clicks outside of it
		 * @param modal
		 *            <code>true</code> if keyboard or mouse events that do not
		 *            target the PopupPanel or its children should be ignored
		 * @param prefix
		 *            the prefix applied to child style names
		 */
		PopupMenuPanel() {
			super(true, false);
			CSS.ensureInjected();
			decPanel = new PopupDecoratorPanel();
			setStylePrimaryName("gwt-DecoratedPopupPanel");
			super.setWidget(decPanel);
			setStyleName(getContainerElement(), "popupContent", false);
			//setScroll(true);
		}

		@Override
		public void clear() {
			decPanel.clear();
		}

		@Override
		public Widget getWidget() {
			return decPanel.getWidget();
		}

		@Override
		public Iterator<Widget> iterator() {
			return decPanel.iterator();
		}

		@Override
		public boolean remove(Widget w) {
			return decPanel.remove(w);
		}

		@Override
		public void setWidget(Widget w) {
			decPanel.setWidget(w);
//			maybeUpdateSize();
		}

		public void setScroll(boolean show) {
			setStyleName(decPanel.getContainerElement(), CSS.atContextContent(), show);
		}

		@Override
		protected void doAttachChildren() {
			// See comment in doDetachChildren for an explanation of this call
			decPanel.onAttach();
		}

		@Override
		protected void doDetachChildren() {
			decPanel.onDetach();
		}
	}

	private PopupMenuPanel popup;
	private UIObject parentEl;

	@SuppressWarnings("deprecation")
	public ContextMenu() {
		super(true);
		// Create a new popup for this item, and position it next to
		// the item (below if this is a horizontal menu bar, to the
		// right if it's a vertical bar).
		popup = new PopupMenuPanel() {
			{
				setWidget(getPopupContent());
				setPreviewingAllNativeEvents(true);
				// clear the selection; a keyboard user can cursor down to the
				// first item
				ContextMenu.this.selectItem(null);
			}

			@Override
			protected void onPreviewNativeEvent(NativePreviewEvent event) {
				// Hook the popup panel's event preview. We use this to keep it
				// from
				// auto-hiding when the parent menu is clicked.
				if (!event.isCanceled()) {

					switch (event.getTypeInt()) {
					case Event.ONMOUSEDOWN:
						// If the event target is part of the parent menu,
						// suppress the
						// event altogether.
						EventTarget target = event.getNativeEvent().getEventTarget();
						UIObject parent = getParentEl();
						Element parentMenuElement = parent.getElement();
						if (parentMenuElement.isOrHasChild(Element.as(target))) {
							event.cancel();
							return;
						}
						super.onPreviewNativeEvent(event);
						if (event.isCanceled()) {
							selectItem(null);
						}
						return;
					}
				}
				super.onPreviewNativeEvent(event);
			}
		};
		popup.setAnimationType(PopupMenuPanel.AnimationType.ONE_WAY_CORNER);
		popup.setStyleName("gwt-MenuBarPopup");
		popup.addPopupListener(this);
	}

	protected Widget getPopupContent() {
		return this;
	}

	public void popup(final UIObject parent) {
		parentEl = parent;
		popup.setAnimationEnabled(isAnimationEnabled());
		popup.setScroll(getItems().size() > 14);

		popup.showRelativeTo(parent);
	}

	public UIObject getParentEl() {
		return parentEl;
	}

	public void hide() {
		popup.hide();
	}

	public void setModal(boolean modal) {
		popup.setModal(modal);
	}

	public void addAutoHidePartner(Element partner) {
		popup.addAutoHidePartner(partner);
	}

}