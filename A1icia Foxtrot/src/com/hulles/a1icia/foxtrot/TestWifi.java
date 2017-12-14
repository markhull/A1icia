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
package com.hulles.a1icia.foxtrot;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.hulles.a1icia.foxtrot.monitor.LanScanner;

public class TestWifi {
	
	@SuppressWarnings("null")
	public static void main4(String[] args) {
		String local1 = null;
		String local2 = null;
		byte[] local3array = null;
		String local4 = null;
		InetAddress[] addresses = null;
		
		try {
			local1 = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("getLocalHost().toString() => " + local1);
		
		try {
			local2 = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("getLocalHost().getHostAddress() => " + local2);
		
		try {
			local3array = InetAddress.getLocalHost().getAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("getLocalHost().getAddress() (byte[]) =>" + local3array.toString());
		
		try {
			local4 = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("getLocalHost().getAddress().getCanonicalHostName() => " + local4);
		
		try {
			System.out.println("isLoopback => " + InetAddress.getLocalHost().isLoopbackAddress());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			addresses = InetAddress.getAllByName("betty");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (InetAddress ia : addresses) {
			System.out.println(ia.toString());
		}
	}
	public static void main5(String args[]) throws SocketException {
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			displayInterfaceInformation(netint);
		}
	}

	private static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
		System.out.printf("Display name: %s%n", netint.getDisplayName());
		System.out.printf("Name: %s%n", netint.getName());
		Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		for (InetAddress inetAddress : Collections.list(inetAddresses)) {
			System.out.printf("InetAddress: %s%n", inetAddress);
		}

		System.out.printf("Parent: %s%n", netint.getParent());
		System.out.printf("Up? %s%n", netint.isUp());
		System.out.printf("Loopback? %s%n", netint.isLoopback());
		System.out.printf("PointToPoint? %s%n", netint.isPointToPoint());
		System.out.printf("Supports multicast? %s%n", netint.supportsMulticast());
		System.out.printf("Virtual? %s%n", netint.isVirtual());
		System.out.printf("Hardware address: %s%n", Arrays.toString(netint.getHardwareAddress()));
		System.out.printf("MTU: %s%n", netint.getMTU());

		List<InterfaceAddress> interfaceAddresses = netint.getInterfaceAddresses();
		for (InterfaceAddress addr : interfaceAddresses) {
			System.out.printf("InterfaceAddress: %s%n", addr.getAddress());
		}
		System.out.printf("%n");
		Enumeration<NetworkInterface> subInterfaces = netint.getSubInterfaces();
		for (NetworkInterface networkInterface : Collections.list(subInterfaces)) {
			System.out.printf("%nSubInterface%n");
			displayInterfaceInformation(networkInterface);
		}
		System.out.printf("%n");
	}
	
	public static void main(String[] args) throws IOException {
//		main2(args);
//		main3(args);
		main5(args);
		System.out.println();
		System.out.println();
		LanScanner.getLanHosts();
		System.out.println();
		System.out.println();
		NetworkAddress.getAddresses();
		
	}
}
