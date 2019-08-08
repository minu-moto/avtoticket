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
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 сент. 2016 г. 0:42:04
 */
public class RequisiteEditor extends FlexTable implements Editor<BaseModel> {

	private TextBox edBank = new TextBox();
	private TextBox edINN = new TextBox();
	private TextBox edOGNIP = new TextBox();
	private TextBox edBIK = new TextBox();
	private TextBox edKPP = new TextBox();
	private TextBox edRS = new TextBox();
	private TextBox edKS = new TextBox();
	private Anchor btnNext = new Anchor("Далее");

	public RequisiteEditor(Style CSS) {
		addStyleName(CSS.atCarrierRequisite());

		setText(0, 0, "Заполните Ваши реквизиты");
		getCellFormatter().addStyleName(0, 0, CSS.atCarrierRequisiteCaption());

		edBank.getElement().setAttribute("placeholder", "Наименование банка");
		setWidget(1, 0, edBank);

		edINN.getElement().setAttribute("placeholder", "ИНН");
		setWidget(2, 0, edINN);

		edOGNIP.getElement().setAttribute("placeholder", "ОГНИП");
		setWidget(3, 0, edOGNIP);

		edBIK.getElement().setAttribute("placeholder", "БИК");
		setWidget(4, 0, edBIK);

		edKPP.getElement().setAttribute("placeholder", "КПП");
		setWidget(1, 1, edKPP);

		edRS.getElement().setAttribute("placeholder", "РС");
		setWidget(2, 1, edRS);

		edKS.getElement().setAttribute("placeholder", "КС");
		setWidget(3, 1, edKS);

		btnNext.addStyleName(CSS.atCarrierRequisiteNextBtn());
		setWidget(4, 1, btnNext);
		getCellFormatter().setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_RIGHT);
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