/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.shared.models.core;

import java.util.function.Function;

import com.avtoticket.shared.models.EnumType;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.Constants;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 26 янв. 2016 г. 22:47:25
 */
@EnumType("core.help_type")
public enum HelpType {

	QUESTIONS(Strings::questions),
	INFO(Strings::info),
	DOCS(Strings::docs),
	COMPANY(Strings::company),
	ABOUT(Strings::about),
	CONFIDENTIALITY(Strings::confidentiality);

	private static final String QUESTIONS_CAPTION = "Вопросы";
	private static final String INFO_CAPTION = "Информация";
	private static final String DOCS_CAPTION = "Документы";
	private static final String COMPANY_CAPTION = "Оазис";
	private static final String ABOUT_CAPTION = "О нас";
	private static final String CONFIDENTIALITY_CAPTION = "Конфиденциальность";

	public interface Strings extends Constants {
		@DefaultStringValue(QUESTIONS_CAPTION)
		String questions();

		@DefaultStringValue(INFO_CAPTION)
		String info();

		@DefaultStringValue(DOCS_CAPTION)
		String docs();

		@DefaultStringValue(COMPANY_CAPTION)
		String company();

		@DefaultStringValue(ABOUT_CAPTION)
		String about();

		@DefaultStringValue(CONFIDENTIALITY_CAPTION)
		String confidentiality();
	}

	private static Strings getStrings() {
		if (GWT.isClient())
			return GWT.create(Strings.class);
		else
			return new Strings() {
				@Override
				public String questions() {
					return QUESTIONS_CAPTION;
				}
	
				@Override
				public String info() {
					return INFO_CAPTION;
				}
	
				@Override
				public String docs() {
					return DOCS_CAPTION;
				}
	
				@Override
				public String company() {
					return COMPANY_CAPTION;
				}
	
				@Override
				public String about() {
					return ABOUT_CAPTION;
				}

				@Override
				public String confidentiality() {
					return CONFIDENTIALITY_CAPTION;
				}
		};
	}

	private static final Strings STRINGS = getStrings();

	private final Function<Strings, String> textSupplier;

	private HelpType(Function<Strings, String> textSupplier) {
		this.textSupplier = textSupplier;
	}

	@Override
	public String toString() {
		return textSupplier.apply(STRINGS);
	}

}