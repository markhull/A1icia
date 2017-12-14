package com.hulles.a1icia.api;

import java.util.logging.Level;

import com.hulles.a1icia.api.remote.A1icianID;

public class A1iciaConstants {
	private final static A1icianID ALICIA_ALICIAN_ID = new A1icianID("ALICIA");
	private final static A1icianID BROADCAST_ALICIAN_ID = new A1icianID("ALL");
	private final static String ALICIAS_WELCOME = "Daily greater with all horizon users!";
	private final static Level ALICIA_LOGLEVEL = Level.FINE;
		
	/**
	 * Get the A1ician ID representing A1icia herself.
	 * 
	 * @return The ID
	 */
	public static A1icianID getA1iciaA1icianID() {
	
		return ALICIA_ALICIAN_ID;
	}
	
	/**
	 * Get the A1ician ID representing a broadcast message to everyone.
	 * 
	 * @return The ID
	 */
	public static A1icianID getBroadcastA1icianID() {
		
		return BROADCAST_ALICIAN_ID;
	}
	
	/**
	 * Get A1icia's welcome message.
	 * 
	 * @return The message
	 */
	public static String getA1iciasWelcome() {
		
		return ALICIAS_WELCOME;
	}
	
	/**
	 * Get the master logging level
	 * 
	 * @return The logging Level
	 */
	public static Level getA1iciaLogLevel() {
		
		return ALICIA_LOGLEVEL;
	}

}
