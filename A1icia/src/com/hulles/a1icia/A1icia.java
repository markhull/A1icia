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
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.dialog.DialogRequest;
import com.hulles.a1icia.api.dialog.DialogResponse;
import com.hulles.a1icia.api.jebus.JebusHub;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.A1iciaException;
import com.hulles.a1icia.api.shared.ApplicationKeys;
import com.hulles.a1icia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.api.shared.SharedUtils.PortCheck;
import com.hulles.a1icia.api.tools.A1iciaUtils;
import com.hulles.a1icia.crypto.PurdahKeys;
import com.hulles.a1icia.crypto.PurdahKeys.PurdahKey;
import com.hulles.a1icia.house.StationServer;
import com.hulles.a1icia.house.ClientDialogRequest;
import com.hulles.a1icia.house.ClientDialogResponse;
import com.hulles.a1icia.house.House;
import com.hulles.a1icia.house.Session;
import com.hulles.a1icia.house.UrHouse;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomObject;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.ticket.TicketJournal;

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
final public class A1icia implements Closeable {
	final static Logger LOGGER = Logger.getLogger("A1icia.A1icia");
	final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
//	final static Level LOGLEVEL = Level.INFO;
	private static final String BUNDLE_NAME = "com.hulles.a1icia.Version";
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
        AsyncEventBus streetBus;
        AsyncEventBus hallBus;
		
		System.out.println(getVersionString());
		System.out.println(getDatabaseString());
		System.out.println(getJebusString());
		System.out.println(A1iciaConstants.getA1iciasWelcome());
		System.out.println();
        
		SharedUtils.exitIfAlreadyRunning(PortCheck.A1ICIA);
		a1icianID = A1iciaConstants.getA1iciaA1icianID();
		busPool = Executors.newCachedThreadPool();
		streetBus = new AsyncEventBus("Street", busPool);
		hallBus = new AsyncEventBus("Hall", busPool);
		addDelayedShutdownHook(busPool);
		startServices(streetBus, hallBus);
	}
	public A1icia(Boolean noPrompts) {
		this();
		
		SharedUtils.checkNotNull(noPrompts);
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
	
	private static String getDatabaseString() {
		StringBuilder sb;
		PurdahKeys purdah;
		
		sb = new StringBuilder();
		purdah = PurdahKeys.getInstance();
		sb.append("Database is ");
		sb.append(purdah.getPurdahKey(PurdahKey.DATABASENAME));
		sb.append(" at ");
		sb.append(purdah.getPurdahKey(PurdahKey.DATABASESERVER));
		sb.append(", port ");
		sb.append(purdah.getPurdahKey(PurdahKey.DATABASEPORT));
		return sb.toString();
	}
	
	private static String getJebusString() {
		StringBuilder sb;
		ApplicationKeys appKeys;
		
		sb = new StringBuilder();
		appKeys = ApplicationKeys.getInstance();
		sb.append("Jebus server is at ");
		sb.append(appKeys.getKey(ApplicationKey.JEBUSSERVER));
		sb.append(", port ");
		sb.append(appKeys.getKey(ApplicationKey.JEBUSPORT));
		return sb.toString();
	}
	
	/**
	 * Start the Guava Service Manager which in turn starts up the house-related services.
	 * 
	 * @param street The street event bus
     * @param hall The hall event bus
	 */
	private void startServices(EventBus street, EventBus hall) {
		List<Service> services;
		Set<Entry<Service,Long>> startupTimes;
        UrHouse stationServer;
        String millis;
        
        SharedUtils.checkNotNull(street);
        SharedUtils.checkNotNull(hall);
		services = new ArrayList<>(3);
        a1iciaHouse = new A1iciaHouse(street);
		services.add(a1iciaHouse);
        a1iciaRoom = new A1iciaRoom(hall);
		services.add(new Controller(hall, a1iciaRoom));
        stationServer = new StationServer(street, noPrompts);
		services.add(stationServer);
		
		serviceManager = new ServiceManager(services);
		serviceManager.startAsync();
		serviceManager.awaitHealthy();
		startupTimes = serviceManager.startupTimes().entrySet();
		for (Entry<Service,Long> entry : startupTimes) {
			millis = A1iciaUtils.formatElapsedMillis(entry.getValue());
			LOGGER.log(Level.INFO, "{0} started in {1}", new Object[]{entry.getKey(), millis});
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

        SharedUtils.checkNotNull(request);
        LOGGER.log(Level.INFO, "A1icia: in forwardRequestToRoom");
		if (!request.isValid()) {
			A1iciaUtils.error("A1icia: DialogRequest is not valid, refusing it",
					request.toString());
			return;
		}
		clientRequest = new ClientDialogRequest(request);
		a1iciaRoom.receiveRequest(clientRequest);
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

        SharedUtils.checkNotNull(response);
        LOGGER.log(Level.INFO, "A1icia: in forwardResponseToHouse");
		a1iciaHouse.receiveResponse(response);
	}
	
	/**
	 * Close A1icia, shut down the Service Manager and destroy the Jebus pools and the
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
		
        SharedUtils.checkNotNull(pool);
		System.out.println("ALICIA -- Shutting down A1icia");
		pool.shutdown();
		try {
			if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
				pool.shutdownNow();
				if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("ALICIA -- Pool did not terminate");
                }
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
		
        SharedUtils.checkNotNull(pool);
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
			
            LOGGER.log(Level.INFO, "A1iciaHouse: in receiveResponse");
			SharedUtils.checkNotNull(response);
			getStreet().post(response);
		}
		
		/**
		 * Instantiate the house and start a session for it.
		 * 
		 */
		@Override
		protected void houseStartup() {
			Session session;
			
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
         * @param request
         * 
		 */
		@Override
		protected void newDialogRequest(DialogRequest request) {
			
			SharedUtils.checkNotNull(request);
			LOGGER.log(LOGLEVEL, "A1iciaHouse: Forwarding dialog request from house to room");
			forwardRequestToRoom(request);
		}

		/**
		 * Handle a new DialogResponse on the street for one of the A1icians in
		 * our house.
		 * 
         * @param response
         * 
		 */
		@Override
		protected void newDialogResponse(DialogResponse response) {
			throw new A1iciaException("Response not implemented in " + getThisHouse());
		}

		/**
		 * Return which House this is.
		 * 
         * @return The house enum
         * 
		 */
		@Override
		public House getThisHouse() {

			return House.ALICIA;
		}

		/**
		 * For A1icia's house, currently the run() method doesn't do anything except loop
		 * until the service is stopped.
		 * 
         * @throws Exception
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
			
			SharedUtils.checkNotNull(clientRequest);
			request = clientRequest.getDialogRequest();
			if (!clientRequest.isValid()) {
				A1iciaUtils.error("A1icia: ClientDialogRequest is not valid, refusing it",
						request.toString());
				return;
			}
			ticket = createNewTicket(clientRequest);
			
			roomRequest = new RoomRequest(ticket, request.getDocumentID());
			roomRequest.setFromRoom(getThisRoom());
			roomRequest.setSememePackages(SememePackage.getSingletonDefault("respond_to_client"));
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
		private Ticket createNewTicket(ClientDialogRequest clientRequest) {
			Ticket ticket;
			TicketJournal journal;
			DialogRequest request;
			
			SharedUtils.checkNotNull(clientRequest);
			request = clientRequest.getDialogRequest();
			ticket = Ticket.createNewTicket(getHall(), getThisRoom());
			ticket.setFromA1icianID(request.getFromA1icianID());
			ticket.setPersonUUID(request.getPersonUUID());
			journal = ticket.getJournal();
			journal.setClientRequest(clientRequest);
			return ticket;
		}
		
		/**
		 * Return which room this is.
		 * 
         * @return This room
		 */
		@Override
		public Room getThisRoom() {

			return Room.ALICIA;
		}

		@Override
		protected void roomStartup() {
			
		}

		@Override
		protected void roomShutdown() {
			
		}

		/**
		 * Handle the room responses returned to us that result from an earlier RoomRequest. 
         * This used to be very complicated before we sundered the connection between 
         * requests and responses; now it just closes the ticket.
         * 
         * @param request The RoomRequest
         * @param responses The list of RoomResponses
		 */
		@Override
		protected void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
			Ticket ticket;
			
			SharedUtils.checkNotNull(request);
			SharedUtils.checkNotNull(responses);
			ticket = request.getTicket();
			ticket.close();
		}

		/**
		 * Create an action package for one of the sememes we have committed to process.
		 * 
         * @param sememePkg The sememe we advertised earlier
         * @param request The room request 
         * @return The new ActionPackage
		 */
		@Override
		protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

			SharedUtils.checkNotNull(sememePkg);
			SharedUtils.checkNotNull(request);
			switch (sememePkg.getName()) {
				case "like_a_version":
					return createVersionActionPackage(sememePkg, request);
				case "client_response":
				case "indie_response":
					return createResponseActionPackage(sememePkg, request);
				default:
					throw new A1iciaException("Received unknown sememe in " + getThisRoom());
			}
		}

		/**
		 * Here we create a MessageAction with A1icia's version information.
		 * 
		 * @param sememePkg
		 * @param request
		 * @return The MessageAction action package
		 */
		private ActionPackage createVersionActionPackage(SememePackage sememePkg, RoomRequest request) {
			ActionPackage pkg;
			MessageAction action;
			
			SharedUtils.checkNotNull(sememePkg);
			SharedUtils.checkNotNull(request);
			pkg = new ActionPackage(sememePkg);
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
		 * @param sememePkg
		 * @param request
		 * @return The MessageAction action package with educational info for the room
		 */
		private ActionPackage createResponseActionPackage(SememePackage sememePkg, RoomRequest request) {
			ActionPackage pkg;
			MessageAction action;
			String result;
            String expl;
			ClientDialogResponse clientResponse;
			DialogResponse dialogResponse;
			RoomObject obj;
			
			SharedUtils.checkNotNull(sememePkg);
			SharedUtils.checkNotNull(request);
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
			pkg = new ActionPackage(sememePkg);
			action = new MessageAction();
			result = "gargouillade";
			action.setMessage(result);
			expl = "A complex balletic step, defined differently for different schools " +
					"but generally involving a double rond de jambe. ‒ Wikipedia [Please don't " +
					"make me read this out loud. ‒ A1icia]";
			action.setExplanation(expl);
			pkg.setActionObject(action);
			return pkg;
		}
		
		/**
		 * Load the sememes we can process into a list that UrRoom can use.
		 * 
         * @return The set of sememes we advertise
         * 
		 */
		@Override
		protected Set<SerialSememe> loadSememes() {
			Set<SerialSememe> sememes;
			
			sememes = new HashSet<>();
			sememes.add(SerialSememe.find("client_response"));
			sememes.add(SerialSememe.find("indie_response"));
			sememes.add(SerialSememe.find("like_a_version"));
			// these sememes are not really "handled" by A1icia, but we want to mark them
			//    that way for completeness
			sememes.add(SerialSememe.find("central_startup"));
			sememes.add(SerialSememe.find("central_shutdown"));
			sememes.add(SerialSememe.find("client_startup"));
			sememes.add(SerialSememe.find("client_shutdown"));
            sememes.add(SerialSememe.find("notify"));
			return sememes;
		}

		/**
		 * We don't do anything with announcements.
		 * 
         * @param announcement The RoomAnnouncement
         * 
		 */
		@Override
		protected void processRoomAnnouncement(RoomAnnouncement announcement) {
		}
		
	}
}
