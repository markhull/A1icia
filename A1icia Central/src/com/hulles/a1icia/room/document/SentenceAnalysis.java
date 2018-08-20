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
package com.hulles.a1icia.room.document;

import java.util.List;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.base.A1iciaException;

/**
 * The result of the NLP analysis for this sentence, among all the sentences discerned in the
 * query from the A1ician.
 * 
 * @author hulles
 *
 */
final public class SentenceAnalysis {
	private final String sentence;
	private List<String> tokens = null;
	private List<String> nerPersons = null;
	private List<String> nerLocations  = null;
	private List<String> nerOrganizations = null;
	private List<String> nerDates = null;
	private List<String> nerTimes = null;
	private List<String> nerMoney = null;
	private List<String> nerPercentages = null;
	private List<String> myriaCitizens = null;
	private List<String> myriaLocations  = null;
	private List<String> myriaOrganizations = null;
	// posTags and posTagDefs and lemmata match up 1:1 with tokens
	private List<String> posTags = null;
	private List<String> posTagDefs = null;
	private List<String> lemmata = null;
	private List<String> dictionaryLemmata = null;
	private List<String> chunkTags = null;
	private List<String> chunkSegments = null;
	private String docCategory = null;
	
	public SentenceAnalysis(String sentence) {
		
		SharedUtils.checkNotNull(sentence);
		this.sentence = sentence;
	}
	
	public String getSentence() {
		
		return sentence;
	}

	public List<String> getPOSTagDefinitions() {
		
		return posTagDefs;
	}

	public void setPOSTagDefinitions(List<String> posTagDefs) {
		
		SharedUtils.checkNotNull(posTagDefs);
		this.posTagDefs = posTagDefs;
	}
	
	public List<String> getLemmata() {
		
		return lemmata;
	}

	public void setLemmata(List<String> lemmata) {
		
		SharedUtils.checkNotNull(lemmata);
		this.lemmata = lemmata;
	}

	public List<String> getDictionaryLemmata() {
		
		return dictionaryLemmata;
	}

	public void setDictionaryLemmata(List<String> dictionaryLemmata) {
		
		SharedUtils.checkNotNull(dictionaryLemmata);
		this.dictionaryLemmata = dictionaryLemmata;
	}

	public List<String> getTokens() {
		
		return tokens;
	}

	public void setTokens(List<String> tokens) {
		
		SharedUtils.checkNotNull(tokens);
		this.tokens = tokens;
	}
	
	public List<String> getNERPersons() {
		
		return nerPersons;
	}
	
	public void setNERPersons(List<String> persons) {
		
		SharedUtils.checkNotNull(persons);
		this.nerPersons = persons;
	}
	
	public List<String> getNERLocations() {
		
		return nerLocations;
	}
	
	public void setNERLocations(List<String> locations) {
		
		SharedUtils.checkNotNull(locations);
		this.nerLocations = locations;
	}
	
	public List<String> getNEROrganizations() {
		
		return nerOrganizations;
	}
	
	public void setNEROrganizations(List<String> organizations) {
		
		SharedUtils.checkNotNull(organizations);
		this.nerOrganizations = organizations;
	}
	
	public List<String> getNERDates() {
		
		return nerDates;
	}

	public void setNERDates(List<String> dates) {
		
		SharedUtils.checkNotNull(dates);
		this.nerDates = dates;
	}

	public List<String> getNERTimes() {
		
		return nerTimes;
	}

	public void setNERTimes(List<String> times) {
		
		SharedUtils.checkNotNull(times);
		this.nerTimes = times;
	}

	public List<String> getNERMoney() {
		
		return nerMoney;
	}

	public void setNERMoney(List<String> money) {
		
		SharedUtils.checkNotNull(money);
		this.nerMoney = money;
	}

	public List<String> getNERPercentages() {
		
		return nerPercentages;
	}

	public void setNERPercentages(List<String> percentages) {
		
		SharedUtils.checkNotNull(percentages);
		this.nerPercentages = percentages;
	}

	public List<String> getMyriaCitizens() {
		
		return myriaCitizens;
	}

	public void setMyriaCitizens(List<String> myriaCitizens) {
		
		SharedUtils.checkNotNull(myriaCitizens);
		this.myriaCitizens = myriaCitizens;
	}

	public List<String> getMyriaLocations() {
		
		return myriaLocations;
	}

	public void setMyriaLocations(List<String> myriaLocations) {
		
		SharedUtils.checkNotNull(myriaLocations);
		this.myriaLocations = myriaLocations;
	}

	public List<String> getMyriaOrganizations() {
		
		return myriaOrganizations;
	}

	public void setMyriaOrganizations(List<String> myriaOrganizations) {
		
		SharedUtils.checkNotNull(myriaOrganizations);
		this.myriaOrganizations = myriaOrganizations;
	}

	public List<String> getPOSTags() {
		
		return posTags;
	}
	
	public void setPOSTags(List<String> posTags) {
		
		SharedUtils.checkNotNull(posTags);
		this.posTags = posTags;
	}
	
	public List<String> getChunkTags() {
		
		return chunkTags;
	}
	
	public void setChunkTags(List<String> tags) {
		
		SharedUtils.checkNotNull(tags);
		this.chunkTags = tags;
	}
	
	public List<String> getChunkSegments() {
		
		return chunkSegments;
	}
	
	public void setChunkSegments(List<String> segments) {
		
		SharedUtils.checkNotNull(segments);
		this.chunkSegments = segments;
	}
	
	public String getDocumentCategory() {
		
		return docCategory;
	}
	
	public void setDocumentCategory(String category) {
		
		SharedUtils.checkNotNull(category);
		this.docCategory = category;
	}

	@Override
	public String toString() {
		StringBuilder sb;
		
		if (posTags.size() != posTagDefs.size()) {
			throw new A1iciaException("POS tags (" + posTags.size() + ") and tag defs (" +
					posTagDefs.size() + ") mismatch");
		}
		sb = new StringBuilder();
		sb.append("<style type='text/css'>");
		sb.append("dt {");
        sb.append("font-weight: bold;");
    	sb.append("}");
		sb.append("</style>\n");
		sb.append("<h3>SENTENCE ANALYSIS</h3>");
		sb.append("<dl style=''>\n");
		
		sb.append("<dt>SENTENCE</dt>\n");
		sb.append("<dd>");
		sb.append(sentence);
		sb.append("</dd>\n");
		
		sb.append("<dt>TOKENS</dt>\n");
		sb.append("<dd>\n");
		for (String s : tokens) {
			sb.append(s + "<br />\n");
		}
		sb.append("</dd>\n");
		
		sb.append("<dt>NER PERSONS</dt>\n");
		sb.append("<dd>\n");
		for (String s : nerPersons) {
			sb.append(s + "<br />\n");
		}
		sb.append("</dd>\n");
		
		sb.append("<dt>NER LOCATIONS</dt>\n");
		sb.append("<dd>\n");
		for (String s : nerLocations) {
			sb.append(s + "<br />\n");
		}
		sb.append("</dd>\n");
		
		sb.append("<dt>NER ORGANIZATIONS</dt>\n");
		sb.append("<dd>\n");
		for (String s : nerOrganizations) {
			sb.append(s + "<br />\n");
		}
		sb.append("</dd>\n");
		
		sb.append("<dt>MYRIA CITIZENS</dt>\n");
		sb.append("<dd>\n");
		for (String s : myriaCitizens) {
			sb.append(s + "<br />\n");
		}
		sb.append("</dd>\n");
		
		sb.append("<dt>MYRIA LOCATIONS</dt>\n");
		sb.append("<dd>\n");
		for (String s : myriaLocations) {
			sb.append(s + "<br />\n");
		}
		sb.append("</dd>\n");
		
		sb.append("<dt>MYRIA ORGANIZATIONS</dt>\n");
		sb.append("<dd>\n");
		for (String s : myriaOrganizations) {
			sb.append(s + "<br />\n");
		}
		sb.append("</dd>\n");
		
		sb.append("<dt>POS TAGS</dt>\n");
		sb.append("<dd>\n");
		for (int ix = 0; ix < posTags.size(); ix++) {
			sb.append(posTags.get(ix));
			sb.append(" (");
			sb.append(posTagDefs.get(ix));
			sb.append(") : ");
			sb.append(tokens.get(ix));
			sb.append("<br />\n");
		}
		sb.append("</dd>\n");
		
		sb.append("<dt>CHUNK TAGS</dt>\n");
		sb.append("<dd>\n");
		for (int ix = 0; ix < chunkTags.size(); ix++) {
			sb.append(chunkTags.get(ix));
			sb.append(" : ");
			sb.append(tokens.get(ix));
			sb.append("<br />\n");
		}
		sb.append("</dd>\n");
		
		sb.append("<dt>CHUNK SPANS</dt>\n");
		sb.append("<dd>\n");
		for (String span : chunkSegments) {
			sb.append(span + "<br />\n");
		}
		sb.append("</dd>\n");
		
		sb.append("<dt>LEMMATA</dt>\n");
		sb.append("<dd>\n");
		for (String lemma : lemmata) {
			sb.append(lemma + "<br />\n");
		}
		sb.append("</dd>\n");
		
		sb.append("<dt>DICTIONARY LEMMATA</dt>\n");
		sb.append("<dd>\n");
		for (String lemma : dictionaryLemmata) {
			sb.append(lemma + "<br />\n");
		}
		sb.append("</dd>\n");
		
		sb.append("<dt>DOCUMENT CATEGORY</dt>\n");
		sb.append("<dd>");
		sb.append(docCategory);
		sb.append("</dd>\n");
		sb.append("</dl>\n");
		
		return sb.toString();
	}
	
}
