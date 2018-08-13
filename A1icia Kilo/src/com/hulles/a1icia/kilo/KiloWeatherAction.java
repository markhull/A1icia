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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.hulles.a1icia.room.document.RoomActionObject;
import com.hulles.a1icia.tools.A1iciaUtils;

public class KiloWeatherAction extends RoomActionObject {
	private LocalDateTime ldt;
	private Float coordLat;
	private Float coordLon;
	private List<WeatherCondition> weatherConditions;
	private Float temperature;
	private Float pressure;
	private Float humidity;
	private Float tempMin;
	private Float tempMax;
//	private Integer visibility; 
	private Float windspeed;
	private Integer winddegrees;
	private String windBearing;
	private Float windgust;
	private Integer cloudiness; // percent
	private String cityName;
	private LocalTime sunrise;
	private LocalTime sunset;
	private String summary;
	private Integer cityID;
	private Float rain3Hrs;
	private Float snow3Hrs;
	private String message;
	
	@Override
	public String getMessage() {
		
		return message;
	}

	public void setMessage(String message) {
		
		A1iciaUtils.nullsOkay(message);
		this.message = message;
	}

	public Integer getCityID() {
		
		return cityID;
	}

	public void setCityID(Integer cityID) {
		
		A1iciaUtils.checkNotNull(cityID);
		this.cityID = cityID;
	}

	public LocalDateTime getLocalDateTime() {
		
		return ldt;
	}
	
	public void setLocalDateTime(LocalDateTime ldt) {
		
		A1iciaUtils.checkNotNull(ldt);
		this.ldt = ldt;
	}
	
	public Float getCoordLat() {
		
		return coordLat;
	}
	
	public void setCoordLat(Float coordLat) {
		
		A1iciaUtils.checkNotNull(coordLat);
		this.coordLat = coordLat;
	}
	
	public Float getCoordLon() {
		
		return coordLon;
	}
	
	public void setCoordLon(Float coordLon) {
		
		A1iciaUtils.checkNotNull(coordLon);
		this.coordLon = coordLon;
	}
	
	public List<WeatherCondition> getWeatherConditions() {
		
		return weatherConditions;
	}

	public void setWeatherConditions(List<WeatherCondition> weatherConditions) {
		
		A1iciaUtils.checkNotNull(weatherConditions);
		this.weatherConditions = weatherConditions;
	}

	public Float getTemperature() {
		
		return temperature;
	}

	public void setTemperature(Float temperature) {
		
		A1iciaUtils.checkNotNull(temperature);
		this.temperature = temperature;
	}

	public Float getPressure() {
		
		return pressure;
	}

	public void setPressure(Float pressure) {
		
		A1iciaUtils.checkNotNull(pressure);
		this.pressure = pressure;
	}

	public Float getHumidity() {
		
		return humidity;
	}

	public void setHumidity(Float humidity) {
		
		A1iciaUtils.checkNotNull(humidity);
		this.humidity = humidity;
	}

	public Float getTempMin() {
		
		return tempMin;
	}

	public void setTempMin(Float tempMin) {
		
		A1iciaUtils.checkNotNull(tempMin);
		this.tempMin = tempMin;
	}

	public Float getTempMax() {
		
		return tempMax;
	}

	public void setTempMax(Float tempMax) {
		
		A1iciaUtils.checkNotNull(tempMax);
		this.tempMax = tempMax;
	}
//
//	public Integer getVisibility() {
//		
//		return visibility;
//	}
//
//	public void setVisibility(Integer visibility) {
//		
//		this.visibility = visibility;
//	}

	public Float getWindSpeed() {
		
		return windspeed;
	}

	public void setWindSpeed(Float windspeed) {
		
		A1iciaUtils.checkNotNull(windspeed);
		this.windspeed = windspeed;
	}

	public Integer getWindDegrees() {
		
		return winddegrees;
	}

	public void setWindDegrees(Integer winddegrees) {
		
		A1iciaUtils.checkNotNull(winddegrees);
		this.winddegrees = winddegrees;
	}

	public String getWindBearing() {
		
		return windBearing;
	}

	public void setWindBearing(String windBearing) {
		
		A1iciaUtils.checkNotNull(windBearing);
		this.windBearing = windBearing;
	}

	public Float getWindGust() {
		
		return windgust;
	}

	public void setWindGust(Float windgust) {
		
		A1iciaUtils.nullsOkay(windgust);
		this.windgust = windgust;
	}

	public Integer getCloudiness() {
		
		return cloudiness;
	}

	public void setCloudiness(Integer cloudiness) {
		
		A1iciaUtils.checkNotNull(cloudiness);
		this.cloudiness = cloudiness;
	}

	public Float getRain3Hrs() {
		
		return rain3Hrs;
	}

	public void setRain3Hrs(Float rain3Hrs) {
		
		A1iciaUtils.nullsOkay(rain3Hrs);
		this.rain3Hrs = rain3Hrs;
	}

	public Float getSnow3Hrs() {
		
		return snow3Hrs;
	}

	public void setSnow3Hrs(Float snow3Hrs) {
		
		A1iciaUtils.nullsOkay(snow3Hrs);
		this.snow3Hrs = snow3Hrs;
	}

	public String getCityName() {
		
		return cityName;
	}

	public void setCityName(String cityName) {
		
		A1iciaUtils.checkNotNull(cityName);
		this.cityName = cityName;
	}

	public LocalTime getSunrise() {
		
		return sunrise;
	}

	public void setSunrise(LocalTime sunrise) {
		
		A1iciaUtils.checkNotNull(sunrise);
		this.sunrise = sunrise;
	}

	public LocalTime getSunset() {
		
		return sunset;
	}

	public void setSunset(LocalTime sunset) {
		
		A1iciaUtils.checkNotNull(sunset);
		this.sunset = sunset;
	}

	public String getSummary() {
		
		return summary;
	}

	public void setSummary(String summary) {
		
		A1iciaUtils.checkNotNull(summary);
		this.summary = summary;
	}

	@Override
	public String getExplanation() {
		StringBuilder sb;
		int condIx = 0;
	
		sb = new StringBuilder();
		sb.append("The current weather for ");
		sb.append(getCityName());
		sb.append(" is:\n");
		for (WeatherCondition cond : getWeatherConditions()) {
			if (condIx++ > 0) {
				sb.append(", and ");
			}
			sb.append(cond.category);
			sb.append(": ");
			sb.append(cond.description);
		}
		sb.append("\n");
		sb.append("Temperature: ");
		sb.append(getTemperature());
		sb.append("°F\n");
		sb.append("Pressure: ");
		sb.append(getPressure());
		sb.append("\n");
		sb.append("Humidity: ");
		sb.append(getHumidity());
		sb.append("%\n");
		sb.append("Cloudiness: ");
		sb.append(getCloudiness());
		sb.append("%\n");
		sb.append("Windspeed: ");
		sb.append(getWindSpeed());
		sb.append(" mph, out of the ");
		sb.append(getWindBearing());
		if (getWindGust() != null) {
			sb.append(", gusting to ");
			sb.append(getWindGust());
			sb.append(" mph\n");
		} else {
			sb.append("\n");
		}
		if (getRain3Hrs() != null) {
			sb.append("Rain: ");
			sb.append(getRain3Hrs());
			sb.append("\" in the last 3 hours\n");
		}
		if (getSnow3Hrs() != null) {
			sb.append("Snow: ");
			sb.append(getSnow3Hrs());
			sb.append("\" in the last 3 hours\n");
		}
//		sb.append("Visibility: ");
//		sb.append(getVisibility());
//		sb.append("\n");
		sb.append("Sunrise: ");
		sb.append(getSunrise());
		sb.append("\n");
		sb.append("Sunset: ");
		sb.append(getSunset());
		sb.append("\n");
		return sb.toString();
	}
	
	public static class WeatherCondition {
		public Integer weatherCondID;
		public String category;
		public String description;
		public String iconID;
	}

}
