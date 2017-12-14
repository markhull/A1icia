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
package com.hulles.a1icia.echo.w2v;

import java.util.Map;

import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * WordToVecAnalyze loads the word2vec file and performs a simple analysis of the words contained in it. 
 *   The vector values of the map are not used. I wrote this simply out of curiosity, and to see if there
 *   was any garbage in the file I could filter out to save some time and aggravation. I ended up just removing
 *   the 1-character entries from the word2vec file.
 *   
 *   This class also serves as a good test of being able to load the desired word2vec BIN file.
 *   
 * @author hulles
 *
 */
final public class WordToVecAnalyze {
	private Map<String, float[]> words;
	
	/**
	 * Load the map and analyze the word keys in it.
	 * 
	 * @param fileName the name of the word2vec BIN FORMAT file, e.g. vectors.bin
	 */
	public WordToVecAnalyze(String fileName) {
		WordToVecLoader loader;
		
		A1iciaUtils.checkNotNull(fileName);
		loader = new WordToVecLoader();
		loader.load(fileName);
		words = loader.getMap();
		calculateStats();
	}
	
	/**
	 * Calculate statistics for the words contained in the map and dump the results to
	 *   System.out.
	 * 
	 */
	private void calculateStats() {
		int minLen = Integer.MAX_VALUE;
		int maxLen = 0;
		long sum = 0;
		int len;
		double temp = 0;
		double mean;
		double variance;
		double stdDeviation;
		int zeroSize = 0;
		int oneSize = 0;
		int punctuationSize = 0;
		int digitSize = 0;
		char c;
		
		for (String word : words.keySet()) {
			len = word.length();
			if (len < minLen) minLen = len;
			if (len > maxLen) maxLen = len;
			if (len == 0) zeroSize++;
			if (len == 1) {
				oneSize++;
				c = word.charAt(0);
				if (!Character.isLetterOrDigit(c)) punctuationSize++;
				if (Character.isDigit(c)) digitSize++;
				System.out.println("1char word " + word);
			}
			sum += len;
		}
		mean = sum / words.size();
		for (String word : words.keySet()) {
			len = word.length();
			temp += (mean - len) * (mean - len);
		}
		variance = temp / words.size();
		stdDeviation = Math.sqrt(variance);
		
		System.out.println("list size = " + words.size());
		System.out.println("zero length words = " + zeroSize);
		System.out.println("one char words = " + oneSize);
		System.out.println("punctuation words = " + punctuationSize);
		System.out.println("digit words = " + digitSize);
		System.out.println("min word len = " + minLen);
		System.out.println("max word len = " + maxLen);
		System.out.println("mean word len = " + mean);
		System.out.println("word len variance = " + variance);
		System.out.println("word len std deviation = " + stdDeviation);
	}

}
