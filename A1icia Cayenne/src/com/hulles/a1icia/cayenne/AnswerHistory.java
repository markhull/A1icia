/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
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
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
package com.hulles.a1icia.cayenne;

import java.util.List;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Query;
import org.apache.cayenne.query.SortOrder;

import com.hulles.a1icia.cayenne.auto._AnswerHistory;
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
