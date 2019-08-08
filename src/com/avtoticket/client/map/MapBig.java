/*
 * Copyright Avtoticket (c) 2015.
 */
package com.avtoticket.client.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.avtoticket.client.map.MapPlace.Strings;
import com.avtoticket.client.map.MapPlace.Style;
import com.avtoticket.client.ui.ModelListBox;
import com.avtoticket.client.utils.RPC;
import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.core.Station;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.overlays.Animation;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.view.client.ListDataProvider;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 20 дек. 2015 г. 21:37:27
 */
public class MapBig extends FlowPanel {

	private final Strings STRINGS;
	private FlowPanel content = new FlowPanel();
	private boolean isInit = false;
	private boolean isAttached = false;

	private MapWidget map;
	private MapOptions options;
	private List<Marker> markers = new ArrayList<Marker>();

	private ModelListBox<BaseModel> lbFrom = new ModelListBox<BaseModel>(false);
	private ListDataProvider<BaseModel> ldpFrom = new ListDataProvider<BaseModel>();

	public MapBig(Style CSS, Strings STRINGS) {
		this.STRINGS = STRINGS;
		content.addStyleName(CSS.atMapBig());
		add(content);

		Label label = new Label(STRINGS.caption());
		label.addStyleName(CSS.atMapLabel());
		add(label);

		LoadApi.go(new Runnable() {
			@Override
			public void run() {
				isInit = true;
				if (isAttached()) {
					isAttached = true;
					createMap();
				}
			}
		}, true);

		RPC.getTS().getMapStations(new AsyncCallback<Map<String, List<Station>>>() {
			@Override
			public void onSuccess(Map<String, List<Station>> result) {
				List<String> s = new ArrayList<String>(result.keySet());
				Collections.sort(s, String::compareToIgnoreCase);
				List<BaseModel> list = new ArrayList<BaseModel>(s.size());
				List<Station> all = new ArrayList<Station>();
				for (String name : s) {
					List<Station> ss = result.get(name);
					list.add(new BaseModel(ss, name));
					all.addAll(ss);
				}
				if (!s.isEmpty())
					list.add(0, new BaseModel(all, STRINGS.all()));
				ldpFrom.setList(list);
				lbFrom.setValue(s.isEmpty() ? STRINGS.empty() : STRINGS.all(), true);
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}
		});

		// выбор населённого пункта
		Label lblFrom = new Label(STRINGS.searchLabel());
		lblFrom.addStyleName(CSS.atMapLocalityLabel());
		add(lblFrom);
		ldpFrom.addDataDisplay(lbFrom);
		lbFrom.addStyleName(CSS.atMapLocalitySelector());
		lbFrom.setPixelSize(200, 30);
		add(lbFrom);

		lbFrom.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@SuppressWarnings("unchecked")
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				BaseModel locality = lbFrom.getSelectedModel();
				if (locality != null)
					showStations((List<Station>) locality.getValueField());
			}
		});
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		if (isInit && !isAttached) {
			isAttached = true;
			createMap();
		} else {
			if (!ldpFrom.getList().isEmpty())
				lbFrom.setValue(STRINGS.all(), true);
		}
	}

	@SuppressWarnings("unchecked")
	private void createMap() {
		options = MapOptions.newInstance();
		options.setMapTypeId(MapTypeId.ROADMAP);
		options.setMapTypeControl(true);
		options.setScrollWheel(true);
		options.setZoom(16);

		map = new MapWidget(options);
		map.setSize("100%", "100%");
		content.add(map);

		BaseModel locality = lbFrom.getSelectedModel();
		if (locality != null)
			showStations((List<Station>) locality.getValueField());
	}

	private void boundMap() {
		if (markers.size() == 1) {
			map.panTo(markers.get(0).getPosition());
			map.setZoom(12);
		} else if (markers.size() > 0) {
			LatLngBounds bounds = LatLngBounds.newInstance(markers.get(0).getPosition(), markers.get(1).getPosition());
			for (int i = 2; i < markers.size(); i++)
				bounds.extend(markers.get(i).getPosition());
			map.fitBounds(bounds);
		}
	}

	private void removeAllMarkers() {
		for (Marker m : markers)
			m.setMap((MapWidget) null);
		markers.clear();
	}

	private void showStations(List<Station> stations) {
		if ((stations == null) || (map == null))
			return;

		removeAllMarkers();
		Collections.sort(stations, (o1, o2) -> o2.getLatitude().compareTo(o1.getLatitude()));
		for (Station s : stations) {
			MarkerOptions mOptions = MarkerOptions.newInstance();
			mOptions.setTitle(s.getName() + "\n" + s.getAddress());
			mOptions.setAnimation(Animation.DROP);
			LatLng pos = LatLng.newInstance(s.getLatitude(), s.getLongitude());
			Marker marker = Marker.newInstance(mOptions);
			marker.setPosition(pos);
			marker.setMap(map);
			markers.add(marker);
		}
		boundMap();
	}

}