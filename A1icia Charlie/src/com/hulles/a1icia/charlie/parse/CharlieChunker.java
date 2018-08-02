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
package com.hulles.a1icia.charlie.parse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.ApplicationKeys;
import com.hulles.a1icia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.tools.A1iciaUtils;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.util.Sequence;
import opennlp.tools.util.Span;

final public class CharlieChunker {
	private final static Logger LOGGER = Logger.getLogger("A1iciaCharlie.CharlieChunker");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
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
            throw new A1iciaException("Can't create Chunker URL", ex);
        }
		
		try {
			chunkerModel = new ChunkerModel(chunkerModelURL);
		}
		catch (IOException e) {
			throw new A1iciaException("Can't load Chunker model(s)", e);
		}
		chunker = new ChunkerME(chunkerModel);
	}
	
	public String[] chunkDocument(String[] tokenizedInput, String[] posTags) {
		String[] chunkTags;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		A1iciaUtils.checkNotNull(posTags);
		chunkTags = chunker.chunk(tokenizedInput, posTags);
		for (String s : chunkTags) {
			LOGGER.log(LOGLEVEL, "CHUNK: {0}", s);
		}
		return chunkTags;
	}
	
	public String[] segmentDocument(String[] tokenizedInput, String[] posTags) {
		Span[] chunkSpans;
		String[] spanStrings;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		A1iciaUtils.checkNotNull(posTags);
		chunkSpans = chunker.chunkAsSpans(tokenizedInput, posTags);
		spanStrings = Span.spansToStrings(chunkSpans, tokenizedInput);
		for (String s : spanStrings) {
			LOGGER.log(LOGLEVEL, "CHUNK SPAN: {0}", s);
		}
		return spanStrings;
	}
	
	public Sequence[] getChunkSequences(String[] tokenizedInput, String[] posTags) {
		Sequence[] chunkSeqs;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		A1iciaUtils.checkNotNull(posTags);
		chunkSeqs = chunker.topKSequences(tokenizedInput, posTags);
		return chunkSeqs;
	}

}
