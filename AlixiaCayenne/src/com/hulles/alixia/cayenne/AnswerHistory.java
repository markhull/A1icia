/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of Alixia.
 *  
 * Alixia is free software: you can redistribute it and/or modify
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
package com.hulles.alixia.cayenne;

import java.util.List;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Query;
import org.apache.cayenne.query.SortOrder;

import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.cayenne.auto._AnswerHistory;

public class AnswerHistory extends _AnswerHistory {
    private static final long serialVersionUID = 1L; 
    
    public static AnswerHistory findAnswerHistory(Integer typeID) {
		ObjectContext context;
		AnswerHistory item;
		
		SharedUtils.checkNotNull(typeID);
		context = AlixiaApplication.getEntityContext();
		item = Cayenne.objectForPK(context, AnswerHistory.class, typeID);
		return item;
    }
    public static AnswerHistory findAnswerHistory(String exactMatch) {
		Query query;
		ObjectContext context;

		SharedUtils.checkNotNull(exactMatch);
		context = AlixiaApplication.getEntityContext();
		query = ObjectSelect
				.query(AnswerHistory.class)
				.where(_AnswerHistory.ORIGINAL_QUESTION.eq(exactMatch));
		return (AnswerHistory) Cayenne.objectForQuery(context, query);
    }
    
    public static List<AnswerHistory> getAnswerHistory(String question) {
		ObjectContext context;
		List<AnswerHistory> history;
    	
    	SharedUtils.checkNotNull(question);
		context = AlixiaApplication.getEntityContext();
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
		
		context = AlixiaApplication.getEntityContext();
		dbAnswerHistorys = ObjectSelect
				.query(AnswerHistory.class)
				.select(context);
		return dbAnswerHistorys;
    }

	public static AnswerHistory createNew() {
    	ObjectContext context;
    	AnswerHistory dbAnswerHistory;
    	
    	context = AlixiaApplication.getEntityContext();
        dbAnswerHistory = context.newObject(AnswerHistory.class);
    	// NOT committed yet
    	return dbAnswerHistory;
	}
}
