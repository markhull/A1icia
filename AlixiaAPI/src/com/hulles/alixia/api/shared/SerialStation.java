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
package com.hulles.alixia.api.shared;

import java.util.Date;

import com.hulles.alixia.api.remote.Station.IronType;
import com.hulles.alixia.api.remote.Station.OsType;
import com.hulles.alixia.media.Language;

public class SerialStation extends SerialEntity {
	private static final long serialVersionUID = -6603249151083919155L;
	private SerialUUID<SerialStation> uuid;
	private String centralHost;
	private Integer centralPort;
	private Boolean hasPico;
	private Boolean hasMpv;
	private Boolean hasPrettyLights;
	private Boolean hasLEDS;
	private IronType ironType;
	private OsType osType;
	private Language defaultLanguage;
	private Date quietStart;
	private Date quietEnd;

	public SerialUUID<SerialStation> getUUID() {
		
		return uuid;
	}

	public void setUUID(SerialUUID<SerialStation> uuid) {
		
		SharedUtils.checkNotNull(uuid);
		this.uuid = uuid;
	}

	public String getCentralHost() {
		
		return centralHost;
	}

	public void setCentralHost(String centralHost) {
		
		SharedUtils.checkNotNull(centralHost);
		this.centralHost = centralHost;
	}

	public Integer getCentralPort() {
		
		return centralPort;
	}

	public void setCentralPort(Integer centralPort) {
		
		SharedUtils.checkNotNull(centralPort);
		this.centralPort = centralPort;
	}

	public Boolean hasPico() {
		
		return hasPico;
	}

	public void setHasPico(Boolean hasPico) {
		
		SharedUtils.checkNotNull(hasPico);
		this.hasPico = hasPico;
	}

	public Boolean hasMpv() {
		
		return hasMpv;
	}

	public void setHasMpv(Boolean hasMpv) {
		
		SharedUtils.checkNotNull(hasMpv);
		this.hasMpv = hasMpv;
	}

	public Boolean getHasPrettyLights() {
		
		return hasPrettyLights;
	}

	public void setHasPrettyLights(Boolean hasPrettyLights) {
		
		SharedUtils.checkNotNull(hasPrettyLights);
		this.hasPrettyLights = hasPrettyLights;
	}

	public Boolean getHasLEDS() {
		
		return hasLEDS;
	}

	public void setHasLEDS(Boolean hasLEDS) {
		
		SharedUtils.checkNotNull(hasLEDS);
		this.hasLEDS = hasLEDS;
	}

	public IronType getIronType() {
		
		return ironType;
	}

	public void setIronType(IronType ironType) {
		
		SharedUtils.checkNotNull(ironType);
		this.ironType = ironType;
	}

	public OsType getOsType() {
		
		return osType;
	}

	public void setOsType(OsType osType) {
		
		SharedUtils.checkNotNull(osType);
		this.osType = osType;
	}

	public Language getDefaultLanguage() {
		
		return defaultLanguage;
	}

	public void setDefaultLanguage(Language defaultLanguage) {
		
		SharedUtils.checkNotNull(defaultLanguage);
		this.defaultLanguage = defaultLanguage;
	}

	public Date getQuietStart() {
		
		return quietStart;
	}

	public void setQuietStart(Date quietStart) {
		
		SharedUtils.checkNotNull(quietStart);
		this.quietStart = quietStart;
	}

	public Date getQuietEnd() {
		
		return quietEnd;
	}

	public void setQuietEnd(Date quietEnd) {
		
		SharedUtils.checkNotNull(quietEnd);
		this.quietEnd = quietEnd;
	}

	@Override
	public SerialUUID<SerialStation> getKey() {

		return uuid;
	}

}
