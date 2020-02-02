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
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.dialog.DialogRequest;
import com.hulles.alixia.api.dialog.DialogResponse;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.ApplicationKeys;
import com.hulles.alixia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.shared.SharedUtils.PortCheck;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.api.tools.AlixiaVersion;
import com.hulles.alixia.crypto.PurdahKeys;
import com.hulles.alixia.crypto.PurdahKeys.PurdahKey;
import com.hulles.alixia.house.AlixiaHouse;
import com.hulles.alixia.house.ClientDialogRequest;
import com.hulles.alixia.house.UrHouse;
import com.hulles.alixia.room.AlixiaRoom;
import com.hulles.alixia.room.UrRoom;

/**
 * This class and this project are totally not named after Alicia Vikander.
 * 
 * Just so you know, this is probably the 200th rewrite of Alixia, because:
 * <p>
 * "Everything should be made as simple as possible, but not simpler." -- Albert Einstein
 * 
 * @author hulles
 *
 */
final public class Alixia implements Closeable {
	final static Logger LOGGER = LoggerFactory.getLogger(Alixia.class);
	public static final String BUNDLE_NAME = "com.hulles.alixia.Version";
	final AlixianID alixianID;
	private final ExecutorService busPool;
	boolean shuttingDownOnClose = false;
	private ServiceManager serviceManager;
	private static AlixiaRoom alixiaRoom;
	private static AlixiaHouse alixiaHouse;
	private final Boolean showOrphans;
	
	/**
	 * Start up the executor service for the "street" bus (Controller starts up
	 * the "hall" bus) and start the service manager apparatus.
	 */
	public Alixia(List<UrHouse> houses, List<UrRoom> rooms, Boolean showOrphans) {	
        AsyncEventBus streetBus;
        AsyncEventBus hallBus;
        String version;
 		ResourceBundle bundle;
 		
 		SharedUtils.checkNotNull(houses);
 		SharedUtils.checkNotNull(rooms);
 		SharedUtils.checkNotNull(showOrphans);
 		LOGGER.info("Alixia starting up");
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        version = AlixiaVersion.getVersionString(bundle);
		System.out.println(version);
		System.out.println(getDatabaseString());
		System.out.println(getJebusString());
		System.out.println(AlixiaConstants.getAlixiasWelcome());
		System.out.println();
        
		SharedUtils.exitIfAlreadyRunning(PortCheck.ALIXIA);
		this.showOrphans = showOrphans;
		alixianID = AlixiaConstants.getAlixiaAlixianID();
		busPool = Executors.newCachedThreadPool();
		streetBus = new AsyncEventBus("Street", busPool);
		hallBus = new AsyncEventBus("Hall", busPool);
		addDelayedShutdownHook(busPool);
		startServices(streetBus, houses, hallBus, rooms);
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
	private void startServices(EventBus street, List<UrHouse> houses, EventBus hall, List<UrRoom> rooms) {
		List<Service> services;
		Set<Entry<Service,Long>> startupTimes;
        String millis;
        
        SharedUtils.checkNotNull(street);
        SharedUtils.checkNotNull(hall);
        services = new ArrayList<>(6);
        
        alixiaHouse = new AlixiaHouse(street);
        alixiaRoom = new AlixiaRoom(hall);
        rooms.add(alixiaRoom);
        
		services.add(alixiaHouse);
		services.add(new Controller(hall, rooms, showOrphans));
        for (UrHouse house : houses) {
			LOGGER.info("Loading house {}", house.getThisHouse());
            house.setStreet(street);
			services.add(house);
        }
		
		serviceManager = new ServiceManager(services);
		serviceManager.startAsync();
		serviceManager.awaitHealthy();
		startupTimes = serviceManager.startupTimes().entrySet();
		for (Entry<Service,Long> entry : startupTimes) {
			millis = AlixiaUtils.formatElapsedMillis(entry.getValue());
			LOGGER.info("{} started in {}", entry.getKey(), millis);
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
	public static void forwardRequestToRoom(DialogRequest request) {
		ClientDialogRequest clientRequest;

        SharedUtils.checkNotNull(request);
        LOGGER.debug("Alixia: in forwardRequestToRoom");
		if (!request.isValid()) {
			LOGGER.error("Alixia: DialogRequest is not valid, refusing it: {}", request.toString());
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
	public static void forwardResponseToHouse(DialogResponse response) {

        SharedUtils.checkNotNull(response);
        LOGGER.debug("Alixia: in forwardResponseToHouse");
		alixiaHouse.receiveResponse(response);
	}
	
	/**
	 * Close Alixia, shut down the Service Manager and destroy the Jebus pools and the
	 * bus pool.eventBus.
	 * 
	 */
	@Override
	public void close() {
		
		LOGGER.info("Alixia close");
		// shut down the houses
		serviceManager.stopAsync();
		serviceManager.awaitStopped();
		LOGGER.debug("Alixia close: after awaitStopped");
		JebusHub.destroyJebusPools();
		LOGGER.debug("Alixia close: after destroyJebusPools");
		shuttingDownOnClose = true;
		shutdownAndAwaitTermination(busPool);
		LOGGER.debug("Alixia close: after shutdownAndAwaitTermination");
	}
	
	/**
	 * Shut down the street ExecutorService bus pool.
	 * 
	 * @param pool
	 */
	static void shutdownAndAwaitTermination(ExecutorService pool) {
		
        SharedUtils.checkNotNull(pool);
		LOGGER.info("ALIXIA -- Shutting down Alixia");
		pool.shutdown();
		LOGGER.debug("shutdownAndAwaitTermination: after pool shutdown");
		try {
			if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
				pool.shutdownNow();
				if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOGGER.error("ALIXIA -- Pool did not terminate");
                }
			}
		} catch (InterruptedException ie) {
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
		LOGGER.debug("shutdownAndAwaitTermination: after shutdown try-catch");
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
	    		LOGGER.info("ALIXIA -- Orderly shutdown, hook not engaged");
	    	} else {
		    	LOGGER.info("ALIXIA -- Exceptional shutdown, hook engaged");
				shutdownAndAwaitTermination(pool);
	    	}
	    }
	}
}
