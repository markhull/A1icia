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
package com.hulles.alixia.foxtrot.monitor;

import java.io.IOException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaTimer;

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
 * scan the two common site-local nets, and make it pretty for Alixia. She likes pretty.
 * 
 * @author hulles
 * 
 */

public final class LanScanner {
	final static Logger LOGGER = LoggerFactory.getLogger("AlixiaFoxtrot.LanScanner");
//	private final static int THREADCOUNT = 8000;
	private final String submask;
	private final ExecutorService scanExecutor;
	final Set<String> liveHosts;

	/**
	 * We use a thread cache here for the Executor, which provides awesome performance. Don't believe
	 * it? Use a FixedThreadPool and set the commented-out THREADCOUNT above to the number of 
	 * cores in your PC + 1 (e.g.) and run it again.
	 * 
	 * @see https://markhull.github.io/2017/12/21/Divergent.html
	 * @param ip
	 */
	LanScanner(String ip) {
		String[] ipBytes;
		
		SharedUtils.checkNotNull(ip);
		liveHosts = new HashSet<>();		
//		scanExecutor = Executors.newFixedThreadPool(THREADCOUNT);
		scanExecutor = Executors.newCachedThreadPool();
		
		ipBytes = ip.split("\\.");

		if (ipBytes.length==4) {
			this.submask = ipBytes[0] + "." + ipBytes[1];
		} else {
			throw new AlixiaException();
		}
		LOGGER.debug("Scan being performed on submask : {}", submask);
	}

	/**
	 * Scan the specified subnet (10.0. e.g.) for live hosts
	 * @return
	 */
	private Set<String> getLiveHosts() {
		List<Future<ScanResult>> futures;
		String ip;
		ScanResult scanResult;
		
		futures = new ArrayList<>();
		for (int thirdByte = 0; thirdByte < 20; thirdByte++) {
//		for (int thirdByte = 0; thirdByte < 255; thirdByte++) {
			for (int fourthByte = 0; fourthByte < 255; fourthByte++) {
				ip = this.submask + "." + thirdByte + "." + fourthByte;
				futures.add(hostScan(ip));
			}
		}

		scanExecutor.shutdown();

		try {
			for (final Future<ScanResult> f : futures) {
                // future.get() blocks until the result is ready
				scanResult = f.get();
				if (scanResult.isOpen()) {
					this.liveHosts.add(scanResult.getEntity());
				}
			}
		} catch (ExecutionException | InterruptedException e) {
			throw new AlixiaException("LanScanner: scan error", e);
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
				} catch (IOException e) {
					throw new AlixiaException("LanScanner: error scanning host " + ipScanned, e);
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
		
		AlixiaTimer.startTimer(ip);
		lanScanner = new LanScanner(ip);
		upHosts = lanScanner.getLiveHosts();
		AlixiaTimer.stopTimer(ip);
		
		for (String host : upHosts) {
			LOGGER.info("Host {} is up", host);
		}
		return upHosts;
	}
	
	private final class ScanResult {
		private final String host;
		private final boolean isOpen;

		ScanResult(String host, boolean isOpen) {
			this.host = host;
			this.isOpen = isOpen;
		}

		String getEntity() {
			return host;
		}

		boolean isOpen() {
			return isOpen;
		}
	}
	
}
