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
package com.hulles.alixia.room.document;

import com.hulles.alixia.api.object.AlixiaClientObject;
import com.hulles.alixia.api.shared.SharedUtils;

/**
 * Wrap the AlixiaClientObject up as a RoomActionObject.
 * 
 * @author hulles
 *
 */
public final class ClientObjectWrapper extends RoomActionObject {
	private String message;
	private String explanation;
	private final AlixiaClientObject clientObject;
	
	public ClientObjectWrapper(AlixiaClientObject object) {
		
		SharedUtils.checkNotNull(object);
		this.clientObject = object;
	}
	
	public AlixiaClientObject getClientObject() {
		
		return clientObject;
	}
	
	@Override
	public String getMessage() {
		
		return message;
	}

	public void setMessage(String msg) {
		
		SharedUtils.checkNotNull(msg);
		this.message = msg;
	}
	
	@Override
	public String getExplanation() {
		
		return explanation;
	}

	public void setExplanation(String expl) {
		
		SharedUtils.checkNotNull(expl);
		this.explanation = expl;
	}
}
