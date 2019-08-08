/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.login;

import com.avtoticket.client.login.LoginPanel.Strings;
import com.avtoticket.client.login.LoginPanel.Style;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.Waiter;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 6 янв. 2016 г. 21:49:08
 */
public class RemindDialog extends DialogBox {

	private VerticalPanel layout = new VerticalPanel();
	private TextBox edLogin = new TextBox();
	private Button btnRemind;

	public RemindDialog(Style CSS, Strings STRINGS) {
		setText(STRINGS.remind());
		setAnimationEnabled(true);
		setAutoHideEnabled(true);
		setGlassEnabled(true);
		setModal(true);
		setWidget(layout);
		layout.addStyleName(CSS.atLoginRemindDialogLayout());

		addDomHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == 13)
					btnRemind.click();
			}
		}, KeyDownEvent.getType());

		edLogin.getElement().setAttribute("placeholder", STRINGS.email());
		layout.add(edLogin);

		btnRemind = new Button(STRINGS.doRemind());
		layout.add(btnRemind);

		btnRemind.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if ((edLogin.getValue() != null) && !edLogin.getValue().isEmpty()) {
					Waiter.start();
					RPC.getTS().restorePassword(edLogin.getValue(), new AsyncCallback<Void>() {
						@Override
						public void onSuccess(Void result) {
							Waiter.stop();
							Window.alert(STRINGS.remindEmail());
							hide();
						}

						@Override
						public void onFailure(Throwable caught) {
							Waiter.stop();
							Window.alert(STRINGS.remindFail());
						}
					});
				} else
					Window.alert(STRINGS.email() + STRINGS.notEmpty());
			}
		});
	}

}