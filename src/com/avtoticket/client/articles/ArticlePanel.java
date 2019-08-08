/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.articles;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.avtoticket.client.articles.ArticlePlace.Style;
import com.avtoticket.client.utils.DefaultCallback;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.shared.models.core.Help;
import com.avtoticket.shared.models.core.HelpType;

import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 28 дек. 2015 г. 22:09:18
 */
public class ArticlePanel extends FlexTable {

	private void addHeadCell(Element tr, HelpType text) {
		Element th = DOM.createTH();
		tr.appendChild(th);
		th.setInnerText(text.toString());
	}

	private void addCell(List<Help> help, int col) {
		if (help == null)
			return;
		Collections.sort(help, (o1, o2) -> o1.getId().compareTo(o2.getId()));
		int i = 0;
		for (Help h : help)
			setWidget(i++, col, new Anchor(h.getName(), "#" + ArticlePlace.NAME + ArticlePlace.PARAM_SEPARATOR + h.getId()));
	}

	public ArticlePanel(Style CSS) {
		CSS.ensureInjected();
		addStyleName(CSS.atArticlesPanel());

		Element head = DOM.createTHead();
		Element tr = DOM.createTR();
		head.appendChild(tr);
		getElement().insertFirst(head);

		addHeadCell(tr, HelpType.QUESTIONS);
		addHeadCell(tr, HelpType.INFO);
		addHeadCell(tr, HelpType.DOCS);
		addHeadCell(tr, HelpType.COMPANY);
		addHeadCell(tr, HelpType.ABOUT);
		addHeadCell(tr, HelpType.CONFIDENTIALITY);

		ColumnFormatter formatter = getColumnFormatter();
		formatter.setWidth(0, "17%");
		formatter.setWidth(1, "17%");
		formatter.setWidth(2, "17%");
		formatter.setWidth(3, "17%");
		formatter.setWidth(4, "17%");
		formatter.setWidth(5, "17%");

		RPC.getTS().getHelp(LocaleInfo.getCurrentLocale().getLocaleName(), (DefaultCallback<Map<HelpType, List<Help>>>) map -> {
			addCell(map.get(HelpType.QUESTIONS), 0);
			addCell(map.get(HelpType.INFO), 1);
			addCell(map.get(HelpType.DOCS), 2);
			addCell(map.get(HelpType.COMPANY), 3);
			addCell(map.get(HelpType.ABOUT), 4);
			addCell(map.get(HelpType.CONFIDENTIALITY), 5);
		});
	}

}