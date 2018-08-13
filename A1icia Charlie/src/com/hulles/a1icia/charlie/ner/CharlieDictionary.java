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
package com.hulles.a1icia.charlie.ner;

import java.util.List;

import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.OwmCity;
import com.hulles.a1icia.cayenne.Person;
import com.hulles.a1icia.tools.A1iciaUtils;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.util.StringList;

public class CharlieDictionary extends Dictionary {
	private final DictionaryType dictionaryType;
	private int maxTokens = 0;
	private Dictionary orgDictionary;
	
	public CharlieDictionary(DictionaryType type, TokenizerME tokenizer) {
		String[] tokens;
		StringList orgList;
		
		A1iciaUtils.checkNotNull(type);
		this.dictionaryType = type;
		switch (type) {
			case PERSON:
				maxTokens = 3;
				break;
			case LOCATION:
				maxTokens = 12;
				break;
			case ORGANIZATION:
				orgDictionary = new Dictionary(false);
				tokens = tokenizer.tokenize("Hulles Industries LLC");
				orgList = new StringList(tokens);
				orgDictionary.put(orgList);
				if (tokens.length > maxTokens) {
					maxTokens = tokens.length;
				}
				tokens = tokenizer.tokenize("A1icia Death Commandos");
				orgList = new StringList(tokens);
				orgDictionary.put(orgList);
				if (tokens.length > maxTokens) {
					maxTokens = tokens.length;
				}
				break;
			default:
				throw new A1iciaException("CharlieDictionary: unsupported dictionary type = " + type.name());
		}
	}
	
	@Override
	public int getMinTokenCount() {
		
		return 1;
	}

	@Override
	public int getMaxTokenCount() {

		return maxTokens;
	}

	@Override
	public boolean contains(StringList tokens) {
		String tokenStr;
		StringBuilder sb;
		List<Person> persons;
		Long cityCount;
		
		A1iciaUtils.checkNotNull(tokens);
		sb = new StringBuilder();
		for (String token : tokens) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(token);
		}
		tokenStr = sb.toString();
		switch (dictionaryType) {
			case ORGANIZATION:
				return orgDictionary.contains(tokens);
			case PERSON:
				persons = Person.getPersonsByFirstName(tokens.getToken(0) + "%");
				if (persons.isEmpty()) {
					return false;
				}
				if (tokens.size() == 1) {
					return true;
				}
				for (Person person : persons) {
					if (person.getFullNameFL().startsWith(tokenStr)) {
						return true;
					}
				}
				return false;
			case LOCATION:
				cityCount = OwmCity.countOwmCities(tokenStr + "%");
				return (cityCount > 0);
			default:
				throw new A1iciaException("CharlieDictionary: unsupported dictionary type = " + 
						dictionaryType.name());
		}
	}

	public enum DictionaryType {
		PERSON,
		ORGANIZATION,
		LOCATION
	}
}
