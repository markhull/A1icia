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
package com.hulles.a1icia.jebus;

import java.io.UnsupportedEncodingException;

import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.jebus.JebusPool.JebusPoolType;

/**
 * The JebusBible simply provides a strongly-typed way of retrieving the various Jebus strings
 * we use for keys in A1icia.
 * 
 * @author hulles
 *
 */
public final class JebusBible {
	private final static String ALICIAERRORKEY = "a1icia:error";
	private final static String MESSAGEFIELD = "message";
	private final static String TIMESTAMPFIELD = "timestamp";
	
    private final static String WIKIDATAKEY = "wikidata:property";
    private final static String WIKIDATALABEL = "label";
    private final static String WIKIDATADESC = "description";
    private final static String WIKIDATATYPE = "datatype";
	
    private final static String PENNTREEBANKKEY = "penntreebank:tag";
    private final static String PENNTREEBANKSETKEY = "penntreebank:set";
    private final static String PTCUPSYMBOL = "cup_symbol";
    private final static String PTTAGDESCRIPTION = "tag_desc";
    
    private final static String ALICIASESSIONKEY = "a1icia:session";
    private final static String SESSION_PERSONUUID = "personuuid";
    private final static String SESSION_TIMESTAMP = "timestamp";
    private final static String SESSION_TIMELINE = "timeline";
    private final static String SESSION_STATIONID = "station";
    private final static String SESSION_LANGUAGE = "language";
	
    private final static String ALICIATIMERKEY = "a1icia:timer";
 
    private final static String ALICIADOCUMENTKEY = "a1icia:document";
    
    private final static String ALICIATICKETKEY = "a1icia:ticket";
    
    private final static String ALICIASENTENCEKEY = "a1icia:sentence";
    
    private final static String ALICIASPARKKEY = "a1icia:sememe";
    
    private final static String ALICIAACTIONKEY = "a1icia:action";
    
    private final static String ALICIAWEATHERKEY = "a1icia:weather";
    
    private final static String ALICIACHANNELKEY = "a1icia:channel:";
	private final static String FROMCHANNEL = "from";
	private final static String TOCHANNEL = "to";
	
	private final static String ALICIANLPKEY = "a1icia:nlplist";
	
	private final static String ALICIAMEDIAFILEUPDATEKEY = "alicia:mediaupdate";
	
    /************************/
    /***** ALICIA ERROR *****/
    /************************/

	/**
	 * Get the Jebus key for the error counter.
	 * 
	 * @return The key
	 */
	public static String getErrorCounterKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIAERRORKEY + ":nextErrorKey"; 
	}
	
	/**
	 * Get the Jebus key for the error timeline sorted set.
	 * 
	 * @return The key
	 */
	public static String getErrorTimelineKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIAERRORKEY + ":error_timeline";
	}
	
    /************************/
    /***** ALICIA TIMER *****/
    /************************/

	/**
	 * Get the Jebus key for the timer counter.
	 * 
	 * @return The key
	 */
	public static String getTimerCounterKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIATIMERKEY + ":nextTimerKey"; 
	}
	
	/**
	 * Get the Jebus key for the timer person set.
	 * 
	 * @return The key
	 */
	public static String DELETEgetTimerPersonKey(JebusPool pool, String id) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIATIMERKEY + ":person:" + id;
	}
	
	/**
	 * Get the Jebus key for the timer timeline sorted set.
	 * 
	 * @return The key
	 */
	public static String DELETEgetTimerTimelineKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIATIMERKEY + ":timeline";
	}
	
	/**
	 * Get the Jebus key for the Redis pub/sub channel.
	 * 
	 * @return The key
	 */
	public static String getErrorChannelKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIAERRORKEY + ":channel";
	}
	
	/**
	 * Get the Jebus field name for the message field in the error hash.
	 * 
	 * @return The key
	 */
	public static String getErrorMessageField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return MESSAGEFIELD;
	}
	
	/**
	 * Get the Jebus field name for the timestamp field in the error hash.
	 * 
	 * @return The key
	 */
	public static String getErrorTimestampField(JebusPool pool) {
	
		matchPool(pool, JebusPoolType.LOCAL);
		return TIMESTAMPFIELD;
	}
	
	/**
	 * Construct the Jebus error hash key given the long ID of the error.
	 * 
	 * @param idNo The error ID
	 * @return The key.
	 */
	public static String getErrorHashKey(JebusPool pool, Long idNo) {
		
		SharedUtils.checkNotNull(idNo);
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIAERRORKEY + ":" + idNo;
	}
	/**
	 * Construct the Jebus error hash key given the string ID of the error.
	 * 
	 * @param idNo The error ID
	 * @return The key.
	 */
	public static String getErrorHashKey(JebusPool pool, String idStr) {
		
		SharedUtils.checkNotNull(idStr);
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIAERRORKEY + ":" + idStr;
	}

	/********************/
	/***** WIKIDATA *****/
	/********************/
	
	/**
	 * Construct a WikiData property hash key given the property ID.
	 * 
	 * @param pool
	 * @param propID
	 * @return
	 */
	public static String getWikiDataHashKey(JebusPool pool, String propID) {
		
		SharedUtils.checkNotNull(propID);
		matchPool(pool, JebusPoolType.LOCAL);
		return WIKIDATAKEY + ":" + propID;
	}
	
	/**
	 * Get the key field for WikiData label.
	 * 
	 * @param pool
	 * @return
	 */
	public static String getWikiDataLabelField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return WIKIDATALABEL;
	}
	
	/**
	 * Get the key field for WikiData description.
	 * 
	 * @param pool
	 * @return
	 */
	public static String getWikiDataDescriptionField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return WIKIDATADESC;
	}
	
	/**
	 * Get the key field for WikiData type.
	 * 
	 * @param pool
	 * @return
	 */
	public static String getWikiDataTypeField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return WIKIDATATYPE;
	}
	
	/*************************/
	/***** PENN TREEBANK *****/
	/*************************/
	
	/**
	 * Construct a Penn Treebank hash key given the tag.
	 * 
	 * @param pool
	 * @param tag
	 * @return
	 */
	public static String getPennTreebankHashKey(JebusPool pool, String tag) {
		
		SharedUtils.checkNotNull(tag);
		matchPool(pool, JebusPoolType.LOCAL);
		return PENNTREEBANKKEY + ":" + tag;
	}
	
	/**
	 * Get the set key for the Penn Treebank data.
	 * 
	 * @param pool
	 * @return
	 */
	public static String getPennTreebankSetKey(JebusPool pool) {
	
		matchPool(pool, JebusPoolType.LOCAL);
		return PENNTREEBANKSETKEY;
	}
	
	/**
	 * Get the cup symbol field name.
	 * 
	 * @param pool
	 * @return
	 */
	public static String getPTCupSymbolField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return PTCUPSYMBOL;
	}
	
	/**
	 * Get the tag description field name.
	 * 
	 * @param pool
	 * @return
	 */
	public static String getPTTagDescField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return PTTAGDESCRIPTION;
	}
		
	/***************************/
	/***** ALICIA TICKET *****/
	/***************************/
	
	/**
	 * Get the Jebus key for the ticket ID counter.
	 * 
	 * @return The key
	 */
	public static String getA1iciaTicketCounterKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIATICKETKEY + ":nextTicketKey";
	}
	
	/***********************************/
	/***** ALICIA SENTENCE PACKAGE *****/
	/***********************************/
	
	/**
	 * Get the Jebus key for the sentence package ID counter.
	 * 
	 * @return The key
	 */
	public static String getA1iciaSentenceCounterKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIASENTENCEKEY + ":nextSentenceKey";
	}
	
	/********************************/
	/***** ALICIA SPARK PACKAGE *****/
	/********************************/
	
	/**
	 * Get the Jebus key for the sememe package ID counter.
	 * 
	 * @return The key
	 */
	public static String getA1iciaSememeCounterKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIASPARKKEY + ":nextSememeKey";
	}
	
	/********************************/
	/***** ALICIA ACTION PACKAGE *****/
	/********************************/
	
	/**
	 * Get the Jebus key for the action package ID counter.
	 * 
	 * @return The key
	 */
	public static String getA1iciaActionCounterKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIAACTIONKEY + ":nextActionKey";
	}
	
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
	
	/**
	 * Get the Jebus hash key for a document. Note that we're currently not using it
	 * as a hash, since we're just storing one datum (the document graph); it's a plain
	 * Redis key.
	 *  
	 * @param documentID
	 * @return
	 */
	public static String getA1iciaDocumentHashKey(JebusPool pool, String documentID) {
		
		SharedUtils.checkNotNull(documentID);
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIADOCUMENTKEY + ":" + documentID;
	}

	/**************************/
	/***** ALICIA SESSION *****/
	/**************************/
	
	/**
	 * Construct a hash key for an A1icia Session given an A1ician ID.
	 * 
	 * @param pool
	 * @param id
	 * @return
	 */
	public static String getA1iciaSessionHashKey(JebusPool pool, A1icianID id) {
		
		SharedUtils.checkNotNull(id);
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIASESSIONKEY + ":" + id.toString();
	}
	
	/**
	 * Get the field name of the session Person ID.
	 * 
	 * @param pool
	 * @return
	 */
	public static String getSessionPersonIdField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return SESSION_PERSONUUID;
	}
	
	/**
	 * Get the field name of the session timestamp.
	 * 
	 * @param pool
	 * @return
	 */
	public static String getSessionTimestampField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return SESSION_TIMESTAMP;
	}
	
	/**
	 * Get the field name of the session Station ID.
	 * 
	 * @param pool
	 * @return
	 */
	public static String getSessionStationIdField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return SESSION_STATIONID;
	}
	
	/**
	 * Get the field name of the session Language.
	 * 
	 * @param pool
	 * @return
	 */
	public static String getSessionLanguageField(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return SESSION_LANGUAGE;
	}
	
	/**
	 * Get the set key name for the session timeline.
	 * 
	 * @param pool
	 * @return
	 */
	public static String getSessionTimelineKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		return ALICIASESSIONKEY + ":" + SESSION_TIMELINE;
	}
	
	/**************************/
	/***** ALICIA WEATHER *****/
	/**************************/
	
	/**
	 * Construct the key for the weather cache, given the city ID.
	 * 
	 * @param pool
	 * @param cityID
	 * @return
	 */
	public static String getA1iciaWeatherKey(JebusPool pool, String cityID) {
		
		SharedUtils.checkNotNull(cityID);
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIAWEATHERKEY + ":" + cityID;
	}

	/**********************/
	/***** ALICIA NLP *****/
	/**********************/
	
	/**
	 * The Jebus NLP list is a LIFO list of NLP analyses generated, in the sense that 
	 * the head (the left-most item) of the list is the most recent one added.
	 * 
	 * @return
	 */
	public static String getA1iciaNLPKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIANLPKEY;
	}

	/************************/
	/***** ALICIA MEDIA *****/
	/************************/
	
	/**
	 * We store the date the media library was last updated.
	 * 
	 * @return
	 */
	public static String getA1iciaMediaFileUpdateKey(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.LOCAL);
		return ALICIAMEDIAFILEUPDATEKEY;
	}
	
	/**************************/
	/***** ALICIA CHANNEL *****/
	/**************************/
	
	/**
	 * This Redis channel is from A1icia to the (unspecified) console. It is implemented
	 * as BinaryJedisPubSub, vs. JedisPubSub.
	 * 
	 * @return The channel key
	 */
	public static byte[] getA1iciaFromChannelBytes(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		try {
			return (ALICIACHANNELKEY + FROMCHANNEL).getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new A1iciaException("JebusBible: UnsupportedEncodingException", e);
		}
	}
	
	/**
	 * This Redis channel is to A1icia from the (unspecified) console. It is implemented
	 * as BinaryJedisPubSub, vs. JedisPubSub.
	 * 
	 * @return The channel key
	 */
	public static byte[] getA1iciaToChannelBytes(JebusPool pool) {
		
		matchPool(pool, JebusPoolType.CENTRAL);
		try {
			return (ALICIACHANNELKEY + TOCHANNEL).getBytes("UTF-8");
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
}
