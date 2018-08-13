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
package com.hulles.a1icia.kilo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.hulles.a1icia.api.shared.PurdahKeys;
import com.hulles.a1icia.api.shared.PurdahKeys.PurdahKey;
import com.hulles.a1icia.tools.ExternalAperture;

public class KiloLocation {
	
	public static KiloLocationAction getLocation() {
		PurdahKeys purdahKeys;
		String locationJSON;
		JsonObject locObj;
		KiloLocationAction action = null;
		String locStr;
		String[] locs;
		Float lat;
		Float lon;
		
		action = new KiloLocationAction();
		purdahKeys = PurdahKeys.getInstance();
		locationJSON = ExternalAperture.getCurrentLocation(purdahKeys.getPurdahKey(PurdahKey.IPINFOKEY));
		try (BufferedReader reader = new BufferedReader(new StringReader(locationJSON))) {
			try (JsonReader jsonReader = Json.createReader(reader)) {
				locObj = jsonReader.readObject();
				action.setLocationIP(locObj.getString("ip"));
				action.setCity(locObj.getString("city"));
				action.setRegion(locObj.getString("region"));
				action.setCountry(locObj.getString("country"));
				locStr = locObj.getString("loc");
				locs = locStr.split(",");
				assert locs.length == 2: "Bad loc string";
				lat = Float.parseFloat(locs[0]);
				lon = Float.parseFloat(locs[1]);
				action.setLatitude(lat);
				action.setLongitude(lon);
				action.setOrganization(locObj.getString("org"));
				action.setPostalCode(locObj.getString("postal"));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return action;
	}
/*
{
  "ip": "127.0.0.1",
  "city": "Humboldt",
  "region": "Iowa",
  "country": "US",
  "loc": "42.7411,-94.1873",
  "org": "AS40837 Goldfield Telecom, LLC",
  "postal": "50548"
} 
 */
}
