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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hulles.a1icia.api.shared.SerialSpark;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.jebus.JebusBible;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.jebus.JebusPool;
import com.hulles.a1icia.room.document.RoomActionObject;
import com.hulles.a1icia.tools.A1iciaUtils;

import redis.clients.jedis.Jedis;
/**
 * An ActionPackage is a bundle consisting of a spark (in a SparkPackage) and a corresponding
 * RoomActionObject that is the result of the spark.
 * 
 * @author hulles
 *
 */
public final class ActionPackage {
	private final SparkPackage sparkPackage;
	private RoomActionObject actionObject;
	private final String idString;
	
	public ActionPackage(SparkPackage pkg) {
		long idValue;
		
		A1iciaUtils.checkNotNull(pkg);
		this.sparkPackage = pkg;
		idValue = getNewActionPackageID();
		this.idString = "AP" + idValue;
	}

	public String getActionPackageID() {
		
		return idString;
	}
	
	public RoomActionObject getActionObject() {
		
		return actionObject;
	}

	public void setActionObject(RoomActionObject actionObject) {
		
		A1iciaUtils.checkNotNull(actionObject);
		this.actionObject = actionObject;
	}

	public SerialSpark getSpark() {
		
		return sparkPackage.getSpark();
	}
	
	public SparkPackage getSparkPackage() {
		
		return sparkPackage;
	}
	
	public String getName() {
	
		return sparkPackage.getName();
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
		if (!(obj instanceof ActionPackage)) {
			return false;
		}
		ActionPackage other = (ActionPackage) obj;
		if (idString == null) {
			if (other.idString != null) {
				return false;
			}
		} else if (!idString.equals(other.idString)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Return the action package if it's in the list, null otherwise.
	 * 
	 * @param spark The action package spark
	 * @param pkgs The list of action packages that possibly contains the named package
	 * @return The named package, or null if not a member of the list
	 */
	public static ActionPackage has(SerialSpark spark, List<ActionPackage> pkgs) {
		SerialSpark pkgSpark;
        
		A1iciaUtils.checkNotNull(spark);
		A1iciaUtils.checkNotNull(pkgs);
		for (ActionPackage pkg : pkgs) {
            pkgSpark = pkg.getSpark();
			if (pkgSpark.equals(spark)) {
				return pkg;
			}
		}
		return null;
	}
	
	/**
	 * Return a sublist of ActionPackages from the provided list of ActionPackages
	 *     such that the spark of the returned packages equals the provided spark.
	 *     
	 * @param spark The target spark
	 * @param pkgs The list of packages that may contain the spark
	 * @return The sublist of packages that contain the spark; empty list if none
	 */
	public static List<ActionPackage> hasActions(SerialSpark spark, List<ActionPackage> pkgs) {
		List<ActionPackage> subList;
		
		A1iciaUtils.checkNotNull(spark);
		A1iciaUtils.checkNotNull(pkgs);
		subList = new ArrayList<>();
		for (ActionPackage pkg : pkgs) {
			if (pkg.getSpark().equals(spark)) {
				subList.add(pkg);
			}
		}
		return subList;
	}
	
	/**
	 * Remove and return the action package with this spark if it's in the list, otherwise
	 * return null.
	 * 
	 * @param spark The action package spark
	 * @param pkgs The list of packages that possibly contains the named package
	 * @return The named package, or null if not a member of the list
	 */
	public static ActionPackage consume(SerialSpark spark, List<ActionPackage> pkgs) {
		ActionPackage pkg;
		
		A1iciaUtils.checkNotNull(spark);
		A1iciaUtils.checkNotNull(pkgs);
		for (Iterator<ActionPackage> iter = pkgs.iterator(); iter.hasNext(); ) {
			pkg = iter.next();
			if (pkg.getSpark().equals(spark)) {
				iter.remove();
				return pkg;
			}
		}
		return null;
	}
	
	/**
	 * Remove and return the action packages with this spark from the packages
	 * in the provided list, otherwise return null.
	 * 
	 * @param spark The target spark
	 * @param pkgs The list of packages that possibly contains the spark
	 * @return The sublist of packages, or an empty list if none
	 */
	public static List<ActionPackage> consumeActions(SerialSpark spark, List<ActionPackage> pkgs) {
		List<ActionPackage> subList;
		ActionPackage pkg;
		
		A1iciaUtils.checkNotNull(spark);
		A1iciaUtils.checkNotNull(pkgs);
		subList = new ArrayList<>();
		for (Iterator<ActionPackage> iter = pkgs.iterator(); iter.hasNext(); ) {
			pkg = iter.next();
			if (pkg.getSpark().equals(spark)) {
				subList.add(pkg);
				iter.remove();
			}
		}
		return subList;
	}

	/**
	 * Remove and return the action package with this spark if it's in the list, otherwise
	 * return null. That package should be the last one in the list; i.e. the
	 * set should be empty after its removal.
	 * 
	 * @param spark The spark
	 * @param pkgs The list of action packages that possibly contains the target package
	 * @return The action package, or null if not a member of the list
	 */
	public static ActionPackage consumeFinal(SerialSpark spark, List<ActionPackage> pkgs) {
		ActionPackage pkg;
		ActionPackage foundPkg = null;
		String errMsg;
		
		A1iciaUtils.checkNotNull(spark);
		A1iciaUtils.checkNotNull(pkgs);
		for (Iterator<ActionPackage> iter = pkgs.iterator(); iter.hasNext(); ) {
			pkg = iter.next();
			if (pkg.getSpark().equals(spark)) {
				foundPkg = pkg;
				iter.remove();
				break;
			}
		}
		if (foundPkg != null && !pkgs.isEmpty()) {
			errMsg = "ActionPackage set not empty after consumeFinal:\n";
			errMsg += "Found package spark is " + foundPkg.getSpark().getName();
			errMsg += "\n";
			for (ActionPackage p : pkgs) {
				errMsg += "Remaining package for spark is ";
				errMsg += p.getSpark().getName();
				errMsg += "\n";
			}
			throw new A1iciaException(errMsg);
		}
		return foundPkg;
	}

	// TODO change this to isValid to be consistent, but make sure that's true first
	public boolean isReady() {
		
		if (sparkPackage == null) {
			A1iciaUtils.error("ActionPackage: spark package is null in packageIsReady");
			return false;
		}
		if (getActionObject() == null) {
			A1iciaUtils.error("ActionPackage: missing 'action object' in action package for spark " +
					sparkPackage.getSpark().getName());
			return false;
		}
		return true;
	}
	
	// TODO get rid of proxy crap, I think
	
	public static ActionPackage getProxyActionPackage() {
		ActionPackage pkg;
		SparkPackage spark;
		
		spark = SparkPackage.getProxyPackage();
		pkg = new ActionPackage(spark);
		pkg.setActionObject(new ProxyAction());
		return pkg;
	}
	
	public static ActionPackage getProxyActionPackage(SparkPackage sparkPackage) {
		ActionPackage pkg;
		
		pkg = new ActionPackage(sparkPackage);
		pkg.setActionObject(new ProxyAction());
		return pkg;
	}
	
	public static ActionPackage getProxyActionPackage(SerialSpark spark) {
		ActionPackage pkg;
		SparkPackage sparkPkg;
		
		sparkPkg = SparkPackage.getDefaultPackage(spark);
		pkg = new ActionPackage(sparkPkg);
		pkg.setActionObject(new ProxyAction());
		return pkg;
	}
	
	private static long getNewActionPackageID() {
		JebusPool jebusPool;
		
		jebusPool = JebusHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			return jebus.incr(JebusBible.getA1iciaActionCounterKey(jebusPool));
		}		
	}

	@Override
	public String toString() {
		
		return "Action Package for " + getName();
	}
	
}
