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
package com.hulles.a1icia.room.document;

/**
 * The abstract super-class for RoomActionObjects.
 * 
 * @author hulles
 *
 */
public abstract class RoomActionObject implements RoomObject {
    
	public RoomActionObject() {
	}

	@Override
	public RoomObjectType getRoomObjectType() {
		
		return RoomObjectType.ROOMACTION;
	}

	public abstract String getMessage();
	
	public abstract String getExplanation();
	
	@Override
	public String toString() {
		
		return getExplanation();
	}
}
