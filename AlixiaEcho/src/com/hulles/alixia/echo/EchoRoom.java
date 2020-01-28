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
package com.hulles.alixia.echo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.ApplicationKeys;
import com.hulles.alixia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.echo.EchoAnalysis.VectorLoad;
import com.hulles.alixia.echo.w2v.WordDistance;
import com.hulles.alixia.echo.w2v.WordToVecSearch;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;

/**
 * Echo Room analyzes input with the venerable Word2Vec processes. 
 * 
 * @author hulles
 *
 */
public final class EchoRoom extends UrRoom {
	final static Logger LOGGER = LoggerFactory.getLogger(EchoRoom.class);
	private final static String DISTANCE_FORMAT = "(%.4f)";
	final static VectorLoad WHICHLOAD = VectorLoad.LITTLEGINA;
	final WordToVecSearch searcher;
	volatile boolean ready = false;

	public EchoRoom() {
		super();
		
		searcher = new WordToVecSearch();
	}
	
	public String matchWordOrPhrase(String clientMsg) {
		List<WordDistance> matches;
		
		SharedUtils.checkNotNull(clientMsg);
		if (searcher == null) {
			return null;
		}
		try {
			matches = searcher.getWordMatches(clientMsg, 20);
		} catch (AlixiaException e) {
			return null;
		}
		return formatResult(matches);
	}
	
	public String finishAnalogy(String word1, String word2, String word3) {
		List<WordDistance> matches;
		
		SharedUtils.checkNotNull(word1);
		SharedUtils.checkNotNull(word2);
		SharedUtils.checkNotNull(word3);
		if (searcher == null) {
			return null;
		}
		try {
			matches = searcher.getAnalogy(word1, word2, word3, 8);
		} catch (AlixiaException e) {
			return null;
		}
		return formatResult(matches);
	}
	
	private static String formatResult(List<WordDistance> matches) {
		StringBuilder sb;
		
		sb = new StringBuilder();
		for (WordDistance match : matches) {
			if (match.getToWord().equals("init")) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(match.getToWord());
			sb.append(" ");
			sb.append(String.format(DISTANCE_FORMAT, match.getDistance()));
		}
		sb.append("\n");
		return sb.toString();
	}

	@Override
	public Room getThisRoom() {

		return Room.ECHO;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new AlixiaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {
		Thread loader;
		ApplicationKeys appKeys;
		
		appKeys = ApplicationKeys.getInstance();
		loader = new Thread() {
			@Override
			public void run() {
				String w2vPath;
				
				switch (WHICHLOAD) {
					case LITTLEGINA:
						LOGGER.debug("Starting w2v load <-- Little Gina");
						w2vPath = appKeys.getKey(ApplicationKey.LITTLEGINA);
						break;
					case GOOGLENEWS:
						LOGGER.debug("Starting w2v load <-- Google News");
						w2vPath = appKeys.getKey(ApplicationKey.GOOGLENEWS);
						break;
					case FREEBASE:
						LOGGER.debug("Starting w2v load <-- Freebase");
						w2vPath = appKeys.getKey(ApplicationKey.FREEBASE);
						break;
					case BIGJOAN:
						LOGGER.debug("Starting w2v load <-- Big Joan");
						w2vPath = appKeys.getKey(ApplicationKey.BIGJOAN);
						break;
					default:
						throw new AlixiaException("Bad word2vec path");
				}
				searcher.loadFile(w2vPath);
				LOGGER.debug("Finished w2v load");
				ready = true;
			}
		};
		LOGGER.debug("Spawning w2v load");
		loader.start();
	}

	@Override
	protected void roomShutdown() {
		
	}
	
	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "match_word_or_phrase":
				return createMatchesActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
		}
	}

	private ActionPackage createMatchesActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		EchoAnalysis analysis;
		WordMatchingRequest analysisRequest;
		String wordToMatch;
		String result;
		String wordA;
		String wordB;
		String wordC;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		analysis = new EchoAnalysis(WHICHLOAD);
		analysisRequest = (WordMatchingRequest) request.getRoomObject();
		if (!ready) {
			result = "Echo is not yet ready";
		} else {
			wordToMatch = analysisRequest.getWordToMatch();
			if (wordToMatch == null || wordToMatch.isEmpty()) {
				result = "No matching input";
			} else {
				wordToMatch = wordToMatch.replace(" ", "_");
				result = matchWordOrPhrase(wordToMatch);
				if (result == null) {
					result = "No matching results";
				} else {
					result = result.replace("_", " ");
				}
			}
		}
		analysis.setMatchingResult(result);
		if (!ready) {
			result = "Echo is not yet ready";
		} else {
			wordA = analysisRequest.getAnalogyWord();
			wordB = analysisRequest.getAnalogyIsTo();
			wordC = analysisRequest.getAnalogyAs();
			if (wordA == null || wordA.isEmpty() || wordB == null || wordB.isEmpty() ||
					wordC == null || wordC.isEmpty()) {
				result = "No client message";
			} else {
				result = finishAnalogy(wordA, wordB, wordC);
				if (result == null) {
					result = "No matching results";
				}
			}
		}
		analysis.setAnalogyResult(result);
		pkg.setActionObject(analysis);
		return pkg;
	}
	
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("match_word_or_phrase"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
