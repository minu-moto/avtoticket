/*
 * Copyright Avtoticket (c) 2018.
 */
package com.avtoticket.shared.models.core;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.View;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 28 сент. 2018 г. 20:55:15
 */
@View("core.tradios")
@Table("core.tradios")
public class Radio extends BaseModel {

	private static final long serialVersionUID = -3303123994686888348L;

	@TableField
	public static final transient String NAME = "name";
	@TableField
	public static final transient String URL = "url";
	@TableField
	public static final transient String ICON = "icon";

	public Radio() {
		super(Radio.class.getName());
	}

	public String getName() {
		return getStringProp(NAME);
	}
	public void setName(String name) {
		set(NAME, name);
	}

	public String getUrl() {
		return getStringProp(URL);
	}
	public void setUrl(String url) {
		set(URL, url);
	}

	public String getIcon() {
		return getStringProp(ICON);
	}
	public void setIcon(String icon) {
		set(ICON, icon);
	}

}