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
package com.hulles.a1icia.room.document;

import com.hulles.a1icia.tools.A1iciaUtils;
import com.hulles.a1icia.api.object.A1iciaClientObject;

/**
 * Wrap the A1iciaClientObject up as a RoomActionObject.
 * 
 * @author hulles
 *
 */
public final class ClientObjectWrapper extends RoomActionObject {
	private String message;
	private String explanation;
	private A1iciaClientObject clientObject;
	
	public ClientObjectWrapper(A1iciaClientObject object) {
		
		A1iciaUtils.checkNotNull(object);
		this.clientObject = object;
	}
	
	public A1iciaClientObject getClientObject() {
		
		return clientObject;
	}
	
	@Override
	public String getMessage() {
		
		return message;
	}

	public void setMessage(String msg) {
		
		A1iciaUtils.checkNotNull(msg);
		this.message = msg;
	}
	
	@Override
	public String getExplanation() {
		
		return explanation;
	}

	public void setExplanation(String expl) {
		
		A1iciaUtils.checkNotNull(expl);
		this.explanation = expl;
	}
}
