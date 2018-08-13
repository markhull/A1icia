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

import java.util.Set;

import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * This action is a room's response to a "what_sememes" query asking which sememes the room can handle.
 * 
 * @author hulles
 *
 */
public class WhatSememesAction extends RoomActionObject {
	private Set<SerialSememe> sememes;

	public WhatSememesAction() {
		
	}
	
	public Set<SerialSememe> getSememes() {
		
		return sememes;
	}

	public void setSememes(Set<SerialSememe> sememes) {
		
		A1iciaUtils.checkNotNull(sememes);
		this.sememes = sememes;
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
