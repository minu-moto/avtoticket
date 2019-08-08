/*
 * Copyright MinuSoft (c) 2015.
 */
package com.avtoticket.client;

import com.avtoticket.client.about.AboutPlace;
import com.avtoticket.client.admin.AdminPlace;
import com.avtoticket.client.articles.ArticlePlace;
import com.avtoticket.client.carrier.CarrierPlace;
import com.avtoticket.client.map.MapPlace;
import com.avtoticket.client.mobile.MobilePlace;
import com.avtoticket.client.order.OrderPlace;
import com.avtoticket.client.partners.PartnersPlace;
import com.avtoticket.client.profile.ProfilePlace;
import com.avtoticket.client.project.ProjectPlace;
import com.avtoticket.client.reference.ReferencePlace;
import com.avtoticket.client.routes.RoutesPlace;
import com.avtoticket.client.service.ServicePlace;
import com.avtoticket.client.tickets.TicketsPlace;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 27 мая 2015 г. 0:05:51
 */
public class AppPlaceHistoryMapper implements PlaceHistoryMapper {

	@Override
	public Place getPlace(String token) {
		int idx = token.indexOf('/');
		if (idx < 0)
			idx = token.length();

		switch (token.substring(0, idx)) {
		case AboutPlace.NAME:
			return AboutPlace.INSTANCE;
		case MapPlace.NAME:
			return MapPlace.INSTANCE;
		case MobilePlace.NAME:
			return MobilePlace.INSTANCE;
		case PartnersPlace.NAME:
			return PartnersPlace.INSTANCE;
		case ProjectPlace.NAME:
			return ProjectPlace.INSTANCE;
		case ReferencePlace.NAME:
			return ReferencePlace.INSTANCE;
		case ServicePlace.NAME:
			return ServicePlace.INSTANCE;
		case ProfilePlace.NAME:
			return ProfilePlace.INSTANCE;
		case RoutesPlace.NAME:
			return RoutesPlace.getInstance(token);
		case OrderPlace.NAME:
			return OrderPlace.getInstance(token);
		case ArticlePlace.NAME:
			return ArticlePlace.getInstance(token);
		case TicketsPlace.NAME:
			return TicketsPlace.getInstance(token);
		case AdminPlace.NAME:
			return AdminPlace.INSTANCE;
		case CarrierPlace.NAME:
			return CarrierPlace.INSTANCE;
		default:
			return null;			
		}
	}

	@Override
	public String getToken(Place place) {
		return (place != null) ? place.toString() : "";
	}

}