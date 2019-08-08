/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client;

import com.avtoticket.client.Avtoticket.Strings;
import com.avtoticket.client.Avtoticket.Style;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 19 дек. 2015 г. 23:51:08
 */
public class FooterPanel extends HTML {

	public FooterPanel(Style CSS, Strings STRINGS) {
		super("<!-- Yandex.Metrika informer -->"
				+ "<a href=\"https://metrika.yandex.ru/stat/?id=44394868&amp;from=informer\" style=\"bottom: -12px; position: absolute; right: 348px;\" "
				+ "target=\"_blank\" rel=\"nofollow\"><img src=\"https://informer.yandex.ru/informer/44394868/3_0_FFFFFFFF_FFFFFFFF_0_pageviews\" "
				+ "style=\"width:88px; height:31px; border:0;\" alt=\"Яндекс.Метрика\" title=\"Яндекс.Метрика: данные за сегодня (просмотры, визиты и уникальные посетители)\" class=\"ym-advanced-informer\" data-cid=\"44394868\" data-lang=\"ru\" /></a>"
				+ "<!-- /Yandex.Metrika informer -->"
				+ "<div style=\"float: right; font-size: 14px;\">" + STRINGS.footer() + "</div>");
		addStyleName(CSS.atFooter());
	}

}