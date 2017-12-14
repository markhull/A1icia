package com.hulles.a1icia.charlie.pos;

import com.hulles.a1icia.tools.A1iciaUtils;

/* POS in WordPOS stands for Part Of Speech, not Piece Of Shit
 * 
 */
public class WordPOS {
	private String word;
	private String posTag;
	
	public WordPOS(String word, String posTag) {
		
		A1iciaUtils.checkNotNull(word);
		A1iciaUtils.checkNotNull(posTag);
		this.word = word;
		this.posTag = posTag;
	}

	public String getWord() {
		
		return word;
	}

	public void setWord(String word) {
		
		A1iciaUtils.checkNotNull(word);
		this.word = word;
	}

	public String getPosTag() {
		
		return posTag;
	}

	public void setPosTag(String posTag) {
		
		A1iciaUtils.checkNotNull(posTag);
		this.posTag = posTag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((posTag == null) ? 0 : posTag.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual;
		WordPOS other;
		
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof WordPOS)) {
			return false;
		}
		other = (WordPOS)obj;
		if (other.getWord() == null || other.getPosTag() == null) {
			return false;
		}
		isEqual = (word.equals(other.getWord()) && posTag.equals(other.getPosTag()));
		return isEqual;
	}
	
}
