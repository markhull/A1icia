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

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.cayenne.auto._NbestAnswer;

public class NbestAnswer extends _NbestAnswer {
    private static final long serialVersionUID = 1L; 
    
    public static NbestAnswer findNbestAnswer(Integer typeID) {
		ObjectContext context;
		NbestAnswer cellarItemType;
		
		SharedUtils.checkNotNull(typeID);
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
