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
package com.hulles.a1icia.api.jebus;

import com.hulles.a1icia.api.shared.A1iciaAPIException;
import com.hulles.a1icia.api.shared.SharedUtils;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * The reason for this existing as a subclass of JedisPool is for type checking, and because
 * it is very easy to accidentally close the pool so we make it harder here.
 * 
 * @author hulles
 *
 */
public final class JebusPool extends JedisPool {
	private final JebusPoolType poolType; 
	
	JebusPool(JebusPoolType type) {
		super();
		
		SharedUtils.checkNotNull(type);
		this.poolType = type;
	}
	
	JebusPool(JebusPoolType type, JedisPoolConfig jedisPoolConfig, String host, Integer port) {
		super(jedisPoolConfig, host, port);
		
		SharedUtils.checkNotNull(type);
		this.poolType = type;
	}
	JebusPool(JebusPoolType type, JedisPoolConfig jedisPoolConfig, String host, Integer port, 
			int readTimeout) {
		super(jedisPoolConfig, host, port, readTimeout);
		
		SharedUtils.checkNotNull(type);
		this.poolType = type;
	}

	/**
	 * Return the type of this pool, central or local.
	 * 
	 * @return The type
	 */
	public JebusPoolType getPoolType() {
	
		return poolType;
	}
	
	/**
	 * Close the pool from a public location, which throws an error. Let JebusHub close it.
	 * We're supposed to really release the resources in a Closeable close fail, *then*
	 * throw the exception.
	 * 
	 * @see realClose
	 * 
	 */
	@Override
	public final void close() {
		
		realClose();
		throw new A1iciaAPIException("JebusAPIPool: attempting to close pool remotely");
	}
	
	void realClose() {
		super.close();
	}
	
	
	
	/**
	 * Destroy the pool from a public location, which throws an error. Let JebusHub close it.
	 * We're supposed to really release the resources in a Closeable close fail, *then*
	 * throw the exception. This is equivalent to the close fail {Jedis close just calls destroy()},
	 * but of course that might not always be true so we emulate the two Jedis methods here.
	 * 
	 * @see realDestroy
	 * 
	 */
	@Override
	public final void destroy() {
		
		realDestroy();
		throw new A1iciaAPIException("JebusAPIPool: attempting to destroy pool remotely");
	}
	
	/**
	 * Really close the pool. Note that this is package-visible, not public.
	 * 
	 */
	void realDestroy() {
		super.destroy();
	}
	
	public enum JebusPoolType {
		CENTRAL,
		LOCAL
	}
}
