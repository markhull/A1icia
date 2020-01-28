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
package com.hulles.alixia.delta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.AlixianAction;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;

/**
 * The new Delta Room handles Station hardware requests (turn LED on, etc.) and 
 * ASR (Automated Speech Recognition). Upon further review we're eliminating Sphinx4,
 * so it's just station hardware for now.
 * 
 * @author hulles
 *
 */
public final class DeltaRoom extends UrRoom {
	private final static Logger LOGGER = LoggerFactory.getLogger(DeltaRoom.class);

	public DeltaRoom() {
		super();
	}

	@Override
	public Room getThisRoom() {

		return Room.DELTA;
	}

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
	
	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "set_red_LED_on":
			case "set_red_LED_off":
			case "set_green_LED_on":
			case "set_green_LED_off":
			case "set_yellow_LED_on":
			case "set_yellow_LED_off":
			case "set_white_LED_on":
			case "set_white_LED_off":
			case "blink_red_LED":
			case "blink_green_LED":
			case "blink_yellow_LED":
			case "blink_white_LED":
			case "pulse_red_LED":
			case "pulse_green_LED":
			case "pulse_yellow_LED":
			case "pulse_white_LED":
			case "wake_up_console":
			case "pretty_lights_off":
			case "pretty_lights_random":
			case "pretty_lights_spinny":
			case "pretty_lights_color_wipe":
			case "pretty_lights_theater":
			case "pretty_lights_rainbows":
				return createAlixianActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
		}
	}
	
	private static ActionPackage createAlixianActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		AlixianAction action;
		String alixianStr;
		AlixianID alixianID;
		SerialSememe sememe;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		LOGGER.debug("DeltaRoom: in createAlixianActionPackage");
		alixianStr = sememePkg.getSememeObject();
		if (alixianStr == null) {
			LOGGER.error("Delta Room: alixian ID sememeObject is null");
			return null;
		}
		LOGGER.debug("DeltaRoom: alixianStr = {}", alixianStr);
		if (alixianStr.equals("{consoleID}")) {
			alixianID = null;
		} else {
			alixianID = new AlixianID(alixianStr);
			if (!alixianID.isValid()) {
				alixianID = null;
			}
		}
		LOGGER.debug("DeltaRoom: alixianID = {}", alixianID);
		sememe = sememePkg.getSememe();
		pkg = new ActionPackage(sememePkg);
		action = new AlixianAction();
		action.setMessage("Sent the console message you requested");
		action.setClientAction(sememe);
		action.setToAlixianID(alixianID);
		pkg.setActionObject(action);
		LOGGER.debug("DeltaRoom: sending action package");
		return pkg;
	}

	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("set_red_LED_on"));
		sememes.add(SerialSememe.find("set_red_LED_off"));
		sememes.add(SerialSememe.find("set_green_LED_on"));
		sememes.add(SerialSememe.find("set_green_LED_off"));
		sememes.add(SerialSememe.find("set_yellow_LED_on"));
		sememes.add(SerialSememe.find("set_yellow_LED_off"));
		sememes.add(SerialSememe.find("set_white_LED_on"));
		sememes.add(SerialSememe.find("set_white_LED_off"));
		sememes.add(SerialSememe.find("blink_red_LED"));
		sememes.add(SerialSememe.find("blink_green_LED"));
		sememes.add(SerialSememe.find("blink_yellow_LED"));
		sememes.add(SerialSememe.find("blink_white_LED"));
		sememes.add(SerialSememe.find("pulse_red_LED"));
		sememes.add(SerialSememe.find("pulse_green_LED"));
		sememes.add(SerialSememe.find("pulse_yellow_LED"));
		sememes.add(SerialSememe.find("pulse_white_LED"));
		sememes.add(SerialSememe.find("wake_up_console"));
		sememes.add(SerialSememe.find("pretty_lights_off"));
		sememes.add(SerialSememe.find("pretty_lights_random"));
		sememes.add(SerialSememe.find("pretty_lights_spinny"));
		sememes.add(SerialSememe.find("pretty_lights_color_wipe"));
		sememes.add(SerialSememe.find("pretty_lights_theater"));
		sememes.add(SerialSememe.find("pretty_lights_rainbows"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
