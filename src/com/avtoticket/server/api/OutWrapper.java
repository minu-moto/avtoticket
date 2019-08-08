package com.avtoticket.server.api;

public class OutWrapper<T extends Object> {

	private int error_code;
	private String error_text;
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
		this.error_code = errorCode;
		this.error_text = errorText;
	}

	public int getErrorCode() {
		return error_code;
	}
	public void setErrorCode(int errorCode) {
		this.error_code = errorCode;
	}

	public String getErrorText() {
		return error_text;
	}
	public void setErrorText(String errorText) {
		this.error_text = errorText;
	}

	public T getContent() {
		return content;
	}
	public void setContent(T content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "{error_code:" + error_code + ", error_text:'" + error_text + "', content:" + content + "}";
	}

}