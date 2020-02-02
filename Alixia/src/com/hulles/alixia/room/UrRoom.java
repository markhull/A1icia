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
package com.hulles.alixia.room;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractIdleService;
import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.house.ClientDialogResponse;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomDocument;
import com.hulles.alixia.room.document.RoomDocumentType;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.room.document.WhatSememesAction;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.alixia.ticket.Ticket;

/**
 * UrRoom is the base room (superclass) for all the Mind rooms. It contains the logic to send
 * and receive room documents.
 * 
 * @author hulles
 *
 */
public abstract class UrRoom extends AbstractIdleService {
	private final static Logger LOGGER = LoggerFactory.getLogger(UrRoom.class);
	private EventBus hall;
    private static SetMultimap<SerialSememe, Room> sememeRooms;
    private static ImmutableSet<Room> implementedRooms;
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
	protected Set<SerialSememe> getRoomSememes() {
		
		return new HashSet<>(roomSememes);
	}
	
	/**
	 * Get all of the <b>defined</b> Rooms. The <b>implemented</b> rooms are a different set.
	 * 
	 * @return All of the defined Rooms
	 */
	protected static Set<Room> getAllRooms() {
		Set<Room> rooms;
		
		rooms = EnumSet.allOf(Room.class);
		return rooms;
	}
	
	/**
	 * Get all of the <b>implemented</b> Rooms. The <b>defined</b> rooms are a different set.
	 * 
	 * @return All of the defined Rooms
	 */
	protected static Set<Room> getImplementedRooms() {
		
		return implementedRooms;
	}
	
	/**
	 * Get the count of implemented rooms.
	 * 
	 * @return The count of rooms
	 */
	protected static Integer getImplementedRoomCount() {
	
		return implementedRooms.size();
	}
	
	/**
	 * Return if a given room is implemented in this run.
	 * 
	 * @param room The room to query
	 * @return True if the room is implemented
	 */
	protected static Boolean isImplemented(Room room) {
		
		SharedUtils.checkNotNull(room);
		return implementedRooms.contains(room);
	}

    /**
     * Return a set of rooms that can process the sememe.
     *
     * @param sememe The sememe in question
     * @return A list of rooms that have advertised they can process the sememe
     */
	protected static Set<Room> getRoomsForSememe(SerialSememe sememe) {

        return sememeRooms.get(sememe);
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
		LOGGER.debug("UrRoom: in sendRoomRequest, request type = {}", docType);
		if (!request.documentIsReady()) {
			LOGGER.error("Document is not ready in UrRoom.sendRoomRequest",
					"Document is from {}, type is {}", request.getFromRoom(), request.getDocumentType());
			return;
		}
		if (request.getFromRoom() != getThisRoom()) {
			LOGGER.error("Someone is trying to forge a document! Document is really from {}, forged room is {}", getThisRoom(), request.getFromRoom());
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
	protected void sendRoomResponse(RoomResponse response) {
		
		SharedUtils.checkNotNull(response);
		LOGGER.debug("UrRoom: in sendRoomReponse");
		if (!response.documentIsReady()) {
			LOGGER.error("Document is not ready in UrRoom.sendRoomResponse, document is from {}, type = {01}", response.getFromRoom(), response.getDocumentType());
			return;
		}
		if (response.getFromRoom() != getThisRoom()) {
            LOGGER.error("Someone is trying to forge a document! Document is really from {}, forged room is {}", getThisRoom(), response.getFromRoom());
			return;
		}
		hall.post(response);
	}

	/**
	 * Set the list of implemented rooms discovered by the Controller
	 * 
	 * @param rooms The set of rooms
	 */
	public static void setImplementedRooms(Set<Room> rooms) {
		
		SharedUtils.checkNotNull(rooms);
		implementedRooms = ImmutableSet.copyOf(rooms);
	}

	/**
	 * Set the MultiMap set of sememes and the rooms that implement them as discovered by the Controller
	 * 
	 * @param rooms The set of rooms
	 */
	public static void setRoomSememes(SetMultimap<SerialSememe, Room> roomSememes) {
		
		SharedUtils.checkNotNull(roomSememes);
		sememeRooms = roomSememes;
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
		LOGGER.debug("UrRoom for {}: in documentArrival", thisRoomName);
		if (document instanceof RoomResponse) {
			LOGGER.debug("UrRoom for {}: document is RoomResponse", thisRoomName);
			roomResponse = (RoomResponse) document;
			toRoom = roomResponse.getRespondToRoom();
			if (toRoom != getThisRoom()) {
				return;
			}
			responseTo = roomResponse.getResponseToRequestID();
			synchronized (responseCabinet) {
				responseCabinet.put(responseTo, roomResponse);
				collectedResponses = responseCabinet.get(responseTo);
                fromName = roomResponse.getFromRoom().getDisplayName();
				LOGGER.debug("put response from {} into cabinet.", fromName);
			}
			if (collectedResponses.size() == getImplementedRoomCount()) {
				// we have them all, unless some rascal sent more than one back....
				// also note that if something blows up the (other) responses will stay
				// in the cabinet forever -- FIXME
				roomRequest = requestCabinet.get(responseTo);
				if (roomRequest == null) {
					LOGGER.error("UrRoom: couldn't find request for response");
				}
				processRoomResponses(roomRequest, collectedResponses);
				synchronized (responseCabinet) {
					responseCabinet.removeAll(responseTo);
				}
				requestCabinet.remove(responseTo);
                cabinetSize = responseCabinet.keySet().size();
				LOGGER.debug("We have them all for {}; there are {} documents remaining in the cabinet", responseTo, cabinetSize);
			}
		} else if (document instanceof RoomAnnouncement) {
			LOGGER.debug("UrRoom for {}: document is RoomAnnouncement", thisRoomName);
			announcement = (RoomAnnouncement) document;
			processRoomAnnouncement(announcement);
		} else if (document instanceof RoomRequest) {
			LOGGER.debug("UrRoom for {}: document is RoomRequest", thisRoomName);
			roomRequest = (RoomRequest) document;
			// we trap WHAT_SEMEMES before it gets to processRoomRequest
			sememePackages = roomRequest.getSememePackages(); 
			sememePackage = SememePackage.consume("what_sememes", sememePackages);
			if (sememePackage != null) {
				LOGGER.debug("UrRoom for {}: sememe is WHAT_SEMEMES", thisRoomName);
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
            if (document == null) {
                LOGGER.error("Null document in documentArrival");
            } else {
    			LOGGER.error("Unknown document type in documentArrival = {}", document.getDocumentType());
            }
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
		LOGGER.debug("UrRoom for {}: evaluating sememes = {}", thisRoomName, updatedPkgs);
		for (SememePackage sememePkg : updatedPkgs) {
			if (roomSememes.contains(sememePkg.getSememe())) {
				LOGGER.debug("This should be a good sememe/room: {} / {}", sememePkg.getName(), thisRoomName);
				pkg = createActionPackage(sememePkg, request);
				LOGGER.debug( "Got actionpackage back from {}", thisRoomName);
				if (pkg != null) {
					LOGGER.debug("Adding actionpackage for {} from {}", sememePkg.getName(), thisRoomName);
					response.addActionPackage(pkg);
				}
			}
		}
		sendRoomResponse(response);
	}
		
    /**
     * Send a push notification to an AlixianID <i>via</i> a RoomRequest. Alixia advertises 
     * and handles the "indie_response" sememe (TODO needs a new name) and forwards
     * it to the specified AlixianID (station), so the "request" part of the title
     * is really a request to Alixia to forward the wrapped ClientDialogResponse.
     * 
     * @param response 
     */
	protected final void postPushRequest(ClientDialogResponse response) {
		Ticket ticket;
		RoomRequest roomRequest;

		SharedUtils.checkNotNull(response);
		ticket = Ticket.createNewTicket(getHall(), getThisRoom());
		ticket.setFromAlixianID(AlixiaConstants.getAlixiaAlixianID());
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
	 * Respond to a WHAT_SEMEMES room request.
	 * 
	 * @param request The incoming request
	 * @param whatSememes The sememe initiating the response
	 */
	private void returnSememes(RoomRequest request, SememePackage whatSememes) {
		RoomResponse sememesResponse;
		WhatSememesAction sememesAction;
		ActionPackage pkg;
		
		LOGGER.debug("UrRoom for {}: in returnSememes", this.getThisRoom().getDisplayName());
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
		
		LOGGER.info("URROOM -- Shutting down room");
		pool.shutdown();
		try {
			if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
				pool.shutdownNow();
				if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
                    LOGGER.error("URROOM -- Pool did not terminate");
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
	    		LOGGER.info("URROOM -- Orderly shutdown, hook not engaged");
	    	} else {
		    	LOGGER.info("URROOM -- Exceptional shutdown, hook engaged");
				shutdownAndAwaitTermination(pool);
	    	}
	    }
	}

	@Override
	protected final void startUp() {
		Set<SerialSememe> sememes;
		
        // we can't function at all without a bus and a knowledge of how many rooms are implemented (vs. defined)
        if (this.hall == null) {
            throw new AlixiaException("Starting room "  + getThisRoom().getDisplayName() + " with null event bus");
        }
        if (getImplementedRoomCount() == null) {
            throw new AlixiaException("Starting room "  + getThisRoom().getDisplayName() + " with null room count");
        }
		sememes = loadSememes();
		LOGGER.debug("SEMEMES: {}", sememes.toString());
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
