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
package com.hulles.a1icia.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
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
import com.hulles.a1icia.room.BusMonitor;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.room.document.WhatSparksAction;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.A1iciaUtils;
import com.hulles.a1icia.alpha.AlphaRoom;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.bravo.BravoRoom;
import com.hulles.a1icia.cayenne.A1iciaApplication;
import com.hulles.a1icia.cayenne.Spark;
import com.hulles.a1icia.charlie.CharlieRoom;
import com.hulles.a1icia.delta.DeltaRoom;
import com.hulles.a1icia.echo.EchoRoom;
import com.hulles.a1icia.foxtrot.FoxtrotRoom;
import com.hulles.a1icia.golf.GolfRoom;
import com.hulles.a1icia.hotel.HotelRoom;
import com.hulles.a1icia.india.IndiaRoom;
import com.hulles.a1icia.juliet.JulietRoom;
import com.hulles.a1icia.kilo.KiloRoom;
import com.hulles.a1icia.lima.LimaRoom;
import com.hulles.a1icia.mike.MikeRoom;
import com.hulles.a1icia.november.NovemberRoom;
import com.hulles.a1icia.oscar.OscarRoom;
import com.hulles.a1icia.overmind.OvermindRoom;
import com.hulles.a1icia.papa.PapaRoom;
import com.hulles.a1icia.quebec.QuebecRoom;
import com.hulles.a1icia.tracker.Tracker;

/**
 * The Controller is a little simpler than its name would suggest -- it basically just 
 * starts and stops all the in-house rooms. However, if we ever need dependency 
 * injection or some crazy thing like that this is the guy to deliver room service, so to speak.
 * 
 * @author hulles
 *
 */
public final class Controller extends AbstractIdleService {
	final static Logger logger = Logger.getLogger("A1icia.Controller");
	final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
//	private final static int THREADCOUNT = 12;
	private final AsyncEventBus hallBus;
	private final ExecutorService busPool;
	boolean shuttingDownOnClose = false;
	final SetMultimap<Spark, Room> sparkRooms;
	private ControllerRoom controllerRoom;
	private ServiceManager serviceManager;
	private final A1icia a1iciaInstance;
	private volatile static Controller controllerInstance = null;
	
	public Controller(A1icia a1icia) {
		
		A1iciaUtils.checkNotNull(a1icia);
		if (controllerInstance != null) {
			throw new A1iciaException("Controller: attempting multiple instances");
		}
		this.a1iciaInstance = a1icia;
		busPool = Executors.newCachedThreadPool();
		addDelayedShutdownHook(busPool);
		A1iciaApplication.setJdbcLogging(false); 
		hallBus = new AsyncEventBus("MindBus", busPool);
		sparkRooms = MultimapBuilder.hashKeys().enumSetValues(Room.class).build();
		controllerInstance = this;
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
		
		A1iciaUtils.checkNotNull(hall);
		services = new ArrayList<>(40);
		services.add(new Tracker(hall));
		for (Room room : UrRoom.getAllRooms()) {
			switch (room) {
				case CONTROLLER:
					controllerRoom = new ControllerRoom(hall);
					services.add(controllerRoom);
					break;
				case BUSMONITOR:
					services.add(new BusMonitor(hall));
					break;
				case ALICIA:
					services.add(a1iciaInstance.new A1iciaRoom(hall));
					break;
				case OVERMIND:
					services.add(new OvermindRoom(hall));
					break;
				case ALPHA:
					services.add(new AlphaRoom(hall));
					break;
				case BRAVO:
					services.add(new BravoRoom(hall));
					break;
				case CHARLIE:
					services.add(new CharlieRoom(hall));
					break;
				case DELTA:
					services.add(new DeltaRoom(hall));
					break;
				case ECHO:
					services.add(new EchoRoom(hall));
					break;
				case FOXTROT:
					services.add(new FoxtrotRoom(hall));
					break;
				case GOLF:
					services.add(new GolfRoom(hall));
					break;
				case HOTEL:
					services.add(new HotelRoom(hall));
					break;
				case INDIA:
					services.add(new IndiaRoom(hall));
					break;
				case JULIET:
					services.add(new JulietRoom(hall));
					break;
				case KILO:
					services.add(new KiloRoom(hall));
					break;
				case LIMA:
					services.add(new LimaRoom(hall));
					break;
				case MIKE:
					services.add(new MikeRoom(hall));
					break;
				case NOVEMBER:
					services.add(new NovemberRoom(hall));
					break;
				case OSCAR:
					services.add(new OscarRoom(hall));
					break;
				case PAPA:
					services.add(new PapaRoom(hall));
					break;
				case QUEBEC:
					services.add(new QuebecRoom(hall));
					break;
				default:
					throw new A1iciaException("Bad room = " + room.getDisplayName());
			}
		}
		return services;
	}

	/**
	 * Return a set of rooms that can process the spark.
	 * 
	 * @param spark The spark in question
	 * @return A list of rooms that have advertised they can process the spark
	 */
	public Set<Room> getRoomsForSpark(Spark spark) {
		
		return sparkRooms.get(spark);
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
	 * Create a ServiceManager and start all the room services. We also send a "what_sparks"
	 * request to see what sparks each room advertises that it can handle.
	 * 
	 */
	@Override
	protected void startUp() throws Exception {
		RoomRequest sparksQuery;
		Ticket ticket;
		List<Service> services;
		Set<Entry<Service,Long>> startupTimes;
		
		services = loadServices(hallBus);
		serviceManager = new ServiceManager(services);
		serviceManager.startAsync();
		serviceManager.awaitHealthy();
		startupTimes = serviceManager.startupTimes().entrySet();
		for (Entry<Service,Long> entry : startupTimes) {
			logger.log(Level.INFO, entry.getKey() + " started in " + 
					A1iciaUtils.formatElapsedMillis(entry.getValue()));
		}
		
		// send WHAT_SPARKS request to load sparkRooms
		ticket = Ticket.createNewTicket(hallBus, Room.CONTROLLER);
		ticket.setFromA1icianID(A1iciaConstants.getA1iciaA1icianID());
		sparksQuery = new RoomRequest(ticket, null);
		sparksQuery.setFromRoom(Room.CONTROLLER);
		sparksQuery.setSparkPackages(SparkPackage.getSingletonDefault("what_sparks"));
		sparksQuery.setMessage("WHAT_SPARKS query");
		controllerRoom.sendParentRequest(sparksQuery);
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
		private final Spark whatSparksSpark;
		
		ControllerRoom(EventBus bus) {
			super(bus);
			
			whatSparksSpark = Spark.find("what_sparks");
		}
		
		/**
		 * Send the "what_sparks" request on behalf of the Controller proper.
		 * 
		 * @param request The "what_sparks" request
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
		 * Process all the room responses we received from our "what_sparks" request.
		 * 
		 */
		@Override
		public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
			List<ActionPackage> pkgs;
			ActionPackage pkg;
			WhatSparksAction action;
			Set<Spark> sparks;
			Room fromRoom;
			Ticket ticket = null;
			List<Spark> allSparks;
			
			A1iciaUtils.checkNotNull(responses);
			ticket = request.getTicket();
			if (ticket == null) {
				A1iciaUtils.error("Controller: null ticket");
			}
			for (RoomResponse rr : responses) {
				fromRoom = rr.getFromRoom();
				pkgs = rr.getActionPackages();
				pkg = ActionPackage.has(whatSparksSpark, pkgs);
				if (pkg != null) {
					action = (WhatSparksAction) pkg.getActionObject();
					sparks = action.getSparks();
					logger.log(LOGLEVEL, "In ControllerRoom:processRoomResponse with response from " +
							fromRoom + ", sparks from action = " + sparks);
					for (Spark s : sparks) {
						sparkRooms.put(s, fromRoom);
					}
				} else {
					A1iciaUtils.error("Controller: unable to find what_sparks in action packages");
				}
			}
			// we do a couple quick reality checks before we go
			allSparks = Spark.getAllSparks();
			for (Spark s : sparkRooms.keySet()) {
				if (!allSparks.contains(s)) {
					// Type I error
					A1iciaUtils.error("ControllerRoom: spark " + s.getName() + " is not a valid spark");
				}
			}
			for (Spark s : allSparks) {
				if (!sparkRooms.containsKey(s)) {
					// Type II error
					A1iciaUtils.error("ControllerRoom: spark " + s.getName() + " not implemented");
				}
			}
			if (ticket != null) {
				ticket.close();
			}
		}

		/**
		 * Load the list of sparks that we can handle (just one, "what_sparks").
		 * 
		 */
		@Override
		protected Set<Spark> loadSparks() {
			Set<Spark> sparks;
			
			sparks = new HashSet<>();
			sparks.add(Spark.find("what_sparks"));
			return sparks;
		}

		/**
		 * We don't respond to room requests. Bear in mind that this method is only called
		 * if a request should be handled by us, so if we get one it's a serious error.
		 * 
		 */
		@Override
		public ActionPackage createActionPackage(SparkPackage sparkPkg, RoomRequest request) {
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
