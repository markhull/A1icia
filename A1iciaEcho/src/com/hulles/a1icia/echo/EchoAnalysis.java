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
package com.hulles.a1icia.echo;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.room.document.RoomActionObject;

public class EchoAnalysis extends RoomActionObject {
	private String matchingResult;
	private String analogyResult;
	private final VectorLoad whichVector;
	
	public EchoAnalysis(VectorLoad vector) {
		
		SharedUtils.checkNotNull(vector);
		this.whichVector = vector;
	}
	
	@Override
	public String getMessage() {

		return matchingResult;
	}
	
	public String getMatchingResult() {

		return matchingResult;
	}

	public void setMatchingResult(String result) {
		
		SharedUtils.checkNotNull(result);
		this.matchingResult = result;
	}

	public String getAnalogyResult() {
		
		return analogyResult;
	}
	
	public void setAnalogyResult(String result) {
	
		SharedUtils.checkNotNull(result);
		this.analogyResult = result;
	}
	
	@Override
	public String getExplanation() {
		StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append("Matching Result:\n");
		sb.append(getMatchingResult());
		sb.append("\n\nAnalogy Result:\n");
		sb.append(getAnalogyResult());
		return sb.toString();
	}

	public VectorLoad getWhichVector() {
		
		return whichVector;
	}

	public enum VectorLoad {
		BIGJOAN,
		FREEBASE,
		GOOGLENEWS,
		LITTLEGINA
	}
	
}
