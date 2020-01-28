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

package com.hulles.alixia.central;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.Alixia;
import com.hulles.alixia.alpha.AlphaRoom;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.bravo.BravoRoom;
import com.hulles.alixia.charlie.CharlieRoom;
import com.hulles.alixia.delta.DeltaRoom;
import com.hulles.alixia.echo.EchoRoom;
import com.hulles.alixia.foxtrot.FoxtrotRoom;
import com.hulles.alixia.golf.GolfRoom;
import com.hulles.alixia.hotel.HotelRoom;
import com.hulles.alixia.house.UrHouse;
import com.hulles.alixia.india.IndiaRoom;
import com.hulles.alixia.juliet.JulietRoom;
import com.hulles.alixia.kilo.KiloRoom;
import com.hulles.alixia.lima.LimaRoom;
import com.hulles.alixia.mike.MikeRoom;
import com.hulles.alixia.nodeserver.pages.NodeWebServer;
import com.hulles.alixia.november.NovemberRoom;
import com.hulles.alixia.oscar.OscarRoom;
import com.hulles.alixia.overmind.OvermindRoom;
import com.hulles.alixia.papa.PapaRoom;
import com.hulles.alixia.quebec.QuebecRoom;
import com.hulles.alixia.romeo.RomeoRoom;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.sierra.SierraRoom;
import com.hulles.alixia.stationserver.StationServer;
import com.hulles.alixia.tracker.TrackerRoom;

/**
 * AlixiaCentral is a simple class with a main method to start up all the 
 * various rooms and run Alixia.
 * 
 * @author hulles
 */
public class AlixiaCentral {   
	private final static Logger LOGGER = LoggerFactory.getLogger("Alixia.AlixiaCentral");
	private static Options options;
	
    private static void setupOptions() {
        
        options = new Options();
        options.addOption("n", "noprompt", false, "do not send timed prompts to clients");
        options.addOption("o", "orphans", false, "display unimplemented sememes");
        options.addOption("h", "help", false, "show help");
    }
	
	private static void waitForKey() {
		
		System.out.println("Hit a key ");
		try {
			System.in.read();
		} catch (IOException e) {
			throw new AlixiaException("AlixiaRunner: IO error reading key", e);
		}
	}

	@SuppressWarnings("null")
	public static void main(String[] args) {
 		boolean noprompt = false;
        Boolean showOrphans = false;
		CommandLineParser parser;
        CommandLine commandLine;
        HelpFormatter formatter;
		List<UrHouse> houses;
		List<UrRoom> rooms;
		
 		setupOptions();
 		
        parser = new DefaultParser();
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
        	commandLine = null;
            LOGGER.error("Error parsing command line", ex);
            System.exit(1);
        }
        
        if (commandLine.hasOption("h")) {
            formatter = new HelpFormatter();
            formatter.printHelp( "AlixiaCentral", options, true);
            System.exit(0);
        }
        if (commandLine.hasOption("n")) {
        	noprompt = true;
        }
        if (commandLine.hasOption("o")) {
        	showOrphans = true;
        }

		// load houses
        houses = new ArrayList<>(2);
        houses.add(new StationServer(noprompt));
        houses.add(new NodeWebServer(noprompt));
        
		// a minimal configuration consists of Overmind, Alpha and Charlie rooms if noprompt;
        //    add India and/or Mike if you want prompts
		
		// load rooms
        rooms = new ArrayList<>(24);
        rooms.add(new TrackerRoom());
		rooms.add(new OvermindRoom());
//		rooms.add(new QARoom());
		rooms.add(new AlphaRoom());
		rooms.add(new BravoRoom());
		rooms.add(new CharlieRoom());
		rooms.add(new DeltaRoom());
        rooms.add(new EchoRoom());
        rooms.add(new FoxtrotRoom());
        rooms.add(new GolfRoom());
        rooms.add(new HotelRoom());
        rooms.add(new IndiaRoom());
        rooms.add(new JulietRoom());
        rooms.add(new KiloRoom());
        rooms.add(new LimaRoom());
        rooms.add(new MikeRoom());
        rooms.add(new NovemberRoom());
        rooms.add(new OscarRoom());
        rooms.add(new PapaRoom());
        rooms.add(new QuebecRoom());
        rooms.add(new RomeoRoom());
        rooms.add(new SierraRoom());

		try (Alixia alixia = new Alixia(houses, rooms, showOrphans)) {
            
			waitForKey();
			LOGGER.debug("AlixiaCentral: received shutdown key");
		}
		LOGGER.debug("AlixiaCentral: after try");
	}
    
}
