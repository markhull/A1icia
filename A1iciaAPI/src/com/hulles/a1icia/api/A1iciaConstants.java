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
package com.hulles.a1icia.api;

import com.hulles.a1icia.api.remote.A1icianID;
import java.util.logging.Level;

/**
 * Constants for A1icia. I expect this class to grow as more constants are migrated to here,
 * as part of A1icia's evolution.
 * 
 * 
 * @author hulles
 *
 */
public class A1iciaConstants {
	private final static A1icianID ALICIA_ALICIAN_ID = new A1icianID("ALICIA");
	private final static A1icianID BROADCAST_ALICIAN_ID = new A1icianID("ALL");
	private final static String ALICIAS_WELCOME = "Daily greater with all horizon users!";
    private final static Level ALICIA_LEVEL = Level.FINE;
		
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
     * Get A1icia's current default logging level.
     * 
     * @return The Level
     */
    public static Level getA1iciaLogLevel() {
        
        return ALICIA_LEVEL;
    }
}
