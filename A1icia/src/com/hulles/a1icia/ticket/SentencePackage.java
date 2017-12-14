/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
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
 *******************************************************************************/
package com.hulles.a1icia.ticket;

import java.util.List;

import com.hulles.a1icia.jebus.JebusBible;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.jebus.JebusPool;
import com.hulles.a1icia.room.document.SentenceAnalysis;
import com.hulles.a1icia.tools.A1iciaUtils;

import redis.clients.jedis.Jedis;

/**
 * A SentencePackage is a bundle of different NLP analyses of a given sentence in a query.
 * 
 * @author hulles
 *
 */
public class SentencePackage {
	private String inputSentence;
	private String strippedSentence; // lower case, doesn't have ending punctuation or trailing spaces
	private String lemmatizedSentence;
	private String posTagString;
	private SentenceAnalysis analysis;
	private final String idString;
	private List<SentenceChunk> chunks;
	
	public SentencePackage() {
		long idValue;
		
		idValue = getNewSentencePackageID();
		this.idString = "SP" + idValue;
	}

	public String getSentencePackageID() {
		
		return idString;
	}
	
	public List<SentenceChunk> getChunks() {
		
		return chunks;
	}

	public void setChunks(List<SentenceChunk> chunks) {
		
		A1iciaUtils.checkNotNull(chunks);
		this.chunks = chunks;
	}

	public String getPosTagString() {
		
		return posTagString;
	}

	public void setPosTagString(String posTagString) {
		
		A1iciaUtils.checkNotNull(posTagString);
		this.posTagString = posTagString;
	}

	public String getInputSentence() {
		
		return inputSentence;
	}

	public void setInputSentence(String inputSentence) {
		
		A1iciaUtils.checkNotNull(inputSentence);
		this.inputSentence = inputSentence;
	}

	public String getStrippedSentence() {
		
		return strippedSentence;
	}

	public void setStrippedSentence(String sentence) {
		
		A1iciaUtils.checkNotNull(sentence);
		this.strippedSentence = sentence;
	}

	public String getLemmatizedSentence() {
		
		return lemmatizedSentence;
	}

	public void setLemmatizedSentence(String lemmatizedSentence) {
		
		A1iciaUtils.nullsOkay(lemmatizedSentence);
		this.lemmatizedSentence = lemmatizedSentence;
	}

	public SentenceAnalysis getAnalysis() {
		
		return analysis;
	}

	public void setAnalysis(SentenceAnalysis analysis) {
		
		A1iciaUtils.nullsOkay(analysis);
		this.analysis = analysis;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idString == null) ? 0 : idString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SentencePackage)) {
			return false;
		}
		SentencePackage other = (SentencePackage) obj;
		if (idString == null) {
			if (other.idString != null) {
				return false;
			}
		} else if (!idString.equals(other.idString)) {
			return false;
		}
		return true;
	}
	
	private static long getNewSentencePackageID() {
		JebusPool jebusPool;
		
		jebusPool = JebusHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			return jebus.incr(JebusBible.getA1iciaSentenceCounterKey(jebusPool));
		}		
	}
	
	public static class SentenceChunk {
		private final String chunk;
		private String posTagString;
		private final Short sequence;
		
		public SentenceChunk(Short sequence, String chunk) {
			
			A1iciaUtils.checkNotNull(sequence);
			A1iciaUtils.checkNotNull(chunk);
			this.chunk = chunk;
			this.sequence = sequence;
		}

		public String getPosTagString() {
			
			return posTagString;
		}

		public void setPosTagString(String string) {
			
			A1iciaUtils.checkNotNull(string);
			this.posTagString = string;
		}

		public String getChunk() {
			
			return chunk;
		}

		public Short getSequence() {
			
			return sequence;
		}
	}
	
}
