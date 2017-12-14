package com.hulles.a1icia.charlie.ner;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.ApplicationKeys;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.charlie.ner.CharlieDictionary.DictionaryType;
import com.hulles.a1icia.tools.A1iciaUtils;
import java.net.MalformedURLException;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

final public class CharlieNER {
	private final static Logger LOGGER = Logger.getLogger("A1iciaCharlie.CharlieNER");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final DictionaryNameFinder myriaOrganizationFinder;
	private final DictionaryNameFinder myriaLocationFinder;
	private final DictionaryNameFinder myriaCitizenFinder;
	private final NameFinderME organizationFinder;
	private final NameFinderME locationFinder;
	private final NameFinderME personFinder;
	private final NameFinderME dateFinder;
	private final NameFinderME timeFinder;
	private final NameFinderME moneyFinder;
	private final NameFinderME percentageFinder;
	private final TokenizerME tokenizer;
	
	public CharlieNER() {
		URL personModelURL;
		URL locationModelURL;
		URL organizationModelURL;
		URL dateModelURL;
		URL timeModelURL;
		URL moneyModelURL;
		URL percentageModelURL;
		URL tokenModelURL;
		
		TokenizerModel tokenizerModel;
		
		TokenNameFinderModel personModel;
		TokenNameFinderModel locationModel;
		TokenNameFinderModel organizationModel;
		TokenNameFinderModel timeModel;
		TokenNameFinderModel dateModel;
		TokenNameFinderModel moneyModel;
		TokenNameFinderModel percentageModel;
		
		Dictionary citizenDictionary;
		Dictionary organizationDictionary;
		Dictionary locationDictionary;
		
		ApplicationKeys appKeys;
        
        appKeys = ApplicationKeys.getInstance();
        String openNLPPath = appKeys.getOpenNLPPath();
        try {
            personModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-ner-person.bin"));
            locationModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-ner-location.bin"));
            organizationModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-ner-organization.bin"));
            dateModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-ner-date.bin"));
            timeModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-ner-time.bin"));
            moneyModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-ner-money.bin"));
            percentageModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-ner-percentage.bin"));
            tokenModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-token.bin"));
        } catch (MalformedURLException ex) {
            throw new A1iciaException("Can't create NER URL", ex);
        }
		try {
			personModel = new TokenNameFinderModel(personModelURL);
			locationModel = new TokenNameFinderModel(locationModelURL);
			organizationModel = new TokenNameFinderModel(organizationModelURL);
			dateModel = new TokenNameFinderModel(dateModelURL);
			timeModel = new TokenNameFinderModel(timeModelURL);
			moneyModel = new TokenNameFinderModel(moneyModelURL);
			percentageModel = new TokenNameFinderModel(percentageModelURL);
			tokenizerModel = new TokenizerModel(tokenModelURL);
		}
		catch (IOException e) {
			throw new A1iciaException("Can't load NER model(s)", e);
		}
		personFinder = new NameFinderME(personModel);
		locationFinder = new NameFinderME(locationModel);
		organizationFinder = new NameFinderME(organizationModel);
		dateFinder = new NameFinderME(dateModel);
		timeFinder = new NameFinderME(timeModel);
		moneyFinder = new NameFinderME(moneyModel);
		percentageFinder = new NameFinderME(percentageModel);
		
		tokenizer = new TokenizerME(tokenizerModel);
		
		citizenDictionary = new CharlieDictionary(DictionaryType.PERSON, tokenizer);
		myriaCitizenFinder = new DictionaryNameFinder(citizenDictionary);
		locationDictionary = new CharlieDictionary(DictionaryType.LOCATION, tokenizer);
		myriaLocationFinder = new DictionaryNameFinder(locationDictionary);
		organizationDictionary = new CharlieDictionary(DictionaryType.ORGANIZATION, tokenizer);
		myriaOrganizationFinder = new DictionaryNameFinder(organizationDictionary);
	}
	
	public String[] findPersons(String[] tokenizedInput) {
		Span[] outputSpans;
		String[] outputStrings;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		outputSpans = personFinder.find(tokenizedInput);
		outputStrings = Span.spansToStrings(outputSpans, tokenizedInput);
		for (String s : outputStrings) {
			LOGGER.log(LOGLEVEL, "NAME: {0}", s);
		}
		return outputStrings;
	}

	public String[] findLocations(String[] tokenizedInput) {
		Span[] outputSpans;
		String[] outputStrings;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		outputSpans = locationFinder.find(tokenizedInput);
		outputStrings = Span.spansToStrings(outputSpans, tokenizedInput);
		for (String s : outputStrings) {
			LOGGER.log(LOGLEVEL, "LOCATION: {0}", s);
		}
		return outputStrings;
	}

	public String[] findOrganizations(String[] tokenizedInput) {
		Span[] outputSpans;
		String[] outputStrings;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		outputSpans = organizationFinder.find(tokenizedInput);
		outputStrings = Span.spansToStrings(outputSpans, tokenizedInput);
		for (String s : outputStrings) {
			LOGGER.log(LOGLEVEL, "ORGANIZATION: {0}", s);
		}
		return outputStrings;
	}

	public String[] findDates(String[] tokenizedInput) {
		Span[] outputSpans;
		String[] outputStrings;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		outputSpans = dateFinder.find(tokenizedInput);
		outputStrings = Span.spansToStrings(outputSpans, tokenizedInput);
		for (String s : outputStrings) {
			LOGGER.log(LOGLEVEL, "DATE: {0}", s);
		}
		return outputStrings;
	}

	public String[] findTimes(String[] tokenizedInput) {
		Span[] outputSpans;
		String[] outputStrings;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		outputSpans = timeFinder.find(tokenizedInput);
		outputStrings = Span.spansToStrings(outputSpans, tokenizedInput);
		for (String s : outputStrings) {
			LOGGER.log(LOGLEVEL, "TIME: {0}", s);
		}
		return outputStrings;
	}

	public String[] findMoney(String[] tokenizedInput) {
		Span[] outputSpans;
		String[] outputStrings;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		outputSpans = moneyFinder.find(tokenizedInput);
		outputStrings = Span.spansToStrings(outputSpans, tokenizedInput);
		for (String s : outputStrings) {
			LOGGER.log(LOGLEVEL, "MONEY: {0}", s);
		}
		return outputStrings;
	}

	public String[] findPercentages(String[] tokenizedInput) {
		Span[] outputSpans;
		String[] outputStrings;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		outputSpans = percentageFinder.find(tokenizedInput);
		outputStrings = Span.spansToStrings(outputSpans, tokenizedInput);
		for (String s : outputStrings) {
			LOGGER.log(LOGLEVEL, "PERCENTAGE: {0}", s);
		}
		return outputStrings;
	}
	
	public String[] findMyriaCitizens(String[] tokenizedInput) {
		Span[] outputSpans;
		String[] outputStrings;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		outputSpans = myriaCitizenFinder.find(tokenizedInput);
		outputStrings = Span.spansToStrings(outputSpans, tokenizedInput);
		for (String s : outputStrings) {
			LOGGER.log(LOGLEVEL, "MYRIA CITIZEN: {0}", s);
		}
		return outputStrings;
	}
	
	public String[] findMyriaLocations(String[] tokenizedInput) {
		Span[] outputSpans;
		String[] outputStrings;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		outputSpans = myriaLocationFinder.find(tokenizedInput);
		outputStrings = Span.spansToStrings(outputSpans, tokenizedInput);
		for (String s : outputStrings) {
			LOGGER.log(LOGLEVEL, "MYRIA LOCATION: {0}", s);
		}
		return outputStrings;
	}
	
	public String[] findMyriaOrganizations(String[] tokenizedInput) {
		Span[] outputSpans;
		String[] outputStrings;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		outputSpans = myriaOrganizationFinder.find(tokenizedInput);
		outputStrings = Span.spansToStrings(outputSpans, tokenizedInput);
		for (String s : outputStrings) {
			LOGGER.log(LOGLEVEL, "MYRIA INSTITUTION: {0}", s);
		}
		return outputStrings;
	}
	
	public void endOfDocument() {
		
		personFinder.clearAdaptiveData();
		locationFinder.clearAdaptiveData();
		organizationFinder.clearAdaptiveData();
		dateFinder.clearAdaptiveData();
		timeFinder.clearAdaptiveData();
		moneyFinder.clearAdaptiveData();
		percentageFinder.clearAdaptiveData();
		myriaCitizenFinder.clearAdaptiveData();
		myriaOrganizationFinder.clearAdaptiveData();
		myriaLocationFinder.clearAdaptiveData();
	}
	
	public TokenizerME getTokenizer() {
		
		return tokenizer;
	}
}
