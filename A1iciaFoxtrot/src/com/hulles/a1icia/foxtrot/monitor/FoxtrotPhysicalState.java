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
package com.hulles.a1icia.foxtrot.monitor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.api.tools.A1iciaUtils;

final public class FoxtrotPhysicalState {
	private String hostName;			// "betty"
	private String architecture;		// "amd64"
	private String osName; 				// "Linux"
	private String osVersion;			// "4.2.0-35-generic"
	private String osFlavor;			// "Ubuntu 15.10"
	private List<Processor> processors;
	
    private String dbProductName;
    private String dbProductVersion;
    private String dbDriverName;
    private String dbDriverVersion;
    private String dbURL;
    private String dbUserName;

	// volatile
	private Long upTimeInSeconds;
	private Long totalMemoryKb;
	private Long freeMemoryKb;
	private Long totalSwapKb;
	private Long freeSwapKb;
	private Boolean haveInternet;
	private Boolean haveWebServer;
	private Boolean haveTomcat;
	private Boolean haveDatabase;

	// Java™
	private String javaVendor;			// "Oracle Corporation"
	private String javaVersion;			// "1.8.0_77"
	private String javaHome;
	private String javaUserHome;
	private String javaUserName;
	
	// Web Server
	private String webServer;
	private String serverName;
	private String serverVersion;
	private Long serverUptime;
	private Long serverAccesses;
	private Long serverKBytes;
	
	// volatile
	private Integer jvmProcessors;
	private Long jvmFreeMemoryKb;
	private Long jvmTotalMemoryKb;
	private Long jvmMaxMemoryKb;

	// volatile
	private Set<String> lanHosts;
	
	private Map<String, SensorValue> sensorValues;
	private List<FoxtrotFS> fileSystems;
	private List<NetworkDevice> networkDevices;
	
	public Map<String, SensorValue> getSensorValues() {
		
		return sensorValues;
	}

	public void setSensorValues(Map<String, SensorValue> sensorValues) {
		
		SharedUtils.checkNotNull(sensorValues);
		this.sensorValues = sensorValues;
	}

	public String getHostName() {
		
		return hostName;
	}

	public void setHostName(String hostName) {
		
		SharedUtils.checkNotNull(hostName);
		this.hostName = hostName;
	}

	public String getArchitecture() {
		
		return architecture;
	}

	public void setArchitecture(String architecture) {
		
		SharedUtils.checkNotNull(architecture);
		this.architecture = architecture;
	}

	public String getOSName() {
		return osName;
	}

	public void setOSName(String osName) {
		
		SharedUtils.checkNotNull(osName);
		this.osName = osName;
	}

	public String getOSVersion() {
		
		return osVersion;
	}

	public void setOSVersion(String osVersion) {
		
		SharedUtils.checkNotNull(osVersion);
		this.osVersion = osVersion;
	}

	public String getOSFlavor() {
		
		return osFlavor;
	}

	public void setOSFlavor(String osFlavor) {
		
		SharedUtils.checkNotNull(osFlavor);
		this.osFlavor = osFlavor;
	}

	public Long getUpTimeInSeconds() {
		
		return upTimeInSeconds;
	}

	public void setUpTimeInSeconds(Long upTime) {
		
		SharedUtils.checkNotNull(upTime);
		this.upTimeInSeconds = upTime;
	}

	public List<Processor> getProcessors() {

		return processors;
	}

	public void setProcessors(List<Processor> processors) {
		
		SharedUtils.checkNotNull(processors);
		this.processors = processors;
	}

	public Long getTotalMemoryKb() {
		
		return totalMemoryKb;
	}

	public void setTotalMemoryKb(Long totalMemoryKb) {
		
		SharedUtils.checkNotNull(totalMemoryKb);
		this.totalMemoryKb = totalMemoryKb;
	}

	public Long getFreeMemoryKb() {
		
		return freeMemoryKb;
	}

	public void setFreeMemoryKb(Long freeMemoryKb) {
		
		SharedUtils.checkNotNull(freeMemoryKb);
		this.freeMemoryKb = freeMemoryKb;
	}

	public Long getTotalSwapKb() {
		
		return totalSwapKb;
	}

	public void setTotalSwapKb(Long totalSwapKb) {
		
		SharedUtils.checkNotNull(totalSwapKb);
		this.totalSwapKb = totalSwapKb;
	}

	public Long getFreeSwapKb() {
		
		return freeSwapKb;
	}

	public void setFreeSwapKb(Long freeSwapKb) {
		
		SharedUtils.checkNotNull(freeSwapKb);
		this.freeSwapKb = freeSwapKb;
	}

	public String getWebServer() {
		
		return webServer;
	}

	public void setWebServer(String name) {
		
		SharedUtils.checkNotNull(name);
		this.webServer = name;
	}

	public String getServerName() {
		
		return serverName;
	}

	public void setServerName(String serverName) {
		
		SharedUtils.nullsOkay(serverName);
		this.serverName = serverName;
	}

	public String getServerVersion() {
		
		return serverVersion;
	}

	public void setServerVersion(String serverVersion) {
		
		SharedUtils.nullsOkay(serverVersion);
		this.serverVersion = serverVersion;
	}

	public Long getServerUptime() {
		
		return serverUptime;
	}

	public void setServerUptime(Long serverUptime) {
		
		SharedUtils.nullsOkay(serverUptime);
		this.serverUptime = serverUptime;
	}

	public Long getServerAccesses() {
		
		return serverAccesses;
	}

	public void setServerAccesses(Long serverAccesses) {
		
		SharedUtils.checkNotNull(serverAccesses);
		this.serverAccesses = serverAccesses;
	}

	public Long getServerKBytes() {
		
		return serverKBytes;
	}

	public void setServerKBytes(Long serverKBytes) {
		
		SharedUtils.nullsOkay(serverKBytes);
		this.serverKBytes = serverKBytes;
	}

	public String getJavaVendor() {
		
		return javaVendor;
	}

	public void setJavaVendor(String javaVendor) {
		
		SharedUtils.checkNotNull(javaVendor);
		this.javaVendor = javaVendor;
	}

	public String getJavaVersion() {
		
		return javaVersion;
	}

	public void setJavaVersion(String javaVersion) {
		
		SharedUtils.checkNotNull(javaVersion);
		this.javaVersion = javaVersion;
	}

	public String getJavaHome() {
		
		return javaHome;
	}

	public void setJavaHome(String javaHome) {
		
		SharedUtils.checkNotNull(javaHome);
		this.javaHome = javaHome;
	}

	public String getJavaUserHome() {
		
		return javaUserHome;
	}

	public void setJavaUserHome(String userHome) {
		
		SharedUtils.checkNotNull(userHome);
		this.javaUserHome = userHome;
	}

	public String getJavaUserName() {
		
		return javaUserName;
	}

	public void setJavaUserName(String userName) {
		
		SharedUtils.checkNotNull(userName);
		this.javaUserName = userName;
	}

	public Integer getJVMProcessors() {
		
		return jvmProcessors;
	}

	public void setJVMProcessors(Integer jvmProcessors) {
		
		SharedUtils.checkNotNull(jvmProcessors);
		this.jvmProcessors = jvmProcessors;
	}

	public Long getJVMFreeMemoryKb() {
		
		return jvmFreeMemoryKb;
	}

	public void setJVMFreeMemoryKb(Long jvmFreeMemoryKb) {
		
		SharedUtils.checkNotNull(jvmFreeMemoryKb);
		this.jvmFreeMemoryKb = jvmFreeMemoryKb;
	}

	public Long getJVMTotalMemoryKb() {
		
		return jvmTotalMemoryKb;
	}

	public void setJVMTotalMemoryKb(Long jvmTotalMemoryKb) {
		
		SharedUtils.checkNotNull(jvmTotalMemoryKb);
		this.jvmTotalMemoryKb = jvmTotalMemoryKb;
	}

	public Long getJVMMaxMemoryKb() {
		
		return jvmMaxMemoryKb;
	}

	public void setJVMMaxMemoryKb(Long jvmMaxMemoryKb) {
		
		SharedUtils.checkNotNull(jvmMaxMemoryKb);
		this.jvmMaxMemoryKb = jvmMaxMemoryKb;
	}

	public String getDbProductName() {
		
		return dbProductName;
	}

	public void setDbProductName(String dbProductName) {
		
		SharedUtils.checkNotNull(dbProductName);
		this.dbProductName = dbProductName;
	}

	public String getDbProductVersion() {
		
		return dbProductVersion;
	}

	public void setDbProductVersion(String dbProductVersion) {
		
		SharedUtils.checkNotNull(dbProductVersion);
		this.dbProductVersion = dbProductVersion;
	}

	public String getDbDriverName() {
		
		return dbDriverName;
	}

	public void setDbDriverName(String dbDriverName) {
		
		SharedUtils.checkNotNull(dbDriverName);
		this.dbDriverName = dbDriverName;
	}

	public String getDbDriverVersion() {
		
		return dbDriverVersion;
	}

	public void setDbDriverVersion(String dbDriverVersion) {
		
		SharedUtils.checkNotNull(dbDriverVersion);
		this.dbDriverVersion = dbDriverVersion;
	}

	public String getDbURL() {
		
		return dbURL;
	}

	public void setDbURL(String dbURL) {
		
		SharedUtils.checkNotNull(dbURL);
		this.dbURL = dbURL;
	}

	public String getDbUserName() {
		
		return dbUserName;
	}

	public void setDbUserName(String dbUserName) {
		
		SharedUtils.checkNotNull(dbUserName);
		this.dbUserName = dbUserName;
	}

	public Set<String> getLanHosts() {
		
		return lanHosts;
	}

	public void setLanHosts(Set<String> lanHosts) {
		
		SharedUtils.checkNotNull(lanHosts);
		this.lanHosts = lanHosts;
	}

	public List<NetworkDevice> getNetworkDevices() {
		
		return networkDevices;
	}

	public void setNetworkDevices(List<NetworkDevice> networkDevices) {
		
		SharedUtils.checkNotNull(networkDevices);
		this.networkDevices = networkDevices;
	}

	public List<FoxtrotFS> getFileSystems() {
		
		return fileSystems;
	}

	public void setFileSystems(List<FoxtrotFS> fileSystems) {
		
		SharedUtils.checkNotNull(fileSystems);
		this.fileSystems = fileSystems;
	}
	
	public Boolean haveInternet() {
		
		return haveInternet;
	}

	public void setHaveInternet(Boolean haveInternet) {
		
		SharedUtils.checkNotNull(haveInternet);
		this.haveInternet = haveInternet;
	}

	public Boolean haveWebServer() {
		
		return haveWebServer;
	}

	public void setHaveWebServer(Boolean haveServer) {
		
		SharedUtils.checkNotNull(haveServer);
		this.haveWebServer = haveServer;
	}

	public Boolean haveTomcat() {
		
		return haveTomcat;
	}

	public void setHaveTomcat(Boolean haveTomcat) {
		
		SharedUtils.checkNotNull(haveTomcat);
		this.haveTomcat = haveTomcat;
	}

	public Boolean haveDatabase() {
		
		return haveDatabase;
	}

	public void setHaveDatabase(Boolean haveDatabase) {
		
		SharedUtils.checkNotNull(haveDatabase);
		this.haveDatabase = haveDatabase;
	}

	@Override
	public String toString() {
		StringBuilder sb;
		String kbStr;
		long seconds;
		String etStr;
		
		sb = new StringBuilder();
		sb.append("<h3>SYSTEM INFO</h3>\n");
		sb.append("<dl>\n");
        sb.append("<dt>Host Name</dt>\n");
        sb.append("<dd>");
        sb.append(this.getHostName());
        sb.append("</dd>\n");
        sb.append("<dt>Architecture</dt>\n");
        sb.append("<dd>");
        sb.append(this.getArchitecture());
        sb.append("</dd>\n");
        sb.append("<dt>OS Name</dt>\n");
        sb.append("<dd>");
        sb.append(this.getOSName());
        sb.append("</dd>\n");
        sb.append("<dt>OS Version</dt>\n");
        sb.append("<dd>");
        sb.append(this.getOSVersion());
        sb.append("</dd>\n");
        sb.append("<dt>OS Flavor</dt>\n");
        sb.append("<dd>");
        sb.append(this.getOSFlavor());
        sb.append("</dd>\n");
		seconds = this.getUpTimeInSeconds();
		sb.append("<dt>Up Time</dt>\n");
		sb.append("<dd>");
        sb.append(A1iciaUtils.formatElapsedSeconds(seconds));
        sb.append("</dd>\n");
		sb.append("<dt>CPU Info</dt>\n");
		sb.append("<dd>");
		for (Processor cpu : this.getProcessors()) {
			sb.append("Processor ");
            sb.append(cpu.getProcessorNumber());
			sb.append(" ");
            sb.append(cpu.getModelName());
			sb.append(" ");
            sb.append(cpu.getCpuMhz());
            sb.append("MHz<br />\n");
		}
		sb.append("</dd>\n");
		sb.append("<dt>Total memory</dt>\n");
		kbStr = A1iciaUtils.formatKb(this.getTotalMemoryKb());
		sb.append("<dd>");
        sb.append(kbStr);
        sb.append("</dd>\n");
		sb.append("<dt>Free memory</dt>\n");
		kbStr = A1iciaUtils.formatKb(this.getFreeMemoryKb());
		sb.append("<dd>");
        sb.append(kbStr);
        sb.append("</dd>\n");
		sb.append("<dt>Total swap</dt>\n");
		kbStr = A1iciaUtils.formatKb(this.getTotalSwapKb());
		sb.append("<dd>");
        sb.append(kbStr);
        sb.append("</dd>\n");
		sb.append("<dt>Free swap</dt>\n");
		kbStr = A1iciaUtils.formatKb(this.getFreeSwapKb());
		sb.append("<dd>");
        sb.append(kbStr);
        sb.append("</dd>\n");
		sb.append("<dt>Java Vendor</dt>\n");
		sb.append("<dd>");
        sb.append(this.getJavaVendor());
        sb.append("</dd>\n");
        sb.append("<dt>Java Version</dt>\n");
        sb.append("<dd>");
        sb.append(this.getJavaVersion());
        sb.append("</dd>\n");
        sb.append("<dt>Java Home</dt>\n");
        sb.append("<dd>");
        sb.append(this.getJavaHome());
        sb.append("</dd>\n");
        sb.append("<dt>Java User Name</dt>\n");
        sb.append("<dd>");
        sb.append(this.getJavaUserName());
        sb.append("</dd>\n");
        sb.append("<dt>Java User Home Directory</dt>\n");
        sb.append("<dd>");
        sb.append(this.getJavaUserHome());
        sb.append("</dd>\n");
		sb.append("<dt>JVM Processors</dt>\n");
		sb.append("<dd>");
        sb.append(this.getJVMProcessors());
        sb.append("</dd>\n");
		sb.append("<dt>JVM Free Memory</dt>\n");
		kbStr = A1iciaUtils.formatKb(this.getJVMFreeMemoryKb());
		sb.append("<dd>");
        sb.append(kbStr);
        sb.append("</dd>\n");
		sb.append("<dt>JVM Max Memory</dt>\n");
		kbStr = A1iciaUtils.formatKb(this.getJVMMaxMemoryKb());
		sb.append("<dd>");
        sb.append(kbStr);
        sb.append("</dd>\n");
		sb.append("<dt>JVM Total Memory</dt>\n");
		kbStr = A1iciaUtils.formatKb(this.getJVMTotalMemoryKb());
		sb.append("<dd>");
        sb.append(kbStr);
        sb.append("</dd>\n");

		sb.append("<dt>Database Product</dt>\n");
		sb.append("<dd>");
        sb.append(this.getDbProductName());
        sb.append("</dd>\n");
		sb.append("<dt>Database Version</dt>\n");
		sb.append("<dd>");
        sb.append(this.getDbProductVersion());
        sb.append("</dd>\n");
		sb.append("<dt>Database Driver Name</dt>\n");
		sb.append("<dd>");
        sb.append(this.getDbDriverName());
        sb.append("</dd>\n");
		sb.append("<dt>Database Driver Version</dt>\n");
		sb.append("<dd>");
        sb.append(this.getDbDriverVersion());
        sb.append("</dd>\n");
		sb.append("<dt>Database User Name</dt>\n");
		sb.append("<dd>");
        sb.append(this.getDbUserName());
        sb.append("</dd>\n");
		sb.append("<dt>Database URL</dt>\n");
		sb.append("<dd>");
        sb.append(this.getDbURL());
        sb.append("</dd>\n");

		if (this.getWebServer() != null) {
			sb.append("<dt>Web Server</dt>\n");
			sb.append("<dd>");
            sb.append(this.getWebServer());
            sb.append("</dd>\n");
		}
		if (this.getServerName() != null) {
			sb.append("<dt>Web Server Host Name</dt>\n");
			sb.append("<dd>");
            sb.append(this.getServerName());
            sb.append("</dd>\n");
		}
		if (this.getServerVersion() != null) {
			sb.append("<dt>Web Server Version</dt>\n");
			sb.append("<dd>");
            sb.append(this.getServerVersion());
            sb.append("</dd>\n");
		}
		if (this.getServerUptime() != null) {
			sb.append("<dt>Web Server Total Uptime</dt>\n");
			etStr = A1iciaUtils.formatElapsedSeconds(this.getServerUptime());
			sb.append("<dd>");
            sb.append(etStr);
            sb.append("</dd>\n");
		}
		if (this.getServerAccesses() != null) {
			sb.append("<dt>Web Server Total Accesses</dt>\n");
			sb.append("<dd>");
            sb.append(this.getServerAccesses());
            sb.append("</dd>\n");
		}
		if (this.getServerKBytes() != null) {
			sb.append("<dt>Web Server Total kBytes Served</dt>\n");
			kbStr = A1iciaUtils.formatKb(this.getServerKBytes());
			sb.append("<dd>");
            sb.append(kbStr);
            sb.append("</dd>\n");
		}		
		
		for (SensorValue sv : this.getSensorValues().values()) {
			sb.append("<dt>Sensor ");
            sb.append(sv.getLabel());
            sb.append("</dt>\n");
			sb.append("<dd>");
            sb.append(sv.getValue());
			if (sv.getAlarm()) {
				sb.append(" ALARM");
			}
			sb.append("</dd>\n");
		}
		for (FoxtrotFS fs : this.getFileSystems()) {
			sb.append("<dt>File System ");
            sb.append(fs.getFsName());
            sb.append("</dt>\n");
			kbStr = A1iciaUtils.formatKb(fs.getTotalSpaceKb());
			sb.append("<dd>Total Space ");
            sb.append(kbStr);
			kbStr = A1iciaUtils.formatKb(fs.getUsedSpaceKb());
			sb.append(" | Used Space ");
            sb.append(kbStr);
			kbStr = A1iciaUtils.formatKb(fs.getFreeSpaceKb());
			sb.append(" | Free Space ");
            sb.append(kbStr);
			sb.append(" | Used ");
            sb.append(fs.getUsedPercent());
            sb.append("%");
			sb.append(" | Mounted at ");sb.append(fs.getMountPoint());
            sb.append("</dd>\n");
		}
		for (NetworkDevice device : this.getNetworkDevices()) {
			sb.append("<dt>Network Device ");
            sb.append(device.getDeviceName());
            sb.append("</dt>\n");
			kbStr = A1iciaUtils.formatKb(device.getTransmitKb());
			sb.append("<dd>Transmitted ");
            sb.append(kbStr);
			kbStr = A1iciaUtils.formatKb(device.getReceiveKb());
			sb.append(" | Received ");
            sb.append(kbStr);
            sb.append("</dd>\n");
		}
		sb.append("<dt>Current LAN Host IPs</dt>\n");
		for (String host : this.getLanHosts()) {
			sb.append("<dd>");
            sb.append(host);
            sb.append("</dd>\n");
		}
		sb.append("<dt>Have Internet</dt>\n");
		sb.append("<dd>");
        sb.append(this.haveInternet());
        sb.append("</dd>\n");
		sb.append("<dt>Have Http Server</dt>\n");
		sb.append("<dd>");
        sb.append(this.haveWebServer());
        sb.append("</dd>\n");
		sb.append("<dt>Have Tomcat</dt>\n");
		sb.append("<dd>");
        sb.append(this.haveTomcat());
        sb.append("</dd>\n");
		sb.append("<dt>Have Database</dt>\n");
		sb.append("<dd>");
        sb.append(this.haveDatabase());
        sb.append("</dd>\n");
		sb.append("</dl>\n");
		return sb.toString();
	}

	public class NetworkDevice {
		private String deviceName;
		private Long transmitKb;
		private Long receiveKb;
		
		public String getDeviceName() {
			
			return deviceName;
		}
		
		public void setDeviceName(String deviceName) {
			
			SharedUtils.checkNotNull(deviceName);
			this.deviceName = deviceName;
		}
		
		public Long getTransmitKb() {
			
			return transmitKb;
		}
		
		public void setTransmitKb(Long transmitKb) {
			
			SharedUtils.checkNotNull(transmitKb);
			this.transmitKb = transmitKb;
		}
		
		public Long getReceiveKb() {
			
			return receiveKb;
		}
		
		public void setReceiveKb(Long receiveKb) {
			
			SharedUtils.checkNotNull(receiveKb);
			this.receiveKb = receiveKb;
		}
		
	}

	public class FoxtrotFS {
		private String fsName;
		private Long totalSpaceKb;
		private Long usedSpaceKb;
		private Long freeSpaceKb;
		private Integer usedPercent; 
		private String mountPoint;
		
		public String getFsName() {
			
			return fsName;
		}
		
		public void setFsName(String fsName) {
			
			SharedUtils.checkNotNull(fsName);
			this.fsName = fsName;
		}
		
		public Long getTotalSpaceKb() {
			
			return totalSpaceKb;
		}
		
		public void setTotalSpaceKb(Long totalSpaceKb) {
			
			SharedUtils.checkNotNull(totalSpaceKb);
			this.totalSpaceKb = totalSpaceKb;
		}
		
		public Long getUsedSpaceKb() {
			
			return usedSpaceKb;
		}
		
		public void setUsedSpaceKb(Long usedSpaceKb) {
			
			SharedUtils.checkNotNull(usedSpaceKb);
			this.usedSpaceKb = usedSpaceKb;
		}
		
		public Long getFreeSpaceKb() {
			
			return freeSpaceKb;
		}
		
		public void setFreeSpaceKb(Long freeSpaceKb) {
			
			SharedUtils.checkNotNull(freeSpaceKb);
			this.freeSpaceKb = freeSpaceKb;
		}
		
		public Integer getUsedPercent() {
			
			return usedPercent;
		}
		
		public void setUsedPercent(Integer usedPercent) {
			
			SharedUtils.checkNotNull(usedPercent);
			this.usedPercent = usedPercent;
		}
		
		public String getMountPoint() {
			
			return mountPoint;
		}
		
		public void setMountPoint(String mountPoint) {
			
			SharedUtils.checkNotNull(mountPoint);
			this.mountPoint = mountPoint;
		}

	}
	
	public class SensorValue {
		private String chip;
		private String label;
		private Float value;
		private Boolean alarm;
		
		public String getChip() {
			
			return chip;
		}

		public void setChip(String chip) {
			
			SharedUtils.checkNotNull(chip);
			this.chip = chip;
		}
		
		public String getLabel() {
			
			return label;
		}

		public void setLabel(String label) {
			
			SharedUtils.checkNotNull(label);
			this.label = label;
		}
		
		public Float getValue() {
			
			return value;
		}
		
		public void setValue(Float value) {
			
			SharedUtils.checkNotNull(value);
			this.value = value;
		}
		
		public Boolean getAlarm() {
			
			return alarm;
		}
		
		public void setAlarm(Boolean alarm) {
			
			SharedUtils.checkNotNull(alarm);
			this.alarm = alarm;
		}
		
	}

	public class Processor {
		private String modelName;
		private Integer processorNumber;
		private Float cpuMhz;
		
		public String getModelName() {
			
			return modelName;
		}
		
		public void setModelName(String modelName) {
			
			SharedUtils.checkNotNull(modelName);
			this.modelName = modelName;
		}
		
		public Integer getProcessorNumber() {
			
			return processorNumber;
		}
		
		public void setProcessorNumber(Integer processorNumber) {
			
			SharedUtils.checkNotNull(processorNumber);
			this.processorNumber = processorNumber;
		}
		
		public Float getCpuMhz() {
			
			return cpuMhz;
		}
		
		public void setCpuMhz(Float cpuMhz) {
			
			SharedUtils.checkNotNull(cpuMhz);
			this.cpuMhz = cpuMhz;
		}
		
		
	}
}
