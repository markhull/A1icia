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
import com.hulles.alixia.cayenne.auto._TaskStatus;

public class TaskStatus extends _TaskStatus {
    private static final long serialVersionUID = 1L; 
    private final static Integer COMPLETED_STATUS_ID = 2;
    
    public static TaskStatus findTaskStatus(Integer statusID) {
		ObjectContext context;
		TaskStatus status;
		
		SharedUtils.checkNotNull(statusID);
		context = AlixiaApplication.getEntityContext();
		status = Cayenne.objectForPK(context, TaskStatus.class, statusID);
		return status;
    }
    
    public static TaskStatus getCompletedStatus() {
		ObjectContext context;
		TaskStatus status;
		
		context = AlixiaApplication.getEntityContext();
		status = Cayenne.objectForPK(context, TaskStatus.class, COMPLETED_STATUS_ID);
		return status;
    }
    
	public static List<TaskStatus> getAllTaskStatuses() {
		ObjectContext context;
		List<TaskStatus> dbTaskStatuses;
		
		context = AlixiaApplication.getEntityContext();
		dbTaskStatuses = ObjectSelect
				.query(TaskStatus.class)
				.select(context);
		return dbTaskStatuses;
    }

	public static TaskStatus createNew() {
    	ObjectContext context;
    	TaskStatus dbTaskStatus;
    	
    	context = AlixiaApplication.getEntityContext();
        dbTaskStatus = context.newObject(TaskStatus.class);
        dbTaskStatus.setTaskStatusUuid(UUID.randomUUID().toString());
    	// NOT committed yet
    	return dbTaskStatus;
	}
}
