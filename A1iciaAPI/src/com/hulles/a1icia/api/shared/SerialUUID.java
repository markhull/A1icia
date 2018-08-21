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

import java.io.Serializable;

final public class SerialUUID<T extends SerialEntity> implements Serializable {
	private static final long serialVersionUID = -2501164431706716035L;
	// we store it as a string so we can use this on both the client and the server
	private String uuidStr;
	
	public SerialUUID() {
		// we need a no-arg constructor for serialization
		uuidStr = null;
	}
	public SerialUUID(String uuid) throws UUIDException {

		SharedUtils.checkNotNull(uuid);
		setUUIDString(uuid);
	}
	public SerialUUID(SerialUUID<?> otherUUID) throws UUIDException {
		
		SharedUtils.checkNotNull(otherUUID);
		setUUIDString(otherUUID.getUUIDString());
	}
	
	public String getUUIDString() {
		
		return uuidStr;
	}

	private void setUUIDString(String uuidStr)  throws UUIDException{
		
		SharedUtils.checkNotNull(uuidStr);
		if (!validUUIDStringFormat(uuidStr)) {
			throw new UUIDException(uuidStr);
		}
		this.uuidStr = uuidStr;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuidStr == null) ? 0 : uuidStr.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SerialUUID<?>)) {
			return false;
		}
		SerialUUID<?> other = (SerialUUID<?>) obj;
		if (uuidStr == null) {
			if (other.uuidStr != null) {
				return false;
			}
		} else if (!uuidStr.equals(other.uuidStr)) {
			return false;
		}
		return true;
	}
	
	/*
	 * Can't use regular expressions because GWT and regular Java don't have comparable implementations
	 * Can't use java UUID.fromString and catch the exception either, that just works on the server
	 * 
	 */
	private static boolean validUUIDStringFormat(String uuid) {
		// private String uuidPattern = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";
		// "e00ea957-5b9b-400b-a731-459092a22f73"
		//  012345678901234567890123456789012345
		//  0         1         2         3 
		String subStr;
		
		SharedUtils.checkNotNull(uuid);
		if (uuid.length() != 36) {
			return false;
		}
		subStr = uuid.substring(0, 8);
		if (!isHexString(subStr)) {
			return false;
		}
		if (uuid.charAt(8) != '-') {
			return false;
		}
		subStr = uuid.substring(9, 13);
		if (!isHexString(subStr)) {
			return false;
		}
		if (uuid.charAt(13) != '-') {
			return false;
		}
		subStr = uuid.substring(14, 18);
		if (!isHexString(subStr)) {
			return false;
		}
		if (uuid.charAt(18) != '-') {
			return false;
		}
		subStr = uuid.substring(19, 23);
		if (!isHexString(subStr)) {
			return false;
		}
		if (uuid.charAt(23) != '-') {
			return false;
		}
		subStr = uuid.substring(24, 36);
		if (!isHexString(subStr)) {
			return false;
		}
		return true;
	}
	
	private static boolean isHexString(String s) {
	
		for (int ix = 0; ix < s.length(); ix++) {
			if (Character.digit(s.charAt(ix), 16) == -1) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		
		return uuidStr;
	}

	public static class UUIDException extends RuntimeException {
		private static final long serialVersionUID = 5948362124878872778L;

		UUIDException() {
		}
		
		UUIDException(String badArg) {
			super(badArg);
		}
	}

}
