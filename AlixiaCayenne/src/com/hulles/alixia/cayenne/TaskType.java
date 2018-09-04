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

import java.util.List;
import java.util.UUID;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.cayenne.auto._TaskType;

public class TaskType extends _TaskType {
    private static final long serialVersionUID = 1L; 
    
    public static TaskType findTaskType(Integer typeID) {
		ObjectContext context;
		TaskType type;
		
		SharedUtils.checkNotNull(typeID);
		context = AlixiaApplication.getEntityContext();
		type = Cayenne.objectForPK(context, TaskType.class, typeID);
		return type;
    }
    
	public static List<TaskType> getAllTaskTypes() {
		ObjectContext context;
		List<TaskType> dbTaskTypes;
		
		context = AlixiaApplication.getEntityContext();
		dbTaskTypes = ObjectSelect
				.query(TaskType.class)
				.select(context);
		return dbTaskTypes;
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

	public static TaskType createNew() {
    	ObjectContext context;
    	TaskType dbTaskType;
    	
    	context = AlixiaApplication.getEntityContext();
        dbTaskType = context.newObject(TaskType.class);
        dbTaskType.setTaskTypeUuid(UUID.randomUUID().toString());
    	// NOT committed yet
    	return dbTaskType;
	}
}
