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
package com.hulles.a1icia.config.station;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.jebus.JebusApiBible;
import com.hulles.a1icia.api.jebus.JebusApiHub;
import com.hulles.a1icia.api.jebus.JebusPool;
import com.hulles.a1icia.api.remote.Station;
import com.hulles.a1icia.api.shared.A1iciaAPIException;
import com.hulles.a1icia.api.shared.Serialization;
import com.hulles.a1icia.api.shared.SharedUtils;

import redis.clients.jedis.Jedis;

public class ImportExport {
	private final static Logger LOGGER = Logger.getLogger("A1iciaStationInstaller.ImportExport");
	private final static Level LOGLEVEL = LOGGER.getParent().getLevel();
//	private final static Level LOGLEVEL = Level.INFO;
	private final static Charset CHARSET = Charset.forName("UTF-8");
	
	public ImportExport() {
		
	}
		
	private static void exportStation(Path path) throws IOException {
		Station station;
		Map<String, String> stringMap;
		String key;
		String value;
		
		SharedUtils.checkNotNull(path);
		station = Station.getInstance();
		stringMap = station.getKeyMap();
		try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
			for (Entry<String, String> entry : stringMap.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				writer.write(key);
				writer.write("=");
				writer.write(value);
				writer.newLine();
			}
		}
		System.out.println("Exported " + stringMap.size() + " key/value pairs from local Station");
	}
	
	private static void importStation(Path path) throws IOException {
		Station station;
		Map<String, String> stringMap;
		String line;
		String[] keyValue;
		
		SharedUtils.checkNotNull(path);
		station = new Station();
		stringMap = new HashMap<>();
		try (BufferedReader reader = Files.newBufferedReader(path, CHARSET)) {
		    line = null;
		    while ((line = reader.readLine()) != null) {
		    	if (line.isEmpty()) {
		    		continue;
		    	}
		    	if (line.startsWith("#")) {
		    		// it's a comment
		    		continue;
		    	}
		    	keyValue = line.split("=", 2);
		    	LOGGER.log(LOGLEVEL, "IMPORT: Line is " + line + ", values = " + keyValue.length);
		    	LOGGER.log(LOGLEVEL, "IMPORT: Key is " + keyValue[0] + ", Value is " + keyValue[1]);
		    	if (keyValue[0].equals("STATIONID") && (keyValue.length == 1 || keyValue[1].isEmpty())) {
		    		LOGGER.log(LOGLEVEL, "Generating UUID");
		    		// for "STATIONID=", we can help out by generating our own UUID
		    		stringMap.put(keyValue[0], UUID.randomUUID().toString());
		    	} else {
		    		stringMap.put(keyValue[0], keyValue[1]);
		    	}
		    }
		}
		if (station.setKeyMap(stringMap)) {
			storeStation(station);
			System.out.println("Successfully imported " + stringMap.size() + 
					" key/value pairs into local Station");
		} else {
			System.err.println("Error(s) while importing key/value pairs into local Station");
			System.err.println("Station not updated");
		}
	}
	
	@SuppressWarnings("resource")
	private static void storeStation(Station station) {
		JebusPool jebusPool;
		byte[] stationBytes;
		byte[] stationKeyBytes;

		SharedUtils.checkNotNull(station);
		jebusPool = JebusApiHub.getJebusLocal();
		stationKeyBytes = JebusApiBible.getA1iciaStationKey(jebusPool).getBytes();
		try {
			stationBytes = Serialization.serialize(station);
		} catch (IOException e) {
			throw new A1iciaAPIException("ImportExport: can't serialize station", e);
		}
		try (Jedis jebus = jebusPool.getResource()) {
			jebus.set(stationKeyBytes, stationBytes);		
		}
	}
	
	static void importExportStation() {
		InputStreamReader stdIn;
		Boolean exportStation;
		Boolean importStation;
		Path path;
		
		stdIn = new InputStreamReader(System.in);
		try (BufferedReader reader = new BufferedReader(stdIn)) {
			while ((exportStation = getYN(reader,"Do you want to export the local Station? [yN]: ")) == null) {}
			if (exportStation) {
				path = getPath(reader, true);
				exportStation(path);
				return;
			}
			while ((importStation = getYN(reader,"Do you want to import the local Station? [yN]: ")) == null) {}
			if (importStation) {
				path = getPath(reader, false);
				importStation(path);
				return;
			}
		} catch (IOException e) {
			System.err.println("System error: I/O error, exiting");
			System.exit(1);
		}
		
	}
	
	private static Path getPath(BufferedReader reader, boolean writing) throws IOException {
		String fn;
		Path path = null;
		Boolean overwrite;
		
		fn = null;
		while (fn == null) {
			System.out.print("Enter the file name ");
			System.out.print(writing ? "to export to: " : "to import from: ");
			fn = reader.readLine();
			if (fn == null) {
				continue;
			}
			path = Paths.get(fn);
			if (writing && Files.exists(path)) {
				while ((overwrite = getYN(reader,"That file exists, do you want to overwrite it? [yN]: ")
						== null)) {}
				if (!overwrite) {
					fn = null;
					continue;
				}
			}
			if (!writing && !Files.exists(path)) {
				System.out.println("That file doesn't exist");
				fn = null;
				continue;
			}
		}
		return path;
	}
	
	private static Boolean getYN(BufferedReader reader, String prompt) throws IOException {
		String input;
		String ucInput;
		
		System.out.print(prompt);
		input = reader.readLine();
		System.out.println();
		if (input == null) {
			ucInput = "N";
		} else {
			ucInput = input.toUpperCase();
		}
		if (ucInput.startsWith("Y")) {
			return true;
		}
		if (ucInput.startsWith("N")) {
			return false;
		}
		System.out.println("Please type Y or N, or hit Enter for default");
		return null;
	}

}
