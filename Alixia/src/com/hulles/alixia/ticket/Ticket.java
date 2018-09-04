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
package com.hulles.alixia.ticket;

import com.google.common.eventbus.EventBus;
import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusBible.JebusKey;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.SerialPerson;
import com.hulles.alixia.api.shared.SerialUUID;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.room.Room;

import redis.clients.jedis.Jedis;

/**
 * Ticket is just a wrapper around the String class, but it's important in spite of this because we
 * use it for strong type checking on the datum.
 * <p>
 * Breaking news: Ticket is more than a wrapper now. It also carries TicketJournal.
 * Breakinger news: Ticket has become a document VIP now.
 * <p>
 * We need to rethink the Ticket concept; it has changed a lot since we decoupled requests and
 * responses. TODO review.
 * 
 * @see TicketJournal
 * 
 * @author hulles
 *
 */
public final class Ticket {
	private final String idString;
	private final TicketJournal journal;
	private ActionPackage clientPackage = null;
//	private final EventBus bus;
	private AlixianID fromAlixianID;
	private SerialUUID<SerialPerson> personUUID;
    
	private Ticket(EventBus bus) {
		long idValue;
		
        SharedUtils.checkNotNull(bus);
//        this.bus = bus;
		idValue = getNewTicketID();
		this.idString = "TKT" + idValue;
		this.journal = new TicketJournal(idString);
	}
	
	/**
	 * Create a new ticket.
	 * 
     * @param bus The hall bus
     * @param room The room creating the ticket
	 * @return A new ticket
	 */
	public static Ticket createNewTicket(EventBus bus, Room room) {
		Ticket ticket;
//		RoomAnnouncement openedTicket;
        
        SharedUtils.checkNotNull(bus);
 		ticket = new Ticket(bus);
//		openedTicket = new RoomAnnouncement(RoomDocumentType.TICKETOPENED, ticket);
//		openedTicket.setFromRoom(room);
//		assert(openedTicket.documentIsReady());
//		bus.post(openedTicket);
		return ticket;
	}

    public void close() {
//		RoomAnnouncement closedTicket;
//        
// 		closedTicket = new RoomAnnouncement(RoomDocumentType.TICKETCLOSED, this);
//		closedTicket.setFromRoom(Room.CONTROLLER);
//		assert(closedTicket.documentIsReady());
//		bus.post(closedTicket);
   }
    
	public TicketJournal getJournal() {
		
		return journal;
	}

	public ActionPackage getClientPackage() {
		
		return clientPackage;
	}

	public void setClientPackage(ActionPackage clientPackage) {
		
		SharedUtils.checkNotNull(clientPackage);
		this.clientPackage = clientPackage;
	}

	public AlixianID getFromAlixianID() {
		
		return fromAlixianID;
	}

	public void setFromAlixianID(AlixianID alixianID) {
		
		SharedUtils.checkNotNull(alixianID);
		this.fromAlixianID = alixianID;
	}

	public SerialUUID<SerialPerson> getPersonUUID() {
		
		return personUUID;
	}

	public void setPersonUUID(SerialUUID<SerialPerson> uuid) {
		
		SharedUtils.nullsOkay(uuid);
		this.personUUID = uuid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idString == null) ? 0 : idString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Ticket)) {
			return false;
		}
		Ticket other = (Ticket) obj;
		if (idString == null) {
			if (other.idString != null) {
				return false;
			}
		} else if (!idString.equals(other.idString)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Override the toString method in Object to print the ID string.
	 * 
     * @return 
	 */
	@Override
	public String toString() {
		
		return idString;
	}
	
	private static long getNewTicketID() {
		JebusPool jebusPool;
        String key;
        
		jebusPool = JebusHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			key = JebusBible.getStringKey(JebusKey.ALIXIATICKETCOUNTERKEY, jebusPool);
			return jebus.incr(key);
		}		
	}
	
}
