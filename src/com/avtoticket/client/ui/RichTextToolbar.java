/*
 * Copyright Бездна (c) 2014.
 */
package com.avtoticket.client.ui;

import com.avtoticket.client.ui.grid.ModelEditorDialog;
import com.avtoticket.client.ui.grid.cells.LinkCell;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.client.ui.rich.RichTextImages;
import com.avtoticket.client.utils.DefaultCallback;
import com.avtoticket.shared.models.BaseModel;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.RichTextArea.FontSize;
import com.google.gwt.user.client.ui.RichTextArea.Formatter;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 16.04.2014 17:48:08
 */
public class RichTextToolbar extends Composite {

	public interface Strings extends Constants {

		String background();

		String black();

		String blue();

		String bold();

		String color();

		String createLink();

		String font();

		String foreground();

		String green();

		String hr();

		String indent();

		String insertImage();

		String italic();

		String justifyCenter();

		String justifyLeft();

		String justifyRight();

		String large();

		String medium();

		String normal();

		String ol();

		String outdent();

		String red();

		String removeFormat();

		String removeLink();

		String size();

		String small();

		String strikeThrough();

		String subscript();

		String superscript();

		String ul();

		String underline();

		String white();

		String xlarge();

		String xsmall();

		String xxlarge();

		String xxsmall();

		String yellow();

		String link();

		String imgLink();

	}

	public static interface Style extends CssResource {
		String CSS_PATH = "rich.css";

		String atImgToggleBtn();

		String atRichToolbarBtnPanel();
	}

	public static interface Resources extends RichTextImages {
		@Source(Style.CSS_PATH)
		@Import(ImageBtn.Style.class)
	    Style richStyle();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	public static final Style CSS = RESOURCES.richStyle();

	private class EventHandler implements ClickHandler, KeyUpHandler {
		private ModelEditorDialog<BaseModel> editorDialog;

		@edu.umd.cs.findbugs.annotations.SuppressWarnings(
			    value = "DM_CONVERT_CASE", 
			    justification = "No GWT emulation of String#toLowerCase(Locale)")
		@Override
		public void onClick(ClickEvent event) {
			Widget sender = (Widget) event.getSource();

			if (sender == bold)
				formatter.toggleBold();
			else if (sender == italic)
				formatter.toggleItalic();
			else if (sender == underline)
				formatter.toggleUnderline();
			else if (sender == subscript)
				formatter.toggleSubscript();
			else if (sender == superscript)
				formatter.toggleSuperscript();
			else if (sender == strikethrough)
				formatter.toggleStrikethrough();
			else if (sender == indent)
				formatter.rightIndent();
			else if (sender == outdent)
				formatter.leftIndent();
			else if (sender == justifyLeft)
				formatter.setJustification(RichTextArea.Justification.LEFT);
			else if (sender == justifyCenter)
				formatter.setJustification(RichTextArea.Justification.CENTER);
			else if (sender == justifyRight)
				formatter.setJustification(RichTextArea.Justification.RIGHT);
			else if (sender == createLink) {
				ModelEditor<BaseModel> editor = new ModelEditor<BaseModel>() {
					@Override
					protected void onEnterKeyDown() {
						editorDialog.onOkClick();
					}

					@Override
					protected void onEscKeyDown() {
						editorDialog.hide();
					}

					@Override
					protected void onError(String message) {
						if (message == null)
							editorDialog.clearState();
						else
							editorDialog.errorMessage(message);
					}
				};
				editor.addField(Field.asTextTo(null).modelKey(BaseModel.DISPLAY_FIELD).caption("Текст").editable().showInEditor());
				editor.addField(Field.asTextTo(null).modelKey(BaseModel.VALUE_FIELD).caption("Ссылка").editable().showInEditor());
				editorDialog = new ModelEditorDialog<BaseModel>(editor);
				editorDialog.setGlassEnabled(false);
				BaseModel model = new BaseModel();
				model.setValueField("http://");
				editorDialog.edit(true, model, new DefaultCallback<BaseModel>() {
					@Override
					public void onSuccess(BaseModel ret) {
						editorDialog.hide();
						String url = (String) ret.getValueField();
						String urlLower = url.toLowerCase();
						url = (urlLower.startsWith("http://") || urlLower.startsWith("https://") || urlLower.startsWith("ftp://")) ? url : "http://" + url;
						if ((ret.getDisplayField() == null) || ret.getDisplayField().isEmpty())
							ret.setDisplayField(url);
						formatter.insertHTML(LinkCell.template.link(UriUtils.fromString(url), ret.getDisplayField()).asString());
					}
				});
				editorDialog.setText("Создание ссылки");
			} else if (sender == removeLink)
				formatter.removeLink();
			else if (sender == hr)
				formatter.insertHorizontalRule();
			else if (sender == ol)
				formatter.insertOrderedList();
			else if (sender == ul)
				formatter.insertUnorderedList();
			else if (sender == removeFormat)
				formatter.removeFormat();
			else if (sender == richText)
				updateStatus();
		}

		@Override
		public void onKeyUp(KeyUpEvent event) {
			Widget sender = (Widget) event.getSource();
			if (sender == richText)
				updateStatus();
		}
	}

	interface FontTemplates extends SafeHtmlTemplates {
		@Template("<font size='{0}'>{1}</font>")
		SafeHtml size(int size, String text);

		@Template("<font face='{0}'>{1}</font>")
		SafeHtml face(String face, String text);

		@Template("<div style='{0} width: 12px; height: 12px; float: left; border: 1px solid #CCCCCC; margin-right: 4px;'></div>{1}")
		SafeHtml color(SafeStyles color, String text);
	}

	private static final FontSize[] fontSizesConstants = new FontSize[] {
			FontSize.XX_SMALL, FontSize.X_SMALL, FontSize.SMALL, FontSize.MEDIUM, FontSize.LARGE, FontSize.X_LARGE, FontSize.XX_LARGE};
	private static final String[] FONTS = new String[] {"Times New Roman", "Arial", "Courier New", "Georgia", "Trebuchet", "Verdana"};
	private static final String[] COLORS = new String[] {"white", "black", "red", "green", "yellow", "blue"};

	private RichTextImages images = GWT.create(RichTextImages.class);
	private Strings strings = GWT.create(Strings.class);
	private FontTemplates fontTemplates = GWT.create(FontTemplates.class);
	private EventHandler handler = new EventHandler();

	private RichTextArea richText;
	private Formatter formatter;

	private FlowPanel layout = new FlowPanel();
	private ToggleButton bold;
	private ToggleButton italic;
	private ToggleButton underline;
	private ToggleButton strikethrough;
	private ToggleButton subscript;
	private ToggleButton superscript;
	private ImageBtn fonts;
	private ImageBtn sizes;
	private ImageBtn foreColors;
	private ImageBtn backColors;
	private ImageBtn indent;
	private ImageBtn outdent;
	private ImageBtn justifyLeft;
	private ImageBtn justifyCenter;
	private ImageBtn justifyRight;
	private ImageBtn hr;
	private ImageBtn ol;
	private ImageBtn ul;
	private ImageBtn createLink;
	private ImageBtn removeLink;
	private ImageBtn removeFormat;

	public RichTextToolbar(RichTextArea richText) {
		CSS.ensureInjected();
		this.richText = richText;
		formatter = richText.getFormatter();

		initWidget(layout);
		richText.addStyleName("hasRichTextToolbar");

		if (formatter != null) {
			FlowPanel bp = createButtonPanel();
			bp.add(bold = createToggleButton(images.bold(), strings.bold()));
			bp.add(italic = createToggleButton(images.italic(), strings.italic()));
			bp.add(underline = createToggleButton(images.underline(), strings.underline()));
			bp.add(strikethrough = createToggleButton(images.strikeThrough(), strings.strikeThrough()));
			layout.add(bp);

			bp = createButtonPanel();
			final ContextMenu fontMenu = createFonts();
			bp.add(fonts = new ImageBtn(images.fonts(), strings.font(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					fontMenu.popup(fonts);
				}
			}));
			final ContextMenu sizeMenu = createFontSizes();
			bp.add(sizes = new ImageBtn(images.fontSizes(), strings.size(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					sizeMenu.popup(sizes);
				}
			}));
			layout.add(bp);

			bp = createButtonPanel();
			final ContextMenu colorMenu = createFontColors(false);
			bp.add(foreColors = new ImageBtn(images.foreColors(), strings.foreground(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					colorMenu.popup(foreColors);
				}
			}));
			final ContextMenu backColorMenu = createFontColors(true);
			bp.add(backColors = new ImageBtn(images.backColors(), strings.background(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					backColorMenu.popup(backColors);
				}
			}));
			layout.add(bp);

			bp = createButtonPanel();
			bp.add(subscript = createToggleButton(images.subscript(), strings.subscript()));
			bp.add(superscript = createToggleButton(images.superscript(), strings.superscript()));
			layout.add(bp);

			bp = createButtonPanel();
			bp.add(removeFormat = new ImageBtn(images.removeFormat(), strings.removeFormat(), handler));
			layout.add(bp);

			bp = createButtonPanel();
			bp.add(justifyLeft = new ImageBtn(images.justifyLeft(), strings.justifyLeft(), handler));
			bp.add(justifyCenter = new ImageBtn(images.justifyCenter(), strings.justifyCenter(), handler));
			bp.add(justifyRight = new ImageBtn(images.justifyRight(), strings.justifyRight(), handler));
			layout.add(bp);

			bp = createButtonPanel();
			bp.add(indent = new ImageBtn(images.indent(), strings.indent(), handler));
			bp.add(outdent = new ImageBtn(images.outdent(), strings.outdent(), handler));
			layout.add(bp);

			bp = createButtonPanel();
			bp.add(hr = new ImageBtn(images.hr(), strings.hr(), handler));
			layout.add(bp);

			bp = createButtonPanel();
			bp.add(ol = new ImageBtn(images.ol(), strings.ol(), handler));
			bp.add(ul = new ImageBtn(images.ul(), strings.ul(), handler));
			layout.add(bp);

			bp = createButtonPanel();
			bp.add(createLink = new ImageBtn(images.createLink(), strings.createLink(), handler));
			bp.add(removeLink = new ImageBtn(images.removeLink(), strings.removeLink(), handler));
			layout.add(bp);

			richText.addKeyUpHandler(handler);
			richText.addClickHandler(handler);
		}
	}

	private ContextMenu createFontColors(final boolean back) {
		final ContextMenu ret = new ContextMenu();
		String[] colors = new String[] {strings.white(), strings.black(), strings.red(), strings.green(), strings.yellow(), strings.blue()};
		for (int i = 0; i < COLORS.length; i++) {
			final String color = COLORS[i];
			ret.addItem(fontTemplates.color(SafeStylesUtils.forTrustedBackgroundColor(color), colors[i]), new Command() {
				@Override
				public void execute() {
					ret.hide();
					if (back)
						formatter.setBackColor(color);
					else
						formatter.setForeColor(color);
				}
			});
		}
		return ret;
	}

	private ContextMenu createFonts() {
		final ContextMenu ret = new ContextMenu();
		ret.addItem(strings.normal(), new Command() {
			@Override
			public void execute() {
				ret.hide();
				formatter.setFontName("");
			}
		});
		for (final String font : FONTS)
			ret.addItem(fontTemplates.face(font, font), new Command() {
				@Override
				public void execute() {
					ret.hide();
					formatter.setFontName(font);
				}
			});
		return ret;
	}

	private ContextMenu createFontSizes() {
		final ContextMenu ret = new ContextMenu();
		String[] sizes = new String[] {strings.xxsmall(), strings.xsmall(), strings.small(), strings.medium(), strings.large(), strings.xlarge(), strings.xxlarge()};
		for (int i = 0; i < sizes.length; i++) {
			final FontSize fsize = fontSizesConstants[i];
			ret.addItem(fontTemplates.size(fsize.getNumber(), sizes[i]), new Command() {
				@Override
				public void execute() {
					ret.hide();
					formatter.setFontSize(fsize);
				}
			});
		}
		return ret;
	}

	private ToggleButton createToggleButton(ImageResource img, String tip) {
		ToggleButton tb = new ToggleButton(new ImageBtn(img, tip));
		tb.addClickHandler(handler);
		tb.setStyleName(CSS.atImgToggleBtn());
		return tb;
	}

	private FlowPanel createButtonPanel() {
		FlowPanel ret = new FlowPanel();
		ret.addStyleName(CSS.atRichToolbarBtnPanel());
		return ret;
	}

	private void updateStatus() {
		if (formatter != null) {
			bold.setDown(formatter.isBold());
			italic.setDown(formatter.isItalic());
			underline.setDown(formatter.isUnderlined());
			subscript.setDown(formatter.isSubscript());
			superscript.setDown(formatter.isSuperscript());
			strikethrough.setDown(formatter.isStrikethrough());
		}
	}

	public void setEnabled(boolean enabled) {
		if (bold != null)
			bold.setEnabled(enabled);
		if (italic != null)
			italic.setEnabled(enabled);
		if (underline != null)
			underline.setEnabled(enabled);
		if (subscript != null)
			subscript.setEnabled(enabled);
		if (superscript != null)
			superscript.setEnabled(enabled);
		if (strikethrough != null)
			strikethrough.setEnabled(enabled);
		if (indent != null)
			indent.setEnabled(enabled);
		if (outdent != null)
			outdent.setEnabled(enabled);
		if (justifyLeft != null)
			justifyLeft.setEnabled(enabled);
		if (justifyCenter != null)
			justifyCenter.setEnabled(enabled);
		if (justifyRight != null)
			justifyRight.setEnabled(enabled);
		if (hr != null)
			hr.setEnabled(enabled);
		if (ol != null)
			ol.setEnabled(enabled);
		if (ul != null)
			ul.setEnabled(enabled);
		if (createLink != null)
			createLink.setEnabled(enabled);
		if (removeLink != null)
			removeLink.setEnabled(enabled);
		if (removeFormat != null)
			removeFormat.setEnabled(enabled);
		if (backColors != null)
			backColors.setEnabled(enabled);
		if (foreColors != null)
			foreColors.setEnabled(enabled);
		if (fonts != null)
			fonts.setEnabled(enabled);
		if (sizes != null)
			sizes.setEnabled(enabled);
		if (richText != null)
			richText.setEnabled(enabled);
	}

}