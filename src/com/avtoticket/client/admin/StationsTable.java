/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.admin;

import com.avtoticket.client.ui.DBTable;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.shared.models.core.NasPunkt;
import com.avtoticket.shared.models.core.Station;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 12 февр. 2016 г. 0:42:31
 */
public class StationsTable extends DBTable<Station, Long> {

	public StationsTable() {
		super("Автостанции", new Station());
	}

	@Override
	protected void buildTableColumns() {
		addColumn(Field.asTextTo(grid).modelKey(Station.NAME).caption("Название").editable().width("100%").sortByDefault().showInEditor());
		addColumn(Field.asLongTo(grid).modelKey(Station.KPAS_ID).caption("ID КПАС").editable().width(80).sortable().showInEditor());
		addColumn(Field.asTextTo(grid).modelKey(Station.STAV_ID).caption("ID СтавАвто").editable().width(80).sortable().showInEditor());
		addColumn(Field.asListTo(grid, NasPunkt.class).modelKey(Station.NASPUNKT_ID).caption("Населенный пункт").editable().width(140).sortable());
		addColumn(Field.asTextTo(grid).modelKey(Station.ADDRESS).caption("Адрес").editable().width(200).sortable().showInEditor());
		addColumn(Field.asFloatTo(grid).modelKey(Station.LAT).caption("Широта").editable().width(100).sortable().showInEditor().format("0.##########"));
		addColumn(Field.asFloatTo(grid).modelKey(Station.LNG).caption("Долгота").editable().width(100).sortable().showInEditor().format("0.##########"));
		addColumn(Field.asBooleanTo(grid).modelKey(Station.IS_DEPARTURE_POINT).caption("Активировать").editable().width(90).sortable().showInEditor());
		addColumn(Field.asTextTo(grid).modelKey(Station.HOST).caption("Адрес сервера").editable().width(140).sortable().showInEditor());
	}

}