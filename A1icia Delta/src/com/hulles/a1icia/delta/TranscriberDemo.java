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
package com.hulles.a1icia.delta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.tools.A1iciaUtils;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;

public class TranscriberDemo {       
	private static StreamSpeechRecognizer recognizer;
	
	static {
		Configuration configuration;
				
		configuration = new Configuration();
		configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
		try {
			recognizer = new StreamSpeechRecognizer(configuration);
		} catch (IOException ex) {
			recognizer = null;
		}
	}
	
	public static String transcribe(File speechFile) {
		SpeechResult result;
		List<String> results;
		String hypothesis;
		StringBuilder sb;
		
		A1iciaUtils.checkNotNull(speechFile);
		results = new ArrayList<>();
		try (InputStream stream = new FileInputStream(speechFile)) {
			recognizer.startRecognition(stream);
			while ((result = recognizer.getResult()) != null) {
				hypothesis = result.getHypothesis();
				results.add(hypothesis);
				System.out.format("Hypothesis: %s\n", hypothesis);
			}
			recognizer.stopRecognition();
		} catch (FileNotFoundException e) {
			throw new A1iciaException("Unable to find speech file", e);
		} catch (IOException e) {
			throw new A1iciaException("IO exception transcribing speech file", e);
		}
		if (results.isEmpty()) {
			return null;
		}
		sb = new StringBuilder();
		for (String s : results) {
			if (sb.length() > 0) {
				sb.append(". ");
			}
			sb.append(s);
		}
		return sb.toString();
	}
	
	private static void transcribeTest() throws Exception {
		SpeechResult result;

		try (InputStream stream = new FileInputStream(new File("/home/hulles/Media/sheep.wav"))) {
			recognizer.startRecognition(stream);
			while ((result = recognizer.getResult()) != null) {
				System.out.format("Hypothesis: %s\n", result.getHypothesis());
				// Get individual words and their times.
				for (WordResult r : result.getWords()) {
				    System.out.println(r);
				}
			}
			recognizer.stopRecognition();
		}
	}
	
	public static void main(String[] args) {
		
		try {
			TranscriberDemo.transcribeTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}