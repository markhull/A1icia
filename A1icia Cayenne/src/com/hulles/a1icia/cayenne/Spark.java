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
package com.hulles.a1icia.cayenne;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.hulles.a1icia.api.shared.SerialSpark;
import com.hulles.a1icia.cayenne.auto._Spark;
import com.hulles.a1icia.tools.A1iciaUtils;

public class Spark extends _Spark implements Comparable<Spark> {
    private static final long serialVersionUID = 1L; 
    
//    public static SerialSpark findSpark(Integer sparkID) {
//		ObjectContext context;
//		Spark spark;
//		
//		A1iciaUtils.checkNotNull(sparkID);
//		context = A1iciaApplication.getEntityContext();
//		spark = Cayenne.objectForPK(context, Spark.class, sparkID);
//		if (spark == null) {
//			return null;
//		}
//		return spark.toSerial();
//    }
    
//    public static SerialSpark find(String nm) {
//		Spark spark;
//
//		spark = findRaw(nm);
//		if (spark == null) {
//			A1iciaUtils.error("Spark: cannot find spark named " + nm, "Returning null spark");
//			return null;
//		}
//		return spark.toSerial();
//    }
    
    private static Spark findRaw(String nm) {
		ObjectContext context;
		Spark spark;
		
		A1iciaUtils.checkNotNull(nm);
		context = A1iciaApplication.getEntityContext();
		spark = ObjectSelect
				.query(Spark.class)
				.where(_Spark.NAME.eq(nm))
				.selectOne(context);
		return spark;
    }
    
	public static Set<SerialSpark> getAllSparks() {
		ObjectContext context;
		List<Spark> dbSparks;
		Set<SerialSpark> sparks;
		SerialSpark spark;
		
		context = A1iciaApplication.getEntityContext();
		dbSparks = ObjectSelect
				.query(Spark.class)
				.select(context);
		sparks = new HashSet<>(dbSparks.size());
		for (Spark dbSpark : dbSparks) {
			spark = dbSpark.toSerial();
			sparks.add(spark);
		}
    	return sparks;
    }
    
	public static Set<SerialSpark> getExternalSparks() {
		ObjectContext context;
		List<Spark> dbSparks;
		Set<SerialSpark> sparks;
		SerialSpark spark;
		
		context = A1iciaApplication.getEntityContext();
		dbSparks = ObjectSelect
				.query(Spark.class)
				.where(_Spark.EXTERNAL.eq(true))
				.select(context);
		sparks = new HashSet<>(dbSparks.size());
		for (Spark dbSpark : dbSparks) {
			spark = dbSpark.toSerial();
			sparks.add(spark);
		}
    	return sparks;
    }
    
//    public boolean is(String nm) {
//    
//    	A1iciaUtils.checkNotNull(nm);
//    	return getName().equals(nm);
//    }
	
//    public boolean isExternal() {
//    
//    	return getExternal();
//    }
    
	public static Spark fromSerial(SerialSpark serialSpark) {
		Spark spark;
		
		A1iciaUtils.checkNotNull(serialSpark);
		spark = findRaw(serialSpark.getName());
		return spark;
	}
	
	public SerialSpark toSerial() {
		SerialSpark serialSpark;
		
		serialSpark = new SerialSpark();
		serialSpark.setName(getName());
		serialSpark.setCanonicalForm(getCanonicalForm());
		serialSpark.setExternalUse(getExternal());
		serialSpark.setAdminOnly(adminOnly);
		serialSpark.setLoggedIn(loggedIn);
		return serialSpark;
	}
	
	public static void dumpSparks() {
		Set<SerialSpark> sparks;
		
		sparks = getAllSparks();
		for (SerialSpark spark : sparks) {
			java.lang.System.out.println(spark);
		}
	}

	public static SerialSpark getProxySpark() {
		Spark spark;
		
		A1iciaUtils.warning("Getting proxy spark");
		spark = Spark.findRaw("exclamation");
		if (spark == null) {
			return null;
		}
		return spark.toSerial();
	}
	
	/**
	 * Override the toString method in Object to print the Spark name and canonical form.
	 * 
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
		if (!(obj instanceof Spark)) {
			return false;
		}
		Spark other = (Spark) obj;
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
	 * This compareTo compares Sparks on their names.
	 * It is case-insensitive.
	 * 
	 */
	@Override
	public int compareTo(Spark otherSpark) {
		
		A1iciaUtils.checkNotNull(otherSpark);
        return this.getName().compareToIgnoreCase(otherSpark.getName());
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

	public static Spark createNew(String sparkName) {
    	ObjectContext context;
    	Spark dbSpark;
    	
    	A1iciaUtils.checkNotNull(sparkName);
    	context = A1iciaApplication.getEntityContext();
        dbSpark = context.newObject(Spark.class);
        dbSpark.setName(sparkName);
    	// NOT committed yet
    	return dbSpark;
	}

	public static boolean exists(String nm) {
		Spark spark;
		
		spark = findRaw(nm);
		return spark != null;
	}
}
