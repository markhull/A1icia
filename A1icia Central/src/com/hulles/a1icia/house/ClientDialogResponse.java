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
package com.hulles.a1icia.house;

import com.hulles.a1icia.room.document.RoomActionObject;
import com.hulles.a1icia.api.dialog.DialogResponse;

/**
 * This class wraps DialogResponse so it can be a RoomActionObject.
 * 
 * @author hulles
 *
 */
public class ClientDialogResponse extends RoomActionObject {
	private final DialogResponse dialogResponse;
	
	public ClientDialogResponse() {
	
		dialogResponse = new DialogResponse();
	}

	public DialogResponse getDialogResponse() {
		
		return dialogResponse;
	}
	
	@Override
	public String getMessage() {

		return dialogResponse.getMessage();
	}
	
	@Override
	public String getExplanation() {

		return dialogResponse.getExplanation();
	}
	
	public boolean isValidDialogResponse() {
		
		if (dialogResponse == null) {
			return false;
		}
		return dialogResponse.isValid();
	}
}
