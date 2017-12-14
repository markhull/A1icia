/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of A1icia.
 *  
 * A1icia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *    
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.hulles.a1icia.kilo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.shared.ApplicationKeys;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.OwmCity;
import com.hulles.a1icia.cayenne.Spark;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.RoomActionObject;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * Kilo Room is our weather room. I'm currently using Open Weather Room for the data, and I'm
 * mostly happy with it, but I may add some other services to compare. (Weather Underground?) 
 * 
 * @author hulles
 *
 */
public final class KiloRoom extends UrRoom {
	private final ApplicationKeys appKeys;

	public KiloRoom(EventBus bus) {
		super(bus);
		
		appKeys = ApplicationKeys.getInstance();
	}

	@Override
	public Room getThisRoom() {

		return Room.KILO;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new A1iciaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {
	}

	@Override
	protected void roomShutdown() {
	}

	@Override
	protected ActionPackage createActionPackage(SparkPackage sparkPkg, RoomRequest request) {

		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		switch (sparkPkg.getName()) {
			case "weather_forecast":
				return createForecastActionPackage(sparkPkg, request);
			case "current_weather":
				return createWeatherActionPackage(sparkPkg, request);
			case "this_location":
				return createLocationActionPackage(sparkPkg, request);
			case "current_time":
				return createTimeActionPackage(sparkPkg, request);
			default:
				throw new A1iciaException("Received unknown spark in " + getThisRoom());
		}
	}

	private static ActionPackage createTimeActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		ActionPackage pkg;
		KiloTimeAction action;
		LocalDateTime now;
		KiloLocationAction locationAction;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		locationAction = KiloLocation.getLocation();
		action = new KiloTimeAction();
		action.setLocation(locationAction.getCity());
		now = LocalDateTime.now();
		pkg = new ActionPackage(sparkPkg);
		action.setLocalDateTime(now);
		pkg.setActionObject(action);
		return pkg;
	}
	
	private ActionPackage createForecastActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		ActionPackage pkg;
		RoomActionObject action;
		String idStr;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		idStr = appKeys.getDefaultOWMCity();
		action = KiloWeather.getForecastWeather(Integer.parseInt(idStr));
		pkg = new ActionPackage(sparkPkg);
		pkg.setActionObject(action);
		return pkg;
	}

	private ActionPackage createWeatherActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		ActionPackage pkg;
		RoomActionObject action;
		String idStr;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		idStr = appKeys.getDefaultOWMCity();
		action = KiloWeather.getCurrentWeather(Integer.parseInt(idStr));
		pkg = new ActionPackage(sparkPkg);
		pkg.setActionObject(action);
		return pkg;
	}

	private static ActionPackage createLocationActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		ActionPackage pkg;
		RoomActionObject action;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		action = KiloLocation.getLocation();
		pkg = new ActionPackage(sparkPkg);
		pkg.setActionObject(action);
		return pkg;
	}
	
	@SuppressWarnings("unused")
	private static KiloLocationAction findLocation(String sparkObject) {
		List<OwmCity> cities;
		KiloLocationAction action;
		OwmCity match;
		
		A1iciaUtils.checkNotNull(sparkObject);
		cities = OwmCity.getOwmCities(sparkObject);
		if (cities.isEmpty()) {
			// whoever created the initial spark package should have vetted
			//    the city already, from NER e.g.
			A1iciaUtils.error("KiloRoom:findLocation: location not found");
			return null;
		}
		// TODO ask client to narrow down list of cities
		//  For now we'll just take the first one
		match = cities.get(0);
		action = new KiloLocationAction();
		action.setCity(match.getName());
		action.setCountry(match.getCountry().getName());
		action.setLatitude(match.getLatitude());
		action.setLongitude(match.getLongitude());
		action.setOwmCityID(match.getOwmId());
		return action;
	}
	
	@Override
	protected Set<Spark> loadSparks() {
		Set<Spark> sparks;
		
		sparks = new HashSet<>();
		sparks.add(Spark.find("weather_forecast"));
		sparks.add(Spark.find("current_weather"));
		sparks.add(Spark.find("current_weather_location"));
		sparks.add(Spark.find("this_location"));
		sparks.add(Spark.find("current_time"));
		sparks.add(Spark.find("current_time_location"));
		return sparks;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
