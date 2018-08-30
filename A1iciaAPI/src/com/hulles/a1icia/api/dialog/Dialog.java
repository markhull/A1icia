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
package com.hulles.a1icia.api.dialog;

import java.io.Serializable;

import com.hulles.a1icia.api.jebus.JebusBible;
import com.hulles.a1icia.api.jebus.JebusBible.JebusKey;
import com.hulles.a1icia.api.jebus.JebusHub;
import com.hulles.a1icia.api.jebus.JebusPool;
import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.remote.A1icianID;

import redis.clients.jedis.Jedis;

/**
 * A generalization of dialog-related documents.
 * 
 * @author hulles
 *
 */
public abstract class Dialog implements Serializable {
	private static final long serialVersionUID = -2608181091469557754L;
	private final Long documentID;

	public Dialog() {
		
		this.documentID = getNewDocumentID();
	}
	
	public abstract A1iciaClientObject getClientObject();
	
	public abstract A1icianID getFromA1icianID();
	
	public abstract A1icianID getToA1icianID();
	
	public Long getDocumentID() {
		
		return documentID;
	}
	
	/**
	 * Create and return a new document ID, using Jebus
	 * 
	 * @return The ID
	 */
	private static long getNewDocumentID() {
		JebusPool jebusPool;
		String key;
        
		jebusPool = JebusHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			 key = JebusBible.getStringKey(JebusKey.ALICIADOCUMENTCOUNTERKEY, jebusPool);
			return jebus.incr(key);
		}		
	}

}
