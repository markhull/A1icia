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

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.remote.Station;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.shared.SharedUtils.PortCheck;
import com.hulles.alixia.cli.AlixiaCLIConsole.ConsoleType;

public class AlixiaCLI implements Closeable {
	private static final String BUNDLE_NAME = "com.hulles.alixia.cli.Version";
	private final static String USAGE = "usage: java -jar AlixiaCLI.jar [--help] [--host=HOST] [--port=PORT] [--daemon|--defaultconsole|--javaconsole|--sysconsole]\n\twhere HOST = IP address, and PORT = port number";
	private static AlixiaCLIConsole cli;
	
	AlixiaCLI() {

		SharedUtils.exitIfAlreadyRunning(PortCheck.ALIXIA_CLI);
	}
	
    @Override
	public void close() {
    	
    	cli.stopAsync();
    	cli.awaitTerminated();
    }
	
	public static String getVersionString() {
		ResourceBundle bundle;
		StringBuilder sb;
		String value;
		
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
		sb = new StringBuilder();
		value = bundle.getString("Name");
		sb.append(value);
		sb.append(" \"");
		value = bundle.getString("Build-Title");
		sb.append(value);
		sb.append("\", Version ");
		value = bundle.getString("Build-Version");
		sb.append(value);
		sb.append(", Build #");
		value = bundle.getString("Build-Number");
		sb.append(value);
		sb.append(" on ");
		value = bundle.getString("Build-Date");
		sb.append(value);
		return sb.toString();
	}
	
	public static void main(String[] args) {
		String host;
		String portStr;
		Integer port;
		Station station;
		ConsoleType whichConsole;
		
		station = Station.getInstance();
		station.ensureStationExists();
		host = station.getCentralHost();
		port = station.getCentralPort();
		whichConsole = ConsoleType.DEFAULT;
		for (String arg : args) {
			if (arg.startsWith("--host=")) {
				host = arg.substring(7);
			} else if (arg.startsWith("--port=")) {
				portStr = arg.substring(7);
				port = Integer.parseInt(portStr);
			} else if (arg.equals("--daemon")) {
				whichConsole = ConsoleType.DAEMON;
			} else if (arg.equals("--javaconsole")) {
				whichConsole = ConsoleType.JAVACONSOLE;
			} else if (arg.equals("--sysconsole")) {
				whichConsole = ConsoleType.STANDARDIO;
			} else if (arg.equals("--defaultconsole")) {
				whichConsole = ConsoleType.DEFAULT;
			} else if (arg.equals("--help")) {
				System.out.println(USAGE);
				System.exit(0);
			} else {
				System.err.println("Bad argument: " + arg);
				System.err.println(USAGE);
				System.exit(1);
			}
		}
		System.out.println(getVersionString());
		System.out.println(AlixiaConstants.getAlixiasWelcome());
		System.out.println();
		cli = new AlixiaCLIConsole(host, port, whichConsole);
		cli.startAsync();
		cli.awaitTerminated();
	}
}
