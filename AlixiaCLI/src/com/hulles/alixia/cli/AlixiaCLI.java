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
package com.hulles.alixia.cli;

import java.io.Closeable;
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
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.shared.SharedUtils.PortCheck;
import com.hulles.alixia.api.tools.AlixiaVersion;
import com.hulles.alixia.cli.AlixiaCLIConsole.ConsoleType;

public class AlixiaCLI implements Closeable {
    private final static Logger LOGGER = LoggerFactory.getLogger(AlixiaCLI.class);
	private static final String BUNDLE_NAME = "com.hulles.alixia.cli.Version";
	@SuppressWarnings("unused")
	private final static String USAGE = "usage: java -jar AlixiaCLI.jar [--help] [--host=HOST] [--port=PORT] [--daemon|--defaultconsole|--javaconsole|--sysconsole]\n\twhere HOST = IP address, and PORT = port number";
	private static AlixiaCLIConsole cli;
	private static Options options;
    
	AlixiaCLI() {

		SharedUtils.exitIfAlreadyRunning(PortCheck.ALIXIA_CLI);
	}
	
    @Override
	public void close() {
    	
    	cli.stopAsync();
    	cli.awaitTerminated();
    }
	
    private static void setupOptions() {
        Option host;
        Option port;
        Option daemon;
        Option javaConsole;
        Option sysConsole;
        Option defaultConsole;
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
        javaConsole = Option.builder()
                .argName("j")
                .required(false)
                .longOpt("javaconsole")
                .hasArg(false)
                .desc("run as a Java Console")
                .build();
        sysConsole = Option.builder()
                .argName("s")
                .required(false)
                .longOpt("sysconsole")
                .hasArg(false)
                .desc("run as a System.in console")
                .build();
        defaultConsole = Option.builder()
                .argName("c")
                .required(false)
                .longOpt("defaultconsole")
                .hasArg(false)
                .desc("run as a default console")
                .build();
        
        options = new Options();
        options.addOption(host);
        options.addOption(port);
        options.addOption("h", "help", false, "show help");
        consoleGroup = new OptionGroup();
        consoleGroup.addOption(daemon);
        consoleGroup.addOption(javaConsole);
        consoleGroup.addOption(sysConsole);
        consoleGroup.addOption(defaultConsole);
    }
 	
	public static void main(String[] args) {
		String host;
		String portStr;
		Integer port;
		Station station;
		ConsoleType whichConsole;
		CommandLineParser parser;
        CommandLine commandLine;
        HelpFormatter formatter;
        String version;
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
            formatter.printHelp( "AlixiaCLI", options, true);
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
        } else if (commandLine.hasOption("j")) {
            whichConsole = ConsoleType.JAVACONSOLE;
        } else if (commandLine.hasOption("s")) {
            whichConsole = ConsoleType.STANDARDIO;
        } else if (commandLine.hasOption("c")) {
            whichConsole = ConsoleType.DEFAULT;
        }
               
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        version = AlixiaVersion.getVersionString(bundle);
		System.out.println(version);
		System.out.println(AlixiaConstants.getAlixiasWelcome());
		System.out.println();
		cli = new AlixiaCLIConsole(host, port, whichConsole);
		cli.startAsync();
		cli.awaitTerminated();
	}
}
