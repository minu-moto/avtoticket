/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.news;

import com.avtoticket.client.news.NewsPlace.Strings;
import com.avtoticket.client.news.NewsPlace.Style;
import com.avtoticket.shared.models.core.News;
import com.avtoticket.shared.utils.DateUtil;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 23 дек. 2015 г. 22:14:25
 */
public class NewsCell extends AbstractCell<News> {

	private static final String URL = Window.Location.getProtocol() + "://" + Window.Location.getHost() + "/";
//	private static final String URL = "http://avtoticket.com/";

	public interface Templates extends SafeHtmlTemplates {
		@Template("<div class=\"{0}\" style=\"background-image: url('{1}');\"></div>")
		SafeHtml image(String imgClass, String imgSrc);		// image to attract attention

		@Template("<div class=\"{0}\">{1}</div>")
		SafeHtml title(String titleClass, String text);

		@Template("<div class=\"{0}\">{1}</div>")
		SafeHtml content(String contClass, SafeHtml text);

		@Template("<div class=\"{0}\">{1}{2}</div>")
		SafeHtml container(String contClass, SafeHtml title, SafeHtml content);

		@Template("<div class=\"{0}\"><table style='width: 100%; height: 100%; border-collapse: collapse;'>"
				+ "<colgroup><col width='34px'/><col width='34px'/><col width='34px'/></colgroup><tr>"
				+ "<td>{1}</td>"
				+ "<td>{2}</td>"
				+ "<td>{3}</td>"
				+ "<td style='text-align: right; font-size: 14px;'>{4}</td></tr></table></div>")
		SafeHtml footer(String footerClass, SafeHtml vkShare, SafeHtml fbShare, SafeHtml okShare, String date);
	}

	private static Templates templates = GWT.create(Templates.class);

	private final Style CSS;
	private final Strings STRINGS;

	public NewsCell(Style CSS, Strings STRINGS) {
		this.CSS = CSS;
		this.STRINGS = STRINGS;
	}

	@Override
	public void render(Context context, News value, SafeHtmlBuilder sb) {
		SafeHtml vkShare = new SafeHtmlBuilder().appendHtmlConstant("<a class='").appendEscaped(CSS.atNewsVK())
				.appendHtmlConstant("' onclick='window.open(\"https://vk.com/share.php?url=")
				.append(SafeHtmlUtils.fromString(URL)).appendHtmlConstant("news.jsp?id=").append(value.getId())
				.appendHtmlConstant("\", \"vk_share\", \"left=20,top=20,width=555,height=400,toolbar=1,resizable=0\"); return false;' href='javascript:;'></a>").toSafeHtml();
		SafeHtml fbShare = new SafeHtmlBuilder().appendHtmlConstant("<a class='").appendEscaped(CSS.atNewsFB())
				.appendHtmlConstant("' onclick='window.open(\"https://www.facebook.com/dialog/share?app_id=954289294630389&display=popup&href=")
				.append(SafeHtmlUtils.fromString(URL)).appendHtmlConstant("news.jsp?id=").append(value.getId()).appendHtmlConstant("&redirect_uri=")
				.append(SafeHtmlUtils.fromString(URL)).appendHtmlConstant("news.jsp\", \"fb_share\", \"left=20,top=20,width=555,height=500,toolbar=1,resizable=0\"); return false;' href='javascript:;'></a>").toSafeHtml();
		SafeHtml okShare = new SafeHtmlBuilder().appendHtmlConstant("<a class='").appendEscaped(CSS.atNewsOK())
				.appendHtmlConstant("' onclick='window.open(\"https://ok.ru/dk?st.cmd=addShare&st.s=1&st._surl=")
				.append(SafeHtmlUtils.fromString(URL)).appendHtmlConstant("news.jsp?id=").append(value.getId())
				.appendHtmlConstant("\", \"ok_share\", \"left=20,top=20,width=555,height=400,toolbar=1,resizable=0\"); return false;' href='javascript:;'></a>").toSafeHtml();

		sb.append(templates.image(CSS.atNewsImgPreview(), value.getImage()))
			.append(templates.container(CSS.atNewsFlexContainer(), templates.title(CSS.atNewsTitle(), value.getTitle()),
					templates.content(CSS.atNewsContent(), SafeHtmlUtils.fromTrustedString(value.getDescription()))))
			.append(templates.footer(CSS.atNewsFooter(), vkShare, fbShare, okShare,
					STRINGS.posted(value.getDateCreate(), (TimeZone) DateUtil.getMSKTimeZone())))
			.appendHtmlConstant("</div>");
	}

}