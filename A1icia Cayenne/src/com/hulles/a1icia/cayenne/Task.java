package com.hulles.a1icia.cayenne;

import java.util.List;
import java.util.UUID;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;

import com.hulles.a1icia.api.shared.SerialPerson;
import com.hulles.a1icia.api.shared.SerialUUID;
import com.hulles.a1icia.cayenne.auto._Task;
import com.hulles.a1icia.tools.A1iciaUtils;

public class Task extends _Task {
    private static final long serialVersionUID = 1L; 
    
    public static Task findTask(Integer taskID) {
		ObjectContext context;
		Task task;
		
		A1iciaUtils.checkNotNull(taskID);
		context = A1iciaApplication.getEntityContext();
		task = Cayenne.objectForPK(context, Task.class, taskID);
		return task;
    }
    
	public static List<Task> getAllTasks() {
		ObjectContext context;
		List<Task> dbTasks;
		
		context = A1iciaApplication.getEntityContext();
		dbTasks = ObjectSelect
				.query(Task.class)
				.orderBy("dateDue", SortOrder.DESCENDING)
				.select(context);
		return dbTasks;
    }
    
	public static List<Task> getTasks(SerialUUID<SerialPerson> personUUID) {
		Ordering ordering;
		List<Task> dbTasks;
		Person person;
		
		A1iciaUtils.checkNotNull(personUUID);
		person = Person.findPerson(personUUID);
		if (person == null) {
			return null;
		}
		ordering = new Ordering("dateDue", SortOrder.DESCENDING);
		dbTasks = person.getTasks();
		ordering.orderList(dbTasks);
		return dbTasks;
    }
    
	public static List<Task> getUncompletedTasks(SerialUUID<SerialPerson> personUUID) {
		ObjectContext context;
		List<Task> dbTasks;
		Person person;
		TaskStatus completedStatus;
		
		A1iciaUtils.checkNotNull(personUUID);
		person = Person.findPerson(personUUID);
		if (person == null) {
			return null;
		}
		completedStatus = TaskStatus.getCompletedStatus();
		context = A1iciaApplication.getEntityContext();
		dbTasks = ObjectSelect
				.query(Task.class)
				.where(_Task.PERSON.eq(person)
						.andExp(_Task.TASK_STATUS.ne(completedStatus)))
				.orderBy("dateDue", SortOrder.DESCENDING)
				.select(context);
		return dbTasks;
    }
	
/*    public static List<Task> getTask(String word, String pos) {
		ObjectContext context;
		List<Task> dbTasks = null;
		
		A1iciaUtils.checkNotNull(word);
		A1iciaUtils.checkNotNull(pos);
		context = A1iciaApplication.getEntityContext();
		dbTasks = ObjectSelect
				.query(Task.class)
				.where(_Task.WORD.eq(word)
						.andExp(_Task.POS.eq(pos))
						.andExp(_Task.LEMMA.ne(DUMMY_LEMMA)))
				.select(context);
		return dbTasks;
    }
*/    
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

	public static Task createNew() {
    	ObjectContext context;
    	Task dbTask;
    	
    	context = A1iciaApplication.getEntityContext();
        dbTask = context.newObject(Task.class);
        dbTask.setTaskUuid(UUID.randomUUID().toString());
    	// NOT committed yet
    	return dbTask;
	}
}
