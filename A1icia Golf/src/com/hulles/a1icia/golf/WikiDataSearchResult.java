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
package com.hulles.a1icia.golf;

import com.hulles.a1icia.tools.A1iciaUtils;

public class WikiDataSearchResult {
	private final String qID;
	private final String label;
	private final String description;

	public WikiDataSearchResult(String qID, String label, String description) {

		A1iciaUtils.checkNotNull(qID);
		A1iciaUtils.checkNotNull(label);
		A1iciaUtils.checkNotNull(description);
		this.qID = qID;
		this.label = label;
		this.description = description;
	}

	public String getqID() {
		
		return qID;
	}

	public String getLabel() {
		
		return label;
	}

	public String getDescription() {
		
		return description;
	}
	
}
