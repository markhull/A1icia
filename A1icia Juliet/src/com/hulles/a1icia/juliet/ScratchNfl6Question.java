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
package com.hulles.a1icia.juliet;

import java.io.Serializable;
import java.util.List;

import com.hulles.a1icia.tools.A1iciaUtils;

public class ScratchNfl6Question implements Serializable {
	private static final long serialVersionUID = -776982529628460706L;
	private String question;
	private String bestAnswer;
	private String mainCategory;
	private List<String> nBestAnswers;
	private int score;
	
	public ScratchNfl6Question() {
		// needs no-arg constructor
	}

	public String getQuestion() {
		
		return question;
	}

	public void setQuestion(String question) {
		
		A1iciaUtils.checkNotNull(question);
		this.question = question;
	}

	public String getBestAnswer() {
		
		return bestAnswer;
	}

	public void setBestAnswer(String bestAnswer) {
		
		A1iciaUtils.checkNotNull(bestAnswer);
		this.bestAnswer = bestAnswer;
	}

	public String getMainCategory() {
		
		return mainCategory;
	}

	public void setMainCategory(String mainCategory) {
		
		A1iciaUtils.checkNotNull(mainCategory);
		this.mainCategory = mainCategory;
	}

	public List<String> getnBestAnswers() {
		
		return nBestAnswers;
	}

	public void setnBestAnswers(List<String> nBestAnswers) {
		
		A1iciaUtils.checkNotNull(nBestAnswers);
		this.nBestAnswers = nBestAnswers;
	}

	public int getScore() {
		
		return score;
	}

	public void setScore(int score) {
		
		A1iciaUtils.checkNotNull(score);
		this.score = score;
	}
	
	
}
