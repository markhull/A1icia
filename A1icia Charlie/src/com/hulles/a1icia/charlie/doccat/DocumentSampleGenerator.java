package com.hulles.a1icia.charlie.doccat;

import com.hulles.a1icia.api.shared.ApplicationKeys;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.charlie.parse.CharlieParser;
import com.hulles.a1icia.tools.A1iciaUtils;
import java.net.MalformedURLException;

import opennlp.tools.doccat.DocumentSample;

final public class DocumentSampleGenerator {
	private final CharlieParser parser;
	
	public DocumentSampleGenerator(CharlieParser parser) {
		
		A1iciaUtils.checkNotNull(parser);
		this.parser = parser;
	}
	
	public List<DocumentSample> getSamples() {
		List<DocumentSample> samples;
		String line;
		URL textURL;
		ApplicationKeys appKeys;
        
        appKeys = ApplicationKeys.getInstance();
        String openNLPPath = appKeys.getOpenNLPPath();
        try {
            textURL = new URL(ApplicationKeys.toURL(openNLPPath + "/affirmatives.txt"));
        } catch (MalformedURLException ex) {
            throw new A1iciaException("Can't create affirmatives URL", ex);
        }
		
		samples = new ArrayList<>();
		
		try (BufferedReader dataStream = new BufferedReader(new InputStreamReader(textURL.openStream()))) {
			while ((line = dataStream.readLine()) != null) {
				samples.add(genSample("Affirmative", line));
			}
		} catch (IOException e) {
			throw new A1iciaException("Can't retrieve affirmative data", e);
		}
		
        try {
            textURL = new URL(openNLPPath + "/negatives.txt");
        } catch (MalformedURLException ex) {
            throw new A1iciaException("Can't create negatives URL", ex);
        }
		try (BufferedReader dataStream = new BufferedReader(new InputStreamReader(textURL.openStream()))) {
			while ((line = dataStream.readLine()) != null) {
				samples.add(genSample("Negative", line));
			}
		} catch (IOException e) {
			throw new A1iciaException("Can't retrieve negative data", e);
		}
		
		return samples;
	}
	
	private DocumentSample genSample(String category, String line) {
		DocumentSample sample;
		String[] tokens;
		
		tokens = parser.parseSentenceTokens(line);
		sample = new DocumentSample(category, tokens);
		return sample;
	}
}
