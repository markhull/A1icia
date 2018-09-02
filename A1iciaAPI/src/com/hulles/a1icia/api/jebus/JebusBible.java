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

import com.hulles.a1icia.api.jebus.JebusPool.JebusPoolType;
import java.io.UnsupportedEncodingException;

import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.A1iciaException;
import com.hulles.a1icia.api.shared.SharedUtils;

/**
 * The JebusBible simply provides a strongly-typed way of retrieving the various Jebus strings
 * we use for keys in A1icia.
 * 
 * @author hulles
 *
 */
public final class JebusBible {	
	
	public static String getA1iciaMediaCacheHashKey(Long val, JebusPool pool) {
		String key;
        
		SharedUtils.checkNotNull(val);
        key = getStringKey(JebusKey.ALICIAMEDIACACHEKEY, pool);
		return key + ":" + val.toString();
	}
	
	/**
	 * Construct the Jebus error hash key given the long ID of the error.
	 * 
	 * @param pool The Jebus pool
	 * @param idNo The error ID
	 * @return The key.
	 */
	public static String getErrorHashKey(JebusPool pool, Long idNo) {
		String key;
        
		SharedUtils.checkNotNull(idNo);
        key = getStringKey(JebusKey.ALICIAERRORKEY, pool);
		return key + ":" + idNo;
	}
    
	/**
	 * Construct the Jebus error hash key given the string ID of the error.
	 * 
	 * @param pool The Jebus pool
	 * @param idStr The error ID as a String
	 * @return The key.
	 */
	public static String getErrorHashKey(JebusPool pool, String idStr) {
		String key;
        
		SharedUtils.checkNotNull(idStr);
        key = getStringKey(JebusKey.ALICIAERRORKEY, pool);
		return key + ":" + idStr;
	}
	
	/**
	 * Construct a WikiData property hash key given the property ID.
	 * 
	 * @param pool The Jebus pool
	 * @param propID The WikiData property ID
	 * @return The hash key as a String
	 */
	public static String getWikiDataHashKey(JebusPool pool, String propID) {
		String keyValue;
        
		SharedUtils.checkNotNull(propID);
        keyValue = getStringKey(JebusKey.WIKIDATAKEY, pool);
		return keyValue + ":" + propID;
	}
	
	/**
	 * Construct a Penn Treebank hash key given the tag.
	 * 
	 * @param pool The Jebus pool
	 * @param tag The PT tag to hash
	 * @return The key as a String
	 */
	public static String getPennTreebankHashKey(JebusPool pool, String tag) {
		String keyValue;
        
		SharedUtils.checkNotNull(tag);
        keyValue = getStringKey(JebusKey.PENNTREEBANKKEY, pool);
		return keyValue + ":" + tag;
	}
	
		
	/**
	 * Get the Jebus hash key for a document. Note that we're currently not using it
	 * as a hash, since we're just storing one datum (the document graph); it's a plain
	 * Redis key.
	 *  
     * @param pool The Jebus pool we want to use
	 * @param documentID The ID of the document
	 * @return The Jebus key value as a String
	 */
	public static String getA1iciaDocumentHashKey(JebusPool pool, String documentID) {
		String docKey;
        
		SharedUtils.checkNotNull(documentID);
        docKey = getStringKey(JebusKey.ALICIADOCUMENTKEY, pool);
		return docKey + ":" + documentID;
	}

	/**
	 * Construct a hash key for an A1icia Session given an A1ician ID.
	 * 
	 * @param pool The JebusPool for which we want the key
	 * @param id The A1icianID whose session it is
	 * @return The Jebus key as a String
	 */
	public static String getA1iciaSessionHashKey(JebusPool pool, A1icianID id) {
		String sessionKey;
        
		SharedUtils.checkNotNull(id);
        sessionKey = getStringKey(JebusKey.ALICIASESSIONKEY, pool);
		return sessionKey + ":" + id.toString();
	}
		
	/**
	 * Construct the key for the weather cache, given the city ID.
	 * 
	 * @param pool
	 * @param cityID
	 * @return
	 */
	public static String getA1iciaWeatherKey(JebusPool pool, String cityID) {
		String weatherKey;
        
		SharedUtils.checkNotNull(cityID);
        weatherKey = getStringKey(JebusKey.ALICIAWEATHERKEY, pool);
		return weatherKey + ":" + cityID;
	}    
    
    /**
     * Get a Jebus key value
     * 
     * @param key The JebusKey enum
     * @param pool The pool for which we want the key
     * @return  The key value as a String
     */
 	public static String getStringKey(JebusKey key, JebusPool pool) {
		
        SharedUtils.checkNotNull(key);
        SharedUtils.checkNotNull(pool);
		matchPool(pool, key.getPoolType());
		return key.getKeyValue();
	}
   
    /**
     * Get a Jebus key value
     * 
     * @param key The JebusKey enum
     * @param pool The pool for which we want the key
     * @return  The key value as a byte array
     */
	public static byte[] getBytesKey(JebusKey key, JebusPool pool) {
		String keyValue;
        
        SharedUtils.checkNotNull(key);
        SharedUtils.checkNotNull(pool);
		matchPool(pool, key.getPoolType());
        keyValue = key.getKeyValue();
		try {
			return keyValue.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new A1iciaException("JebusBible: UnsupportedEncodingException", e);
		}
	}
    
	/**
	 * Match the Jebus pool type to that of the pool.
	 *  
	 * @param pool
	 * @param type
	 */
	private static void matchPool(JebusPool pool, JebusPoolType type) {
		
		SharedUtils.checkNotNull(pool);
		SharedUtils.checkNotNull(type);
		if (pool.getPoolType() != type) {
			throw new A1iciaException("JebusBible: JebusPool doesn't match expected type of " + type.toString());
		}
	}
    
    public enum JebusKey { 
//        ALICIAACTIONKEY("a1icia:action", JebusPoolType.LOCAL),
        ALICIAACTIONCOUNTERKEY("a1icia:action:nextActionKey", JebusPoolType.CENTRAL),
        ALICIAAESKEY("a1icia:aes", JebusPoolType.CENTRAL),
        ALICIAAPPSKEY("a1icia:appkeys", JebusPoolType.CENTRAL),
        ALICIAJSONAPPSKEY("a1icia:jsonappkeys", JebusPoolType.CENTRAL),
//        ALICIACHANNELKEY("", JebusPoolType.LOCAL),
        ALICIADOCUMENTKEY("a1icia:document", JebusPoolType.CENTRAL),
        ALICIADOCUMENTCOUNTERKEY("a1icia:document:nextDocumentKey", JebusPoolType.CENTRAL),
        ALICIAERRORCOUNTERKEY("a1icia:error:nextErrorKey", JebusPoolType.LOCAL),
        ALICIAERRORKEY("a1icia:error", JebusPoolType.LOCAL),
        ALICIAERRORTIMELINEKEY("a1icia:error:error_timeline", JebusPoolType.LOCAL),
        ALICIAERRORCHANNELKEY("a1icia:error:channel", JebusPoolType.LOCAL),
        ALICIAMEDIACACHEKEY("a1icia:mediacache", JebusPoolType.LOCAL),
        ALICIAMEDIACACHECOUNTERKEY("a1icia:mediacache:nextKey", JebusPoolType.LOCAL),
        ALICIAMEDIAFILEUPDATEKEY("alicia:mediaupdate", JebusPoolType.LOCAL),
        ALICIANCOUNTERKEY("a1icia:a1ician:next_a1ician", JebusPoolType.CENTRAL),
        ALICIANLPKEY("a1icia:nlplist", JebusPoolType.CENTRAL),
        ALICIAPURDAHKEY("a1icia:purdah", JebusPoolType.CENTRAL),
        ALICIASEMEMECOUNTERKEY("a1icia:sememe:nextSememeKey", JebusPoolType.CENTRAL),
//        ALICIASENTENCEKEY("a1icia:sentence", JebusPoolType.LOCAL),
        ALICIASENTENCECOUNTERKEY("a1icia:sentence:nextSentenceKey", JebusPoolType.CENTRAL),
        ALICIASESSIONKEY("a1icia:session", JebusPoolType.CENTRAL),
//        ALICIASPARKKEY("a1icia:sememe", JebusPoolType.LOCAL),
        ALICIASTATIONKEY("a1icia:station", JebusPoolType.LOCAL),
        ALICIAJSONSTATIONKEY("a1icia:jsonstation", JebusPoolType.LOCAL),
        ALICIASWINGHISTORYKEY("a1icia:swingconsole", JebusPoolType.LOCAL),
//        ALICIATICKETKEY("a1icia:ticket", JebusPoolType.LOCAL),
        ALICIATICKETCOUNTERKEY("a1icia:ticket:nextTicketKey", JebusPoolType.CENTRAL),
        ALICIATIMERCOUNTERKEY("a1icia:timer:nextTimerKey", JebusPoolType.LOCAL),
        ALICIAWEATHERKEY("a1icia:weather", JebusPoolType.CENTRAL),
        CHANNELINCR("a1icia:channel:next_console", JebusPoolType.CENTRAL),
        FROMCHANNEL("a1icia:channel:from", JebusPoolType.CENTRAL),
        FROMTEXTCHANNEL("a1icia:channel:text:from", JebusPoolType.CENTRAL),
        MEDIABYTESFIELD("mediabytes", JebusPoolType.LOCAL),
        MEDIAFORMATFIELD("mediafmt", JebusPoolType.LOCAL),
        MESSAGEFIELD("message", JebusPoolType.LOCAL),
        PENNTREEBANKKEY("penntreebank:tag", JebusPoolType.CENTRAL),
        PENNTREEBANKSETKEY("penntreebank:set", JebusPoolType.CENTRAL),
        PTCUPSYMBOL("cup_symbol", JebusPoolType.CENTRAL),
        PTTAGDESCRIPTION("tag_desc", JebusPoolType.CENTRAL),
        SESSION_LANGUAGE("language", JebusPoolType.CENTRAL),
        SESSION_PERSONUUID("personuuid", JebusPoolType.CENTRAL),
        SESSION_STATIONID("station", JebusPoolType.CENTRAL),
        SESSION_TIMELINE("a1icia:session:timeline", JebusPoolType.CENTRAL),
        SESSION_TIMESTAMP("timestamp", JebusPoolType.CENTRAL),
        SESSION_SESSIONTYPE("sessiontype", JebusPoolType.CENTRAL),
        TIMESTAMPFIELD("timestamp", JebusPoolType.LOCAL),
        TOCHANNEL("a1icia:channel:to", JebusPoolType.CENTRAL),
        TOTEXTCHANNEL("a1icia:channel:text:to", JebusPoolType.CENTRAL),
        WIKIDATADESC("description", JebusPoolType.CENTRAL),
        WIKIDATAKEY("wikidata:property", JebusPoolType.CENTRAL),
        WIKIDATALABEL("label", JebusPoolType.CENTRAL),
        WIKIDATATYPE("datatype", JebusPoolType.CENTRAL);
        private final String keyValue;
        private final JebusPoolType poolType;

        private JebusKey(String keyValue, JebusPoolType poolType) {

            this.keyValue = keyValue;
            this.poolType = poolType;
        }

        public String getKeyValue() {

            return keyValue;
        }

        public JebusPoolType getPoolType() {

            return poolType;
        }
        
    }
}
