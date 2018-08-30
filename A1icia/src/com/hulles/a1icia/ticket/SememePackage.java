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
package com.hulles.a1icia.ticket;

import com.hulles.a1icia.api.jebus.JebusBible;
import com.hulles.a1icia.api.jebus.JebusBible.JebusKey;
import com.hulles.a1icia.api.jebus.JebusHub;
import com.hulles.a1icia.api.jebus.JebusPool;
import com.hulles.a1icia.api.shared.A1iciaException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.cayenne.Sememe;

import redis.clients.jedis.Jedis;

/**
 * A SememePackage is a bundle of sememe-related fields: sememe, optional sememe object type, etc. For
 * example: Sememe = "play_video", SememeObjectType = "VIDEOTITLE", SememeObject = "Un Chien Andalou".
 * So given a SememePackage, we should know what action to perform ("play_video") and what the object of 
 * the action is ("Un Chien Andalou", a movie), but we haven't yet performed the action.
 * <p>
 * TODO The SememeObjectType turns out to be not very useful and a pain to keep track of, so we should
 * change this. Also the confidence rating has not been used much yet, so consider it tentative.
 * Pretty sure about the sememe though.
 * 
 * @author hulles
 *
 */
public class SememePackage {
	private SerialSememe sememe;
	private String sememeObject;
	private SentencePackage sentencePackage;	
	private Integer confidence; // 0 - 100, 100 = certainty
	private final String idString;
	
	private SememePackage() {
		long idValue;

		idValue = getNewSememePackageID();
		this.idString = "SK" + idValue;
	}
	
	public String getSememePackageID() {
		
		return idString;
	}

	public SerialSememe getSememe() {
		
		return sememe;
	}

	public void setSememe(SerialSememe sememe) {
		
		SharedUtils.checkNotNull(sememe);
		this.sememe = sememe;
	}

	public String getSememeObject() {
		
		return sememeObject;
	}

	public void setSememeObject(String object) {
		
		SharedUtils.nullsOkay(object);
		this.sememeObject = object;
	}

	public SentencePackage getSentencePackage() {
		
		return sentencePackage;
	}

	public void setSentencePackage(SentencePackage pkg) {
		
		SharedUtils.nullsOkay(pkg);
		this.sentencePackage = pkg;
	}

	public Integer getConfidence() {
		
		return confidence;
	}

	public void setConfidence(Integer confidence) {
		
		SharedUtils.checkNotNull(confidence);
		this.confidence = confidence;
	}
	
	public boolean is(String name) {
	
		SharedUtils.checkNotNull(name);
		return name.equals(sememe.getName());
	}
	
	public String getName() {
		
		return sememe.getName();
	}
	
	/**
	 * Return the sememe package with the named sememe if it's in the list, null otherwise.
	 * 
	 * @param name The sememe name
	 * @param sememePackages The list of sememe packages that possibly contains the named sememe
	 * @return The package with the named sememe, or null if not a member of the list
	 */
	public static SememePackage has(String name, List<SememePackage> sememePackages) {
		
		SharedUtils.checkNotNull(name);
		SharedUtils.checkNotNull(sememePackages);
		for (SememePackage pkg : sememePackages) {
			if (pkg.is(name)) {
				return pkg;
			}
		}
		return null;
	}
	
	/**
	 * Remove and return the package with the named sememe if it's in the list, otherwise
	 * return null.
	 * 
	 * @param name The sememe name
	 * @param sememePackages The list of sememes that possibly contains the named sememe
	 * @return The named sememe, or null if not a member of the list
	 */
	public static SememePackage consume(String name, List<SememePackage> sememePackages) {
		SememePackage pkg;
		
		SharedUtils.checkNotNull(name);
		SharedUtils.checkNotNull(sememePackages);
		for (Iterator<SememePackage> iter = sememePackages.iterator(); iter.hasNext(); ) {
			pkg = iter.next();
			if (pkg.is(name)) {
				iter.remove();
				return pkg;
			}
		}
		return null;
	}
	
	/**
	 * Remove and return the named sememe if it's in the list, otherwise
	 * return null. That sememe should be the last one in the list; i.e. the
	 * set should be empty after its removal.
	 * 
	 * @param name The sememe name
	 * @param sememePackages The list of sememes that possibly contains the named sememe
	 * @return The named sememe, or null if not a member of the list
	 */
	public static SememePackage consumeFinal(String name, List<SememePackage> sememePackages) {
		SememePackage pkg;
		SememePackage foundPkg = null;
		
		SharedUtils.checkNotNull(name);
		SharedUtils.checkNotNull(sememePackages);
		for (Iterator<SememePackage> iter = sememePackages.iterator(); iter.hasNext(); ) {
			pkg = iter.next();
			if (pkg.is(name)) {
				foundPkg = pkg;
				iter.remove();
				break;
			}
		}
		if (!sememePackages.isEmpty()) {
			throw new A1iciaException("Sememe package list not empty after consumeFinal");
		}
		return foundPkg;
	}
	
	private static long getNewSememePackageID() {
		JebusPool jebusPool;
        String key;
        
		jebusPool = JebusHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			key = JebusBible.getStringKey(JebusKey.ALICIASEMEMECOUNTERKEY, jebusPool);
			return jebus.incr(key);
		}		
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
		if (!(obj instanceof SememePackage)) {
			return false;
		}
		SememePackage other = (SememePackage) obj;
		if (idString == null) {
			if (other.idString != null) {
				return false;
			}
		} else if (!idString.equals(other.idString)) {
			return false;
		}
		return true;
	}
	
	public static SememePackage getNewPackage() {

		return new SememePackage();
	}
	
	public static SememePackage getDefaultPackage(SerialSememe sememe) {
		SememePackage pkg;
		
		SharedUtils.checkNotNull(sememe);
		pkg = new SememePackage();
		pkg.setSememe(sememe);
		pkg.setConfidence(0);
		return pkg;
	}
	public static SememePackage getDefaultPackage(String name) {
		SememePackage pkg;
		SerialSememe sememe;
		
		SharedUtils.checkNotNull(name);
		sememe = SerialSememe.find(name);
		pkg = new SememePackage();
		pkg.setSememe(sememe);
		pkg.setConfidence(0);
		return pkg;
	}
	
	public static List<SememePackage> getSingletonDefault(SerialSememe sememe) {
		SememePackage pkg;
		
		SharedUtils.checkNotNull(sememe);
		pkg = getDefaultPackage(sememe);
		return Collections.singletonList(pkg);
	}
	public static List<SememePackage> getSingletonDefault(String name) {
		SememePackage pkg;
		SerialSememe sememe;
		
		SharedUtils.checkNotNull(name);
		sememe = SerialSememe.find(name);
		pkg = getDefaultPackage(sememe);
		return Collections.singletonList(pkg);
	}
	
	public static SememePackage getProxyPackage() {
		SerialSememe sememe;
		
		sememe = Sememe.getProxySememe();
		return getDefaultPackage(sememe);
	}
	
	public boolean isValid() {
		
		if (sememe == null) {
			return false;
		}
		
		// sememeObject can be null
		
		// sentencePackage can be null
		
		if (confidence == null) {
			return false;
		}
		if (idString == null) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		
		return "Sememe Package for " + getName();
	}
}
