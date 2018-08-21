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
package com.hulles.a1icia.charlie.pos;

import com.hulles.a1icia.api.shared.SharedUtils;

/* POS in WordPOS stands for Part Of Speech, not Piece Of Shit
 * 
 */
public class WordPOS {
	private String word;
	private String posTag;
	
	public WordPOS(String word, String posTag) {
		
		SharedUtils.checkNotNull(word);
		SharedUtils.checkNotNull(posTag);
		this.word = word;
		this.posTag = posTag;
	}

	public String getWord() {
		
		return word;
	}

	public void setWord(String word) {
		
		SharedUtils.checkNotNull(word);
		this.word = word;
	}

	public String getPosTag() {
		
		return posTag;
	}

	public void setPosTag(String posTag) {
		
		SharedUtils.checkNotNull(posTag);
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
