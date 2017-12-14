package com.hulles.a1icia.api.jebus;

import com.hulles.a1icia.api.shared.A1iciaAPIException;
import com.hulles.a1icia.api.shared.SharedUtils;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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

	public JebusPoolType getPoolType() {
	
		return poolType;
	}
	
	@Override
	public final void close() {
		
		// we're supposed to really release the resources in a Closeable close fail, *then*
		//    throw the exception. So we do.
		realClose();
		throw new A1iciaAPIException("JebusAPIPool: attempting to close pool remotely");
	}
	
	void realClose() {
		super.close();
	}
	
	@Override
	public final void destroy() {
		
		// we're supposed to really release the resources in a Closeable close fail, *then*
		//    throw the exception. This is equivalent to the close fail -- Jedis close() just 
		//    calls destroy().
		realDestroy();
		throw new A1iciaAPIException("JebusAPIPool: attempting to destroy pool remotely");
	}
	
	void realDestroy() {
		super.destroy();
	}
	
	public enum JebusPoolType {
		CENTRAL,
		LOCAL
	}
}
