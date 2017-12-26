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
		
		A1iciaUtils.checkNotNull(inputMessage);
		this.inputMessage = inputMessage;
	}

	public List<String> getSentences() {
		
		return sentences;
	}

	public void setSentences(List<String> sentences) {
		
		A1iciaUtils.checkNotNull(sentences);
		this.sentences = sentences;
	}

	public List<SentenceAnalysis> getSentenceAnalyses() {
		
		return sentenceAnalyses;
	}

	public void setSentenceAnalyses(List<SentenceAnalysis> sentenceAnalyses) {
		
		A1iciaUtils.checkNotNull(sentenceAnalyses);
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
