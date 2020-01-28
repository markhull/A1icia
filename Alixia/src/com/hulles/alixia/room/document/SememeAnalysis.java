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

import java.util.List;

import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.ticket.SememePackage;

/**
 * The SememeAnalysis is an attempt to determine what actions, if any, are called for by a query
 * from an Alixian somewhere.
 * 
 * @author hulles
 *
 */
public final class SememeAnalysis extends RoomActionObject {
	private List<SememePackage> sememePackages;
	
	public SememeAnalysis() {		
	}

	public List<SememePackage> getSememePackages() {
		
		return sememePackages;
	}

	public void setSememePackages(List<SememePackage> sememePackages) {
		
		SharedUtils.checkNotNull(sememePackages);
		this.sememePackages = sememePackages;
	}

	@Override
	public String getExplanation() {

		return null;
	}

	@Override
	public String getMessage() {

		return null;
	}
	
	
}
