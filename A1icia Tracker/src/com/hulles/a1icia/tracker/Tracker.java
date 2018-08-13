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

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.util.concurrent.AbstractIdleService;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.ApplicationKeys;
import com.hulles.a1icia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.graphviz.Graph;
import com.hulles.a1icia.graphviz.GraphViz;
import com.hulles.a1icia.jebus.JebusBible;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.jebus.JebusPool;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomDocument;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.A1iciaUtils;

import redis.clients.jedis.Jedis;

/**
 * Tracker listens to everything on the bus and keeps track of what's happening. Note that
 * it is not a subclass of UrRoom.
 * <p>
 * NOTE: Tracker is now turned off until we figure out a better use for it.
 * 
 * @author hulles
 *
 */
public final class Tracker extends AbstractIdleService {
	private final static Logger LOGGER = Logger.getLogger("A1icia.Tracker");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final static int GRAPHTTL = 60 * 60 * 24; // graphs are stored in Jebus for 24 hours
	private final static boolean SAVEIMAGE = false;
	private final static boolean SAVEDOTGRAPH = false;
	private final JebusPool jebusPool;
	private static Tracker instance = null;
	private final EventBus bus;
	private final ConcurrentMap<Ticket,TicketTrack> ticketMap;
	private final String imagePath;
	private ExecutorService executor;
	
	public Tracker(EventBus bus) {
		ApplicationKeys appKeys;
		
		A1iciaUtils.checkNotNull(bus);
		this.bus = bus;
		ticketMap = new ConcurrentHashMap<>();
		jebusPool = JebusHub.getJebusLocal();
		appKeys = ApplicationKeys.getInstance();
		imagePath = appKeys.getKey(ApplicationKey.TRACKERPATH);
		executor = Executors.newCachedThreadPool();
	}
	
	@SuppressWarnings({ "static-method", "unused" })
	@Subscribe public void track(RoomDocument document) {
		RoomAnnouncement roomAnnouncement;
		RoomRequest roomRequest;
		RoomResponse roomResponse;
		
		A1iciaUtils.checkNotNull(document);
		if (document instanceof RoomAnnouncement) {
			// we don't track announcements
			roomAnnouncement = (RoomAnnouncement) document;
			switch (roomAnnouncement.getDocumentType()) {
                case TICKETOPENED:
//                    initiateTicket(roomAnnouncement.getTicket());
                    break;
                case TICKETCLOSED:
//                    closeTicket(roomAnnouncement.getTicket());
                    break;
				default:
					A1iciaUtils.error("Unknown announcement type in documentArrival");
					break;
			}
		// we don't really have to recast these, might take it out later if we don't need it
		} else if (document instanceof RoomResponse) {
			roomResponse = (RoomResponse) document;
//			trackDocument(roomResponse);
		} else if (document instanceof RoomRequest) {
			roomRequest = (RoomRequest) document;
//			trackDocument(roomRequest);
		} else {
			A1iciaUtils.error("Unknown document type in Tracker = " + document.getDocumentType());
		}
	}

	public static Tracker getInstance() {
	
		if (instance == null) {
			throw new A1iciaException();
		}
		return instance;
	}
	
	public void initiateTicket(Ticket ticket) {
		MutableValueGraph<RoomNode,DocumentEdge> graph;
		TicketTrack ticketGraph;
		
		A1iciaUtils.nullsOkay(ticket);
		if (ticket == null) {
			return;
		}
		graph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
		ticketGraph = new TicketTrack(ticket, graph);
		ticketMap.put(ticket, ticketGraph);
		LOGGER.log(LOGLEVEL, "TRACKER: added ticket, ticketmap has " + ticketMap.size() + " entries.");
	}
	
	public void closeTicket(Ticket ticket) {
		Graph dotGraph;
		TicketTrack ticketTrack;
        
		A1iciaUtils.nullsOkay(ticket);
		if (ticket == null) {
			return;
		}
		ticketTrack = ticketMap.get(ticket);
		dotGraph = ticketTrack.getDotGraph();
		// TODO close ticket even if the ticket isn't punched for some reason in ClientPortal;
        //    that is, even if the ticket doesn't complete normally for some reason and there
        //    is no close event
		if (SAVEDOTGRAPH) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
			 		String hashKey;
					hashKey = JebusBible.getA1iciaDocumentHashKey(jebusPool, ticket.toString());
					try (Jedis jebus = jebusPool.getResource()) {
						jebus.set(hashKey, dotGraph.genGraphDotString());
						jebus.expire(hashKey, GRAPHTTL);
						LOGGER.log(LOGLEVEL, "Tracker: wrote graph dot string to {0}", hashKey);
					}
				}
			});
		}
		if (SAVEIMAGE) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					GraphViz graphViz;
			        String imageFileName;

			        imageFileName = imagePath + "/" + ticket.toString();
					graphViz = new GraphViz();
					graphViz.writeGraphToImageFile(dotGraph, "png", "100", imageFileName);
				}
			});
		}
		// update database with graph as well
		ticketMap.remove(ticket);
		LOGGER.log(LOGLEVEL, "TRACKER: removed ticket, ticketmap has " + ticketMap.size() + " entries.");
	}
	
	public Set<Room> whatsComingToMe(Ticket ticket, Room me) {
		TicketTrack ticketTrack;
		Set<Room> incomingRooms;
		ValueGraph<RoomNode,DocumentEdge> valueGraph;
		int incoming;
		RoomNode meNode;
		Set<RoomNode> predecessors;
		Set<RoomNode> nodes;
		
		A1iciaUtils.checkNotNull(ticket);
		A1iciaUtils.checkNotNull(me);
		LOGGER.log(LOGLEVEL, "TRACKER: {0} is inquiring about what's coming for ticket {1}", 
                new Object[]{me.getDisplayName(), ticket});
		ticketTrack = ticketMap.get(ticket);
		if (ticketTrack == null) {
			A1iciaUtils.error("TRACKER: Room asked whatsComingToMe on a bad or stale ticket");
			return null;
		}
		incomingRooms = EnumSet.noneOf(Room.class);
		valueGraph = ticketTrack.getValueGraph();
		meNode = new RoomNode(me);
		nodes = valueGraph.nodes();
		if (!nodes.contains(meNode)) {
			A1iciaUtils.error("TRACKER: In whatsComingToMe, node not present in graph");
			return null;
		}
		incoming = valueGraph.inDegree(meNode);
		LOGGER.log(LOGLEVEL, "TRACKER: {0} documents are tracking in", incoming);
		predecessors = valueGraph.predecessors(meNode);
		for (RoomNode node : predecessors) {
			incomingRooms.add(node.getRoom());
			LOGGER.log(LOGLEVEL, "TRACKER: {0} has a document tracking in", node);
		}
		return incomingRooms;
	}
	
	@SuppressWarnings("unused")
	private void trackDocument(RoomDocument document) {
		MutableValueGraph<RoomNode,DocumentEdge> valueGraph;
		Graph dotGraph;
		TicketTrack ticketTrack;
		Ticket ticket;
		RoomNode fromNode;
		RoomNode toNode;
		DocumentEdge edge;
        RoomResponse response;
		
		A1iciaUtils.checkNotNull(document);
		ticket = document.getTicket();
		if (ticket == null) {
			A1iciaUtils.error("Tracker: trying to track null ticket for document #" + 
					document.getDocumentID());
			return;
		}
		ticketTrack = ticketMap.get(ticket);
		if (ticketTrack == null) {
			A1iciaUtils.error("Tracker: ticket but no map key for document #" + 
					document.getDocumentID());
			return;
		}
		valueGraph = ticketTrack.getMutableValueGraph();
		if (valueGraph == null) {
			A1iciaUtils.error("Tracker: ticket but no ValueGraph for document #" + 
					document.getDocumentID());
			return;
		}
		dotGraph = ticketTrack.getDotGraph();
		if (dotGraph == null) {
			A1iciaUtils.error("Tracker: ticket but no dot graph for document #" + 
					document.getDocumentID());
			return;
		}
        if (document instanceof RoomResponse) {
            response = (RoomResponse) document;
            if (!response.hasNoResponse()) {
            	// show the from room
        		fromNode = new RoomNode(document.getFromRoom());
        		valueGraph.addNode(fromNode);
        		dotGraph.addNode(fromNode.getDotNode());
        		// show the to room
        		toNode = new RoomNode(response.getRespondToRoom());
                valueGraph.addNode(toNode);
                dotGraph.addNode(toNode.getDotNode());
                // show the request
                edge = new DocumentEdge(ticket, toNode, document, fromNode);
                valueGraph.putEdgeValue(toNode, fromNode, edge);
                dotGraph.addEdge(edge.getDotEdge());
                // show the response
                edge = new DocumentEdge(ticket, fromNode, document, toNode);
                valueGraph.putEdgeValue(fromNode, toNode, edge);
                dotGraph.addEdge(edge.getDotEdge());
            }
        }
	}
	
	@Override
	protected void shutDown() {
		
		bus.unregister(this);
		instance = null;
		if (executor != null) {
			try {
				LOGGER.log(LOGLEVEL, "attempting to shutdown executor");
			    executor.shutdown();
			    executor.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
				LOGGER.log(LOGLEVEL, "tasks interrupted");
			}
			finally {
			    if (!executor.isTerminated()) {
			    	LOGGER.log(LOGLEVEL, "cancelling non-finished tasks");
			    }
			    executor.shutdownNow();
			    LOGGER.log(LOGLEVEL, "shutdown finished");
			}
		}
		executor = null;
	}

	@Override
	protected void startUp() {

		bus.register(this);
	}
}
