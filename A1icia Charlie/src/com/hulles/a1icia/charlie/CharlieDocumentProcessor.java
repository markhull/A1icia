package com.hulles.a1icia.charlie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.charlie.doccat.CharlieDocCat;
import com.hulles.a1icia.charlie.ner.CharlieNER;
import com.hulles.a1icia.charlie.parse.CharlieChunker;
import com.hulles.a1icia.charlie.parse.CharlieParser;
import com.hulles.a1icia.charlie.pos.CharlieLemmatizer;
import com.hulles.a1icia.charlie.pos.CharliePOS;
import com.hulles.a1icia.room.document.NLPAnalysis;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.SentenceAnalysis;
import com.hulles.a1icia.tools.A1iciaTimer;
import com.hulles.a1icia.tools.A1iciaUtils;

final public class CharlieDocumentProcessor {
	private final CharlieParser parser;
	private final CharlieNER ner;
	private final CharliePOS pos;
	private final CharlieDocCat docCat;
	private final CharlieChunker chunker;
	private final CharlieLemmatizer lemmatizer;
	
	public CharlieDocumentProcessor() {
		
		parser = new CharlieParser();
		ner = new CharlieNER();
		pos = new CharliePOS();
		docCat = new CharlieDocCat();
		chunker = new CharlieChunker();
		lemmatizer = new CharlieLemmatizer();
	}
	
	public NLPAnalysis processDocument(RoomRequest request) {
		NLPAnalysis analysis = null;
		String input;
		String[] tokens;
		String[] sentences;
		String[] nerPersons;
		String[] nerLocations;
		String[] nerOrganizations;
		String[] nerDates;
		String[] nerTimes;
		String[] nerPercentages;
		String[] nerMoney;
		String[] myriaCitizens;
		String[] myriaLocations;
		String[] myriaOrganizations;
		String[] posTags;
		String[] chunkTags;
		String[] chunkSegments;
		String[] lemmata;
		String[] dictLemmata;
		String docCategory;
		String posDef;
		List<SentenceAnalysis> sentenceAnalyses;
		SentenceAnalysis sentenceAnalysis = null;
		List<String> defs;
		
		A1iciaUtils.checkNotNull(request);
		input = request.getMessage().trim();
		if (!input.isEmpty()) {
			analysis = new NLPAnalysis();
			analysis.setMessage(input);
			sentences = parser.detectSentences(input);
			analysis.setSentences(Arrays.asList(sentences));
			sentenceAnalyses = new ArrayList<>(sentences.length);
			for (String sentence : sentences) {
				
				// parse sentences and tokens
				sentenceAnalysis = new SentenceAnalysis(sentence);
				tokens =  parser.parseSentenceTokens(sentence);
				sentenceAnalysis.setTokens(Arrays.asList(tokens));
				
				// NER (Named Entity Recognition)
				nerPersons = ner.findPersons(tokens);
				sentenceAnalysis.setNERPersons(Arrays.asList(nerPersons));
				nerLocations = ner.findLocations(tokens);
				sentenceAnalysis.setNERLocations(Arrays.asList(nerLocations));
				nerOrganizations = ner.findOrganizations(tokens);
				sentenceAnalysis.setNEROrganizations(Arrays.asList(nerOrganizations));
				nerDates = ner.findDates(tokens);
				sentenceAnalysis.setNERDates(Arrays.asList(nerDates));
				nerTimes = ner.findTimes(tokens);
				sentenceAnalysis.setNERTimes(Arrays.asList(nerTimes));
				nerMoney = ner.findMoney(tokens);
				sentenceAnalysis.setNERMoney(Arrays.asList(nerMoney));
				nerPercentages = ner.findPercentages(tokens);
				sentenceAnalysis.setNERPercentages(Arrays.asList(nerPercentages));
				
				// Myria NER
				myriaCitizens = ner.findMyriaCitizens(tokens);
				sentenceAnalysis.setMyriaCitizens(Arrays.asList(myriaCitizens));
				myriaLocations = ner.findMyriaLocations(tokens);
				sentenceAnalysis.setMyriaLocations(Arrays.asList(myriaLocations));
				myriaOrganizations = ner.findMyriaOrganizations(tokens);
				sentenceAnalysis.setMyriaOrganizations(Arrays.asList(myriaOrganizations));
				ner.endOfDocument();
				
				// POS (Part of Speech) analysis
				posTags = pos.generatePOS(tokens);
				sentenceAnalysis.setPOSTags(Arrays.asList(posTags));
				defs = new ArrayList<>();
				for (String tag : sentenceAnalysis.getPOSTags()) {
					posDef = PennTreebank.getTagDefinition(tag);
					if (posDef == null) {
						throw new A1iciaException("Can't find Penn Treebank tag, shouldn't happen");
					}
					defs.add(posDef);
				}
				sentenceAnalysis.setPOSTagDefinitions(defs);
				
				// Lemma analysis
				A1iciaTimer.startTimer("lemmatizer1");
				lemmata = lemmatizer.generateLemmata(tokens, posTags);
				sentenceAnalysis.setLemmata(Arrays.asList(lemmata));
				A1iciaTimer.stopTimer("lemmatizer1");
				A1iciaTimer.startTimer("lemmatizer2");
				dictLemmata = CharlieLemmatizer.generateDictionaryLemmata(tokens, posTags);
				sentenceAnalysis.setDictionaryLemmata(Arrays.asList(dictLemmata));
				A1iciaTimer.stopTimer("lemmatizer2");
				
				// Chunker
				chunkTags = chunker.chunkDocument(tokens, posTags);
				sentenceAnalysis.setChunkTags(Arrays.asList(chunkTags));
				chunkSegments = chunker.segmentDocument(tokens, posTags);
				sentenceAnalysis.setChunkSegments(Arrays.asList(chunkSegments));
				
				// Document categorizer (doesn't do much, not enough input cases)
				docCategory = docCat.categorizeDocument(tokens);
				sentenceAnalysis.setDocumentCategory(docCategory);
				sentenceAnalyses.add(sentenceAnalysis);
			}
			analysis.setSentenceAnalyses(sentenceAnalyses);
		}
		return analysis;
	}
	
	/**
	 * Here we update our dictionaries. We currently just update the lemmata dictionary
	 * but we could also update the NER dictionaries if we needed to for some reason. We
	 * could also train our models here, and in fact ought to (TODO).
	 * 
	 * @param analysis The analysis to use for updating
	 */
	public static void postProcessAnalysis(NLPAnalysis analysis) {
		
		A1iciaUtils.checkNotNull(analysis);
		for (SentenceAnalysis a : analysis.getSentenceAnalyses()) {
			CharlieLemmatizer.updateDictionaryLemmata(a.getDictionaryLemmata(), 
					a.getTokens(), a.getPOSTags());
		}
	}
}
