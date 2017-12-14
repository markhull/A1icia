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
package com.hulles.a1icia.graphviz;

import com.hulles.a1icia.room.Room;

/**
 * A node in a (mathematical) graph is connected to other nodes by edges. In A1icia,
 * nodes are rooms.
 * 
 * @author hulles
 *
 */
public class Node extends GraphObject {

	public Node(String id) {
		super(id);
	}
    public Node(Room room) {
    	
        this(room.name());
    }

    @Override
    public String genDotString() {
    	StringBuffer sb;
    	String attrStr;
    	
        sb = new StringBuffer();
        sb.append("[");
        attrStr = this.genAttributeDotString();
        sb.append(attrStr);
        sb.append("]");
        return sb.toString();
    }
    
	@Override
	public GraphObjectType getGraphObjectType() {

		return GraphObjectType.NODE;
	}

	/**
	 * Override the equals method in Object to assert that Nodes are equal if their 
	 * id values are equal.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		final Node other;
		
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Node)) {
            return false;
        }
		other = (Node) obj;
        if (!this.getId().equals(other.getId())) {
            return false;
        }
        return true;
	}
	
	/**
	 * Override the toString method in Object to print the node id.
	 * 
	 */
	@Override
	public String toString() {
		
		return "Node: " + this.getId();
	}
}
