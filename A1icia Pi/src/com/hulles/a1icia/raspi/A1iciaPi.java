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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.a1icia.raspi;

import java.util.ResourceBundle;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.remote.Station;

/**
 *
 * @author hulles
 */
public final class A1iciaPi  {
	private static final String BUNDLE_NAME = "com.hulles.a1icia.raspi.Version";
	private final static String USAGE = "usage: java -jar A1iciaPi.jar PITYPE [--host=HOST] [--port=PORT] [--daemon]\n\twhere PITYPE = '--console' or '--mirror', HOST = IP address, and PORT = port number";
	private static PiConsole cli;
	private static MagicMirrorConsole mirror;
	
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
		WakeUpCall caller;
		PiType piType = PiType.CONSOLE;
		String host;
		String portStr;
		Integer port;
		Station station;
		Boolean daemon;
		
		station = Station.getInstance();
		station.ensureStationExists();
		host = station.getCentralHost();
		port = station.getCentralPort();
		daemon = false;
		for (String arg : args) {
			if (arg.startsWith("--host=")) {
				host = arg.substring(7);
			} else if (arg.startsWith("--port=")) {
				portStr = arg.substring(7);
				port = Integer.parseInt(portStr);
			} else if (arg.equals("--console")) {
		        piType = PiType.CONSOLE;
		    } else if (arg.equals("--mirror")) {
		        piType = PiType.MIRROR;
			} else if (arg.equals("--daemon")) {
				daemon = true;
			} else {
				System.out.println(USAGE);
				System.exit(1);
			}
		}
		System.out.println(getVersionString());
		System.out.println(A1iciaConstants.getA1iciasWelcome());
		System.out.println();
		switch (piType) {
			case CONSOLE:
				try (HardwareLayer hardwareLayer = new HardwareLayer()) {
					cli = new PiConsole(host, port, daemon, hardwareLayer);
					caller = new WakeUpCall(cli);
					hardwareLayer.setWakeUpCall(caller);
					cli.startAsync();
					cli.awaitTerminated();
				}
				break;
			case MIRROR:
				try (HardwareLayerMirror hardwareLayer = new HardwareLayerMirror()) {
					mirror = new MagicMirrorConsole(host, port, daemon, hardwareLayer);
					caller = new WakeUpCall(mirror);
					hardwareLayer.setWakeUpCall(caller);
					mirror.startAsync();
					mirror.awaitTerminated();
				}
				break;
			default:
				System.out.println(USAGE);
				System.exit(1);
		} 
    }
    
    private enum PiType {
    	CONSOLE,
    	MIRROR
    }
}
