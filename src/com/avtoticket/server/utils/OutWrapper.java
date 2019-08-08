package com.avtoticket.server.utils;

public class OutWrapper<T extends Object> {

	private int errorCode;
	private String errorText;
	private T content;

	public OutWrapper() {
		this(null, 0, null);
	}

	public OutWrapper(T content) {
		this(content, 0, null);
	}

	public OutWrapper(int errorCode, String errorText) {
		this(null, errorCode, errorText);
	}

	public OutWrapper(T content, int errorCode, String errorText) {
		this.content = content;
		this.errorCode = errorCode;
		this.errorText = errorText;
	}

	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorText() {
		return errorText;
	}
	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}

	public T getContent() {
		return content;
	}
	public void setContent(T content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "{errorCode:" + errorCode + ", errorText:'" + errorText + "', content:" + content + "}";
	}

}