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
package com.hulles.a1icia.api.object;

import com.hulles.a1icia.api.shared.SharedUtils;

/**
 * Request to log a person at a remote station in or out on the A1icia server.
 * 
 * @see LoginResponseObject
 * 
 * @author hulles
 *
 */
public class LoginObject implements A1iciaClientObject {
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
