/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.profile;

import java.util.List;

import com.avtoticket.client.ui.DBTable;
import com.avtoticket.client.ui.PagedDataProvider;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.shared.models.PageContainer;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.DocType;
import com.avtoticket.shared.models.core.Gender;
import com.avtoticket.shared.models.core.Nationality;
import com.avtoticket.shared.models.core.Requisite;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 11 февр. 2016 г. 23:05:08
 */
public class RequisitesTable extends DBTable<Requisite, Long> {

	public RequisitesTable() {
		super("Шаблоны", new Requisite());
	}

	@Override
	protected void buildDataProvider() {
		dataProvider = new PagedDataProvider<Requisite, Long>(Requisite.class, modelKeyProvider, grid) {
			@Override
			protected void getObjects(String className, List<Long> forDel, Where where, String sortColumn,
					AsyncCallback<PageContainer<Requisite>> callback) {
				RPC.getTS().getRequisites(forDel, where, sortColumn, callback);
			}
		};
		dataProvider.addDataDisplay(grid);
	}

	@Override
	protected void save(Requisite saveModel, AsyncCallback<Requisite> callback) {
		RPC.getTS().saveRequisite(saveModel, callback);
	}

	@Override
	protected void buildTableColumns() {
		addColumn(Field.asTextTo(grid).modelKey(Requisite.LASTNAME).caption("Фамилия").width(100).editable().require()/*.filter()*/.sortByDefault().showInEditor());
		addColumn(Field.asTextTo(grid).modelKey(Requisite.FIRSTNAME).caption("Имя").width(100).editable().require()/*.filter()*/.sortable().showInEditor());
		addColumn(Field.asTextTo(grid).modelKey(Requisite.MIDDLENAME).caption("Отчество").width(100).editable().require()/*.filter()*/.sortable().showInEditor());
		addColumn(Field.asEnumTo(grid, Gender.class).modelKey(Requisite.GENDER).caption("Пол").width(80).editable().require().sortable()/*.showInEditor()*/);
		addColumn(Field.asTextTo(grid).modelKey(Requisite.PHONE).caption("Телефон").width(110).editable().require()/*.filter()*/.sortable().showInEditor());
		addColumn(Field.asDateTo(grid).modelKey(Requisite.B_DATE).caption("Дата рождения").width(100).editable().require().sortable().showInEditor());
		addColumn(Field.asListTo(grid, DocType.class).modelKey(Requisite.DOCTYPE).caption("Тип документа").width(130).editable().require().sortable()/*.showInEditor()*/);
		addColumn(Field.asTextTo(grid).modelKey(Requisite.PASP_SERIYA).caption("Серия").width(80).editable().require()/*.filter()*/.sortable().showInEditor());
		addColumn(Field.asTextTo(grid).modelKey(Requisite.PASP_NUMBER).caption("Номер").width(80).editable().require()/*.filter()*/.sortable().showInEditor());
		addColumn(Field.asDateTo(grid).modelKey(Requisite.V_DATE).caption("Дата выдачи").width(100).editable().require().sortable().showInEditor());
		addColumn(Field.asListTo(grid, Nationality.class).modelKey(Requisite.GRAJD).caption("Гражданство").width(100).editable().require().sortable()/*.showInEditor()*/);
	}

}