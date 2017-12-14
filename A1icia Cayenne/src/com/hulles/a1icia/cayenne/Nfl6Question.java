package com.hulles.a1icia.cayenne;

import java.util.List;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.hulles.a1icia.cayenne.auto._Nfl6Question;
import com.hulles.a1icia.tools.A1iciaUtils;

public class Nfl6Question extends _Nfl6Question {
    private static final long serialVersionUID = 1L; 
    
    public static Nfl6Question findNfl6Question(Integer typeID) {
		ObjectContext context;
		Nfl6Question question;
		
		A1iciaUtils.checkNotNull(typeID);
		context = A1iciaApplication.getEntityContext();
		question = Cayenne.objectForPK(context, Nfl6Question.class, typeID);
		return question;
    }
    
    public static List<Nfl6Question> getNfl6Question(String question) {
		ObjectContext context;
		List<Nfl6Question> dbQuestions;
    	
    	A1iciaUtils.checkNotNull(question);
		context = A1iciaApplication.getEntityContext();
		dbQuestions = ObjectSelect
				.query(Nfl6Question.class)
				.where(_Nfl6Question.QUESTION.eq(question))
				.select(context);
		return dbQuestions;
    }
    
	public static List<Nfl6Question> getAllNfl6Questions() {
		ObjectContext context;
		List<Nfl6Question> dbNfl6Questions;
		
		context = A1iciaApplication.getEntityContext();
		dbNfl6Questions = ObjectSelect
				.query(Nfl6Question.class)
				.select(context);
		return dbNfl6Questions;
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

	public static Nfl6Question createNew() {
    	ObjectContext context;
    	Nfl6Question dbNfl6Question;
    	
    	context = A1iciaApplication.getEntityContext();
        dbNfl6Question = context.newObject(Nfl6Question.class);
    	// NOT committed yet
    	return dbNfl6Question;
	}
}
