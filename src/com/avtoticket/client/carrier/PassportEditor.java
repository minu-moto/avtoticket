/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.carrier;

import com.avtoticket.client.carrier.CarrierPlace.Style;
import com.avtoticket.client.ui.Editor;
import com.avtoticket.shared.models.BaseModel;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 19 сент. 2016 г. 23:58:04
 */
public class PassportEditor extends FlexTable implements Editor<BaseModel> {

	private TextBox edForm = new TextBox();
	private TextBox edName = new TextBox();
	private TextBox edLicense = new TextBox();
	private TextBox edIndex = new TextBox();
	private TextBox edRegion = new TextBox();
	private TextBox edCity = new TextBox();
	private TextBox edStreet = new TextBox();
	private Anchor btnNext = new Anchor("Далее");

	public PassportEditor(Style CSS) {
		addStyleName(CSS.atCarrierPassport());

		setText(0, 0, "Заполните паспорт перевозчика");
		getCellFormatter().addStyleName(0, 0, CSS.atCarrierPassportCaption());

		edForm.getElement().setAttribute("placeholder", "Форма организации");
		setWidget(1, 0, edForm);

		edName.getElement().setAttribute("placeholder", "Название организации");
		setWidget(2, 0, edName);

		edLicense.getElement().setAttribute("placeholder", "Лицензия на перевозку");
		setWidget(3, 0, edLicense);

		setText(4, 0, "Юридический адрес");

		edIndex.getElement().setAttribute("placeholder", "Индекс");
		setWidget(5, 0, edIndex);

		edRegion.getElement().setAttribute("placeholder", "Край, область");
		setWidget(6, 0, edRegion);

		edCity.getElement().setAttribute("placeholder", "Город");
		setWidget(7, 0, edCity);

		edStreet.getElement().setAttribute("placeholder", "Улица, номер строения");
		setWidget(8, 0, edStreet);

		btnNext.addStyleName(CSS.atCarrierPassportNextBtn());
		setWidget(8, 1, btnNext);
	}

	@Override
	public void edit(BaseModel model) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean flush() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BaseModel getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		// TODO Auto-generated method stub
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return btnNext.addClickHandler(handler);
	}

}