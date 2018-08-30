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
package com.hulles.a1icia.echo.w2v;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.A1iciaException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.api.tools.A1iciaTimer;

/**
 * WordToVecSearch is the class that uses the loaded word2vec BIN file for word matches 
 *   (aka distance in the original C version) and analogies
 *   
 * @author hulles
 *
 */
final public class WordToVecSearch {
	final static Logger LOGGER = Logger.getLogger("A1iciaEcho.WordToVecSearch");
	final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final static String DISTANCE_FORMAT = "(%.4f)";
	private Map<String, float[]> wordVectors = null;
	
	public WordToVecSearch() {
		// that's a big map; we only want one of these laying around...
	}
	
	public boolean fileIsLoaded() {
		
		return (wordVectors != null);
	}
	
	/**
	 * Load the word2vec BIN FORMAT file into the map
	 * 
	 * @param fileName The name of the file (e.g. vectors.bin)
	 */
	public void loadFile(String fileName) {
		WordToVecLoader loader;
		
		SharedUtils.checkNotNull(fileName);
		loader = new WordToVecLoader();
		LOGGER.log(LOGLEVEL, "WordToVecSearch: about to load w2v file");
		loader.load(fileName);
		wordVectors = loader.getMap();
		LOGGER.log(LOGLEVEL, "WordToVecSearch: finished loading w2v file");
	}
	
	/**
	 * Return the map that has already been loaded
	 * 
	 * @return The map of words and vectors
	 */
	public Map<String, float[]> getVectorMap() {
		
		if (wordVectors == null) {
			System.err.println("You need to call the loadFile method before accessing the map");
			return null;
		}
		return wordVectors;
	}
	
	/**
	 * Get the specified number of closest matches to word from the file; comparable to the original 
	 *   word2vec 'distance.c' program in that it returns the cosine distance of word matches
	 *   
	 * @param word The word to match
	 * @param maxNumberOfMatches Self-explanatory
	 * @return A list of "matching" WordDistances
	 */
	public static List<WordDistance> getWordMatches(String word, Map<String, float[]> vectorMap, Integer maxNumberOfMatches) {
		float[] result;
		List<WordDistance> matches;

		SharedUtils.checkNotNull(word);
		SharedUtils.checkNotNull(vectorMap);
		SharedUtils.checkNotNull(maxNumberOfMatches);
		A1iciaTimer.startTimer("MATCHES");
		result = vectorMap.get(word);
		if (result == null) {
			return Collections.emptyList();
		}
		matches = getVectorMatches(Collections.singletonList(word), result, vectorMap, maxNumberOfMatches);
		A1iciaTimer.stopTimer("MATCHES");
		return matches;
	}

	public List<WordDistance> getWordMatches(String word, Integer maxNumberOfMatches) {
		return getWordMatches(word, wordVectors, maxNumberOfMatches);
	}
	
	
	/**
	 * Gets a list of possible analogues to the provided three words; see the 
	 *   original C word-analogy.c program for more details. The logic is: word1 is to 
	 *   word2 as word3 is to...?
	 * @param word1
	 * @param word2
	 * @param word3
	 * @param maxNumberOfMatches
	 * @return A list of "matching" WordDistances
	 * @throws A1iciaException
	 */
	public static List<WordDistance> getAnalogy(String word1, String word2, String word3, Map<String, float[]> vectorMap, Integer maxNumberOfMatches) throws A1iciaException {
		float[] result1;
		float[] result2;
		float[] result3;
		float[] searchFor;
		float[] searchVector;
		List<String> ignores;
		List<WordDistance> matches;

		SharedUtils.checkNotNull(word1);
		SharedUtils.checkNotNull(word2);
		SharedUtils.checkNotNull(word3);
		SharedUtils.checkNotNull(vectorMap);
		SharedUtils.checkNotNull(maxNumberOfMatches);
		A1iciaTimer.startTimer("ANALOGY");
		result1 = vectorMap.get(word1);
		if (result1 == null) {
			throw new A1iciaException(word1);
		}
		result2 = vectorMap.get(word2);
		if (result2 == null) {
			throw new A1iciaException(word2);
		}
		result3 = vectorMap.get(word3);
		if (result3 == null) {
			throw new A1iciaException(word3);
		}
		ignores = new ArrayList<>(3);
		ignores.add(word1);
		ignores.add(word2);
		ignores.add(word3);
		searchFor = new float[result1.length];
		for (int ix = 0; ix < searchFor.length; ix++) {
			searchFor[ix] = result2[ix] - result1[ix] + result3[ix];
		}
		searchVector = WordToVecLoader.normalize(searchFor);
		matches = getVectorMatches(ignores, searchVector, vectorMap, maxNumberOfMatches);
		A1iciaTimer.stopTimer("ANALOGY");
		return matches;
	}

	public List<WordDistance> getAnalogy(String word1, String word2, String word3, Integer maxNumberOfMatches)  throws A1iciaException {
		return getAnalogy(word1, word2, word3, wordVectors, maxNumberOfMatches);
	}
	
	/**
	 * Look up words in the map and sum their vectors (if present)
	 * 
	 * @param words Words to mash up
	 * @return A normalized sum of the words' vectors
	 */
	public static float[] getMashUp(List<String> words, Map<String, float[]> vectorMap) {
		double[] vectorSum;
		float[] result;
		int vectorSize;
		
		SharedUtils.checkNotNull(words);
		SharedUtils.checkNotNull(vectorMap);
		result = grabAMapValue(vectorMap);
		vectorSize = result.length;
		vectorSum = new double[vectorSize];
		for (String word : words) {
			result = vectorMap.get(word);
			if (result != null) {
				for (int ix = 0; ix < vectorSize; ix++) {
					vectorSum[ix] += result[ix];
				}
			}
		}
		return WordToVecLoader.normalize(vectorSum);
	}
	
	public float[] getMashUp(List<String> words) {
		return getMashUp(words, wordVectors);
	}
	
	/**
	 * Simple lookup of word in vector map
	 * 
	 * @param word The word to look up
	 * @param vectorMap The vector map
	 * @return the float[] vector of the word if available, or null
	 */
	public static float[] getVector(String word, Map<String, float[]> vectorMap) {
		float[] result;
		
		SharedUtils.checkNotNull(word);
		SharedUtils.checkNotNull(vectorMap);
		result = vectorMap.get(word);
		return result;
	}
	
	public float[] getVector(String word) {
		return getVector(word, wordVectors);
	}
	
	/**
	 * This is the heart of the whole shooting match. We convert the wordVectors map to an entry 
	 *   set and evaluate each vector against the target vector. We use a sum of the products 
	 *   of the two vectors to get a scalar that we can use to evaluate the closeness of the 
	 *   match (the cosine distance).
	 *   
	 * @param ignores Words to ignore in the file (the search word(s) themselves)
	 * @param thisVector The vector of the word we're matching
	 * @param maxNumberOfMatches Self-explanatory
	 * @return A list of "matching" WordDistances
	 */
	public static List<WordDistance> getVectorMatches(List<String> ignores, float[] thisVector, Map<String, float[]> vectorMap, Integer maxNumberOfMatches) {
		Set<Entry<String, float[]>> entrySet;
		Double distance;
		List<WordDistance> bestMatches;
		WordDistance wDistance;
		Double leastBestDistance = 0.0;
		
		SharedUtils.checkNotNull(ignores);
		SharedUtils.checkNotNull(thisVector);
		SharedUtils.checkNotNull(vectorMap);
		SharedUtils.checkNotNull(maxNumberOfMatches);
		entrySet = vectorMap.entrySet();
		bestMatches = new ArrayList<>(maxNumberOfMatches);
		wDistance = new WordDistance("init", 0.0);
		bestMatches.addAll(Collections.nCopies(maxNumberOfMatches, wDistance));
		LOGGER.log(LOGLEVEL, "WordToVecSearch: searching entry table");
		for (Entry<String, float[]> entry : entrySet) {
			if (ignores.contains(entry.getKey())) {
				continue;
			}
//			System.out.println("SEARCH:");
//			WordToVecLoader.dumpArray(entry.getValue());
			distance = calculateDistance(thisVector, entry.getValue());
			if (distance > leastBestDistance) {
				// then it belongs in the bestMatches list
				leastBestDistance = updateBestMatches(distance, bestMatches, entry.getKey());
			}
		}
		LOGGER.log(LOGLEVEL, "WordToVecSearch: built match table for search");
		return bestMatches;
	}

	public List<WordDistance> getVectorMatches(List<String> ignores, float[] thisVector, Integer maxNumberOfMatches) {
		return getVectorMatches(ignores, thisVector, wordVectors, maxNumberOfMatches);
	}
	
	/**
	 * For a given word, insert it into its position in the bestMatches list and remove the last item
	 *   so the list remains the same size
	 * @param distance The cosine distance of the word
	 * @param bestMatches The list of WordDistances that are the best matches so far
	 * @param word The matching word we're inserting into the list
	 * @return The (new) smallest cosine distance of the bestMatches list 
	 */
	private static Double updateBestMatches(Double distance, List<WordDistance> bestMatches, String word) {
		WordDistance wordDistance;
		WordDistance newWordDistance;
		
//		SharedUtils.checkNotNull(distance);
//		SharedUtils.checkNotNull(distances);
//		SharedUtils.checkNotNull(word);
		for (int listIx = 0; listIx < bestMatches.size(); listIx++) {
			wordDistance = bestMatches.get(listIx);
			if (distance > wordDistance.getDistance()) {
				newWordDistance = new WordDistance(word, distance);
				bestMatches.add(listIx, newWordDistance);
				bestMatches.remove(bestMatches.size() - 1);
				break;
			}
		}
		return bestMatches.get(bestMatches.size() - 1).getDistance();
	}
	
	/**
	 * Calculate the cosine distance of the from and to vectors
	 * 
	 * @param from The from vector
	 * @param to The to vector
	 * @return The distance of the vectors
	 */
	private static Double calculateDistance(float[] from, float[] to) {
		double sum = 0.0;
		
//		SharedUtils.checkNotNull(from);
//		SharedUtils.checkNotNull(to);
//		if (from.length != to.length) {
//			throw new IllegalArgumentException();
//		}
		for (int ix = 0; ix < from.length; ix++) {
			sum += from[ix] * to[ix];
		}
		return sum;
	}

	/**
	 * Simple formatter for WordDistance result list
	 * 
	 * @param matches List of WordDistance objects from above
	 * @return The formatted output string
	 */
	public static String formatResult(List<WordDistance> matches) {
		StringBuilder sb;
		
		SharedUtils.checkNotNull(matches);
		sb = new StringBuilder();
		for (WordDistance match : matches) {
			if (match.getToWord().equals("init")) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(match.getToWord());
			sb.append(" ");
			sb.append(String.format(DISTANCE_FORMAT, match.getDistance()));
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public static <K,E> E grabAMapValue(Map<K,E> map) {
		Collection<E> values;
		Iterator<E> iter;
		
		SharedUtils.checkNotNull(map);
		if (map.isEmpty()) {
			return null;
		}
		values = map.values();
		iter = values.iterator();
		if (iter.hasNext()) {
			return iter.next();
		}
		return null;
	}
	
}
