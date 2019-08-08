/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.shared.models.core;

import java.util.Date;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.View;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 24 дек. 2015 г. 19:02:18
 */
@View("core.tstationlinks")
@Table("core.tstationlinks")
public class StationLink extends BaseModel {

	private static final long serialVersionUID = -4636175122892537442L;

	@TableField
	public static final transient String DEP_ID = "depid";
	@TableField
	public static final transient String DEST_ID = "destid";
	@TableField
	public static final transient String DATEOP = "dateop";

	public StationLink() {
		super(StationLink.class.getName());
	}

	public Long getDepId() {
		return getLongProp(DEP_ID);
	}
	public void setDepId(Long val) {
		set(DEP_ID, val);
	}

	public Long getDestId() {
		return getLongProp(DEST_ID);
	}
	public void setDestId(Long val) {
		set(DEST_ID, val);
	}

	public Date getDateOp() {
		return getDateProp(DATEOP);
	}
	public void setDateOp(Date dateOp) {
		set(DATEOP, dateOp);
	}

}