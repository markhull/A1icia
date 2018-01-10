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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.hulles.a1icia.api.shared.SerialSpark;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.Spark;
import com.hulles.a1icia.jebus.JebusBible;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.jebus.JebusPool;
import com.hulles.a1icia.tools.A1iciaUtils;

import redis.clients.jedis.Jedis;

/**
 * A SparkPackage is a bundle of spark-related fields: spark, optional spark object type, etc. For
 * example: Spark = "play_video", SparkObjectType = "VIDEOTITLE", SparkObject = "Un Chien Andalou".
 * So given a SparkPackage, we should know what action to perform ("play_video") and what the object of 
 * the action is ("Un Chien Andalou", a movie), but we haven't yet performed the action.
 * <p>
 * TODO The SparkObjectType turns out to be not very useful and a pain to keep track of, so we should
 * change this. Also the confidence rating has not been used much yet, so consider it tentative.
 * Pretty sure about the spark though.
 * 
 * @author hulles
 *
 */
public class SparkPackage {
	private SerialSpark spark;
	private String sparkObject;
	private SentencePackage sentencePackage;	
	private Integer confidence; // 0 - 100, 100 = certainty
	private final String idString;
	
	private SparkPackage() {
		long idValue;

		idValue = getNewSparkPackageID();
		this.idString = "SK" + idValue;
	}
	
	public String getSparkPackageID() {
		
		return idString;
	}

	public SerialSpark getSpark() {
		
		return spark;
	}

	public void setSpark(SerialSpark spark) {
		
		A1iciaUtils.checkNotNull(spark);
		this.spark = spark;
	}

	public String getSparkObject() {
		
		return sparkObject;
	}

	public void setSparkObject(String object) {
		
		A1iciaUtils.nullsOkay(object);
		this.sparkObject = object;
	}

	public SentencePackage getSentencePackage() {
		
		return sentencePackage;
	}

	public void setSentencePackage(SentencePackage pkg) {
		
		A1iciaUtils.nullsOkay(pkg);
		this.sentencePackage = pkg;
	}

	public Integer getConfidence() {
		
		return confidence;
	}

	public void setConfidence(Integer confidence) {
		
		A1iciaUtils.checkNotNull(confidence);
		this.confidence = confidence;
	}
	
	public boolean is(String name) {
	
		A1iciaUtils.checkNotNull(name);
		return name.equals(spark.getName());
	}
	
	public String getName() {
		
		return spark.getName();
	}
	
	/**
	 * Return the spark package with the named spark if it's in the list, null otherwise.
	 * 
	 * @param name The spark name
	 * @param sparks The list of spark packages that possibly contains the named spark
	 * @return The package with the named spark, or null if not a member of the list
	 */
	public static SparkPackage has(String name, List<SparkPackage> sparkPackages) {
		
		A1iciaUtils.checkNotNull(name);
		A1iciaUtils.checkNotNull(sparkPackages);
		for (SparkPackage pkg : sparkPackages) {
			if (pkg.is(name)) {
				return pkg;
			}
		}
		return null;
	}
	
	/**
	 * Remove and return the package with the named spark if it's in the list, otherwise
	 * return null.
	 * 
	 * @param name The spark name
	 * @param sparks The list of sparks that possibly contains the named spark
	 * @return The named spark, or null if not a member of the list
	 */
	public static SparkPackage consume(String name, List<SparkPackage> sparkPackages) {
		SparkPackage pkg;
		
		A1iciaUtils.checkNotNull(name);
		A1iciaUtils.checkNotNull(sparkPackages);
		for (Iterator<SparkPackage> iter = sparkPackages.iterator(); iter.hasNext(); ) {
			pkg = iter.next();
			if (pkg.is(name)) {
				iter.remove();
				return pkg;
			}
		}
		return null;
	}
	
	/**
	 * Remove and return the named spark if it's in the list, otherwise
	 * return null. That spark should be the last one in the list; i.e. the
	 * set should be empty after its removal.
	 * 
	 * @param name The spark name
	 * @param sparks The list of sparks that possibly contains the named spark
	 * @return The named spark, or null if not a member of the list
	 */
	public static SparkPackage consumeFinal(String name, List<SparkPackage> sparkPackages) {
		SparkPackage pkg;
		SparkPackage foundPkg = null;
		
		A1iciaUtils.checkNotNull(name);
		A1iciaUtils.checkNotNull(sparkPackages);
		for (Iterator<SparkPackage> iter = sparkPackages.iterator(); iter.hasNext(); ) {
			pkg = iter.next();
			if (pkg.is(name)) {
				foundPkg = pkg;
				iter.remove();
				break;
			}
		}
		if (!sparkPackages.isEmpty()) {
			throw new A1iciaException("Spark package list not empty after consumeFinal");
		}
		return foundPkg;
	}
	
	private static long getNewSparkPackageID() {
		JebusPool jebusPool;
		
		jebusPool = JebusHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			return jebus.incr(JebusBible.getA1iciaSparkCounterKey(jebusPool));
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
		if (!(obj instanceof SparkPackage)) {
			return false;
		}
		SparkPackage other = (SparkPackage) obj;
		if (idString == null) {
			if (other.idString != null) {
				return false;
			}
		} else if (!idString.equals(other.idString)) {
			return false;
		}
		return true;
	}
	
	public static SparkPackage getNewPackage() {

		return new SparkPackage();
	}
	
	public static SparkPackage getDefaultPackage(SerialSpark spark) {
		SparkPackage pkg;
		
		A1iciaUtils.checkNotNull(spark);
		pkg = new SparkPackage();
		pkg.setSpark(spark);
		pkg.setConfidence(0);
		return pkg;
	}
	public static SparkPackage getDefaultPackage(String name) {
		SparkPackage pkg;
		SerialSpark spark;
		
		A1iciaUtils.checkNotNull(name);
		spark = SerialSpark.find(name);
		pkg = new SparkPackage();
		pkg.setSpark(spark);
		pkg.setConfidence(0);
		return pkg;
	}
	
	public static List<SparkPackage> getSingletonDefault(SerialSpark spark) {
		SparkPackage pkg;
		
		A1iciaUtils.checkNotNull(spark);
		pkg = getDefaultPackage(spark);
		return Collections.singletonList(pkg);
	}
	public static List<SparkPackage> getSingletonDefault(String name) {
		SparkPackage pkg;
		SerialSpark spark;
		
		A1iciaUtils.checkNotNull(name);
		spark = SerialSpark.find(name);
		pkg = getDefaultPackage(spark);
		return Collections.singletonList(pkg);
	}
	
	public static SparkPackage getProxyPackage() {
		SerialSpark spark;
		
		spark = Spark.getProxySpark();
		return getDefaultPackage(spark);
	}
	
	public boolean isValid() {
		
		if (spark == null) {
			return false;
		}
		
		// sparkObject can be null
		
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
		
		return "Spark Package for " + getName();
	}
}
