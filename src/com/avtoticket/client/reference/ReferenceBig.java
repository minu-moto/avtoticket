/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.reference;

import com.avtoticket.client.reference.ReferencePlace.Strings;
import com.avtoticket.client.reference.ReferencePlace.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 дек. 2015 г. 21:37:27
 */
public class ReferenceBig extends FlowPanel {

	private Style CSS;
	private FlowPanel content = new FlowPanel();
	private FlowPanel cells = new FlowPanel();
	private Label caption = new Label("СПРАВОЧНЫЕ СЛУЖБЫ");

	private String filterEmpty(String s) {
		return ((s != null) && !s.isEmpty()) ? s : "\u00a0";
	}

	private HTML getCell(String header, String c11, String c12, String c21, String c22) {
		HTML ret = new HTML(new SafeHtmlBuilder()
				.appendHtmlConstant("<div class='" + CSS.atReferenceCellHeader() + "'>").appendEscaped(header).appendHtmlConstant("</div>")
				.appendHtmlConstant("<div class='" + CSS.atReferenceFieldLeft() + "'>").appendEscaped(filterEmpty(c11)).appendHtmlConstant("</div>")
				.appendHtmlConstant("<div class='" + CSS.atReferenceFieldRight() + "'>").appendEscaped(filterEmpty(c12)).appendHtmlConstant("</div>")
				.appendHtmlConstant("<div class='" + CSS.atReferenceFieldLeft() + "'>").appendEscaped(filterEmpty(c21)).appendHtmlConstant("</div>")
				.appendHtmlConstant("<div class='" + CSS.atReferenceFieldRight() + "'>").appendEscaped(filterEmpty(c22)).appendHtmlConstant("</div>")
				.toSafeHtml());
		ret.addStyleName(CSS.atReferenceCell());
		return ret;
	}

	public ReferenceBig(Style CSS, Strings STRINGS) {
		this.CSS = CSS;

		caption.addStyleName(CSS.atReferenceCaption());
		content.add(caption);

		cells.addStyleName(CSS.atReferenceCells());
		content.add(cells);

		cells.add(getCell("КРАСНОДАР АВ-1",
				"справочная", "(861) 262-51-44",
				"оператор заказов билетов", "(861) 262-42-71"));

		cells.add(getCell("КРАСНОДАР АС-1",
				"диспетчерская", "(861) 262-04-18",
				"касса-справочная", "(861) 262-04-98"));

		cells.add(getCell("КРАСНОДАР АС-2",
				"диспетчерская", "(861) 259-14-62",
				"справочная", "(861) 255-06-18"));

		cells.add(getCell("АБИНСКИЙ АВ",
				"353320, г. Абинск,", "",
				"ул. Заводская, 4", "(86150) 5-19-02"));

		cells.add(getCell("АНАПСКИЙ АВ",
				"353440, г. Анапа,", "",
				"ул. Красноармейская, 11", "(86133) 5-68-97"));

		cells.add(getCell("АПШЕРОНСКИЙ АВ",
				"352690, г. Апшеронск,", "",
				"ул. Ворошилова, 2", "(86152) 2-19-30"));

		cells.add(getCell("АРМАВИРСКИЙ АВ",
				"352900, г. Армавир,", "",
				"ул. Ефремова, 145", "(86137) 5-70-25"));

		cells.add(getCell("БЕЛОРЕЧЕНСКАЯ АС",
				"352635, г. Белореченск,", "",
				"ул. Ленина, 84", "(86155) 2-28-52"));

		cells.add(getCell("БРЮХОВЕЦКАЯ АС",
				"353730, ст. Брюховецкая,", "",
				"Привокзальная площадь, 8", "(86156) 3-11-44"));

		cells.add(getCell("ВЫСЕЛКОВСКАЯ АС",
				"353100, ст. Выселки,", "",
				"ул. Лунева, 1", "(86157) 7-34-73"));

		cells.add(getCell("ГЕЛЕНДЖИКСКИЙ АВ",
				"353460, г. Геленджик,", "",
				"ул. Объездная, 3", "(86141) 3-27-93"));

		cells.add(getCell("ГОРЯЧИЙ КЛЮЧ АС",
				"353290, г. Горячий Ключ,", "",
				"ул. Ярославского, 138", "(86159) 4-64-01"));

		cells.add(getCell("ДЖУБСКАЯ АС",
				"352844, п. Джубга,", "",
				"Новороссийское шоссе, 1 в", "(86167) 9-49-37"));

		cells.add(getCell("ЕЙСКИЙ АВ",
				"353691, г. Ейск,", "",
				"ул. Коммунистическая, 18", "(86132) 4-35-00"));

		cells.add(getCell("КАНЕВСКАЯ АС",
				"353710, ст. Каневская,", "",
				"ул. Привокзальная площадь, 1", "(86164) 7-07-51"));

		cells.add(getCell("КОРЕНОВСКАЯ АС",
				"353150, г. Кореновск,", "",
				"ул. Циолковского, 1", "(86142) 4-14-01"));

		cells.add(getCell("КРОПОТКИНСКИЙ АВ",
				"352380, г. Кропоткин,", "",
				"ул. Базарная, 25А", "(86138) 6-15-50"));

		cells.add(getCell("КРЫМСКАЯ АС",
				"353380, г. Крымск,", "",
				"ул. Маршала Гречко, 130", "(86131) 4-23-23"));

		cells.add(getCell("КУРГАНИНСКАЯ АС",
				"352431, г. Курганинск,", "",
				"ул. Привокзальная, 21", "(86147) 2-13-32"));

		cells.add(getCell("КУЩЕВСКАЯ АС",
				"352030, ст. Кущевская,", "",
				"ул. Транспортная, 8", "(86168) 5-52-56"));

		cells.add(getCell("ЛАБИНСКИЙ АВ",
				"352500, г. Лабинск,", "",
				"ул. Халтурина, 18\1", "(86169) 3-36-93"));

		cells.add(getCell("ЛЕНИНГРАДСКАЯ АС",
				"353740, ст. Ленинградская,", "",
				"ул. Кооперации, 88", "(86145) 3-91-09"));

		cells.add(getCell("НОВОПОКРОВСКАЯ АС",
				"353020, ст. Новопокровская,", "",
				"ул. Советская, 12", "(86149) 7-13-04"));

		cells.add(getCell("НОВОРОССИЙСКИЙ АВ",
				"352900, г. Новороссийск,", "",
				"ул. Чайковского, 15", "(8617) 64-42-79"));

		cells.add(getCell("ОТРАДНАЯ АС",
				"352290, ст. Отрадная,", "",
				"ул. Широкая, 6", "(86144) 3-30-35"));

		cells.add(getCell("ПАВЛОВСКАЯ АС",
				"352014, ст. Павловская,", "",
				"ул. Чкалова, 3", "(86191) 5-19-57"));

		cells.add(getCell("ПРИМОРСКО-АХТАРСКАЯ АС",
				"353680, г. Приморско-Ахтарск,", "",
				"ул. Тамаровского, 2", "(86143) 3-11-48"));

		cells.add(getCell("СЕВЕРСКАЯ АС",
				"353240, ст. Северская,", "",
				"ул. Базарная, 8", "(86166) 2-14-34"));

		cells.add(getCell("СЛАВЯНСКИЙ АВ",
				"353840, г. Славянск-на-Кубани,", "",
				"ул. Ковтюха, 120", "(86146) 2-13-52"));

		cells.add(getCell("СТАРОМИНСКАЯ АС",
				"353600, ст. Староминская,", "",
				"ул. Толстого, 1", "(86153) 5-88-31"));

		cells.add(getCell("ТБИЛИССКАЯ АС",
				"352360, ст. Тбилисская,", "",
				"ул. Октябрьская, 179", "(86158) 3-22-31"));

		cells.add(getCell("ТЕМРЮКСКАЯ АС",
				"353500, г. Темрюк,", "",
				"ул. Урицкого, 52", "(86148) 5-24-84"));

		cells.add(getCell("ТИМАШЕВСКИЙ АВ",
				"353700, г. Тимашевск,", "",
				"ул. Братьев Степановых, 22", "(86130) 4-14-81"));

		cells.add(getCell("ТИХОРЕЦКИЙ АВ",
				"352120, г. Тихорецк,", "",
				"ул. Подвойского, 42 А", "(86196) 7-40-01"));

		cells.add(getCell("УСТЬ-ЛАБИНСКИЙ АВ",
				"352330, г. Усть-Лабинск,", "",
				"ул. Октябрьская, 118", "(86135) 2-15-85"));

		content.add(new FlowPanel());
		content.addStyleName(CSS.atReferenceBig());
		add(content);

		Label label = new Label(STRINGS.caption());
		label.addStyleName(CSS.atReferenceLabel());
		add(label);
	}

}