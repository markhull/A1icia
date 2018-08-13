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
package com.hulles.a1icia.api.shared;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.SecretKey;

import com.hulles.a1icia.api.jebus.JebusApiBible;
import com.hulles.a1icia.api.jebus.JebusApiHub;
import com.hulles.a1icia.api.jebus.JebusPool;
import com.hulles.a1icia.crypto.A1iciaCrypto;

import redis.clients.jedis.Jedis;

/**
 * The purdah is where we keep the sensitive material: passwords, tokens, IDs and the like.
 * 
 * @author hulles
 *
 */
public class PurdahKeys implements Serializable {
	private static final long serialVersionUID = 2753315524334338502L;
	private static PurdahKeys instance = null;
	private Map<PurdahKey, String> keyMap;

	public PurdahKeys() {
		// needs explicit no-args constructor
	}
	
	public static synchronized PurdahKeys getInstance() {
	
		if (instance ==  null) {
			instance = getPurdahKeys();
		}
		return instance;
	}
	
	public String getPurdahKey(PurdahKey key) {
		
		SharedUtils.checkNotNull(key);
		return keyMap.get(key);
	}

	public void setPurdahKey(PurdahKey key, String value) {
		
		SharedUtils.checkNotNull(key);
		SharedUtils.checkNotNull(value);
		keyMap.put(key, value);
	}

/*	public String getWolframRemoteID() {
		
		return keyMap.get(PurdahKey.WOLFRAMALPHAREMOTEKEY);
	}

	public void setWolframRemoteID(String id) {
		
		SharedUtils.checkNotNull(id);
		keyMap.put(PurdahKey.WOLFRAMALPHAREMOTEKEY, id);
	}

	public String getDatabaseUser() {
		
		return keyMap.get(PurdahKey.DATABASEUSER);
	}

	public void setDatabaseUser(String user) {
		
		SharedUtils.checkNotNull(user);
		keyMap.put(PurdahKey.DATABASEUSER, user);
	}

	public String getDatabasePassword() {
		
		return keyMap.get(PurdahKey.DATABASEPW);
	}

	public void setDatabasePassword(String password) {
		
		SharedUtils.checkNotNull(password);
		keyMap.put(PurdahKey.DATABASEPW, password);
	}

	public String getDatabaseServer() {
		
		return keyMap.get(PurdahKey.DATABASESERVER);
	}

	public void setDatabaseServer(String server) {
		
		SharedUtils.checkNotNull(server);
		keyMap.put(PurdahKey.DATABASESERVER, server);
	}

	public Integer getDatabasePort() {
		String portStr;
		
		portStr = keyMap.get(PurdahKey.DATABASEPORT);
		return Integer.parseInt(portStr);
	}

	public void setDatabasePort(Integer port) {
		String portStr;
		
		SharedUtils.checkNotNull(port);
		portStr = port.toString();
		keyMap.put(PurdahKey.DATABASEPORT, portStr);
	}
	
	public String getDatabaseName() {
		
		return keyMap.get(PurdahKey.DATABASENAME);
	}
	
	public void setDatabaseName(String name) {
		
		SharedUtils.checkNotNull(name);
		keyMap.put(PurdahKey.DATABASENAME, name);
	}

	public Boolean getDatabaseUseSSL() {
		String sslStr;
		
		sslStr = keyMap.get(PurdahKey.DATABASEUSESSL);
		return Boolean.parseBoolean(sslStr);
	}
	
	public void setDatabaseUseSSL(Boolean value) {
	
		SharedUtils.checkNotNull(value);
		keyMap.put(PurdahKey.DATABASEUSESSL, value.toString());
	}

	public String getIpInfoToken() {
		
		return keyMap.get(PurdahKey.IPINFOKEY);
	}

	public void setIpInfoToken(String token) {
		
		SharedUtils.checkNotNull(token);
		keyMap.put(PurdahKey.IPINFOKEY, token);
	}

	public String getOpenWeatherID() {
		
		return keyMap.get(PurdahKey.OPENWEATHERID);
	}

	public void setOpenWeatherID(String openWeatherID) {
		
		SharedUtils.checkNotNull(openWeatherID);
		keyMap.put(PurdahKey.OPENWEATHERID, openWeatherID);
	}
*/
	public static void setInstance(PurdahKeys keys) {
		
		SharedUtils.checkNotNull(keys);
		instance = keys;
	}
	
	@SuppressWarnings("resource")
	private static PurdahKeys getPurdahKeys() {
		SecretKey aesKey = null;
		byte[] purdahKeyBytes;
		byte[] purdah;
		byte[] purdahBytes;
		PurdahKeys purdahKeys;
		JebusPool jebusPool;
		
		jebusPool = JebusApiHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			try {
				aesKey = A1iciaCrypto.getA1iciaFileAESKey();
			} catch (Exception e) {
				throw new A1iciaAPIException("PurdahKeys: can't recover AES key", e);
			}
			purdahKeyBytes = JebusApiBible.getA1iciaPurdahKey(jebusPool).getBytes();
			purdah = jebus.get(purdahKeyBytes);
			try {
				purdahBytes = A1iciaCrypto.decrypt(aesKey, purdah);
			} catch (Exception e) {
				throw new A1iciaAPIException("PurdahKeys: can't decrypt purdah", e);
			}
			try {
				purdahKeys = (PurdahKeys) Serialization.deSerialize(purdahBytes);
			} catch (ClassNotFoundException | IOException e) {
				throw new A1iciaAPIException("PurdahKeys: can't deserialize purdah", e);
			}
		}
		return purdahKeys;
	}
	
	/**
	 * Replace the keyMap with the contents of the new string map. If there is
	 * is an error while looking up the enum values we return false, otherwise 
	 * we return true if the map is replaced. If an error occurs the keyMap 
	 * is not modified in any way.
	 *  
	 * @param stringMap The map with updated contents
	 * @return True if our map was updated, false otherwise
	 */
	public boolean setKeyMap(Map<String, String> stringMap) {
		Map<PurdahKey, String> newMap;
		PurdahKey key;
		
		SharedUtils.checkNotNull(stringMap);
		newMap = new HashMap<>(stringMap.size());
		for (Entry<String, String> entry : stringMap.entrySet()) {
			try {
				key = PurdahKey.valueOf(entry.getKey());
			} catch (IllegalArgumentException e) {
				return false;
			}
			newMap.put(key, entry.getValue());
		}
		keyMap = new HashMap<>(newMap);
		return true;
	}
	
	/**
	 * Return a "stringified" copy of our keyMap.
	 * 
	 * @return The keyMap
	 */
	public Map<String, String> getKeyMap() {
		Map<String, String> stringMap;
		
		stringMap = new HashMap<>(keyMap.size());
		for (Entry<PurdahKey, String> entry : keyMap.entrySet()) {
			stringMap.put(entry.getKey().name(), entry.getValue());
		}
		return stringMap;
	}
	
	public enum PurdahKey {
		DATABASEPW,
		DATABASEPORT,
		DATABASESERVER,
		DATABASEUSER,
		DATABASENAME,
		DATABASEUSESSL,
		IPINFOKEY,
		OPENWEATHERID,
		WOLFRAMALPHAKEY,
		WOLFRAMALPHAREMOTEKEY
	}
}
