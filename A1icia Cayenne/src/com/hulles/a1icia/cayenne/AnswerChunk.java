package com.hulles.a1icia.cayenne;

import java.util.List;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.hulles.a1icia.cayenne.auto._AnswerChunk;
import com.hulles.a1icia.tools.A1iciaUtils;

public class AnswerChunk extends _AnswerChunk {
    private static final long serialVersionUID = 1L; 
    
    public static AnswerChunk findAnswerChunk(Integer chunkID) {
		ObjectContext context;
		AnswerChunk item;
		
		A1iciaUtils.checkNotNull(chunkID);
		context = A1iciaApplication.getEntityContext();
		item = Cayenne.objectForPK(context, AnswerChunk.class, chunkID);
		return item;
    }
    
	public static List<AnswerChunk> getAllAnswerChunks() {
		ObjectContext context;
		List<AnswerChunk> dbAnswerChunks;
		
		context = A1iciaApplication.getEntityContext();
		dbAnswerChunks = ObjectSelect
				.query(AnswerChunk.class)
				.select(context);
		return dbAnswerChunks;
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

	public static AnswerChunk createNew() {
    	ObjectContext context;
    	AnswerChunk dbAnswerChunk;
    	
    	context = A1iciaApplication.getEntityContext();
        dbAnswerChunk = context.newObject(AnswerChunk.class);
    	// NOT committed yet
    	return dbAnswerChunk;
	}
}
