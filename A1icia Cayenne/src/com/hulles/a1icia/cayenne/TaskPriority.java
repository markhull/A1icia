package com.hulles.a1icia.cayenne;

import java.util.List;
import java.util.UUID;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.hulles.a1icia.cayenne.auto._TaskPriority;
import com.hulles.a1icia.tools.A1iciaUtils;

public class TaskPriority extends _TaskPriority {
    private static final long serialVersionUID = 1L; 
    
    public static TaskPriority findTaskPriority(Integer priorityID) {
		ObjectContext context;
		TaskPriority priority;
		
		A1iciaUtils.checkNotNull(priorityID);
		context = A1iciaApplication.getEntityContext();
		priority = Cayenne.objectForPK(context, TaskPriority.class, priorityID);
		return priority;
    }
    
	public static List<TaskPriority> getAllTaskPriorities() {
		ObjectContext context;
		List<TaskPriority> dbTaskPriorities;
		
		context = A1iciaApplication.getEntityContext();
		dbTaskPriorities = ObjectSelect
				.query(TaskPriority.class)
				.select(context);
		return dbTaskPriorities;
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

	public static TaskPriority createNew() {
    	ObjectContext context;
    	TaskPriority dbTaskPriority;
    	
    	context = A1iciaApplication.getEntityContext();
        dbTaskPriority = context.newObject(TaskPriority.class);
        dbTaskPriority.setTaskPriorityUuid(UUID.randomUUID().toString());
    	// NOT committed yet
    	return dbTaskPriority;
	}
}
