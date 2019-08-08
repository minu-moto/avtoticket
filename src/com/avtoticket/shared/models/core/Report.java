/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket.shared.models.core;

import java.util.List;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.View;

/**
 * Модель отчёта
 * 
 * @author Minu <<a href='minu-moto@mail.ru'>minu-moto@mail.ru</a>>
 * @since 05.03.2012 9:37:18
 */
@View("core.reports")
@Table("core.treports")
public class Report extends BaseModel {

	private static final long serialVersionUID = 4472408167141657621L;

	/* ******** REPORTS ******************* */
	/** Купленные билеты */
	public static final String TICKETS = "tickets";
	/* ************************************ */

	/** Название отчёта */
	@TableField
	public static final transient String NAME = "name";
	/** Уникальная текстовая метка отчёта */
	@TableField
	public static final transient String SIGN = "sign";
	/** Xml с формой отчёта */
	@TableField
	public static final transient String FORM = "form";

	/** Список параметров отчёта */
	public static final transient String PARAMS = "params";

	/** Свойства отчёта */
	public static final transient String PROPS = "props";

	/**
	 * Новый экземпляр модели
	 */
	public Report() {
		super(Report.class.getName());
	}

	/**
	 * Новый экземпляр модели
	 *
	 * @param name - название отчёта
	 * @param sign - уникальная метка отчёта
	 * @param form - форма отчёта
	 */
	public Report(String name, String sign, String form) {
		this();
		setName(name);
		setSign(sign);
		setReportForm(form);
	}

	/**
	 * @return название отчёта
	 */
	public String getName() {
		return getStringProp(NAME);
	}
	public void setName(String name) {
		set(NAME, name);
	}

	/**
	 * @return уникальная текстовая метка отчёта
	 */
	public String getSign() {
		return getStringProp(SIGN);
	}
	public void setSign(String sign) {
		set(SIGN, sign);
	}

	/**
	 * @return xml с формой отчёта
	 */
	public String getReportForm() {
		return getStringProp(FORM);
	}
	public void setReportForm(String form) {
		set(FORM, form);
	}

	/**
	 * @return список параметров отчёта
	 */
	public List<ReportParam> getParams() {
		return getListProp(PARAMS);
	}
	public void setParams(List<ReportParam> params) {
		set(PARAMS, params);
	}

	/**
	 * @return список параметров отчёта
	 */
	public BaseModel getProps() {
		return getModelProp(PROPS);
	}
	public void setProps(BaseModel props) {
		set(PROPS, props);
	}

}