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
package com.hulles.alixia.charlie.parse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.ApplicationKeys;
import com.hulles.alixia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.alixia.api.shared.SharedUtils;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.util.Sequence;
import opennlp.tools.util.Span;

final public class CharlieChunker {
	private final static Logger LOGGER = LoggerFactory.getLogger(CharlieChunker.class);
	private final ChunkerME chunker;

	public CharlieChunker() {
		URL chunkerModelURL;
		ChunkerModel chunkerModel;
		ApplicationKeys appKeys;
        String openNLPPath;
        
        appKeys = ApplicationKeys.getInstance();
        openNLPPath = appKeys.getKey(ApplicationKey.OPENNLPPATH);
        try {
            chunkerModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-chunker.bin"));
        } catch (MalformedURLException ex) {
            throw new AlixiaException("Can't create Chunker URL", ex);
        }
		
		try {
			chunkerModel = new ChunkerModel(chunkerModelURL);
		}
		catch (IOException e) {
			throw new AlixiaException("Can't load Chunker model(s)", e);
		}
		chunker = new ChunkerME(chunkerModel);
	}
	
	public String[] chunkDocument(String[] tokenizedInput, String[] posTags) {
		String[] chunkTags;
		
		SharedUtils.checkNotNull(tokenizedInput);
		SharedUtils.checkNotNull(posTags);
		chunkTags = chunker.chunk(tokenizedInput, posTags);
		for (String s : chunkTags) {
			LOGGER.debug("CHUNK: {}", s);
		}
		return chunkTags;
	}
	
	public String[] segmentDocument(String[] tokenizedInput, String[] posTags) {
		Span[] chunkSpans;
		String[] spanStrings;
		
		SharedUtils.checkNotNull(tokenizedInput);
		SharedUtils.checkNotNull(posTags);
		chunkSpans = chunker.chunkAsSpans(tokenizedInput, posTags);
		spanStrings = Span.spansToStrings(chunkSpans, tokenizedInput);
		for (String s : spanStrings) {
			LOGGER.debug("CHUNK SPAN: {}", s);
		}
		return spanStrings;
	}
	
	public Sequence[] getChunkSequences(String[] tokenizedInput, String[] posTags) {
		Sequence[] chunkSeqs;
		
		SharedUtils.checkNotNull(tokenizedInput);
		SharedUtils.checkNotNull(posTags);
		chunkSeqs = chunker.topKSequences(tokenizedInput, posTags);
		return chunkSeqs;
	}

}
