package com.hulles.a1icia.charlie.parse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.ApplicationKeys;
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
        
        appKeys = ApplicationKeys.getInstance();
        String openNLPPath = appKeys.getOpenNLPPath();
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
