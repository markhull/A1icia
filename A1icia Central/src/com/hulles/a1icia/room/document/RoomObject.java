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

/**
 * This is an interface for all of the yclept objects exchanged on the hall bus.
 * <p>
 * I've come here a bunch of times trying to normalize this away, but the reason I don't
 * is because the A1iciaClientObject in the API is Serializable, and this isn't.
 * 
 * @author hulles
 *
 */
public interface RoomObject {
	
	RoomObjectType getRoomObjectType();
		
	
	enum RoomObjectType {
		ROOMACTION,
		IMAGEINPUT,
		HISTORYREQUEST,
		HISTORYUPDATE,
		CLIENTREQUEST,
		SENTENCEMEANINGREQUEST,
		LOGINLOGOUT
	}
}
