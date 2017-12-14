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
package com.hulles.a1icia.api.shared;

import java.io.Serializable;

public abstract class SerialEntity implements Serializable {
	private static final long serialVersionUID = -4704550566143714188L;

	public SerialEntity() {
	}
	
	abstract public SerialUUID<? extends SerialEntity> getKey();

	@Override
	public boolean equals(Object obj) {
		String uuidStr;
		String otherUUIDStr;
		
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SerialEntity)) {
			return false;
		}
		SerialEntity other = (SerialEntity) obj;
		uuidStr = this.getKey().getUUIDString();
		otherUUIDStr = other.getKey().getUUIDString();
		if (uuidStr == null) {
			if (otherUUIDStr != null) {
				return false;
			}
		} else if (!uuidStr.equals(otherUUIDStr)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((getKey() == null) ? 0 : getKey().getUUIDString().hashCode());
		return result;
	}

}
