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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.jebus.JebusBible;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.jebus.JebusPool;
import com.hulles.a1icia.room.document.RoomActionObject;
import com.hulles.a1icia.tools.A1iciaUtils;

import redis.clients.jedis.Jedis;
/**
 * An ActionPackage is a bundle consisting of a sememe (in a SememePackage) and a corresponding
 * RoomActionObject that is the result of the sememe.
 * 
 * @author hulles
 *
 */
public final class ActionPackage {
	private final SememePackage sememePackage;
	private RoomActionObject actionObject;
	private final String idString;
	
	public ActionPackage(SememePackage pkg) {
		long idValue;
		
		SharedUtils.checkNotNull(pkg);
		this.sememePackage = pkg;
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
		
		SharedUtils.checkNotNull(actionObject);
		this.actionObject = actionObject;
	}

	public SerialSememe getSememe() {
		
		return sememePackage.getSememe();
	}
	
	public SememePackage getSememePackage() {
		
		return sememePackage;
	}
	
	public String getName() {
	
		return sememePackage.getName();
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
	 * @param sememe The action package sememe
	 * @param pkgs The list of action packages that possibly contains the named package
	 * @return The named package, or null if not a member of the list
	 */
	public static ActionPackage has(SerialSememe sememe, List<ActionPackage> pkgs) {
		SerialSememe pkgSememe;
        
		SharedUtils.checkNotNull(sememe);
		SharedUtils.checkNotNull(pkgs);
		for (ActionPackage pkg : pkgs) {
            pkgSememe = pkg.getSememe();
			if (pkgSememe.equals(sememe)) {
				return pkg;
			}
		}
		return null;
	}
	
	/**
	 * Return a sublist of ActionPackages from the provided list of ActionPackages
	 *     such that the sememe of the returned packages equals the provided sememe.
	 *     
	 * @param sememe The target sememe
	 * @param pkgs The list of packages that may contain the sememe
	 * @return The sublist of packages that contain the sememe; empty list if none
	 */
	public static List<ActionPackage> hasActions(SerialSememe sememe, List<ActionPackage> pkgs) {
		List<ActionPackage> subList;
		
		SharedUtils.checkNotNull(sememe);
		SharedUtils.checkNotNull(pkgs);
		subList = new ArrayList<>();
		for (ActionPackage pkg : pkgs) {
			if (pkg.getSememe().equals(sememe)) {
				subList.add(pkg);
			}
		}
		return subList;
	}
	
	/**
	 * Remove and return the action package with this sememe if it's in the list, otherwise
	 * return null.
	 * 
	 * @param sememe The action package sememe
	 * @param pkgs The list of packages that possibly contains the named package
	 * @return The named package, or null if not a member of the list
	 */
	public static ActionPackage consume(SerialSememe sememe, List<ActionPackage> pkgs) {
		ActionPackage pkg;
		
		SharedUtils.checkNotNull(sememe);
		SharedUtils.checkNotNull(pkgs);
		for (Iterator<ActionPackage> iter = pkgs.iterator(); iter.hasNext(); ) {
			pkg = iter.next();
			if (pkg.getSememe().equals(sememe)) {
				iter.remove();
				return pkg;
			}
		}
		return null;
	}
	
	/**
	 * Remove and return the action packages with this sememe from the packages
	 * in the provided list, otherwise return null.
	 * 
	 * @param sememe The target sememe
	 * @param pkgs The list of packages that possibly contains the sememe
	 * @return The sublist of packages, or an empty list if none
	 */
	public static List<ActionPackage> consumeActions(SerialSememe sememe, List<ActionPackage> pkgs) {
		List<ActionPackage> subList;
		ActionPackage pkg;
		
		SharedUtils.checkNotNull(sememe);
		SharedUtils.checkNotNull(pkgs);
		subList = new ArrayList<>();
		for (Iterator<ActionPackage> iter = pkgs.iterator(); iter.hasNext(); ) {
			pkg = iter.next();
			if (pkg.getSememe().equals(sememe)) {
				subList.add(pkg);
				iter.remove();
			}
		}
		return subList;
	}

	/**
	 * Remove and return the action package with this sememe if it's in the list, otherwise
	 * return null. That package should be the last one in the list; i.e. the
	 * set should be empty after its removal.
	 * 
	 * @param sememe The sememe
	 * @param pkgs The list of action packages that possibly contains the target package
	 * @return The action package, or null if not a member of the list
	 */
	public static ActionPackage consumeFinal(SerialSememe sememe, List<ActionPackage> pkgs) {
		ActionPackage pkg;
		ActionPackage foundPkg = null;
		String errMsg;
		
		SharedUtils.checkNotNull(sememe);
		SharedUtils.checkNotNull(pkgs);
		for (Iterator<ActionPackage> iter = pkgs.iterator(); iter.hasNext(); ) {
			pkg = iter.next();
			if (pkg.getSememe().equals(sememe)) {
				foundPkg = pkg;
				iter.remove();
				break;
			}
		}
		if (foundPkg != null && !pkgs.isEmpty()) {
			errMsg = "ActionPackage set not empty after consumeFinal:\n";
			errMsg += "Found package sememe is " + foundPkg.getSememe().getName();
			errMsg += "\n";
			for (ActionPackage p : pkgs) {
				errMsg += "Remaining package for sememe is ";
				errMsg += p.getSememe().getName();
				errMsg += "\n";
			}
			throw new A1iciaException(errMsg);
		}
		return foundPkg;
	}

	// TODO change this to isValid to be consistent, but make sure that's true first
	public boolean isReady() {
		
		if (sememePackage == null) {
			A1iciaUtils.error("ActionPackage: sememe package is null in packageIsReady");
			return false;
		}
		if (getActionObject() == null) {
			A1iciaUtils.error("ActionPackage: missing 'action object' in action package for sememe " +
					sememePackage.getSememe().getName());
			return false;
		}
		return true;
	}
	
	// TODO get rid of proxy crap, I think
	
	public static ActionPackage getProxyActionPackage() {
		ActionPackage pkg;
		SememePackage sememe;
		
		sememe = SememePackage.getProxyPackage();
		pkg = new ActionPackage(sememe);
		pkg.setActionObject(new ProxyAction());
		return pkg;
	}
	
	public static ActionPackage getProxyActionPackage(SememePackage sememePackage) {
		ActionPackage pkg;
		
		pkg = new ActionPackage(sememePackage);
		pkg.setActionObject(new ProxyAction());
		return pkg;
	}
	
	public static ActionPackage getProxyActionPackage(SerialSememe sememe) {
		ActionPackage pkg;
		SememePackage sememePkg;
		
		sememePkg = SememePackage.getDefaultPackage(sememe);
		pkg = new ActionPackage(sememePkg);
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
