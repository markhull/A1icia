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
package com.hulles.alixia.foxtrot;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.tools.AlixiaUtils;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkAddress {
	private final static Logger LOGGER = Logger.getLogger("AlixiaFoxtrot.NetworkAddress");
	private final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();

	public static void getAddresses() {
		String prettyMac;
		InetAddress lanIp;
		String ipAddress;
		Enumeration<NetworkInterface> net;
		NetworkInterface element;
		Enumeration<InetAddress> addresses;
		InetAddress ip;
		byte[] hardwareAddress;
		
		try {
			net = NetworkInterface.getNetworkInterfaces();
			while (net.hasMoreElements()) {
				element = net.nextElement();
				LOGGER.log(LOGLEVEL, "Element: {0}", element);
				hardwareAddress = element.getHardwareAddress();
				// hardwareAddress is null for loopback, AFAIK
				if (hardwareAddress != null && !isVMMac(hardwareAddress)) {
					addresses = element.getInetAddresses();
					while (addresses.hasMoreElements()) {
						ip = addresses.nextElement();
                        if (ip == null) {
                            AlixiaUtils.error("NetworkAddress: Null IP");
                        } else {
                            LOGGER.log(LOGLEVEL, "IP: {0}", ip);
                            if (ip instanceof Inet4Address) {
                                LOGGER.log(LOGLEVEL, "Is IPV4");
                            } else if (ip instanceof Inet6Address){
                                LOGGER.log(LOGLEVEL, "Is IPV6");
                                continue;
                            }
                            if (ip.isSiteLocalAddress()) {
                                LOGGER.log(LOGLEVEL, "Site-Local");
                                ipAddress = ip.getHostAddress();
                                LOGGER.log(LOGLEVEL, "IP host address: {0}", ipAddress);
                                lanIp = InetAddress.getByName(ipAddress);
                                prettyMac = getMacAddress(lanIp);
                                LOGGER.log(LOGLEVEL, "MAC: {0}", prettyMac);
                            }
                        }
					}
				}
			}
		} catch (UnknownHostException ex) {
            throw new AlixiaException("NetworkAddress: Unknown host", ex);
		} catch (SocketException ex) {
            throw new AlixiaException("NetworkAddress: Socket exception", ex);
		}
	}

	private static String getMacAddress(InetAddress iNet) {
		String address = null;
		NetworkInterface network;
		byte[] mac;
		StringBuilder sb;
		
		try {
			network = NetworkInterface.getByInetAddress(iNet);
			if (network == null) {
				return null;
			}
			mac = network.getHardwareAddress();
			sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
			}
			address = sb.toString();
		} catch (SocketException ex) {
            throw new AlixiaException("NetworkAddress: Socket exception", ex);
		}
		return address;
	}

	private static boolean isVMMac(byte[] mac) {
		if(null == mac) {
            return false;
        }
		byte invalidMacs[][] = {
				{0x00, 0x05, 0x69},             //VMWare
				{0x00, 0x1C, 0x14},             //VMWare
				{0x00, 0x0C, 0x29},             //VMWare
				{0x00, 0x50, 0x56},             //VMWare
				{0x08, 0x00, 0x27},             //Virtualbox
				{0x0A, 0x00, 0x27},             //Virtualbox
				{0x00, 0x03, (byte)0xFF},       //Virtual-PC
				{0x00, 0x15, 0x5D}              //Hyper-V
		};
		for (byte[] invalid: invalidMacs){
			if (invalid[0] == mac[0] && invalid[1] == mac[1] && invalid[2] == mac[2]) {
                return true;
            }
		}
		return false;
	}

}

