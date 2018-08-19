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
package com.hulles.a1icia.charlie.doccat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.hulles.a1icia.api.shared.ApplicationKeys;
import com.hulles.a1icia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.charlie.parse.CharlieParser;

import opennlp.tools.doccat.DocumentSample;

final public class DocumentSampleGenerator {
	private final CharlieParser parser;
	
	public DocumentSampleGenerator(CharlieParser parser) {
		
		SharedUtils.checkNotNull(parser);
		this.parser = parser;
	}
	
	public List<DocumentSample> getSamples() {
		List<DocumentSample> samples;
		String line;
		URL textURL;
		ApplicationKeys appKeys;
        String openNLPPath;
        
        appKeys = ApplicationKeys.getInstance();
        openNLPPath = appKeys.getKey(ApplicationKey.OPENNLPPATH);
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
