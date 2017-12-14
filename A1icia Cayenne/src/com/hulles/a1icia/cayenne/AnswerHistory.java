package com.hulles.a1icia.cayenne;

import java.util.List;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Query;
import org.apache.cayenne.query.SortOrder;

import com.hulles.a1icia.cayenne.auto._AnswerHistory;
import com.hulles.a1icia.ticket.SparkObjectType;
import com.hulles.a1icia.tools.A1iciaUtils;

public class AnswerHistory extends _AnswerHistory {
    private static final long serialVersionUID = 1L; 
    
    public static AnswerHistory findAnswerHistory(Integer typeID) {
		ObjectContext context;
		AnswerHistory item;
		
		A1iciaUtils.checkNotNull(typeID);
		context = A1iciaApplication.getEntityContext();
		item = Cayenne.objectForPK(context, AnswerHistory.class, typeID);
		return item;
    }
    public static AnswerHistory findAnswerHistory(String exactMatch) {
		Query query;
		ObjectContext context;

		A1iciaUtils.checkNotNull(exactMatch);
		context = A1iciaApplication.getEntityContext();
		query = ObjectSelect
				.query(AnswerHistory.class)
				.where(_AnswerHistory.ORIGINAL_QUESTION.eq(exactMatch));
		return (AnswerHistory) Cayenne.objectForQuery(context, query);
    }
    
    public static List<AnswerHistory> getAnswerHistory(String question) {
		ObjectContext context;
		List<AnswerHistory> history;
    	
    	A1iciaUtils.checkNotNull(question);
		context = A1iciaApplication.getEntityContext();
		history = ObjectSelect
				.query(AnswerHistory.class)
				.where(_AnswerHistory.LEMMATIZED_QUESTION.eq(question))
				.orderBy("satisfaction", SortOrder.DESCENDING)
				.select(context);
		return history;
    }
    
	public static List<AnswerHistory> getAllAnswerHistory() {
		ObjectContext context;
		List<AnswerHistory> dbAnswerHistorys;
		
		context = A1iciaApplication.getEntityContext();
		dbAnswerHistorys = ObjectSelect
				.query(AnswerHistory.class)
				.select(context);
		return dbAnswerHistorys;
    }
	
	public SparkObjectType getSparkObjectType() {
		Short code;
		
		code = getSparkObjectTypeCode();
		return SparkObjectType.findSparkObjectType(code);
	}
	
	public void setSparkObjectType(SparkObjectType type) {
		Short code;
		
		A1iciaUtils.checkNotNull(type);
		code = type.getStoreID();
		setSparkObjectTypeCode(code);
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

	public static AnswerHistory createNew() {
    	ObjectContext context;
    	AnswerHistory dbAnswerHistory;
    	
    	context = A1iciaApplication.getEntityContext();
        dbAnswerHistory = context.newObject(AnswerHistory.class);
    	// NOT committed yet
    	return dbAnswerHistory;
	}
}
