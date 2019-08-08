/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.login;

import com.avtoticket.client.login.LoginPanel.Strings;
import com.avtoticket.client.login.LoginPanel.Style;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.Waiter;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 6 янв. 2016 г. 21:49:08
 */
public class RegisterDialog extends PopupPanel {

	private final Strings STRINGS;
	private FlexTable layout = new FlexTable();
	private TextBox edLogin = new TextBox();
	private PasswordTextBox edPassword = new PasswordTextBox();
	private Anchor btnReg;
//	private Anchor btnCarrier;

	public RegisterDialog(Style CSS, Strings STRINGS) {
		this.STRINGS = STRINGS;
		setAnimationEnabled(true);
		setAutoHideEnabled(true);
		setAutoHideOnHistoryEventsEnabled(true);
		setGlassEnabled(true);
		setModal(true);
		setWidget(layout);
		layout.addStyleName(CSS.atLoginRegisterDialogLayout());

		addDomHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == 13)
					onRegClick();
			}
		}, KeyDownEvent.getType());

		layout.setText(0, 0, STRINGS.register());
		layout.setHTML(1, 0, STRINGS.confirmMessage());
		layout.setWidget(2, 0, edLogin);
		layout.setWidget(3, 0, edPassword);
		btnReg = new Anchor(STRINGS.doRegister());
		layout.setWidget(4, 0, btnReg);
//		btnCarrier = new Anchor(STRINGS.carrier(), "#" + CarrierPlace.NAME);
//		layout.setWidget(5, 0, btnCarrier);

		layout.getCellFormatter().addStyleName(0, 0, CSS.atLoginRegisterDialogTitle());
		layout.getCellFormatter().addStyleName(1, 0, CSS.atLoginRegisterDialogHint());
		edLogin.addStyleName(CSS.atLoginRegisterDialogFields());
		edPassword.addStyleName(CSS.atLoginRegisterDialogFields());
		btnReg.addStyleName(CSS.atLoginRegisterDialogBtn());

		edLogin.getElement().setAttribute("placeholder", STRINGS.email());
		edPassword.getElement().setAttribute("placeholder", STRINGS.password());
		btnReg.addClickHandler(event -> onRegClick());
	}

	private void onRegClick() {
		if ((edLogin.getValue() == null) || edLogin.getValue().isEmpty())
			Window.alert(STRINGS.email() + STRINGS.notEmpty());
		else if (!edLogin.getValue().contains("@"))
			Window.alert(STRINGS.invalidEmail());
		else if ((edPassword.getValue() == null) || edPassword.getValue().isEmpty())
			Window.alert(STRINGS.password() + STRINGS.notEmpty());
		else {
			Waiter.start();
			RPC.getTS().isLoginFree(edLogin.getValue(), new AsyncCallback<Boolean>() {
				@Override
				public void onSuccess(Boolean result) {
					if (result)
						RPC.getTS().regUser(edLogin.getValue(), edPassword.getValue(), new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								Waiter.stop();
								Window.alert(STRINGS.confirmEmail());
								hide();
							}

							@Override
							public void onFailure(Throwable caught) {
								Waiter.stop();
								Window.alert(caught.getMessage());
							}
						});
					else {
						Waiter.stop();
						Window.alert(STRINGS.emailAlreadyTaken());
					}
				}

				@Override
				public void onFailure(Throwable caught) {
					Waiter.stop();
					Window.alert(caught.getMessage());
				}
			});
		}
	}

}