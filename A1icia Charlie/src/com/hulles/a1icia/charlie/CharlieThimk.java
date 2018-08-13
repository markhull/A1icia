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
