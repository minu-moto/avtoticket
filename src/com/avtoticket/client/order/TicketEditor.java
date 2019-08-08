/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.order;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.avtoticket.client.order.OrderPlace.Style;
import com.avtoticket.client.ui.DateTextBox;
import com.avtoticket.client.ui.EnumListBox;
import com.avtoticket.client.ui.ModelListBox;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.SessionUtil;
import com.avtoticket.shared.models.core.DocType;
import com.avtoticket.shared.models.core.Gender;
import com.avtoticket.shared.models.core.Nationality;
import com.avtoticket.shared.models.core.Requisite;
import com.avtoticket.shared.models.core.User;
import com.avtoticket.shared.utils.DateUtil;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 24 янв. 2016 г. 20:42:43
 */
public abstract class TicketEditor extends AbsolutePanel {

	private static final int CHILDHOOD = SessionUtil.getChildhood();
	private TextBox edLastName = new TextBox();
	private TextBox edFirstName = new TextBox();
	private TextBox edMiddleName = new TextBox();
	private TextBox edPhone = new TextBox();
	private EnumListBox<Gender> lbGender = new EnumListBox<Gender>(false, Gender.class);
	private ModelListBox<DocType> lbDocType = new ModelListBox<DocType>(false);
	private ListDataProvider<DocType> ldpDocType = new ListDataProvider<DocType>();
	private TextBox edSeries = new TextBox();
	private TextBox edNumber = new TextBox();
	private ModelListBox<Nationality> lbNationality = new ModelListBox<Nationality>(false);
	private ListDataProvider<Nationality> ldpNationality = new ListDataProvider<Nationality>();
	private DateTextBox dtpDocDate = new DateTextBox();
	private DateTextBox dtpBirthday = new DateTextBox();
	private final String rbGroup = Document.get().createUniqueId();
	private RadioButton rbAdult = new RadioButton("ticketType-" + rbGroup, "Взрослый");
	private RadioButton rbChildren = new RadioButton("ticketType-" + rbGroup, "Детский");
	private CheckBox cbBaggage1 = new CheckBox();
	private CheckBox cbBaggage2 = new CheckBox();
	private Anchor btnTemplates = new Anchor("Шаблоны");
	private PopupPanel popupMenu = new PopupPanel(true, true);
	private MenuBar templatesMenu = new MenuBar(true);

	public TicketEditor(Style CSS, Date departure, boolean showBags) {
		addStyleName(CSS.atOrderTicketEditor());

		Label title = new Label("ИНФОРМАЦИЯ О ПАССАЖИРАХ");
		title.addStyleName(CSS.atOrderTicketTitle());
		add(title);

		Anchor btnAdd = new Anchor("Ещё один билет");
		btnAdd.addStyleName(CSS.atOrderTicketAdd());
		btnAdd.addClickHandler(event -> onAddTicketClick());
		add(btnAdd);

		edLastName.getElement().setAttribute("placeholder", "Фамилия");
		edLastName.addStyleName(CSS.atOrderTicketField());
		add(edLastName, 15, 50);

		edFirstName.getElement().setAttribute("placeholder", "Имя");
		edFirstName.addStyleName(CSS.atOrderTicketField());
		add(edFirstName, 15, 115);

		edMiddleName.getElement().setAttribute("placeholder", "Отчество");
		edMiddleName.addStyleName(CSS.atOrderTicketField());
		add(edMiddleName, 15, 180);

		edPhone.getElement().setAttribute("placeholder", "Моб. тел.");
		edPhone.addStyleName(CSS.atOrderTicketField());
		add(edPhone, 15, 245);

		rbAdult.setEnabled(false);
		rbAdult.setValue(true);
		rbAdult.addStyleName(CSS.atOrderTicketType());
		rbAdult.addValueChangeHandler(event -> TicketEditor.this.onValueChange());
		add(rbAdult, 470, 6);

		rbChildren.setEnabled(false);
		rbChildren.addStyleName(CSS.atOrderTicketType());
		rbChildren.addValueChangeHandler(event -> TicketEditor.this.onValueChange());
		add(rbChildren, 580, 6);

		lbGender.getElement().setAttribute("placeholder", "Пол");
		lbGender.addStyleName(CSS.atOrderTicketField());
		add(lbGender, 470, 50);

		ldpDocType.addDataDisplay(lbDocType);
		lbDocType.getElement().setAttribute("placeholder", "Документ");
		lbDocType.addStyleName(CSS.atOrderTicketField());
		add(lbDocType, 470, 115);

		edSeries.getElement().setAttribute("placeholder", "Серия");
		edSeries.addStyleName(CSS.atOrderTicketField());
		edSeries.setWidth("100px");
		add(edSeries, 470, 180);

		edNumber.getElement().setAttribute("placeholder", "Номер");
		edNumber.addStyleName(CSS.atOrderTicketField());
		edNumber.setWidth("156px");
		add(edNumber, 595, 180);

		Label lblBaggage = new Label("Багаж");
		lblBaggage.addStyleName(CSS.atOrderTicketBaggageLbl());
		add(lblBaggage, 470, 255);

		cbBaggage1.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (cbBaggage2.getValue())
					cbBaggage1.setValue(true);
				cbBaggage2.setValue(false);
				TicketEditor.this.onValueChange();
			}
		});
		cbBaggage1.addStyleName(CSS.atOrderTicketBaggage());
		add(cbBaggage1, 595, 250);

		cbBaggage2.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				cbBaggage1.setValue(event.getValue());
				TicketEditor.this.onValueChange();
			}
		});
		cbBaggage2.addStyleName(CSS.atOrderTicketBaggage());
		add(cbBaggage2, 646, 250);

		lblBaggage.setVisible(showBags);
		cbBaggage1.setVisible(showBags);
		cbBaggage2.setVisible(showBags);

		dtpDocDate.getDatePicker().setYearAndMonthDropdownVisible(true);
		dtpDocDate.getDatePicker().setYearArrowsVisible(true);
		dtpDocDate.getElement().setAttribute("placeholder", "Дата выдачи");
		dtpDocDate.addStyleName(CSS.atOrderTicketField());
		add(dtpDocDate, 910, 50);

		ldpNationality.addDataDisplay(lbNationality);
		lbNationality.getElement().setAttribute("placeholder", "Гражданство");
		lbNationality.addStyleName(CSS.atOrderTicketField());
		add(lbNationality, 910, 115);

		dtpBirthday.getDatePicker().setYearAndMonthDropdownVisible(true);
		dtpBirthday.getDatePicker().setYearArrowsVisible(true);
		dtpBirthday.getElement().setAttribute("placeholder", "Дата рождения");
		dtpBirthday.addStyleName(CSS.atOrderTicketField());
		dtpBirthday.setFireNullValues(true);
		dtpBirthday.addValueChangeHandler(event -> {
			Date bd = DateUtil.localToMsk(event.getValue());
			if (bd != null) {
				int age = DateUtil.getAge(bd, departure, 2);
				if (age < CHILDHOOD)
					rbChildren.setValue(true, true);
				else
					rbAdult.setValue(true, true);
			} else
				rbAdult.setValue(true, true);
		});
		add(dtpBirthday, 910, 180);

		templatesMenu.addStyleName(CSS.atOrderTicketTemplateMenu());
		popupMenu.addStyleName(CSS.atOrderTicketTemplatePopup());
		popupMenu.add(templatesMenu);
		btnTemplates.addStyleName(CSS.atOrderTicketTemplateBtn());
		btnTemplates.addClickHandler(handler -> popupMenu.showRelativeTo(btnTemplates));
		add(btnTemplates);

		RPC.getTS().getNationalities(new AsyncCallback<List<Nationality>>() {
			@Override
			public void onSuccess(List<Nationality> result) {
				ldpNationality.setList(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
				ldpNationality.setList(Collections.emptyList());
			}
		});
		RPC.getTS().getDocTypes(new AsyncCallback<List<DocType>>() {
			@Override
			public void onSuccess(List<DocType> result) {
				ldpDocType.setList(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
				ldpDocType.setList(Collections.emptyList());
			}
		});
		if (SessionUtil.getUser() != null)
			RPC.getTS().getRequisites(new AsyncCallback<List<Requisite>>() {
				@Override
				public void onSuccess(List<Requisite> result) {
					setTemplates(result);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					Window.alert(caught.getMessage());
					setTemplates(Collections.emptyList());
				}
			});
		else
			setTemplates(Collections.emptyList());
	}

	private void setTemplates(List<Requisite> reqs) {
		templatesMenu.clearItems();
		templatesMenu.addItem("Сохранить этот билет в шаблон", () -> {
			popupMenu.hide();
			User user = getUser();
			if (user != null) {
				Requisite req = new Requisite();
				req.fill(user);
				RPC.getTS().saveRequisite(req, new AsyncCallback<Requisite>() {
					@Override
					public void onSuccess(Requisite result) {
						templatesMenu.addItem(result.getDisplayField(), () -> {
							popupMenu.hide();
							setUser(user);
						});
					}

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}
				});
			} else
				Window.alert("Заполнены не все поля. Проверьте правильность заполнения даных и повторите попытку");
		});
		if ((reqs != null) && !reqs.isEmpty()) {
			templatesMenu.addSeparator();
			for (Requisite req : reqs)
				templatesMenu.addItem(req.getDisplayField(), () -> {
					popupMenu.hide();
					User user = new User();
					user.fill(req);
					setUser(user);
				});
		}
	}

	private boolean isNotEmpty(String val) {
		return (val != null) && !val.isEmpty();
	}

	private boolean checkFileds() {
		return isNotEmpty(edFirstName.getValue())
				&& isNotEmpty(edLastName.getValue())
				&& isNotEmpty(edMiddleName.getValue())
				&& isNotEmpty(edPhone.getValue())
				&& isNotEmpty(edSeries.getValue())
				&& isNotEmpty(edNumber.getValue())
				&& (dtpBirthday.getValue() != null)
				&& (lbGender.getSelectedModel() != null)
				&& (lbDocType.getSelectedModel() != null)
				&& (lbNationality.getSelectedModel() != null)
				&& (dtpDocDate.getValue() != null);
	}

	public void setUser(User user) {
		if (user != null) {
			edFirstName.setValue(user.getFirstName());
			edLastName.setValue(user.getLastName());
			edMiddleName.setValue(user.getMiddleName());
			edPhone.setValue(user.getPhone());
			lbGender.setValue((user.getGender() != null) ? user.getGender().toString() : "");
			lbDocType.setValue(user.getDocument());
			edSeries.setValue(user.getPaspSeriya());
			edNumber.setValue(user.getPaspNumber());
			dtpDocDate.setValue(user.getVdate());
			lbNationality.setValue(user.getGrajdName());
			dtpBirthday.setValue(user.getBirthday(), true);
		} else {
			edFirstName.setValue(null);
			edLastName.setValue(null);
			edMiddleName.setValue(null);
			edPhone.setValue(null);
			lbGender.setValue(null);
			lbDocType.setValue(null);
			edSeries.setValue(null);
			edNumber.setValue(null);
			dtpDocDate.setValue(null);
			lbNationality.setValue(null);
			dtpBirthday.setValue(null, true);
		}
	}

	public User getUser() {
		if (checkFileds()) {
			User user = new User();
			user.setFirstName(edFirstName.getValue());
			user.setLastName(edLastName.getValue());
			user.setMiddleName(edMiddleName.getValue());
			user.setPhone(edPhone.getValue());
			user.setGender(lbGender.getSelectedModel());
			user.setDocument(lbDocType.getValue());
			user.setDocType(lbDocType.getSelectedModel().getId());
			user.setPaspSeriya(edSeries.getValue());
			user.setPaspNumber(edNumber.getValue());
			user.setVdate(dtpDocDate.getValue());
			user.setGrajdName(lbNationality.getValue());
			user.setGrajd(lbNationality.getSelectedModel().getId());
			user.setBirthday(dtpBirthday.getValue());
			user.setBags((long) getBags());
			user.setIsChild(isChild());
			return user;
		} else
			return null;
	}

	protected abstract void onAddTicketClick();

	protected abstract void onValueChange();

	public boolean isChild() {
		return rbChildren.getValue();
	}

	public int getBags() {
		return (cbBaggage1.getValue() ? 1 : 0) + (cbBaggage2.getValue() ? 1 : 0);
	}

}