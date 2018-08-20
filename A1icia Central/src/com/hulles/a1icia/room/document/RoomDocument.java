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
package com.hulles.a1icia.room.document;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.jebus.JebusBible;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.jebus.JebusPool;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.A1iciaUtils;

import redis.clients.jedis.Jedis;

/**
 * Okay folks, listen up: here are the new Rules:
 * 
 * 1) Don't talk about RoomDocument.
 * 2) Every RoomRequest is always addressed to ALL rooms. The way a room reacts to the 
 *      RoomRequest depends upon what sememes it bears.
 * 3) The RoomDocument always has the real room that it's from as the request sender. 
 * 		No forwarding allowed.
 * 3.5) Every RoomResponse is ALWAYS made to the request sender. (No forwarding.)
 * 4) UrRoom will be in charge of marshalling the responses for a request and sending them on
 * 		to the room subclasses for processing when they are all received.
 * 5) Every room must allow for multiple legitimate answers to their request, even when they 
 * 		think only one room will answer. (Hint: more threads coming down the road in the future.) 
 * 
 * @author hulles
 *
 */
public abstract class RoomDocument {
	private final Long documentID;
	private Ticket ticket;
	private final RoomDocumentType type;
	private Room fromRoom;
	
	public RoomDocument(RoomDocumentType type, Ticket ticket) {
		this(type, ticket, getNewDocumentID());		
	}
	public RoomDocument(RoomDocumentType type, Ticket ticket, Long documentID) {
		
		SharedUtils.checkNotNull(type);
		SharedUtils.nullsOkay(ticket);
		SharedUtils.checkNotNull(documentID);
		this.type = type;
		this.ticket = ticket;
		this.documentID = documentID;
	}
	
	public Long getDocumentID() {
		
		return documentID;
	}

	public Ticket getTicket() {
	
		return ticket;
	}
	
	protected void setTicket(Ticket ticket) {
		
		SharedUtils.checkNotNull(ticket);
		this.ticket = ticket;
	}
	
	public RoomDocumentType getDocumentType() {
		
		return type;
	}

	public Room getFromRoom() {
		
		return fromRoom;
	}
	
	public void setFromRoom(Room room) {
	
		SharedUtils.checkNotNull(room);
		this.fromRoom = room;
	}
	
	public boolean documentIsReady() {
		
		if (documentID == null) {
			A1iciaUtils.error("No document ID");
			return false;
		}
		if (ticket == null) {
			A1iciaUtils.error("No ticket");
			return false;
		}
		if (type == null) {
			A1iciaUtils.error("No type");
			return false;
		}
		if (fromRoom == null) {
			A1iciaUtils.error("No from room");
			return false;
		}
		return true;
	}
	
	private static long getNewDocumentID() {
		JebusPool jebusPool;
		
		jebusPool = JebusHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			return jebus.incr(JebusBible.getA1iciaDocumentCounterKey(jebusPool));
		}		
	}
}
