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
	
	// see guava SharedUtils, this is just adapted from there
	public static <T> void checkNotNull(T reference) {
		
		if (reference == null) {			
			throw new NullPointerException("Null reference in checkNotNull");
		}
	}

	public static <T> void nullsOkay(T reference) {
		// doesn't do anything, just indicates we don't use checkNotNull
	}
	
	public static boolean alreadyRunning(int port) {
		
		try {
			canary = new ServerSocket(port, 10, InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// shouldn't happen, it's localhost
			throw new A1iciaAPIException();
		} catch (IOException e) {
			// port taken, so app is already running
			return true;
		}
		return false;
	}
}
