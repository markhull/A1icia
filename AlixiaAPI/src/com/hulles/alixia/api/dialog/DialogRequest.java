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
package com.hulles.alixia.api.dialog;

import java.util.Set;

import com.hulles.alixia.api.object.AlixiaClientObject;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.SerialPerson;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SerialStation;
import com.hulles.alixia.api.shared.SerialUUID;
import com.hulles.alixia.api.shared.SessionType;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.media.Language;

/**
 * This is a request (aka a query) from a client, asking for a response of some sort.
 * 
 * @author hulles
 *
 */
public class DialogRequest extends Dialog {
	private static final long serialVersionUID = 2902774382678495512L;
	private Set<SerialSememe> sememes;
	private String requestString;
	private byte[] requestAudio;
	private AlixiaClientObject requestObject;
	private AlixianID fromAlixianID;
	private SerialUUID<SerialPerson> personUUID;
	private SerialUUID<SerialStation> stationUUID;
    private SessionType sessionType;
	private AlixianID toAlixianID;
	private Language language;
    private Boolean isQuiet;
	
	public DialogRequest() {
		super();
		
	}

    /**
     * Get the session type (e.g. SERIALIZED)
     * 
     * @see com.hulles.alixia.api.shared.SessionType
     * 
     * @return The session type
     */
    public SessionType getSessionType() {
        
        return sessionType;
    }

    /**
     * Set the session type (e.g. SERIALIZED)
     * 
     * @see com.hulles.alixia.api.shared.SessionType
     * 
     * @param sessionType The session type
     */
    public void setSessionType(SessionType sessionType) {
        
        SharedUtils.checkNotNull(sessionType);
        this.sessionType = sessionType;
    }

    /**
     * True if this request was created during "quiet time"
     * 
     * @return True if it's "quiet time"
     */
    public Boolean isQuiet() {
        
        return isQuiet;
    }

    /**
     * Set whether it's "quiet time" or not
     * 
     * @param isQuiet True if it's "quiet time"
     */
    public void setIsQuiet(Boolean isQuiet) {
        
        SharedUtils.checkNotNull(isQuiet);
        this.isQuiet = isQuiet;
    }

	/**
	 * Get the original language of the DialogRequest
	 * 
     * @see com.hulles.alixia.media.Language
     * 
	 * @return The language
	 */
	public Language getLanguage() {
		
		return language;
	}

	/**
	 * Set the original language of the DialogRequest
	 * 
     * @see com.hulles.alixia.media.Language
     * 
	 * @param language The language
	 */
	public void setLanguage(Language language) {
		
		SharedUtils.checkNotNull(language);
		this.language = language;
	}

	/**
	 * Get the Station UUID of the originator
	 * 
	 * @return The UUID
	 */
	public SerialUUID<SerialStation> getStationUUID() {
		
		return stationUUID;
	}

	/**
	 * Set the Station UUID of the originator
	 * 
	 * @param uuid The UUID
	 */
	public void setStationUUID(SerialUUID<SerialStation> uuid) {
		
		SharedUtils.checkNotNull(uuid);
		this.stationUUID = uuid;
	}

	/**
	 * Get the UUID of the originating person, if known.
	 * 
	 * @return The UUID
	 */
	public SerialUUID<SerialPerson> getPersonUUID() {
		
		return personUUID;
	}

	/**
	 * Set the UUID of the originating person, or null if the person is not known.
	 * 
	 * @param personUUID The UUID
	 */
	public void setPersonUUID(SerialUUID<SerialPerson> personUUID) {
		
		SharedUtils.nullsOkay(personUUID);
		this.personUUID = personUUID;
	}

	@Override
	public AlixianID getFromAlixianID() {
		
		return fromAlixianID;
	}

	public void setFromAlixianID(AlixianID alixianID) {
		
		SharedUtils.checkNotNull(alixianID);
		this.fromAlixianID = alixianID;
	}
	
	@Override
	public AlixianID getToAlixianID() {
	
		return toAlixianID;
	}
	
	public void setToAlixianID(AlixianID alixianID) {
	
		SharedUtils.checkNotNull(alixianID);
		this.toAlixianID = alixianID;
	}
	
	public Set<SerialSememe> getRequestActions() {
		
		return sememes;
	}
	
	public void setRequestActions(Set<SerialSememe> sememes) {
		
		SharedUtils.nullsOkay(sememes);
		this.sememes = sememes;
	}
	
	public String getRequestMessage() {
		
		return requestString;
	}
	
	public void setRequestMessage(String msg) {
		
		SharedUtils.nullsOkay(msg);
		this.requestString = msg;
	}
	
	public byte[] getRequestAudio() {
		
		return requestAudio;
	}

	public void setRequestAudio(byte[] audio) {
		
		SharedUtils.nullsOkay(audio);
		this.requestAudio = audio;
	}

	@Override
	public AlixiaClientObject getClientObject() {
		
		return requestObject;
	}
	
	public void setClientObject(AlixiaClientObject obj) {
		
		SharedUtils.nullsOkay(obj);
		this.requestObject = obj;
	}
	
	public boolean isValid() {
		AlixiaClientObject clientObject;
		
		if (getFromAlixianID() == null) {
			return false;
		}
		if (getStationUUID() == null) {
			return false;
		}
		if (getToAlixianID() == null) {
			return false;
		}
		if (getLanguage() == null) {
			return false;
		}
		if (getSessionType() == null) {
			return false;
		}
		if (isQuiet() == null) {
			return false;
		}
		clientObject = getClientObject();
		if (clientObject != null) {
			if (!clientObject.isValid()) {
				return false;
			}
		}
		if (getRequestActions() == null) {
			// s/b an empty set if there are none
			return false;
		}
		if (getRequestMessage() == null && 
				clientObject == null &&
				getRequestActions().isEmpty()) {
			// nothing to do
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb;
		
		sb = new StringBuilder("DIALOG REQUEST\n");
		if (sememes == null) {
			sb.append("Sememes: NULL\n");
		} else if (sememes.isEmpty()) {
			sb.append("Sememes: EMPTY\n");
		} else {
			for (SerialSememe sememe : sememes) {
				sb.append("Sememe: ");
				sb.append(sememe.getName());
				sb.append("\n");
			}
		}
		sb.append("Session Type: ");
		if (sessionType == null) {
			sb.append("NULL");
		} else {
			sb.append(sessionType.name());
		}
		sb.append("\nLanguage: ");
		if (language == null) {
			sb.append("NULL");
		} else {
			sb.append(language.getDisplayName());
		}
		sb.append("\nIs Quiet: ");
		if (isQuiet == null) {
			sb.append("NULL");
		} else {
			sb.append(isQuiet.toString());
		}
		sb.append("\nPerson UUID: ");
		if (personUUID == null) {
			sb.append("NULL");
		} else {
			sb.append(personUUID.getUUIDString());
		}
		sb.append("\nStation UUID: ");
		if (stationUUID == null) {
			sb.append("NULL");
		} else {
			sb.append(stationUUID.toString());
		}
		sb.append("\nRequest String: ");
		sb.append(requestString);
		sb.append("\nRequest Object: ");
		if (requestObject == null) {
			sb.append("NULL");
		} else {
			sb.append(requestObject.getClientObjectType().toString());
		}
		sb.append("\nFrom Alixian ID: ");
		sb.append(fromAlixianID);
		sb.append("\nTo Alixian ID: ");
		sb.append(toAlixianID);
		sb.append("\n");
		return sb.toString();
	}
}
