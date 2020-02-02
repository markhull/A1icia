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
package com.hulles.alixia.cayenne;

import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.cayenne.auto._OwmCity;

public class OwmCity extends _OwmCity {
    private static final long serialVersionUID = 1L; 
    
    public static OwmCity findOwmCity(Integer typeID) {
		ObjectContext context;
		OwmCity owmCity;
		
		SharedUtils.checkNotNull(typeID);
		context = AlixiaApplication.getEntityContext();
		owmCity = Cayenne.objectForPK(context, OwmCity.class, typeID);
		return owmCity;
    }
    
    public static OwmCity getOwmCity(Integer owmId) {
		ObjectContext context;
		OwmCity dbOwnCity;
    	
    	SharedUtils.checkNotNull(owmId);
		context = AlixiaApplication.getEntityContext();
		dbOwnCity = ObjectSelect
				.query(OwmCity.class)
				.where(_OwmCity.OWM_ID.eq(owmId))
				.selectOne(context);
		return dbOwnCity;
    }
    
    public static List<OwmCity> getOwmCities(String cityName) {
		ObjectContext context;
    	List<OwmCity> matches;
    	
    	SharedUtils.checkNotNull(cityName);
		context = AlixiaApplication.getEntityContext();
		matches = ObjectSelect
				.query(OwmCity.class)
				.where(_OwmCity.NAME.likeIgnoreCase(cityName))
				.select(context);
		return matches;
    }
    
    public static Long countOwmCities(String cityName) {
		ObjectContext context;
    	long matches;
    	
    	SharedUtils.checkNotNull(cityName);
		context = AlixiaApplication.getEntityContext();
		matches = ObjectSelect
				.query(OwmCity.class)
				.where(_OwmCity.NAME.likeIgnoreCase(cityName))
				.selectCount(context);
		return matches;
    }
    
	public static List<OwmCity> getAllOwmCities() {
		ObjectContext context;
		List<OwmCity> dbOwmCities;
		
		context = AlixiaApplication.getEntityContext();
		dbOwmCities = ObjectSelect
				.query(OwmCity.class)
				.select(context);
		return dbOwmCities;
    }
    
	public static List<String> getAllCityNames() {
		List<OwmCity> dbCities;
		List<String> cityNames;
		
		dbCities = getAllOwmCities();
		cityNames = new ArrayList<>(dbCities.size());
		dbCities.stream().forEach(c -> cityNames.add(c.name));
		return cityNames;
	}

	public static OwmCity createNew() {
    	ObjectContext context;
    	OwmCity dbOwmCity;
    	
    	context = AlixiaApplication.getEntityContext();
        dbOwmCity = context.newObject(OwmCity.class);
    	// NOT committed yet
    	return dbOwmCity;
	}

}
