package com.hulles.a1icia.api.remote;

import java.io.Serializable;

import com.hulles.a1icia.api.jebus.JebusApiBible;
import com.hulles.a1icia.api.jebus.JebusApiHub;
import com.hulles.a1icia.api.jebus.JebusPool;
import com.hulles.a1icia.api.shared.SharedUtils;

import redis.clients.jedis.Jedis;

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
