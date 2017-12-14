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

import java.util.List;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.google.common.collect.ImmutableList;
import com.hulles.a1icia.api.shared.SerialSpark;
import com.hulles.a1icia.cayenne.auto._Spark;
import com.hulles.a1icia.tools.A1iciaUtils;

public class Spark extends _Spark implements Comparable<Spark> {
    private static final long serialVersionUID = 1L; 
    
    public static Spark findSpark(Integer sparkID) {
		ObjectContext context;
		Spark item;
		
		A1iciaUtils.checkNotNull(sparkID);
		context = A1iciaApplication.getEntityContext();
		item = Cayenne.objectForPK(context, Spark.class, sparkID);
		return item;
    }
    
    public static Spark find(String nm) {
		ObjectContext context;
		Spark item;
		
		A1iciaUtils.checkNotNull(nm);
		context = A1iciaApplication.getEntityContext();
		item = ObjectSelect
				.query(Spark.class)
				.where(_Spark.NAME.eq(nm))
				.selectOne(context);
		if (item == null) {
			A1iciaUtils.error("Spark: cannot find spark named " + nm, "Returning null spark");
		}
		return item;
    }
    
	public static List<Spark> getAllSparks() {
		ObjectContext context;
		List<Spark> dbSparks;
		
		context = A1iciaApplication.getEntityContext();
		dbSparks = ObjectSelect
				.query(Spark.class)
				.select(context);
    	return ImmutableList.copyOf(dbSparks);
    }
    
	public static List<Spark> getExternalSparks() {
		ObjectContext context;
		List<Spark> dbSparks;
		
		context = A1iciaApplication.getEntityContext();
		dbSparks = ObjectSelect
				.query(Spark.class)
				.where(_Spark.EXTERNAL.eq(true))
				.select(context);
    	return ImmutableList.copyOf(dbSparks);
    }
    
    public boolean is(String nm) {
    
    	A1iciaUtils.checkNotNull(nm);
    	return getName().equals(nm);
    }
	
    public boolean isExternal() {
    
    	return getExternal();
    }
    
	public static Spark fromSerial(SerialSpark serialSpark) {
		Spark spark;
		
		A1iciaUtils.checkNotNull(serialSpark);
		spark = find(serialSpark.getName());
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
		List<Spark> sparks;
		
		sparks = getAllSparks();
		for (Spark spark : sparks) {
			java.lang.System.out.println(spark);
		}
	}

	public static Spark getProxySpark() {
		Spark spark;
		
		A1iciaUtils.warning("Getting proxy spark");
		spark = Spark.find("exclamation");
		return spark;
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
		
		spark = find(nm);
		return spark != null;
	}
}
