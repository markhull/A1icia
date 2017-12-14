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
package com.hulles.a1icia.foxtrot.monitor;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.tools.A1iciaTimer;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * Class that scans ip local network. Any address in the range 192.168.xxx.xxx is
 * a private (aka site local) IP address. The same applies to 10.xxx.xxx.xxx
 * addresses, and 172.16.xxx.xxx through 172.31.xxx.xxx. Addresses in the range
 * 169.254.xxx.xxx are link local IP addresses. These are reserved for use on a
 * single network segment. Addresses in the range 224.xxx.xxx.xxx through
 * 239.xxx.xxx.xxx are multicast addresses. The address 255.255.255.255 is the
 * broadcast address. Anything else should be a valid public point-to-point IPv4
 * address.
 * 
 * @author germo_000
 *
 * Adapted the original code to increase(!) the number of threads based on trials,
 * [actually just changed it to cached, and it's even slightly faster than fixed(8000)]
 * scan the two common site-local nets, and make it pretty for A1icia. She likes pretty.
 * 
 */

public final class LanScanner {
//	private final static int THREADCOUNT = 8000;
	private final String submask;
	private final ExecutorService scanExecutor;
	final Set<String> liveHosts;

	LanScanner(String ip) {
		String[] ipBytes;
		
		A1iciaUtils.checkNotNull(ip);
		liveHosts = new HashSet<>();		
//		scanExecutor = Executors.newFixedThreadPool(THREADCOUNT);
		scanExecutor = Executors.newCachedThreadPool();
		
		ipBytes = ip.split("\\.");

		if (ipBytes.length==4) {
			this.submask = ipBytes[0] + "." + ipBytes[1];
		} else {
			throw new A1iciaException();
		}
		System.out.println("Scan being performed on submask : " + submask);
	}

	private Set<String> getLiveHosts() {
		List<Future<ScanResult>> futures;
		String ip;
		ScanResult scanResult;
		
		futures = new ArrayList<>();
		for (int firstByte = 0; firstByte < 20; firstByte++) {
			for (int secondByte = 0; secondByte < 255; secondByte++) {
				ip = this.submask + "." + firstByte + "." + secondByte;
				futures.add(hostScan(ip));
			}
		}

		scanExecutor.shutdown();

		try {
			for (final Future<ScanResult> f : futures) {
				scanResult = f.get();
				if (scanResult.isOpen()) {
					this.liveHosts.add(scanResult.getEntity());
				}
			}
		} catch (ExecutionException | InterruptedException e) {
			throw new A1iciaException("LanScanner: scan error", e);
		}
		return liveHosts;
	}

	private Future<ScanResult> hostScan(final String ipScanned) {
		
		return scanExecutor.submit(new Callable<ScanResult>() {
			@Override
			public ScanResult call() {
				boolean isOpen;
				InetAddress ipAdress;
				
				isOpen = false;
				try {
					ipAdress = InetAddress.getByName(ipScanned);
					if (ipAdress.isReachable(2500)) {
						liveHosts.add(ipScanned);
						isOpen = true;
					}
				} catch (Exception e) {
					throw new A1iciaException("LanScanner: error scanning host " + ipScanned, e);
				}
				return new ScanResult(ipScanned, isOpen);
			}
		});
	}

	public static Set<String> getLanHosts() {
		
		return runTwoNetworks();
	}
	
	private static Set<String> runTwoNetworks() {
		Set<String> hosts;
		Set<String> moreHosts;
		
		// scan the common local networks, for now
		hosts = scanLan("10.0.0.1");
		moreHosts = scanLan("192.168.0.1");
		hosts.addAll(moreHosts);
		return hosts;
	}

	private static Set<String> scanLan(String ip) {
		LanScanner lanScanner;
		Set<String> upHosts;
		
		A1iciaTimer.startTimer(ip);
		lanScanner = new LanScanner(ip);
		upHosts = lanScanner.getLiveHosts();
		A1iciaTimer.stopTimer(ip);
		
		for (String host : upHosts) {
			System.out.println("Host " + host + " is up");
		}
		return upHosts;
	}
	
	private final class ScanResult {
		private final String host;
		private final boolean isOpen;

		public ScanResult(String host, boolean isOpen) {
			this.host = host;
			this.isOpen = isOpen;
		}

		public String getEntity() {
			return host;
		}

		public boolean isOpen() {
			return isOpen;
		}
	}
	
}
