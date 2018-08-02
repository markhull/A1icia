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

import java.util.List;

import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * The SememeAnalysis is an attempt to determine what actions, if any, are called for by a query
 * from an A1ician somewhere.
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
		
		A1iciaUtils.checkNotNull(sememePackages);
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
