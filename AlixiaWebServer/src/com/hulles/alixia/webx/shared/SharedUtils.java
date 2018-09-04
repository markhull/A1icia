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
package com.hulles.alixia.webx.shared;

import com.hulles.alixia.api.AlixiaConstants;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SharedUtils implements Serializable {
	private final static Logger LOGGER = Logger.getLogger("AlixiaWeb.SharedUtils");
	private final static Level LOGLEVEL = Level.SEVERE;
	private static final long serialVersionUID = 8123983689858668155L;
	
	// see guava SharedUtils, this is just adapted from there
	public static <T> void checkNotNull(T reference) {
		
		if (reference == null) {			
			throw new NullPointerException("Null reference in checkNotNull");
		}
	}

	public static <T> void nullsOkay(T reference) {
		// doesn't do anything, just indicates we don't use checkNotNull
	}
	
	public static void error(String errStr) {
		
		LOGGER.log(LOGLEVEL, errStr);
	}
	
	public static void error(String errStr, Throwable ex) {
		
		LOGGER.log(LOGLEVEL, errStr);
		ex.printStackTrace();
	}
}
