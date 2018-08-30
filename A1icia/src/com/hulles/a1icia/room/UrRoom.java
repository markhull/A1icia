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
package com.hulles.a1icia.room;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractIdleService;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.A1iciaException;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.api.tools.A1iciaUtils;
import com.hulles.a1icia.house.ClientDialogResponse;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomDocument;
import com.hulles.a1icia.room.document.RoomDocumentType;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.room.document.WhatSememesAction;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.ticket.Ticket;

/**
 * UrRoom is the base room (superclass) for all the Mind rooms. It contains the logic to send
 * and receive room documents.
 * 
 * @author hulles
 *
 */
public abstract class UrRoom extends AbstractIdleService {
	private final static Logger LOGGER = Logger.getLogger("A1icia.UrRoom");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
//	private final static Level LOGLEVEL = Level.INFO;
	private EventBus hall;
    private Integer roomCount;
	private ImmutableSet<SerialSememe> roomSememes;
	private final ListMultimap<Long, RoomResponse> responseCabinet;
	private final ConcurrentMap<Long, RoomRequest> requestCabinet;
	private final ExecutorService threadPool;
	boolean shuttingDownOnClose = false;
	
	public UrRoom() {
								
		responseCabinet = MultimapBuilder.hashKeys().arrayListValues().build();
		requestCabinet = new ConcurrentHashMap<>();
		threadPool = Executors.newCachedThreadPool();
		addDelayedShutdownHook(threadPool);
	}
	public UrRoom(EventBus hallBus) {
        this();
        
        SharedUtils.checkNotNull(hallBus);
        setHall(hallBus);
    }
    
	/**
	 * Returns a new MUTABLE COPY of roomSememes
	 * 
	 * @return The copy of the set
	 */
	public Set<SerialSememe> getRoomSememes() {
		
		return new HashSet<>(roomSememes);
	}
	
	/**
	 * Get all of the <b>defined</b> Rooms. The <b>implemented</b> rooms are a different set.
	 * 
	 * @return All of the defined Rooms
	 */
	public static Set<Room> getAllRooms() {
		Set<Room> rooms;
		
		rooms = EnumSet.allOf(Room.class);
		return rooms;
	}
	
	/**
	 * Post a room request on the bus if it's ready. If not, it generates an error message and returns.
	 * 
	 * @param request The outgoing request
	 */
	public void sendRoomRequest(RoomRequest request) {
		RoomDocumentType docType;
        
		SharedUtils.checkNotNull(request);
		docType = request.getDocumentType();
		LOGGER.log(LOGLEVEL, "UrRoom: in sendRoomRequest, request type = {0}", docType);
		if (!request.documentIsReady()) {
			A1iciaUtils.error("Document is not ready in UrRoom.sendRoomRequest",
					"Document is from " + request.getFromRoom() +
					", type is " + request.getDocumentType());
			return;
		}
		if (request.getFromRoom() != getThisRoom()) {
			A1iciaUtils.error("Someone is trying to forge a document",
					"Document is really from " + getThisRoom() +
					", forged room is " + request.getFromRoom());
			return;
		}
		requestCabinet.put(request.getDocumentID(), request);
		hall.post(request);
	}
	
	/**
	 * Post a room response on the bus if it's ready. If not, it generates an error message and returns.
	 * 
	 * @param response The outgoing response
	 */
	public void sendRoomResponse(RoomResponse response) {
		
		SharedUtils.checkNotNull(response);
		LOGGER.log(Level.FINE, "UrRoom: in sendRoomReponse");
		if (!response.documentIsReady()) {
			A1iciaUtils.error("Document is not ready in UrRoom.sendRoomResponse",
					"Document is from " + response.getFromRoom() + ", type = " + 
					response.getDocumentType());
			return;
		}
		if (response.getFromRoom() != getThisRoom()) {
			A1iciaUtils.error("Someone is trying to forge a document",
					"Document is really from " + getThisRoom() +
					", forged room is " + response.getFromRoom());
			return;
		}
		hall.post(response);
	}

    public final void setRoomCount(Integer count) {
        
        SharedUtils.checkNotNull(count);
        this.roomCount = count;
    }
    
	/**
	 * Set the Guava asynchronous event bus (hall) for the room
	 * 
	 * @param hallBus The bus
	 */
    public final void setHall(EventBus hallBus) {
        
        this.hall = hallBus;
		hall.register(this);
    }
    
    
	/**
	 * Get the Guava asynchronous event bus (hall) for the room
	 * 
	 * @return The bus
	 */
	protected EventBus getHall() {
		return hall;
	}
	
	/**
	 * A RoomDocument has arrived on the bus, so we figure out what kind it is and what 
	 * to do with it (if anything).
	 * 
     * @param document
	 */
	@Subscribe public void documentArrival(RoomDocument document) {
		RoomRequest roomRequest;
		RoomAnnouncement announcement;
		RoomResponse roomResponse;
		List<SememePackage> sememePackages;
		SememePackage sememePackage;
		List<RoomResponse> collectedResponses;
		Long responseTo;
		Room toRoom;
		String thisRoomName;
        String fromName;
        int cabinetSize;
        
		SharedUtils.checkNotNull(document);
        thisRoomName = this.getThisRoom().getDisplayName();
		LOGGER.log(Level.FINER, "UrRoom for {0}: in documentArrival", thisRoomName);
		if (document instanceof RoomResponse) {
			LOGGER.log(Level.FINER, "UrRoom for {0}: document is RoomResponse", thisRoomName);
			roomResponse = (RoomResponse) document;
			toRoom = roomResponse.getRespondToRoom();
			if (toRoom != getThisRoom()) {
				return;
			}
			responseTo = roomResponse.getResponseToRequestID();
			synchronized (responseCabinet) {
				responseCabinet.put(roomResponse.getResponseToRequestID(), roomResponse);
				collectedResponses = responseCabinet.get(responseTo);
                fromName = roomResponse.getFromRoom().getDisplayName();
				LOGGER.log(Level.FINE, "put response from {0} into cabinet.", fromName);
			}
			if (collectedResponses.size() == roomCount) {
				// we have them all, unless some rascal sent more than one back....
				// also note that if something blows up the (other) responses will stay
				// in the cabinet forever -- FIXME
				roomRequest = requestCabinet.get(responseTo);
				if (roomRequest == null) {
					A1iciaUtils.error("UrRoom: couldn't find request for response");
				}
				processRoomResponses(roomRequest, collectedResponses);
				synchronized (responseCabinet) {
					responseCabinet.removeAll(responseTo);
				}
				requestCabinet.remove(responseTo);
                cabinetSize = responseCabinet.keySet().size();
				LOGGER.log(LOGLEVEL, "we have them all for {0}; there are {1} documents remaining in the cabinet",
                        new Object[]{responseTo, cabinetSize});
			}
		} else if (document instanceof RoomAnnouncement) {
			LOGGER.log(Level.FINER, "UrRoom for {0}: document is RoomAnnouncement", thisRoomName);
			announcement = (RoomAnnouncement) document;
			processRoomAnnouncement(announcement);
		} else if (document instanceof RoomRequest) {
			LOGGER.log(Level.FINER, "UrRoom for {0}: document is RoomRequest", thisRoomName);
			roomRequest = (RoomRequest) document;
			// we trap WHAT_SPARKS before it gets to processRoomRequest
			sememePackages = roomRequest.getSememePackages(); 
			sememePackage = SememePackage.consume("what_sememes", sememePackages);
			if (sememePackage != null) {
				LOGGER.log(LOGLEVEL, "UrRoom for {0}: sememe is WHAT_SPARKS", thisRoomName);
				returnSememes(roomRequest, sememePackage);
			}
			if (!sememePackages.isEmpty()) {
				threadPool.submit(new Runnable() {
					@Override
					public void run() {
						processRoomRequest(sememePackages, roomRequest);
					}
				});
			}
		} else {
			A1iciaUtils.error("Unknown document type in documentArrival = " + document.getDocumentType());
		}
	}
	
	/**
	 * Process the updated set of sememe packages from the room request.
	 * 
	 * @param updatedPkgs
	 * @param request
	 */
	protected final void processRoomRequest(List<SememePackage> updatedPkgs, RoomRequest request) {
		RoomResponse response;
		ActionPackage pkg;
		String thisRoomName;
        
		SharedUtils.checkNotNull(request);
		// the contract states that we send a response no matter what
        thisRoomName = this.getThisRoom().getDisplayName();
		response = new RoomResponse(request);
		response.setFromRoom(getThisRoom());
		LOGGER.log(Level.FINE, "UrRoom for {0}: evaluating sememes = {1}", new Object[]{thisRoomName, updatedPkgs});
		for (SememePackage sememePkg : updatedPkgs) {
			if (roomSememes.contains(sememePkg.getSememe())) {
				LOGGER.log(LOGLEVEL, "This should be a good sememe/room: {0} / {1}", new Object[]{sememePkg.getName(), thisRoomName});
				pkg = createActionPackage(sememePkg, request);
				LOGGER.log(LOGLEVEL, "Got actionpackage back from {0}", thisRoomName);
				if (pkg != null) {
					LOGGER.log(LOGLEVEL, "Adding actionpackage for {0} from {1}", new Object[]{sememePkg.getName(), thisRoomName});
					response.addActionPackage(pkg);
				}
			}
		}
		sendRoomResponse(response);
	}
		
    /**
     * Send a push notification to an A1icianID <i>via</i> a RoomRequest. A1icia advertises 
     * and handles the "indie_response" sememe (TODO needs a new name) and forwards
     * it to the specified A1icianID (station), so the "request" part of the title
     * is really a request to A1icia to forward the wrapped ClientDialogResponse.
     * 
     * @param response 
     */
	protected final void postPushRequest(ClientDialogResponse response) {
		Ticket ticket;
		RoomRequest roomRequest;

		SharedUtils.checkNotNull(response);
		ticket = Ticket.createNewTicket(getHall(), getThisRoom());
		ticket.setFromA1icianID(A1iciaConstants.getA1iciaA1icianID());
		roomRequest = new RoomRequest(ticket);
		roomRequest.setFromRoom(getThisRoom());
		roomRequest.setSememePackages(SememePackage.getSingletonDefault("indie_response"));
		roomRequest.setMessage("Indie client response");
		roomRequest.setRoomObject(response);
		sendRoomRequest(roomRequest);
	}
    
	public abstract Room getThisRoom();
	
	protected abstract void roomStartup();
	
	protected abstract void roomShutdown();
	
	protected abstract ActionPackage createActionPackage(SememePackage sememe, RoomRequest request);
	
	protected abstract void processRoomResponses(RoomRequest request, List<RoomResponse> response);
	
	protected abstract void processRoomAnnouncement(RoomAnnouncement announcement);
	
	protected abstract Set<SerialSememe> loadSememes();
	
	/**
	 * Respond to a WHAT_SPARKS room request.
	 * 
	 * @param request The incoming request
	 * @param whatSememes The sememe initiating the response
	 */
	private void returnSememes(RoomRequest request, SememePackage whatSememes) {
		RoomResponse sememesResponse;
		WhatSememesAction sememesAction;
		ActionPackage pkg;
		
		LOGGER.log(LOGLEVEL, "UrRoom for {0}: in returnSememes", this.getThisRoom().getDisplayName());
		sememesAction = new WhatSememesAction();
		sememesAction.setSememes(roomSememes);
		pkg = new ActionPackage(whatSememes);
		pkg.setActionObject(sememesAction);
		sememesResponse = new RoomResponse(request);
		sememesResponse.addActionPackage(pkg);
		sememesResponse.setFromRoom(getThisRoom());
		sendRoomResponse(sememesResponse);
	}

	static void shutdownAndAwaitTermination(ExecutorService pool) {
		
		System.out.println("URROOM -- Shutting down room");
		pool.shutdown();
		try {
			if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
				pool.shutdownNow();
				if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
                    System.err.println("URROOM -- Pool did not terminate");
                }
			}
		} catch (InterruptedException ie) {
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
	
	private void addDelayedShutdownHook(final ExecutorService pool) {
		Runnable shutdownHook;
		Thread hook;
		
		shutdownHook = new ShutdownHook(pool);
		hook = new Thread(shutdownHook);
		Runtime.getRuntime().addShutdownHook(hook);
	}
	
	private class ShutdownHook implements Runnable {
		ExecutorService pool;
		
		ShutdownHook(ExecutorService pool) {
			this.pool = pool;
		}
		
	    @Override
		public void run() {
	    	
	    	if (shuttingDownOnClose) {
	    		System.out.println("URROOM -- Orderly shutdown, hook not engaged");
	    	} else {
		    	System.out.println("URROOM -- Exceptional shutdown, hook engaged");
				shutdownAndAwaitTermination(pool);
	    	}
	    }
	}

	@Override
	protected final void startUp() {
		Set<SerialSememe> sememes;
		
        // we can't function at all without a bus and a knowledge of how many rooms are implemented (vs. defined)
        if (this.hall == null) {
            throw new A1iciaException("Starting room "  + getThisRoom().getDisplayName() + " with null event bus");
        }
        if (this.roomCount == null) {
            throw new A1iciaException("Starting room "  + getThisRoom().getDisplayName() + " with null room count");
        }
		sememes = loadSememes();
		LOGGER.log(LOGLEVEL, "SEMEMES: {0}", sememes.toString());
		roomSememes = ImmutableSet.copyOf(sememes);
		
		roomStartup();
	}
	
	@Override
	protected final void shutDown() {
		
		roomShutdown();
		hall.unregister(this);
		shuttingDownOnClose = true;
		shutdownAndAwaitTermination(threadPool);
	}
}
