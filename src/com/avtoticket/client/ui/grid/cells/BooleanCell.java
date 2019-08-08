/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui.grid.cells;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 09.02.2014 22:24:59
 */
public class BooleanCell extends CheckboxCell {

	private static final SafeHtml INPUT_CHECKED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked/>");
	private static final SafeHtml INPUT_UNCHECKED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\"/>");
	private static final SafeHtml INPUT_CHECKED_DISABLED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked disabled/>");
	private static final SafeHtml INPUT_UNCHECKED_DISABLED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled/>");

	private boolean readonly;

	public BooleanCell(boolean readonly) {
		super(true, false);
		this.readonly = readonly;
	}

	@Override
	public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
	    Object key = context.getKey();
	    Boolean viewData = getViewData(key);
	    if (viewData != null && viewData.equals(value)) {
	    	clearViewData(key);
	    	viewData = null;
	    }

	    if (value != null && ((viewData != null) ? viewData : value))
	    	sb.append(readonly ? INPUT_CHECKED_DISABLED : INPUT_CHECKED);
	    else
	    	sb.append(readonly ? INPUT_UNCHECKED_DISABLED : INPUT_UNCHECKED);
	}

}