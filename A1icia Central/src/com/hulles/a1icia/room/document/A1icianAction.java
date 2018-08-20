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
package com.hulles.a1icia.room.document;

import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;

/**
 * This represents an action for a receiving A1ician, for example turning on a red LED. This isn't at
 * the Station level, because we can have multiple "red LEDs" in software, for different users on
 * the same web server e.g. But it could also be implemented in hardware, as for the A1icia Pi Mirror.
 * 
 * @author hulles
 *
 */
public class A1icianAction extends RoomActionObject {
	private String message;
	private String explanation;
	private SerialSememe clientAction;
	private A1icianID toA1icianID;
	
	public A1icianID getToA1icianID() {
		
		return toA1icianID;
	}

	public void setToA1icianID(A1icianID toA1icianID) {
		
		SharedUtils.nullsOkay(toA1icianID);
		this.toA1icianID = toA1icianID;
	}

	public SerialSememe getClientAction() {
		
		return clientAction;
	}

	public void setClientAction(SerialSememe action) {
		
		SharedUtils.nullsOkay(action);
		this.clientAction = action;
	}

	@Override
	public String getMessage() {

		return message;
	}

	public void setMessage(String message) {
		
		SharedUtils.checkNotNull(message);
		this.message = message;
	}
	
	@Override
	public String getExplanation() {

		return explanation;
	}

	public void setExplanation(String expl) {
		
		SharedUtils.nullsOkay(expl);
		this.explanation = expl;
	}
}
