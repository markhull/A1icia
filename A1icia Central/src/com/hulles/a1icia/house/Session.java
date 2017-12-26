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
package com.hulles.a1icia.house;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.jebus.JebusBible;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.jebus.JebusPool;
import com.hulles.a1icia.tools.A1iciaUtils;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.SerialPerson;
import com.hulles.a1icia.api.shared.SerialStation;
import com.hulles.a1icia.api.shared.SerialUUID;
import com.hulles.a1icia.media.Language;

import redis.clients.jedis.Jedis;

/**
 * A Session is an instance of an A1ician at a station, communicating with A1icia proper. Sessions
 * are volatile, and expire after 15 minutes of no activity. Session data is stored in Redis.
 * 
 * @author hulles
 *
 */
public class Session {
	final static Logger LOGGER = Logger.getLogger("A1icia.A1iciaSession");
	final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final static int SESSIONTTL = 60 * 15; // 15 minutes in seconds
	private final A1icianID a1icianID;
	private final JebusPool jebusPool;
	private final String hashKey;
	private final String timelineKey;
	
	private Session(A1icianID a1icianID) {
		
		A1iciaUtils.checkNotNull(a1icianID);
		LOGGER.log(LOGLEVEL, "A1iciaSession: constructor");
		this.a1icianID = a1icianID;
		jebusPool = JebusHub.getJebusCentral();
		hashKey = JebusBible.getA1iciaSessionHashKey(jebusPool, a1icianID);
		timelineKey = JebusBible.getSessionTimelineKey(jebusPool);
	}
	
	/**
	 * Get a new instance of a session for the named A1ician.
	 * 
	 * @param a1icianID
	 * @return The session
	 */
	public static Session getSession(A1icianID a1icianID) {
		Session session;
		
		A1iciaUtils.checkNotNull(a1icianID);
		LOGGER.log(LOGLEVEL, "A1iciaSession: getSession");
		session = new Session(a1icianID);
		session.update();
		LOGGER.log(LOGLEVEL, "A1iciaSession: getSession after update");
		return session;
	}

	/**
	 * Whether it's a new session or we're updating an existing session
	 * with a new timestamp, the process is the same.
	 * 
	 * @param session The session to update
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
		tsField = JebusBible.getSessionTimestampField(jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			// set hash timestamp field
			jebus.hset(hashKey, tsField, timestampStr);
			jebus.expire(hashKey, SESSIONTTL);
			// set timeline key
			jebus.zadd(timelineKey, timeScore, a1icianID.toString());
			// clean up timeline
			timeScore = timeScore - (SESSIONTTL * 1000);
			jebus.zremrangeByScore(timelineKey, 0, timeScore);
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
		
		field = JebusBible.getSessionTimestampField(jebusPool);
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
		
		field = JebusBible.getSessionPersonIdField(jebusPool);
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
		
		A1iciaUtils.nullsOkay(personUUID);
		field = JebusBible.getSessionPersonIdField(jebusPool);
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
		
		field = JebusBible.getSessionStationIdField(jebusPool);
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
		
		A1iciaUtils.checkNotNull(uuid);
		field = JebusBible.getSessionStationIdField(jebusPool);
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
		
		field = JebusBible.getSessionLanguageField(jebusPool);
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
		
		A1iciaUtils.checkNotNull(language);
		field = JebusBible.getSessionLanguageField(jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			jebus.hset(hashKey, field, language.name());
		}
	}
	
	public A1icianID getA1icianID() {
		
		return a1icianID;
	}
	
	/**
	 * Get a list of currently-active sessions for the person.
	 *
	 * 
	 */
	public static List<Session> getPersonSessions(SerialUUID<SerialPerson> uuid) {
		List<Session>  sessions;
		List<Session> userSessions;
		SerialUUID<SerialPerson> sessionUUID;
		
		A1iciaUtils.checkNotNull(uuid);
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
		Set<String> a1icianIDStrs;
		String timelineKey;
		List<Session> sessions;
		Session session;
		A1icianID a1icianID;
		
		jebusPool = JebusHub.getJebusCentral();
		timelineKey = JebusBible.getSessionTimelineKey(jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			a1icianIDStrs = jebus.zrange(timelineKey, 0, -1);
		}
		sessions = new ArrayList<>(a1icianIDStrs.size());
		for (String str : a1icianIDStrs) {
			a1icianID = new A1icianID(str);
			session = new Session(a1icianID);
			sessions.add(session);
		}
		return sessions;
	}
}
