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
package com.hulles.a1icia.api.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.SharedUtils;

/**
 * A1iciaTimer is a simple little timer class, written so no external library is needed to perform
 * this function. It keeps track of simultaneous timers in a map.
 * 
 * @author hulles
 *
 */
public final class A1iciaTimer {
	final static Logger LOGGER = Logger.getLogger("A1icia.A1iciaTimer");
	final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final static Map<String, Long> TIMERMAP;
	
	private A1iciaTimer() {
		// only static methods, no need to instantiate it
	}
	
	static {
		TIMERMAP = new HashMap<>();
	}
	
	/**
	 * Start the timer.
	 * 
	 * @param timerName A name for the timer
	 */
	public static void startTimer(String timerName) {
		
		SharedUtils.checkNotNull(timerName);
		TIMERMAP.put(timerName, System.currentTimeMillis());
	}
	
	/**
	 * Stop the timer.
	 * 
	 * @param timerName The name of the timer started earlier
	 * @return The time, in milliseconds
	 */
	public static Long stopTimer(String timerName) {
		Long endTime;
		Long startTime;
		Long elapsedMillis;
		
		SharedUtils.checkNotNull(timerName);
		endTime = System.currentTimeMillis();
		startTime = TIMERMAP.remove(timerName);
		if (startTime == null) {
			System.err.println("Bad map start time in A1iciaTimer");
			return null;
		}
		elapsedMillis = endTime - startTime;
		LOGGER.log(LOGLEVEL, "Timer {0}: {1}", 
                new String[]{timerName, A1iciaUtils.formatElapsedMillis(elapsedMillis)});
		return elapsedMillis;
	}

}
