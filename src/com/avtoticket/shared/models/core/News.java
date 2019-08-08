package com.avtoticket.shared.models.core;

import java.util.Date;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Table;
import com.avtoticket.shared.models.TableField;
import com.avtoticket.shared.models.View;

/**
 * Модель новости
 * 
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 23 нояб. 2015 г. 22:39:55
 */
@View("core.news")
@Table("core.tnews")
public class News extends BaseModel {

	private static final long serialVersionUID = -9096516723408987310L;

	@TableField
	public static final transient String TITLE = "title";
	@TableField
	public static final transient String IMAGE = "imagesrc";
	@TableField
	public static final transient String DATE_CREATE = "date_create";
	@TableField
	public static final transient String DESCRIPTION = "description";
	@TableField
	public static final transient String LOCALE = "locale";

	public News() {
		super(News.class.getName());
	}

	public String getTitle() {
		return getStringProp(TITLE);
	}
	public void setTitle(String value) {
		set(TITLE, value);
	}

	public Date getDateCreate() {
		return getDateProp(DATE_CREATE);
	}
	public void setDateCreate(Date value) {
		set(DATE_CREATE, value);
	}

	public String getDescription() {
		return getStringProp(DESCRIPTION);
	}
	public void setDescription(String value) {
		set(DESCRIPTION, value);
	}

	public String getImage() {
		return getStringProp(IMAGE);
	}
	public void setImage(String val) {
		set(IMAGE, val);
	}

	public Locales getLocale() {
		return getEnumProp(LOCALE);
	}
	public void setLocale(Locales locale) {
		set(LOCALE, locale);
	}

}