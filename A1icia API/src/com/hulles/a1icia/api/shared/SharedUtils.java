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
