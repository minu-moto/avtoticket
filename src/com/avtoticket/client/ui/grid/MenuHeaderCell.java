/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid;

import com.avtoticket.client.ui.ImageBtn;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.shared.models.BaseModel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.client.DOM;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 21.10.2013 15:44:28
 */
public class MenuHeaderCell<C extends BaseModel> extends AbstractCell<String> {

    interface Template extends SafeHtmlTemplates {

        @Template("<div class=\"{0}\">{1}</div>")
        SafeHtml header(String className, SafeHtml content);

        @Template("<div class=\"{0}\" title=\"{2}\">{1}</div>")
        SafeHtml header(String className, SafeHtml content, String title);

        @Template("<div class=\"{0}\">{1}</div>")
        SafeHtml caption(String className, SafeHtml caption);

        @Template("<div class=\"{0}\"></div>")
        SafeHtml gradient(String className);

        @Template("<img id=\"{0}\" class=\"{1}\"></img>")
        SafeHtml button(String id, String className);

    }

	public static interface Style extends CssResource {
		String CSS_PATH = "header.css";

		String atHeaderMenuCell();

		String atHeaderGradient();

		String atFilterEditor();

		String atFilteredHeader();

		String atHeaderColumnCaption();

		String atHeaderSortBtn();

		String atHeaderFilterBtn();
	}

	public static interface Resources extends ClientBundle {
		ImageResource sortAsc();

		ImageResource sortDesc();

		ImageResource search();

		@Source(Style.CSS_PATH)
	    Style headerStyle();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);
	public static final Style CSS = RESOURCES.headerStyle();

    private static Template template = GWT.create(Template.class);

    private ColumnMenu<C> columnMenu;
    private final String filterElementId;
    private final String sortElementId;
	private final Field<?, C> info;

    private ImageBtn filter;
    private ImageBtn sort;

    public MenuHeaderCell(Field<?, C> info) {
        super(BrowserEvents.CLICK);

        CSS.ensureInjected();
		this.info = info;
        filterElementId = DOM.createUniqueId();
        sortElementId = DOM.createUniqueId();
    }

    private ColumnMenu<C> getColumnMenu() {
    	if (columnMenu == null)
    		columnMenu = new ColumnMenu<C>(info);
        return columnMenu;
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
		ColumnSortList sortList = info.getGrid().getColumnSortList();
		ColumnSortInfo sortedInfo = (sortList.size() == 0) ? null : sortList.get(0);
		Column<?, ?> sortedColumn = (sortedInfo == null) ? null : sortedInfo.getColumn();
		final boolean isSortAscending = (sortedInfo == null) ? false : sortedInfo.isAscending();
		final boolean isSorted = info.isSortable() && (info.getColumn() == sortedColumn);

        if (value != null) {
            SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();

            if (!value.isEmpty()) {
                SafeHtml caption = template.caption(CSS.atHeaderColumnCaption(), SafeHtmlUtils.fromTrustedString(value));
                cellBuilder.append(caption);
            }

            if (info.hasFilter())
                cellBuilder.append(template.button(filterElementId, ""));

            if (isSorted)
                cellBuilder.append(template.button(sortElementId, ""));

            cellBuilder.append(template.gradient(CSS.atHeaderGradient()));

            SafeHtml content = cellBuilder.toSafeHtml();
            if ((info.getHint() != null) && !info.getHint().isEmpty())
            	sb.append(template.header(CSS.atHeaderMenuCell(), content, info.getHint()));
            else
            	sb.append(template.header(CSS.atHeaderMenuCell(), content));

            Scheduler.get().scheduleFinally(new ScheduledCommand() {
				@Override
				public void execute() {
					String capt = info.isSortable() ? "Сортировка" : "";
					if (info.isSortable() && info.hasFilter())
						capt += "/";
					if (info.hasFilter())
						capt += "Фильтр";
					if (isSorted) {
						ImageResource icon = isSortAscending ? RESOURCES.sortAsc() : RESOURCES.sortDesc();
						sort = ImageBtn.wrap(Document.get().getElementById(sortElementId), icon, capt, new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								getColumnMenu().popup(sort);
								event.preventDefault();
								event.stopPropagation();
							}
						});
						if (sort != null) {
							sort.removeStyleName(ImageBtn.CSS.atImgBtn());
							sort.addStyleName(CSS.atHeaderSortBtn());
						}
					} else
						sort = null;

					if (info.hasFilter()) {
						filter = ImageBtn.wrap(Document.get().getElementById(filterElementId), RESOURCES.search(), capt, new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								getColumnMenu().popup(filter);
								event.preventDefault();
								event.stopPropagation();
							}
						});
						if (filter != null) {
							filter.removeStyleName(ImageBtn.CSS.atImgBtn());
							filter.addStyleName(CSS.atHeaderFilterBtn());
						}
					} else
						filter = null;
				}
			});
        }
    }

    public void clearFilter() {
    	getColumnMenu().clearFilter();
    }

}