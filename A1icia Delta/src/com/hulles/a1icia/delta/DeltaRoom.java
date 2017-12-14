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
package com.hulles.a1icia.delta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.Spark;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.A1icianAction;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SparkObjectType;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * The new Delta Room handles remote hardware requests (turn LED on, etc.) and 
 * ASR (Automated Speech Recognition). 
 * 
 * @author hulles
 *
 */
public final class DeltaRoom extends UrRoom {
	private final static Logger LOGGER = Logger.getLogger("A1iciaDelta.DeltaRoom");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();

	public DeltaRoom(EventBus bus) {
		super(bus);
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
	protected ActionPackage createActionPackage(SparkPackage sparkPkg, RoomRequest request) {

		switch (sparkPkg.getName()) {
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
				return createA1icianActionPackage(sparkPkg, request);
			default:
				throw new A1iciaException("Received unknown spark in " + getThisRoom());
		}
	}
	
	private static ActionPackage createA1icianActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		ActionPackage pkg;
		A1icianAction action;
		String a1icianStr;
		A1icianID a1icianID;
		SparkObjectType type;
		Spark spark;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		LOGGER.log(LOGLEVEL, "DeltaRoom: in createA1icianActionPackage");
		type = sparkPkg.getSparkObjectType();
		if (type != SparkObjectType.ALICIAN) {
			A1iciaUtils.error("Delta Room: expected ALICIAN spark type, got " + type);
			return null;
		}
		a1icianStr = sparkPkg.getSparkObject();
		if (a1icianStr == null) {
			A1iciaUtils.error("Delta Room: a1ician ID sparkObject is null");
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
		spark = sparkPkg.getSpark();
		pkg = new ActionPackage(sparkPkg);
		action = new A1icianAction();
		action.setMessage("Sent the console message you requested");
		action.setClientAction(spark);
		action.setToA1icianID(a1icianID);
		pkg.setActionObject(action);
		LOGGER.log(LOGLEVEL, "DeltaRoom: sending action package");
		return pkg;
	}

	@Override
	protected Set<Spark> loadSparks() {
		Set<Spark> sparks;
		
		sparks = new HashSet<>();
		sparks.add(Spark.find("set_red_LED_on"));
		sparks.add(Spark.find("set_red_LED_off"));
		sparks.add(Spark.find("set_green_LED_on"));
		sparks.add(Spark.find("set_green_LED_off"));
		sparks.add(Spark.find("set_yellow_LED_on"));
		sparks.add(Spark.find("set_yellow_LED_off"));
		sparks.add(Spark.find("set_white_LED_on"));
		sparks.add(Spark.find("set_white_LED_off"));
		sparks.add(Spark.find("blink_red_LED"));
		sparks.add(Spark.find("blink_green_LED"));
		sparks.add(Spark.find("blink_yellow_LED"));
		sparks.add(Spark.find("blink_white_LED"));
		sparks.add(Spark.find("pulse_red_LED"));
		sparks.add(Spark.find("pulse_green_LED"));
		sparks.add(Spark.find("pulse_yellow_LED"));
		sparks.add(Spark.find("pulse_white_LED"));
		sparks.add(Spark.find("wake_up_console"));
		sparks.add(Spark.find("pretty_lights_off"));
		sparks.add(Spark.find("pretty_lights_random"));
		sparks.add(Spark.find("pretty_lights_spinny"));
		sparks.add(Spark.find("pretty_lights_color_wipe"));
		sparks.add(Spark.find("pretty_lights_theater"));
		sparks.add(Spark.find("pretty_lights_rainbows"));
		return sparks;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
