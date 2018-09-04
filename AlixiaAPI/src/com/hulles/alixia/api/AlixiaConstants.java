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
package com.hulles.alixia.api;

import com.hulles.alixia.api.remote.AlixianID;
import java.util.logging.Level;

/**
 * Constants for Alixia. I expect this class to grow as more constants are migrated to here,
 * as part of Alixia's evolution.
 * 
 * 
 * @author hulles
 *
 */
public class AlixiaConstants {
	private final static AlixianID ALIXIA_ALIXIAN_ID = new AlixianID("ALIXIA");
	private final static AlixianID BROADCAST_ALIXIAN_ID = new AlixianID("ALL");
	private final static String ALIXIAS_WELCOME = "Daily greater with all horizon users!";
    private final static Level ALIXIA_LEVEL = Level.FINE;
		
	/**
	 * Get the Alixian ID representing Alixia herself.
	 * 
	 * @return The ID
	 */
	public static AlixianID getAlixiaAlixianID() {
	
		return ALIXIA_ALIXIAN_ID; 
	}
	
	/**
	 * Get the Alixian ID representing a broadcast message to everyone.
	 * 
	 * @return The ID
	 */
	public static AlixianID getBroadcastAlixianID() {
		
		return BROADCAST_ALIXIAN_ID;
	}
	
	/**
	 * Get Alixia's welcome message.
	 * 
	 * @return The message
	 */
	public static String getAlixiasWelcome() {
		
		return ALIXIAS_WELCOME;
	}
	
    /**
     * Get Alixia's current default logging level.
     * 
     * @return The Level
     */
    public static Level getAlixiaLogLevel() {
        
        return ALIXIA_LEVEL;
    }
}
