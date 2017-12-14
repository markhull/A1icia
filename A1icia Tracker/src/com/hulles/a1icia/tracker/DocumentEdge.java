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
package com.hulles.a1icia.tracker;

import com.hulles.a1icia.graphviz.Edge;
import com.hulles.a1icia.room.document.RoomDocument;
import com.hulles.a1icia.room.document.RoomDocumentType;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * An edge for the ticket ValueGraph that represents a document moving from one room (node) to 
 * another.
 * 
 * @author hulles
 *
 */
public class DocumentEdge {
	private final Long documentKey;
	private final RoomDocumentType documentType;
	private final Edge edge;
	private final Ticket ticket;
	
	public DocumentEdge(Ticket ticket, RoomNode fromNode, RoomDocument document, RoomNode toNode) {
		
		A1iciaUtils.checkNotNull(ticket);
		A1iciaUtils.checkNotNull(fromNode);
		A1iciaUtils.checkNotNull(document);
		A1iciaUtils.checkNotNull(toNode);
		this.ticket = ticket;
		this.documentKey = document.getDocumentID();
		this.documentType = document.getDocumentType();
		this.edge = new Edge(documentKey, fromNode.getDotNode(), toNode.getDotNode());
	}
	
	public Ticket getTicket() {
		
		return ticket;
	}

	public Long getDocumentKey() {
		
		return documentKey;
	}

	public Edge getDotEdge() {
		
		return edge;
	}

	public RoomDocumentType getDocumentType() {
	
		return documentType;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((documentKey == null) ? 0 : documentKey.hashCode());
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
		if (!(obj instanceof DocumentEdge)) {
			return false;
		}
		DocumentEdge other = (DocumentEdge) obj;
		if (documentKey == null) {
			if (other.documentKey != null) {
				return false;
			}
		} else if (!documentKey.equals(other.documentKey)) {
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
		
		return "DocumentEdge: " + documentType.toString() + ": " + documentKey.toString();
	}
	
}
