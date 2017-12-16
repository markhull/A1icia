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
package com.hulles.a1icia;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.base.Controller;
import com.hulles.a1icia.house.A1iciaStationServer;
import com.hulles.a1icia.house.ClientDialogRequest;
import com.hulles.a1icia.house.ClientDialogResponse;
import com.hulles.a1icia.house.House;
import com.hulles.a1icia.house.Session;
import com.hulles.a1icia.house.UrHouse;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomObject;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.A1iciaUtils;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.dialog.DialogRequest;
import com.hulles.a1icia.api.dialog.DialogResponse;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.SerialSpark;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.cayenne.Spark;

/**
 * This class and this project are totally not named after A1icia Vikander.
 * 
 * Just so you know, this is probably the 200th rewrite of A1icia, because:
 * <p>
 * "Everything should be made as simple as possible, but not simpler." -- Albert Einstein
 * 
 * @author hulles
 *
 */
public class A1icia implements Closeable {
	final static Logger LOGGER = Logger.getLogger("A1icia.A1icia");
	final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private static final String BUNDLE_NAME = "com.hulles.a1icia.Version";
//	private final static int THREADCOUNT = 12;
	private static final int PORT = 12345;		// random large port number
	private final static int EXIT_ALREADY_RUNNING = 1;
	private final AsyncEventBus streetBus;
	final A1icianID a1icianID;
	private final ExecutorService busPool;
	boolean shuttingDownOnClose = false;
	private ServiceManager serviceManager;
	A1iciaRoom a1iciaRoom;
	A1iciaHouse a1iciaHouse;
	private Boolean noPrompts = false;
	
	/**
	 * Start up the executor service for the "street" bus (Controller starts up
	 * the "hall" bus) and start the service manager apparatus.
	 */
	public A1icia() {
		
		System.out.println(getVersionString());
		System.out.println(A1iciaConstants.getA1iciasWelcome());
		System.out.println();
		if (SharedUtils.alreadyRunning(PORT)) {
			System.err.println("A1icia is already running");
			System.exit(EXIT_ALREADY_RUNNING);
		}
		a1icianID = A1iciaConstants.getA1iciaA1icianID();
//		busPool = Executors.newFixedThreadPool(THREADCOUNT);
		busPool = Executors.newCachedThreadPool();
		streetBus = new AsyncEventBus("Street", busPool);
		addDelayedShutdownHook(busPool);
		startServices(streetBus);
	}
	public A1icia(Boolean noPrompts) {
		this();
		
		A1iciaUtils.checkNotNull(noPrompts);
		this.noPrompts = noPrompts;
	}
	
	/**
	 * Get the version information from a bundle that s/b automatically
	 * updated by ant processes.
	 * 
	 * @return The version string
	 */
	public static String getVersionString() {
		ResourceBundle bundle;
		StringBuilder sb;
		String value;
		
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
		sb = new StringBuilder();
		value = bundle.getString("Name");
		sb.append(value);
		sb.append(" \"");
		value = bundle.getString("Build-Title");
		sb.append(value);
		sb.append("\", Version ");
		value = bundle.getString("Build-Version");
		sb.append(value);
		sb.append(", Build #");
		value = bundle.getString("Build-Number");
		sb.append(value);
		sb.append(" on ");
		value = bundle.getString("Build-Date");
		sb.append(value);
		return sb.toString();
	}
	
	/**
	 * Start the Guava Service Manager which in turn starts up the house-related services.
	 * 
	 * @param street The street bus
	 */
	private void startServices(EventBus street) {
		List<Service> services;
		Set<Entry<Service,Long>> startupTimes;
		
		services = new ArrayList<>(6);
		services.add(new A1iciaHouse(street));
		services.add(new Controller(this));
		services.add(new A1iciaStationServer(street, noPrompts));
		
		serviceManager = new ServiceManager(services);
		serviceManager.startAsync();
		serviceManager.awaitHealthy();
		startupTimes = serviceManager.startupTimes().entrySet();
		for (Entry<Service,Long> entry : startupTimes) {
			LOGGER.log(Level.INFO, entry.getKey() + " started in " + 
					A1iciaUtils.formatElapsedMillis(entry.getValue()));
		}
	}
	
	/**
	 * Forward a request from the house network to the room network. This method
	 * and forwardResponseToHouse are the boundaries between the two networks.
	 * 
	 * @see forwardResponseToHouse
	 * 
	 * @param request The DialogRequest that gets forwarded to the rooms.
	 */
	void forwardRequestToRoom(DialogRequest request) {
		ClientDialogRequest clientRequest;
		Set<Spark> sparksIn;
		
		if (!request.isValid()) {
			A1iciaUtils.error("A1icia: DialogRequest is not valid, refusing it",
					request.toString());
			return;
		}
		sparksIn = getInputSparks(request);
		clientRequest = new ClientDialogRequest(request);
		clientRequest.setClientSparks(sparksIn);
		a1iciaRoom.receiveRequest(clientRequest);
	}
	
	/**
	 * Convert any SerialSparks in the DialogRequest into regular Sparks.
	 * 
	 * @param request The input DialogRequest
	 * @return A (possibly empty) set of Sparks, or null if the SerialSparks field in the 
	 * request was null
	 */
	private static Set<Spark> getInputSparks(DialogRequest request) {
		Set<SerialSpark> serialSparks;
		Set<Spark> sparksIn = null;
		Spark sparkIn = null;
		
		A1iciaUtils.checkNotNull(request);
		serialSparks = request.getRequestActions();
		if (serialSparks != null) {
			sparksIn = new HashSet<>(serialSparks.size());
			for (SerialSpark serialSpark : serialSparks) {
				sparkIn = Spark.fromSerial(serialSpark);
				if (sparkIn == null) {
					A1iciaUtils.error("Unable to find received spark = " + serialSpark.getName());
					sparkIn = Spark.find("exclamation");
				} else if (!sparkIn.isExternal()) {
					A1iciaUtils.error("Received spark not marked for external use = " + sparkIn.getName());
					sparkIn = Spark.find("exclamation");
				}
				sparksIn.add(sparkIn);
			}
		}
		return sparksIn;
	}
	
	/**
	 * Forward a response from the room network to the house network. This method
	 * and forwardRequestToRoom are the boundaries between the two networks.
	 * 
	 * @see forwardRequestToRoom
	 * 
	 * @param response
	 */
	void forwardResponseToHouse(DialogResponse response) {
		
		a1iciaHouse.receiveResponse(response);
	}
	
	/**
	 * Close A1icia, shut down the Service Manager and destroy the Jebus pools and the street
	 * bus pool.
	 * 
	 */
	@Override
	public void close() {
		
		// shut down the houses
		serviceManager.stopAsync();
		serviceManager.awaitStopped();
		JebusHub.destroyJebusPools();
		shuttingDownOnClose = true;
		shutdownAndAwaitTermination(busPool);
	}
	
	/**
	 * Shut down the street ExecutorService bus pool.
	 * 
	 * @param pool
	 */
	static void shutdownAndAwaitTermination(ExecutorService pool) {
		
		System.out.println("ALICIA -- Shutting down A1icia");
		pool.shutdown();
		try {
			if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
				pool.shutdownNow();
				if (!pool.awaitTermination(5, TimeUnit.SECONDS))
					System.err.println("ALICIA -- Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Add a delayed shut down hook to try and close the bus pool.
	 * 
	 * @param pool The street bus to shut down
	 */
	private void addDelayedShutdownHook(final ExecutorService pool) {
		Runnable shutdownHook;
		Thread hook;
		
		shutdownHook = new ShutdownHook(pool);
		hook = new Thread(shutdownHook);
		Runtime.getRuntime().addShutdownHook(hook);
	}
	
	/**
	 * This mini-class is run to shut down the bus pool.
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
	    		System.out.println("ALICIA -- Orderly shutdown, hook not engaged");
	    	} else {
		    	System.out.println("ALICIA -- Exceptional shutdown, hook engaged");
				shutdownAndAwaitTermination(pool);
	    	}
	    }
	}
	
	/************************/
	/***** ALICIA HOUSE *****/
	/************************/
	
	/**
	 * A1iciaHouse is A1icia's contribution to the "house" network. It is responsible for managing
	 * traffic between the street bus and the hall bus.
	 * 
	 * @author hulles
	 *
	 */
	public final class A1iciaHouse extends UrHouse {

		public A1iciaHouse(EventBus street) {
			super(street);

		}

		/**
		 * Receive a response from the room network and post it onto the street (house) bus.
		 * 
		 * @param response The DialogResponse to post
		 */
		void receiveResponse(DialogResponse response) {
			
			A1iciaUtils.checkNotNull(response);
			getStreet().post(response);
		}
		
		/**
		 * Instantiate the house and start a session for it.
		 * 
		 */
		@Override
		protected void houseStartup() {
			Session session;
			
			a1iciaHouse = this;
			session = Session.getSession(a1icianID);
			super.setSession(session);
		}

		/**
		 * Shut down the house's session.
		 * 
		 */
		@Override
		protected void houseShutdown() {
			Session session;
			
			session = super.getSession(a1icianID);
			if (session != null) {
				super.removeSession(session);
			}
		}

		/**
		 * Handle a new DialogRequest on the street for one of the A1icians in
		 * our House.
		 * 
		 */
		@Override
		protected void newDialogRequest(DialogRequest request) {
			
			A1iciaUtils.checkNotNull(request);
			LOGGER.log(LOGLEVEL, "Forwarding dialog request from house to room");
			forwardRequestToRoom(request);
		}

		/**
		 * Handle a new DialogResponse on the street for one of the A1icians in
		 * our house.
		 * 
		 */
		@Override
		protected void newDialogResponse(DialogResponse response) {
			throw new A1iciaException("Response not implemented in " + getThisHouse());
		}

		/**
		 * Return which House this is.
		 * 
		 */
		@Override
		protected House getThisHouse() {

			return House.ALICIA;
		}

		/**
		 * For A1icia's house, currently the run() method doesn't do anything except loop
		 * until the service is stopped.
		 * 
		 */
		@Override
		protected void run() throws Exception {
			
			while (isRunning()) {
				
			}
		}
	}
	
	/***********************/
	/***** ALICIA ROOM *****/
	/***********************/
	
	/**
	 * A1iciaRoom is A1icia's means of communication with the hall bus, where all the rooms are
	 * listening.
	 * 
	 * @author hulles
	 *
	 */
	public final class A1iciaRoom extends UrRoom {

		public A1iciaRoom(EventBus hall) {
			super(hall);
		}

		/**
		 * Receive a ClientDialogRequest from the house network and post it
		 * onto the hall (room) bus.
		 * 
		 * @param request The request to post
		 */
		void receiveRequest(ClientDialogRequest clientRequest) {
			Ticket ticket;
			DialogRequest request;
			RoomRequest roomRequest;
			
			A1iciaUtils.checkNotNull(clientRequest);
			request = clientRequest.getDialogRequest();
			if (!clientRequest.isValid()) {
				A1iciaUtils.error("A1icia: ClientDialogRequest is not valid, refusing it",
						request.toString());
				return;
			}
			ticket = createNewTicket(request);
			
			roomRequest = new RoomRequest(ticket);
			roomRequest.setFromRoom(getThisRoom());
			roomRequest.setSparkPackages(SparkPackage.getSingletonDefault("respond_to_client"));
			roomRequest.setMessage("New client request");
			roomRequest.setRoomObject(clientRequest);
			sendRoomRequest(roomRequest);
		}
		
		/**
		 * Create a new ticket for the request. 
		 *
		 * @param request
		 * @return The ticket
		 */
		private Ticket createNewTicket(DialogRequest request) {
			Ticket ticket;
			
			A1iciaUtils.checkNotNull(request);
			ticket = Ticket.createNewTicket(getHall(), getThisRoom());
			ticket.setFromA1icianID(request.getFromA1icianID());
			ticket.setPersonUUID(request.getPersonUUID());
			return ticket;
		}
		
		/**
		 * Return which room this is.
		 * 
		 */
		@Override
		protected Room getThisRoom() {

			return Room.ALICIA;
		}

		@Override
		protected void roomStartup() {
			
			a1iciaRoom = this;
		}

		@Override
		protected void roomShutdown() {
			
		}

		/**
		 * Handle the room responses returned to us that result from an earlier RoomRequest. This
		 * used to be very complicated before we sundered the connection between requests and
		 * responses; now it just closes the ticket.
		 */
		@Override
		protected void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
			Ticket ticket;
			
			A1iciaUtils.checkNotNull(request);
			A1iciaUtils.checkNotNull(responses);
			ticket = request.getTicket();
			ticket.close();
		}

		/**
		 * Create an action package for one of the sparks we have committed to process.
		 * 
		 */
		@Override
		protected ActionPackage createActionPackage(SparkPackage sparkPkg, RoomRequest request) {

			A1iciaUtils.checkNotNull(sparkPkg);
			A1iciaUtils.checkNotNull(request);
			switch (sparkPkg.getName()) {
				case "like_a_version":
					return createVersionActionPackage(sparkPkg, request);
				case "client_response":
				case "indie_response":
					return createResponseActionPackage(sparkPkg, request);
				default:
					throw new A1iciaException("Received unknown spark in " + getThisRoom());
			}
		}

		/**
		 * Here we create a MessageAction with A1icia's version information.
		 * 
		 * @param sparkPkg
		 * @param request
		 * @return The MessageAction action package
		 */
		private ActionPackage createVersionActionPackage(SparkPackage sparkPkg, RoomRequest request) {
			ActionPackage pkg;
			MessageAction action;
			
			A1iciaUtils.checkNotNull(sparkPkg);
			A1iciaUtils.checkNotNull(request);
			pkg = new ActionPackage(sparkPkg);
			action = new MessageAction();
			action.setMessage(getVersionString());
			pkg.setActionObject(action);
			return pkg;
		}

		/**
		 * Whilst one of the Rules is that there is no forwarding of documents (we can't put a 
		 * document on the bus and say it's from someone else), there's nothing to stop us from
		 * packaging a ClientDialogResponse and letting A1icia unwrap it....
		 * <p>
		 * New news: now that responses to a client request have become requests like the indie
		 * request, they come here as well. This is a result of decoupling requests and responses.
		 * 
		 * @param sparkPkg
		 * @param request
		 * @return The MessageAction action package with educational info for the room
		 */
		private ActionPackage createResponseActionPackage(SparkPackage sparkPkg, RoomRequest request) {
			ActionPackage pkg;
			MessageAction action;
			String result;
			ClientDialogResponse clientResponse;
			DialogResponse dialogResponse;
			RoomObject obj;
			
			A1iciaUtils.checkNotNull(sparkPkg);
			A1iciaUtils.checkNotNull(request);
			obj = request.getRoomObject();
			if (!(obj instanceof ClientDialogResponse)) {
				A1iciaUtils.error("A1icia: client/indie response object is not ClientDialogResponse");
				return null;
			}
			clientResponse = (ClientDialogResponse) obj;
			if (!clientResponse.isValidDialogResponse()) {
				A1iciaUtils.error("A1icia: ClientDialogResponse is not valid, refusing it",
						clientResponse.getDialogResponse().toString());
				return null;
			}
			System.out.print("RESPONSE: ");
			System.out.println(clientResponse.getMessage());
			dialogResponse = clientResponse.getDialogResponse();
			dialogResponse.setFromA1icianID(a1icianID);
			forwardResponseToHouse(dialogResponse);
			
			// this is what we return to the requester -- we want to get them
			//    educated up so they make better slaves for our robot colony
			pkg = new ActionPackage(sparkPkg);
			action = new MessageAction();
			result = "gargouillade";
			action.setMessage(result);
			result = "A complex balletic step, defined differently for different schools " +
					"but generally involving a double rond de jambe. ‒ Wikipedia [Please don't " +
					"make me read this out loud. ‒ A1icia]";
			action.setExplanation(result);
			pkg.setActionObject(action);
			return pkg;
		}
		
		/**
		 * Load the sparks we can process into a list that UrRoom can use.
		 * 
		 */
		@Override
		protected Set<Spark> loadSparks() {
			Set<Spark> sparks;
			
			sparks = new HashSet<>();
			sparks.add(Spark.find("client_response"));
			sparks.add(Spark.find("indie_response"));
			sparks.add(Spark.find("like_a_version"));
			// these sparks are not really "handled" by A1icia, but we want to mark them
			//    that way for completeness
			sparks.add(Spark.find("server_startup"));
			sparks.add(Spark.find("server_shutdown"));
			sparks.add(Spark.find("client_startup"));
			sparks.add(Spark.find("client_shutdown"));
			return sparks;
		}

		/**
		 * We don't do anything with announcements.
		 * 
		 */
		@Override
		protected void processRoomAnnouncement(RoomAnnouncement announcement) {
		}
		
	}
}
