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

import com.hulles.alixia.api.shared.SharedUtils;

/**
 * Request to log a person at a remote station in or out on Alixia Central.
 * 
 * @see LoginResponseObject
 * 
 * @author hulles
 *
 */
public class LoginObject implements AlixiaClientObject {
	private static final long serialVersionUID = 5508127665810763843L;
	private String userName;
	private String password;
		
	public String getUserName() {
		
		return userName;
	}

	public void setUserName(String userName) {
		
		SharedUtils.checkNotNull(userName);
		this.userName = userName;
	}

	public String getPassword() {
		
		return password;
	}

	public void setPassword(String password) {
		
		SharedUtils.checkNotNull(password);
		this.password = password;
	}

	@Override
	public ClientObjectType getClientObjectType() {
		
		return ClientObjectType.LOGIN;
	}

	@Override
	public boolean isValid() {

		// user name and/or password can legitimately be null
		//    if the person is logging out
		return true;
	}

}
