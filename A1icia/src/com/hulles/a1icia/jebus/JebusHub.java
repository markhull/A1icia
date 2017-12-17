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
package com.hulles.a1icia.jebus;

import com.hulles.a1icia.jebus.JebusPool.JebusPoolType;
import com.hulles.a1icia.tools.A1iciaUtils;

import redis.clients.jedis.JedisPoolConfig;

/**
 * Jebus hub exists to provide a JebusPool (aka a JedisPool) to anyone who asks, and to eventually
 * destroy the pool during cleanup. Note to self: make sure we actually do the cleanup.
 * 
 * @author hulles
 *
 */
public final class JebusHub {
	private static final int MAX_HARD_OUTPUT_BUFFER_LIMIT = 64 * 1024 * 1024;
	private static final int MAX_SOFT_OUTPUT_BUFFER_LIMIT = 16 * 1024 * 1024;
	private static final String CENTRAL_SERVER = "10.0.0.3"; // get it from AppKeys...
	private static final String LOCAL_SERVER = "localhost";
	private static final int CENTRAL_SERVER_PORT = 6379;
	private static final int LOCAL_SERVER_PORT = 6379;
	private static JebusPool jebusCentral = null;
	private static JebusPool jebusLocal = null;
	private static JebusMonitor jebusCentralMonitor = null;
	private static JebusMonitor jebusLocalMonitor = null;

	private JebusHub() {
		// this only contains static methods; you can't instantiate it
		
	}
	
	public static String getCentralServerName() {
		
		return CENTRAL_SERVER;
	}
	
	/**
	 * Get the official A1icia III JebusPool, creating one if it doesn't exist. Optionally
	 * create a new JebusMonitor as well. Because we're the server, jebusCentral and jebusLocal
	 * can have the same host and port number. So far I have no opinion on whether they *should*
	 * or not. TODO evaluate performance with both the same.
	 * 
	 * @return The JebusPool
	 */
	public static JebusPool getJebusCentral() {
		
		return getJebusCentral(false);
	}
	public static JebusPool getJebusCentral(Boolean alsoStartMonitor) {
		
		A1iciaUtils.checkNotNull(alsoStartMonitor);
		return getJebusCentral(CENTRAL_SERVER, CENTRAL_SERVER_PORT, alsoStartMonitor);
	}
	public synchronized static JebusPool getJebusCentral(String host, Integer port, Boolean alsoStartMonitor) {
		
		A1iciaUtils.checkNotNull(alsoStartMonitor);
		A1iciaUtils.checkNotNull(host);
		A1iciaUtils.checkNotNull(port);
		if (jebusCentral == null) {
			jebusCentral = new JebusPool(JebusPoolType.CENTRAL, new JedisPoolConfig(), host, port);
			if (alsoStartMonitor) {
				jebusCentralMonitor = new JebusMonitor(jebusCentral);
			}
		}
		return jebusCentral;
	}
	
	/**
	 * Get the local A1icia III JebusPool, creating one if it doesn't exist. Optionally
	 * create a new local JebusMonitor as well.
	 * 
	 * @see getJebusCentral
	 * 
	 * @return The JebusPool
	 */
	public static JebusPool getJebusLocal() {
		
		return getJebusLocal(false);
	}
	public static JebusPool getJebusLocal(Boolean alsoStartMonitor) {
		
		A1iciaUtils.checkNotNull(alsoStartMonitor);
		return getJebusLocal(LOCAL_SERVER, LOCAL_SERVER_PORT, alsoStartMonitor);
	}
	public synchronized static JebusPool getJebusLocal(String host, Integer port, Boolean alsoStartMonitor) {
		
		A1iciaUtils.checkNotNull(alsoStartMonitor);
		A1iciaUtils.checkNotNull(host);
		A1iciaUtils.checkNotNull(port);
		if (jebusLocal == null) {
			jebusLocal = new JebusPool(JebusPoolType.LOCAL, new JedisPoolConfig(), host, port);
			if (alsoStartMonitor) {
				jebusLocalMonitor = new JebusMonitor(jebusLocal);
			}
		}
		return jebusLocal;
	}
	
	/**
	 * Get the Redis "hard" output buffer limit for pub/sub in bytes. This can be set in 
	 * the Redis config file. We can also look it up, though it might change if set elsewhere.
	 * 
	 * @see getMaxSoftOutputBufferLimit
	 * 
	 * @return The maximum buffer size in bytes
	 */
	public static int getMaxHardOutputBufferLimit() {
		
		return MAX_HARD_OUTPUT_BUFFER_LIMIT;
	}
	
	/**
	 * Get the Redis "soft" output buffer limit for pub/sub in bytes. This can be set in 
	 * the Redis config file. We can also look it up, though it might change if set elsewhere.
	 * 
	 * @see getMaxHardOutputBufferLimit
	 * 
	 * @return The maximum buffer size in bytes
	 */
	public static int getMaxSoftOutputBufferLimit() {
		
		return MAX_SOFT_OUTPUT_BUFFER_LIMIT;
	}
	
	/**
	 * Close any monitors and destroy the JebusPools.
	 * 
	 */
	public static void destroyJebusPools() {
		
		if (jebusCentral != null) {
			if (jebusCentralMonitor != null) {
				jebusCentralMonitor.close();
				jebusCentralMonitor = null;
			}
			jebusCentral.realDestroy();
			jebusCentral = null;
		}
		if (jebusLocal != null) {
			if (jebusLocalMonitor != null) {
				jebusLocalMonitor.close();
				jebusLocalMonitor = null;
			}
			jebusLocal.realDestroy();
			jebusLocal = null;
		}
	}
}
