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

import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * The SparkAnalysis is an attempt to determine what actions, if any, are called for by a query
 * from an A1ician somewhere.
 * 
 * @author hulles
 *
 */
public final class SparkAnalysis extends RoomActionObject {
	private List<SparkPackage> sparkPackages;
	
	public SparkAnalysis() {		
	}

	public List<SparkPackage> getSparkPackages() {
		
		return sparkPackages;
	}

	public void setSparkPackages(List<SparkPackage> sparkPackages) {
		
		A1iciaUtils.checkNotNull(sparkPackages);
		this.sparkPackages = sparkPackages;
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
