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
package com.hulles.alixia.raspi;


import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.remote.Station;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.tools.AlixiaVersion;
import com.hulles.alixia.cli.AlixiaCLIConsole.ConsoleType;

/**
 *
 * @author hulles
 */
public final class AlixiaPi  {
    private final static Logger LOGGER = LoggerFactory.getLogger(AlixiaPi.class);
	private static final String BUNDLE_NAME = "com.hulles.alixia.raspi.Version";
//	private final static String USAGE = "usage: java -jar AlixiaPi.jar PITYPE [--help] [--host=HOST] [--port=PORT] [--daemon]\n\twhere PITYPE = '--console' or '--mirror', HOST = IP address, and PORT = port number";
	private static PiConsole cli;
	private static MagicMirrorConsole mirror;
    private static Options options;
	
    private static void setupOptions() {
        Option host;
        Option port;
        Option daemon;
        Option isConsole;
        Option isMirror;
        OptionGroup consoleGroup;
        
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
        
        
        isConsole = Option.builder()
                .argName("c")
                .required(false)
                .longOpt("console")
                .hasArg(false)
                .desc("run as a CLI console")
                .build();
        isMirror = Option.builder()
                .argName("m")
                .required(false)
                .longOpt("mirror")
                .hasArg(false)
                .desc("run as a \"Magic Mirror\"")
                .build();
        
        options = new Options();
        options.addOption(host);
        options.addOption(port);
        options.addOption(daemon);
        options.addOption("h", "help", false, "show help");
        consoleGroup = new OptionGroup();
        consoleGroup.isRequired();
        consoleGroup.addOption(isConsole);
        consoleGroup.addOption(isMirror);
    }
   
    public static void main(String[] args) {
		WakeUpCall caller;
		PiType piType = PiType.CONSOLE;
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
            formatter.printHelp( "AlixiaPi", options, true);
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
        
        if (commandLine.hasOption("c")) {
            piType = PiType.CONSOLE;
        } else if (commandLine.hasOption("m")) {
            piType = PiType.MIRROR;
        }
        
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        version = AlixiaVersion.getVersionString(bundle);
		System.out.println(version);
		System.out.println(AlixiaConstants.getAlixiasWelcome());
		System.out.println();
		switch (piType) {
			case CONSOLE:
				try (HardwareLayer hardwareLayer = new HardwareLayer()) {
					cli = new PiConsole(host, port, whichConsole, hardwareLayer);
					caller = new WakeUpCall(cli);
					hardwareLayer.setWakeUpCall(caller);
					cli.startAsync();
					cli.awaitTerminated();
				}
				break;
			case MIRROR:
				try (HardwareLayerMirror hardwareLayer = new HardwareLayerMirror()) {
					mirror = new MagicMirrorConsole(host, port, hardwareLayer);
					caller = new WakeUpCall(mirror);
					hardwareLayer.setWakeUpCall(caller);
					mirror.startAsync();
					mirror.awaitTerminated();
				}
				break;
			default:
                throw new AlixiaException("System error: fell through switch in main, value is " + piType);
		} 
    }
    
    private enum PiType {
    	CONSOLE,
    	MIRROR
    }
}
