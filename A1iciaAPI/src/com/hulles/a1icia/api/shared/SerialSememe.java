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
package com.hulles.a1icia.api.shared;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * Sememe is an important class in A1icia. It represents an object that causes an action of some
 * kind to take place: a question that prompts an answer, a command that prompts a response, and
 * so forth.
 *  <p>
 *  SerialSememe is a pared-down version of the main (non-API) A1icia Sememe. 
 *  
 * @author hulles
 *
 */
public class SerialSememe implements Serializable, Comparable<SerialSememe> {
	private static final long serialVersionUID = 980860858637714677L;
	private String sememeName;
	private String canonicalForm;
	private Boolean externalUse;
	private Boolean adminOnly;
	private Boolean loggedIn;
	private static Set<SerialSememe> sememeSet = null;
	
    public SerialSememe() {
    	// need no-arg constructor
    }
    
    public static void setSememes(Set<SerialSememe> sememes) {
    	
    	SharedUtils.checkNotNull(sememes);
    	sememeSet = sememes;
    }
    
    /**
     * Find a sememe in the set of all sememes. If not found, return null.
     * @param name The name of the sememe
     * @return The sememe, or null if not found
     */
    public static SerialSememe possiblyFind(String name) {
    	SerialSememe sememe = null;
    	
    	SharedUtils.checkNotNull(name);
    	if (sememeSet == null) {
    		throw new A1iciaAPIException("SerialSememe: sememe set not loaded");
    	}
    	for (SerialSememe spk : sememeSet) {
    		if (spk.is(name)) {
    			sememe = spk;
    			break;
    		}
    	}
    	return sememe;
    }
    

    /**
     * Find a sememe in the set of all sememes. If the name is not found, throw an {@link A1iciaAPIException}.
     * @param name The name of the sememe to find
     * @return The sememe
     */
    public static SerialSememe find(String name) {
    	SerialSememe sememe = null;
    	
    	SharedUtils.checkNotNull(name);
    	sememe = possiblyFind(name);
    	if (sememe == null) {
    		throw new A1iciaAPIException("Unable to find sememe named " + name);
    	}
    	return sememe;
    }
    
    public boolean is(String name) {
    
    	SharedUtils.nullsOkay(name);
    	return getName().equals(name);
    }
    
    public Boolean getExternalUse() {
    	
		return externalUse;
	}

	public void setExternalUse(Boolean externalUse) {
		
		SharedUtils.checkNotNull(externalUse);
		this.externalUse = externalUse;
	}

	public String getName() {
    	
		return sememeName;
	}

	public void setName(String sememeName) {
		
		SharedUtils.checkNotNull(sememeName);
		this.sememeName = sememeName;
	}

	public String getCanonicalForm() {
		
		return canonicalForm;
	}

	public void setCanonicalForm(String canonicalForm) {
		
		SharedUtils.checkNotNull(canonicalForm);
		this.canonicalForm = canonicalForm;
	}

	public Boolean getAdminOnly() {
		
		return adminOnly;
	}

	public void setAdminOnly(Boolean adminOnly) {
		
		SharedUtils.checkNotNull(adminOnly);
		this.adminOnly = adminOnly;
	}

	public Boolean getLoggedIn() {
		
		return loggedIn;
	}

	public void setLoggedIn(Boolean loggedIn) {
		
		SharedUtils.checkNotNull(loggedIn);
		this.loggedIn = loggedIn;
	}
	
	/**
	 * Remove and return SerialSememe if it's in the list, otherwise
	 * return null.
	 * 
	 * @param name The sememe name
	 * @param sememes The set of sememes that possibly contains the named sememe
	 * @return The named sememe, or null if not a member of the set
	 */
	public static SerialSememe consume(String name, Set<SerialSememe> sememes) {
		SerialSememe sememe;
		
		SharedUtils.checkNotNull(name);
		SharedUtils.checkNotNull(sememes);
		for (Iterator<SerialSememe> iter = sememes.iterator(); iter.hasNext(); ) {
			sememe = iter.next();
			if (sememe.is(name)) {
				iter.remove();
				return sememe;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		SerialSememe other;
		
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SerialSememe)) {
			return false;
		}
		other = (SerialSememe) obj;
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		return true;
	}

	/**
	 * This compareTo compares sememes on their names.
	 * It is case-insensitive.
	 * 
	 */
	@Override
	public int compareTo(SerialSememe otherSememe) {
		
		SharedUtils.checkNotNull(otherSememe);
        return this.getName().compareToIgnoreCase(otherSememe.getName());
	}

	@Override
	public String toString() {
		StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append("SEMEME: Name: ");
		sb.append(this.getName());
		sb.append(" Canonical Form: ");
		sb.append(this.getCanonicalForm());
		if (this.getAdminOnly()) {
			sb.append(" (admin only)");
		}
		if (this.getExternalUse()) {
			sb.append(" (external use)");
		}
		if (this.getLoggedIn()) {
			sb.append(" (logged in)");
		}
		return sb.toString();
	}

}
