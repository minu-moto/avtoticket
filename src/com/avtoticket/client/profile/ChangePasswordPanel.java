/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.profile;

import java.util.Objects;

import com.avtoticket.client.profile.ProfilePlace.Style;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.SessionUtil;
import com.avtoticket.client.utils.Waiter;
import com.avtoticket.shared.models.UUID;
import com.avtoticket.shared.models.core.User;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 13 янв. 2016 г. 22:49:37
 */
public class ChangePasswordPanel extends AbsolutePanel {

	private Label lblTitle = new Label("Изменение пароля");
	private PasswordTextBox edOldPassword = new PasswordTextBox();
	private PasswordTextBox edNewPassword = new PasswordTextBox();
	private PasswordTextBox edConfirmPassword = new PasswordTextBox();
	private TextBox edAPIToken = new TextBox();
	private Anchor btnGenerateToken = new Anchor("Сгенерировать");
	private Anchor btnEdit = new Anchor("Сохранить");

	public ChangePasswordPanel(Style CSS) {
		addStyleName(CSS.atProfilePanel());

		lblTitle.addStyleName(CSS.atProfileAccountEditorTitle());
		add(lblTitle, 530, 30);

		edOldPassword.getElement().setAttribute("placeholder", "Текущий пароль");
		edOldPassword.addStyleName(CSS.atProfileAccountField());
		add(edOldPassword, 472, 120);

		edNewPassword.getElement().setAttribute("placeholder", "Новый пароль");
		edNewPassword.addStyleName(CSS.atProfileAccountField());
		add(edNewPassword, 472, 200);

		edConfirmPassword.getElement().setAttribute("placeholder", "Подтверждение пароля");
		edConfirmPassword.addStyleName(CSS.atProfileAccountField());
		add(edConfirmPassword, 472, 280);

		User user = SessionUtil.getUser();
		edAPIToken.setValue((user.getApiToken() != null) ? user.getApiToken().toString() : null);
		edAPIToken.getElement().setAttribute("placeholder", "API-токен");
		edAPIToken.addStyleName(CSS.atProfileAccountField());
		edAPIToken.setReadOnly(true);
		edAPIToken.setWidth("400px");
		add(edAPIToken, 412, 360);

		btnGenerateToken.addClickHandler(event -> {
			btnGenerateToken.setEnabled(false);
			Waiter.start();
			RPC.getTS().generateApiToken(
					new AsyncCallback<UUID>() {
						@Override
						public void onFailure(Throwable caught) {
							Waiter.stop();
							Window.alert(caught.getMessage());
							btnGenerateToken.setEnabled(true);
						}

						@Override
						public void onSuccess(UUID token) {
							Waiter.stop();
							user.setApiToken(token);
							btnGenerateToken.setEnabled(true);
							edAPIToken.setValue(token.toString());
						}
					});
		});
		btnGenerateToken.addStyleName(CSS.atProfileAccountEditBtn());
		add(btnGenerateToken, 820, 368);

		btnEdit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!Objects.equals(edNewPassword.getValue(), edConfirmPassword.getValue()))
					Window.alert("Введённые пароли не совпадают");
				else {
					Waiter.start();
					RPC.getTS().changePassword(edOldPassword.getValue(), edNewPassword.getValue(), new AsyncCallback<Boolean>() {
						@Override
						public void onSuccess(Boolean result) {
							Waiter.stop();
							if (result == Boolean.TRUE) {
								Window.alert("Пароль успешно изменён");
								edOldPassword.setValue(null);
								edNewPassword.setValue(null);
								edConfirmPassword.setValue(null);
							} else
								Window.alert("Текущий пароль указан неверно");
						}

						@Override
						public void onFailure(Throwable caught) {
							Waiter.stop();
							Window.alert(caught.getMessage());
						}
					});
				}
			}
		});
		btnEdit.addStyleName(CSS.atProfileAccountEditBtn());
		add(btnEdit);
	}

}