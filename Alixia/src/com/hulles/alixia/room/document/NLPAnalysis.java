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

/**
 * A RoomActionObject that contains the result of our Natural Language Processing (NLP) analysis.
 * 
 * @author hulles
 *
 */
final public class NLPAnalysis extends RoomActionObject {
	private String inputMessage;
	private List<String> sentences;
	private List<SentenceAnalysis> sentenceAnalyses;

	@Override
	public String getMessage() {
		
		return inputMessage;
	}

	public void setMessage(String inputMessage) {
		
		SharedUtils.checkNotNull(inputMessage);
		this.inputMessage = inputMessage;
	}

	public List<String> getSentences() {
		
		return sentences;
	}

	public void setSentences(List<String> sentences) {
		
		SharedUtils.checkNotNull(sentences);
		this.sentences = sentences;
	}

	public List<SentenceAnalysis> getSentenceAnalyses() {
		
		return sentenceAnalyses;
	}

	public void setSentenceAnalyses(List<SentenceAnalysis> sentenceAnalyses) {
		
		SharedUtils.checkNotNull(sentenceAnalyses);
		this.sentenceAnalyses = sentenceAnalyses;
	}
	
	@Override
	public String getExplanation() {
		StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append("<style type='text/css'>");
		sb.append("dt {");
        sb.append("font-weight: bold;");
    	sb.append("}");
		sb.append("</style>\n");
		sb.append("<h3>INPUT ANALYSIS</h3>");
		sb.append("<dl style=''>\n");
		
		sb.append("<dt>SENTENCES</dt>\n");
		sb.append("<dd>");
		for (String s : sentences) {
			sb.append(s + "<br />\n");
		}
		sb.append("</dd></dl>\n");
		
		for (SentenceAnalysis csa : sentenceAnalyses) {
			sb.append(csa.toString());
		}
		return sb.toString();
	}

}
