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
import java.util.ArrayList;
import java.util.List;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.kilo.KiloWeatherAction.WeatherCondition;
import com.hulles.a1icia.room.document.RoomActionObject;

public class KiloForecastAction extends RoomActionObject {
	private final List<ThreeHrForecast> forecasts;
	private final LocalDateTime now;
	
	public KiloForecastAction() {
	
		forecasts = new ArrayList<>(40); // every 3 hrs for 5 days
		now = LocalDateTime.now();
	}
	
	public List<ThreeHrForecast> getForecasts() {
		
		return forecasts;
	}

	public void setForecasts(List<ThreeHrForecast> forecasts) {
		
		SharedUtils.checkNotNull(forecasts);
		this.forecasts.clear();
		this.forecasts.addAll(forecasts);
	}
	
	public void addForecast(ThreeHrForecast forecast) {
		
		SharedUtils.checkNotNull(forecast);
		this.forecasts.add(forecast);
	}

	@Override
	public String getMessage() {

		return "Weather Forecast";
	}

	@Override
	public String getExplanation() {

		return "Weather forecast for the next 5 days from " + now;
	}

	public class ThreeHrForecast {
		private LocalDateTime ldt;
		private Float temperature;
		private Float pressure;
		private Float humidity;
		private Float tempMin;
		private Float tempMax;
		private List<WeatherCondition> weatherConditions;
		private Integer cloudiness; // percent
		private Float windspeed;
		private Integer winddegrees;
		private String windBearing;
		private Float rain3Hrs;
		private Float snow3Hrs;
		private String dateText;
		
		public String getDateText() {
			
			return dateText;
		}
		
		public void setDateText(String text) {
		
			SharedUtils.checkNotNull(text);
			this.dateText = text;
		}
		
		public LocalDateTime getLocalDateTime() {
			
			return ldt;
		}
		
		public void setLocalDateTime(LocalDateTime ldt) {
			
			SharedUtils.checkNotNull(ldt);
			this.ldt = ldt;
		}

		public Float getTemperature() {
			
			return temperature;
		}

		public void setTemperature(Float temperature) {
			
			SharedUtils.checkNotNull(temperature);
			this.temperature = temperature;
		}

		public Float getPressure() {
			
			return pressure;
		}

		public void setPressure(Float pressure) {
			
			SharedUtils.checkNotNull(pressure);
			this.pressure = pressure;
		}

		public Float getHumidity() {
			
			return humidity;
		}

		public void setHumidity(Float humidity) {
			
			SharedUtils.checkNotNull(humidity);
			this.humidity = humidity;
		}

		public Float getTempMin() {
			
			return tempMin;
		}

		public void setTempMin(Float tempMin) {
			
			SharedUtils.checkNotNull(tempMin);
			this.tempMin = tempMin;
		}

		public Float getTempMax() {
			
			return tempMax;
		}

		public void setTempMax(Float tempMax) {
			
			SharedUtils.checkNotNull(tempMax);
			this.tempMax = tempMax;
		}
		
		public List<WeatherCondition> getWeatherConditions() {
			
			return weatherConditions;
		}

		public void setWeatherConditions(List<WeatherCondition> weatherConditions) {
			
			SharedUtils.checkNotNull(weatherConditions);
			this.weatherConditions = weatherConditions;
		}

		public Integer getCloudiness() {
			
			return cloudiness;
		}

		public void setCloudiness(Integer cloudiness) {
			
			SharedUtils.checkNotNull(cloudiness);
			this.cloudiness = cloudiness;
		}

		public Float getWindSpeed() {
			
			return windspeed;
		}

		public void setWindSpeed(Float windspeed) {
			
			SharedUtils.checkNotNull(windspeed);
			this.windspeed = windspeed;
		}

		public Integer getWindDegrees() {
			
			return winddegrees;
		}

		public void setWindDegrees(Integer winddegrees) {
			
			SharedUtils.checkNotNull(winddegrees);
			this.winddegrees = winddegrees;
		}

		public String getWindBearing() {
			
			return windBearing;
		}

		public void setWindBearing(String windBearing) {
			
			SharedUtils.checkNotNull(windBearing);
			this.windBearing = windBearing;
		}

		public Float getRain3Hrs() {
			
			return rain3Hrs;
		}

		public void setRain3Hrs(Float rain3Hrs) {
			
			SharedUtils.nullsOkay(rain3Hrs);
			this.rain3Hrs = rain3Hrs;
		}

		public Float getSnow3Hrs() {
			
			return snow3Hrs;
		}

		public void setSnow3Hrs(Float snow3Hrs) {
			
			SharedUtils.nullsOkay(snow3Hrs);
			this.snow3Hrs = snow3Hrs;
		}
	}
}
