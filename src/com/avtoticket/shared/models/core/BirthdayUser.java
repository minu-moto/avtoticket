/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.shared.models.core;

import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.View;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 8 мая 2016 г. 19:59:55
 */
@View("core.birthday_users")
@Table("core.tuser")
public class BirthdayUser extends User {

	private static final long serialVersionUID = 7231170095964732544L;

	public BirthdayUser() {
		super(BirthdayUser.class.getName());
	}

}