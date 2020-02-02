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
package com.hulles.alixia.api.remote;

import java.io.Serializable;

import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;
import com.hulles.alixia.api.shared.SharedUtils;

import redis.clients.jedis.Jedis;

/**
 * An ID for an Alixian, a client entity that interacts with Alixia Central.
 * This ID is not permanently assigned to the Alixian; it exists for the session (while
 * the Alixian is actively interacting) only.
 * 
 * @author hulles
 *
 */
public class AlixianID implements Serializable {
	private static final long serialVersionUID = -7396766025766617796L;
	private final String alixianID;
	
	public AlixianID(String id) {
		
		SharedUtils.checkNotNull(id);
		this.alixianID = id;
	}

	@Override
	public String toString() {
		
		return alixianID;
	}

	public boolean isValid() {
		
		try {
			Long.parseLong(alixianID);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alixianID == null) ? 0 : alixianID.hashCode());
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
		if (!(obj instanceof AlixianID)) {
			return false;
		}
		AlixianID other = (AlixianID) obj;
		if (alixianID == null) {
			if (other.alixianID != null) {
				return false;
			}
		} else if (!alixianID.equals(other.alixianID)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Create a new Alixian ID with a Jebus counter.
	 * 
	 * @return The ID
	 */
	public static AlixianID createAlixianID() {
		JebusPool jebusPool;
		String alixianID;
		String counterKey;
		
		jebusPool = JebusHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			counterKey = JebusBible.getStringKey(JebusBible.JebusKey.ALIXIANCOUNTERKEY, jebusPool);
			alixianID = jebus.incr(counterKey).toString();
	        return new AlixianID(alixianID);
		}
	}
	
}
