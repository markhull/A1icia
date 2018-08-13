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
package com.hulles.a1icia.api.remote;

import java.io.Serializable;

import com.hulles.a1icia.api.jebus.JebusApiBible;
import com.hulles.a1icia.api.jebus.JebusApiHub;
import com.hulles.a1icia.api.jebus.JebusPool;
import com.hulles.a1icia.api.shared.SharedUtils;

import redis.clients.jedis.Jedis;

/**
 * An ID for an A1ician, a client entity that interacts with A1icia Central.
 * This ID is not permanently assigned to the A1ician; it exists for the session (while
 * the A1ician is actively interacting) only.
 * 
 * @author hulles
 *
 */
public class A1icianID implements Serializable {
	private static final long serialVersionUID = -7396766025766617796L;
	private final String a1icianID;
	
	public A1icianID(String id) {
		
		SharedUtils.checkNotNull(id);
		this.a1icianID = id;
	}

	@Override
	public String toString() {
		
		return a1icianID;
	}

	public boolean isValid() {
		
		try {
			Long.parseLong(a1icianID);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a1icianID == null) ? 0 : a1icianID.hashCode());
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
		if (!(obj instanceof A1icianID)) {
			return false;
		}
		A1icianID other = (A1icianID) obj;
		if (a1icianID == null) {
			if (other.a1icianID != null) {
				return false;
			}
		} else if (!a1icianID.equals(other.a1icianID)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Create a new A1ician ID with a Jebus counter.
	 * 
	 * @return The ID
	 */
	@SuppressWarnings("resource")
	public static A1icianID createA1icianID() {
		JebusPool jebusPool;
		String a1icianID;
		String counterKey;
		
		jebusPool = JebusApiHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			counterKey = JebusApiBible.getA1icianCounterKey(jebusPool);
			a1icianID = jebus.incr(counterKey).toString();
		}
		return new A1icianID(a1icianID);
	}
	
}
