package com.hulles.alixia.tools;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.hulles.alixia.api.shared.SharedUtils;

/**
 * FuzzyMatch returns a quantified match between one string and another using the 
 * Levenshtein Distance. It actually does a pretty good job of returning interesting results, even
 * compared to the beefier fuzzywuzzy stuff implemented elsewhere. Not my code originally, TODO document
 * where I got this from.
 * 
 * @author hulles
 *
 */
public class FuzzyMatch {
	private final static Logger LOGGER = LoggerFactory.getLogger(FuzzyMatch.class);
	private static final LevenshteinDistance LEVDISTANCE;

	static {
		LEVDISTANCE = new LevenshteinDistance();
	}
	
	/*
	 * t0 = [SORTED_INTERSECTION] 
	 * t1 = [SORTED_INTERSECTION] + [SORTED_REST_OF_STRING1] 
	 * t2 = [SORTED_INTERSECTION] + [SORTED_REST_OF_STRING2]
	 * 
	 * outcome = max(t0,t1,t2)
	 * 
	 */
	
	public static int getRatio(String str1, String str2, boolean debug) {
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
		String t0 = "";
		String t1 = "";
		String t2 = "";
		String xstr1;
		String xstr2;
		
		SharedUtils.checkNotNull(str1);
		SharedUtils.checkNotNull(str2);
		if (str1.length() >= str2.length()) {		
			// We need to swap s1 and s2		
			xstr2 = str1;
			xstr1 = str2;			
		} else {
		    xstr1 = str1;
		    xstr2 = str2;
		}

		// Get alpha numeric characters
		
		xstr1 = escapeString(xstr1);
		xstr2 = escapeString(xstr2);
		
		xstr1 = xstr1.toLowerCase();
		xstr2 = xstr2.toLowerCase();
				
		set1 = new HashSet<>();
		set2 = new HashSet<>();
		
		//split the string by space and store words in sets
		st1 = new StringTokenizer(xstr1);		
		while (st1.hasMoreTokens()) {
			set1.add(st1.nextToken());
		}

		st2 = new StringTokenizer(xstr2);		
		while (st2.hasMoreTokens()) {
			set2.add(st2.nextToken());
		}
		
		intersection = Sets.intersection(set1, set2);
		
		sortedIntersection = Sets.newTreeSet(intersection);

	    LOGGER.debug("Sorted intersection --> ");
		for (String s : sortedIntersection) {
            LOGGER.debug(s);
		}
		
		// Find out difference of sets set1 and intersection of set1,set2
		restOfSet1 = Sets.symmetricDifference(set1, intersection);
		sortedRestOfSet1 = Sets.newTreeSet(restOfSet1);
		restOfSet2 = Sets.symmetricDifference(set2, intersection);
		sortedRestOfSet2 = Sets.newTreeSet(restOfSet2);
		
	    LOGGER.debug("Sorted rest of 1 --> ");
		for (String s : sortedRestOfSet1) {
            LOGGER.debug(s);
		}
	    LOGGER.debug("Sorted rest of 2 --> ");
		for (String s : sortedRestOfSet2) {
            LOGGER.debug(s);
		}

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
		
        LOGGER.debug("t0 = {} --> {}", t0, amt1);
        LOGGER.debug("t1 = {} --> {}", t1, amt2);
        LOGGER.debug("t2 = {} --> {}", t2, amt3);
		
		finalScore = Math.max(Math.max(amt1, amt2), amt3);
		return finalScore;	
	}
	
	public static int calculateLevensteinDistance(String s1, String s2) {
		int distance;
		double ratio;
		
		SharedUtils.checkNotNull(s1);
		SharedUtils.checkNotNull(s2);
		distance = LEVDISTANCE.apply(s1, s2);
		ratio = ((double) distance) / (Math.max(s1.length(), s2.length()));
		return 100 - ((Double)(ratio*100)).intValue();		
	}
	
	public static String escapeString(String token) {
		StringBuffer s;
		CharacterIterator it;
		String tokenOut;
		
		SharedUtils.checkNotNull(token);
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
		tokenOut = s.toString();
		return tokenOut;
	}

	public static List<Match> getNBestMatches(String target, List<String> matchList, int bestNAnswers) {
	
		return getNBestMatches(target, matchList, bestNAnswers, true);
	}
	
	/**
	 * Return a set of at most N best fuzzy matches to the target string, sorted 
	 * best to worst match.
	 * 
	 * @param target The string to compare
	 * @param matchList The list of possible matching strings
	 * @param bestNAnswers The number of best matches to return
	 * @param skipExact True if we are to ignore exact matches
	 * @return A list of 0 to N best fuzzy matches to the target string
	 */
	public static List<Match> getNBestMatches(String target, List<String> matchList, int bestNAnswers, boolean skipExact) {
		List<Match> matches;
		Match match;
		int ratio;
		int answers;
		
		SharedUtils.checkNotNull(target);
		SharedUtils.checkNotNull(matchList);
		SharedUtils.checkNotNull(bestNAnswers);
		SharedUtils.checkNotNull(skipExact);
		matches = new ArrayList<>(matchList.size());
		for (String possible : matchList) {
			if (possible == null || possible.isEmpty()) {
				continue;
			}
			if (skipExact && (target.equals(possible))) {
				continue;
			}
			ratio = FuzzyMatch.getRatio(target, possible, false);
			match = new Match();
			match.setRatio(ratio);
			match.setString(possible);
			matches.add(match);
		}
		if (bestNAnswers > matches.size()) {
			answers = matches.size();
		} else {
		    answers = bestNAnswers;
		}
		Collections.sort(matches);
		return matches.subList(0, answers);
	}
	
	public static class Match implements Comparable<Match> {
		private Integer ratio;
		private String string;
		
		public Integer getRatio() {
			
			return ratio;
		}
		
		public void setRatio(Integer ratio) {
			
			SharedUtils.checkNotNull(ratio);
			this.ratio = ratio;
		}
		
		public String getString() {
			
			return string;
		}
		
		public void setString(String string) {
			
			SharedUtils.checkNotNull(string);
			this.string = string;
		}

		/**
		 * Sort matches in descending order based on ratio
		 * 
		 * @param other The Match to which this is to be compared
		 * @return The value 0 if this Match is equal to the argument Match; a value less than 0 if 
		 * this Match is numerically greater than the argument Match; and a value greater than 0 if 
		 * this Match is numerically less than the argument Match (signed comparison).

		 */
		@Override
		public int compareTo(Match other) {
			Integer otherRatio;
			
			SharedUtils.checkNotNull(other);
			otherRatio = other.getRatio();
			return otherRatio.compareTo(getRatio());
		}
		
	}
}
