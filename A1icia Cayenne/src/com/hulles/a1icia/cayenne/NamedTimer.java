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
package com.hulles.a1icia.cayenne;

import java.util.List;
import java.util.UUID;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.hulles.a1icia.cayenne.auto._NamedTimer;
import com.hulles.a1icia.tools.A1iciaUtils;

public class NamedTimer extends _NamedTimer {
    private static final long serialVersionUID = 1L; 
    
    public static NamedTimer findNamedTimer(String timerName) {
		ObjectContext context;
		NamedTimer timer;

		A1iciaUtils.checkNotNull(timerName);
		context = A1iciaApplication.getEntityContext();
		timer = ObjectSelect
				.query(NamedTimer.class)
				.where(_NamedTimer.NAME.likeIgnoreCase(timerName))
				.selectOne(context);
		return timer;
    }
    public static NamedTimer findNamedTimer(Integer timerID) {
		ObjectContext context;
		NamedTimer timer;
		
		A1iciaUtils.checkNotNull(timerID);
		context = A1iciaApplication.getEntityContext();
		timer = Cayenne.objectForPK(context, NamedTimer.class, timerID);
		return timer;
    }
    
	public static List<NamedTimer> getNamedTimers() {
		ObjectContext context;
		List<NamedTimer> dbNamedTimers;
		
		context = A1iciaApplication.getEntityContext();
		dbNamedTimers = ObjectSelect
				.query(NamedTimer.class)
				.orderBy("name")
				.select(context);
		return dbNamedTimers;
    }
    
    public void commit() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
    	context.commitChanges();
    }
    
    public void rollback() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
    	context.rollbackChanges();
    }

	public void delete() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
     	context.deleteObjects(this);
    	context.commitChanges();
	}

	public static NamedTimer createNew() {
    	ObjectContext context;
    	NamedTimer dbTimer;
    	String uuidStr;
    	
    	context = A1iciaApplication.getEntityContext();
        dbTimer = context.newObject(NamedTimer.class);
		uuidStr = UUID.randomUUID().toString();
		dbTimer.setTimerUuid(uuidStr);
		
		// NOT committed yet
    	return dbTimer;
	}
}
