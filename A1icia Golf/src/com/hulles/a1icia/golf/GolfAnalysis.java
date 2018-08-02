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

import java.util.List;

import com.hulles.a1icia.room.document.RoomActionObject;
import com.hulles.a1icia.tools.A1iciaUtils;

public class GolfAnalysis extends RoomActionObject {
	private String message;
	private String explanation;
	private List<WikiDataSearchResult> searchResults;
	
	@Override
	public String getMessage() {

		return message;
	}

	public void setMessage(String msg) {
		
		A1iciaUtils.checkNotNull(msg);
		this.message = msg;
	}

	public List<WikiDataSearchResult> getSearchResults() {
		
		return searchResults;
	}

	public void setSearchResults(List<WikiDataSearchResult> searchResults) {
		
		A1iciaUtils.checkNotNull(searchResults);
		this.searchResults = searchResults;
	}

	@Override
	public String getExplanation() {

		return explanation;
	}

	public void setExplanation(String explanation) {
		
		A1iciaUtils.checkNotNull(explanation);
		this.explanation = explanation;
	}

}
