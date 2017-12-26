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
package com.hulles.a1icia.room.document;

import java.util.Set;

import com.hulles.a1icia.tools.A1iciaUtils;
import com.hulles.a1icia.cayenne.Spark;

/**
 * This action is a room's response to a "what_sparks" query asking which sparks the room can handle.
 * 
 * @author hulles
 *
 */
public class WhatSparksAction extends RoomActionObject {
	private Set<Spark> sparks;

	public WhatSparksAction() {
		
	}
	
	public Set<Spark> getSparks() {
		
		return sparks;
	}

	public void setSparks(Set<Spark> sparks) {
		
		A1iciaUtils.checkNotNull(sparks);
		this.sparks = sparks;
	}

	@Override
	public String getMessage() {

		return null;
	}

	@Override
	public String getExplanation() {

		return null;
	}

}
