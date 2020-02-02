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
package com.hulles.alixia.api.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusBible.JebusKey;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.media.MediaFormat;

import redis.clients.jedis.Jedis;

/**
 * MediaServer serves as a cache for media requests from e.g. a MiniConsole.
 * There should not be any Jebus collisions if more than one instance is running
 * on a local Station, if different servers instantiate their own MediaServers.
 * 
 * @author hulles
 */
public final class MediaServer {
	private final static Logger LOGGER = LoggerFactory.getLogger(MediaServer.class);
	private static final int MEDIA_CACHE_TTL = 60 * 60 * 1; // 1 hr in seconds
	
	private MediaServer()  {
        // only static methods
	}
	
    /**
     * Save the media format and byte array of a given media file.
     * 
     * @param format The media's format
     * @param audioBytes The media content as a byte array
     * @return A key to retrieve the media later
     */
	public static synchronized Long saveMediaBytes(MediaFormat format, byte[] audioBytes) {
		JebusPool jebusPool;
		String counterKey;
		Long val;
		byte[] keyBytes;
		String hashKey;
		String mediaBytesKey;
		String mediaFormatKey;
		
        SharedUtils.checkNotNull(format);
		SharedUtils.checkNotNull(audioBytes);
		LOGGER.debug("MediaServer: in saveMediaBytes, bytes len = {}", audioBytes.length);
		jebusPool = JebusHub.getJebusLocal();
		try (Jedis jebus = jebusPool.getResource()) {
			counterKey = JebusBible.getStringKey(JebusKey.ALIXIAMEDIACACHECOUNTERKEY, jebusPool);
			val = jebus.incr(counterKey);
			hashKey = JebusBible.getAlixiaMediaCacheHashKey(val, jebusPool);
			keyBytes = hashKey.getBytes();
			mediaBytesKey = JebusBible.getStringKey(JebusKey.MEDIABYTESFIELD, jebusPool);
			mediaFormatKey = JebusBible.getStringKey(JebusKey.MEDIAFORMATFIELD, jebusPool);
			jebus.hset(keyBytes, mediaBytesKey.getBytes(), audioBytes);
			jebus.hset(hashKey, mediaFormatKey, format.name());
			jebus.expire(hashKey, MEDIA_CACHE_TTL);
	        return val;
		}
	}
	
    /**
     * Retrieve the cached media object as a byte array.
     * 
     * @param key The unique key to access the media object
     * @return  The media object as a byte array
     */
	public static synchronized byte[] getMediaBytes(String key) {
		JebusPool jebusPool;
		Long val;
		String hashKey;
		byte[] mediaBytes;
		byte[] keyBytes;
		String mediaBytesKey;
		
		SharedUtils.checkNotNull(key);
		LOGGER.debug("MediaServer: in getMediaBytes");
		jebusPool = JebusHub.getJebusLocal();
		val = Long.parseLong(key);
		try (Jedis jebus = jebusPool.getResource()) {
			hashKey = JebusBible.getAlixiaMediaCacheHashKey(val, jebusPool);
			keyBytes = hashKey.getBytes();
			mediaBytesKey = JebusBible.getStringKey(JebusKey.MEDIABYTESFIELD, jebusPool);
			mediaBytes = jebus.hget(keyBytes, mediaBytesKey.getBytes());
	        LOGGER.debug("MediaServer: leaving getMediaBytes, bytes len = {}", mediaBytes.length);
	        return mediaBytes;
		}
	}
	
    /**
     * Retrieve the format of the cached media object.
     * 
     * @param key The unique key to access the media object
     * @return  The media format of the cached object
     */
	public static synchronized MediaFormat getMediaFormat(String key) {
		JebusPool jebusPool;
		Long val;
		String hashKey;
		String formatStr;
		String mediaFormatKey;
		
		SharedUtils.checkNotNull(key);
		jebusPool = JebusHub.getJebusLocal();
		val = Long.parseLong(key);
		try (Jedis jebus = jebusPool.getResource()) {
			hashKey = JebusBible.getAlixiaMediaCacheHashKey(val, jebusPool);
			mediaFormatKey = JebusBible.getStringKey(JebusKey.MEDIAFORMATFIELD, jebusPool);
			formatStr = jebus.hget(hashKey, mediaFormatKey);
	        return MediaFormat.valueOf(formatStr);
		}
	}
	
}
