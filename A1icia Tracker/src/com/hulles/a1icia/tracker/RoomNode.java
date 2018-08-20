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
package com.hulles.a1icia.tracker;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.graphviz.Node;
import com.hulles.a1icia.room.Room;

public class RoomNode {
	private final Room roomKey;
	private final Node node;
	
	public RoomNode(Room room) {
		
		SharedUtils.checkNotNull(room);
		this.roomKey = room;
		this.node = new Node(room);
	}
		
	public Room getRoom() {
		
		return roomKey;
	}

	public Node getDotNode() {
		
		return node;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roomKey == null) ? 0 : roomKey.hashCode());
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
		if (!(obj instanceof RoomNode)) {
			return false;
		}
		RoomNode other = (RoomNode) obj;
		if (roomKey != other.roomKey) {
			return false;
		}
		return true;
	}
	
	/**
	 * Override the toString method in Object to print the roomKey.
	 * 
	 */
	@Override
	public String toString() {
		
		return "RoomNode: " + roomKey.getDisplayName();
	}
	
}
