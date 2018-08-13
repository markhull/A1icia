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

import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * The semantic analysis of a sentence as requested by a SentenceMeaningRequest. This is still
 * pretty fluid and we expect it to evolve soon.
 * 
 * @see SentenceMeaningRequest
 * 
 * @author hulles
 *
 */
public class SentenceMeaningAnalysis extends RoomActionObject {
	private Integer confidence;
	private Boolean result;
	private SentenceMeaningQuery query;
	
	public SentenceMeaningAnalysis(SentenceMeaningQuery query) {
		
		A1iciaUtils.checkNotNull(query);
		this.query = query;
	}

	public SentenceMeaningQuery getSentenceMeaningQuery() {
		
		return query;
	}

	public Integer getConfidence() {
		
		return confidence;
	}

	public void setConfidence(Integer confidence) {
		
		A1iciaUtils.checkNotNull(confidence);
		this.confidence = confidence;
	}

	public Boolean getResult() {
		
		return result;
	}

	public void setResult(Boolean result) {
		
		A1iciaUtils.checkNotNull(result);
		this.result = result;
	}

	@Override
	public String getExplanation() {
		StringBuilder sb;
		
		sb = new StringBuilder(getMessage());
		sb.append("\nConfidence that the result is correct is ");
		sb.append(confidence);
		sb.append("%.");
		return sb.toString();
	}

	@Override
	public String getMessage() {
		StringBuilder sb;
		
		sb = new StringBuilder("Sentence ");
		sb.append(result ? "is" : "is not");
		sb.append(" a/an ");
		sb.append(query);
		sb.append(" sentence.");
		return sb.toString();
	}

	public enum SentenceMeaningQuery {
		IMAGE_CLASSIFICATION
	}
}
