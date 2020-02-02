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
package com.hulles.alixia.room.document;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.alixia.ticket.Ticket;

/**
 * A RoomRequest is a request to the various rooms for action of some kind or another. 
 * The sememes say what kind of action is required.
 * 
 * @author hulles
 *
 */
public class RoomRequest extends RoomDocument {
    private final static Logger LOGGER = LoggerFactory.getLogger(RoomRequest.class);
	private String message;
	private RoomObject roomObject;
	private final List<SememePackage> sememePackages;
	
	public RoomRequest(RoomDocumentType type, Ticket ticket) {
		super(type, ticket);
		
		SharedUtils.checkNotNull(type);
		SharedUtils.checkNotNull(ticket); // ticket can't be null for RoomRequest
		sememePackages = new ArrayList<>();
	}
	public RoomRequest(Ticket ticket) {
		this(RoomDocumentType.ROOMREQUEST, ticket);
	}
	public RoomRequest(RoomDocumentType type, Ticket ticket, Long documentID) {
		super(type, ticket, documentID);
		
		SharedUtils.checkNotNull(type);
		SharedUtils.checkNotNull(ticket); // ticket can't be null for RoomRequest
		sememePackages = new ArrayList<>();
	}
	public RoomRequest(Ticket ticket, Long documentID) {
		this(RoomDocumentType.ROOMREQUEST, ticket, documentID);
	}

	public String getMessage() {
		
		return message;
	}
	
	public void setMessage(String msg) {
		
		SharedUtils.nullsOkay(msg);
		this.message = msg;
	}

	public RoomObject getRoomObject() {
		
		return roomObject;
	}

	public void setRoomObject(RoomObject obj) {
		
		SharedUtils.nullsOkay(obj);
		this.roomObject = obj;
	}
	
	/**
	 * Return a MUTABLE COPY of the request's sememe packages.
	 * 
	 * @return The copy of the set
	 */
	public List<SememePackage> getSememePackages() {
		
		return new ArrayList<>(sememePackages);
	}
	
	public void setSememePackages(List<SememePackage> pkgs) {
		
		SharedUtils.checkNotNull(pkgs);
		this.sememePackages.clear();
		this.sememePackages.addAll(pkgs);
	}

	public void addSememePackage(SememePackage sememePackage) {
	
		this.sememePackages.add(sememePackage);
	}
	
	private String dumpSememes() {
		StringBuilder sb;
		boolean first = true;
		
		sb = new StringBuilder(" Sememes: ");
		for (SememePackage pkg : sememePackages) {
			if (!first) {
				sb.append(",");
			}
			sb.append(pkg.getName());
			first = false;
		}
		return sb.toString();
	}
	
	@Override
	public boolean documentIsReady() {
		
		if (message == null && roomObject == null && sememePackages == null) {
			LOGGER.error("No payload");
			return false;
		}		
		if (sememePackages == null || sememePackages.isEmpty()) {
			LOGGER.error("Null or empty sememe packages");
			return false;
		}
		return super.documentIsReady();
	}
	
	@Override
	public String toString() {
		StringBuilder sb;
		
		sb = new StringBuilder("Room Request #");
		sb.append(getDocumentID());
		sb.append(" from ");
		sb.append(getFromRoom());
		sb.append(dumpSememes());
		return sb.toString();
	}
}
