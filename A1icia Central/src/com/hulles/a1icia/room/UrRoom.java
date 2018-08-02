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
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomDocument;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.room.document.WhatSememesAction;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * UrRoom is the base room (superclass) for all the Mind rooms. It contains the logic to send
 * and receive room documents.
 * 
 * @author hulles
 *
 */
public abstract class UrRoom extends AbstractIdleService {
	private final static Logger logger = Logger.getLogger("A1icia.UrRoom");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
//	private final static Level LOGLEVEL = Level.INFO;
	private final EventBus hall;
	private final ImmutableSet<SerialSememe> roomSememes;
	private final ListMultimap<Long, RoomResponse> responseCabinet;
	private final ConcurrentMap<Long, RoomRequest> requestCabinet;
	private final ExecutorService threadPool;
	boolean shuttingDownOnClose = false;
	
	public UrRoom(EventBus hall) {
		Set<SerialSememe> sememes;
		
		A1iciaUtils.checkNotNull(hall);
		this.hall = hall;
		hall.register(this);
		sememes = loadSememes();
		logger.log(LOGLEVEL, "SEMEMES: " + sememes.toString());
		roomSememes = ImmutableSet.copyOf(sememes);
		responseCabinet = MultimapBuilder.hashKeys().arrayListValues().build();
		requestCabinet = new ConcurrentHashMap<>();
//		threadPool = Executors.newFixedThreadPool(THREADCOUNT);
		threadPool = Executors.newCachedThreadPool();
		addDelayedShutdownHook(threadPool);
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
	 * Get all of the defined Rooms.
	 * 
	 * @return All of the Rooms
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
		
		A1iciaUtils.checkNotNull(request);
		logger.log(LOGLEVEL, "UrRoom: in sendRoomRequest, request type = " + request.getDocumentType());
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
		getHall().post(request);
	}
	
	/**
	 * Post a room response on the bus if it's ready. If not, it generates an error message and returns.
	 * 
	 * @param response The outgoing response
	 */
	public void sendRoomResponse(RoomResponse response) {
		
		A1iciaUtils.checkNotNull(response);
		logger.log(Level.FINE, "UrRoom: in sendRoomReponse");
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
		getHall().post(response);
	}
	
	/**
	 * Get the Guava asynchronous event bus (hall) for rooms
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
		
		A1iciaUtils.checkNotNull(document);
		logger.log(Level.FINER, "UrRoom for " + this.getThisRoom().getDisplayName() + 
				": in documentArrival");
		if (document instanceof RoomResponse) {
			logger.log(Level.FINER, "UrRoom for " + this.getThisRoom().getDisplayName() + 
					": document is RoomResponse");
			roomResponse = (RoomResponse) document;
			toRoom = roomResponse.getRespondToRoom();
			if (toRoom != getThisRoom()) {
				return;
			}
			responseTo = roomResponse.getResponseToRequestID();
			synchronized (responseCabinet) {
				responseCabinet.put(roomResponse.getResponseToRequestID(), roomResponse);
				collectedResponses = responseCabinet.get(responseTo);
				logger.log(Level.FINE, "put response from " + roomResponse.getFromRoom() + " into cabinet.");
			}
			if (collectedResponses.size() == Room.values().length) {
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
				logger.log(LOGLEVEL, "we have them all for " + responseTo + "; there are " +
						responseCabinet.keySet().size() + " documents remaining in the cabinet");
			}
		} else if (document instanceof RoomAnnouncement) {
			logger.log(Level.FINER, "UrRoom for " + this.getThisRoom().getDisplayName() + 
					": document is RoomAnnouncement");
			announcement = (RoomAnnouncement) document;
/*			switch (announcement.getDocumentType()) {
			//  TODO create a "processRoomAnnouncements" abstract method and pass these to it
                case TICKETOPENED:
                case TICKETCLOSED:
                case LOGGEDIN:
                case LOGGEDOUT:
                    break;
				default:
					A1iciaUtils.error("Unknown announcement type in documentArrival");
					break;
			}
*/			
			processRoomAnnouncement(announcement);
		} else if (document instanceof RoomRequest) {
			logger.log(Level.FINER, "UrRoom for " + this.getThisRoom().getDisplayName() + 
					": document is RoomRequest");
			roomRequest = (RoomRequest) document;
			// we trap WHAT_SPARKS before it gets to processRoomRequest
			sememePackages = roomRequest.getSememePackages(); 
			sememePackage = SememePackage.consume("what_sememes", sememePackages);
			if (sememePackage != null) {
				logger.log(LOGLEVEL, "UrRoom for " + this.getThisRoom().getDisplayName() + 
						": sememe is WHAT_SPARKS");
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
		
		A1iciaUtils.checkNotNull(request);
		// the contract states that we send a response no matter what
		response = new RoomResponse(request);
		response.setFromRoom(getThisRoom());
		logger.log(Level.FINE, "UrRoom for " + this.getThisRoom().getDisplayName() + 
				": evaluating sememes = " + updatedPkgs);
		for (SememePackage sememePkg : updatedPkgs) {
			if (roomSememes.contains(sememePkg.getSememe())) {
				logger.log(LOGLEVEL, "This should be a good sememe/room: " + 
						sememePkg.getName() + " / " + getThisRoom());
				pkg = createActionPackage(sememePkg, request);
				logger.log(LOGLEVEL, "Got actionpackage back from " + getThisRoom());
				if (pkg != null) {
					logger.log(LOGLEVEL, "Adding actionpackage for " + sememePkg.getName() + 
							" from " + getThisRoom());
					response.addActionPackage(pkg);
				}
			}
		}
		sendRoomResponse(response);
	}
	
	protected abstract Room getThisRoom();
	
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
		
		logger.log(LOGLEVEL, "UrRoom for " + this.getThisRoom().getDisplayName() + 
				": in returnSememes");
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
				if (!pool.awaitTermination(3, TimeUnit.SECONDS))
					System.err.println("URROOM -- Pool did not terminate");
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
