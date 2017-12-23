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
package com.hulles.a1icia.api.dialog;

import java.io.Serializable;

import com.hulles.a1icia.api.jebus.JebusApiBible;
import com.hulles.a1icia.api.jebus.JebusApiHub;
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
	
	private static long getNewDocumentID() {
		JebusPool jebusPool;
		
		jebusPool = JebusApiHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			return jebus.incr(JebusApiBible.getA1iciaDocumentCounterKey(jebusPool));
		}		
	}

}
