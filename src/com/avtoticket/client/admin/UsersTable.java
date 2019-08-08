/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.admin;

import com.avtoticket.client.ui.DBTable;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.shared.models.core.User;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 11 февр. 2016 г. 23:05:08
 */
public class UsersTable extends DBTable<User, Long> {

	public UsersTable() {
		super("Пользователи", new User());
	}

	@Override
	protected void buildTableColumns() {
		addColumn(Field.asTextTo(grid).modelKey(User.LASTNAME).caption("Фамилия").width(100).editable()/*.filter()*/.sortByDefault().showInEditor());
		addColumn(Field.asTextTo(grid).modelKey(User.FIRSTNAME).caption("Имя").width(100).editable()/*.filter()*/.sortable().showInEditor());
		addColumn(Field.asTextTo(grid).modelKey(User.MIDDLENAME).caption("Отчество").width(100).editable()/*.filter()*/.sortable().showInEditor());
		addColumn(Field.asTextTo(grid).modelKey(User.LOGIN).caption("Логин").width("100%").editable()/*.filter()*/.sortable().showInEditor());
		addColumn(Field.asTextTo(grid).modelKey(User.PHONE).caption("Телефон").width(110).editable()/*.filter()*/.sortable().showInEditor());
		addColumn(Field.asDateTo(grid).modelKey(User.REG_DATE).caption("Дата регистрации").width(110).sortable().format("dd.MM.yyyy HH:mm").showInEditor());
		addColumn(Field.asDateTo(grid).modelKey(User.B_DATE).caption("Дата рождения").width(110).editable().sortable().showInEditor());
		addColumn(Field.asTextTo(grid).modelKey(User.PASP_SERIYA).caption("Серия").width(80).editable()/*.filter()*/.sortable().showInEditor());
		addColumn(Field.asTextTo(grid).modelKey(User.PASP_NUMBER).caption("Номер").width(80).editable()/*.filter()*/.sortable().showInEditor());
		addColumn(Field.asBooleanTo(grid).modelKey(User.IS_ADMIN).caption("Админ").width(60).editable()/*.filter()*/.sortable().showInEditor());
		addColumn(Field.asBooleanTo(grid).modelKey(User.IS_PRINT_TICKET).caption("Печать билетов").width(60).editable()/*.filter()*/.sortable().showInEditor());
	}

}