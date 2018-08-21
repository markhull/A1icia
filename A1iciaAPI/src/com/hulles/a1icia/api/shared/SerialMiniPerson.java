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
package com.hulles.a1icia.api.shared;


/**
 * 
 * @author hulles
 */
final public class SerialMiniPerson extends SerialEntity {
	private static final long serialVersionUID = 1732012700735206589L;
	private String userName;
	private String fullNameLF; // lastname, firstname
	private String fullNameFL; // firstname lastname
	private UserType userType;
	private transient SerialUUID<SerialPerson>  uuid;

	public SerialMiniPerson() {
		// need no-arg constructor
	}

	public SerialUUID<SerialPerson> getUUID() {
		
        return uuid;
    }

    public void setUUID(SerialUUID<SerialPerson> uuid) {
    	
		SharedUtils.checkNotNull(uuid);
        this.uuid = uuid;
    }

	public String getUserName() {
		
		return userName;
	}

	public void setUserName(String userName) {
		
		SharedUtils.checkNotNull(userName);
		this.userName = userName;
	}

	public UserType getUserType() {
		
		return this.userType;
	}

	public void setUserType(UserType userType) {
		
		SharedUtils.checkNotNull(userType);
		this.userType = userType;
	}

	public String getFullNameLF() {
		
		return this.fullNameLF;
	}

	public String getFullNameFL() {
		
		return this.fullNameFL;
	}

	public void setFullNameFL(String name) {
		
		SharedUtils.checkNotNull(name);
		this.fullNameFL = name;
	}

	public void setFullNameLF(String name) {
		
		SharedUtils.checkNotNull(name);
		this.fullNameLF = name;
	}

	@Override
	public SerialUUID<SerialPerson> getKey() {

		return uuid;
	}

}
