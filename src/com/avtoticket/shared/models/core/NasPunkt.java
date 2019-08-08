/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.shared.models.core;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.View;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 12 февр. 2016 г. 22:35:26
 */
@View("core.naspunkts")
@Table("core.tnaspunkts")
public class NasPunkt extends BaseModel {

	private static final long serialVersionUID = -4136427884658981178L;

	@TableField
	public static final transient String NAME = "name";
	@TableField
	public static final transient String MUNICIPALITY = "municipality";
	@TableField
	public static final transient String SIGN = "sign";

	public NasPunkt() {
		super(NasPunkt.class.getName());
	}

	public String getName() {
		return getStringProp(NAME);
	}
	public void setName(String name) {
		set(NAME, name);
	}

	public String getMunicipality() {
		return getStringProp(MUNICIPALITY);
	}
	public void setMunicipality(String municipality) {
		set(MUNICIPALITY, municipality);
	}

	public String getSign() {
		return getStringProp(SIGN);
	}
	public void setSign(String sign) {
		set(SIGN, sign);
	}

}