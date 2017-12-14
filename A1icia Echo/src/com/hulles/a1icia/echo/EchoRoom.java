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
package com.hulles.a1icia.echo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.ApplicationKeys;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.Spark;
import com.hulles.a1icia.echo.EchoAnalysis.VectorLoad;
import com.hulles.a1icia.echo.w2v.WordDistance;
import com.hulles.a1icia.echo.w2v.WordToVecSearch;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * Echo Room analyzes input with the venerable Word2Vec processes. 
 * 
 * @author hulles
 *
 */
public final class EchoRoom extends UrRoom {
	final static Logger LOGGER = Logger.getLogger("A1iciaEcho.EchoRoom");
	final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final static String DISTANCE_FORMAT = "(%.4f)";
	final static VectorLoad WHICHLOAD = VectorLoad.LITTLEGINA;
	final WordToVecSearch searcher;
	volatile boolean ready = false;

	public EchoRoom(EventBus bus) {
		super(bus);
		
		searcher = new WordToVecSearch();
	}
	
	public String matchWordOrPhrase(String clientMsg) {
		List<WordDistance> matches;
		
		A1iciaUtils.checkNotNull(clientMsg);
		if (searcher == null) {
			return null;
		}
		try {
			matches = searcher.getWordMatches(clientMsg, 20);
		} catch (EchoWordToVecException e) {
			return null;
		}
		return formatResult(matches);
	}
	
	public String finishAnalogy(String word1, String word2, String word3) {
		List<WordDistance> matches;
		
		A1iciaUtils.checkNotNull(word1);
		A1iciaUtils.checkNotNull(word2);
		A1iciaUtils.checkNotNull(word3);
		if (searcher == null) {
			return null;
		}
		try {
			matches = searcher.getAnalogy(word1, word2, word3, 8);
		} catch (EchoWordToVecException e) {
			return null;
		}
		return formatResult(matches);
	}
	
	public static String formatResult(List<WordDistance> matches) {
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
		throw new A1iciaException("Response not implemented in " + 
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
				switch (WHICHLOAD) {
					case LITTLEGINA:
						LOGGER.log(LOGLEVEL, "Starting w2v load <-- Little Gina");
						searcher.loadFile(appKeys.getLittleGinaPath());
						break;
					case GOOGLENEWS:
						LOGGER.log(LOGLEVEL, "Starting w2v load <-- Google News");
						searcher.loadFile(appKeys.getGoogleNewsPath());
						break;
					case FREEBASE:
						LOGGER.log(LOGLEVEL, "Starting w2v load <-- Freebase");
						searcher.loadFile(appKeys.getFreebasePath());
						break;
					case BIGJOAN:
						LOGGER.log(LOGLEVEL, "Starting w2v load <-- Big Joan");
						searcher.loadFile(appKeys.getBigJoanPath());
						break;
					default:
						break;
				}
				LOGGER.log(LOGLEVEL, "Finished w2v load");
				ready = true;
			}
		};
		LOGGER.log(LOGLEVEL, "Spawning w2v load");
		loader.start();
	}

	@Override
	protected void roomShutdown() {
		
	}
	
	@Override
	protected ActionPackage createActionPackage(SparkPackage sparkPkg, RoomRequest request) {

		switch (sparkPkg.getName()) {
			case "match_word_or_phrase":
				return createMatchesActionPackage(sparkPkg, request);
			default:
				throw new A1iciaException("Received unknown spark in " + getThisRoom());
		}
	}

	private ActionPackage createMatchesActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		ActionPackage pkg;
		EchoAnalysis analysis;
		WordMatchingRequest analysisRequest;
		String wordToMatch;
		String result;
		String wordA;
		String wordB;
		String wordC;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sparkPkg);
		analysis = new EchoAnalysis(WHICHLOAD);
		analysisRequest = (WordMatchingRequest) request.getRoomObject();
		if (!ready) {
			result = "Echo is not yet ready";
		} else {
			wordToMatch = analysisRequest.getWordToMatch();
			if (wordToMatch == null || wordToMatch.isEmpty()) {
				result = "No matching input";
			} else {
				wordToMatch.replace(" ", "_");
				result = matchWordOrPhrase(wordToMatch);
				if (result == null) {
					result = "No matching results";
				} else {
					result.replace("_", " ");
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
	protected Set<Spark> loadSparks() {
		Set<Spark> sparks;
		
		sparks = new HashSet<>();
		sparks.add(Spark.find("match_word_or_phrase"));
		return sparks;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}