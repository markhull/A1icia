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

import com.hulles.a1icia.tools.A1iciaUtils;
import com.hulles.a1icia.tools.FuzzyMatch.Match;

/**
 * This class exists to answer the question (e.g.) "What artists or titles that we know about, if
 * any, match the input query?".
 * 
 * @author hulles
 *
 */
public class MediaAnalysis extends RoomActionObject {
	private List<Match> artists;
	private List<Match> titles;
	private String inputToMatch;
	
	public List<Match> getArtists() {
		
		return artists;
	}
	
	public void setArtists(List<Match> artists) {
		
		A1iciaUtils.checkNotNull(artists);
		this.artists = artists;
	}
	
	public List<Match> getTitles() {
		
		return titles;
	}
	
	public void setTitles(List<Match> titles) {
	
		A1iciaUtils.checkNotNull(titles);
		this.titles = titles;
	}
	
	public String getInputToMatch() {
		
		return inputToMatch;
	}
	
	public void setInputToMatch(String inputToMatch) {
	
		A1iciaUtils.checkNotNull(inputToMatch);
		this.inputToMatch = inputToMatch;
	}

	@Override
	public String getMessage() {

		return getExplanation();
	}

	@Override
	public String getExplanation() {
		StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append("Matching Artists: \n");
		for (Match artist : artists) {
			sb.append(artist.getString());
			sb.append(": ");
			sb.append(artist.getRatio());
			sb.append("\n");
		}
		sb.append("\n\nMatching Titles: \n");
		for (Match title : titles) {
			sb.append(title.getString());
			sb.append(": ");
			sb.append(title.getRatio());
			sb.append("\n");
		}
		return sb.toString();
	}
	
}
