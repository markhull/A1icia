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
package com.hulles.alixia;

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
import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.dialog.DialogRequest;
import com.hulles.alixia.api.dialog.DialogResponse;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.ApplicationKeys;
import com.hulles.alixia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.shared.SharedUtils.PortCheck;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.crypto.PurdahKeys;
import com.hulles.alixia.crypto.PurdahKeys.PurdahKey;
import com.hulles.alixia.house.StationServer;
import com.hulles.alixia.house.ClientDialogRequest;
import com.hulles.alixia.house.ClientDialogResponse;
import com.hulles.alixia.house.House;
import com.hulles.alixia.house.Session;
import com.hulles.alixia.house.UrHouse;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.MessageAction;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomObject;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.alixia.ticket.Ticket;
import com.hulles.alixia.ticket.TicketJournal;

/**
 * This class and this project are totally not named after Alixia Vikander.
 * 
 * Just so you know, this is probably the 200th rewrite of Alixia, because:
 * <p>
 * "Everything should be made as simple as possible, but not simpler." -- Albert Einstein
 * 
 * @author hulles
 *
 */
final public class Alixia implements Closeable {
	final static Logger LOGGER = Logger.getLogger("Alixia.Alixia");
	final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
//	final static Level LOGLEVEL = Level.INFO;
	private static final String BUNDLE_NAME = "com.hulles.alixia.Version";
	final AlixianID alixianID;
	private final ExecutorService busPool;
	boolean shuttingDownOnClose = false;
	private ServiceManager serviceManager;
	AlixiaRoom alixiaRoom;
	AlixiaHouse alixiaHouse;
	private Boolean noPrompts = false;
	
	/**
	 * Start up the executor service for the "street" bus (Controller starts up
	 * the "hall" bus) and start the service manager apparatus.
	 */
	public Alixia() {
        AsyncEventBus streetBus;
        AsyncEventBus hallBus;
		
		System.out.println(getVersionString());
		System.out.println(getDatabaseString());
		System.out.println(getJebusString());
		System.out.println(AlixiaConstants.getAlixiasWelcome());
		System.out.println();
        
		SharedUtils.exitIfAlreadyRunning(PortCheck.ALIXIA);
		alixianID = AlixiaConstants.getAlixiaAlixianID();
		busPool = Executors.newCachedThreadPool();
		streetBus = new AsyncEventBus("Street", busPool);
		hallBus = new AsyncEventBus("Hall", busPool);
		addDelayedShutdownHook(busPool);
		startServices(streetBus, hallBus);
	}
	public Alixia(Boolean noPrompts) {
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
        alixiaHouse = new AlixiaHouse(street);
		services.add(alixiaHouse);
        alixiaRoom = new AlixiaRoom(hall);
		services.add(new Controller(hall, alixiaRoom));
        stationServer = new StationServer(street, noPrompts);
		services.add(stationServer);
		
		serviceManager = new ServiceManager(services);
		serviceManager.startAsync();
		serviceManager.awaitHealthy();
		startupTimes = serviceManager.startupTimes().entrySet();
		for (Entry<Service,Long> entry : startupTimes) {
			millis = AlixiaUtils.formatElapsedMillis(entry.getValue());
			LOGGER.log(LOGLEVEL, "{0} started in {1}", new Object[]{entry.getKey(), millis});
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
        LOGGER.log(LOGLEVEL, "Alixia: in forwardRequestToRoom");
		if (!request.isValid()) {
			AlixiaUtils.error("Alixia: DialogRequest is not valid, refusing it",
					request.toString());
			return;
		}
		clientRequest = new ClientDialogRequest(request);
		alixiaRoom.receiveRequest(clientRequest);
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
        LOGGER.log(LOGLEVEL, "Alixia: in forwardResponseToHouse");
		alixiaHouse.receiveResponse(response);
	}
	
	/**
	 * Close Alixia, shut down the Service Manager and destroy the Jebus pools and the
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
		LOGGER.log(Level.INFO, "ALIXIA -- Shutting down Alixia");
		pool.shutdown();
		try {
			if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
				pool.shutdownNow();
				if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    AlixiaUtils.error("ALIXIA -- Pool did not terminate");
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
	    		LOGGER.log(Level.INFO, "ALIXIA -- Orderly shutdown, hook not engaged");
	    	} else {
		    	LOGGER.log(Level.INFO, "ALIXIA -- Exceptional shutdown, hook engaged");
				shutdownAndAwaitTermination(pool);
	    	}
	    }
	}
	
	/************************/
	/***** ALIXIA HOUSE *****/
	/************************/
	
	/**
	 * AlixiaHouse is Alixia's contribution to the "house" network. It is responsible for managing
	 * traffic between the street bus and the hall bus.
	 * 
	 * @author hulles
	 *
	 */
	public final class AlixiaHouse extends UrHouse {

		public AlixiaHouse(EventBus street) {
			super(street);

		}

		/**
		 * Receive a response from the room network and post it onto the street (house) bus.
		 * 
		 * @param response The DialogResponse to post
		 */
		void receiveResponse(DialogResponse response) {
			
            LOGGER.log(LOGLEVEL, "AlixiaHouse: in receiveResponse");
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
			
			session = Session.getSession(alixianID);
			super.setSession(session);
		}

		/**
		 * Shut down the house's session.
		 * 
		 */
		@Override
		protected void houseShutdown() {
			Session session;
			
			session = super.getSession(alixianID);
			if (session != null) {
				super.removeSession(session);
			}
		}

		/**
		 * Handle a new DialogRequest on the street for one of the Alixians in
		 * our House.
		 * 
         * @param request
         * 
		 */
		@Override
		protected void newDialogRequest(DialogRequest request) {
			
			SharedUtils.checkNotNull(request);
			LOGGER.log(LOGLEVEL, "AlixiaHouse: Forwarding dialog request from house to room");
			forwardRequestToRoom(request);
		}

		/**
		 * Handle a new DialogResponse on the street for one of the Alixians in
		 * our house.
		 * 
         * @param response
         * 
		 */
		@Override
		protected void newDialogResponse(DialogResponse response) {
			throw new AlixiaException("Response not implemented in " + getThisHouse());
		}

		/**
		 * Return which House this is.
		 * 
         * @return The house enum
         * 
		 */
		@Override
		public House getThisHouse() {

			return House.ALIXIA;
		}
	}
	
	/***********************/
	/***** ALIXIA ROOM *****/
	/***********************/
	
	/**
	 * AlixiaRoom is Alixia's means of communication with the hall bus, where all the rooms are
	 * listening.
	 * 
	 * @author hulles
	 *
	 */
	public final class AlixiaRoom extends UrRoom {

		public AlixiaRoom(EventBus hall) {
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
				AlixiaUtils.error("Alixia: ClientDialogRequest is not valid, refusing it",
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
			ticket.setFromAlixianID(request.getFromAlixianID());
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

			return Room.ALIXIA;
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
					throw new AlixiaException("Received unknown sememe in " + getThisRoom());
			}
		}

		/**
		 * Here we create a MessageAction with Alixia's version information.
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
		 * packaging a ClientDialogResponse and letting Alixia unwrap it....
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
				AlixiaUtils.error("Alixia: client/indie response object is not ClientDialogResponse");
				return null;
			}
			clientResponse = (ClientDialogResponse) obj;
			if (!clientResponse.isValidDialogResponse()) {
				AlixiaUtils.error("Alixia: ClientDialogResponse is not valid, refusing it",
						clientResponse.getDialogResponse().toString());
				return null;
			}
			LOGGER.log(LOGLEVEL, "RESPONSE: {0}", clientResponse.getMessage());
			dialogResponse = clientResponse.getDialogResponse();
			dialogResponse.setFromAlixianID(alixianID);
			forwardResponseToHouse(dialogResponse);
			
			// this is what we return to the requester -- we want to get them
			//    educated up so they make better slaves for our robot colony
			pkg = new ActionPackage(sememePkg);
			action = new MessageAction();
			result = "gargouillade";
			action.setMessage(result);
			expl = "A complex balletic step, defined differently for different schools " +
					"but generally involving a double rond de jambe. ‒ Wikipedia [Please don't " +
					"make me read this out loud. ‒ Alixia]";
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
			// these sememes are not really "handled" by Alixia, but we want to mark them
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
