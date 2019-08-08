/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.articles;

import com.avtoticket.client.articles.ArticlePlace.Style;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.shared.models.core.Help;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 26 янв. 2016 г. 22:17:58
 */
public class Article extends FlowPanel {

	private HTML content = new HTML();
	private Label label = new Label();

	public Article(Style CSS, Long id) {
		addStyleName(CSS.atArticlesArticle());
		label.addStyleName(CSS.atArticlesLabel());
		content.addStyleName(CSS.atArticlesContent());
		content.addStyleName(CSS.atArticlesWaiter());

		add(content);
		add(label);

		RPC.getTS().getHelp(id, new AsyncCallback<Help>() {
			@Override
			public void onSuccess(Help result) {
				content.removeStyleName(CSS.atArticlesWaiter());
				if (result != null) {
					label.setText(result.getName());
					content.setHTML(result.getText());
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				content.removeStyleName(CSS.atArticlesWaiter());
				Window.alert(caught.getMessage());
			}
		});
	}

}