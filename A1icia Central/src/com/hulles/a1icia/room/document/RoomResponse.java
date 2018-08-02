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

import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * The response from a room to a RoomRequest.
 * 
 * @author hulles
 *
 */
public final class RoomResponse extends RoomDocument {
	private final Long responseToRequestID;
	private final Room respondToRoom;
	private final List<ActionPackage> actionPackages;
	
	public RoomResponse(Ticket ticket, Long responseTo, Room respondTo) {
		super(RoomDocumentType.ROOMRESPONSE, ticket);
		
		A1iciaUtils.checkNotNull(ticket); // a little late...
		A1iciaUtils.checkNotNull(responseTo);
		A1iciaUtils.checkNotNull(respondTo);
		this.responseToRequestID = responseTo;
		this.respondToRoom = respondTo;
		actionPackages = new ArrayList<>();
	}
	public RoomResponse(RoomRequest request) {
		this(request.getTicket(), request.getDocumentID(), request.getFromRoom());
	}
	
	public Long getResponseToRequestID() {
		
		return responseToRequestID;
	}
	
	public Room getRespondToRoom() {
		
		return respondToRoom;
	}
	
	public boolean hasNoResponse() {
		
		return actionPackages.isEmpty();
	}
	
	/**
	 * Return a MUTABLE COPY of the response's action packages.
	 * 
	 * @return The copy of the set
	 */
	public List<ActionPackage> getActionPackages() {

		return new ArrayList<>(actionPackages);
	}
	
	public void addActionPackage(ActionPackage pkg) {
		
		A1iciaUtils.checkNotNull(pkg);
		// make sure the ActionPackage we're adding is kosher...
		if (!pkg.isReady()) {
			A1iciaUtils.error("RoomResponse: package not ready, so not added");
			return;
		}
		actionPackages.add(pkg);
	}

	@Override
	public boolean documentIsReady() {

		if (responseToRequestID == null) {
			return false;
		}
		if (respondToRoom == null) {
			return false;
		}
		// it's possible that actionPackages could be empty, if the room
		//    had no response for any of the sememes -- it's effectively a
		//    null response to the request
		if (getActionPackages() == null) {
			A1iciaUtils.error("No action packages");
			return false;
		}
		return super.documentIsReady();
	}

	@Override
	public String toString() {

		return "Room response from " + getFromRoom();
	}

}
