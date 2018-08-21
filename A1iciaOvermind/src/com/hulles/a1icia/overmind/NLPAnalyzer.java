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
package com.hulles.a1icia.overmind;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.jebus.JebusBible;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.jebus.JebusPool;
import com.hulles.a1icia.room.document.NLPAnalysis;
import com.hulles.a1icia.room.document.SentenceAnalysis;
import com.hulles.a1icia.ticket.SentencePackage;
import com.hulles.a1icia.ticket.SentencePackage.SentenceChunk;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.ticket.TicketJournal;
import com.hulles.a1icia.tools.A1iciaUtils;

import redis.clients.jedis.Jedis;

public final class NLPAnalyzer {
	private final static Logger LOGGER = Logger.getLogger("A1iciaOvermind.NLPAnalyzer");
	private final static Level LOGLEVEL = LOGGER.getParent().getLevel();
	private final static long MAXNLPITEMS = 20L;
	
	@SuppressWarnings("resource")
	static void processAnalysis(Ticket ticket, NLPAnalysis analysis) {
		List<String> sentences;
		List<SentenceAnalysis> sentenceAnalyses;
		List<SentencePackage> sentencePackages;
		SentencePackage sentencePackage;
		String listKey;
		TicketJournal journal;
		JebusPool jebusPool;
		
		SharedUtils.checkNotNull(analysis);
		LOGGER.log(LOGLEVEL, "In NLPAnalyzer processAnalysis");
		jebusPool = JebusHub.getJebusLocal();
		
		// First, we save the analysis explanation to Jebus so we can pore over it 
		//    later wearing green eyeshades in a dimly-lit room late at night....
		try (Jedis jebus = jebusPool.getResource()) {
			listKey = JebusBible.getA1iciaNLPKey(jebusPool);
			jebus.lpush(listKey, analysis.getExplanation());
			jebus.ltrim(listKey, 0, MAXNLPITEMS);
		}
		
		sentences = analysis.getSentences();
		sentenceAnalyses = analysis.getSentenceAnalyses();
		
		sentencePackages = new ArrayList<>();
		for (int ix = 0; ix < sentences.size(); ix++) {
			sentencePackage = processSentence(sentences.get(ix), sentenceAnalyses.get(ix));
			sentencePackages.add(sentencePackage);
		}
		
		// Post the sentence packages in the ticket journal
		journal = ticket.getJournal();
		journal.setSentencePackages(sentencePackages);
		journal.setNlpAnalysis(analysis);
		
		// TODO now we should add not-found-by-us NER entities to our own lists
		
	}

	private static SentencePackage processSentence(String sentence, SentenceAnalysis analysis) {
		String lemmatizedSentence;
		String posTagLine;
		List<String> lemmata;
		List<String> posTags;
		String posTag;
		StringBuilder lemmaSb;
		StringBuilder posSb;
		SentencePackage sentencePackage;
		List<String> chunks;
		List<String> chunkTags;
		String tag;
		int tagIx;
		short chunkIx;
		SentenceChunk sentenceChunk;
		List<SentenceChunk> sentenceChunks;
		StringBuilder chunkSb;
		String tagString;
		String fixedSentence;
		
		sentencePackage = new SentencePackage();
		sentencePackage.setInputSentence(sentence);
		fixedSentence = fixOriginalSentence(sentence);
		sentencePackage.setStrippedSentence(fixedSentence);
		sentencePackage.setAnalysis(analysis);
		lemmata  = analysis.getLemmata();
		posTags = analysis.getPOSTags();
		lemmaSb = new StringBuilder();
		posSb = new StringBuilder();
		for (int ix=0; ix<lemmata.size(); ix++) {
			posTag = posTags.get(ix);
			if (posSb.length() > 0) {
				posSb.append(" ");
			}
			posSb.append(posTag);
			if (!posTag.equals(".")) { // sentence terminator, we skip it
				if (lemmaSb.length() > 0) {
					lemmaSb.append(" ");
				}
				lemmaSb.append(lemmata.get(ix));
			}	
		}
		posTagLine = posSb.toString();
		sentencePackage.setPosTagString(posTagLine);
		lemmatizedSentence = lemmaSb.toString();
		sentencePackage.setLemmatizedSentence(lemmatizedSentence);
		
		chunks = analysis.getChunkSegments();
		chunkTags = new ArrayList<>();
		for (String ct : analysis.getChunkTags()) {
			// get rid of the "O" (outside) tags
			if (!ct.equals("O")) {
				chunkTags.add(ct);
			}
		}
		tagIx = 0;
		sentenceChunks = new ArrayList<>(chunkTags.size());
		chunkIx = 0;
		for (String chunk : chunks) {
			if (tagIx >= chunkTags.size()) {
				A1iciaUtils.error("NLPAnalyzer: tags don't correspond to chunks");
				break;
			}
			chunkSb = new StringBuilder();
			sentenceChunk = new SentenceChunk(chunkIx, chunk);
			tag = chunkTags.get(tagIx);
			do {
				if (chunkSb.length() > 0) {
					chunkSb.append(" ");
				}
				chunkSb.append(tag);
				if (++tagIx < chunkTags.size()) {
					tag = chunkTags.get(tagIx);
				} else {
					break;
				}
			} while (tag.startsWith("I-"));
			tagString = chunkSb.toString();
			sentenceChunk.setPosTagString(tagString);
			LOGGER.log(LOGLEVEL, "NLPAnalyzer: chunk = " + chunk + ", tags = " + tagString);
			sentenceChunks.add(chunkIx, sentenceChunk);
			chunkIx++;
		}
		sentencePackage.setChunks(sentenceChunks);
		
		LOGGER.log(LOGLEVEL, "NLPAnalyzer: lemmatizedSentence = " + lemmatizedSentence);
		return sentencePackage;
	}

	@SuppressWarnings("resource")
	public static void dumpAnalyses() {
		String listKey;
		long listCount;
		String analysis;
		JebusPool jebusPool;
		
		jebusPool = JebusHub.getJebusLocal();
		System.out.println("NLP ANALYSES\n\n");
		try (Jedis jebus = jebusPool.getResource()) {
			listKey = JebusBible.getA1iciaNLPKey(jebusPool);
			listCount = jebus.llen(listKey);
			for (int i=0; i<listCount; i++) {
				analysis = jebus.lindex(listKey, i);
				System.out.println(analysis);
				System.out.println("\n\n");
			}
		}
	}
	
	private static String fixOriginalSentence(String sentence) {
		String rTrimmedSentence;
		String lcSentence;
		
		rTrimmedSentence = sentence.replaceAll("\\s*\\p{Punct}+\\s*$", "");
		lcSentence = rTrimmedSentence.toLowerCase();
		return lcSentence;
	}
		
}
