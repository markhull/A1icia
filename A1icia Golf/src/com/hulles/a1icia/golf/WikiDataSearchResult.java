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
package com.hulles.a1icia.golf;

import com.hulles.a1icia.api.shared.SharedUtils;

public class WikiDataSearchResult {
	private final String qID;
	private final String label;
	private final String description;

	public WikiDataSearchResult(String qID, String label, String description) {

		SharedUtils.checkNotNull(qID);
		SharedUtils.checkNotNull(label);
		SharedUtils.checkNotNull(description);
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
