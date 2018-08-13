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
package com.hulles.a1icia.tools;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.text.similarity.LevenshteinDistance;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * FuzzyMatch returns a quantified match between one string and another using the 
 * Levenshtein Distance. It actually does a pretty good job of returning interesting results, even
 * compared to the beefier fuzzywuzzy stuff implemented elsewhere. TODO Document where I got this from.
 * 
 * @author hulles
 *
 */
public class FuzzyMatch {
	private static final LevenshteinDistance levDistance;

	static {
		levDistance = new LevenshteinDistance();
	}
	
	/*
	 * t0 = [SORTED_INTERSECTION] 
	 * t1 = [SORTED_INTERSECTION] + [SORTED_REST_OF_STRING1] 
	 * t2 = [SORTED_INTERSECTION] + [SORTED_REST_OF_STRING2]
	 * 
	 * outcome = max(t0,t1,t2)
	 * 
	 */
	
	public static int getRatio(String s1, String s2, boolean debug) {
		Set<String> set1;
		Set<String> set2;
		StringTokenizer st1;
		StringTokenizer st2;
		SetView<String> intersection;
		TreeSet<String> sortedIntersection;
		SetView<String> restOfSet1;
		TreeSet<String> sortedRestOfSet1;
		SetView<String> restOfSet2;
		TreeSet<String> sortedRestOfSet2;
		Set<String> setT1;
		Set<String> setT2;
		int finalScore;
		
		if (s1.length() >= s2.length()) {		
			// We need to swap s1 and s2		
			String temp = s2;
			s2 = s1;
			s1 = temp;			
		}

		// Get alpha numeric characters
		
		s1 = escapeString(s1);
		s2 = escapeString(s2);
		
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();
		
		
		set1 = new HashSet<>();
		set2 = new HashSet<>();
		
		//split the string by space and store words in sets
		st1 = new StringTokenizer(s1);		
		while (st1.hasMoreTokens()) {
			set1.add(st1.nextToken());
		}

		st2 = new StringTokenizer(s2);		
		while (st2.hasMoreTokens()) {
			set2.add(st2.nextToken());
		}
		
		intersection = Sets.intersection(set1, set2);
		
		sortedIntersection = Sets.newTreeSet(intersection);

		if (debug) {
		    System.out.print("Sorted intersection --> ");
		for (String s:sortedIntersection) 
			System.out.print(s + " ");
		}
		
		// Find out difference of sets set1 and intersection of set1,set2
		restOfSet1 = Sets.symmetricDifference(set1, intersection);
		sortedRestOfSet1 = Sets.newTreeSet(restOfSet1);
		restOfSet2 = Sets.symmetricDifference(set2, intersection);
		sortedRestOfSet2 = Sets.newTreeSet(restOfSet2);
		
		if (debug) {
			System.out.print("\nSorted rest of 1 --> ");
			for (String s:sortedRestOfSet1) 
				System.out.print(s + " ");
			
			System.out.print("\nSorted rest of 2 -->");
			for (String s:sortedRestOfSet2) 
				System.out.print(s + " ");
		}
		
		String t0 = "";
		String t1 = "";
		String t2 = "";
		
		for (String s:sortedIntersection) {
			t0 = t0 + " " + s;			
		}
		t0 = t0.trim();
		
		setT1 = Sets.union(sortedIntersection, sortedRestOfSet1);
		for (String s:setT1) {
			t1 = t1 + " " + s;			
		}
		t1 = t1.trim();
		
		setT2 = Sets.union(intersection, sortedRestOfSet2);		
		for (String s:setT2) {
			t2 = t2 + " " + s;			
		}
		t2 = t2.trim();
		
		int amt1 = calculateLevensteinDistance(t0, t1);
		int amt2 = calculateLevensteinDistance(t0, t2);
		int amt3 = calculateLevensteinDistance(t1, t2);
		
		if (debug) {
			System.out.println();
			System.out.println("t0 = " + t0 + " --> " + amt1);
			System.out.println("t1 = " + t1 + " --> " + amt2);
			System.out.println("t2 = " + t2 + " --> " + amt3);
			System.out.println();
		}
		
		finalScore = Math.max(Math.max(amt1, amt2), amt3);
		return finalScore;	
	}
	
	public static int calculateLevensteinDistance(String s1, String s2) {
		int distance;
		double ratio;
		
		distance = levDistance.apply(s1, s2);
		ratio = ((double) distance) / (Math.max(s1.length(), s2.length()));
		return 100 - new Double(ratio*100).intValue();		
	}
	
	public static String escapeString(String token) {
		StringBuffer s;
		CharacterIterator it;
		
		s = new StringBuffer(token.length());
		it = new StringCharacterIterator(token);
		for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {
			switch (ch) {
				// '-,)(!`\":/][?;~><
				case '\'':
				case '/':
				case '\\':
				case '-':
				case ',':
				case ')':
				case '(':
				case '!':
				case '`':
				case '\"':
				case ':':
				case ']':
				case '[':
				case '?':
				case ';':
				case '~':
				case '<':
				case '>':
					s.append(" ");
					break;
				default:
					s.append(ch);
					break;
			}
		}
		token = s.toString();
		return token;
	}

	public static List<Match> getNBestMatches(String target, List<String> matchList, int bestNAnswers) {
		List<String> topResults;
		List<Integer> topRatios;
		List<Match> matches;
		Match match;
		String result;
		int ratio;
		int lastRatioIx;
		
		lastRatioIx = bestNAnswers - 1;
		topResults = new ArrayList<>(bestNAnswers);
		topRatios = new ArrayList<>(bestNAnswers);
		for (int i=0; i<bestNAnswers; i++) {
			topResults.add("");
			topRatios.add(0);
		}
		for (String possible : matchList) {
			ratio = FuzzyMatch.getRatio(target, possible, false);
			for (int i=0; i<bestNAnswers; i++) {
				if (ratio > topRatios.get(i)) {
					topRatios.set(i, ratio);
					topResults.set(i, possible);
					continue;
				}
				if (topRatios.get(lastRatioIx) == 100) {
					// all the buckets are maxed out
					break;
				}
			}
		}
		matches = new ArrayList<>(bestNAnswers);
		for (int j=0; j<topResults.size(); j++) {
			result = topResults.get(j);
			ratio = topRatios.get(j);
			match = new Match();
			match.setRatio(ratio);
			match.setString(result);
			matches.add(match);
		}
		Collections.sort(matches);
		return matches;
	}
	
	public static class Match implements Comparable<Match> {
		private Integer ratio;
		private String string;
		
		public int getRatio() {
			
			return ratio;
		}
		
		public void setRatio(int ratio) {
			
			this.ratio = ratio;
		}
		
		public String getString() {
			
			return string;
		}
		
		public void setString(String string) {
			
			A1iciaUtils.checkNotNull(string);
			this.string = string;
		}

		/**
		 * Sort matches in descending order based on ration
		 * 
		 * @param other
		 * @return
		 */
		@Override
		public int compareTo(Match other) {
			
			return other.ratio.compareTo(ratio);
		}
		
	}
}
