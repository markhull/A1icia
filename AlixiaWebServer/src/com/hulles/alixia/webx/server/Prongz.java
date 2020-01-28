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
package com.hulles.alixia.webx.server;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusBible.JebusKey;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.webx.shared.ProngException;
import com.hulles.alixia.webx.shared.SerialProng;

import redis.clients.jedis.Jedis;

/**
 * 
 * This is a starter implementation of prong access control. Obviously, anyone with access
 * to the Redis host can obtain access to the prong set as well. But for now we use this one,
 * knowing that we will improve it later.
 * 
 * This is NOT GWT-safe; it uses the java UUID object.
 * 
 * @author hulles
 *
 */
public final class Prongz {
	final static Logger LOGGER = Logger.getLogger("AlixiaProng.Prongz");
	static final Level LOGLEVEL = Level.FINE;
	private static final long PRONG_MILLIS = 15l * 1000l * 60l; // 15 minutes
    private static Prongz prongz;
    private final JebusPool jebusPool;
    private final String alixiaProngKey;
    
    private Prongz() {
    	
    	jebusPool = JebusHub.getJebusLocal();
		alixiaProngKey = JebusBible.getStringKey(JebusKey.ALIXIAPRONGKEY, jebusPool);
    }

    public synchronized static Prongz getInstance() {
    	
        if (prongz == null) {
            prongz = new Prongz();
        }
        return prongz;
    }
    
    /**
     * Create a new prong value and put it into the Redis prong sorted set, deleting the old one 
     * if present. Also clean out any expired prongs.
     * 
     * @return newProng
     */
    public SerialProng getNewProng() {
    	String prongString;
     	Double expireScore;
     	Long expireMillis;
     	Double nowScore;
     	Long deathToll;
     	
		prongString = UUID.randomUUID().toString();
		// we use millis for sorting purposes
		expireMillis = System.currentTimeMillis() + PRONG_MILLIS;
		expireScore = Double.valueOf(expireMillis);
		nowScore = Double.valueOf(System.currentTimeMillis());
		try (Jedis jebus = jebusPool.getResource()) {
			jebus.zadd(alixiaProngKey, expireScore, prongString);
			LOGGER.log(LOGLEVEL, "New session at {0}", Instant.now().toString());
			deathToll = jebus.zremrangeByScore(alixiaProngKey, Double.MIN_VALUE, nowScore);
			if (deathToll > 0) {
				LOGGER.log(LOGLEVEL, "Expired {0} prongs at {1}", 
                        new Object[]{deathToll, Instant.now().toString()});
			}
		}
		LOGGER.log(LOGLEVEL, "GETNEWPRONG: NEW PRONG = {0}", prongString);
		return new SerialProng(prongString);
    }

    /**
     * Remove the old prong value from the Redis sorted set, if present. Also clean out
     * any expired prongs.
     * 
     * @param oldProng
     */
    public void removeProng(SerialProng oldProng) {
     	Double nowScore;
     	String oldString;
     	Long deathToll;
     	
    	SharedUtils.checkNotNull(oldProng);
    	oldString = oldProng.getProngString();
		nowScore = Double.valueOf(System.currentTimeMillis());
		try (Jedis jebus = jebusPool.getResource()) {
			jebus.zrem(alixiaProngKey, oldString);
			LOGGER.log(LOGLEVEL, "Closed session at {0}", Instant.now().toString());
			deathToll = jebus.zremrangeByScore(alixiaProngKey, Double.MIN_VALUE, nowScore);
			if (deathToll > 0) {
				LOGGER.log(LOGLEVEL, "Expired {0} prongs at {1}", 
                        new Object[]{deathToll, Instant.now().toString()});
			}
		}
		LOGGER.log(LOGLEVEL, "REMOVEPRONG: OLD PRONG = {0}", oldString);
    }
    
    /**
     * Tries to find tryProng in Redis prong sorted set; if not found or expired, throws a ProngException,
     * otherwise it resets the timeout value in the set. Also clean out any expired prongs.
     *     
     * @param tryProng
     * @throws ProngException
     */
	public void matchProng(SerialProng tryProng) throws ProngException {
		Double score;
		String tryString;
		Long expireMillis;
		Double expireScore;
		Double nowScore;
		Long deathToll;
		
    	SharedUtils.checkNotNull(tryProng);
    	tryString = tryProng.getProngString();
		nowScore = Double.valueOf(System.currentTimeMillis());
		try (Jedis jebus = jebusPool.getResource()) {
			deathToll = jebus.zremrangeByScore(alixiaProngKey, Double.MIN_VALUE, nowScore);
			if (deathToll > 0) {
				LOGGER.log(LOGLEVEL, "Expired {0} prongs at {1}", new Object[]{deathToll, Instant.now().toString()});
			}
			score = jebus.zscore(alixiaProngKey, tryString);
			if (score == null) {
				throw new ProngException(tryProng);
			}
			// good prong so reset time
			expireMillis = System.currentTimeMillis() + PRONG_MILLIS;
			expireScore = Double.valueOf(expireMillis);
			jebus.zadd(alixiaProngKey, expireScore, tryString);
		}
		LOGGER.log(LOGLEVEL, "MATCHPRONG: TRY PRONG = {0}", tryString);
	}

	/**
	 * We use this set to trim the console map. And see, it's being
	 * able to do something like this in just a few lines of code that makes me like
	 * Redis a LOT.
	 * 
	 * @return The set of prong keys
	 */
	public Set<String> getCurrentProngKeys() {
		Set<String> prongKeys;
		
		try (Jedis jebus = jebusPool.getResource()) {
			prongKeys = jebus.zrange(alixiaProngKey, 0, -1);
		}
		return prongKeys;
	}
}
