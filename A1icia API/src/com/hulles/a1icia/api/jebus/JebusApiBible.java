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
package com.hulles.a1icia.api.jebus;

import com.hulles.a1icia.api.jebus.JebusPool.JebusPoolType;
import com.hulles.a1icia.api.shared.A1iciaAPIException;
import com.hulles.a1icia.api.shared.SharedUtils;

/**
 * The JebusBible simply provides a strongly-typed way of retrieving the various Jebus strings
 * we use for keys in A1icia III. This is the API pocket version of the bible, the one they 
 * give you at AA meetings.
 * 
 * @author hulles
 *
 */
public final class JebusApiBible {
	
    private final static String ALICIACHANNELKEY = "a1icia:channel:";
	private final static String FROMCHANNEL = "from";
	private final static String TOCHANNEL = "to";
	private final static String CHANNELINCR = "a1icia:channel:next_console";

	private final static String ALICIANCOUNTERKEY = "a1icia:a1ician:next_a1ician";
	
	private final static String ALICIAAESKEY = "a1icia:aes";
    
	private final static String ALICIAAPPSKEY = "a1icia:appkeys";
	private final static String ALICIAPURDAHKEY = "a1icia:purdah";
    
    private final static String ALICIASTATIONKEY = "a1icia:station";
	
    private final static String ALICIAMEDIACACHEKEY = "a1icia:mediacache";
    private final static String MEDIAFORMATFIELD = "mediafmt";
    private final static String MEDIABYTESFIELD = "mediabytes";
    
    private final static String ALICIADOCUMENTKEY = "a1icia:document";
	
	/***************************/
	/***** ALICIA DOCUMENT *****/
	/***************************/
	
	/**
	 * Get the Jebus key for the document ID counter.
	 * 
	 * @return The key
	 */
	public static String getA1iciaDocumentCounterKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIADOCUMENTKEY + ":nextDocumentKey";
	}
    
	/*************************/
	/***** ALICIA CRYPTO *****/
	/*************************/

	public static String getA1iciaAESKey(JebusPool pool) {

		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIAAESKEY;
	}
	
	/******************************/
	/***** ALICIA MEDIA CACHE *****/
	/******************************/
	
	public static String getA1iciaMediaCacheHashKey(Long val, JebusPool pool) {
		
		SharedUtils.checkNotNull(val);
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIAMEDIACACHEKEY + ":" + val.toString();
	}
	
	public static String getA1iciaMediaCacheCounterKey(JebusPool pool) {
	
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIAMEDIACACHEKEY + ":nextKey";
	}
	
	public static String getMediaFormatField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return MEDIAFORMATFIELD;
	}
	
	public static String getMediaBytesField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return MEDIABYTESFIELD;
	}
	
	/******************************/
	/***** ALICIA APPLICATION *****/
	/******************************/

	public static String getA1iciaAppsKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIAAPPSKEY;
	}

	public static String getA1iciaPurdahKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIAPURDAHKEY;
	}

	/**************************/
	/***** ALICIA STATION *****/
	/**************************/

	public static String getA1iciaStationKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIASTATIONKEY;
	}
	
	/*******************/
	/***** ALICIAN *****/
    /*******************/
	
	public static String getA1icianCounterKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIANCOUNTERKEY;
	}
	
	/**************************/
	/***** ALICIA CHANNEL *****/
    /**************************/
	
	public static String getA1iciaChannelCounterKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return CHANNELINCR;
	}
	
	/**
	 * This Redis channel is from A1icia to the (unspecified) console. It is implemented
	 * as BinaryJedisPubSub, vs. JedisPubSub.
	 * 
	 * @return The channel key
	 */
	public static byte[] getA1iciaFromChannelBytes(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return (ALICIACHANNELKEY + FROMCHANNEL).getBytes();
	}
	
	/**
	 * This Redis channel is to A1icia from the (unspecified) console. It is implemented
	 * as BinaryJedisPubSub, vs. JedisPubSub.
	 * 
	 * @return The channel key
	 */
	public static byte[] getA1iciaToChannelBytes(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return (ALICIACHANNELKEY + TOCHANNEL).getBytes();
	}
	
	/**
	 * Match the pool type with the pool; if they don't match throw an A1iciaAPIException.
	 * 
	 * @param pool The Jebus pool
	 * @param type The JebusPoolType
	 */
	private static void matchPool(JebusPool pool, JebusPoolType type) {
		
		SharedUtils.checkNotNull(pool);
		SharedUtils.checkNotNull(type);
		if (pool.getPoolType() != type) {
			throw new A1iciaAPIException("JebusBible: JebusPool doesn't match expected type of " + type.toString());
		}
	}
	
}
