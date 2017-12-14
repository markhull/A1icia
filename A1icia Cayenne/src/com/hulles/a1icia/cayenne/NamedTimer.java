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
