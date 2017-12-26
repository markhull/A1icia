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
package com.hulles.a1icia.room.document;

import java.util.ArrayList;
import java.util.List;

import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * A RoomRequest is a request to the various rooms for action of some kind or another. 
 * The sparks say what kind of action is required.
 * 
 * @author hulles
 *
 */
public class RoomRequest extends RoomDocument {
	private String message;
	private RoomObject roomObject;
	private final List<SparkPackage> sparkPackages;
	
	public RoomRequest(RoomDocumentType type, Ticket ticket) {
		super(type, ticket);
		
		A1iciaUtils.checkNotNull(type);
		A1iciaUtils.checkNotNull(ticket); // ticket can't be null for RoomRequest
		sparkPackages = new ArrayList<>();
	}
	public RoomRequest(Ticket ticket) {
		this(RoomDocumentType.ROOMREQUEST, ticket);
	}
	public RoomRequest(RoomDocumentType type, Ticket ticket, Long documentID) {
		super(type, ticket, documentID);
		
		A1iciaUtils.checkNotNull(type);
		A1iciaUtils.checkNotNull(ticket); // ticket can't be null for RoomRequest
		sparkPackages = new ArrayList<>();
	}
	public RoomRequest(Ticket ticket, Long documentID) {
		this(RoomDocumentType.ROOMREQUEST, ticket, documentID);
	}

	public String getMessage() {
		
		return message;
	}
	
	public void setMessage(String msg) {
		
		A1iciaUtils.nullsOkay(msg);
		this.message = msg;
	}

	public RoomObject getRoomObject() {
		
		return roomObject;
	}

	public void setRoomObject(RoomObject obj) {
		
		A1iciaUtils.nullsOkay(obj);
		this.roomObject = obj;
	}
	
	/**
	 * Return a MUTABLE COPY of the request's spark packages.
	 * 
	 * @return The copy of the set
	 */
	public List<SparkPackage> getSparkPackages() {
		
		return new ArrayList<>(sparkPackages);
	}
	
	public void setSparkPackages(List<SparkPackage> pkgs) {
		
		A1iciaUtils.checkNotNull(pkgs);
		this.sparkPackages.clear();
		this.sparkPackages.addAll(pkgs);
	}

	public void addSparkPackage(SparkPackage sparkPackage) {
	
		this.sparkPackages.add(sparkPackage);
	}
	
	@Override
	public boolean documentIsReady() {
		
		if (message == null && roomObject == null && sparkPackages == null) {
			A1iciaUtils.error("No payload");
			return false;
		}		
		if (sparkPackages == null || sparkPackages.isEmpty()) {
			A1iciaUtils.error("Null or empty spark packages");
			return false;
		}
		return super.documentIsReady();
	}
	
	@Override
	public String toString() {

		return "Room Request #"  + getDocumentID() + " from " + getFromRoom();
	}
}
