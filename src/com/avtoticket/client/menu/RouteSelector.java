/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.avtoticket.client.BodyPanel;
import com.avtoticket.client.menu.MainMenu.Resources;
import com.avtoticket.client.menu.MainMenu.Strings;
import com.avtoticket.client.menu.MainMenu.Style;
import com.avtoticket.client.routes.RoutesPlace;
import com.avtoticket.client.ui.ModelListBox;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.client.utils.SessionUtil;
import com.avtoticket.shared.models.core.Station;
import com.avtoticket.shared.utils.DateUtil;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.gwt.view.client.ListDataProvider;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 17 дек. 2015 г. 20:40:11
 */
public class RouteSelector extends AbsolutePanel {

//	private static final Logger logger = Logger.getLogger(RouteSelector.class.getName());

	private static RouteSelector INSTANCE;

	private Map<Station, List<Station>> routes;
	private Map<String, Station> stations;
	private Map<String, Station> depsMap;
	private Date threshold;
	private Date[] allDates;

	private ModelListBox<Station> lbFrom = new ModelListBox<Station>(false);
	private ListDataProvider<Station> ldpFrom = new ListDataProvider<Station>();

	private MultiWordSuggestOracle edToOracle = new MultiWordSuggestOracle();
	private DefaultSuggestionDisplay edToDisplay = new DefaultSuggestionDisplay();
	private SuggestBox edTo = new SuggestBox(edToOracle, new TextBox(), edToDisplay);

	private DateSelector dateSelector;
	private Button btnSearch;
	private FlowPanel btnXchg;
	private String xchg = null;
	private Long depIdSetup;
	private Long destIdSetup;
	private Date dateSetup;

	private Set<Date> collectDates(List<Station> ss) {
		return ss.stream().flatMap(s -> s.getDepDates().stream()).filter(d -> !threshold.after(d)).collect(Collectors.toSet());
	}

	private void refreshDateSelector() {
		Station dest = (stations != null) ? stations.get(edTo.getText()) : null;
		if (dest == null) {
			Station dep = depsMap.get(lbFrom.getValue());
			if (dep == null)
				dateSelector.setDates(allDates);
			else {
				Set<Date> dates = collectDates(routes.get(dep));
				Date[] dts = dates.toArray(new Date[dates.size()]);
				Arrays.sort(dts);
				dateSelector.setDates(dts);
			}
		} else {
			List<Date> dts = dest.getDepDates().stream().filter(d -> !threshold.after(d)).collect(Collectors.toList());
			dateSelector.setDates(dts.toArray(new Date[dts.size()]));
		}
	}

	private void refreshChangeBtn() {
		Station dep = depsMap.get(edTo.getText());
		String dest = lbFrom.getValue();
		btnXchg.setVisible((dep != null) && routes.containsKey(dep) && (dest != null)
				&& routes.get(dep).stream().map(Station::getDisplayField).filter(s -> dest.equalsIgnoreCase(s)).findAny().isPresent());
	}

	private RouteSelector(Resources RESOURCES, Strings STRINGS) {
		Style CSS = RESOURCES.menuStyle();
		addStyleName(CSS.atRouteSelector());

		Label lblRouteSelector = new Label(STRINGS.choose());
		lblRouteSelector.addStyleName(CSS.atRouteSelectorLabel());
		add(lblRouteSelector);

		// выбор пункта отправления
		ldpFrom.addDataDisplay(lbFrom);
		lbFrom.setPixelSize(192, 30);
		lbFrom.addStyleName(CSS.atRouteSelectorFrom());
		lbFrom.getElement().setAttribute("placeholder", STRINGS.from());
		lbFrom.addSelectionHandler(event -> {
			edTo.setValue(xchg);
			xchg = null;
			edToOracle.clear();
			stations = new HashMap<String, Station>();
			Station dep = depsMap.get(event.getSelectedItem().getDisplayString());
			if (routes.containsKey(dep))
				for (Station s : routes.get(dep)) {
					edToOracle.add(s.getDisplayField());
					stations.put(s.getDisplayField(), s);
				}
			refreshDateSelector();
			refreshChangeBtn();
		});
		add(lbFrom);

		// выбор пункта прибытия
		edTo.setLimit(30);
		edTo.addStyleName(CSS.atRouteSelectorTo());
		edToDisplay.setPopupStyleName("gwt-SuggestBoxPopup " + CSS.atRouteSelectorToSuggest());
		edTo.getElement().setAttribute("placeholder", STRINGS.to());
		edTo.getElement().<InputElement> cast().setReadOnly(true);
		edTo.setText(STRINGS.loading());
		edTo.addValueChangeHandler(event -> {
			if (event.getValue().isEmpty()) {
				refreshDateSelector();
				refreshChangeBtn();
			}
		});
		edTo.addSelectionHandler(event -> {
			refreshDateSelector();
			refreshChangeBtn();
		});
		add(edTo);

		// выбор пункта прибытия осуществляется вводом текста, стрелку выпадающего списка пока что закоментил
//		FlowPanel btnTo = new FlowPanel();
//		btnTo.addStyleName(CSS.atRouteSelectorArrow());
//		add(btnTo, 806, 36);

		FlowPanel curve = new FlowPanel();
		curve.addStyleName(CSS.atRouteSelectorCurve());
		add(curve);

		threshold = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(SessionUtil.getSalePeriod()));
		CalendarUtil.resetTime(threshold);
		threshold = DateUtil.localToMsk(threshold);

		dateSelector = new DateSelector(CSS, STRINGS);
		dateSelector.addStyleName(CSS.atDateSelector());
		add(dateSelector);

		btnXchg = new FlowPanel();
		btnXchg.addDomHandler(event -> {
			xchg = lbFrom.getValue();
			lbFrom.setValue(edTo.getValue(), true);
		}, ClickEvent.getType());
		btnXchg.addStyleName(CSS.atRouteSelectorChangeBtn());
		btnXchg.setVisible(false);
		add(btnXchg);

		btnSearch = new Button(STRINGS.find());
		btnSearch.addStyleName(CSS.atRouteSelectorSearchBtn());
		btnSearch.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Station dep = depsMap.get(lbFrom.getValue());
				if (dep == null) {
					if (lbFrom.getValue().isEmpty()) {
						Window.alert(STRINGS.chooseDep());
						lbFrom.showSuggestionList();
						lbFrom.setFocus(true);
					} else
						Window.alert(STRINGS.depNotFound(lbFrom.getValue()));
					return;
				}

				Station dest = (stations != null) ? stations.get(edTo.getText()) : null;
				if (dest == null) {
					if (edTo.getText().isEmpty()) {
						Window.alert(STRINGS.chooseDest());
						edTo.setFocus(true);
					} else
						Window.alert(STRINGS.destNotFound(edTo.getText()));
					return;
				}

				BodyPanel.goTo(new RoutesPlace(dep.getId(), dest.getId(), dateSelector.getValue()));
			}
		});
		add(btnSearch);

		RPC.getTS().getStations(new AsyncCallback<Map<Station, List<Station>>>() {
			@Override
			public void onSuccess(Map<Station, List<Station>> result) {
				routes = result;
				Set<Date> dates = new HashSet<Date>();
				for (List<Station> ss : result.values())
					dates.addAll(collectDates(ss));
				allDates = dates.toArray(new Date[dates.size()]);
				Arrays.sort(allDates);

				depsMap = new HashMap<String, Station>();
				List<Station> deps = new ArrayList<Station>(result.keySet());
				for (Station s : deps)
					depsMap.put(s.getDisplayField(), s);
				Collections.sort(deps, Comparator.comparing(Station::getDisplayField, String::compareToIgnoreCase));
				ldpFrom.setList(deps);
				lbFrom.getElement().setAttribute("placeholder", deps.isEmpty() ? STRINGS.empty() : STRINGS.from());
				edTo.getElement().setAttribute("placeholder", deps.isEmpty() ? STRINGS.empty() : STRINGS.to());

				edTo.setValue(null, true);
				edTo.getElement().<InputElement> cast().setReadOnly(false);

				setValues(depIdSetup, destIdSetup, dateSetup);
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}
		});
	}

	private void setValues(Long depId, Long destId, Date date) {
		if (routes != null) {
			Station dep = routes.keySet().stream().filter(s -> Objects.equals(depId, s.getId())).findAny().orElse(null);
			if (dep != null) {
				Station dest = routes.get(dep).stream().filter(s -> Objects.equals(destId, s.getId())).findAny().orElse(null);
				xchg = (dest != null) ? dest.getName() : null;
				lbFrom.setValue(dep.getName(), true);
			}
			depIdSetup = null;
			destIdSetup = null;
			dateSetup = null;
			Scheduler.get().scheduleDeferred(() -> dateSelector.setValue(date));
		} else {
			depIdSetup = depId;
			destIdSetup = destId;
			dateSetup = date;
		}
	}

	public static RouteSelector get(Resources RESOURCES, Strings STRINGS) {
		if (INSTANCE == null)
			INSTANCE = new RouteSelector(RESOURCES, STRINGS);
		return INSTANCE;
	}

	public static void search(String dep, String dest) {
		INSTANCE.xchg = dest;
		INSTANCE.lbFrom.setValue(dep, true);
		Scheduler.get().scheduleDeferred(() -> {
			INSTANCE.dateSelector.reset();
			INSTANCE.btnSearch.click();
		});
	}

	public static void setParams(Long depId, Long destId, Date date) {
		INSTANCE.setValues(depId, destId, date);
	}

}