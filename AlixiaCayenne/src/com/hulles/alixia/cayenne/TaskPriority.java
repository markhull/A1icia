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
import com.hulles.alixia.cayenne.auto._TaskPriority;

public class TaskPriority extends _TaskPriority {
    private static final long serialVersionUID = 1L; 
    
    public static TaskPriority findTaskPriority(Integer priorityID) {
		ObjectContext context;
		TaskPriority priority;
		
		SharedUtils.checkNotNull(priorityID);
		context = AlixiaApplication.getEntityContext();
		priority = Cayenne.objectForPK(context, TaskPriority.class, priorityID);
		return priority;
    }
    
	public static List<TaskPriority> getAllTaskPriorities() {
		ObjectContext context;
		List<TaskPriority> dbTaskPriorities;
		
		context = AlixiaApplication.getEntityContext();
		dbTaskPriorities = ObjectSelect
				.query(TaskPriority.class)
				.select(context);
		return dbTaskPriorities;
    }

	public static TaskPriority createNew() {
    	ObjectContext context;
    	TaskPriority dbTaskPriority;
    	
    	context = AlixiaApplication.getEntityContext();
        dbTaskPriority = context.newObject(TaskPriority.class);
        dbTaskPriority.setTaskPriorityUuid(UUID.randomUUID().toString());
    	// NOT committed yet
    	return dbTaskPriority;
	}
}
