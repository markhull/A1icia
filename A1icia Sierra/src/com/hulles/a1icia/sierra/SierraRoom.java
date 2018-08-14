/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
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
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
package com.hulles.a1icia.sierra;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.tools.A1iciaUtils;
import com.hulles.a1icia.tools.ExternalAperture;

/**
 * Sierra Room is where we interact with the so-called Internet of Things, and let A1icia 
 * turn our stereo on and blast Rick Astley at concert volume while we're on vacation.
 * 
 * @author hulles
 *
 */
public final class SierraRoom extends UrRoom {
	private final static Logger LOGGER = Logger.getLogger("A1iciaSierra.SierraRoom");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
//	private final static Level LOGLEVEL = Level.INFO;
	private static Float saveTemp = null;
	private static Float saveHumidity = null;
	
	public SierraRoom(EventBus bus) {
		super(bus);
	}

	/**
	 * Here we create an ActionPackage from Sierra for an IoT function and return it to UrRoom.
	 * 
	 */
	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "room_temperature":
				return createRoomTemperatureActionPackage(sememePkg, request);
			case "room_humidity":
				return createRoomHumidityActionPackage(sememePkg, request);
			default:
				throw new A1iciaException("Received unknown sememe in " + getThisRoom());
		}
	}

	/**
	 * Create an action package for the "room_temperature" sememe
	 * 
	 * @param sememePkg
	 * @param request
	 * @return
	 */
	private static ActionPackage createRoomTemperatureActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		MessageAction action;
		String temperatureStr;
		String messageStr;
		Float temperature;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		LOGGER.log(LOGLEVEL, "In createRoomTemperature");
		pkg = new ActionPackage(sememePkg);
		action = new MessageAction();
		temperatureStr = ExternalAperture.getCurrentTempAndHumidity("temp");
		// we get a NAN value occasionally with the DHT11 when the device is queried more than once
		//    every two seconds, so we cache the last good value and return it
		if (temperatureStr.equals("nan")) {
			if (saveTemp == null) {
				messageStr = "invalid";
			} else {
				messageStr = saveTemp.intValue() + "℉";
			}
		} else {
			try {
				temperature = Float.parseFloat(temperatureStr);
				saveTemp = temperature;
				messageStr = temperature.intValue() + "℉";
			} catch (NullPointerException npe) {
				messageStr = "unavailable";
			} catch (NumberFormatException nfe) {
				messageStr = "invalid";
			}
		}
		LOGGER.log(LOGLEVEL, "messageStr = " +  messageStr);
		LOGGER.log(LOGLEVEL, "temperatureStr = " +  temperatureStr);
		action.setMessage("The room temperature is " + messageStr);
		action.setExplanation("The room temperature was reported as " + temperatureStr);
		pkg.setActionObject(action);
		return pkg;
	}

	/**
	 * Create an action package for the "room_humidity" sememe
	 * 
	 * @param sememePkg
	 * @param request
	 * @return
	 */
	private static ActionPackage createRoomHumidityActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		MessageAction action;
		String humidityStr;
		String messageStr;
		Float humidity;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		LOGGER.log(LOGLEVEL, "In createRoomHumidityActionPackage");
		pkg = new ActionPackage(sememePkg);
		action = new MessageAction();
		humidityStr = ExternalAperture.getCurrentTempAndHumidity("humidity");
		// we get a NAN value occasionally with the DHT11 when the device is queried more than once
		//    every two seconds, so we cache the last good value and return it
		if (humidityStr.equals("nan")) {
			if (saveHumidity == null) {
				messageStr = "invalid";
			} else {
				messageStr = saveHumidity.intValue() + "%";
			}
		} else {
			try {
				humidity = Float.parseFloat(humidityStr);
				saveHumidity = humidity;
				messageStr = humidity.intValue() + "%";
			} catch (NullPointerException npe) {
				messageStr = "unavailable";
			} catch (NumberFormatException nfe) {
				messageStr = "invalid";
			}
		}
		LOGGER.log(LOGLEVEL, "humidityStr = " + humidityStr);
		LOGGER.log(LOGLEVEL, "messageStr = " + messageStr);
		action.setMessage("The room humidity is " + messageStr);
		action.setExplanation("The room relative humidity was reported as " + humidityStr);
		pkg.setActionObject(action);
		return pkg;
	}
	
	/**
	 * Return this room name.
	 * 
	 */
	@Override
	public Room getThisRoom() {

		return Room.SIERRA;
	}

	/**
	 * We don't get responses for anything, so if there is one it's an error.
	 * 
	 */
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

	/**
	 * Advertise which sememes we handle.
	 * 
	 */
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("room_temperature"));
		sememes.add(SerialSememe.find("room_humidity"));
		return sememes;
	}

	/**
	 * We don't do anything with RoomAnnouncements but it is legitimate to receive them here,
	 * so no error.
	 * 
	 */
	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
