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
import com.hulles.alixia.cayenne.auto._Lemma;

public class Lemma extends _Lemma {
	private final static String DUMMY_LEMMA = "EDIT_ME";
    private static final long serialVersionUID = 1L; 
    
    public static Lemma findLemma(Integer lemmaID) {
		ObjectContext context;
		Lemma lemma;
		
		SharedUtils.checkNotNull(lemmaID);
		context = AlixiaApplication.getEntityContext();
		lemma = Cayenne.objectForPK(context, Lemma.class, lemmaID);
		return lemma;
    }

    public static boolean lemmaExists(String word, String pos, String lemma) {
		ObjectContext context;
		Lemma dbLemma = null;
		
		SharedUtils.checkNotNull(word);
		SharedUtils.checkNotNull(pos);
		SharedUtils.checkNotNull(lemma);
		context = AlixiaApplication.getEntityContext();
		dbLemma = ObjectSelect
				.query(Lemma.class)
				.where(_Lemma.WORD.eq(word)
						.andExp(_Lemma.POS.eq(pos))
						.andExp(_Lemma.LEMMA.eq(lemma)))
				.selectOne(context);
		return dbLemma != null;
    }
    
    public static String getDummyLemmaTag() {
    	
    	return DUMMY_LEMMA;
    }
    
	public static List<Lemma> getAllLemmas() {
		ObjectContext context;
		List<Lemma> dbLemmas;
		
		context = AlixiaApplication.getEntityContext();
		dbLemmas = ObjectSelect
				.query(Lemma.class)
				.orderBy("word")
				.orderBy("pos")
				.where(_Lemma.LEMMA.ne(DUMMY_LEMMA))
				.select(context);
		return dbLemmas;
    }
	
    public static List<Lemma> getLemmas(String word, String pos) {
		ObjectContext context;
		List<Lemma> dbLemmas = null;
		
		SharedUtils.checkNotNull(word);
		SharedUtils.checkNotNull(pos);
		context = AlixiaApplication.getEntityContext();
		dbLemmas = ObjectSelect
				.query(Lemma.class)
				.where(_Lemma.WORD.eq(word)
						.andExp(_Lemma.POS.eq(pos))
						.andExp(_Lemma.LEMMA.ne(DUMMY_LEMMA)))
				.select(context);
		return dbLemmas;
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

	public static Lemma createNew() {
    	ObjectContext context;
    	Lemma dbLemma;
    	
    	context = AlixiaApplication.getEntityContext();
        dbLemma = context.newObject(Lemma.class);
    	// NOT committed yet
    	return dbLemma;
	}
}
