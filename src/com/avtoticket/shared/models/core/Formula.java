/*
 * Copyright Avtoticket (c) 2017.
 */
package com.avtoticket.shared.models.core;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.View;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 02 09 22:17:13
 */
@View("core.tformulas")
@Table("core.tformulas")
public class Formula extends BaseModel {

	private static final long serialVersionUID = -4572578474646089672L;

	@TableField
	public static final transient String PASSAGE_ID = "passage_id";
	@TableField
	public static final transient String ADULT_PRICE_FORMULA = "adult_price_formula";
	@TableField
	public static final transient String CHILD_PRICE_FORMULA = "child_price_formula";
	@TableField
	public static final transient String BAG_PRICE_FORMULA = "bag_price_formula";

	public Formula() {
		super(Formula.class.getName());
	}

	public Long getPassageId() {
		return getLongProp(PASSAGE_ID);
	}
	public void setPassageId(Long id) {
		put(PASSAGE_ID, id);
	}

	public String getAdultPriceFormula() {
		return getStringProp(ADULT_PRICE_FORMULA);
	}
	public void setAdultPriceFormula(String formula) {
		set(ADULT_PRICE_FORMULA, formula);
	}

	public String getChildPriceFormula() {
		return getStringProp(CHILD_PRICE_FORMULA);
	}
	public void setChildPriceFormula(String formula) {
		set(CHILD_PRICE_FORMULA, formula);
	}

	public String getBagPriceFormula() {
		return getStringProp(BAG_PRICE_FORMULA);
	}
	public void setBagPriceFormula(String formula) {
		set(BAG_PRICE_FORMULA, formula);
	}

}