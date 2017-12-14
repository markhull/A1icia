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

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.hulles.a1icia.graphviz.Graph;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.A1iciaUtils;

class TicketTrack {
	private final Ticket ticket;
	private final MutableValueGraph<RoomNode,DocumentEdge> valueGraph;
	private final Graph dotGraph;
	
	TicketTrack(Ticket ticket, MutableValueGraph<RoomNode,DocumentEdge> graph) {
		
		A1iciaUtils.checkNotNull(ticket);
		A1iciaUtils.checkNotNull(graph);
		this.ticket = ticket;
		this.valueGraph = graph;
		this.dotGraph = new Graph(ticket);
	}
	
	public Ticket getTicket() {
		
		return ticket;
	}

	MutableValueGraph<RoomNode,DocumentEdge> getMutableValueGraph() {
		
		return valueGraph;
	}
	
	public ValueGraph<RoomNode,DocumentEdge> getValueGraph() {
		ValueGraph<RoomNode,DocumentEdge> graphCopy;
		
		graphCopy = ValueGraphBuilder.from(valueGraph).allowsSelfLoops(true).build();
		return graphCopy;
	}
	
	public Graph getDotGraph() {
		
		return dotGraph;
	}
}
