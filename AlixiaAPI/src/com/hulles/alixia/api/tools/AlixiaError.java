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
package com.hulles.alixia.api.tools;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusBible.JebusKey;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;
import com.hulles.alixia.api.shared.SharedUtils;

import redis.clients.jedis.Jedis;

/**
 * The AlixiaError class is a way for us to record occurrences of operational errors and retrieve
 * them later.
 * <p>
 * For each instance of AlixiaError we increment an error counter for a new error ID, store the
 * message ID and time in a Redis sorted set, and create a hash using the ID for a key that contains
 * the error message and timestamp.
 * 
 * @author hulles
 *
 */
public final class AlixiaError {
	private final JebusPool jebusPool;
//	private final Long idNo;
	private final String hashKey;
	private final Long ERROR_TTL = 1000L * 60L * 24L * 3L; // 3 days in millis
	
	/**
	 * Create a new instance of AlixiaError from scratch, with a new ID etc. etc.
	 */
	public AlixiaError() {
		Instant timestamp;
		String timestampStr;
		Double timeScore;
		Set<String> ids;
		String timelineKey;
		String deadHashKey;
		String counterKey;
        Long idNo;
        
		jebusPool = JebusHub.getJebusLocal();
		timelineKey = JebusBible.getStringKey(JebusKey.ALIXIAERRORTIMELINEKEY, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
            counterKey = JebusBible.getStringKey(JebusKey.ALIXIAERRORCOUNTERKEY, jebusPool);
			idNo = jebus.incr(counterKey);
			this.hashKey = JebusBible.getErrorHashKey(jebusPool, idNo);
			timestamp = Instant.now();
			timestampStr = timestamp.toString();
			// we use millis for sorting purposes
			timeScore = Double.valueOf(System.currentTimeMillis());
			jebus.hset(hashKey,
					JebusBible.getStringKey(JebusKey.TIMESTAMPFIELD, jebusPool), 
					timestampStr);
			jebus.zadd(timelineKey,
					timeScore, 
					idNo.toString());
			// cleanup
			timeScore -= ERROR_TTL;
			ids = jebus.zrangeByScore(timelineKey, 0, timeScore);
			jebus.zremrangeByScore(timelineKey, 0, timeScore);
			for (String id : ids) {
				deadHashKey = JebusBible.getErrorHashKey(jebusPool, id);
				jebus.del(deadHashKey);
			}
			// notify anyone who might be listening
			jebus.publish(JebusBible.getStringKey(JebusKey.ALIXIAERRORCHANNELKEY, jebusPool), 
					"Recieved error " + idNo.toString() + " at " + timestamp.toString());
		}
	}
	/**
	 * Create a new instance of AlixiaError from an existing Jebus error record.
	 * 
	 * @param idNo The ID number of the error.
	 */
//	public AlixiaError(Long idNo) {
//		
//		SharedUtils.checkNotNull(idNo);
//		jebusPool = JebusHub.getJebusLocal();
//		this.hashKey = JebusBible.getErrorHashKey(jebusPool, idNo);
//	}
	/**
	 * Create a new instance of AlixiaError from an existing Jebus error record.
	 * 
	 * @param idStr The ID string of the error.
	 */
	public AlixiaError(String idStr) {
		
		SharedUtils.checkNotNull(idStr);
		jebusPool = JebusHub.getJebusLocal();
		this.hashKey = JebusBible.getErrorHashKey(jebusPool, idStr);
	}
	
	/**
	 * Get the error Jebus key.
	 * 
	 * @return The key.
	 */
	public String getKey() {
		
		return hashKey;
	}
	
	/**
	 * Get the error message.
	 * 
	 * @return The message
	 */
	public String getMessage() {
		String messageStr;
		
		try (Jedis jebus = jebusPool.getResource()) {
			messageStr = jebus.hget(hashKey,
					JebusBible.getStringKey(JebusKey.MESSAGEFIELD, jebusPool));
			return messageStr;
		}		
	}
	
	/**
	 * Set the error message.
	 * 
	 * @param value The message
	 */
	public void setMessage(String value) {
		
		SharedUtils.nullsOkay(value);
		if (value == null) {
			return;
		}
		try (Jedis jebus = jebusPool.getResource()) {
			jebus.hset(hashKey,
					JebusBible.getStringKey(JebusKey.MESSAGEFIELD, jebusPool),
					value);
		}		
	}
	
	/**
	 * Get the time the error occurred as a fancy new Java™ java.time.Instant.
	 * 
	 * @return The instant.
	 */
	public Instant getTimestamp() {
		String instantStr;
		Instant instant;
		
		try (Jedis jebus = jebusPool.getResource()) {
			instantStr = jebus.hget(hashKey, 
					JebusBible.getStringKey(JebusKey.TIMESTAMPFIELD, jebusPool)); 
			instant = Instant.parse(instantStr);
			return instant;
		}		
	}
    
    public static List<AlixiaError> getRecentErrors() {
        JebusPool jebusPool;
        Set<String> keys;
        String timelineKey;
        AlixiaError alixiaError;
        List<AlixiaError> alixiaErrors;
        
		jebusPool = JebusHub.getJebusLocal();
        alixiaErrors = new ArrayList<>();
		timelineKey = JebusBible.getStringKey(JebusKey.ALIXIAERRORTIMELINEKEY, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
            keys = jebus.zrange(timelineKey, 0, -1);
            for (String key : keys) {
                alixiaError = new AlixiaError(key);
                alixiaErrors.add(alixiaError);
            }
            return alixiaErrors;
		}
    }
}
