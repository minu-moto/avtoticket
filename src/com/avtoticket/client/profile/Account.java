/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.profile;

import java.util.Date;

import com.avtoticket.client.profile.ProfilePlace.Style;
import com.avtoticket.shared.models.core.User;
import com.avtoticket.shared.utils.DateUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 12 янв. 2016 г. 23:16:35
 */
public class Account extends AbsolutePanel {

	private static final DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MM.yyyy");

	private Label lblLastName = new Label();
	private Label lblFirstName = new Label();
	private Label lblAge = new Label();
	private Label lblGender = new Label("пол:");
	private Label lblGenderVal = new Label();
	private Label lblBirthday = new Label("дата рождения:");
	private Label lblBirthdayVal = new Label();
	private Label lblDocument = new Label("Документ:");
	private Label lblDocumentVal = new Label();
	private Label lblSeries = new Label("Серия:");
	private Label lblSeriesVal = new Label();
	private Label lblNumber = new Label("Номер:");
	private Label lblNumberVal = new Label();
	private Label lblNationality = new Label("Гражданство:");
	private Label lblNationalityVal = new Label();
	private Label lblDocDate = new Label("Дата выдачи:");
	private Label lblDocDateVal = new Label();
	private Label lblPhone = new Label("Мобильный телефон:");
	private Label lblPhoneVal = new Label();

	private FlowPanel avatar = new FlowPanel();
	private Anchor btnEdit = new Anchor("Редактировать");

	public Account(Style CSS) {
		addStyleName(CSS.atProfilePanel());

		avatar.addStyleName(CSS.atProfileAvatar());
		add(avatar, 0, 40);

		lblLastName.addStyleName(CSS.atProfileLastName());
		add(lblLastName, 350, 46);

		lblFirstName.addStyleName(CSS.atProfileFirstName());
		add(lblFirstName, 350, 100);

		lblAge.addStyleName(CSS.atProfileAge());
		add(lblAge);

		lblGender.addStyleName(CSS.atProfileAccountLabel());
		add(lblGender, 350, 160);
		lblGenderVal.addStyleName(CSS.atProfileAccountValue());
		add(lblGenderVal, 394, 160);

		lblBirthday.addStyleName(CSS.atProfileAccountLabel());
		add(lblBirthday, 590, 160);
		lblBirthdayVal.addStyleName(CSS.atProfileAccountValue());
		add(lblBirthdayVal, 740, 160);

		lblDocument.addStyleName(CSS.atProfileAccountLabel());
		add(lblDocument, 30, 370);
		lblDocumentVal.addStyleName(CSS.atProfileAccountValue());
		add(lblDocumentVal, 130, 370);

		lblSeries.addStyleName(CSS.atProfileAccountLabel());
		add(lblSeries, 30, 418);
		lblSeriesVal.addStyleName(CSS.atProfileAccountValue());
		add(lblSeriesVal, 100, 418);

		lblNumber.addStyleName(CSS.atProfileAccountLabel());
		add(lblNumber, 230, 418);
		lblNumberVal.addStyleName(CSS.atProfileAccountValue());
		add(lblNumberVal, 302, 418);

		lblNationality.addStyleName(CSS.atProfileAccountLabel());
		add(lblNationality, 30, 466);
		lblNationalityVal.addStyleName(CSS.atProfileAccountValue());
		add(lblNationalityVal, 164, 466);

		lblDocDate.addStyleName(CSS.atProfileAccountLabel());
		add(lblDocDate, 520, 418);
		lblDocDateVal.addStyleName(CSS.atProfileAccountValue());
		add(lblDocDateVal, 652, 418);

		lblPhone.addStyleName(CSS.atProfileAccountLabel());
		add(lblPhone, 520, 466);
		lblPhoneVal.addStyleName(CSS.atProfileAccountValue());
		add(lblPhoneVal, 728, 466);

		btnEdit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onEditClick();
			}
		});
		btnEdit.addStyleName(CSS.atProfileAccountEditBtn());
		add(btnEdit);
	}

	public void fillForm(User user) {
		if (user != null) {
			if (!user.getAvatarId().isEmpty())
				avatar.getElement().getStyle().setBackgroundImage("url('/download?width=400&height=400&id=" + user.getAvatarId() + "')");
			else
				avatar.getElement().getStyle().clearBackgroundImage();
			lblLastName.setText(user.getLastName());
			lblFirstName.setText(user.getFirstName()
					+ (((user.getMiddleName() != null) && !user.getMiddleName().isEmpty()) ? " " + user.getMiddleName() : ""));
			lblAge.setText((user.getBirthday() != null) ? String.valueOf(DateUtil.getAge(user.getBirthday(), DateUtil.localToMsk(new Date()))) : "");
			lblGenderVal.setText((user.getGender() != null) ? user.getGender().toString() : "");
			lblBirthdayVal.setText((user.getBirthday() != null) ? dateFormat.format(user.getBirthday(), DateUtil.getMSKTimeZone()) : null);
			lblDocumentVal.setText(user.getDocument());
			lblSeriesVal.setText(user.getPaspSeriya());
			lblNumberVal.setText(user.getPaspNumber());
			lblNationalityVal.setText(user.getGrajdName());
			lblDocDateVal.setText((user.getVdate() != null) ? dateFormat.format(user.getVdate(), DateUtil.getMSKTimeZone()) : null);
			lblPhoneVal.setText(user.getPhone());
		} else {
			avatar.getElement().getStyle().clearBackgroundImage();
			lblLastName.setText(null);
			lblFirstName.setText(null);
			lblAge.setText(null);
			lblGenderVal.setText(null);
			lblBirthdayVal.setText(null);
			lblDocumentVal.setText(null);
			lblSeriesVal.setText(null);
			lblNumberVal.setText(null);
			lblNationalityVal.setText(null);
			lblDocDateVal.setText(null);
			lblPhoneVal.setText(null);
		}
	}

	public void onEditClick() {
		
	}

}