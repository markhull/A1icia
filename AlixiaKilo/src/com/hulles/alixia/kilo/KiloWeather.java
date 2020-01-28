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
package com.hulles.alixia.kilo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.crypto.PurdahKeys;
import com.hulles.alixia.crypto.PurdahKeys.PurdahKey;
import com.hulles.alixia.kilo.KiloForecastAction.ThreeHrForecast;
import com.hulles.alixia.kilo.KiloWeatherAction.WeatherCondition;
import com.hulles.alixia.tools.ExternalAperture;

public class KiloWeather {
	private final static Logger LOGGER = LoggerFactory.getLogger(KiloWeather.class);
	private static List<KiloWeatherAction> currentWeatherQueue = null;
	private final static int QUEUETTL = 15; // minutes
	@SuppressWarnings("unused")
	private final static String DEGREESCELSIUS = "℃";
	private final static String DEGREESFARENHEIT = "℉";
	@SuppressWarnings("unused")
	private final static String DIRS[] = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",	"S", 
			"SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N"};
	private final static String DIRECTIONS[] = {"North", "North NorthEast", "NorthEast", "East NorthEast", 
			"East", "East SouthEast", "SouthEast", "South SouthEast", "South", "South SouthWest", 
			"SouthWest", "West SouthWest", "West", "West NorthWest", "NorthWest", "North NorthWest", "North"};
	private final static ZoneId DEFAULTZONE = ZoneId.systemDefault();
	
	public static KiloWeatherAction getCurrentWeather(Integer cityID) {
		String weatherJSON;
		JsonObject weatherObj;
		JsonObject coord;
		JsonArray weatherConds;
		JsonObject weatherCond;
		KiloWeatherAction owmWeather = null;
		Float coordLat;
		Float coordLon;
		WeatherCondition weatherCondition;
		List<WeatherCondition> weatherConditions;
		JsonObject main;
		Float temperature;
		Float pressure;
		Float humidity;
		Float tempMin;
		Float tempMax;
//		Integer visibility;
		JsonObject wind;
		Float windspeed;
		Integer winddegrees;
		Float windgust;
		JsonObject rain;
		Float rain3Hrs;
		JsonObject snow;
		Float snow3Hrs;
		JsonNumber windgustJson;
		JsonObject clouds;
		Integer cloudiness;
		JsonObject sys;
		String cityName;
		Long sunriseMillis;
		Long sunsetMillis;
		LocalDateTime sunriseLDT;
		LocalDateTime sunsetLDT;
		StringBuilder sb;
		LocalDateTime expiration;
		Iterator<KiloWeatherAction> weatherIterator;
		KiloWeatherAction searchWeather = null;
		PurdahKeys purdahKeys;
		String openWeatherId;
		
		SharedUtils.checkNotNull(cityID);
		purdahKeys = PurdahKeys.getInstance();
		openWeatherId = purdahKeys.getPurdahKey(PurdahKey.OPENWEATHERID);
		if (currentWeatherQueue == null) {
			currentWeatherQueue = Collections.synchronizedList(new LinkedList<>());
		}
		expiration = LocalDateTime.now().plusMinutes(QUEUETTL);
		weatherIterator = currentWeatherQueue.iterator();
		while (weatherIterator.hasNext()) {
			searchWeather = weatherIterator.next();
			if (searchWeather.getLocalDateTime().isAfter(expiration)) {
				weatherIterator.remove();
				continue;
			}
			if (searchWeather.getCityID().equals(cityID)) {
				// let the iterator keep running...
				owmWeather = searchWeather;
			}
		}
		if (owmWeather != null) {
			LOGGER.info("KiloWeather is using cached weather");
			return owmWeather;
		}
		LOGGER.info("KiloWeather is using fetched weather");
		owmWeather = new KiloWeatherAction();
		owmWeather.setCityID(cityID);
		owmWeather.setLocalDateTime(LocalDateTime.now());
		sb = new StringBuilder();
		weatherJSON = ExternalAperture.getCurrentWeatherOWM(cityID, openWeatherId);
		LOGGER.info(weatherJSON);
		try (BufferedReader reader = new BufferedReader(new StringReader(weatherJSON))) {
			try (JsonReader jsonReader = Json.createReader(reader)) {
				weatherObj = jsonReader.readObject();
				coord = weatherObj.getJsonObject("coord");
				coordLat = (float) coord.getJsonNumber("lat").doubleValue();
				owmWeather.setCoordLat(coordLat);
				coordLon = (float) coord.getJsonNumber("lon").doubleValue();
				owmWeather.setCoordLon(coordLon);
				weatherConds = weatherObj.getJsonArray("weather");
				weatherConditions = new ArrayList<>(weatherConds.size());
				sb.append("Current weather conditions are ");
				for (int ix=0; ix<weatherConds.size(); ix++) {
					weatherCond = weatherConds.getJsonObject(ix);
					weatherCondition = new WeatherCondition();
					weatherCondition.weatherCondID = weatherCond.getInt("id");
					weatherCondition.category = weatherCond.getString("main");
					if (ix > 0) {
						sb.append("; ");
					}
					sb.append(weatherCondition.category);
					weatherCondition.description = weatherCond.getString("description");
					sb.append(": ");
					sb.append(weatherCondition.description);
					weatherCondition.iconID = weatherCond.getString("icon");
					weatherConditions.add(weatherCondition);
				}
				owmWeather.setWeatherConditions(weatherConditions);
				
				main = weatherObj.getJsonObject("main");
				temperature = (float) main.getJsonNumber("temp").doubleValue();
				owmWeather.setTemperature(temperature);
				sb.append(". The temperature is " + temperature + DEGREESFARENHEIT + ".");
				owmWeather.setMessage(sb.toString());
				pressure = (float) main.getJsonNumber("pressure").doubleValue();
				owmWeather.setPressure(pressure);
				humidity = (float) main.getJsonNumber("humidity").doubleValue();
				owmWeather.setHumidity(humidity);
				tempMin = (float) main.getJsonNumber("temp_min").doubleValue();
				owmWeather.setTempMin(tempMin);
				tempMax = (float) main.getJsonNumber("temp_max").doubleValue();
				owmWeather.setTempMax(tempMax);

				// I guess visibility went away
//				visibility = weatherObj.getInt("visibility");
//				owmWeather.setVisibility(visibility);
				
				wind = weatherObj.getJsonObject("wind");
				windspeed = (float) wind.getJsonNumber("speed").doubleValue();
				owmWeather.setWindSpeed(windspeed);
				winddegrees = wind.getInt("deg");
				owmWeather.setWindDegrees(winddegrees);
				owmWeather.setWindBearing(formatBearing(winddegrees));
				windgustJson = wind.getJsonNumber("gust");
				if (windgustJson != null) {
					windgust = (float) windgustJson.doubleValue();
				} else {
					windgust = null;
				}
				owmWeather.setWindGust(windgust);
				
				clouds = weatherObj.getJsonObject("clouds");
				cloudiness = clouds.getInt("all");
				owmWeather.setCloudiness(cloudiness);
				
				rain = weatherObj.getJsonObject("rain");
				if (rain != null) {
					rain3Hrs = (float) rain.getJsonNumber("3h").doubleValue();
					owmWeather.setRain3Hrs(rain3Hrs);
				}
				
				snow = weatherObj.getJsonObject("snow");
				if (snow != null) {
					snow3Hrs = (float) snow.getJsonNumber("3h").doubleValue();
					owmWeather.setSnow3Hrs(snow3Hrs);
				}
				
				cityName = weatherObj.getString("name");
				owmWeather.setCityName(cityName);
				sys = weatherObj.getJsonObject("sys");
				sunriseMillis = sys.getJsonNumber("sunrise").longValue() * 1000L;
				sunsetMillis = sys.getJsonNumber("sunset").longValue() * 1000L;
				sunriseLDT = ldtFromMillis(sunriseMillis);
				owmWeather.setSunrise(LocalTime.from(sunriseLDT));
				sunsetLDT = ldtFromMillis(sunsetMillis);
				owmWeather.setSunset(LocalTime.from(sunsetLDT));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		currentWeatherQueue.add(owmWeather);
		return owmWeather;
	}
	
	public static KiloForecastAction getForecastWeather(Integer cityID) {
		String forecastJSON;
		JsonObject forecastObj;
		JsonArray weatherConds;
		JsonObject weatherCond;
		KiloForecastAction owmForecast = null;
		WeatherCondition weatherCondition;
		List<WeatherCondition> weatherConditions;
		JsonObject main;
		Float temperature;
		Float pressure;
		Float humidity;
		Float tempMin;
		Float tempMax;
		JsonObject wind;
		Float windspeed;
		Integer winddegrees;
		JsonObject rain;
		Float rain3Hrs;
		JsonObject snow;
		Float snow3Hrs;
		JsonObject clouds;
		Integer cloudiness;
		JsonArray forecasts;
		int cnt;
		JsonObject forecast;
		long ldtMillis;
		LocalDateTime ldt;
		ThreeHrForecast hrForecast;
		String dateText;
		PurdahKeys purdahKeys;
		String openWeatherId;
		
		purdahKeys = PurdahKeys.getInstance();
		openWeatherId = purdahKeys.getPurdahKey(PurdahKey.OPENWEATHERID);
		forecastJSON = ExternalAperture.getForecastOWM(cityID, openWeatherId);
		LOGGER.info(forecastJSON);
		try (BufferedReader reader = new BufferedReader(new StringReader(forecastJSON))) {
			try (JsonReader jsonReader = Json.createReader(reader)) {
				forecastObj = jsonReader.readObject();
				owmForecast = new KiloForecastAction();
				forecasts = forecastObj.getJsonArray("list");
				cnt = forecastObj.getInt("cnt");
				assert cnt == forecasts.size() : "Stated forecast count does not match array";
				for (int iw=0; iw<forecasts.size(); iw++) {
					forecast = forecasts.getJsonObject(iw);					
					ldtMillis = forecast.getJsonNumber("dt").longValue() * 1000L;
					ldt = ldtFromMillis(ldtMillis);
					hrForecast = owmForecast.new ThreeHrForecast();
					hrForecast.setLocalDateTime(ldt);
					
					main = forecast.getJsonObject("main");
					temperature = (float) main.getJsonNumber("temp").doubleValue();
					hrForecast.setTemperature(temperature);
					pressure = (float) main.getJsonNumber("pressure").doubleValue();
					hrForecast.setPressure(pressure);
					humidity = (float) main.getJsonNumber("humidity").doubleValue();
					hrForecast.setHumidity(humidity);
					tempMin = (float) main.getJsonNumber("temp_min").doubleValue();
					hrForecast.setTempMin(tempMin);
					tempMax = (float) main.getJsonNumber("temp_max").doubleValue();
					hrForecast.setTempMax(tempMax);
					
					weatherConds = forecast.getJsonArray("weather");
					weatherConditions = new ArrayList<>(weatherConds.size());
					for (int ix=0; ix<weatherConds.size(); ix++) {
						weatherCond = weatherConds.getJsonObject(ix);
						weatherCondition = new WeatherCondition();
						weatherCondition.weatherCondID = weatherCond.getInt("id");
						weatherCondition.category = weatherCond.getString("main");
						weatherCondition.description = weatherCond.getString("description");
						weatherCondition.iconID = weatherCond.getString("icon");
						weatherConditions.add(weatherCondition);
					}
					hrForecast.setWeatherConditions(weatherConditions);
					
					clouds = forecast.getJsonObject("clouds");
					cloudiness = clouds.getInt("all");
					hrForecast.setCloudiness(cloudiness);

					wind = forecast.getJsonObject("wind");
					windspeed = (float) wind.getJsonNumber("speed").doubleValue();
					hrForecast.setWindSpeed(windspeed);
					winddegrees = wind.getInt("deg");
					hrForecast.setWindDegrees(winddegrees);
					hrForecast.setWindBearing(formatBearing(winddegrees));

					rain = forecast.getJsonObject("rain");
					if (rain != null && !rain.isEmpty()) {
						rain3Hrs = (float) rain.getJsonNumber("3h").doubleValue();
						hrForecast.setRain3Hrs(rain3Hrs);
					}
					
					snow = forecast.getJsonObject("snow");
					if (snow != null && !snow.isEmpty()) {
						snow3Hrs = (float) snow.getJsonNumber("3h").doubleValue();
						hrForecast.setSnow3Hrs(snow3Hrs);
					}
					
					dateText = forecast.getString("dt_txt");
					hrForecast.setDateText(dateText);
					
					owmForecast.addForecast(hrForecast);
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return owmForecast;
	}
	
	private static String formatBearing(int bearing) {
		String cardinal;

		cardinal = DIRECTIONS[(int) Math.floor(((bearing + 11.25) % 360) / 22.5)];
		return cardinal + " (" + bearing + "°)";
	}
	
    public static LocalDateTime ldtFromMillis(Long millis) {
		LocalDateTime ldt;
	
		SharedUtils.checkNotNull(millis);
		ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), DEFAULTZONE);
		return ldt;
    }
	
}
