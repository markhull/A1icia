package com.hulles.a1icia.charlie.parse;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.ApplicationKeys;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.tools.A1iciaUtils;
import java.net.MalformedURLException;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

final public class CharlieParser {
	private final static Logger LOGGER = Logger.getLogger("A1iciaCharlie.CharlieParser");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final SentenceDetectorME sentenceParser;
	private final TokenizerME tokenizer;

	public CharlieParser() {
		URL sentenceModelURL = null;
		URL tokenModelURL = null;
		SentenceModel sentenceModel;
		TokenizerModel tokenizerModel;
		ApplicationKeys appKeys;
        
        appKeys = ApplicationKeys.getInstance();
        String openNLPPath = appKeys.getOpenNLPPath();
        try {
            sentenceModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-sent.bin"));
            tokenModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-token.bin"));
        } catch (MalformedURLException ex) {
            throw new A1iciaException("Can't create Parser URL(s)", ex);
        }
		
		try {
			sentenceModel = new SentenceModel(sentenceModelURL);
			tokenizerModel = new TokenizerModel(tokenModelURL);
		}
		catch (IOException e) {
			throw new A1iciaException("Can't load sentence/tokenizer model(s)", e);
		}
		sentenceParser = new SentenceDetectorME(sentenceModel);
		tokenizer = new TokenizerME(tokenizerModel);
	}
	
	public String[] detectSentences(String input) {
		String[] sentences;
		
		A1iciaUtils.checkNotNull(input);
		sentences = sentenceParser.sentDetect(input);
		LOGGER.log(LOGLEVEL, "AFTER SENTENCE DETECT:");
		for (String s : sentences) {
			LOGGER.log(LOGLEVEL, "SENTENCE: {0}", s);
		}
		return sentences;
	}
	
	public String[] parseSentenceTokens(String input) {
		String[] tokens;
		
		A1iciaUtils.checkNotNull(input);
		tokens = tokenizer.tokenize(input);
		for (String s : tokens) {
			LOGGER.log(LOGLEVEL, "TOKEN: {0}", s);
		}
		return tokens;
	}
}
