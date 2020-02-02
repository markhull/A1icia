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
import com.hulles.alixia.cayenne.auto._AnswerChunk;

public class AnswerChunk extends _AnswerChunk {
    private static final long serialVersionUID = 1L; 
    
    public static AnswerChunk findAnswerChunk(Integer chunkID) {
		ObjectContext context;
		AnswerChunk item;
		
		SharedUtils.checkNotNull(chunkID);
		context = AlixiaApplication.getEntityContext();
		item = Cayenne.objectForPK(context, AnswerChunk.class, chunkID);
		return item;
    }
    
	public static List<AnswerChunk> getAllAnswerChunks() {
		ObjectContext context;
		List<AnswerChunk> dbAnswerChunks;
		
		context = AlixiaApplication.getEntityContext();
		dbAnswerChunks = ObjectSelect
				.query(AnswerChunk.class)
				.select(context);
		return dbAnswerChunks;
    }

	public static AnswerChunk createNew() {
    	ObjectContext context;
    	AnswerChunk dbAnswerChunk;
    	
    	context = AlixiaApplication.getEntityContext();
        dbAnswerChunk = context.newObject(AnswerChunk.class);
    	// NOT committed yet
    	return dbAnswerChunk;
	}
}
