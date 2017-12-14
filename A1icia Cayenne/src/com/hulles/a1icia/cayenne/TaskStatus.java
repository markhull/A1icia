package com.hulles.a1icia.cayenne;

import java.util.List;
import java.util.UUID;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.hulles.a1icia.cayenne.auto._TaskStatus;
import com.hulles.a1icia.tools.A1iciaUtils;

public class TaskStatus extends _TaskStatus {
    private static final long serialVersionUID = 1L; 
    private final static Integer COMPLETED_STATUS_ID = 2;
    
    public static TaskStatus findTaskStatus(Integer statusID) {
		ObjectContext context;
		TaskStatus status;
		
		A1iciaUtils.checkNotNull(statusID);
		context = A1iciaApplication.getEntityContext();
		status = Cayenne.objectForPK(context, TaskStatus.class, statusID);
		return status;
    }
    
    public static TaskStatus getCompletedStatus() {
		ObjectContext context;
		TaskStatus status;
		
		context = A1iciaApplication.getEntityContext();
		status = Cayenne.objectForPK(context, TaskStatus.class, COMPLETED_STATUS_ID);
		return status;
    }
    
	public static List<TaskStatus> getAllTaskStatuses() {
		ObjectContext context;
		List<TaskStatus> dbTaskStatuses;
		
		context = A1iciaApplication.getEntityContext();
		dbTaskStatuses = ObjectSelect
				.query(TaskStatus.class)
				.select(context);
		return dbTaskStatuses;
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

	public static TaskStatus createNew() {
    	ObjectContext context;
    	TaskStatus dbTaskStatus;
    	
    	context = A1iciaApplication.getEntityContext();
        dbTaskStatus = context.newObject(TaskStatus.class);
        dbTaskStatus.setTaskStatusUuid(UUID.randomUUID().toString());
    	// NOT committed yet
    	return dbTaskStatus;
	}
}
