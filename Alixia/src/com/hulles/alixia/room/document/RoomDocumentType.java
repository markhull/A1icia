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

/**
 * A list of document types for RoomRequests, RoomResponses, and their various relatives. This is
 * used as the graph edge type.
 * 
 * @author hulles
 *
 */
public enum RoomDocumentType {
	
	// ANNOUNCEMENTS
	STARTUP, // System startup announcement
	SHUTDOWN, // System shutdown announcement
    TICKETOPENED, // A new ticket was opened
    TICKETCLOSED, // A ticket was closed
    LOGGEDIN, // An Alixian was logged in
    LOGGEDOUT, // An Alixian was logged out
	
	// REQUESTS
	WHATSPARKS, // Request asking who responds to what sememes
	ROOMREQUEST, // Request from a room asking for responses
	
	// RESPONSES
	ROOMRESPONSE // Response to request from room

}
