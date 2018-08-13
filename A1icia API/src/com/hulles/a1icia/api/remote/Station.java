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
package com.hulles.a1icia.api.remote;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hulles.a1icia.api.jebus.JebusApiBible;
import com.hulles.a1icia.api.jebus.JebusApiHub;
import com.hulles.a1icia.api.jebus.JebusPool;
import com.hulles.a1icia.api.shared.A1iciaAPIException;
import com.hulles.a1icia.api.shared.SerialStation;
import com.hulles.a1icia.api.shared.SerialUUID;
import com.hulles.a1icia.api.shared.Serialization;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.media.Language;

import redis.clients.jedis.Jedis;

/**
 * 
 * Station physically implements the logical "station" entity, which models the platform
 * upon which the application is running.
 * <p>
 * Any A1icia application should *always* be able to get its Station from the local machine,
 * whatever that may be. Ergo, a local implementation of Redis always needs to exist.
 * <p>
 * As currently implemented, this is <b>not GWT safe</b>: it uses LocalTime for the quiet-time 
 * demarcations. However, SerialStation s/b GWT safe; it uses java.util.Date for that reason
 * instead of java.time.LocalTime.
 * 
 * @author hulles
 *
 */
public final class Station implements Serializable {
	private static final long serialVersionUID = -3325441490956732274L;
	private static Station instance = null;
	private Map<Keys, String> keyMap;
	
	public Station() {
		// need public no-arg constructor
	}

	public static synchronized Station getInstance() {
		
		if (instance == null) {
			instance = getJebusStation();
		}
		return instance;
	}
	public static synchronized Station getInstance(String host, Integer port) {
		
		if (instance == null) {
			instance = getJebusStation(host, port);
		}
		return instance;
	}
	
	public String getCentralHost() {
		
		return keyMap.get(Keys.CENTRALHOST);
	}

	public void setCentralHost(String host) {
		
		SharedUtils.checkNotNull(host);
		keyMap.put(Keys.CENTRALHOST, host);
	}

	public Integer getCentralPort() {
		String port;
		
		port = keyMap.get(Keys.CENTRALPORT);
		return Integer.parseInt(port);
	}

	public void setCentralPort(Integer port) {
		
		SharedUtils.checkNotNull(port);
		keyMap.put(Keys.CENTRALPORT, port.toString());
	}

	public Boolean hasPicoInstalled() {
		String pico;
		
		pico = keyMap.get(Keys.HASPICO);
		return Boolean.parseBoolean(pico);
	}

	public void setHasPicoInstalled(Boolean pico) {
		
		SharedUtils.checkNotNull(pico);
		keyMap.put(Keys.HASPICO, pico.toString());
	}

	public Boolean hasMpvInstalled() {
		String mpv;
		
		mpv = keyMap.get(Keys.HASMPV);
		return Boolean.parseBoolean(mpv);
	}

	public void setHasMpvInstalled(Boolean mpv) {
		
		SharedUtils.checkNotNull(mpv);
		keyMap.put(Keys.HASMPV, mpv.toString());
	}

	public Boolean hasPrettyLights() {
		String lights;
		
		lights = keyMap.get(Keys.PRETTYLIGHTS);
		return Boolean.parseBoolean(lights);
	}

	public void setHasPrettyLights(Boolean lights) {
		
		SharedUtils.checkNotNull(lights);
		keyMap.put(Keys.PRETTYLIGHTS, lights.toString());
	}

	public Boolean hasLEDs() {
		String leds;
		
		leds = keyMap.get(Keys.LEDS);
		return Boolean.parseBoolean(leds);
	}

	public void setHasLEDs(Boolean leds) {
		
		SharedUtils.checkNotNull(leds);
		keyMap.put(Keys.LEDS, leds.toString());
	}

	public IronType getIronType() {
		String type;
		
		type = keyMap.get(Keys.IRONTYPE);
		return IronType.valueOf(type);
	}

	public void setIronType(IronType type) {
		
		SharedUtils.checkNotNull(type);
		keyMap.put(Keys.IRONTYPE, type.name());
	}

	public OsType getOsType() {
		String type;
		
		type = keyMap.get(Keys.OSTYPE);
		return OsType.valueOf(type);
	}

	public void setOsType(OsType type) {
		
		SharedUtils.checkNotNull(type);
		keyMap.put(Keys.OSTYPE, type.name());
	}

	public void ensureStationExists() {
	
		if (keyMap == null) {
			throw new A1iciaAPIException("A1iciaStation: station does not exist");
		}
	}
	
	public SerialUUID<SerialStation> getStationUUID() {
		String uuid;
		
		uuid = keyMap.get(Keys.STATIONID);
		return new SerialUUID<>(uuid);
	}

	public void setStationUUID(SerialUUID<SerialStation> uuid) {
		
		SharedUtils.checkNotNull(uuid);
		keyMap.put(Keys.STATIONID, uuid.getUUIDString());
	}
	
	public Language getDefaultLanguage() {
		String lang;
		Language language;
		
		lang = keyMap.get(Keys.DEFAULT_LANGUAGE);
		if (lang == null) {
			language = Language.AMERICAN_ENGLISH;
			setDefaultLanguage(language);
		} else {
			language = Language.valueOf(lang);
		}
		return language;
	}

	public void setDefaultLanguage(Language lang) {
		
		SharedUtils.checkNotNull(lang);
		keyMap.put(Keys.DEFAULT_LANGUAGE, lang.name());
	}

	public LocalTime getQuietStart() {
		String startStr;
		LocalTime start;
		
		startStr = keyMap.get(Keys.QUIET_START);
		if (startStr == null) {
			start = LocalTime.MIDNIGHT;
			setQuietStart(start);
		} else {
			start = LocalTime.parse(startStr);
		}
		return start;
	}
	
	public void setQuietStart(LocalTime start) {
	
		SharedUtils.checkNotNull(start);
		keyMap.put(Keys.QUIET_START, start.toString());
	}

	public LocalTime getQuietEnd() {
		String endStr;
		LocalTime end;
		
		endStr = keyMap.get(Keys.QUIET_END);
		if (endStr == null) {
			end = LocalTime.MIDNIGHT;
			setQuietEnd(end);
		} else {
			end = LocalTime.parse(endStr);
		}
		return end;
	}
	
	public void setQuietEnd(LocalTime end) {
	
		SharedUtils.checkNotNull(end);
		keyMap.put(Keys.QUIET_END, end.toString());
	}
	
	public boolean isQuiet() {
		LocalTime now;
		LocalTime start;
		LocalTime end;
		
		now = LocalTime.now();
		start = getQuietStart();
		end = getQuietEnd();
		if (start.equals(end)) {
			// if both times are equal we don't use quiet time
			return false;
		}
		if (start.isBefore(end)) {
			// the times don't straddle midnight, example: start 1AM, end 8AM
			if (now.isAfter(start) && now.isBefore(end)) {
				return true;
			}
			return false;
		}
		// Apropos of nothing, I find the logic interesting: start && end for same side of midnight, 
		//    start || end for not, unless I'm missing something. Nah, couldn't happen.
		//    I'm sure it has some explanation within set theory but I'm not going to think *that* 
		//    hard about it.
		// example: start 10PM, end 8AM
		if (now.isAfter(start) || now.isBefore(end)) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("resource")
	private static Station getJebusStation() {
		Station station = null;
		byte[] stationBytes;
		byte[] stationKeyBytes;
		JebusPool jebusPool;
	
		jebusPool = JebusApiHub.getJebusLocal();
		try (Jedis jebus = jebusPool.getResource()) {
			stationKeyBytes = JebusApiBible.getA1iciaStationKey(jebusPool).getBytes();
			stationBytes = jebus.get(stationKeyBytes);
			try {
				station = (Station) Serialization.deSerialize(stationBytes);
			} catch (ClassNotFoundException | IOException e1) {
				e1.printStackTrace();
				throw new RuntimeException("ApplicationKeys: can't deserialize station");
			}
		}
		return station;
	}
	@SuppressWarnings("resource")
	private static Station getJebusStation(String host, Integer port) {
		Station station = null;
		byte[] stationBytes;
		byte[] stationKeyBytes;
		JebusPool jebusPool;
	
		jebusPool = JebusApiHub.getJebusLocal(host, port);
		try (Jedis jebus = jebusPool.getResource()) {
			stationKeyBytes = JebusApiBible.getA1iciaStationKey(jebusPool).getBytes();
			stationBytes = jebus.get(stationKeyBytes);
			try {
				station = (Station) Serialization.deSerialize(stationBytes);
			} catch (ClassNotFoundException | IOException e1) {
				e1.printStackTrace();
				throw new RuntimeException("ApplicationKeys: can't deserialize station");
			}
		}
		return station;
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
		STATIONID,
		CENTRALHOST,
		CENTRALPORT,
		HASPICO,
		HASMPV,
		PRETTYLIGHTS,
		LEDS,
		IRONTYPE,
		OSTYPE,
		DEFAULT_LANGUAGE,
		QUIET_START,
		QUIET_END
	}
	
	public enum IronType {
		DESKTOP,
		LAPTOP,
		NOTEBOOK,
		PHONE
	}

	public enum OsType {
		LINUX,
		MAC,
		WINDOWS,
		ANDROID
	}
}
