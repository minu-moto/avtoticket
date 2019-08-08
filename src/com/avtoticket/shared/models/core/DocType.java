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
@View("core.doctypes")
@Table("core.tdoctypes")
public class DocType extends BaseModel {

	private static final long serialVersionUID = 2618062730001012095L;

	@TableField
	public static final transient String NAME = "name";

	public static final transient String ID_DOC = "id_doc";

	public DocType() {
		super(DocType.class.getName());
	}

	public String getName() {
		return getStringProp(NAME);
	}
	public void setName(String name) {
		set(NAME, name);
	}

	public Long getSourceStavId() {
		return getLongProp(ID_DOC);
	}
	public void setSourceStavId(Long id) {
		set(ID_DOC, id);
	}

}