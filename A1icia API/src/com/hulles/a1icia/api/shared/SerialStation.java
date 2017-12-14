package com.hulles.a1icia.api.shared;

import java.util.Date;

import com.hulles.a1icia.api.remote.Station.IronType;
import com.hulles.a1icia.api.remote.Station.OsType;
import com.hulles.a1icia.media.Language;

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
