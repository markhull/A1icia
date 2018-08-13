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

import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * This class is for updating the answer history with a new query.
 * 
 * @author hulles
 *
 */
public class HistoryUpdate implements RoomObject {
	private Ticket ticket;
	
	public HistoryUpdate(Ticket ticket) {
	
		A1iciaUtils.checkNotNull(ticket);
		this.ticket = ticket;
	}

	public Ticket getTicket() {
	
		return ticket;
	}

	@Override
	public RoomObjectType getRoomObjectType() {

		return RoomObjectType.HISTORYUPDATE;
	}

}
