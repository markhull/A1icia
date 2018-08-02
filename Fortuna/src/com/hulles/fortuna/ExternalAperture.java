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
package com.hulles.fortuna;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

final public class ExternalAperture {
//	private final static Logger LOGGER = Logger.getLogger("Fortuna.ExternalAperture");
//	private final static Level LOGLEVEL = Level.INFO;
	
	private ExternalAperture() {
	}
	
	static String[] getMOTD() {
		URL url;
		JsonObject motdObj;
		String message;
		String source;
		String[] result;
	    
		/*    SAMPLE RETURN:
{
    "text" : "By the way, we rank tenth among the industrialized world in broadband technology 
    	and its availability.  That's not good enough for America.  Tenth is ten spots too 
    	low as far as I'm concerned.",
    "source" : "George W. Bush"
}
		*/
		try {
			url = new URL("http://services.packetizer.com/motd/?f=json");
		} catch (MalformedURLException ex) {
			System.err.println("Bad URL in getMOTD");
			return null;
		}
		try (InputStream inStream = url.openStream()) { 
			try (JsonReader reader = Json.createReader(inStream)) {
				motdObj = reader.readObject();
				message = motdObj.getString("text");
				source = motdObj.getString("source");
			}
		} catch (IOException ex) {
			System.err.println("I/O Exception creating input stream in getMOTD");
			return null;
		}
		result = new String[2];
		result[0] = message;
		result[1] = source;
		return result;
	}

}
