package com.hulles.a1icia.charlie;

import java.util.List;
import java.util.Random;

import com.hulles.a1icia.cayenne.Lemma;

/**
 * CharlieThimk is Charlie's version of the Thimk class in A1icia Overmind; that is, it
 * implements decision-making methods for various choice points in the processing. Also
 * like Thimk, it currently chooses at random.
 * 
 * @see Thimk
 * 
 * @author hulles
 *
 */
public class CharlieThimk {
	private final static Random RANDOM = new Random();
	
	public static Lemma chooseLemma(List<Lemma> lemmata, String token, String posTag, 
			String[] tokens, String[] posTags) {
		int ix;
		
		ix = RANDOM.nextInt(lemmata.size());
		return lemmata.get(ix);
	}
}
