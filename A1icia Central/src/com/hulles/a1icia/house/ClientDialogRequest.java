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
package com.hulles.a1icia.house;

import com.hulles.a1icia.api.dialog.DialogRequest;
import com.hulles.a1icia.room.document.RoomObject;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * This is just a mostly-wrapper for DialogRequest that implements RoomObject.
 * 
 * @author hulles
 *
 */
public class ClientDialogRequest implements RoomObject {
	private final DialogRequest dialogRequest;
	
	public ClientDialogRequest(DialogRequest request) {
		
		A1iciaUtils.checkNotNull(request);
		dialogRequest = request;
	}

	public DialogRequest getDialogRequest() {
	
		return dialogRequest;
	}
	
	@Override
	public RoomObjectType getRoomObjectType() {

		return RoomObjectType.CLIENTREQUEST;
	}

	public boolean isValid() {
		
		if (dialogRequest == null) {
			return false;
		}
		return dialogRequest.isValid();
	}
}

