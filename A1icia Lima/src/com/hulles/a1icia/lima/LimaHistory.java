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
package com.hulles.a1icia.lima;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.cayenne.A1iciaApplication;
import com.hulles.a1icia.cayenne.AnswerChunk;
import com.hulles.a1icia.cayenne.AnswerHistory;
import com.hulles.a1icia.cayenne.Sememe;
import com.hulles.a1icia.room.document.HistoryUpdate;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SentencePackage;
import com.hulles.a1icia.ticket.SentencePackage.SentenceChunk;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.ticket.TicketJournal;
import com.hulles.a1icia.tools.FuzzyMatch;
import com.hulles.a1icia.tools.A1iciaUtils;

public class LimaHistory {
	private final static Logger logger = Logger.getLogger("A1iciaLima.LimaHistory");
	private final static Level LOGLEVEL_A = Level.FINE;
	private final static Level LOGLEVEL_B = Level.FINE;
	private final static int CUTOFFSCORE = 95;
	private List<AnswerHistory> history;
	
	LimaHistory() {
		
	}
	
	void loadHistory() {
		
		history = AnswerHistory.getAllAnswerHistory();
	}
	
	List<ScratchAnswerHistory> getMatchingHistory(SentencePackage sentencePackage) {
		int ratio;
		List<ScratchAnswerHistory> serialHistory;
		ScratchAnswerHistory serialItem;
		String fixedSentence;
		boolean exactMatch;
		String lemmatizedSentence;
		
		serialHistory = new ArrayList<>();
		lemmatizedSentence = sentencePackage.getLemmatizedSentence();
		fixedSentence = sentencePackage.getStrippedSentence();
		
		// first we try for an exact match to our original question
		for (AnswerHistory item : history) {
			exactMatch = item.getOriginalQuestion().equals(fixedSentence);
			if (exactMatch) {
				logger.log(LOGLEVEL_A, "\n\nSERIALHISTORY: exact match for " + fixedSentence);
				serialItem = new ScratchAnswerHistory();
				serialItem.setHistory(item);
				serialItem.setScore(100);
				serialHistory.add(serialItem);
				return serialHistory;
			}
		}
		
		// next we do a fuzzy match on the lemmatized sentence
		for (AnswerHistory item : history) {
			ratio = FuzzyMatch.getRatio(lemmatizedSentence, item.getLemmatizedQuestion(), false);
			if (ratio > CUTOFFSCORE) {
				serialItem = new ScratchAnswerHistory();
				serialItem.setHistory(item);
				serialItem.setScore(ratio);
				serialHistory.add(serialItem);
			}
		}
		if (serialHistory.isEmpty()) {
			return serialHistory;
		}
		
		Collections.sort(serialHistory, new Comparator<ScratchAnswerHistory>() {
			
			@Override
			public int compare(ScratchAnswerHistory o1, ScratchAnswerHistory o2) {
				String q1;
				String q2;

				if (o1.getScore() == o2.getScore()) {
					// if the scores are equal, return the longest match
					q1 = o1.getHistory().getLemmatizedQuestion();
					q2 = o2.getHistory().getLemmatizedQuestion();
					return q2.length() - q1.length();
				}
				return o2.getScore().compareTo(o1.getScore());
			}
		});
		logger.log(LOGLEVEL_A, "\n\nSERIALHISTORY:\n");
		for (ScratchAnswerHistory sah : serialHistory) {
			logger.log(LOGLEVEL_A, sah.getHistory().getLemmatizedQuestion());
			logger.log(LOGLEVEL_A, " :: " + sah.getScore());
		}
		return serialHistory;
	}
	
	void addHistory(HistoryUpdate request) {
		Ticket ticket;
		TicketJournal journal;
		List<SentencePackage> sentencePackages;
		SememePackage sememePkg;
		List<ActionPackage> actionPackages;
		SentencePackage spPkg;
		
		A1iciaUtils.checkNotNull(request);
		ticket = request.getTicket();
		journal = ticket.getJournal();
		sentencePackages = journal.getSentencePackages();
		actionPackages = journal.getActionPackages();
		// TODO we should really figure out how to not duplicate items in the database;
		//    it's not necessarily as simple as checking for duplicate entries by original
		//    sentence or lemmatized sentence, since we may want to track different satisfaction
		//    ratings based on result action e.g.
		// ... 
		//    Okay fine, it is that simple for now. TODO make me better
		logger.log(LOGLEVEL_B, "LimaHistory: in addHistory");
		for (SentencePackage sp : sentencePackages) {
			for (ActionPackage ap : actionPackages) {
				logger.log(LOGLEVEL_B, "LimaHistory: got action package " + ap.getName());
				sememePkg = ap.getSememePackage();
				logger.log(LOGLEVEL_B, "LimaHistory: got sememe package " + sememePkg.getName());
				spPkg = sememePkg.getSentencePackage();
				if (spPkg != null) {
					if (spPkg.getSentencePackageID().equals(sp.getSentencePackageID())) {
						logger.log(LOGLEVEL_B, "LimaHistory: got matching sentence package " +
								sememePkg.getName());
						update(sp, ap);
						// we could do a break, but let's let it run in case we ever have more
						//    than one action per sentence
					}
				}
			}
		}
	}

	private void update(SentencePackage sp, ActionPackage ap) {
		SememePackage sememePkg;
		AnswerHistory answerHistory;
		List<SentenceChunk> chunks;
		AnswerChunk answerChunk;
		String fixedSentence;
		
		A1iciaUtils.checkNotNull(sp);
		A1iciaUtils.checkNotNull(ap);
		sememePkg = ap.getSememePackage();
		fixedSentence = sp.getStrippedSentence();
		logger.log(LOGLEVEL_B, "LimaHistory: updating");
		answerHistory = AnswerHistory.findAnswerHistory(fixedSentence);
		if (answerHistory != null) {
			// we already have it
			return;
		}
		logger.log(LOGLEVEL_B, "LimaHistory: should be updating database with " + sememePkg);
		A1iciaApplication.setErrorOnUncommittedObjects(false);
		answerHistory = AnswerHistory.createNew();
		answerHistory.setSememe(Sememe.fromSerial(sememePkg.getSememe()));
		answerHistory.setSememeObject(sememePkg.getSememeObject());
		answerHistory.setLemmatizedQuestion(sp.getLemmatizedSentence());
		
		answerHistory.setPosTags(sp.getPosTagString());
		answerHistory.setOriginalQuestion(fixedSentence);
		answerHistory.setSatisfaction(0); // FIXME how do we get this?
		logger.log(LOGLEVEL_B, "LimaHistory: should have updated database with " + sememePkg);
		
		chunks = sp.getChunks();
		if (chunks != null) {
			for (SentenceChunk chunk : chunks) {
				answerChunk = AnswerChunk.createNew();
				answerChunk.setAnswerHistory(answerHistory);
				answerChunk.setSequence(chunk.getSequence());
				answerChunk.setChunk(chunk.getChunk());
				answerChunk.setChunkTags(chunk.getPosTagString());
			}
		}
		answerHistory.commit();
		A1iciaApplication.setErrorOnUncommittedObjects(true);
		// add this to our list copy of database AnswerHistory tuples that
		//    we use in our analysis lookup, since we have it here
		history.add(answerHistory);
	}
	
}
