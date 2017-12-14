package com.hulles.a1icia.charlie.pos;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.hulles.a1icia.api.shared.ApplicationKeys;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.A1iciaApplication;
import com.hulles.a1icia.cayenne.Lemma;
import com.hulles.a1icia.charlie.CharlieThimk;
import com.hulles.a1icia.tools.A1iciaUtils;

import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.util.InvalidFormatException;

public class CharlieLemmatizer {
	private final LemmatizerME lemmatizer;
//	private final DictionaryLemmatizer dictLemmatizer;	

	public CharlieLemmatizer() {
		URL lemmatizerModelURL;
		LemmatizerModel lemmatizerModel;
//		List<Lemma> dictLemmata;
//		String dictString;
//		StringBuilder sb;
//		InputStream dictStream;
//		String word = null;
//		String pos = null;
//		String lemma = null;
		ApplicationKeys appKeys;
        
        appKeys = ApplicationKeys.getInstance();
        String openNLPPath = appKeys.getOpenNLPPath();
        try {
            lemmatizerModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-lemmatizer.bin"));
        } catch (MalformedURLException ex) {
            throw new A1iciaException("Can't create Lemmatizer URL", ex);
        }
		
		try {
			lemmatizerModel = new LemmatizerModel(lemmatizerModelURL);
		} catch (InvalidFormatException e) {
			throw new A1iciaException("Can't load lemmatizer model format", e);
		} catch (IOException e) {
			throw new A1iciaException("Can't load lemmatizer model", e);
		}
		lemmatizer = new LemmatizerME(lemmatizerModel);
/*		
		dictLemmata = Lemma.getAllLemmas();
		// As it stands, the Lemma database dictionary contains duplicate word-pos keys; 
		//    instructions say to write one key and put a string together of "lemma1#lemma2..."
		//    for the lemma so we get a little elaborate here
		sb = new StringBuilder(dictLemmata.size() * 12);
		// we rely on the list being sorted in Lemma
		for (Lemma dictLemma : dictLemmata) {
			if (dictLemma.getWord().equals(word) && dictLemma.getPos().equals(pos)) {
				// word-pos duplicate, so add the word to the lemma and continue
				lemma += "#" + dictLemma.getLemma();
				continue;
			}
			// non-duplicate so write out saved word, pos and lemma and save the new ones
			sb.append(word);
			sb.append("\t");
			sb.append(pos);
			sb.append("\t");
			sb.append(lemma);
			sb.append("\n");
			word = dictLemma.getWord();
			pos = dictLemma.getPos();
			lemma = dictLemma.getLemma();
		}
		// all done so append the final word, pos and lemma
		sb.append(word);
		sb.append("\t");
		sb.append(pos);
		sb.append("\t");
		sb.append(lemma);
		sb.append("\n");
		dictString = sb.toString();
		dictStream = new ByteArrayInputStream(dictString.getBytes());
		try {
			dictLemmatizer = new DictionaryLemmatizer(dictStream);
		} catch (IOException e) {
			throw new A1iciaException("Can't create dictionary lemmatizer", e);
		}
*/
	}
		
	public String[] generateLemmata(String[] tokens, String[] posTags) {
		String[] lemmata;
		
		A1iciaUtils.checkNotNull(tokens);
		A1iciaUtils.checkNotNull(posTags);
		lemmata = lemmatizer.lemmatize(tokens, posTags);
		return lemmata;
	}
	
	public static String[] generateDictionaryLemmata(String[] tokens, String[] posTags) {
		String[] lemmata;
		List<Lemma> result;
		Lemma lemma;
		
		A1iciaUtils.checkNotNull(tokens);
		A1iciaUtils.checkNotNull(posTags);
		lemmata = new String[tokens.length];
		for (int ix=0; ix<tokens.length; ix++) {
			result = Lemma.getLemmas(tokens[ix], posTags[ix]);
			if (result == null || result.isEmpty()) {
				lemmata[ix] = "O"; // like the original
			} else if (result.size() == 1){
				lemma = result.get(0);
				lemmata[ix] = lemma.getLemma();
			} else {
				lemma = CharlieThimk.chooseLemma(result, tokens[ix], posTags[ix], tokens, posTags);
				lemmata[ix] = lemma.getLemma();
			}
		}
		return lemmata;
	}
	
	public static synchronized void updateDictionaryLemmata(List<String> lemmata, 
			List<String> tokens, List<String> posTags) {
		Lemma lemma;
		String word;
		String dummy;
		
		dummy = Lemma.getDummyLemmaTag();
		A1iciaApplication.setErrorOnUncommittedObjects(false);
		for (int i=0; i<lemmata.size(); i++) {
			word = lemmata.get(i);
			if (word.isEmpty() || word.equals("O")) {
				if (!Lemma.lemmaExists(tokens.get(i), posTags.get(i), dummy)) {
					lemma = Lemma.createNew();
					lemma.setWord(tokens.get(i));
					lemma.setPos(posTags.get(i));
					// we just create a dummy entry for the new lemma,
					//  it needs to be manually edited later by someone, possibly YOU
					lemma.setLemma(dummy);
				}
			}
		}
		A1iciaApplication.commit();
		A1iciaApplication.setErrorOnUncommittedObjects(true);
	}
}
