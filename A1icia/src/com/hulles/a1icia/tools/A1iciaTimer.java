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
package com.hulles.a1icia.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.A1iciaConstants;

/**
 * A1iciaTimer is a simple little timer class, written so no external library is needed to perform
 * this function. It keeps track of simultaneous timers in a map.
 * 
 * @author hulles
 *
 */
public final class A1iciaTimer {
	final static Logger logger = Logger.getLogger("A1icia.A1iciaTimer");
	final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final static Map<String, Long> timerMap;
	
	private A1iciaTimer() {
		// only static methods, no need to instantiate it
	}
	
	static {
		timerMap = new HashMap<>();
	}
	
	/**
	 * Start the timer.
	 * 
	 * @param timerName A name for the timer
	 */
	public static void startTimer(String timerName) {
		
		A1iciaUtils.checkNotNull(timerName);
		timerMap.put(timerName, System.currentTimeMillis());
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
		
		A1iciaUtils.checkNotNull(timerName);
		endTime = System.currentTimeMillis();
		startTime = timerMap.remove(timerName);
		if (startTime == null) {
			System.err.println("Bad map start time in MindTimer");
			return null;
		}
		elapsedMillis = endTime - startTime;
		logger.log(LOGLEVEL, "Timer " + timerName + ": " + A1iciaUtils.formatElapsedMillis(elapsedMillis));
		return elapsedMillis;
	}

}
