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

import java.util.ArrayList;
import java.util.List;

import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaTimer;
import com.hulles.alixia.cayenne.NbestAnswer;
import com.hulles.alixia.cayenne.Nfl6Question;
import com.hulles.alixia.juliet.fuzzywuzzy.FuzzySearch;
import com.hulles.alixia.juliet.fuzzywuzzy.model.ExtractedResult;
import com.hulles.alixia.tools.FuzzyMatch;

public final class JulietResponder {
	private final List<String> nfl6Questions;
	
	public JulietResponder() {
		
		nfl6Questions = new ArrayList<>(90000);
		prepQAs();
	}
	
	public void getFWMatchingQuestions(String input, int bestNAnswers,
			JulietAnalysis queryResponse) {
		List<ExtractedResult> results;
		List<ScratchNfl6Question> serialQuestions;
		ScratchNfl6Question serialQuestion;
		List<NbestAnswer> dbNBestAnswers;
		List<String> serialAnswers;
		List<Nfl6Question> dbQuestions;
		Long et;
		
		SharedUtils.checkNotNull(input);
		SharedUtils.checkNotNull(bestNAnswers);
		AlixiaTimer.startTimer("JulietFW");
		results = FuzzySearch.extractTop(input, nfl6Questions, bestNAnswers);
		serialQuestions = new ArrayList<>(results.size());
		for (ExtractedResult result : results) {
			// getNfl6Question might return multiple iterations of the same question
			//    with, presumably, different answers; as a result, you might ask for 3 bestNAnswers
			//    and receive 196 serial question objects back from this method.... TODO find a way
			//    to winnow the set of questions resulting from e.g. "What is the meaning of life?" 
			//    to the n best, n being a small integer....
			dbQuestions = Nfl6Question.getNfl6Question(result.getString());
			for (Nfl6Question dbQuestion : dbQuestions) {
				serialQuestion = new ScratchNfl6Question();
				serialQuestion.setQuestion(dbQuestion.getQuestion());
				serialQuestion.setBestAnswer(dbQuestion.getBestAnswer());
				serialQuestion.setMainCategory(dbQuestion.getNfl6Category().getCategoryName());
				serialQuestion.setScore(result.getScore());
				dbNBestAnswers = dbQuestion.getNbestAnswers();
				serialAnswers = new ArrayList<>(dbNBestAnswers.size());
				for (NbestAnswer dbAnswer : dbNBestAnswers) {
					serialAnswers.add(dbAnswer.getAnswer());
				}
				serialQuestion.setnBestAnswers(serialAnswers);
				serialQuestions.add(serialQuestion);
			}
		}
		et = AlixiaTimer.stopTimer("JulietFW");
		queryResponse.setFWElapsed(et);
		queryResponse.setFWQuestionList(serialQuestions);
	}
	
	public void getFMMatchingQuestions(String input, int bestNAnswers,
			JulietAnalysis queryResponse) {
		List<ScratchNfl6Question> serialQuestions;
		ScratchNfl6Question serialQuestion;
		List<NbestAnswer> dbNBestAnswers;
		List<String> serialAnswers;
		List<Nfl6Question> dbQuestions;
		Long et;
		List<String> topQuestions;
		List<Integer> topRatios;
		int ratio;
		int lastRatioIx;
		
		SharedUtils.checkNotNull(input);
		SharedUtils.checkNotNull(bestNAnswers);
		AlixiaTimer.startTimer("JulietFM");		
		lastRatioIx = bestNAnswers - 1;
		topQuestions = new ArrayList<>(bestNAnswers);
		topRatios = new ArrayList<>(bestNAnswers);
		for (int i=0; i<bestNAnswers; i++) {
			topQuestions.add("");
			topRatios.add(0);
		}
		for (String question : nfl6Questions) {
			ratio = FuzzyMatch.getRatio(input, question, false);
			for (int i=0; i<bestNAnswers; i++) {
				if (ratio > topRatios.get(i)) {
					topRatios.set(i, ratio);
					topQuestions.set(i, question);
					continue;
				}
				if (topRatios.get(lastRatioIx) == 100) {
					// all the buckets are maxed out
					break;
				}
			}
		}
		serialQuestions = new ArrayList<>(bestNAnswers);
		for (int i=0; i<bestNAnswers; i++) {
			// getNfl6Question might return multiple iterations of the same question
			//    with, presumably, different answers; as a result, you might ask for 3 bestNAnswers
			//    and receive 196 serial question objects back from this method.... TODO find a way
			//    to winnow the set of questions resulting from e.g. "What is the meaning of life?" 
			//    to the n best, n being a small integer....
			dbQuestions = Nfl6Question.getNfl6Question(topQuestions.get(i));
			for (Nfl6Question dbQuestion : dbQuestions) {
				serialQuestion = new ScratchNfl6Question();
				serialQuestion.setQuestion(dbQuestion.getQuestion());
				serialQuestion.setBestAnswer(dbQuestion.getBestAnswer());
				serialQuestion.setMainCategory(dbQuestion.getNfl6Category().getCategoryName());
				serialQuestion.setScore(topRatios.get(i));
				dbNBestAnswers = dbQuestion.getNbestAnswers();
				serialAnswers = new ArrayList<>(dbNBestAnswers.size());
				for (NbestAnswer dbAnswer : dbNBestAnswers) {
					serialAnswers.add(dbAnswer.getAnswer());
				}
				serialQuestion.setnBestAnswers(serialAnswers);
				serialQuestions.add(serialQuestion);
			}
		}
		et = AlixiaTimer.stopTimer("JulietFM");
		queryResponse.setFMElapsed(et);
		queryResponse.setFMQuestionList(serialQuestions);
	}
	
	private void prepQAs() {
		List<Nfl6Question> dbQuestions;
		
		dbQuestions = Nfl6Question.getAllNfl6Questions();
		dbQuestions.stream().forEach(q -> nfl6Questions.add(q.getQuestion()));
	}
}
