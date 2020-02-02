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
package com.hulles.alixia.api.object;

import com.hulles.alixia.api.shared.SerialPerson;
import com.hulles.alixia.api.shared.SerialUUID;
import com.hulles.alixia.api.shared.SharedUtils;

/**
 * The response from Alixia Central to the log in/out request from the remote station.
 * 
 * @see LoginObject
 * 
 * @author hulles
 *
 */
public class LoginResponseObject implements AlixiaClientObject {
	private static final long serialVersionUID = 4715884810552504623L;
	private SerialUUID<SerialPerson> personUUID;
	private String userName;
	
	public SerialUUID<SerialPerson> getPersonUUID() {
		
		return personUUID;
	}

	public void setPersonUUID(SerialUUID<SerialPerson> personUUID) {
		
		SharedUtils.nullsOkay(personUUID);
		this.personUUID = personUUID;
	}
	
	public String getUserName() {
		
		return userName;
	}

	public void setUserName(String userName) {
		
		SharedUtils.nullsOkay(userName);
		this.userName = userName;
	}

	@Override
	public ClientObjectType getClientObjectType() {

		return ClientObjectType.LOGIN_RESPONSE;
	}

	@Override
	public boolean isValid() {

		// personUUID can be null if failed login or logout
		return true;
	}

}
