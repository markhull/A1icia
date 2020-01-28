/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of Alixia.
 *  
 * Alixia is free software: you can redistribute it and/or modify
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
package com.hulles.alixia.echo;

import com.hulles.alixia.api.shared.SharedUtils;

public class WordMatchingRequest {
	private String wordToMatch;
	// WordA is to WordB as WordC is to WordD
	private String analogyWord;
	private String analogyIsTo;
	private String analogyAs;
	
	public String getWordToMatch() {
		
		return wordToMatch;
	}
	
	public void setWordToMatch(String word) {
		
		SharedUtils.checkNotNull(word);
		this.wordToMatch = word;
	}
	
	public String getAnalogyWord() {
		
		return analogyWord;
	}
	
	public void setAnalogyWord(String word) {
		
		SharedUtils.checkNotNull(word);
		this.analogyWord = word;
	}
	
	public String getAnalogyIsTo() {
		
		return analogyIsTo;
	}
	
	public void setAnalogyIsTo(String word) {
		
		SharedUtils.checkNotNull(word);
		this.analogyIsTo = word;
	}
	
	public String getAnalogyAs() {
		
		return analogyAs;
	}
	
	public void setAnalogyAs(String word) {
		
		SharedUtils.checkNotNull(word);
		this.analogyAs = word;
	}
	
}
