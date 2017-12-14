package com.hulles.a1icia.cayenne;

import java.util.List;
import java.util.UUID;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.hulles.a1icia.cayenne.auto._TaskType;
import com.hulles.a1icia.tools.A1iciaUtils;

public class TaskType extends _TaskType {
    private static final long serialVersionUID = 1L; 
    
    public static TaskType findTaskType(Integer typeID) {
		ObjectContext context;
		TaskType type;
		
		A1iciaUtils.checkNotNull(typeID);
		context = A1iciaApplication.getEntityContext();
		type = Cayenne.objectForPK(context, TaskType.class, typeID);
		return type;
    }
    
	public static List<TaskType> getAllTaskTypes() {
		ObjectContext context;
		List<TaskType> dbTaskTypes;
		
		context = A1iciaApplication.getEntityContext();
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
    	
    	context = A1iciaApplication.getEntityContext();
        dbTaskType = context.newObject(TaskType.class);
        dbTaskType.setTaskTypeUuid(UUID.randomUUID().toString());
    	// NOT committed yet
    	return dbTaskType;
	}
}
