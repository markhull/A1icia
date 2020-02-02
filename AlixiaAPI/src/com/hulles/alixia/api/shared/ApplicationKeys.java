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
package com.hulles.alixia.api.shared;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusBible.JebusKey;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;

import redis.clients.jedis.Jedis;

/**
 * Application keys are clear-text application constants that may change from instance to
 * instance of Alixia, e.g. file names. In other words, your application keys probably 
 * won't be the same as my application keys.
 * 
 * @author hulles
 *
 * @see com.hulles.alixia.crypto.PurdahKeys
 *
 */
public class ApplicationKeys implements Serializable {
	private static final long serialVersionUID = -8756835735170481101L;
	private static ApplicationKeys instance = null;
	private Map<ApplicationKey, String> keyMap;
    
	public ApplicationKeys() {
		// need explicit no-arg constructor
	}

	public static synchronized ApplicationKeys getInstance() {
		
		if (instance == null) {
			instance = getJebusAppKeys();
		}
		return instance;
	}
	public static synchronized ApplicationKeys getInstance(String host, Integer port) {
		
		if (instance == null) {
			instance = getJebusAppKeys(host, port);
		}
		return instance;
	}

	public static String toURL(String fileName) {
        String url;
        
        SharedUtils.checkNotNull(fileName);
        url = "file://" + fileName;
        return url;
    }
    
    public String getKey(ApplicationKey key) {
        String result;
        
		SharedUtils.checkNotNull(key);
        result = keyMap.get(key);
        if (result == null) {
        	throw new AlixiaException("ApplicationKeys: there is no value for key = " + key);
        }
        return result;
    }

    public void setKey(ApplicationKey key, String value) {
        
		SharedUtils.checkNotNull(key);
        SharedUtils.checkNotNull(value);
        keyMap.put(key, value);
    }

	private static ApplicationKeys getJebusAppKeys() {
		ApplicationKeys appKeys = null;
		byte[] appBytes;
		byte[] appKeyBytes;
		JebusPool jebusPool;
	
		jebusPool = JebusHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			appKeyBytes = JebusBible.getBytesKey(JebusKey.ALIXIAAPPSKEY, jebusPool);
			appBytes = jebus.get(appKeyBytes);
			try {
				appKeys = (ApplicationKeys) Serialization.deSerialize(appBytes);
			} catch (ClassNotFoundException | IOException e1) {
				throw new AlixiaException("ApplicationKeys: can't deserialize app keys", e1);
			}
	        return appKeys;
		}
	}
	private static ApplicationKeys getJebusAppKeys(String host, Integer port) {
		ApplicationKeys appKeys = null;
		byte[] appBytes;
		byte[] appKeyBytes;
		JebusPool jebusPool;
	
		jebusPool = JebusHub.getJebusCentral(host, port);
		try (Jedis jebus = jebusPool.getResource()) {
			appKeyBytes = JebusBible.getBytesKey(JebusKey.ALIXIAAPPSKEY, jebusPool);
			appBytes = jebus.get(appKeyBytes);
			try {
				appKeys = (ApplicationKeys) Serialization.deSerialize(appBytes);
			} catch (ClassNotFoundException | IOException e1) {
				throw new AlixiaException("ApplicationKeys: can't deserialize app keys", e1);
			}
	        return appKeys;
		}
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
		Map<ApplicationKey, String> newMap;
		ApplicationKey key;
		
		SharedUtils.checkNotNull(stringMap);
		newMap = new HashMap<>(stringMap.size());
		for (Entry<String, String> entry : stringMap.entrySet()) {
			try {
				key = ApplicationKey.valueOf(entry.getKey());
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
		for (Entry<ApplicationKey, String> entry : keyMap.entrySet()) {
			stringMap.put(entry.getKey().name(), entry.getValue());
		}
		return stringMap;
	}
	
	public enum ApplicationKey {
		SECRETKEYPATH,
		BIGJOAN,
		OWMCITY,
		FREEBASE,
		GOOGLENEWS,
		JEBUSSERVER,
		JEBUSPORT,
		LITTLEGINA,
		MIKELIBRARY,
		VIDEOLIBRARY,
		MUSICLIBRARY,
//		SYSTEMPERSONUUID,
		INCEPTIONPATH,
		INCEPTIONGRAPHURL,
		INCEPTIONLABELURL,
		OPENNLPPATH,
		TRACKERPATH,
		// from ExternalAperture
		WIKIDATAID,
		WIKIDATATITLE,
		WIKIDATASEARCH,
		OWMCURRENT,
		OWMFORECAST,
		OWMICON,
		LOCATIONURL,
		WOLFRAMVALIDATE,
		WOLFRAMQUERY,
		WOLFRAMSPOKEN,
		WOLFRAMSIMPLE,
		WOLFRAMSHORT,
		// other...
		DEEPSPEECH,
		TEMPHUMIDITY,
        GOOGLEXLATE,
        TIKA,
        EXPRESSURL,
        PARSERURL,
        NODEHOST,
        NODEPORT,
        CAYENNEXMLPATH
	}
}
