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
package com.hulles.alixia.room;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;

/**
 * This is an enumeration of the various possible Alixia Rooms. Not all of them are functional yet.
 * <p>
 * Note that order matters : EnumSet lists enums in the order in which they're declared,
 * <p>
 * Note also that this list is not tightly coupled with what rooms may or may not be available
 * at runtime; a new module can help itself to a name on the list.
 *  
 * <i>ceteris paribus</i>.
 * 
 * @author hulles
 *
 */
public enum Room {
	QA(33, "QA"),
	TRACKER(32, "Tracker"),
    BUSMONITOR(0, "Bus Monitor"),    
	CONTROLLER(31, "Controller"),
    ALIXIA(30, "ALIXIA"),
	OVERMIND(1, "Overmind Room"),
    ALPHA(2, "Alpha Room"),
    BRAVO(3, "Bravo Room"),
    CHARLIE(4, "Charlie Room"),
    DELTA(5, "Delta Room"),
    ECHO(6, "Echo Room"),
    FOXTROT(7, "Foxtrot Room"),
    GOLF(8, "Golf Room"),
    HOTEL(9, "Hotel Room"),
    INDIA(10, "India Room"),
    JULIET(11, "Juliet Room"),
    KILO(12, "Kilo Room"),
    LIMA(13, "Lima Room"),
    MIKE(14, "Mike Room"),
    NOVEMBER(15, "November Room"),
    OSCAR(16, "Oscar Room"),
    PAPA(17, "Papa Room"),
    QUEBEC(18, "Quebec Room"),
    ROMEO(19, "Romeo Room"),
    SIERRA(20, "Sierra Room"),
    TANGO(21, "Tango Room"),
    UNIFORM(22, "Uniform Room"),
    VICTOR(23, "Victor Room"),
    WHISKEY(24, "Whiskey Room"),
    XRAY(25, "X-Ray Room"),
    YANKEE(26, "Yankee Room"),
    ZULU(27, "Zulu Room");
	// last-used storeID = 33
    private final Integer storeID;
    private final String displayName;

    private Room(Integer storeID, String displayName) {
        this.storeID = storeID;
        this.displayName = displayName;
    }
    
    public Integer getStoreID() {
        return storeID;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public static Room findRoom(Integer roomID) {
    	
		SharedUtils.checkNotNull(roomID);
    	for (Room a : Room.values()) {
    		if (a.storeID == roomID) {
    			return a;
    		}
        }
    	throw new AlixiaException("Invalid room type = " + roomID.toString());
    }
}
