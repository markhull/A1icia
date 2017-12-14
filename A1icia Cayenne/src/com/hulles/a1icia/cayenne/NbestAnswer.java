package com.hulles.a1icia.cayenne;

import java.util.List;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.hulles.a1icia.cayenne.auto._NbestAnswer;
import com.hulles.a1icia.tools.A1iciaUtils;

public class NbestAnswer extends _NbestAnswer {
    private static final long serialVersionUID = 1L; 
    
    public static NbestAnswer findNbestAnswer(Integer typeID) {
		ObjectContext context;
		NbestAnswer cellarItemType;
		
		A1iciaUtils.checkNotNull(typeID);
		context = A1iciaApplication.getEntityContext();
		cellarItemType = Cayenne.objectForPK(context, NbestAnswer.class, typeID);
		return cellarItemType;
    }
    
	public static List<NbestAnswer> getNbestAnswers() {
		ObjectContext context;
		List<NbestAnswer> dbNbestAnswers;
		
		context = A1iciaApplication.getEntityContext();
		dbNbestAnswers = ObjectSelect
				.query(NbestAnswer.class)
				.select(context);
		return dbNbestAnswers;
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

	public static NbestAnswer createNew() {
    	ObjectContext context;
    	NbestAnswer dbNbestAnswer;
    	
    	context = A1iciaApplication.getEntityContext();
        dbNbestAnswer = context.newObject(NbestAnswer.class);
    	// NOT committed yet
    	return dbNbestAnswer;
	}
}
