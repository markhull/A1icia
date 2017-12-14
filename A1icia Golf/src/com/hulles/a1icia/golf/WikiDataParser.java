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
package com.hulles.a1icia.golf;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import com.hulles.a1icia.jebus.JebusBible;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.jebus.JebusPool;
import com.hulles.a1icia.tools.ExternalAperture;
import com.hulles.a1icia.tools.A1iciaUtils;

import redis.clients.jedis.Jedis;

public class WikiDataParser {
	private final static Logger logger = Logger.getLogger("A1iciaGolf.WikiDataParser");
	private final static Level LOGLEVEL = Level.FINE;
	private final static String DATEPARSE = "+yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static SimpleDateFormat sdf = null;
	
	WikiDataParser() {
	}
	
	public static List<WikiDataSearchResult> parseSearch(String searchResult) {
        JsonObject search;
        JsonArray searchBody;
        int bodyCount;
        JsonObject sbo;
        JsonReader reader;
        StringReader sReader;
        WikiDataSearchResult wdsr;
        List<WikiDataSearchResult> results;
        String idStr;
        String labelStr;
        String descStr;
		
		A1iciaUtils.checkNotNull(searchResult);
        sReader = new StringReader(searchResult);
        reader = Json.createReader(sReader);
        search = reader.readObject();
        reader.close();
        logger.log(LOGLEVEL,"Searchinfo: search:" + search.getJsonObject("searchinfo").getJsonString("search"));
        logger.log(LOGLEVEL,"Search-continue: " + search.getJsonNumber("search-continue"));
        logger.log(LOGLEVEL,"Success: " + search.getJsonNumber("success"));
        searchBody = search.getJsonArray("search");
        bodyCount = searchBody.size();
        logger.log(LOGLEVEL,"Search body length: " + bodyCount);
        results = new ArrayList<>(bodyCount);
        for (int ix = 0; ix < bodyCount; ix++) {
        	sbo = searchBody.getJsonObject(ix);
        	descStr = sbo.getString("description", "");
        	if (!descStr.isEmpty()) {
        		if (descStr.endsWith("disambiguation page")) {
        			continue;
        		}
            	idStr = sbo.getString("id");
            	labelStr = sbo.getString("label", "");
	        	wdsr = new WikiDataSearchResult(idStr, labelStr, descStr);
	        	results.add(wdsr);
        	}
        }
        for (WikiDataSearchResult sr : results) {
        	logger.log(LOGLEVEL,"Desc: " + sr.getDescription());
        }
        return results;
	}
	
	public static List<WikiDataEntity> parseEntities(String entityStr, boolean isSecondaryLookup) {
        JsonObject entity;
        JsonObject entityBody;
        JsonReader reader;
        Collection<JsonValue> values;
        StringReader sReader;
        List<WikiDataEntity> results;
        WikiDataEntity result;
		DateFormat df;
		
		A1iciaUtils.checkNotNull(entityStr);
		if (sdf == null) {
			df = DateFormat.getDateTimeInstance();
			sdf = (SimpleDateFormat)df;
			sdf.applyPattern(DATEPARSE);
		}
        sReader = new StringReader(entityStr);
        reader = Json.createReader(sReader);
        entity = reader.readObject();
        reader.close();
        logger.log(LOGLEVEL,"Success: " + entity.getJsonNumber("success"));
        entityBody = entity.getJsonObject("entities");
        values = entityBody.values();
        results = new ArrayList<>();
        for (JsonValue value : values) {
        	if (isSecondaryLookup) {
        		result = parseSecondaryEntity((JsonObject)value);
        	} else {
        		result = parseEntity((JsonObject)value);
        	}
        	results.add(result);
        }
        return results;
	}

	private static WikiDataEntity parseEntity(JsonObject entity) {
		JsonObject labels;
		String label;
		JsonObject descriptions;
		String description;
		JsonObject claims;
		JsonObject aliases;
		JsonArray aliasArray;
		JsonObject alias;
		String aliasStr;
		Collection<JsonValue> propSet;
		JsonArray propArray;
		JsonObject prop;
		JsonObject mainSnak;
		String propID;
		WikiDataEntity wdEntity;
		WikiDataClaim claim;
		Object dataValue;
		String dataType;
		String wdID;
		String propLabel;
		StringBuilder secondaryLookupIDs;
		String secondaryLookup;
		List<WikiDataEntity> secondaryEntities;
		JebusPool jebusPool;
		String hashKey;
		
		A1iciaUtils.checkNotNull(entity);
		jebusPool = JebusHub.getJebusLocal();
		wdEntity = new WikiDataEntity();
		logger.log(LOGLEVEL,"Type: " + entity.getString("type", "(no type)"));
		labels = entity.getJsonObject("labels");
		if (labels == null) {
			label = "(no label)";
		} else {
			labels = labels.getJsonObject("en");
			if (labels == null) {
				label = "(no label)";
			} else {
				label = labels.getString("value", "(no label)");
			}
		}
		logger.log(LOGLEVEL,"Labels: " + label);
		wdEntity.setLabel(label);
		descriptions = entity.getJsonObject("descriptions");
		if (descriptions == null) {
			description = "(no description)";
		} else {
			descriptions = descriptions.getJsonObject("en");
			if (descriptions == null) {
				description = "(no description)";
			} else {
				description = descriptions.getString("value", "(no description)");
			}
		}
		logger.log(LOGLEVEL,"Descriptions: " + description);
		wdEntity.setDescription(description);
		aliases = entity.getJsonObject("aliases");
		if (aliases == null) {
			aliasStr = "(no alias)";
		} else {
			aliasArray = aliases.getJsonArray("en");
			if (aliasArray != null) {
				for (JsonValue value : aliasArray) {
					alias = (JsonObject)value;
					aliasStr = alias.getString("value", "(no alias)");
					logger.log(LOGLEVEL,"Alias: " + aliasStr);
					wdEntity.addAlias(aliasStr);
				}
			}
		}
		claims = entity.getJsonObject("claims");
		if (claims == null) {
			jebusPool.close();
			return wdEntity;
		}
		propSet = claims.values();
		for (JsonValue value : propSet) {
			propArray = (JsonArray)value;
			for (JsonValue p : propArray) {
				prop = (JsonObject)p;
				mainSnak = prop.getJsonObject("mainsnak");
				propID = mainSnak.getString("property", "(no property)");
				try (Jedis jebus = jebusPool.getResource()) {
					hashKey = JebusBible.getWikiDataHashKey(jebusPool, propID);
					propLabel = jebus.hget(hashKey, JebusBible.getWikiDataLabelField(jebusPool));
				}
				if (propLabel == null) {
					A1iciaUtils.error("Property " + propID + " not in map");
					continue;
				}
				dataType = mainSnak.getString("datatype");
				dataValue = parseDataValue(mainSnak.getJsonObject("datavalue"), dataType);
				logger.log(LOGLEVEL, "Property: " + propID + " " + propLabel + " : " +
							dataValue.toString());
				claim = new WikiDataClaim(propID);
				claim.setLabel(propLabel);
				claim.setValue(dataValue);
				claim.setDataType(dataType);
				wdEntity.addClaim(claim);
			}
		}
		secondaryLookupIDs = new StringBuilder();
		for (WikiDataClaim cl : wdEntity.getClaims()) {
			if (cl.getDataType().equals("wikibase-item")) {
				wdID = (String)cl.getValue();
				if (secondaryLookupIDs.length() > 0) {
					secondaryLookupIDs.append("|");
				}
				secondaryLookupIDs.append(wdID);
			}
		}
		if (secondaryLookupIDs.length() > 0) {
			secondaryLookup = ExternalAperture.getWikiDataByID(secondaryLookupIDs.toString());
			secondaryEntities = parseEntities(secondaryLookup, true);
			for (WikiDataClaim cl : wdEntity.getClaims()) {
				if (cl.getDataType().equals("wikibase-item")) {
					wdID = (String)cl.getValue();
					for (WikiDataEntity wd : secondaryEntities) {
						if (wdID.equals(wd.getqID())) {
							logger.log(LOGLEVEL,"Successfully updated " + wdID);
							cl.setSecondaryLabel(wd.getLabel());
						}
					}
				}
			}
		}
		jebusPool.close();
		return wdEntity;
	}

	private static WikiDataEntity parseSecondaryEntity(JsonObject entity) {
		JsonObject labels;
		String label;
		WikiDataEntity wdEntity;
		
		A1iciaUtils.checkNotNull(entity);
		wdEntity = new WikiDataEntity();
		logger.log(LOGLEVEL,"Type: " + entity.getString("type", "(no type)"));
		labels = entity.getJsonObject("labels");
		if (labels == null) {
			label = "(no label)";
		} else {
			labels = labels.getJsonObject("en");
			if (labels == null) {
				label = "(no label)";
			} else {
				label = labels.getString("value", "(no label)");
			}
		}
		logger.log(LOGLEVEL,"Labels: " + label);
		wdEntity.setLabel(label);
		return wdEntity;
	}
	
	private static Object parseDataValue(JsonObject datavalue, String datatype) {
		JsonObject value;
		String type;
		Integer number;
		Object result;
		String time;
		java.util.Date date;
		String idString;
		
		A1iciaUtils.checkNotNull(datavalue);
		A1iciaUtils.checkNotNull(datatype);
		type = datavalue.getString("type");
		switch (datatype) {
			case "commonsMedia":
				if (type.equals("string")) {
					// file name
					result = datavalue.getString("value", "(no value)");
					return result;
				} 
				A1iciaUtils.error("commonsMedia expected string, got " + type);
				break;
			case "string":
				if (type.equals("string")) {
					result = datavalue.getString("value", "(no value)");
					return result;
				} 
				A1iciaUtils.error("string expected string, got " + type);
				break;
			case "monolingualtext":
				if (type.equals("monolingualtext")) {
					result = datavalue.getString("value", "(no value)");
					return result;
				} 
				A1iciaUtils.error("monolingualtext expected monolingualtext, got " + type);
				break;
			case "time":
				if (type.equals("time")) {
					value = datavalue.getJsonObject("value");
					time = value.getString("time", "(no time)");
					date = null;
					try {
						date = sdf.parse(time);
					} catch (ParseException e) {
						A1iciaUtils.error("Can't parse date/time string " + time);
						e.printStackTrace();
					}
					return date;
				} 
				A1iciaUtils.error("time expected time, got " + type);
				break;
			case "wikibase-item":
				if (type.equals("wikibase-entityid")) {
					value = datavalue.getJsonObject("value");
					number = value.getInt("numeric-id");
					idString = "Q" + number;
					return idString;
				}
				A1iciaUtils.error("wikibase-item expected wikibase-entityid, got " + type);
				break;
			case "external-id":
				if (type.equals("string")) {
					result = datavalue.getString("value", "(no value)");
					return result;
				}
				A1iciaUtils.error("external-id expected string, got " + type);
				break;
			default:
				result = "Unknown datatype " + datatype;
				return result;
		}
		result = "(no result)";
		return result;
	}
	
}
