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
package com.hulles.alixia.api.shared;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SharedUtils is a class that contains various Alixia utility functions. It
 * implements "Serializable", and it is expected that all methods are
 * GWT-safe.
 * 
 * @author hulles
 */
public class SharedUtils implements Serializable {
	private final static Logger LOGGER = LoggerFactory.getLogger(SharedUtils.class);
	private static final long serialVersionUID = 8123983689858668155L;
	@SuppressWarnings("unused")
	private static ServerSocket canary = null;
	public final static int EXIT_ALREADY_RUNNING = 1;

    /**
     * If the reference is null, throw an exception. See Guava Preconditions,
     * this is just adapted from there
     * 
     * @see com.google.common.base.Preconditions.checkNotNull
     * 
     * @param <T> A generic
     * @param reference The reference to check for null state
     */
	public static <T> void checkNotNull(T reference) {
		
		if (reference == null) {			
			throw new NullPointerException("Null reference in checkNotNull");
		}
	}

    /**
     * This doesn't do anything, it just indicates there might be valid null values
     * so we don't use checkNotNull, vs. forgetting to use checkNotNull. This works
     * really well in practice, and Guava should probably have it. ;)
     * 
     * @param <T>
     * @param reference 
     */
	public static <T> void nullsOkay(T reference) {
	}
	
    /**
     * Test if an application is already running on this machine. We
     * attempt to assign a known port number to a ServerSocket; if it works
     * we save it in a static variable, otherwise we return true because the
     * socket was created earlier.
     * 
     * @param portCheck The PortCheck for this program
     * @return True if there is another instance of this program already running.
     */
	public static boolean alreadyRunning(PortCheck portCheck) {
	
        com.google.common.base.Preconditions.
		checkNotNull(portCheck);
		try {
			canary = new ServerSocket(portCheck.getPortNumber(), 10, InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// shouldn't happen, it's localhost
			throw new AlixiaException();
		} catch (IOException e) {
			// port taken, so app is already running
			return true;
		}
		return false;
	}
	
    /**
     * Exit this program if another instance of it is already running.
     * 
     * @param portCheck The PortCheck for this program
     */
	public static void exitIfAlreadyRunning(PortCheck portCheck) {
		
		checkNotNull(portCheck);
		if (alreadyRunning(PortCheck.ALIXIA)) {
			LOGGER.error("Alixia is already running");
			System.exit(EXIT_ALREADY_RUNNING);
		}
	}
	
    /**
     * A list of Alixia executable programs and their assigned port numbers
     * 
     */
	public enum PortCheck {
	    ALIXIA(12345, "Alixia Central"),
	    ALIXIA_CLI(12346, "Alixia CLI"),
	    ALIXIA_MAGIC_MIRROR(12347, "Alixia Magic Mirror"),
	    ALIXIA_PI_CONSOLE(12348, "Alixia Pi Console"),
	    ALIXIA_NODE(12349, "Alixia Node Server"),
	    ALIXIA_SWING_CONSOLE(12350, "Alixia Swing Console");
	    private final int portNumber;
	    private final String displayName;

	    private PortCheck(int portNumber, String displayName) {

	    	this.portNumber = portNumber;
	        this.displayName = displayName;
	    }

	    public int getPortNumber() {
	    	
	        return portNumber;
	    }

	    public String getDisplayName() {
	    	
	        return displayName;
	    }
	}
}
