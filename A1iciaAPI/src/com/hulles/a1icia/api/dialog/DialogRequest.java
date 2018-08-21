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
package com.hulles.a1icia.api.dialog;

import java.util.Set;

import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.SerialPerson;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SerialStation;
import com.hulles.a1icia.api.shared.SerialUUID;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.media.Language;

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
	private A1iciaClientObject requestObject;
	private A1icianID fromA1icianID;
	private SerialUUID<SerialPerson> personUUID;
	private SerialUUID<SerialStation> stationUUID;
	private A1icianID toA1icianID;
	private Language language;
	
	public DialogRequest() {
		super();
		
	}

	/**
	 * Get the original language of the DialogRequest
	 * 
	 * @return The language
	 */
	public Language getLanguage() {
		
		return language;
	}

	/**
	 * Set the original language of the DialogRequest
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
	public A1icianID getFromA1icianID() {
		
		return fromA1icianID;
	}

	public void setFromA1icianID(A1icianID a1icianID) {
		
		SharedUtils.checkNotNull(a1icianID);
		this.fromA1icianID = a1icianID;
	}
	
	@Override
	public A1icianID getToA1icianID() {
	
		return toA1icianID;
	}
	
	public void setToA1icianID(A1icianID a1icianID) {
	
		SharedUtils.checkNotNull(a1icianID);
		this.toA1icianID = a1icianID;
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
	public A1iciaClientObject getClientObject() {
		
		return requestObject;
	}
	
	public void setClientObject(A1iciaClientObject obj) {
		
		SharedUtils.nullsOkay(obj);
		this.requestObject = obj;
	}
	
	public boolean isValid() {
		A1iciaClientObject clientObject;
		
		if (getFromA1icianID() == null) {
			return false;
		}
		if (getStationUUID() == null) {
			return false;
		}
		if (getToA1icianID() == null) {
			return false;
		}
		if (getLanguage() == null) {
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
		sb.append("Language: ");
		if (language == null) {
			sb.append("NULL");
		} else {
			sb.append(language.getDisplayName());
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
		sb.append("\nFrom A1ician ID: ");
		sb.append(fromA1icianID);
		sb.append("\nTo A1ician ID: ");
		sb.append(toA1icianID);
		sb.append("\n");
		return sb.toString();
	}
}
