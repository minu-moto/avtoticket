/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.admin;

import java.util.List;

import com.avtoticket.client.admin.AdminPlace.Style;
import com.avtoticket.client.ui.DBTable;
import com.avtoticket.client.ui.LongTextBox;
import com.avtoticket.client.ui.PagedDataProvider;
import com.avtoticket.client.ui.TabItem;
import com.avtoticket.client.ui.grid.fields.Field;
import com.avtoticket.client.utils.DefaultCallback;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.SessionUtil;
import com.avtoticket.client.utils.Waiter;
import com.avtoticket.shared.models.PageContainer;
import com.avtoticket.shared.models.Where;
import com.avtoticket.shared.models.core.Formula;

import com.google.gwt.dom.client.BRElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.TextAlign;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 24 янв. 2016 г. 18:08:39
 */
public class SalePanel extends FlowPanel implements TabItem {

	private HTML lblHint = new HTML("При активированном сервисном режиме "
			+ "пользователь не сможет осуществлять покупку билетов, "
			+ "о чем будет уведомлен на дисплее");
	private Anchor btnActivate = new Anchor();

	private FlexTable layout = new FlexTable();
	private TextBox edAdultFormula = new TextBox();
	private TextBox edChildFormula = new TextBox();
	private TextBox edBagFormula = new TextBox();
	private TextBox edStavAdultFormula = new TextBox();
	private TextBox edStavChildFormula = new TextBox();
	private TextBox edStavBagFormula = new TextBox();
	private LongTextBox edSalePeriod = new LongTextBox();
	private LongTextBox edChildhood = new LongTextBox();

	private DBTable<Formula, Long> formulasGrid = new DBTable<Formula, Long>(null, new Formula()) {
		@Override
		protected void buildTableColumns() {
			addColumn(Field.asLongTo(grid).modelKey(Formula.PASSAGE_ID).caption("ID рейса").width(80).editable().require().showInEditor());
			addColumn(Field.asTextTo(grid).modelKey(Formula.ADULT_PRICE_FORMULA).caption("Цена взрослого билета").width("33%").editable().showInEditor());
			addColumn(Field.asTextTo(grid).modelKey(Formula.CHILD_PRICE_FORMULA).caption("Цена детского билета").width("33%").editable().showInEditor());
			addColumn(Field.asTextTo(grid).modelKey(Formula.BAG_PRICE_FORMULA).caption("Цена багажного билета").width("33%").editable().showInEditor());
		}

		@Override
		protected void buildDataProvider() {
			dataProvider = new PagedDataProvider<Formula, Long>(Formula.class, modelKeyProvider, grid) {
				@Override
				protected void getObjects(String className, List<Long> forDel, Where where, String sortColumn, AsyncCallback<PageContainer<Formula>> callback) {
					RPC.getTS().getPagedModels(className, (List<Long>) forDel, where, Formula.PASSAGE_ID, callback);
				}
			};
			dataProvider.addDataDisplay(grid);
		}

		@Override
		protected void buildGrid() {
			super.buildGrid();
			grid.asWidget().getElement().getStyle().setFloat(Float.NONE);
			pager.setPageSize(8);
		}
	};

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

	public SalePanel(Style CSS) {
		addStyleName(CSS.atAdminServiceMode());
		lblHint.addStyleName(CSS.atAdminServiceModeHint());
		btnActivate.addStyleName(CSS.atAdminServiceModeButton());
		btnActivate.setText(SessionUtil.isServiceMode() ? "Деактивировать" : "Активировать");
		add(lblHint);
		add(btnActivate);
		getElement().appendChild(DOM.createElement(BRElement.TAG));

		btnActivate.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RPC.getTS().setProp("service_mode", SessionUtil.isServiceMode() ? "false" : "true", (DefaultCallback<Void>) ret -> Window.Location.reload());
			}
		});

		edAdultFormula.setValue(SessionUtil.getAdultPriceFormula());
		edAdultFormula.addValueChangeHandler(event -> {
			Waiter.start();
			RPC.getTS().setProp("adult_price_formula", event.getValue(), callback);
		});

		edChildFormula.setValue(SessionUtil.getChildPriceFormula());
		edChildFormula.addValueChangeHandler(event -> {
			Waiter.start();
			RPC.getTS().setProp("child_price_formula", event.getValue(), callback);
		});

		edBagFormula.setValue(SessionUtil.getBagPriceFormula());
		edBagFormula.addValueChangeHandler(event -> {
			Waiter.start();
			RPC.getTS().setProp("bag_price_formula", event.getValue(), callback);
		});

		edStavAdultFormula.setValue(SessionUtil.getStavAdultPriceFormula());
		edStavAdultFormula.addValueChangeHandler(event -> {
			Waiter.start();
			RPC.getTS().setProp("stav_adult_price_formula", event.getValue(), callback);
		});

		edStavChildFormula.setValue(SessionUtil.getStavChildPriceFormula());
		edStavChildFormula.addValueChangeHandler(event -> {
			Waiter.start();
			RPC.getTS().setProp("stav_child_price_formula", event.getValue(), callback);
		});

		edStavBagFormula.setValue(SessionUtil.getStavBagPriceFormula());
		edStavBagFormula.addValueChangeHandler(event -> {
			Waiter.start();
			RPC.getTS().setProp("stav_bag_price_formula", event.getValue(), callback);
		});

		edSalePeriod.setValue(SessionUtil.getSalePeriod());
		edSalePeriod.addValueChangeHandler(event -> {
			Waiter.start();
			RPC.getTS().setProp("sale_period", (event.getValue() != null) ? event.getValue().toString() : "", callback);
		});

		edChildhood.setValue((long) SessionUtil.getChildhood());
		edChildhood.addValueChangeHandler(event -> {
			Waiter.start();
			RPC.getTS().setProp("childhood", (event.getValue() != null) ? event.getValue().toString() : "", callback);
		});

		formulasGrid.addStyleName(CSS.atAdminFormulasGrid());
		add(formulasGrid);

		layout.setText(0, 0, "Общие формулы");
		layout.setText(0, 1, "Краснодар");
		layout.setText(0, 2, "Ставрополь");

		layout.setText(1, 0, "Цена взрослого билета");
		layout.setText(2, 0, "Цена детского билета");
		layout.setText(3, 0, "Цена багажного билета");
		layout.setText(4, 0, "Закрыть продажу за");
		layout.setText(4, 2, "ч. до отправления");
		layout.setText(5, 0, "Детские билеты до");
		layout.setText(5, 2, "лет + 1 день");

		layout.setWidget(1, 1, edAdultFormula);
		layout.setWidget(2, 1, edChildFormula);
		layout.setWidget(3, 1, edBagFormula);
		layout.setWidget(4, 1, edSalePeriod);
		layout.setWidget(5, 1, edChildhood);

		layout.setWidget(1, 2, edStavAdultFormula);
		layout.setWidget(2, 2, edStavChildFormula);
		layout.setWidget(3, 2, edStavBagFormula);

		layout.getColumnFormatter().setWidth(1, "390px");
		layout.getColumnFormatter().setWidth(2, "390px");
		layout.getCellFormatter().getElement(4, 2).getStyle().setTextAlign(TextAlign.LEFT);
		layout.getCellFormatter().getElement(5, 2).getStyle().setTextAlign(TextAlign.LEFT);
		layout.getCellFormatter().getElement(0, 0).getStyle().setTextAlign(TextAlign.CENTER);
		layout.getCellFormatter().getElement(0, 1).getStyle().setTextAlign(TextAlign.CENTER);
		layout.getCellFormatter().getElement(0, 2).getStyle().setTextAlign(TextAlign.CENTER);
		layout.addStyleName(CSS.atAdminSalePanelLayout());
		add(layout);

		add(new Label("tarif - взрослый тариф; child_tarif - детский тариф; bag_tarif - багажный тариф; kom_sbor - комиссионный сбор; ob_strah - обязательное страхование; raft_tarif - тариф переправы на пароме; transit - наценка на транзитный рейс; "
				+ "stav_markup_tarif - плата за услугу продажи взрослого билета Ставр.; stav_markup_child_tarif - плата за услугу продажи детского билета Ставр.; stav_markup_bag_tarif - плата за услугу продажи багажа Ставр.; stav_booking_price - тариф услуги бронирования места Ставр."));
	}

	@Override
	public String getCaption() {
		return "Настройки продажи";
	}

	@Override
	public void refresh() { }

	@Override
	public void onActivate() { }

	@Override
	public void onDeactivate() { }

}