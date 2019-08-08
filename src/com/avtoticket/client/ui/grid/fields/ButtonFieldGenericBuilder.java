/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.client.ui.grid.fields;

import com.google.gwt.cell.client.Cell;
import com.avtoticket.client.ui.grid.cells.ButtonCell;
import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.cell.client.ActionCell.Delegate;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 16.09.2012 17:30:28
 */
public class ButtonFieldGenericBuilder<B extends ButtonFieldGenericBuilder<B, C>, C extends BaseModel> extends FieldGenericBuilder<B, C, C> {

	private String btnText = "";
	private Delegate<C> delegate = null;

	public String getBtnText() {
		return btnText;
	}
	@SuppressWarnings("unchecked")
	public B text(String btnText) {
		this.btnText = btnText;
		return (B) this;
	}

	public Delegate<C> getDelegate() {
		return delegate;
	}
	@SuppressWarnings("unchecked")
	public B delegate(Delegate<C> delegate) {
		this.delegate = delegate;
		return (B) this;
	}

	@Override
	protected Cell<C> createCell() {
		return new ButtonCell<C>(getBtnText(), getDelegate());
	}

}