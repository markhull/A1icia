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
package com.hulles.alixia.juliet;

import java.util.List;

import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.room.document.RoomActionObject;

public class JulietAnalysis extends RoomActionObject {
	private List<ScratchNfl6Question> fwQuestionList;
	private List<ScratchNfl6Question> fmQuestionList;
	private Long fwElapsed;
	private Long fmElapsed;
	private String message;
	
	public List<ScratchNfl6Question> getFWQuestionList() {
		
		return fwQuestionList;
	}

	public void setFWQuestionList(List<ScratchNfl6Question> questionList) {
		
		SharedUtils.checkNotNull(questionList);
		this.fwQuestionList = questionList;
	}

	public List<ScratchNfl6Question> getFMQuestionList() {
		
		return fmQuestionList;
	}

	public void setFMQuestionList(List<ScratchNfl6Question> questionList) {
		
		SharedUtils.checkNotNull(questionList);
		this.fmQuestionList = questionList;
	}

	public Long getFWElapsed() {
		
		return fwElapsed;
	}

	public void setFWElapsed(Long elapsed) {
		
		SharedUtils.checkNotNull(elapsed);
		this.fwElapsed = elapsed;
	}

	public Long getFMElapsed() {
		
		return fmElapsed;
	}

	public void setFMElapsed(Long elapsed) {
		
		SharedUtils.checkNotNull(elapsed);
		this.fmElapsed = elapsed;
	}
	
	@Override
	public String getMessage() {
		
		return message;
	}

	public void setMessage(String message) {
		
		SharedUtils.nullsOkay(message);
		this.message = message;
	}


	@Override
	public String getExplanation() {
		StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append("<h2>FuzzyWuzzy Results</h2>\n\n;");
		for (ScratchNfl6Question q : fwQuestionList) {
			sb.append("Main Category: ");
			sb.append(q.getMainCategory());
			sb.append("\nQuestion: ");
			sb.append(q.getQuestion());
			sb.append("\nBest Answer: ");
			sb.append(q.getBestAnswer());
			sb.append("\nScore: ");
			sb.append(q.getScore());
			sb.append("\nN Best Answers: ");
			for (String a : q.getnBestAnswers()) {
				sb.append("\n\t");
				sb.append(a);
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
