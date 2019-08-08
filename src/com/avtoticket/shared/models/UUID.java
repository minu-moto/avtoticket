/*
 * Copyright Бездна (c) 2016.
 */
package com.avtoticket.shared.models;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 25.10.2016 23:20:38
 */
public class UUID implements Serializable {

	private static final long serialVersionUID = 138903768189871394L;

	private String val;

	public UUID() {
		this("00000000-0000-0000-0000-000000000000");
	}

	public UUID(String val) {
		this.val = Objects.requireNonNull(val);
        String[] components = val.split("-");
        if (components.length != 5)
            throw new IllegalArgumentException("Invalid UUID string: " + val);
	}

	@Override
	public String toString() {
		return val;
	}

}