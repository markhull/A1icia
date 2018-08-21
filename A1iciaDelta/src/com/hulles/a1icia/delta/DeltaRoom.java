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
package com.hulles.a1icia.delta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.A1icianAction;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SememePackage;

/**
 * The new Delta Room handles Station hardware requests (turn LED on, etc.) and 
 * ASR (Automated Speech Recognition). Upon further review we're eliminating Sphinx4,
 * so it's just station hardware for now.
 * 
 * @author hulles
 *
 */
public final class DeltaRoom extends UrRoom {
	private final static Logger LOGGER = Logger.getLogger("A1iciaDelta.DeltaRoom");
	private final static Level LOGLEVEL = LOGGER.getParent().getLevel();

	public DeltaRoom() {
		super();
	}

	@Override
	public Room getThisRoom() {

		return Room.DELTA;
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
				return createA1icianActionPackage(sememePkg, request);
			default:
				throw new A1iciaException("Received unknown sememe in " + getThisRoom());
		}
	}
	
	private static ActionPackage createA1icianActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		A1icianAction action;
		String a1icianStr;
		A1icianID a1icianID;
		SerialSememe sememe;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		LOGGER.log(LOGLEVEL, "DeltaRoom: in createA1icianActionPackage");
		a1icianStr = sememePkg.getSememeObject();
		if (a1icianStr == null) {
			System.err.println("Delta Room: a1ician ID sememeObject is null");
			return null;
		}
		LOGGER.log(LOGLEVEL, "DeltaRoom: a1icianStr = " + a1icianStr);
		if (a1icianStr.equals("{consoleID}")) {
			a1icianID = null;
		} else {
			a1icianID = new A1icianID(a1icianStr);
			if (!a1icianID.isValid()) {
				a1icianID = null;
			}
		}
		LOGGER.log(LOGLEVEL, "DeltaRoom: a1icianID = " + a1icianID);
		sememe = sememePkg.getSememe();
		pkg = new ActionPackage(sememePkg);
		action = new A1icianAction();
		action.setMessage("Sent the console message you requested");
		action.setClientAction(sememe);
		action.setToA1icianID(a1icianID);
		pkg.setActionObject(action);
		LOGGER.log(LOGLEVEL, "DeltaRoom: sending action package");
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
