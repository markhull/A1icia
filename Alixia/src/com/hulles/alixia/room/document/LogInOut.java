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
package com.hulles.alixia.room.document;

import com.hulles.alixia.api.shared.SerialPerson;
import com.hulles.alixia.api.shared.SerialUUID;
import com.hulles.alixia.api.shared.SharedUtils;

/**
 * A RoomObject for person log-ins and log-outs.
 * 
 * @author hulles
 *
 */
public class LogInOut implements RoomObject {
	SerialUUID<SerialPerson> uuid;
	LogInLogOut logInLogOut;
		
	public SerialUUID<SerialPerson> getPersonUUID() {
		
		return uuid;
	}

	public void setPersonUUID(SerialUUID<SerialPerson> uuid) {
		
		SharedUtils.checkNotNull(uuid);
		this.uuid = uuid;
	}

	public LogInLogOut getLogInLogOut() {
		
		return logInLogOut;
	}

	public void setLogInLogOut(LogInLogOut logInLogOut) {
		
		SharedUtils.checkNotNull(logInLogOut);
		this.logInLogOut = logInLogOut;
	}

	@Override
	public RoomObjectType getRoomObjectType() {

		return RoomObjectType.LOGINLOGOUT;
	}

	public enum LogInLogOut {
		LOGIN,
		LOGOUT
	}
}
