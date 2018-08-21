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

import java.util.List;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.room.document.RoomActionObject;

public class GolfAnalysis extends RoomActionObject {
	private String message;
	private String explanation;
	private List<WikiDataSearchResult> searchResults;
	
	@Override
	public String getMessage() {

		return message;
	}

	public void setMessage(String msg) {
		
		SharedUtils.checkNotNull(msg);
		this.message = msg;
	}

	public List<WikiDataSearchResult> getSearchResults() {
		
		return searchResults;
	}

	public void setSearchResults(List<WikiDataSearchResult> searchResults) {
		
		SharedUtils.checkNotNull(searchResults);
		this.searchResults = searchResults;
	}

	@Override
	public String getExplanation() {

		return explanation;
	}

	public void setExplanation(String explanation) {
		
		SharedUtils.checkNotNull(explanation);
		this.explanation = explanation;
	}

}
