/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.admin;

import com.avtoticket.client.ui.DBTable;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.shared.models.core.NasPunkt;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 11 февр. 2016 г. 23:05:08
 */
public class NasPunktsTable extends DBTable<NasPunkt, Long> {

	public NasPunktsTable() {
		super("Города", new NasPunkt());
	}

	@Override
	protected void buildTableColumns() {
		addColumn(Field.asTextTo(grid).modelKey(NasPunkt.NAME).caption("Название").width("33%").editable()/*.filter()*/.sortByDefault().showInEditor());
		addColumn(Field.asTextTo(grid).modelKey(NasPunkt.MUNICIPALITY).caption("МО").width("33%").editable()/*.filter()*/.sortable().showInEditor());
		addColumn(Field.asTextTo(grid).modelKey(NasPunkt.SIGN).caption("Обозначение").width("33%").editable()/*.filter()*/.sortable().showInEditor());
	}

}