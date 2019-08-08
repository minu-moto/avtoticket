/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.routes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.avtoticket.client.BodyPanel;
import com.avtoticket.client.order.OrderPlace;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.shared.models.core.Passage;
import com.avtoticket.shared.utils.DateUtil;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowHoverEvent;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 21 янв. 2016 г. 23:34:44
 */
public class RoutesGrid extends CellTable<Passage> {

//	private static final Logger logger = Logger.getLogger(PassagesGrid.class.getName());

	public static interface Style extends CellTable.Style {
		String CSS_PATH = "routes.css";

		String atRoutesInactive();
	}

	public static interface Resources extends CellTable.Resources {

		ImageResource waiter();

		@Override
	    @Source(Style.CSS_PATH)
	    Style cellTableStyle();
	}

	private static final Resources RESOURCES = GWT.create(Resources.class);

	private Long depId;
	private Long destId;
	private Date date;

	private AsyncDataProvider<Passage> dataProvider = new AsyncDataProvider<Passage>() {
		@Override
		protected void onRangeChanged(HasData<Passage> display) {
			RPC.getTS().getPassages(depId, date, destId, new AsyncCallback<List<Passage>>() {
				@Override
				public void onSuccess(List<Passage> result) {
					ssm.clear();
					setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);	// очищаем выделение
					updateRowCount(result.size(), true);
					updateRowData(0, result);
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
						}
					});
				}

				@Override
				public void onFailure(Throwable caught) {
					updateRowCount(0, true);
					updateRowData(0, new ArrayList<Passage>());
					Window.alert(caught.getMessage());
				}
			});
		}
	};
	private SingleSelectionModel<Passage> ssm = new SingleSelectionModel<Passage>();

	public RoutesGrid() {
		super(9999, RESOURCES, null, new FlowPanel());

		setEmptyTableWidget(new Label("нет данных для отображения"));
		setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
		setRowStyles(new RowStyles<Passage>() {
			@Override
			public String getStyleNames(Passage row, int rowIndex) {
				return (row.isSaleAvailable() == Boolean.TRUE) ? "" : RESOURCES.cellTableStyle().atRoutesInactive();
			}
		});
		addCellPreviewHandler(new CellPreviewEvent.Handler<Passage>() {
			@Override
			public void onCellPreview(CellPreviewEvent<Passage> event) {
				NativeEvent ne = event.getNativeEvent();
				if (BrowserEvents.CLICK.equals(ne.getType())
						|| (BrowserEvents.KEYDOWN.equals(ne.getType()) && (ne.getKeyCode() == KeyCodes.KEY_ENTER)))
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							Passage selected = ssm.getSelectedObject();
							if ((selected != null) && (selected.isSaleAvailable() == Boolean.TRUE))
								BodyPanel.goTo(new OrderPlace(selected.getDepId(), selected.getDestId(), DateUtil.formatDate(event.getValue().getDeparture(), "yyyyMMddHHmm"), selected.getId()));
						}
					});
			}
		});
		addRowHoverHandler(new RowHoverEvent.Handler() {
			@Override
			public void onRowHover(RowHoverEvent event) {
				if (!event.isUnHover())
					setKeyboardSelectedRow(event.getHoveringRow().getRowIndex() - 1);
			}
		});
		setSelectionModel(ssm);

		DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd MMM HH:mm");
		NumberFormat numberFormat = NumberFormat.getFormat("0");

		Column<Passage, Date> depCol = new Column<Passage, Date>(new DateCell(dateFormat, DateUtil.getMSKTimeZone())) {
			@Override
			public Date getValue(Passage object) {
				return object.getDeparture();
			}
		};
		addColumn(depCol, "Отправление");
		setColumnWidth(depCol, "160px");

		Column<Passage, Date> arrCol = new Column<Passage, Date>(new DateCell(dateFormat, DateUtil.getMSKTimeZone())) {
			@Override
			public Date getValue(Passage object) {
				return object.getArrival();
			}
		};
		addColumn(arrCol, "Прибытие");
		setColumnWidth(arrCol, "160px");

		Column<Passage, Number> numCol = new Column<Passage, Number>(new NumberCell(numberFormat)) {
			@Override
			public Number getValue(Passage object) {
				return object.getId();
			}
		};
		addColumn(numCol, "№ рейса");
		setColumnWidth(numCol, "140px");

		Column<Passage, String> nameCol = new Column<Passage, String>(new TextCell()) {
			@Override
			public String getValue(Passage object) {
				return object.getName();
			}
		};
		addColumn(nameCol, "Направление");
		setColumnWidth(nameCol, "100%");

		Column<Passage, Number> seatsCol = new Column<Passage, Number>(new NumberCell(numberFormat)) {
			@Override
			public Number getValue(Passage object) {
				return object.getFreeSeats();
			}
		};
		addColumn(seatsCol, "Своб.мест");
		setColumnWidth(seatsCol, "180px");

		Column<Passage, Number> priceCol = new Column<Passage, Number>(new NumberCell(NumberFormat.getFormat("#,##0.00 р"))) {
			@Override
			public Number getValue(Passage object) {
				return object.getSumm() / 100.0;
			}
		};
		addColumn(priceCol, "Цена");
		setColumnWidth(priceCol, "120px");

		Column<Passage, String> buyCol = new Column<Passage, String>(new TextCell()) {
			@Override
			public String getValue(Passage object) {
				return "";//"Купить";
			}
		};
		addColumn(buyCol);
		setColumnWidth(buyCol, "120px");
	}

	public void refresh(Long depId, Long destId, Date date) {
		this.depId = depId;
		this.destId = destId;
		this.date = date;
		if (dataProvider.getDataDisplays().contains(this))
			setVisibleRangeAndClearData(getVisibleRange(), true);
		else
			dataProvider.addDataDisplay(this);
	}

}