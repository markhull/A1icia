/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of Alixia.
 *  
 * Alixia is free software: you can redistribute it and/or modify
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
package com.hulles.alixia.sierra;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.MessageAction;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.alixia.tools.ExternalAperture;

/**
 * Sierra Room is where we interact with the so-called Internet of Things (IoT), and let Alixia 
 * turn our stereo on and blast Rick Astley at concert volume while we're on vacation.
 * 
 * @author hulles
 *
 */
public final class SierraRoom extends UrRoom {
	private final static Logger LOGGER = LoggerFactory.getLogger(SierraRoom.class);
	private static Float saveTemp = null;
	private static Float saveHumidity = null;
	
	public SierraRoom() {
		super();
	}

	/**
	 * Here we create an ActionPackage from Sierra for an IoT function and return it to UrRoom.
	 * 
     * @param sememePkg The query sememe
     * @param request The room's request
     * @return The ActionPackage
	 */
	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "room_temperature":
				return createRoomTemperatureActionPackage(sememePkg, request);
			case "room_humidity":
				return createRoomHumidityActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
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
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		LOGGER.debug("In createRoomTemperature");
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
		LOGGER.debug("messageStr = {}", messageStr);
		LOGGER.debug("temperatureStr = {}", temperatureStr);
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
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		LOGGER.debug("In createRoomHumidityActionPackage");
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
		LOGGER.debug("humidityStr = {}", humidityStr);
		LOGGER.debug("messageStr = {}", messageStr);
		action.setMessage("The room humidity is " + messageStr);
		action.setExplanation("The room relative humidity was reported as " + humidityStr);
		pkg.setActionObject(action);
		return pkg;
	}
	
	/**
	 * Return this room name.
	 * 
     * @return This room
     * 
	 */
	@Override
	public Room getThisRoom() {

		return Room.SIERRA;
	}

	/**
	 * We don't get responses for anything, so if there is one it's an error.
	 * 
     * @param request The request
     * @param responses The list of responses we receive
	 */
	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new AlixiaException("Response not implemented in " + 
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
     * @return The list of sememes we support
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
     * @param announcement The announcement
	 */
	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
