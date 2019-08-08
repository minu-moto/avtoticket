/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.ui;

import java.util.function.Consumer;
import java.util.function.Function;

import com.avtoticket.client.ui.TicketsPanel.Style;
import com.avtoticket.shared.models.core.Ticket;
import com.avtoticket.shared.utils.DateUtil;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Minu <<a href=minu-moto@mail.ru>minu-moto@mail.ru</a>>
 * @since 19 янв. 2016 г. 22:36:04
 */
public class TicketCell extends AbstractCell<Ticket> {

	public interface Templates extends SafeHtmlTemplates {
		@Template("<div class=\"{0}\">{1}</div>")
		SafeHtml header(String headerClass, String title);

		@Template("<a id=\"btnDelete\" class=\"{0}\" href=\"javascript:;\">Удалить</a>")
		SafeHtml delete(String btnClass);

		@Template("<a id=\"btnRepeat\" class=\"{0}\" href=\"{1}\">{2}</a>")
		SafeHtml repeat(String btnClass, String url, String text);

		@Template("<div class=\"{0}\"><div class=\"{1}\">{2}</div>{3}</div>")
		SafeHtml row(String rowClass, String ticketLabelClass, String ticketLabel, SafeHtml ticketRecordValue);

		@Template("<div class=\"{0}\">{1}</div>")
		SafeHtml value(String valueClass, String value);
	}

	protected static final Templates templates = GWT.create(Templates.class);

	protected final Style CSS;
	protected final DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MM.yyyy");
	protected final DateTimeFormat timeFormat = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm");
	protected final NumberFormat currencyFormat = NumberFormat.getFormat("#,##0.00 руб");
	private Consumer<Ticket> repeatClickHandler;
	private Consumer<Ticket> deleteClickHandler;
	private String repeatText = "Купить билет на этот маршрут";
	private Function<Ticket, String> repeatUrlSupplier = t -> "javascript:;";

	public TicketCell() {
		super(BrowserEvents.CLICK);
		CSS = TicketsPanel.CSS;
		CSS.ensureInjected();
	}

	protected SafeHtml getRow(String title, String... text) {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		for (String s : text)
			sb.append(templates.value(CSS.atTicketsValue(), s));
		return templates.row(CSS.atTicketsRow(), CSS.atTicketsLabel(), title, sb.toSafeHtml());
	}

	@Override
	public void render(Context context, Ticket ticket, SafeHtmlBuilder sb) {
		if (ticket == null)
			return;
		sb.append(templates.header(CSS.atTicketsHeader(), "Рейс: " + ticket.getReisName() + " " + ticket.getReisId()))
			.append(getRow("Номер билета:", ticket.getTktNumber()))
			.append(getRow("Отправление:", timeFormat.format(ticket.getDeparture(), DateUtil.getMSKTimeZone())))
			.append(getRow("Прибытие:", timeFormat.format(ticket.getArrival(), DateUtil.getMSKTimeZone())))
			.append(getRow("Пассажир:", ticket.getLastname() + " " + ticket.getFirstname(), ticket.getMiddlename()))
			.append(getRow("Дата рождения:", dateFormat.format(ticket.getBirthDate(), DateUtil.getMSKTimeZone())))
			.append(getRow("Документ:", ticket.getDocTypeName(),
					"серия " + ticket.getSeriya() + " номер " + ticket.getNumber(),
					"от " + dateFormat.format(ticket.getVdate(), DateUtil.getMSKTimeZone())))
			.append(getRow("Место:", ticket.isBaggage() ? "багажное"
					: (((ticket.getTBTarif() != null) && (ticket.getTBTarif() > 0L)) ? "б/м" : String.valueOf(ticket.getSeat()))))
			.append(getRow("Стоимость:", (ticket.getAmount() != null) ? currencyFormat.format(ticket.getAmount() / 100.0) : ""))
			.append(getRow("Оплачен:", timeFormat.format(ticket.getSellTime(), DateUtil.getMSKTimeZone())));
		String url = repeatUrlSupplier.apply(ticket);
		if ((url != null) && !url.isEmpty())
			sb.append(templates.repeat(CSS.atTicketsRepeat(), url, repeatText));	// TODO не сохраняется история браузера
		if (!ticket.isBaggage())
			sb.append(templates.delete(CSS.atTicketsDelete()));
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, Ticket value, NativeEvent event, ValueUpdater<Ticket> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		boolean isClickEvent = BrowserEvents.CLICK.equals(event.getType());

		if (isClickEvent) {
			EventTarget eventTarget = event.getEventTarget();
			if (!Element.is(eventTarget))
				return;
			String id = Element.as(eventTarget).getId();
			if ("btnRepeat".equalsIgnoreCase(id) && (repeatClickHandler != null))
				repeatClickHandler.accept(value);
			if ("btnDelete".equalsIgnoreCase(id) && (deleteClickHandler != null))
				deleteClickHandler.accept(value);
		}
	}

	public void setRepeatUrlSupplier(Function<Ticket, String> urlSupplier) {
		repeatUrlSupplier = urlSupplier;
	}

	public void setRepeatText(String text) {
		repeatText = text;
	}

	public void setRepeatClickHandler(Consumer<Ticket> handler) {
		repeatClickHandler = handler;
	}

	public void setDeleteClickHandler(Consumer<Ticket> handler) {
		deleteClickHandler = handler;
	}

}