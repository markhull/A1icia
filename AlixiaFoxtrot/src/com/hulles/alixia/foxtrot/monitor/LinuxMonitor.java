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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.mariadb.jdbc.MariaDbDataSource;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.foxtrot.dummy.DummyDataSource;
import com.hulles.alixia.foxtrot.monitor.FoxtrotPhysicalState.FoxtrotFS;
import com.hulles.alixia.foxtrot.monitor.FoxtrotPhysicalState.NetworkDevice;
import com.hulles.alixia.foxtrot.monitor.FoxtrotPhysicalState.Processor;
import com.hulles.alixia.foxtrot.monitor.FoxtrotPhysicalState.SensorValue;

final public class LinuxMonitor {
	final static Logger LOGGER = Logger.getLogger("AlixiaFoxtrot.LinuxMonitor");
	final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
//	final static Level LOGLEVEL = Level.INFO;
	private static final int KB = 1024;
	private final FoxtrotPhysicalState foxtrotState;
    private static final Pattern DISTRIBUTION =
            Pattern.compile("DISTRIB_DESCRIPTION=\"(.*)\"$", Pattern.MULTILINE);
    private static final Pattern PRETTYNAME =
            Pattern.compile("PRETTY_NAME=\"(.*)\"$", Pattern.MULTILINE);
    private static final String APACHENAME = "Apache";
    private static final String APACHEURL = "http://localhost/server-status?auto";
    private static final String APACHEVERSION = "ServerVersion:";
    private static final String APACHEUPTIME = "ServerUptimeSeconds:";
    private static final String APACHETOTALACCESSES = "Total Accesses:";
    private static final String APACHETOTALKBYTES = "Total kBytes:";
    private static final String NGINXURL = "http://localhost/nginx_status";
    private static final String NGINXNAME = "nginx";
    private static final String NGINXACTIVE = "Active connections:";
    private static final String NGINXSERVER = "server accepts handled requests";
    private static final String NGINXREADING = "Reading:";
    private String databaseUser = null;
    private String databasePassword = null;
    private String databaseServer = null;
    private Integer databasePort= null;
    
    public LinuxMonitor() {
    	foxtrotState = new FoxtrotPhysicalState();
    	// get the one-time values (versions, cores, etc)
    	foxtrotState.setHostName(getHostName());
    	updateJava();
    	foxtrotState.setOSFlavor(getOSFlavor());
    	foxtrotState.setProcessors(getCPUs());
    }

	public void setDatabaseUser(String databaseUser) {
		
		SharedUtils.checkNotNull(databaseUser);
		this.databaseUser = databaseUser;
	}

	public void setDatabasePassword(String databasePassword) {
		
		SharedUtils.checkNotNull(databasePassword);
		this.databasePassword = databasePassword;
	}

	public void setDatabaseServer(String databaseServer) {
		
		SharedUtils.checkNotNull(databaseServer);
		this.databaseServer = databaseServer;
	}

	public void setDatabasePort(Integer databasePort) {
		
		SharedUtils.checkNotNull(databasePort);
		this.databasePort = databasePort;
	}

	private void updateVolatileValues() {
    	updateMemory();
    	updateUptimeInSeconds();
    	updateJVMStats();
    	updateSensors();
    	updateDiskSpace();
    	updateNetwork();
    	updateLanHosts();
    	updateDatabase();
    	if (!updateApacheStatus()) {
    		updateNginxStatus();
    	}
    }
    
    public FoxtrotPhysicalState getFoxtrotPhysicalState() {
    	updateVolatileValues();
    	return foxtrotState;
    }
    
    private void updateLanHosts() {
    	Set<String> hosts;
    	
    	hosts = LanScanner.getLanHosts();
    	foxtrotState.setLanHosts(hosts);
    }
    
    private void updateJava() {
    	
    	foxtrotState.setArchitecture(System.getProperty("os.arch"));
    	foxtrotState.setOSName(System.getProperty("os.name"));
    	foxtrotState.setOSVersion(System.getProperty("os.version"));
    	foxtrotState.setJavaVendor(System.getProperty("java.vendor"));
    	foxtrotState.setJavaVersion(System.getProperty("java.version"));
    	foxtrotState.setJavaHome(System.getProperty("java.home"));
    	foxtrotState.setJavaUserHome(System.getProperty("user.home"));
    	foxtrotState.setJavaUserName(System.getProperty("user.name"));
    }
    
    private static String getHostName() {
    	String host;
    	
    	host = FoxtrotUtils.execReadToString("hostname");
    	return host;
    }
    
	private static String getOSFlavor() {
        String distribution;
        
        distribution = FoxtrotUtils.matchPatternInFile(DISTRIBUTION, "/etc/lsb-release");
        if (distribution == null) {
        	distribution = FoxtrotUtils.matchPatternInFile(PRETTYNAME, "/etc/os-release");
        }
        if (distribution == null) {
        	throw new AlixiaException("No distribution info from /etc/lsb-release or /etc/os-release");
        }
        return distribution;
	}

	private List<Processor> getCPUs() {
		List<String> lines;
		List<Processor> cpus;
		String[] tokens;
		Processor cpu;
		String model;
		String freq;
		String cpuNumber;
		String token;
		
		cpus = new ArrayList<>();
		lines = FoxtrotUtils.getLinesFromFile("/proc/cpuinfo");
        if (lines == null) {
        	throw new AlixiaException("No CPU info from /proc/cpuinfo");
        }
        cpu = foxtrotState.new Processor();
		for (String line : lines) {
			tokens = line.split(":");
			if (tokens.length > 0) {
				token = tokens[0].trim();
				if (token.equals("processor")) {
					cpuNumber = tokens[1].trim();
					cpu.setProcessorNumber(Integer.parseInt(cpuNumber));
					continue;
				}
				if (token.equals("model name")) {
					model = tokens[1].trim();
					cpu.setModelName(model);
					continue;
				}
				if (token.equals("cpu MHz")) {
					freq = tokens[1].trim();
					cpu.setCpuMhz(Float.parseFloat(freq));
					cpus.add(cpu);
					cpu = foxtrotState.new Processor();
				}
			}
		}
        return cpus;
	}

	private void updateNetwork() {
		List<NetworkDevice> networkDevices;
		NetworkDevice device;
		List<String> lines;
		String[] tokens;
		String valueStr;
		String[] valueStrs;
		Long value;
		int pingResult;
		
		lines = FoxtrotUtils.getLinesFromFile("/proc/net/dev");
        if (lines == null) {
        	throw new AlixiaException("No network info from /proc/net/dev");
        }
        networkDevices = new ArrayList<>();
		for (String line : lines) {
			if (line.startsWith("Inter") || line.startsWith(" face")) {
				continue;
			}
			device = foxtrotState.new NetworkDevice();
			tokens = line.split(":");
			device.setDeviceName(tokens[0].trim());
			valueStrs = tokens[1].split("\\s+");
			if (valueStrs.length != 17) {
	        	throw new AlixiaException("Error parsing /proc/net/dev");
			}
			// valueStr[0] is an empty string
			valueStr = valueStrs[1].trim();
			value = Long.parseLong(valueStr);
			device.setReceiveKb(value/KB);
			valueStr = valueStrs[9].trim();
			value = Long.parseLong(valueStr);
			device.setTransmitKb(value/KB);
			networkDevices.add(device);
		}
		foxtrotState.setNetworkDevices(networkDevices);
		pingResult = FoxtrotUtils.statCommand("ping -q -c1 www.example.com");
		foxtrotState.setHaveInternet(pingResult == 0);
		foxtrotState.setHaveWebServer(linkIsOK("http://localhost"));
		foxtrotState.setHaveTomcat(linkIsOK("http://localhost:8080"));
		foxtrotState.setHaveDatabase(haveDatabase());
	}
	
	private void updateMemory() {
		List<String> lines;
		String[] tokens;
		String valueStr;
		String[] valueStrs;
		String token;
		
		lines = FoxtrotUtils.getLinesFromFile("/proc/meminfo");
        if (lines == null) {
        	throw new AlixiaException("No memory info from /proc/meminfo");
        }
		for (String line : lines) {
			tokens = line.split(":");
			if (tokens.length > 0) {
				token = tokens[0].trim();
				if (token.equals("MemTotal")) {
					valueStrs = tokens[1].trim().split(" ");
					valueStr = valueStrs[0].trim();
					foxtrotState.setTotalMemoryKb(Long.parseLong(valueStr)); // Kb
					continue;
				}
				if (token.equals("MemFree")) {
					valueStrs = tokens[1].trim().split(" ");
					valueStr = valueStrs[0].trim();
					foxtrotState.setFreeMemoryKb(Long.parseLong(valueStr));
					continue;
				}
				if (token.equals("SwapTotal")) {
					valueStrs = tokens[1].trim().split(" ");
					valueStr = valueStrs[0].trim();
					foxtrotState.setTotalSwapKb(Long.parseLong(valueStr));
					continue;
				}
				if (token.equals("SwapFree")) {
					valueStrs = tokens[1].trim().split(" ");
					valueStr = valueStrs[0].trim();
					foxtrotState.setFreeSwapKb(Long.parseLong(valueStr));
				}
			}
		}
	}

	private void updateUptimeInSeconds() {
		String uptime;
		Double seconds;
		Long lSeconds;
		String[] tokens;
		
		uptime = FoxtrotUtils.getStringFromFile("/proc/uptime");
        if (uptime == null) {
        	throw new AlixiaException("No uptime info from /proc/uptime");
        }
        tokens = uptime.split(" ");
        seconds = Double.parseDouble(tokens[0]);
        lSeconds = Math.round(seconds);
        foxtrotState.setUpTimeInSeconds(lSeconds);
	}
	
	private void updateJVMStats() {
		Runtime rt;
		int processors;
		long freeMemory;
		long maxMemory;
		long totalMemory;
		
		rt = Runtime.getRuntime();
		processors = rt.availableProcessors();
		foxtrotState.setJVMProcessors(processors);
		freeMemory = rt.freeMemory();
		foxtrotState.setJVMFreeMemoryKb(freeMemory/KB);
		maxMemory = rt.maxMemory();
		foxtrotState.setJVMMaxMemoryKb(maxMemory/KB);
		totalMemory = rt.totalMemory();
		foxtrotState.setJVMTotalMemoryKb(totalMemory/KB);
	}
	
	private void updateSensors() {
		Map<String, SensorValue> sensorValues;
		SensorValue sensorValue;
		StringBuilder result;
		String resultString;
		String chip = null;
		String key = null;
		String line;
		String label;
		String[] tokens;
		Float floatVal;
		
		sensorValues = new HashMap<>();
		result = FoxtrotUtils.runCommand("sensors -uA");
		resultString = result.toString();
		try (BufferedReader inStream = new BufferedReader(new StringReader(resultString))) {
			while ((line = inStream.readLine()) != null) {
				tokens = line.split(":");
				label = tokens[0].trim();
				// series divider
				if (label.isEmpty()) {
					chip = null;
					continue;
				}
				// first line of series
				if (chip == null) {
					chip = label;
					continue;
				}
				if (tokens.length == 1) {
					key = tokens[0].trim();
				}
				if (label.endsWith("_input")) {
					sensorValue = foxtrotState.new SensorValue();
					sensorValue.setChip(chip);
					sensorValue.setLabel(key);
					floatVal = Float.parseFloat(tokens[1].trim());
					sensorValue.setValue(floatVal);
					sensorValue.setAlarm(false);
					sensorValues.put(key, sensorValue);
					continue;
				}
				if (label.endsWith("_alarm")) {
					if (sensorValues.containsKey(key)) {
						sensorValue = sensorValues.get(key);
						floatVal = Float.parseFloat(tokens[1].trim());
						sensorValue.setAlarm(floatVal > 0);
					}
				}
			}
		} catch (IOException e) {
			throw new AlixiaException("Can't read sensors stream", e);
		}
		foxtrotState.setSensorValues(sensorValues);
	}
	
	private void updateDiskSpace() {
		List<FoxtrotFS> fileSystems;
		FoxtrotFS fileSystem;
		StringBuilder result;
		String resultString;
		String line;
		String fsName;
		String strValue;
		Long value;
		Integer percent;
		String[] tokens;
		
		fileSystems = new ArrayList<>();
		result = FoxtrotUtils.runCommand("df");
		resultString = result.toString();
		try (BufferedReader inStream = new BufferedReader(new StringReader(resultString))) {
			while ((line = inStream.readLine()) != null) {
				tokens = line.split("\\s+");
				fsName = tokens[0].trim();
				if (!fsName.startsWith("/dev")) {
					continue;
				}
//				System.out.println("DEBUG TOKEN: " + fsName);
				fileSystem = foxtrotState.new FoxtrotFS();
				fileSystem.setFsName(fsName);
				strValue = tokens[1].trim();
				value = Long.parseLong(strValue);
				fileSystem.setTotalSpaceKb(value);
				strValue = tokens[2].trim();
				value = Long.parseLong(strValue);
				fileSystem.setUsedSpaceKb(value);
				strValue = tokens[3].trim();
				value = Long.parseLong(strValue);
				fileSystem.setFreeSpaceKb(value);
				strValue = tokens[4].trim();
				// get rid of trailing %
				strValue = strValue.substring(0, strValue.length() - 1);
				percent = Integer.parseInt(strValue);
				fileSystem.setUsedPercent(percent);
				strValue = tokens[5].trim();
				fileSystem.setMountPoint(strValue);
				fileSystems.add(fileSystem);
			}
		} catch (IOException e) {
			throw new AlixiaException("Can't read df stream", e);
		}
		foxtrotState.setFileSystems(fileSystems);
	}
	
	private void updateDatabase() {
        DatabaseMetaData metaData;
        DataSource source;
		
		try {
			source = getNonPoolingDataSource();
		} catch (SQLException e) {
			throw new AlixiaException("Can't get non-pooling data source", e);
		}
		if (source instanceof DummyDataSource) {
			return;
		}
        try (Connection conn = source.getConnection()){
            metaData = conn.getMetaData();
            foxtrotState.setDbProductName(metaData.getDatabaseProductName());
            foxtrotState.setDbProductVersion(metaData.getDatabaseProductVersion());
            foxtrotState.setDbDriverName(metaData.getDriverName());
            foxtrotState.setDbDriverVersion(metaData.getDriverVersion());
            foxtrotState.setDbURL(metaData.getURL());
            foxtrotState.setDbUserName(metaData.getUserName());
        } catch (SQLException ex) {
           	throw new AlixiaException("Can't get database connection data: " +
           			ex.getMessage());
        }
	}
	
	private Boolean haveDatabase() {
        DataSource source;
		
		try {
			source = getNonPoolingDataSource();
		} catch (SQLException e) {
			throw new AlixiaException("Can't get non-pooling data source", e);
		}
        if (source == null) {
           	throw new AlixiaException("Can't get data source");
        }
		if (source instanceof DummyDataSource) {
			return false;
		}
        try (Connection conn = source.getConnection()){
        	return true;
        } catch (SQLException ex) {
           	throw new AlixiaException("Can't get database connection: " +
           			ex.getMessage());
        }
	}
    
	private static Boolean linkIsOK(String link) {
		URL url;
		HttpURLConnection conn;
		int responseCode;
		
		try {
			url = new URL(link);
		} catch (MalformedURLException e) {
			return false;
		}
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.connect();
			responseCode = conn.getResponseCode();
			if (responseCode < 400) {
				return true;
			}
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	private boolean updateApacheStatus() {
		List<String> statusLines;
		String serverName;
		String workStr;
		Long value;
		
		statusLines = getWebServerStatus(APACHEURL);
		if (statusLines.isEmpty()) {
			return false;
		}
		foxtrotState.setWebServer(APACHENAME);
		serverName = statusLines.remove(0);
		foxtrotState.setServerName(serverName);
		for (String line : statusLines) {
			if (line.startsWith(APACHEVERSION)) {
				workStr = line.substring(APACHEVERSION.length() + 1);
				foxtrotState.setServerVersion(workStr);
			}
			if (line.startsWith(APACHEUPTIME)) {
				workStr = line.substring(APACHEUPTIME.length() + 1);
				value = Long.parseLong(workStr);
				foxtrotState.setServerUptime(value);
			}
			if (line.startsWith(APACHETOTALACCESSES)) {
				workStr = line.substring(APACHETOTALACCESSES.length() + 1);
				value = Long.parseLong(workStr);
				foxtrotState.setServerAccesses(value);
			}
			if (line.startsWith(APACHETOTALKBYTES)) {
				workStr = line.substring(APACHETOTALKBYTES.length() + 1);
				value = Long.parseLong(workStr);
				foxtrotState.setServerKBytes(value);
			}
		}
		return true;
	}

	// to enable nginx status page, see e.g. https://www.tecmint.com/enable-nginx-status-page/
	private boolean updateNginxStatus() {
		List<String> statusLines;
		Long value;
		String[] counts;
		
		statusLines = getWebServerStatus(NGINXURL);
		if (statusLines.isEmpty()) {
			return false;
		}
		foxtrotState.setWebServer(NGINXNAME);
		foxtrotState.setServerName(null);
		foxtrotState.setServerVersion(null);
		foxtrotState.setServerKBytes(null);
		foxtrotState.setServerUptime(null);
		for (String line : statusLines) {
			if (line.startsWith(NGINXACTIVE)) {
//				workStr = line.substring(NGINXACTIVE.length() + 1);
			} else if (line.startsWith(NGINXSERVER)) {
				// do nothing, header line
			} else if (line.startsWith(NGINXREADING)) {
				// line looks like:
				// Reading: 6 Writing: 179 Waiting: 106
				// ...we don't currently do anything with these stats
			} else {
				counts = line.split("\\s+");
				LOGGER.log(LOGLEVEL, "line = [{0}]", line);
				LOGGER.log(LOGLEVEL, "counts.length = {0}", counts.length);
				for (String count : counts) {
					LOGGER.log(LOGLEVEL, "counts value = [{0}]", count);
				}
				// with split there is an initial space element in the array at pos 0
				if (counts.length == 4) {
					try {
						// accepts
						value = Long.parseLong(counts[1]);
						LOGGER.log(LOGLEVEL, "value 1 = {0}", value);
					} catch (NumberFormatException e) {
						continue;
					}
					try {
						// handled
						value = Long.parseLong(counts[2]);
						LOGGER.log(LOGLEVEL, "value 2 = {0}", value);
					} catch (NumberFormatException e) {
						continue;
					}
					try {
						// requests
						value = Long.parseLong(counts[3]);
						LOGGER.log(LOGLEVEL, "value 3 = {0}", value);
						foxtrotState.setServerAccesses(value);
					} catch (NumberFormatException e) {
						continue;
					}
				}
			}
		}
		return true;
	}
	
	private static List<String> getWebServerStatus(String urlStr) {
		URL url;
		String line;
	    List<String> statusLines;
	    
	    
		SharedUtils.checkNotNull(urlStr);
	    statusLines = new ArrayList<>(40);
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException ex) {
			throw new AlixiaException("Bad URL in getWebServerStatus", ex);
		}
		try (InputStream inStream = url.openStream()) {
			try (BufferedReader in = new BufferedReader(new InputStreamReader(inStream))){
				while (true) {
					line = in.readLine();
					if (line == null) {
						break;
					}
					statusLines.add(line);
				}
			} catch (IOException ex) {
				throw new AlixiaException("I/O Exception reading input stream in getWebServerStatus", ex);
			}
		} catch (IOException ex) {
			return statusLines;
//			throw new AlixiaException("I/O Exception creating input stream in updateApacheStatus", ex);
		}
		return statusLines;
	}
	
    private DataSource getNonPoolingDataSource() throws SQLException {
//        DummyDataSource source;
		MariaDbDataSource source;
    	
        source = new MariaDbDataSource();
//        source = new DummyDataSource();
        if (databaseUser == null || 
        		databasePassword == null || 
        		databaseServer == null || 
        		databasePort == null) {
			AlixiaUtils.error("Warning: database parameters not set in LinuxMonitor");
        }
        source.setUser(databaseUser);
        source.setPassword(databasePassword);
        source.setServerName(databaseServer);
        source.setPort(databasePort);
        return source;
    }
}
