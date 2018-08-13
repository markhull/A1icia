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
package com.hulles.a1icia.kilo;

import com.hulles.a1icia.room.document.RoomActionObject;
import com.hulles.a1icia.tools.A1iciaUtils;

public class KiloLocationAction extends RoomActionObject {
	private String locationIP;
	private String city;
	private String region;
	private String country;
	private Float latitude;
	private Float longitude;
	private String organization;
	private String postalCode;
	private Integer owmCityID;
	
	public Integer getOwmCityID() {
		
		return owmCityID;
	}

	public void setOwmCityID(Integer owmCityID) {
		
		A1iciaUtils.nullsOkay(owmCityID);
		this.owmCityID = owmCityID;
	}

	public String getLocationIP() {
		
		return locationIP;
	}

	public void setLocationIP(String locationIP) {
		
		A1iciaUtils.checkNotNull(locationIP);
		this.locationIP = locationIP;
	}

	public String getCity() {
		
		return city;
	}

	public void setCity(String city) {
		
		A1iciaUtils.checkNotNull(city);
		this.city = city;
	}

	public String getRegion() {
		
		return region;
	}

	public void setRegion(String region) {
		
		A1iciaUtils.checkNotNull(region);
		this.region = region;
	}

	public String getCountry() {
		
		return country;
	}

	public void setCountry(String country) {
		
		A1iciaUtils.checkNotNull(country);
		this.country = country;
	}

	public Float getLatitude() {
		
		return latitude;
	}

	public void setLatitude(Float latitude) {
		
		A1iciaUtils.checkNotNull(latitude);
		this.latitude = latitude;
	}

	public Float getLongitude() {
		
		return longitude;
	}

	public void setLongitude(Float longitude) {
		
		A1iciaUtils.checkNotNull(longitude);
		this.longitude = longitude;
	}

	public String getOrganization() {
		
		return organization;
	}

	public void setOrganization(String organization) {
		
		A1iciaUtils.checkNotNull(organization);
		this.organization = organization;
	}

	public String getPostalCode() {
		
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		
		A1iciaUtils.checkNotNull(postalCode);
		this.postalCode = postalCode;
	}

	@Override
	public String getMessage() {

		return "The current location seems to be " + city + " " + region + " " + 
				country;
	}

	@Override
	public String getExplanation() {

		return getMessage();
	}

}
