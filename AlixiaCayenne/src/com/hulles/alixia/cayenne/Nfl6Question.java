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

import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.cayenne.auto._Nfl6Question;

public class Nfl6Question extends _Nfl6Question {
    private static final long serialVersionUID = 1L; 
    
    public static Nfl6Question findNfl6Question(Integer typeID) {
		ObjectContext context;
		Nfl6Question question;
		
		SharedUtils.checkNotNull(typeID);
		context = AlixiaApplication.getEntityContext();
		question = Cayenne.objectForPK(context, Nfl6Question.class, typeID);
		return question;
    }
    
    public static List<Nfl6Question> getNfl6Question(String question) {
		ObjectContext context;
		List<Nfl6Question> dbQuestions;
    	
    	SharedUtils.checkNotNull(question);
		context = AlixiaApplication.getEntityContext();
		dbQuestions = ObjectSelect
				.query(Nfl6Question.class)
				.where(_Nfl6Question.QUESTION.eq(question))
				.select(context);
		return dbQuestions;
    }
    
	public static List<Nfl6Question> getAllNfl6Questions() {
		ObjectContext context;
		List<Nfl6Question> dbNfl6Questions;
		
		context = AlixiaApplication.getEntityContext();
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
    	
    	context = AlixiaApplication.getEntityContext();
        dbNfl6Question = context.newObject(Nfl6Question.class);
    	// NOT committed yet
    	return dbNfl6Question;
	}
}
