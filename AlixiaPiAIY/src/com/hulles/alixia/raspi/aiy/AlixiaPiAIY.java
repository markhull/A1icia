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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.alixia.raspi.aiy;


import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.remote.Station;
import com.hulles.alixia.api.tools.AlixiaVersion;
import com.hulles.alixia.cli.AlixiaCLIConsole.ConsoleType;

/**
 *
 * @author hulles
 */
public final class AlixiaPiAIY  {
    private final static Logger LOGGER = LoggerFactory.getLogger(AlixiaPiAIY.class);
	private static final String BUNDLE_NAME = "com.hulles.alixia.raspi.aiy.Version";
//	private final static String USAGE = "usage: java -jar AlixiaPiAIY.jar [--host=HOST] [--port=PORT] [--daemon] \n\twhere HOST = IP address and PORT = port number";
	private static PiConsole zero2;
    private static Options options;
	
    private static void setupOptions() {
        Option host;
        Option port;
        Option daemon;
        
        host = Option.builder()
                .argName("H")
                .longOpt("host")
                .required(false)
                .hasArg()
                .desc("the domain name or IP of the Alixia station server")
                .build();
        port = Option.builder()
                .argName("P")
                .required(false)
                .longOpt("port")
                .hasArg()
                .desc("the port number of the Alixia station server")
                .build();
        daemon = Option.builder()
                .argName("d")
                .required(false)
                .longOpt("daemon")
                .hasArg(false)
                .desc("run as a daemon process without user interaction")
                .build();
        
        options = new Options();
        options.addOption(host);
        options.addOption(port);
        options.addOption(daemon);
        options.addOption("h", "help", false, "show help");
    }
   
    public static void main(String[] args) {
		String host;
		String portStr;
		Integer port;
		Station station;
		ConsoleType whichConsole;
        String version;
		CommandLineParser parser;
        CommandLine commandLine;
        HelpFormatter formatter;
 		ResourceBundle bundle;
        
        setupOptions();
        parser = new DefaultParser();
        try {
            commandLine = parser.parse(options, args);
        } catch (org.apache.commons.cli.ParseException ex) {
            LOGGER.error("Error parsing command line {}", ex.getMessage());
            return;
        }
        if (commandLine.hasOption("h")) {
            formatter = new HelpFormatter();
            formatter.printHelp( "AlixiaPiAIY", options, true);
            return;
        }
		station = Station.getInstance();
		station.ensureStationExists();
        if (commandLine.hasOption("H")) {
            host = commandLine.getOptionValue("H");
        } else {
            host = station.getCentralHost();
        }
		if (commandLine.hasOption("P")) {
            portStr = commandLine.getOptionValue("P");
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException ex) {
                LOGGER.error("Port number must be an integer");
                return;
            }
        } else {
    		port = station.getCentralPort();
        }
		
		whichConsole = ConsoleType.DEFAULT;
        if (commandLine.hasOption("d")) {
            whichConsole = ConsoleType.DAEMON;
        }
       
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        version = AlixiaVersion.getVersionString(bundle);
		System.out.println(version);
		System.out.println(AlixiaConstants.getAlixiasWelcome());
		System.out.println();
		try (HardwareLayer hardwareLayer = new HardwareLayer()) {
			zero2 = new PiConsole(host, port, whichConsole, hardwareLayer);
			hardwareLayer.setConsole(zero2);
			hardwareLayer.setLED("blink_blue_LED"); // for now
			zero2.startAsync();
			zero2.awaitTerminated();
		}
    }

}
