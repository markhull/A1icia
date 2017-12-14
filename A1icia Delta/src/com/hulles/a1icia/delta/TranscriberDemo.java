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
package com.hulles.a1icia.delta;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;

public class TranscriberDemo {       

	private static void transcribe() throws Exception {
		SpeechResult result;
		Configuration configuration;
		StreamSpeechRecognizer recognizer;
		
		configuration = new Configuration();
		configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

		recognizer = new StreamSpeechRecognizer(configuration);
		try (InputStream stream = new FileInputStream(new File("/home/hulles/lookdave.wav"))) {
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
			TranscriberDemo.transcribe();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}