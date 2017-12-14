/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of A1icia.
 *  
 * A1icia is free software: you can redistribute it and/or modify
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
 *******************************************************************************/
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
