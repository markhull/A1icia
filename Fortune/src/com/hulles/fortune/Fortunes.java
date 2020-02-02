/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
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
 *******************************************************************************/
package com.hulles.fortune;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.fortune.cayenne.Fortune;

public class Fortunes {
	private final static Logger LOGGER = LoggerFactory.getLogger(Fortunes.class);
    private static final long MILLIS_TO_MINUTES = 1000l * 60l; // millis divided by dbUnit = minutes
	private static final long FORTUNE_MILLIS = 60l * 12l * MILLIS_TO_MINUTES; // every 12 hours (for now)
	@SuppressWarnings("unused")
    private static long nextFortune;
	private static final boolean USE_EXTERNAL_MOTD = true;
	
	public static SerialFortune getFortune() {
		SerialFortune fortune;
		Fortune dbFortune = null;
		String[] result;
		
		if (USE_EXTERNAL_MOTD) {
//			if (System.currentTimeMillis() > nextFortune) {
				// get one from the Packetizer MOTD service
				result = ExternalAperture.getMOTD();
				if (result != null) {
					// we arbitrarily compare the first 36 characters of the quote for a match
					if (!Fortune.fortuneAlreadyExists(result[0])) {
						dbFortune = Fortune.createNew();
						dbFortune.setText(result[0]);
						dbFortune.setSource(result[1]);
						dbFortune.commit();
						LOGGER.info("New fortune: {}", result[0]);
					}
//				}
				nextFortune = java.lang.System.currentTimeMillis() + FORTUNE_MILLIS;
			}
		}
		if (dbFortune == null) {
			dbFortune = Fortune.getRandomFortune();
		}
		fortune = buildSerialFortune(dbFortune);
		return fortune;
	}

    private static SerialFortune buildSerialFortune(Fortune dbFortune) {
    	SerialFortune fortune;
    	String source;
    	
    	SharedUtils.checkNotNull(dbFortune);
		fortune = new SerialFortune();
		source = dbFortune.getSource();
		if (source == null) {
			fortune.setSource("");
		} else {
			fortune.setSource(source);
		}
		fortune.setText(dbFortune.getText());
    	return fortune;
    }

    public static void main(String[] args) {
    	SerialFortune fortune;
    	
    	fortune = getFortune();
    	System.out.println("FORTUNE:");
    	System.out.println(fortune.getText());
    	System.out.print("\t- ");
    	System.out.println(fortune.getSource());
    }
}
