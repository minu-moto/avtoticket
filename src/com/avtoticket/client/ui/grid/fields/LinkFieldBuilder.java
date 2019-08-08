/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid.fields;

import com.avtoticket.client.ui.grid.cells.LinkCell;
import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 08.03.2013 11:56:19
 */
public class LinkFieldBuilder<C extends BaseModel> extends ButtonFieldGenericBuilder<LinkFieldBuilder<C>, C> {

	private SafeHtmlRenderer<C> renderer;

	public SafeHtmlRenderer<C> getHtmlRenderer() {
		return renderer;
	}
	public LinkFieldBuilder<C> htmlRenderer(SafeHtmlRenderer<C> renderer) {
		this.renderer = renderer;
		return this;
	}

	@Override
	protected Cell<C> createCell() {
		if (getHtmlRenderer() != null)
			return new LinkCell<C>(getHtmlRenderer(), getDelegate());
		else if ((getBtnText() != null) && !getBtnText().isEmpty())
			return new LinkCell<C>(getBtnText(), getDelegate());
		else
			return new LinkCell<C>(new SafeHtmlRenderer<C>() {
				@Override
				public SafeHtml render(C context) {
					return LinkCell.template.link(context.getStringProp(getModelKey()));
				}

				@Override
				public void render(C object, SafeHtmlBuilder builder) {
					builder.append(render(object));
				}
			}, getDelegate());
	}

}