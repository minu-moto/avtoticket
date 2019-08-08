/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.articles;

import com.avtoticket.client.tiles.TilePlace;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 26 янв. 2016 г. 21:41:17
 */
public class ArticlePlace extends TilePlace {

	public static final String NAME = "article";

	public interface Style extends CssResource {
		String CSS_PATH = "articles.css";

		String atArticlesPanel();

		String atArticlesWaiter();

		String atArticlesArticle();

		String atArticlesLabel();

		String atArticlesContent();
	}

	public interface Resources extends ClientBundle {

		ImageResource waiter();

		@Source(Style.CSS_PATH)
	    Style articlesStyle();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	private static final Style CSS = RESOURCES.articlesStyle();

	public static final ArticlePanel ARTICLE_PANEL = new ArticlePanel(CSS);

	private final Activity articleActivity = new AbstractActivity() {
		@Override
		public void start(AcceptsOneWidget panel, EventBus eventBus) {
			panel.setWidget(new Article(CSS, articleId));
		}
	};

	private Long articleId;

	public static ArticlePlace getInstance(String token) {
		String[] params = token.split(PARAM_SEPARATOR);
		Long articleId = null;
		if (params.length > 1)
			try {
				articleId = Long.valueOf(params[1]);
			} catch (Exception ignored) {
			}
		return new ArticlePlace(articleId);
	}

	public ArticlePlace(Long articleId) {
		this.articleId = articleId;
	}

	@Override
	public Activity getActivity(Object param) {
		return articleActivity;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder(NAME);
		if (articleId != null)
			ret = ret.append(PARAM_SEPARATOR).append(articleId);
		return ret.toString();
	}

}