/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.shared.models.core;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.View;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 16 янв. 2016 г. 22:52:28
 */
@View("core.grajd")
@Table("core.tgrajd")
public class Nationality extends BaseModel {

	private static final long serialVersionUID = 2618062730001012095L;

	@TableField
	public static final transient String NAME = "name";
	@TableField
	public static final transient String MAIN = "main";
	@TableField
	public static final transient String OKSM = "oksm";

	public static final transient String ID_NATIONALITY = "id_nationality";

	public Nationality() {
		super(Nationality.class.getName());
	}

	public String getName() {
		return getStringProp(NAME);
	}
	public void setName(String name) {
		set(NAME, name);
	}

	public Boolean isMain() {
		return getBooleanProp(MAIN);
	}
	public void setMain(Boolean isMain) {
		set(MAIN, isMain);
	}

	public String getOksm() {
		return getStringProp(OKSM);
	}
	public void setOksm(String id) {
		set(OKSM, id);
	}

	public String getSourceStavId() {
		return getStringProp(ID_NATIONALITY);
	}
	public void setSourceStavId(String id) {
		set(ID_NATIONALITY, id);
	}

}