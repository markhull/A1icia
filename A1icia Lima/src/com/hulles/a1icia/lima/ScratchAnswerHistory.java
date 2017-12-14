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
package com.hulles.a1icia.lima;

import com.hulles.a1icia.cayenne.AnswerHistory;
import com.hulles.a1icia.tools.A1iciaUtils;

public class ScratchAnswerHistory {
	private Integer score;
	private AnswerHistory history;

	public Integer getScore() {
		
		return score;
	}

	public void setScore(Integer score) {
		
		A1iciaUtils.checkNotNull(score);
		this.score = score;
	}

	public AnswerHistory getHistory() {
		
		return history;
	}

	public void setHistory(AnswerHistory history) {
		
		A1iciaUtils.checkNotNull(history);
		this.history = history;
	}
	
}
