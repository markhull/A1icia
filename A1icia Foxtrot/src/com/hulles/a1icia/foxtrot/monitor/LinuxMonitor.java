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
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.foxtrot.dummy.DummyDataSource;
import com.hulles.a1icia.foxtrot.monitor.FoxtrotPhysicalState.FoxtrotFS;
import com.hulles.a1icia.foxtrot.monitor.FoxtrotPhysicalState.NetworkDevice;
import com.hulles.a1icia.foxtrot.monitor.FoxtrotPhysicalState.Processor;
import com.hulles.a1icia.foxtrot.monitor.FoxtrotPhysicalState.SensorValue;
import com.hulles.a1icia.tools.A1iciaUtils;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

final public class LinuxMonitor {
	private static final int KB = 1024;
	private final FoxtrotPhysicalState foxtrotState;
    private static final Pattern DISTRIBUTION =
            Pattern.compile("DISTRIB_DESCRIPTION=\"(.*)\"", Pattern.MULTILINE);
    private static final String APACHEURL = "http://localhost/server-status?auto";
    private static final String APACHEVERSION = "ServerVersion:";
    private static final String APACHEUPTIME = "ServerUptimeSeconds:";
    private static final String APACHETOTALACCESSES = "Total Accesses:";
    private static final String APACHETOTALKBYTES = "Total kBytes:";
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
		
		A1iciaUtils.checkNotNull(databaseUser);
		this.databaseUser = databaseUser;
	}

	public void setDatabasePassword(String databasePassword) {
		
		A1iciaUtils.checkNotNull(databasePassword);
		this.databasePassword = databasePassword;
	}

	public void setDatabaseServer(String databaseServer) {
		
		A1iciaUtils.checkNotNull(databaseServer);
		this.databaseServer = databaseServer;
	}

	public void setDatabasePort(Integer databasePort) {
		
		A1iciaUtils.checkNotNull(databasePort);
		this.databasePort = databasePort;
	}

	private void updateVolatileValues() {
    	updateMemory();
    	updateUptimeInSeconds();
    	updateJVMStats();
    	updateSensors();
    	updateDiskSpace();
    	updateNetwork();
    	updateDatabase();
    	updateApacheStatus();
    }
    
    public FoxtrotPhysicalState getFoxtrotPhysicalState() {
    	updateVolatileValues();
    	return foxtrotState;
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
        	throw new A1iciaException("No distribution info from /etc/lsb-release");
        }
        return distribution;
	}

	private List<Processor> getCPUs() {
		List<String> lines;
		List<Processor> cpus;
		String[] tokens;
		Processor cpu = null;
		String model = null;
		String freq = null;
		String cpuNumber = null;
		String token;
		
		cpus = new ArrayList<>();
		lines = FoxtrotUtils.getLinesFromFile("/proc/cpuinfo");
        if (lines == null) {
        	throw new A1iciaException("No CPU info from /proc/cpuinfo");
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
					cpuNumber = null;
					model = null;
					freq = null;
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
		String valueStr = null;
		String[] valueStrs = null;
		Long value;
		int pingResult;
		
		lines = FoxtrotUtils.getLinesFromFile("/proc/net/dev");
        if (lines == null) {
        	throw new A1iciaException("No network info from /proc/net/dev");
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
	        	throw new A1iciaException("Error parsing /proc/net/dev");
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
		String valueStr = null;
		String[] valueStrs = null;
		String token;
		
		lines = FoxtrotUtils.getLinesFromFile("/proc/meminfo");
        if (lines == null) {
        	throw new A1iciaException("No memory info from /proc/meminfo");
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
					continue;
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
        	throw new A1iciaException("No uptime info from /proc/uptime");
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
		BufferedReader inStream;
		
		sensorValues = new HashMap<>();
		result = FoxtrotUtils.runCommand("sensors -uA");
		resultString = result.toString();
		inStream = new BufferedReader(new StringReader(resultString));
		try {
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
//				System.out.println("DEBUG TOKEN: " + label);
//				switch (label) {
//					case "it8721-isa-0290":
//					case "k10temp-pci-00c3":
//					case "fam15h_power-pci-00c4":
//					case "asus-isa-0000":
//						chip = label;
//						continue;
//					default:
//						break;
//				}
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
					continue;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new A1iciaException("Can't read sensors stream");
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
		BufferedReader inStream;
		
		fileSystems = new ArrayList<>();
		result = FoxtrotUtils.runCommand("df");
		resultString = result.toString();
		inStream = new BufferedReader(new StringReader(resultString));
		try {
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
			e.printStackTrace();
			throw new A1iciaException("Can't read df stream");
		}
		foxtrotState.setFileSystems(fileSystems);
	}
	
	private void updateDatabase() {
        DatabaseMetaData metaData = null;
        DataSource source;
		
		source = getNonPoolingDataSource();
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
           	throw new A1iciaException("Can't get database connection data: " +
           			ex.getMessage());
        }
	}
	
	private Boolean haveDatabase() {
        DataSource source;
		
		source = getNonPoolingDataSource();
		if (source instanceof DummyDataSource) {
			return false;
		}
        try (Connection conn = source.getConnection()){
        	return true;
        } catch (SQLException ex) {
           	throw new A1iciaException("Can't get database connection: " +
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

	private void updateApacheStatus() {
		List<String> statusLines;
		String serverName;
		String workStr;
		Long value;
		
		statusLines = getApacheStatus();
		if (statusLines.isEmpty()) {
			return;
		}
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
	}
	
	private static List<String> getApacheStatus() {
		URL url;
		InputStream inStream;
		String line = null;
	    List<String> statusLines;
	    
	    statusLines = new ArrayList<>(40);
		try {
			url = new URL(APACHEURL);
		} catch (MalformedURLException ex) {
			throw new A1iciaException("Bad URL in updateApacheStatus");
		}
		try { 
			inStream = url.openStream();
		} catch (IOException ex) {
			throw new A1iciaException("I/O Exception creating input stream in updateApacheStatus");
		}
		try (BufferedReader in = new BufferedReader(new InputStreamReader(inStream))){
			while (true) {
				line = in.readLine();
				if (line == null) {
					break;
				}
				statusLines.add(line);
			}
		} catch (IOException e) {
			throw new A1iciaException("I/O Exception reading input stream in updateApacheStatus");
		}
		try {
			inStream.close();
		} catch (IOException ex) {
			throw new A1iciaException("I/O Exception closing input stream in updateApacheStatus");
		}
		return statusLines;
	}
	
    private DataSource getNonPoolingDataSource() {
//        DummyDataSource source;
    	MysqlDataSource source;
    	
        source = new MysqlDataSource();
//        source = new DummyDataSource();
        if (databaseUser == null || 
        		databasePassword == null || 
        		databaseServer == null || 
        		databasePort == null) {
			System.err.println("Warning: database parameters not set in LinuxMonitor");
        }
        source.setUser(databaseUser);
        source.setPassword(databasePassword);
        source.setServerName(databaseServer);
        source.setPort(databasePort);
        return source;
    }
}
