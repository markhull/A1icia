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
package com.hulles.alixia.api.jebus;

import com.hulles.alixia.api.jebus.JebusPool.JebusPoolType;
import java.io.UnsupportedEncodingException;

import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;

/**
 * The JebusBible simply provides a strongly-typed way of retrieving the various Jebus strings
 * we use for keys in Alixia.
 * 
 * @author hulles
 *
 */
public final class JebusBible {	
	
	public static String getAlixiaMediaCacheHashKey(Long val, JebusPool pool) {
		String key;
        
		SharedUtils.checkNotNull(val);
        key = getStringKey(JebusKey.ALIXIAMEDIACACHEKEY, pool);
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
        key = getStringKey(JebusKey.ALIXIAERRORKEY, pool);
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
        key = getStringKey(JebusKey.ALIXIAERRORKEY, pool);
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
	public static String getAlixiaDocumentHashKey(JebusPool pool, String documentID) {
		String docKey;
        
		SharedUtils.checkNotNull(documentID);
        docKey = getStringKey(JebusKey.ALIXIADOCUMENTKEY, pool);
		return docKey + ":" + documentID;
	}

	/**
	 * Construct a hash key for an Alixia Session given an Alixian ID.
	 * 
	 * @param pool The JebusPool for which we want the key
	 * @param id The AlixianID whose session it is
	 * @return The Jebus key as a String
	 */
	public static String getAlixiaSessionHashKey(JebusPool pool, AlixianID id) {
		String sessionKey;
        
		SharedUtils.checkNotNull(id);
        sessionKey = getStringKey(JebusKey.ALIXIASESSIONKEY, pool);
		return sessionKey + ":" + id.toString();
	}
		
	/**
	 * Construct the key for the weather cache, given the city ID.
	 * 
	 * @param pool
	 * @param cityID
	 * @return
	 */
	public static String getAlixiaWeatherKey(JebusPool pool, String cityID) {
		String weatherKey;
        
		SharedUtils.checkNotNull(cityID);
        weatherKey = getStringKey(JebusKey.ALIXIAWEATHERKEY, pool);
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
			throw new AlixiaException("JebusBible: UnsupportedEncodingException", e);
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
			throw new AlixiaException("JebusBible: JebusPool doesn't match expected type of " + type.toString());
		}
	}
    
    public enum JebusKey { 
        ACTIONCOUNTERKEY("alixia:action:nextActionKey", JebusPoolType.CENTRAL),
        ALIXIAAESKEY("alixia:aes", JebusPoolType.CENTRAL),
        ALIXIAAPPSKEY("alixia:appkeys", JebusPoolType.CENTRAL),
        ALIXIAJSONAPPSKEY("alixia:jsonappkeys", JebusPoolType.CENTRAL),
        ALIXIADOCUMENTKEY("alixia:document", JebusPoolType.CENTRAL),
        ALIXIADOCUMENTCOUNTERKEY("alixia:document:nextDocumentKey", JebusPoolType.CENTRAL),
        ALIXIAERRORCOUNTERKEY("alixia:error:nextErrorKey", JebusPoolType.LOCAL),
        ALIXIAERRORKEY("alixia:error", JebusPoolType.LOCAL),
        ALIXIAERRORTIMELINEKEY("alixia:error:error_timeline", JebusPoolType.LOCAL),
        ALIXIAERRORCHANNELKEY("alixia:error:channel", JebusPoolType.LOCAL),
        ALIXIAMEDIACACHEKEY("alixia:mediacache", JebusPoolType.LOCAL),
        ALIXIAMEDIACACHECOUNTERKEY("alixia:mediacache:nextKey", JebusPoolType.LOCAL),
        ALIXIAMEDIAFILEUPDATEKEY("alixia:mediaupdate", JebusPoolType.LOCAL),
        ALIXIANCOUNTERKEY("alixia:alixian:next_alixian", JebusPoolType.CENTRAL),
        ALIXIANLPKEY("alixia:nlplist", JebusPoolType.CENTRAL),
        ALIXIAPURDAHKEY("alixia:purdah", JebusPoolType.CENTRAL),
        ALIXIASEMEMECOUNTERKEY("alixia:sememe:nextSememeKey", JebusPoolType.CENTRAL),
        ALIXIASENTENCECOUNTERKEY("alixia:sentence:nextSentenceKey", JebusPoolType.CENTRAL),
        ALIXIASESSIONKEY("alixia:session", JebusPoolType.CENTRAL),
        ALIXIASTATIONKEY("alixia:station", JebusPoolType.LOCAL),
        ALIXIAJSONSTATIONKEY("alixia:jsonstation", JebusPoolType.LOCAL),
        ALIXIASWINGHISTORYKEY("alixia:swingconsole", JebusPoolType.LOCAL),
        ALIXIATICKETCOUNTERKEY("alixia:ticket:nextTicketKey", JebusPoolType.CENTRAL),
        ALIXIATIMERCOUNTERKEY("alixia:timer:nextTimerKey", JebusPoolType.LOCAL),
        ALIXIAWEATHERKEY("alixia:weather", JebusPoolType.CENTRAL),
        CHANNELINCR("alixia:channel:next_console", JebusPoolType.CENTRAL),
        FROMCHANNEL("alixia:channel:from", JebusPoolType.CENTRAL),
        FROMTEXTCHANNEL("alixia:channel:text:from", JebusPoolType.CENTRAL),
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
        SESSION_TIMELINE("alixia:session:timeline", JebusPoolType.CENTRAL),
        SESSION_TIMESTAMP("timestamp", JebusPoolType.CENTRAL),
        SESSION_SESSIONTYPE("sessiontype", JebusPoolType.CENTRAL),
        SESSION_ISQUIET("isquiet", JebusPoolType.CENTRAL),
        TIMESTAMPFIELD("timestamp", JebusPoolType.LOCAL),
        TOCHANNEL("alixia:channel:to", JebusPoolType.CENTRAL),
        TOTEXTCHANNEL("alixia:channel:text:to", JebusPoolType.CENTRAL),
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
