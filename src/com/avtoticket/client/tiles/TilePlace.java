/*
 * Copyright Avtoticket (c) 2016.
 */
package com.avtoticket.client.tiles;

import com.avtoticket.client.utils.ATPlace;
import com.google.gwt.activity.shared.Activity;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 10 янв. 2016 г. 0:39:22
 */
public abstract class TilePlace extends ATPlace {

	@Override
	public Activity getActivity() {
		return Tiles.TILES_ACTIVITY;
	}

	@Override
	public abstract Activity getActivity(Object param);

}