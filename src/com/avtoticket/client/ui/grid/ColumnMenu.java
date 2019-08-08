/*
 * Copyright Бездна (c) 2013.
 */
package com.avtoticket.client.ui.grid;

import com.avtoticket.client.ui.ContextMenu;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.client.ui.grid.fields.FieldGenericBuilder;
import com.avtoticket.client.ui.grid.filters.FilterChangeEvent;
import com.avtoticket.shared.models.BaseModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 21.10.2013 12:57:56
 */
public class ColumnMenu<T extends BaseModel> extends ContextMenu {

	public static interface Style extends CssResource {
		String CSS_PATH = "menu.css";

		String atColumnMenu();

		String atColumnMenuFilterLabel();

		String atColumnMenuContent();

		String atColumnMenuFilterButtons();
	}

	public static interface Resources extends ClientBundle {
		ImageResource sortAsc16();

		ImageResource sortDesc16();

		@Source(Style.CSS_PATH)
	    Style menuStyle();
	}

    interface Strings extends Messages {
        @DefaultMessage("по возрастанию")
        String sortAsc();

        @DefaultMessage("по убыванию")
        String sortDesc();

        @DefaultMessage("Применить фильтр")
        String applyFilter();

        @DefaultMessage("Сбросить фильтр")
        String clearFilter();
    }

    private static final Strings STRINGS = GWT.create(Strings.class);
	private static final Resources RESOURCES = GWT.create(Resources.class);
	private static final Style CSS = RESOURCES.menuStyle();

    private HorizontalPanel hp;

    private final Command sortDescCommand = new Command() {
        @Override
        public void execute() {
        	GridUtil.sortColumn(info.getGrid(), info.getColumn(), false);
            ColumnMenu.this.hide();
        }
    };

    private final Command sortAscCommand = new Command() {
        @Override
        public void execute() {
        	GridUtil.sortColumn(info.getGrid(), info.getColumn(), true);
            ColumnMenu.this.hide();
        }
    };

    private Field<?, T> info;
    private VerticalPanel verticalPanel;
    private Button btnDelFilter;
    private Button btnApplyFilter;
    private FilterEditor filterPanel;

    private FilterEditor getFilterPanel(Field<?, T> info) {
    	if (filterPanel == null)
    		filterPanel = new FilterEditor(info, this) {
    			@Override
    			protected void onEnterKeyDown() {
    				btnApplyFilter.click();
    			}

				@Override
				protected void onEscKeyDown() {
					ColumnMenu.this.hide();
				}

    			@Override
    			protected void onError(String message) {
    				if (message != null)
    					Window.alert(message);
    			}
    		};
    	return filterPanel;
    }

	public ColumnMenu(final Field<?, T> info) {
		super();
		CSS.ensureInjected();
		addStyleName(CSS.atColumnMenu());
		setAnimationEnabled(true);
		setAutoOpen(true);
		setModal(true);
		this.info = info;

		if (info.isSortable()) {
			Label fl = new Label("Сортировка");
        	fl.addStyleName(CSS.atColumnMenuFilterLabel());
        	verticalPanel.add(fl);
			addItem(new SafeHtmlBuilder().append(AbstractImagePrototype.create(RESOURCES.sortAsc16()).getSafeHtml())
					.append(SafeHtmlUtils.fromString(STRINGS.sortAsc())).toSafeHtml(), sortAscCommand);
			addItem(new SafeHtmlBuilder().append(AbstractImagePrototype.create(RESOURCES.sortDesc16()).getSafeHtml())
					.append(SafeHtmlUtils.fromString(STRINGS.sortDesc())).toSafeHtml(), sortDescCommand);
		}
        getPopupContent();
        verticalPanel.add(this);
        if (info.hasFilter()) {
        	if (info.isSortable())
        		addSeparator();
        	Label fl = new Label("Фильтр");
        	fl.addStyleName(CSS.atColumnMenuFilterLabel());
        	verticalPanel.add(fl);

        	verticalPanel.add(getFilterPanel(info));

            btnApplyFilter = new Button(STRINGS.applyFilter(), new ClickHandler() {
				@SuppressWarnings("unchecked")
				@Override
				public void onClick(ClickEvent event) {
					if ((filterPanel != null) && filterPanel.flush()) {
						((FieldGenericBuilder<?, ?, T>) info).filter(filterPanel.getFilter());
						FilterChangeEvent.fire(info.getGrid());
						ColumnMenu.this.hide();
					}
				}
			});

            hp = new HorizontalPanel();
            btnDelFilter = new Button(STRINGS.clearFilter(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
		        	clearFilter();
				}
			});
            hp.addStyleName(CSS.atColumnMenuFilterButtons());
            hp.add(btnApplyFilter);
            hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            hp.add(btnDelFilter);

            verticalPanel.add(hp);
        }
	}

	@Override
	protected Widget getPopupContent() {
		if (verticalPanel == null) {
			verticalPanel = new VerticalPanel();
			verticalPanel.addStyleName(CSS.atColumnMenuContent());
		}
		return verticalPanel;
	}

	@Override
	public void popup(UIObject parent) {
		if (btnDelFilter != null)
			btnDelFilter.setEnabled(info.getFilter() != null);
		super.popup(parent);
	}

	@SuppressWarnings("unchecked")
	public void clearFilter() {
		((FieldGenericBuilder<?, ?, T>) info).filter(null);
    	if (filterPanel != null)
    		verticalPanel.remove(filterPanel);
    	filterPanel = null;
    	int idx = verticalPanel.getWidgetIndex(hp);
    	if (idx >= 0)
    		verticalPanel.insert(getFilterPanel(info), idx);
    	FilterChangeEvent.fire(info.getGrid());
        hide();
	}

}