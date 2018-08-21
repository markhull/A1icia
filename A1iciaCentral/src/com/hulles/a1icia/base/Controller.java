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
package com.hulles.a1icia.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.hulles.a1icia.A1icia;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.cayenne.A1iciaApplication;
import com.hulles.a1icia.cayenne.Sememe;
import com.hulles.a1icia.room.BusMonitor;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.room.document.WhatSememesAction;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * The Controller is a little simpler than its name would suggest -- it basically just 
 * starts and stops all the in-house rooms. However, if we ever need dependency 
 * injection or some crazy thing like that this is the guy to deliver room service, so to speak.
 * 
 * @author hulles
 *
 */
public final class Controller extends AbstractIdleService {
	final static Logger LOGGER = Logger.getLogger("A1icia.Controller");
	final static Level LOGLEVEL = LOGGER.getParent().getLevel();
//	private final static int THREADCOUNT = 12;
	private final AsyncEventBus hallBus;
	private final ExecutorService busPool;
	boolean shuttingDownOnClose = false;
	final SetMultimap<SerialSememe, Room> sememeRooms;
	private ControllerRoom controllerRoom;
	private ServiceManager serviceManager;
	private final A1icia a1iciaInstance;
	private static Controller controllerInstance = null;
	static Set<SerialSememe> allSememes;
	
	static {
		
		allSememes = Sememe.getAllSememes();
		SerialSememe.setSememes(allSememes);
	}
	
	public Controller(A1icia a1icia) {
		
		SharedUtils.checkNotNull(a1icia);
		if (controllerInstance != null) {
			throw new A1iciaException("Controller: attempting multiple instances");
		}
		this.a1iciaInstance = a1icia;
		busPool = Executors.newCachedThreadPool();
		addDelayedShutdownHook(busPool);
		A1iciaApplication.setJdbcLogging(false); 
		hallBus = new AsyncEventBus("MindBus", busPool);
		sememeRooms = MultimapBuilder.hashKeys().enumSetValues(Room.class).build();
		controllerInstance = this;
	}
	
	public synchronized static Controller getInstance() {
	
		return controllerInstance;
	}
	
	/**
	 * Get the Guava asynchronous event bus (hall)
	 * 
	 * @return The bus
	 */
	public EventBus getHall() {
		return hallBus;
	}
	
	/**
	 * Return true if the ServiceManager says everything is up and running.
	 * 
	 * @return True if ready
	 */
	public static boolean isReady() {
		
		if (controllerInstance.serviceManager == null) {
			return false;
		}
		return controllerInstance.serviceManager.isHealthy();
	}
	
	/**
	 * Instantiate all the rooms and add them to the list of services the ServiceManager
	 * will start.
	 * 
	 * @param hall The room bus
	 * @return A list of services to start
	 */
	protected List<Service> loadServices(EventBus hall) {
		List<Service> services;
		Iterable<UrRoom> rooms;
		
		SharedUtils.checkNotNull(hall);
        rooms = ServiceLoader.load(UrRoom.class);
		services = new ArrayList<>(40);
		controllerRoom = new ControllerRoom();
		services.add(new BusMonitor());
		services.add(a1iciaInstance.new A1iciaRoom());
		for (UrRoom room : rooms) {
			services.add(room);
		}
		return services;
	}

	/**
	 * Return a set of rooms that can process the sememe.
	 * 
	 * @param sememe The sememe in question
	 * @return A list of rooms that have advertised they can process the sememe
	 */
	public Set<Room> getRoomsForSememe(SerialSememe sememe) {
		
		return sememeRooms.get(sememe);
	}
	
	/**
	 * Shut down the hall (room) bus.
	 * 
	 * @param pool
	 */
	static void shutdownAndAwaitTermination(ExecutorService pool) {
		
		System.out.println("CONTROLLER -- Shutting down Controller");
		pool.shutdown();
		try {
			if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
				pool.shutdownNow();
				if (!pool.awaitTermination(10, TimeUnit.SECONDS))
					System.err.println("CONTROLLER -- Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Add a shutdown hook to close down the executor pool.
	 * 
	 * @param pool
	 */
	private void addDelayedShutdownHook(final ExecutorService pool) {
		Runnable shutdownHook;
		Thread hook;
		
		shutdownHook = new ShutdownHook(pool);
		hook = new Thread(shutdownHook);
		Runtime.getRuntime().addShutdownHook(hook);
	}
	
	/**
	 * The little class to do the shutdown.
	 * 
	 * @author hulles
	 *
	 */
	private class ShutdownHook implements Runnable {
		ExecutorService pool;
		
		ShutdownHook(ExecutorService pool) {
			this.pool = pool;
		}
		
	    @Override
		public void run() {
	    	
	    	if (shuttingDownOnClose) {
	    		System.out.println("CONTROLLER -- Orderly shutdown, hook not engaged");
	    	} else {
		    	System.out.println("CONTROLLER -- Exceptional shutdown, hook engaged");
				shutdownAndAwaitTermination(pool);
	    	}
	    }
	}

	/**
	 * Create a ServiceManager and start all the room services. We also send a "what_sememes"
	 * request to see what sememes each room advertises that it can handle.
	 * 
	 */
	@Override
	protected void startUp() throws Exception {
		RoomRequest sememesQuery;
		Ticket ticket;
		List<Service> services;
		Set<Entry<Service,Long>> startupTimes;
		Long startupTime;
		Long totalStartupTime = 0L;
		
		services = loadServices(hallBus);
		serviceManager = new ServiceManager(services);
		serviceManager.startAsync();
		serviceManager.awaitHealthy();
		startupTimes = serviceManager.startupTimes().entrySet();
		for (Entry<Service,Long> entry : startupTimes) {
			startupTime = entry.getValue();
			totalStartupTime += startupTime;
			LOGGER.log(Level.INFO, entry.getKey() + " started in " + 
					A1iciaUtils.formatElapsedMillis(startupTime));
		}
		// Note that the total of startup times is not the same as the total 
		//    elapsed startup time thanks to the miracle of parallel processing....
		// The total of startup times IS useful to compare startup speeds for different 
		//    servers, however....
		LOGGER.log(Level.INFO, "Total of startup times is " + 
				A1iciaUtils.formatElapsedMillis(totalStartupTime));
		
		// send WHAT_SPARKS request to load sememeRooms
		ticket = Ticket.createNewTicket(hallBus, Room.CONTROLLER);
		ticket.setFromA1icianID(A1iciaConstants.getA1iciaA1icianID());
		sememesQuery = new RoomRequest(ticket);
		sememesQuery.setFromRoom(Room.CONTROLLER);
		sememesQuery.setSememePackages(SememePackage.getSingletonDefault("what_sememes"));
		sememesQuery.setMessage("WHAT_SPARKS query");
		controllerRoom.sendParentRequest(sememesQuery);
	}

	/**
	 * Shut down the service manager and bus pool.
	 * 
	 */
	@Override
	protected void shutDown() throws Exception {
		
		serviceManager.stopAsync();
		serviceManager.awaitStopped();
		A1iciaApplication.shutdown();
		shuttingDownOnClose = true;
		shutdownAndAwaitTermination(busPool);
	}

	/**
	 * This is the Controller's very own room. Keep out. No girls allowed.
	 * 
	 * @author hulles
	 *
	 */
	public class ControllerRoom extends UrRoom {
		private final SerialSememe whatSememesSememe;
		
		ControllerRoom() {
			super();
			
			whatSememesSememe = SerialSememe.find("what_sememes");
		}
		
		/**
		 * Send the "what_sememes" request on behalf of the Controller proper.
		 * 
		 * @param request The "what_sememes" request
		 */
		void sendParentRequest(RoomRequest request) {
			
			sendRoomRequest(request);
		}

		/**
		 * Return which room this is.
		 * 
		 */
		@Override
		public Room getThisRoom() {
			
			return Room.CONTROLLER;
		}
	
		@Override
		protected void roomStartup() {
		}
	
		@Override
		protected void roomShutdown() {
		}
	
		/**
		 * Process all the room responses we received from our "what_sememes" request.
		 * 
		 */
		@Override
		public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
			List<ActionPackage> pkgs;
			ActionPackage pkg;
			WhatSememesAction action;
			Set<SerialSememe> sememes;
			Room fromRoom;
			Ticket ticket = null;
			
			SharedUtils.checkNotNull(responses);
			ticket = request.getTicket();
			if (ticket == null) {
				A1iciaUtils.error("Controller: null ticket");
			}
			for (RoomResponse rr : responses) {
				fromRoom = rr.getFromRoom();
				pkgs = rr.getActionPackages();
				pkg = ActionPackage.has(whatSememesSememe, pkgs);
				if (pkg != null) {
					action = (WhatSememesAction) pkg.getActionObject();
					sememes = action.getSememes();
					LOGGER.log(LOGLEVEL, "In ControllerRoom:processRoomResponse with response from " +
							fromRoom + ", sememes from action = " + sememes);
					for (SerialSememe s : sememes) {
						sememeRooms.put(s, fromRoom);
					}
				} else {
					A1iciaUtils.error("Controller: unable to find what_sememes in action packages");
				}
			}
			// we do a couple quick reality checks before we go
			for (SerialSememe s : sememeRooms.keySet()) {
				if (!allSememes.contains(s)) {
					// Type I error
					A1iciaUtils.error("ControllerRoom: sememe " + s.getName() + " is not a valid sememe");
				}
			}
			for (SerialSememe s : allSememes) {
				if (!sememeRooms.containsKey(s)) {
					// Type II error
					A1iciaUtils.error("ControllerRoom: sememe " + s.getName() + " not implemented");
				}
			}
			if (ticket != null) {
				ticket.close();
			}
		}

		/**
		 * Load the list of sememes that we can handle (just one, "what_sememes").
		 * 
		 */
		@Override
		protected Set<SerialSememe> loadSememes() {
			Set<SerialSememe> sememes;
			
			sememes = new HashSet<>();
			sememes.add(SerialSememe.find("what_sememes"));
			return sememes;
		}

		/**
		 * We don't respond to room requests. Bear in mind that this method is only called
		 * if a request should be handled by us, so if we get one it's a serious error.
		 * 
		 */
		@Override
		public ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {
			throw new A1iciaException("Request not implemented in " + 
					getThisRoom().getDisplayName());
		}

		/**
		 * We don't respond to room announcements.
		 * 
		 */
		@Override
		protected void processRoomAnnouncement(RoomAnnouncement announcement) {
		}
		
	}
}
