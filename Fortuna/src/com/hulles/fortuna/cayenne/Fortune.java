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
package com.hulles.fortuna.cayenne;

import java.util.Random;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Query;

import com.hulles.fortuna.SharedUtils;
import com.hulles.fortuna.cayenne.auto._Fortune;

public class Fortune extends _Fortune {
    private static final long serialVersionUID = 1L; 
	private static final Random random = new Random();
	
	public Fortune() {
	}
    
    public static Boolean fortuneAlreadyExists(String text) {
		Query query;
		Fortune fortune;
		String target;
		ObjectContext context;
		
		SharedUtils.checkNotNull(text);
		context = FortunaApplication.getEntityContext();
		if (text.length() > 36) {
			target = text.substring(0, 36) + "%";
		} else {
			target = text;
		}
		query = ObjectSelect
				.query(Fortune.class)
				.where(_Fortune.TEXT.likeIgnoreCase(target));
		fortune = (Fortune) Cayenne.objectForQuery(context, query);
		return (fortune != null);
    }
    
    public static Fortune getRandomFortune() {
		int recCount;
		int recPos;
		Query query;
		ObjectContext context;

		context = FortunaApplication.getEntityContext();
		recCount = getRecordCount();
		recPos = random.nextInt(recCount);
		query = ObjectSelect
				.query(Fortune.class)
				.limit(1)
				.offset(recPos);
		return (Fortune) Cayenne.objectForQuery(context, query);
    }

    public static Fortune createNew() {
    	Fortune fortune;
		ObjectContext context;

		context = FortunaApplication.getEntityContext();
		fortune = context.newObject(Fortune.class);
		fortune.setText("New Fortune");
		context.commitChanges();
		return fortune;
    }
    
    public void commit() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
    	context.commitChanges();
    }
    
	private static int getRecordCount() {
     	ObjectContext context;
     	int count;
    	
    	context = FortunaApplication.getEntityContext();
    	count = (int) ObjectSelect
    			.query(Fortune.class)
    			.selectCount(context);
     	return count;
    }
}
