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
package com.hulles.alixia.cayenne;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.cayenne.auto._Sememe;

public class Sememe extends _Sememe implements Comparable<Sememe> {
    private static final long serialVersionUID = 1L; 
    
//    public static SerialSememe findSememe(Integer sememeID) {
//		ObjectContext context;
//		Sememe sememe;
//		
//		SharedUtils.checkNotNull(sememeID);
//		context = AlixiaApplication.getEntityContext();
//		sememe = Cayenne.objectForPK(context, Sememe.class, sememeID);
//		if (sememe == null) {
//			return null;
//		}
//		return sememe.toSerial();
//    }
    
//    public static SerialSememe find(String nm) {
//		Sememe sememe;
//
//		sememe = findRaw(nm);
//		if (sememe == null) {
//			AlixiaUtils.error("Sememe: cannot find sememe named " + nm, "Returning null sememe");
//			return null;
//		}
//		return sememe.toSerial();
//    }
    
    private static Sememe findRaw(String nm) {
		ObjectContext context;
		Sememe sememe;
		
		SharedUtils.checkNotNull(nm);
		context = AlixiaApplication.getEntityContext();
		sememe = ObjectSelect
				.query(Sememe.class)
				.where(_Sememe.NAME.eq(nm))
				.selectOne(context);
		return sememe;
    }
    
	public static Set<SerialSememe> getAllSememes() {
		ObjectContext context;
		List<Sememe> dbSememes;
		Set<SerialSememe> sememes;
		SerialSememe sememe;
		
		context = AlixiaApplication.getEntityContext();
		dbSememes = ObjectSelect
				.query(Sememe.class)
				.select(context);
		sememes = new HashSet<>(dbSememes.size());
		for (Sememe dbSememe : dbSememes) {
			sememe = dbSememe.toSerial();
			sememes.add(sememe);
		}
    	return sememes;
    }
    
	public static Set<SerialSememe> getExternalSememes() {
		ObjectContext context;
		List<Sememe> dbSememes;
		Set<SerialSememe> sememes;
		SerialSememe sememe;
		
		context = AlixiaApplication.getEntityContext();
		dbSememes = ObjectSelect
				.query(Sememe.class)
				.where(_Sememe.EXTERNAL.eq(true))
				.select(context);
		sememes = new HashSet<>(dbSememes.size());
		for (Sememe dbSememe : dbSememes) {
			sememe = dbSememe.toSerial();
			sememes.add(sememe);
		}
    	return sememes;
    }
    
//    public boolean is(String nm) {
//    
//    	SharedUtils.checkNotNull(nm);
//    	return getName().equals(nm);
//    }
	
//    public boolean isExternal() {
//    
//    	return getExternal();
//    }
    
	public static Sememe fromSerial(SerialSememe serialSememe) {
		Sememe sememe;
		
		SharedUtils.checkNotNull(serialSememe);
		sememe = findRaw(serialSememe.getName());
		return sememe;
	}
	
	public SerialSememe toSerial() {
		SerialSememe serialSememe;
		
		serialSememe = new SerialSememe();
		serialSememe.setName(getName());
		serialSememe.setCanonicalForm(getCanonicalForm());
		serialSememe.setExternalUse(getExternal());
		serialSememe.setAdminOnly(adminOnly);
		serialSememe.setLoggedIn(loggedIn);
		return serialSememe;
	}
	
	public static void dumpSememes() {
		Set<SerialSememe> sememes;
		
		sememes = getAllSememes();
		for (SerialSememe sememe : sememes) {
			java.lang.System.out.println(sememe);
		}
	}

	public static SerialSememe getProxySememe() {
		Sememe sememe;
		
		AlixiaUtils.error("Getting proxy sememe");
		sememe = Sememe.findRaw("exclamation");
		if (sememe == null) {
			return null;
		}
		return sememe.toSerial();
	}
	
	/**
	 * Override the toString method in Object to print the Sememe name and canonical form.
	 * 
     * @return The formatted string
	 */
	@Override
	public String toString() {
		
		return getName() + ": " + getCanonicalForm() + ": " + (getExternal() ? "external" : "internal");
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Sememe)) {
			return false;
		}
		Sememe other = (Sememe) obj;
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
	 * This compareTo compares Sememes on their names.
	 * It is case-insensitive.
	 * 
     * @param otherSememe
     * @return A negative integer, zero, or a positive integer as the other sememe is greater than, equal to, or less than this sememe
	 */
	@Override
	public int compareTo(Sememe otherSememe) {
		
		SharedUtils.checkNotNull(otherSememe);
        return this.getName().compareToIgnoreCase(otherSememe.getName());
	}
	
    public void commit() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
    	context.commitChanges();
    }
    
    public void rollback() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
    	context.rollbackChanges();
    }

	public void delete() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
     	context.deleteObjects(this);
    	context.commitChanges();
	}

	public static Sememe createNew(String sememeName) {
    	ObjectContext context;
    	Sememe dbSememe;
    	
    	SharedUtils.checkNotNull(sememeName);
    	context = AlixiaApplication.getEntityContext();
        dbSememe = context.newObject(Sememe.class);
        dbSememe.setName(sememeName);
    	// NOT committed yet
    	return dbSememe;
	}

	public static boolean exists(String nm) {
		Sememe sememe;
		
		sememe = findRaw(nm);
		return sememe != null;
	}
}
