/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.profile;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.avtoticket.client.profile.ProfilePlace.Style;
import com.avtoticket.client.ui.DateTextBox;
import com.avtoticket.client.ui.EnumListBox;
import com.avtoticket.client.ui.ModelListBox;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.Waiter;
import com.avtoticket.shared.models.core.DocType;
import com.avtoticket.shared.models.core.Gender;
import com.avtoticket.shared.models.core.Nationality;
import com.avtoticket.shared.models.core.User;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 13 янв. 2016 г. 22:49:37
 */
public class AccountEditor extends AbsolutePanel {

	private User model;
	private Label lblTitle = new Label("Редактирование профиля");
	private TextBox edLastName = new TextBox();
	private TextBox edFirstName = new TextBox();
	private TextBox edMiddleName = new TextBox();
	private TextBox edPhone = new TextBox();
	private EnumListBox<Gender> lbGender = new EnumListBox<Gender>(false, Gender.class);
	private ModelListBox<DocType> lbDocType = new ModelListBox<DocType>(false);
	private ListDataProvider<DocType> ldpDocType = new ListDataProvider<DocType>();
	private TextBox edSeries = new TextBox();
	private TextBox edNumber = new TextBox();
	private ModelListBox<Nationality> lbNationality = new ModelListBox<Nationality>(false);
	private ListDataProvider<Nationality> ldpNationality = new ListDataProvider<Nationality>();
	private DateTextBox dtpDocDate = new DateTextBox();
	private DateTextBox dtpBirthday = new DateTextBox();
	private Anchor btnEdit = new Anchor("Сохранить");

	public AccountEditor(Style CSS) {
		addStyleName(CSS.atProfilePanel());

		lblTitle.addStyleName(CSS.atProfileAccountEditorTitle());
		add(lblTitle, 530, 30);

		edLastName.getElement().setAttribute("placeholder", "Фамилия");
		edLastName.addStyleName(CSS.atProfileAccountField());
		add(edLastName, 10, 120);

		edFirstName.getElement().setAttribute("placeholder", "Имя");
		edFirstName.addStyleName(CSS.atProfileAccountField());
		add(edFirstName, 10, 200);

		edMiddleName.getElement().setAttribute("placeholder", "Отчество");
		edMiddleName.addStyleName(CSS.atProfileAccountField());
		add(edMiddleName, 10, 280);

		edPhone.getElement().setAttribute("placeholder", "Моб. тел.");
		edPhone.addStyleName(CSS.atProfileAccountField());
		add(edPhone, 10, 360);

		lbGender.getElement().setAttribute("placeholder", "Пол");
		lbGender.addStyleName(CSS.atProfileAccountField());
		add(lbGender, 470, 120);

		ldpDocType.addDataDisplay(lbDocType);
		lbDocType.getElement().setAttribute("placeholder", "Документ");
		lbDocType.addStyleName(CSS.atProfileAccountField());
		add(lbDocType, 470, 200);

		edSeries.getElement().setAttribute("placeholder", "Серия");
		edSeries.addStyleName(CSS.atProfileAccountField());
		edSeries.setWidth("100px");
		add(edSeries, 470, 280);

		edNumber.getElement().setAttribute("placeholder", "Номер");
		edNumber.addStyleName(CSS.atProfileAccountField());
		edNumber.setWidth("156px");
		add(edNumber, 595, 280);

		dtpDocDate.getDatePicker().setYearAndMonthDropdownVisible(true);
		dtpDocDate.getDatePicker().setYearArrowsVisible(true);
		dtpDocDate.getElement().setAttribute("placeholder", "Дата выдачи");
		dtpDocDate.addStyleName(CSS.atProfileAccountField());
		add(dtpDocDate, 930, 120);

		ldpNationality.addDataDisplay(lbNationality);
		lbNationality.getElement().setAttribute("placeholder", "Гражданство");
		lbNationality.addStyleName(CSS.atProfileAccountField());
		add(lbNationality, 930, 200);

		dtpBirthday.getDatePicker().setYearAndMonthDropdownVisible(true);
		dtpBirthday.getDatePicker().setYearArrowsVisible(true);
		dtpBirthday.getElement().setAttribute("placeholder", "Дата рождения");
		dtpBirthday.addStyleName(CSS.atProfileAccountField());
		add(dtpBirthday, 930, 280);

		btnEdit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (flush()) {
					Waiter.start();
					RPC.getTS().updateUser(getModel(), new AsyncCallback<Void>() {
						@Override
						public void onSuccess(Void result) {
							Waiter.stop();
							onUpdated();
							onClose();
						}

						@Override
						public void onFailure(Throwable caught) {
							Waiter.stop();
							Window.alert(caught.getMessage());
							onClose();
						}
					});
				} else
					onClose();
			}
		});
		btnEdit.addStyleName(CSS.atProfileAccountEditBtn());
		add(btnEdit);

		RPC.getTS().getNationalities(new AsyncCallback<List<Nationality>>() {
			@Override
			public void onSuccess(List<Nationality> result) {
				ldpNationality.setList(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
				ldpNationality.setList(Collections.emptyList());
			}
		});
		RPC.getTS().getDocTypes(new AsyncCallback<List<DocType>>() {
			@Override
			public void onSuccess(List<DocType> result) {
				ldpDocType.setList(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
				ldpDocType.setList(Collections.emptyList());
			}
		});
	}

	public void setModel(User user) {
		model = user;
		if (user != null) {
			edLastName.setValue(user.getLastName());
			edFirstName.setValue(user.getFirstName());
			edMiddleName.setValue(user.getMiddleName());
			edPhone.setValue(user.getPhone());
			edSeries.setValue(user.getPaspSeriya());
			edNumber.setValue(user.getPaspNumber());
			dtpBirthday.setValue(user.getBirthday());
			dtpDocDate.setValue(user.getVdate());
			lbGender.setValue((user.getGender() != null) ? user.getGender().toString() : null);
			lbNationality.setValue(user.getGrajdName());
			lbDocType.setValue(user.getDocument());
		} else {
			edLastName.setValue(null);
			edFirstName.setValue(null);
			edMiddleName.setValue(null);
			edPhone.setValue(null);
			edSeries.setValue(null);
			edNumber.setValue(null);
			dtpBirthday.setValue(null);
			dtpDocDate.setValue(null);
			lbGender.setValue(null);
			lbNationality.setValue(null);
			lbDocType.setValue(null);
		}
	}

	public User getModel() {
		return model;
	}

	public boolean flush() {
		boolean ret = false;
		if (model == null)
			return ret;
		if (!Objects.equals(model.getLastName(), edLastName.getValue())) {
			model.setLastName(edLastName.getValue());
			ret = true;
		}
		if (!Objects.equals(model.getFirstName(), edFirstName.getValue())) {
			model.setFirstName(edFirstName.getValue());
			ret = true;
		}
		if (!Objects.equals(model.getMiddleName(), edMiddleName.getValue())) {
			model.setMiddleName(edMiddleName.getValue());
			ret = true;
		}
		if (!Objects.equals(model.getPhone(), edPhone.getValue())) {
			model.setPhone(edPhone.getValue());
			ret = true;
		}
		if (!Objects.equals(model.getPaspSeriya(), edSeries.getValue())) {
			model.setPaspSeriya(edSeries.getValue());
			ret = true;
		}
		if (!Objects.equals(model.getPaspNumber(), edNumber.getValue())) {
			model.setPaspNumber(edNumber.getValue());
			ret = true;
		}
		if (!Objects.equals(model.getBirthday(), dtpBirthday.getValue())) {
			model.setBirthday(dtpBirthday.getValue());
			ret = true;
		}
		if (!Objects.equals(model.getVdate(), dtpDocDate.getValue())) {
			model.setVdate(dtpDocDate.getValue());
			ret = true;
		}
		if (!Objects.equals(model.getGender(), lbGender.getSelectedModel())) {
			model.setGender(lbGender.getSelectedModel());
			ret = true;
		}
		if (!Objects.equals(model.getGrajdName(), lbNationality.getValue())) {
			model.setGrajdName(lbNationality.getValue());
			Nationality n = lbNationality.getSelectedModel();
			model.setGrajd((n != null) ? n.getId() : null);
			ret = true;
		}
		if (!Objects.equals(model.getDocument(), lbDocType.getValue())) {
			model.setDocument(lbDocType.getValue());
			DocType dt = lbDocType.getSelectedModel();
			model.setDocType((dt != null) ? dt.getId() : null);
			ret = true;
		}
		return ret;
	}

	public boolean checkFileds() {
		return (edLastName.getValue() != null) && !edLastName.getValue().isEmpty()
				&& (edFirstName.getValue() != null) && !edFirstName.getValue().isEmpty()
				&& (edMiddleName.getValue() != null) && !edMiddleName.getValue().isEmpty()
				&& (edSeries.getValue() != null) && !edSeries.getValue().isEmpty()
				&& (edNumber.getValue() != null) && !edNumber.getValue().isEmpty()
				&& (edPhone.getValue() != null) && !edPhone.getValue().isEmpty()
				&& (lbGender.getSelectedModel() != null)
				&& (lbDocType.getSelectedModel() != null)
				&& (lbNationality.getSelectedModel() != null)
				&& (dtpBirthday.getValue() != null)
				&& (dtpDocDate.getValue() != null);
	}

	public void onUpdated() { }

	public void onClose() { }

}