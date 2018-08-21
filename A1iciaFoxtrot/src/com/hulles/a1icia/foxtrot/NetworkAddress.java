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
package com.hulles.a1icia.foxtrot;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class NetworkAddress {

	public static void getAddresses() {
		String prettyMac = null;
		InetAddress lanIp = null;
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
				System.out.println("Element: " + element);
				hardwareAddress = element.getHardwareAddress();
				// hardwareAddress is null for loopback, AFAIK
				if (hardwareAddress != null && !isVMMac(hardwareAddress)) {
					addresses = element.getInetAddresses();
					while (addresses.hasMoreElements()) {
						ip = addresses.nextElement();
						System.out.println("IP: " + ip);
						if (ip instanceof Inet4Address) {
							System.out.println("Is IPV4");
						} else if (ip instanceof Inet6Address){
							System.out.println("Is IPV6");
							continue;
						}
						if (ip.isSiteLocalAddress()) {
							System.out.println("Site-Local");
							ipAddress = ip.getHostAddress();
							System.out.println("IP host address: " + ipAddress);
							lanIp = InetAddress.getByName(ipAddress);
							prettyMac = getMacAddress(lanIp);
							System.out.println("MAC: " + prettyMac);
						}
						System.out.println();
					}
				}
				System.out.println();
			}
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		} catch (SocketException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
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
			ex.printStackTrace();
		}
		return address;
	}

	private static boolean isVMMac(byte[] mac) {
		if(null == mac) return false;
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
			if (invalid[0] == mac[0] && invalid[1] == mac[1] && invalid[2] == mac[2]) return true;
		}
		return false;
	}

}

