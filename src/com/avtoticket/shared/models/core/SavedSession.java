/*
 * Copyright Avtoticket (c) 2018.
 */
package com.avtoticket.shared.models.core;

import java.util.Date;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.UUID;
import com.avtoticket.shared.models.View;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 4 марта 2018 г. 1:46:15
 */
@View("core.tsessions")
@Table("core.tsessions")
public class SavedSession extends BaseModel {

	private static final long serialVersionUID = -7850410069752305094L;

	@TableField
	public static final transient String USER_ID = "tuser_id";
	@TableField
	public static final transient String SESSION_ID = "session_id";
	@TableField
	public static final transient String LAST_LOGIN = "last_login";
	@TableField
	public static final transient String USER_AGENT = "useragent";
	@TableField
	public static final transient String USER_IP = "userip";

	public SavedSession() {
		super(SavedSession.class.getName());
	}

	public Long getUserId() {
		return getLongProp(USER_ID);
	}
	public void setUserId(Long val) {
		put(USER_ID, val);
	}

	public UUID getSessionId() {
		return (UUID) get(SESSION_ID);
	}
	public void setSessionId(UUID sessionId) {
		set(SESSION_ID, sessionId);
	}

	public Date getLastLogin() {
		return getDateProp(LAST_LOGIN);
	}
	public void setLastLogin(Date lastLogin) {
		set(LAST_LOGIN, lastLogin);
	}

	public String getUserAgent() {
		return getStringProp(USER_AGENT);
	}
	public void setUserAgent(String val) {
		set(USER_AGENT, val);
	}

	public String getUserIp() {
		return getStringProp(USER_IP);
	}
	public void setUserIp(String val) {
		set(USER_IP, val);
	}

}