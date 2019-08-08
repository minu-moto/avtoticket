/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.menu;

import java.util.Date;

import com.avtoticket.client.about.AboutPlace;
import com.avtoticket.client.admin.AdminPlace;
import com.avtoticket.client.map.MapPlace;
import com.avtoticket.client.mobile.MobilePlace;
import com.avtoticket.client.partners.PartnersPlace;
import com.avtoticket.client.project.ProjectPlace;
import com.avtoticket.client.reference.ReferencePlace;
import com.avtoticket.client.service.ServicePlace;
import com.avtoticket.client.utils.SessionUtil;
import com.avtoticket.shared.models.core.User;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 16 дек. 2015 г. 23:27:10
 */
public class MainMenu extends FlowPanel {

	public interface Style extends CssResource {
		String CSS_PATH = "menu.css";

		String atMenu();

		String atMenuLogo();

		String atMenuItems();

		String atRouteSelector();

		String atRouteSelectorLabel();

		String atRouteSelectorFrom();

		String atRouteSelectorTo();

		String atRouteSelectorToSuggest();

		String atRouteSelectorCurve();

		String atRouteSelectorSearchBtn();

		String atRouteSelectorChangeBtn();

		String atDateSelectorDateLbl();

		String atDateSelectorLine();

		String atRouteSelectorDateSelector();

		String atDateSelector();
	}

	public interface Resources extends ClientBundle {

		ImageResource logo();

		ImageResource curve();

		ImageResource clock();

		ImageResource reverse();

		ImageResource reverse_hover();

		@Source(Style.CSS_PATH)
	    Style menuStyle();
	}

	@DefaultLocale("ru")
	public interface Strings extends Messages {
		@DefaultMessage("О компании")
		String about();

		@DefaultMessage("На карте")
		String map();

		@DefaultMessage("О проекте")
		String project();

		@DefaultMessage("Мобильные приложения")
		String mobile();

		@DefaultMessage("Партнёры")
		String partners();

		@DefaultMessage("Сервис")
		String service();

		@DefaultMessage("Справочные")
		String reference();

		@DefaultMessage("Найти")
		String find();

		@DefaultMessage("Выберите маршрут и дату отправления")
		String choose();

		@DefaultMessage("Выберите пункт отправления")
		String chooseDep();

		@DefaultMessage("Выберите пункт назначения")
		String chooseDest();

		@DefaultMessage("Не найден пункт отправления ''{0}''")
		String depNotFound(String dep);

		@DefaultMessage("Не найден пункт назначения ''{0}''")
		String destNotFound(String dest);

		@DefaultMessage("Откуда")
		String from();

		@DefaultMessage("Куда")
		String to();

		@DefaultMessage("Когда")
		String when();

		@DefaultMessage("Загрузка...")
		String loading();

		@DefaultMessage("Список пуст")
		String empty();

		@DefaultMessage("{0,date:tz=$tz,ccc, dd.MM.yy}")
		String date(Date date, TimeZone tz);
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	private static final Style CSS = RESOURCES.menuStyle();
	private static final Strings STRINGS = GWT.create(Strings.class);

	private FlexTable menu;

	private Anchor getMenuItem(String text, String href) {
		return new Anchor(text, "#" + href);
	}

	public MainMenu() {
		CSS.ensureInjected();

		addStyleName(CSS.atMenu());

		User user = SessionUtil.getUser();
		Anchor logo = new Anchor("", ((user != null) && (user.isAdmin() == Boolean.TRUE)) ? "#" + AdminPlace.NAME : "#");
		logo.addStyleName(CSS.atMenuLogo());
		add(logo);

		menu = new FlexTable();
		menu.setText(0, 0, "");
		menu.setWidget(0, 1, getMenuItem(STRINGS.about(), AboutPlace.NAME));
		menu.setWidget(0, 2, getMenuItem(STRINGS.map(), MapPlace.NAME));
		menu.setWidget(0, 3, getMenuItem(STRINGS.project(), ProjectPlace.NAME));
		menu.setWidget(0, 4, getMenuItem(STRINGS.mobile(), MobilePlace.NAME));
		menu.setWidget(0, 5, getMenuItem(STRINGS.partners(), PartnersPlace.NAME));
		menu.setWidget(0, 6, getMenuItem(STRINGS.service(), ServicePlace.NAME));
		menu.setWidget(0, 7, getMenuItem(STRINGS.reference(), ReferencePlace.NAME));
		menu.setText(0, 8, "");
		menu.getColumnFormatter().setWidth(0, "50%");
		menu.getColumnFormatter().setWidth(8, "50%");
		menu.addStyleName(CSS.atMenuItems());
		add(menu);

		add(RouteSelector.get(RESOURCES, STRINGS));
	}

}