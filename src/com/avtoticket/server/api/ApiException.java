/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.server.api;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 24 июля 2016 г. 4:26:59
 */
public class ApiException extends Exception {

	private static final long serialVersionUID = 5687134271898250702L;

	private int errorCode;

	public ApiException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public ApiException(int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	@Override
	public String toString() {
		return errorCode + " - " + getMessage();
	}

}