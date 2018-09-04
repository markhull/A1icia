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
package com.hulles.alixia.crypto;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.SecretKey;

import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.Serialization;
import com.hulles.alixia.api.shared.SharedUtils;

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
		
		jebusPool = JebusHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			try {
				aesKey = AlixiaCrypto.getAlixiaFileAESKey();
			} catch (Exception e) {
				throw new AlixiaException("PurdahKeys: can't recover AES key", e);
			}
			purdahKeyBytes = JebusBible.getBytesKey(JebusBible.JebusKey.ALIXIAPURDAHKEY, jebusPool);
			purdah = jebus.get(purdahKeyBytes);
			try {
				purdahBytes = AlixiaCrypto.decrypt(aesKey, purdah);
			} catch (Exception e) {
				throw new AlixiaException("PurdahKeys: can't decrypt purdah", e);
			}
			try {
				purdahKeys = (PurdahKeys) Serialization.deSerialize(purdahBytes);
			} catch (ClassNotFoundException | IOException e) {
				throw new AlixiaException("PurdahKeys: can't deserialize purdah", e);
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
		WOLFRAMALPHAREMOTEKEY,
        GOOGLEXLATEKEY
	}
}
