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
package com.hulles.a1icia.api.shared;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class SharedUtils implements Serializable {
	private static final long serialVersionUID = 8123983689858668155L;
	@SuppressWarnings("unused")
	private static ServerSocket canary = null;
	public final static int EXIT_ALREADY_RUNNING = 1;
	
	// see guava SharedUtils, this is just adapted from there
	public static <T> void checkNotNull(T reference) {
		
		if (reference == null) {			
			throw new NullPointerException("Null reference in checkNotNull");
		}
	}

	public static <T> void nullsOkay(T reference) {
		// doesn't do anything, just indicates we don't use checkNotNull
	}
	
	public static boolean alreadyRunning(PortCheck portCheck) {
	
		checkNotNull(portCheck);
		try {
			canary = new ServerSocket(portCheck.getPortNumber(), 10, InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// shouldn't happen, it's localhost
			throw new A1iciaAPIException();
		} catch (IOException e) {
			// port taken, so app is already running
			return true;
		}
		return false;
	}
	
	public static void exitIfAlreadyRunning(PortCheck portCheck) {
		
		checkNotNull(portCheck);
		if (alreadyRunning(PortCheck.A1ICIA)) {
			System.err.println("A1icia is already running");
			System.exit(EXIT_ALREADY_RUNNING);
		}
	}
	
	public enum PortCheck {
	    A1ICIA(12345, "A1icia"),
	    A1ICIA_CLI(12346, "A1icia CLI"),
	    A1ICIA_MAGIC_MIRROR(12347, "A1icia Magic Mirror"),
	    A1ICIA_PI_CONSOLE(12348, "A1icia Pi Console"),
	    A1ICIA_NODE(12349, "A1icia Node Server"),
	    A1ICIA_SWING_CONSOLE(12350, "A1icia Swing Console");
	    private int portNumber;
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
