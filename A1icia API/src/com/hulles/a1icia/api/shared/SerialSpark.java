package com.hulles.a1icia.api.shared;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * Spark is an important class in A1icia. It represents an object that causes an action of some
 * kind to take place: a question that prompts an answer, a command that prompts a response, and
 * so forth.
 *  <p>
 *  SerialSpark is a pared-down version of the main (non-API) A1icia Spark. It extends 
 *  SerialEntity, and cannot update sparks. Note that sparks could be updated anyway, via Redis;
 *  they just can't be modified from this class.
 *  
 * @author hulles
 *
 */
public class SerialSpark implements Serializable, Comparable<SerialSpark> {
	private static final long serialVersionUID = 980860858637714677L;
	private String sparkName;
	private String canonicalForm;
	private Boolean externalUse;
	private Boolean adminOnly;
	private Boolean loggedIn;
	
    public SerialSpark() {
    	// need no-arg constructor
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
    	
		return sparkName;
	}

	public void setName(String sparkName) {
		
		SharedUtils.checkNotNull(sparkName);
		this.sparkName = sparkName;
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
	 * Remove and return SerialSpark if it's in the list, otherwise
	 * return null.
	 * 
	 * @param name The spark name
	 * @param sparks The set of sparks that possibly contains the named spark
	 * @return The named spark, or null if not a member of the set
	 */
	public static SerialSpark consume(String name, Set<SerialSpark> sparks) {
		SerialSpark spark;
		
		SharedUtils.checkNotNull(name);
		SharedUtils.checkNotNull(sparks);
		for (Iterator<SerialSpark> iter = sparks.iterator(); iter.hasNext(); ) {
			spark = iter.next();
			if (spark.is(name)) {
				iter.remove();
				return spark;
			}
		}
		return null;
	}

	/**
	 * This compareTo compares Sparks on their names.
	 * It is case-insensitive.
	 * 
	 */
	@Override
	public int compareTo(SerialSpark otherSpark) {
		
		SharedUtils.checkNotNull(otherSpark);
        return this.getName().compareToIgnoreCase(otherSpark.getName());
	}

}
