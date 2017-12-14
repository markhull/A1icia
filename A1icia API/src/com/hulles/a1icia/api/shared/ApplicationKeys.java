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
package com.hulles.a1icia.api.shared;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hulles.a1icia.api.jebus.JebusApiBible;
import com.hulles.a1icia.api.jebus.JebusApiHub;
import com.hulles.a1icia.api.jebus.JebusPool;

import redis.clients.jedis.Jedis;

/**
 * Application keys are clear-text application constants that may change from instance to
 * instance of A1icia, e.g. file names. In other words, your application keys probably 
 * won't be the same as my application keys.
 * 
 * @author hulles
 *
 * @see PurdahKeys
 *
 */
public class ApplicationKeys implements Serializable {
	private static final long serialVersionUID = -8756835735170481101L;
	private static ApplicationKeys instance = null;
	private Map<Keys, String> keyMap;
    
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
    
    public String getInceptionPath() {
        
        return keyMap.get(Keys.INCEPTIONPATH);
    }

    public void setInceptionPath(String path) {
        
        SharedUtils.checkNotNull(path);
        keyMap.put(Keys.INCEPTIONPATH, path);
    }

    public String getOpenNLPPath() {
        
        return keyMap.get(Keys.OPENNLPPATH);
    }

    public void setOpenNLPPath(String path) {
        
        SharedUtils.checkNotNull(path);
        keyMap.put(Keys.OPENNLPPATH, path);
    }

    public String getTrackerImagePath() {
        
        return keyMap.get(Keys.TRACKERPATH);
    }

    public void setTrackerImagePath(String path) {
        
        SharedUtils.checkNotNull(path);
        keyMap.put(Keys.TRACKERPATH, path);
    }

	public String getMikeLibrary() {
		
		return keyMap.get(Keys.MIKELIBRARY);
	}

	public void setMikeLibrary(String library) {
		
		SharedUtils.checkNotNull(library);
		keyMap.put(Keys.MIKELIBRARY, library);
	}

	public String getDefaultOWMCity() {
		
		return keyMap.get(Keys.OWMCITY);
	}

	public void setDefaultOWMCity(String city) {
		
		SharedUtils.checkNotNull(city);
		keyMap.put(Keys.OWMCITY, city);
	}

	public String getLittleGinaPath() {
		
		return keyMap.get(Keys.LITTLEGINA);
	}

	public void setLittleGinaPath(String path) {
		
		SharedUtils.checkNotNull(path);
		keyMap.put(Keys.LITTLEGINA, path);
	}

	public String getGoogleNewsPath() {
		
		return keyMap.get(Keys.GOOGLENEWS);
	}

	public void setGoogleNewsPath(String path) {
		
		SharedUtils.checkNotNull(path);
		keyMap.put(Keys.GOOGLENEWS, path);
	}

	public String getFreebasePath() {
		
		return keyMap.get(Keys.FREEBASE);
	}

	public void setFreebasePath(String path) {
		
		SharedUtils.checkNotNull(path);
		keyMap.put(Keys.FREEBASE, path);
	}

	public String getBigJoanPath() {
		
		return keyMap.get(Keys.BIGJOAN);
	}

	public void setBigJoanPath(String path) {
		
		SharedUtils.checkNotNull(path);
		keyMap.put(Keys.BIGJOAN, path);
	}

	public String getSystemPersonUUID() {
		
		return keyMap.get(Keys.SYSTEMPERSONUUID);
	}

	public void setSystemPersonUUID(String uuid) {
		
		SharedUtils.checkNotNull(uuid);
		keyMap.put(Keys.SYSTEMPERSONUUID, uuid);
	}

	public String getA1iciaAESKeyPath() {
	
		return keyMap.get(Keys.AESKEYPATH);
	}

	public void setA1iciaAESKeyPath(String path) {
	
		SharedUtils.checkNotNull(path);
		keyMap.put(Keys.AESKEYPATH, path);
	}

	@SuppressWarnings("resource")
	private static ApplicationKeys getJebusAppKeys() {
		ApplicationKeys appKeys = null;
		byte[] appBytes;
		byte[] appKeyBytes;
		JebusPool jebusPool;
	
		jebusPool = JebusApiHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			appKeyBytes = JebusApiBible.getA1iciaAppsKey(jebusPool).getBytes();
			appBytes = jebus.get(appKeyBytes);
			try {
				appKeys = (ApplicationKeys) Serialization.deSerialize(appBytes);
			} catch (ClassNotFoundException | IOException e1) {
				e1.printStackTrace();
				throw new RuntimeException("ApplicationKeys: can't deserialize app keys");
			}
		}
		return appKeys;
	}
	@SuppressWarnings("resource")
	private static ApplicationKeys getJebusAppKeys(String host, Integer port) {
		ApplicationKeys appKeys = null;
		byte[] appBytes;
		byte[] appKeyBytes;
		JebusPool jebusPool;
	
		jebusPool = JebusApiHub.getJebusCentral(host, port);
		try (Jedis jebus = jebusPool.getResource()) {
			appKeyBytes = JebusApiBible.getA1iciaAppsKey(jebusPool).getBytes();
			appBytes = jebus.get(appKeyBytes);
			try {
				appKeys = (ApplicationKeys) Serialization.deSerialize(appBytes);
			} catch (ClassNotFoundException | IOException e1) {
				e1.printStackTrace();
				throw new RuntimeException("ApplicationKeys: can't deserialize app keys");
			}
		}
		return appKeys;
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
		Map<Keys, String> newMap;
		Keys key;
		
		SharedUtils.checkNotNull(stringMap);
		newMap = new HashMap<>(stringMap.size());
		for (Entry<String, String> entry : stringMap.entrySet()) {
			try {
				key = Keys.valueOf(entry.getKey());
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
		for (Entry<Keys, String> entry : keyMap.entrySet()) {
			stringMap.put(entry.getKey().name(), entry.getValue());
		}
		return stringMap;
	}
	
	private enum Keys {
		AESKEYPATH,
		BIGJOAN,
		OWMCITY,
		FREEBASE,
		GOOGLENEWS,
		JEBUSSERVER,
		JEBUSPORT,
		LITTLEGINA,
		MIKELIBRARY,
		SYSTEMPERSONUUID,
		INCEPTIONPATH,
		OPENNLPPATH,
		TRACKERPATH
	}
}
