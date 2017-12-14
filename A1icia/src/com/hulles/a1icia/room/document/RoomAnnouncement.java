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

import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * A RoomAnnouncement is broadcast to all rooms, but no response is expected. Its 
 * "message" is its RoomDocumentType.
 * 
 * @author hulles
 *
 */
public final class RoomAnnouncement extends RoomDocument {
	private RoomObject roomObject;
	
	public RoomAnnouncement(RoomDocumentType type) {
		this(type, (Ticket) null);
		
	}
    public RoomAnnouncement(RoomDocumentType type, Ticket ticket) {
        super(type, ticket);
        
		A1iciaUtils.checkNotNull(type);
		A1iciaUtils.nullsOkay(ticket);
    }

	public RoomObject getRoomObject() {
		
		return roomObject;
	}
	
	public void setRoomObject(RoomObject roomObject) {
		
		A1iciaUtils.nullsOkay(roomObject);
		this.roomObject = roomObject;
	}
	
	@Override
	public boolean documentIsReady() {
		
		return super.documentIsReady();
	}
	
	@Override
	public String toString() {

		return "Announcement #"  + getDocumentID() + " from " + getFromRoom();
	}

}