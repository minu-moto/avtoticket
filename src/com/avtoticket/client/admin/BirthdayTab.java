/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.admin;

import com.avtoticket.client.ui.RichTextToolbar;
import com.avtoticket.client.ui.TabItem;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.SessionUtil;
import com.avtoticket.client.utils.Waiter;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 13 февр. 2016 г. 21:55:25
 */
public class BirthdayTab extends FlowPanel implements TabItem {

	private RichTextArea textArea = new RichTextArea();
	private TextBox edCaption = new TextBox();
	private AdminPlace.Style CSS;

	private AsyncCallback<Void> callback = new AsyncCallback<Void>() {
		@Override
		public void onFailure(Throwable caught) {
			Waiter.stop();
		}

		@Override
		public void onSuccess(Void result) {
			Waiter.stop();
		}
	};

	public BirthdayTab(AdminPlace.Style css) {
		CSS = css;
		edCaption.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				Waiter.start();
				RPC.getTS().setProp("birthday_theme", event.getValue(), callback);
			}
		});
		textArea.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				Waiter.start();
				RPC.getTS().setProp("birthday_mail", textArea.getHTML(), callback);
			}
		});
		build();
		edCaption.setValue(SessionUtil.getBirthdayTheme());
		textArea.setHTML(SessionUtil.getBirthdayMail());
	}

	private void build() {
		Label lbCaption = new Label("Тема письма");

		lbCaption.addStyleName(CSS.atAdminBirthdayCaptionLbl());
		add(lbCaption);
		edCaption.addStyleName(CSS.atAdminBirthdayCaption());
		add(edCaption);

		FlowPanel richPanel = new FlowPanel();
		RichTextToolbar toolbar = new RichTextToolbar(textArea);
		richPanel.addStyleName(CSS.atAdminBirthdayRichEdit());
		textArea.addStyleName(CSS.atAdminBirthdayRichArea());
		toolbar.addStyleName(CSS.atAdminNewsRichToolbar());
		richPanel.add(toolbar);
		richPanel.add(textArea);
		add(richPanel);
	}

	@Override
	public String getCaption() {
		return "Уведомления";
	}

	@Override
	public void refresh() { }

	@Override
	public void onActivate() { }

	@Override
	public void onDeactivate() { }

}