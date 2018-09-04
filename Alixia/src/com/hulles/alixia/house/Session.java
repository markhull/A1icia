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
package com.hulles.alixia.house;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusBible.JebusKey;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.SerialPerson;
import com.hulles.alixia.api.shared.SerialStation;
import com.hulles.alixia.api.shared.SerialUUID;
import com.hulles.alixia.api.shared.SessionType;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.media.Language;

import redis.clients.jedis.Jedis;

/**
 * A Session is an instance of an Alixian at a station, communicating with Alixia proper. Sessions
 * are volatile, and expire after 15 minutes of no activity. Session data is stored in Redis.
 * 
 * @author hulles
 *
 */
public class Session {
	final static Logger LOGGER = Logger.getLogger("Alixia.AlixiaSession");
	final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
	private final static int SESSIONTTL = 60 * 15; // 15 minutes in seconds
	private final AlixianID alixianID;
	private final JebusPool jebusPool;
	private final String hashKey;
	private final String timelineKey;
	
	private Session(AlixianID alixianID) {
		
		SharedUtils.checkNotNull(alixianID);
		LOGGER.log(LOGLEVEL, "AlixiaSession: constructor");
		this.alixianID = alixianID;
		jebusPool = JebusHub.getJebusCentral();
		hashKey = JebusBible.getAlixiaSessionHashKey(jebusPool, alixianID);
		timelineKey = JebusBible.getStringKey(JebusKey.SESSION_TIMELINE, jebusPool);
	}
	
	/**
	 * Get a new instance of a session for the named Alixian.
	 * 
	 * @param alixianID The ID of the Alixian
	 * @return The session
	 */
	public static Session getSession(AlixianID alixianID) {
		Session session;
		
		SharedUtils.checkNotNull(alixianID);
 		LOGGER.log(LOGLEVEL, "AlixiaSession: getSession");
		session = new Session(alixianID);
		session.update();
		LOGGER.log(LOGLEVEL, "AlixiaSession: getSession after update");
		return session;
	}

	/**
	 * Whether it's a new session or we're updating an existing session
	 * with a new timestamp, the process is the same.
	 * 
	 */
	public void update() {
		String tsField;
		Instant timestamp;
		String timestampStr;
		Double timeScore;
		
		timestamp = Instant.now();
		timestampStr = timestamp.toString();
		// we use millis for sorting purposes
		timeScore = Double.valueOf(System.currentTimeMillis());
		tsField = JebusBible.getStringKey(JebusKey.SESSION_TIMESTAMP, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			// set hash timestamp field
			jebus.hset(hashKey, tsField, timestampStr);
			jebus.expire(hashKey, SESSIONTTL);
			// set timeline key
			jebus.zadd(timelineKey, timeScore, alixianID.toString());
			// clean up timeline
			timeScore = timeScore - (SESSIONTTL * 1000);
			jebus.zremrangeByScore(timelineKey, 0, timeScore);
		}
	}

    /**
     * Get the session type so we know if it's a text console or not.
     * 
     * @return The session type
     */
    public SessionType getSessionType() {
		String typeStr;
		String field;
		SessionType type;
        
		field = JebusBible.getStringKey(JebusKey.SESSION_SESSIONTYPE, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			typeStr = jebus.hget(hashKey, field);
		}
        type = SessionType.valueOf(typeStr);
		return type;
    }

    /**
     * Set the session type.
     * 
     * @param type The session type
     */
    public void setSessionType(SessionType type) {
		String field;
       
        SharedUtils.checkNotNull(type);
		field = JebusBible.getStringKey(JebusKey.SESSION_SESSIONTYPE, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			jebus.hset(hashKey, field, type.name());
		}
    }

    /**
     * Get whether it's "quiet time" or not.
     * 
     * @return True if it's "quiet time"
     * 
     */
    public Boolean isQuiet() {
		String typeStr;
		String field;
		Boolean quiet;
        
		field = JebusBible.getStringKey(JebusKey.SESSION_ISQUIET, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			typeStr = jebus.hget(hashKey, field);
		}
        quiet = Boolean.parseBoolean(typeStr);
		return quiet;
    }

    /**
     * Set whether it's "quiet time" or not.
     * 
     * @param quiet True if it's "quiet time"
     */
    public void setIsQuiet(Boolean quiet) {
		String field;

        SharedUtils.checkNotNull(quiet);
		field = JebusBible.getStringKey(JebusKey.SESSION_ISQUIET, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			jebus.hset(hashKey, field, quiet.toString());
		}
    }
	
	/**
	 * Get the timestamp of the last session access.
	 * 
	 * @return
	 */
	public LocalDateTime getTimestamp() {
		Instant instant;
		String field;
		String timestampStr;
		
		field = JebusBible.getStringKey(JebusKey.SESSION_TIMESTAMP, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			timestampStr = jebus.hget(hashKey, field);
		}
		if (timestampStr == null) {
			return null;
		}
		instant = Instant.parse(timestampStr);
		return LocalDateTime.from(instant);
	}
	
	/**
	 * Get the optional SerialUUID of a person for this session.
	 * 
	 * @return The SerialUUID or null if there is no person logged in for this session
	 */
	public SerialUUID<SerialPerson> getPersonUUID() {
		String personStr;
		String field;
		
		field = JebusBible.getStringKey(JebusKey.SESSION_PERSONUUID, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			personStr = jebus.hget(hashKey, field);
			if (personStr != null) {
				return new SerialUUID<>(personStr);
			}
		}
		return null;
	}

	/**
	 * Set the optional SerialUUID of the person logged in for this session.
	 * 
	 * @param personUUID
	 */
	public void setPersonUUID(SerialUUID<SerialPerson> personUUID) {
		String field;
		
		SharedUtils.nullsOkay(personUUID);
		field = JebusBible.getStringKey(JebusKey.SESSION_PERSONUUID, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			if (personUUID == null) {
				jebus.hdel(hashKey, field);
			} else {
				jebus.hset(hashKey, field, personUUID.getUUIDString());
			}
		}
	}
	
	/**
	 * Get the SerialUUID of the Station associated with this session.
	 * 
	 * @return
	 */
	public SerialUUID<SerialStation> getStationUUID() {
		String uuid;
		String field;
		
		field = JebusBible.getStringKey(JebusKey.SESSION_STATIONID, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			uuid = jebus.hget(hashKey, field);
		}
		return new SerialUUID<>(uuid);
	}

	/**
	 * Set the SerialUUID of the Station associated with this session.
	 * 
	 * @param uuid
	 */
	public void setStationUUID(SerialUUID<SerialStation> uuid) {
		String field;
		
		SharedUtils.checkNotNull(uuid);
		field = JebusBible.getStringKey(JebusKey.SESSION_STATIONID, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			jebus.hset(hashKey, field, uuid.getUUIDString());
		}
	}

	/**
	 * Get the Language for this session.
	 * 
	 * @return
	 */
	public Language getLanguage() {
		String language;
		String field;
		
		field = JebusBible.getStringKey(JebusKey.SESSION_LANGUAGE, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			language = jebus.hget(hashKey, field);
		}
		return Language.valueOf(language);
	}

	/**
	 * Set the language for this session.
	 * @param language
	 */
	public void setLanguage(Language language) {
		String field;
		
		SharedUtils.checkNotNull(language);
		field = JebusBible.getStringKey(JebusKey.SESSION_LANGUAGE, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			jebus.hset(hashKey, field, language.name());
		}
	}
	
	public AlixianID getAlixianID() {
		
		return alixianID;
	}
	
	/**
	 * Get a list of currently-active sessions for the person.
	 *
     * @param uuid The ID of the person
	 * @return The active sessions
	 */
	public static List<Session> getPersonSessions(SerialUUID<SerialPerson> uuid) {
		List<Session>  sessions;
		List<Session> userSessions;
		SerialUUID<SerialPerson> sessionUUID;
		
		SharedUtils.checkNotNull(uuid);
		sessions = getCurrentSessions();
		userSessions = new ArrayList<>(sessions.size());
		for (Session session : sessions) {
			sessionUUID = session.getPersonUUID();
			if (sessionUUID != null) {
				if (sessionUUID.equals(uuid)) {
					userSessions.add(session);
				}
			}
		}
		return userSessions;
	}
	
	/**
	 * Get a list of the currently-active sessions. Note that the session data might be
	 * gone when you try to access it after the list has been created, if it expired in
	 * the meantime.
	 * 
	 * @return The list of sessions
	 */
	@SuppressWarnings("resource")
	public static List<Session> getCurrentSessions() {
		JebusPool jebusPool;
		Set<String> alixianIDStrs;
		String timelineKey;
		List<Session> sessions;
		Session session;
		AlixianID alixianID;
		
		jebusPool = JebusHub.getJebusCentral();
		timelineKey = JebusBible.getStringKey(JebusKey.SESSION_TIMELINE, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			alixianIDStrs = jebus.zrange(timelineKey, 0, -1);
		}
		sessions = new ArrayList<>(alixianIDStrs.size());
		for (String str : alixianIDStrs) {
			alixianID = new AlixianID(str);
			session = new Session(alixianID);
			sessions.add(session);
		}
		return sessions;
	}
}
