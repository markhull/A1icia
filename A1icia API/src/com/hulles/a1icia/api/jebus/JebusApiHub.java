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
package com.hulles.a1icia.api.jebus;

import com.hulles.a1icia.api.jebus.JebusPool.JebusPoolType;
import com.hulles.a1icia.api.shared.SharedUtils;

import redis.clients.jedis.JedisPoolConfig;

/**
 * This is the API version of the A1icia Jebus hub. It exists to provide a JebusPool 
 * (aka a JedisPool) to anyone who asks, and to eventually destroy the pool during 
 * cleanup. Note to self: actually do the cleanup.
 * 
 * @author hulles
 *
 */
public final class JebusApiHub {
	private final static int JEBUS_READ_TIMEOUT = 5 * 1000; // 5 secondds
//	private final static int JEBUS_WRITE_TIMEOUT = 5 * 1000; // 5 seconds
	private static final int MAX_HARD_OUTPUT_BUFFER_LIMIT = 64 * 1024 * 1024;
	private static final int MAX_SOFT_OUTPUT_BUFFER_LIMIT = 16 * 1024 * 1024;
	private static final String CENTRAL_SERVER = "10.0.0.3"; // get it from AppKeys...
	private static final String LOCAL_SERVER = "localhost";
	private static final int CENTRAL_SERVER_PORT = 6379;
	private static final int LOCAL_SERVER_PORT = 6379;
	private static JebusPool jebusCentral = null;
	private static JebusPool jebusLocal = null;

	private JebusApiHub() {
		// this only contains static methods; you can't instantiate it
		
	}
	
	/**
	 * Get the official A1icia JebusPool (API Edition), creating one if it doesn't exist.
	 * 
	 * @return The JebusPool
	 */
	public static JebusPool getJebusCentral() {
		
		return getJebusCentral(CENTRAL_SERVER, CENTRAL_SERVER_PORT, JEBUS_READ_TIMEOUT);
	}
	public static JebusPool getJebusCentral(String host, Integer port) {
		
		return getJebusCentral(host, port, JEBUS_READ_TIMEOUT);
	}
	public synchronized static JebusPool getJebusCentral(String host, Integer port, int readTimeout) {
		
		SharedUtils.checkNotNull(readTimeout);
		SharedUtils.checkNotNull(host);
		SharedUtils.checkNotNull(port);
		if (jebusCentral == null) {
			jebusCentral = new JebusPool(JebusPoolType.CENTRAL, new JedisPoolConfig(), host, port, readTimeout);
		}
		return jebusCentral;
	}
	
	/**
	 * Get the local A1icia JebusPool, creating one if it doesn't exist.
	 * 
	 * @return The JebusPool
	 */
	public static JebusPool getJebusLocal() {
		
		return getJebusLocal(LOCAL_SERVER, LOCAL_SERVER_PORT);
	}
	public synchronized static JebusPool getJebusLocal(String host, Integer port) {
		
		SharedUtils.checkNotNull(host);
		SharedUtils.checkNotNull(port);
		if (jebusLocal == null) {
			jebusLocal = new JebusPool(JebusPoolType.LOCAL, new JedisPoolConfig(), host, port);
		}
		return jebusLocal;
	}
	
	public static int getMaxHardOutputBufferLimit() {
		
		// can also look it up, though it might change if set elsewhere
		return MAX_HARD_OUTPUT_BUFFER_LIMIT;
	}
	
	public static int getMaxSoftOutputBufferLimit() {
		
		// can also look it up, though it might change if set elsewhere
		return MAX_SOFT_OUTPUT_BUFFER_LIMIT;
	}
	
	/**
	 * Destroy the JebusPools.
	 * 
	 */
	public static void destroyJebusPools() {
		
		if (jebusCentral != null) {
			jebusCentral.realDestroy();
			jebusCentral = null;
		}
		if (jebusLocal != null) {
			jebusLocal.realDestroy();
			jebusLocal = null;
		}
	}
}
