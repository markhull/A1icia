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
package com.hulles.a1icia.ticket;

import com.hulles.a1icia.room.document.RoomActionObject;

public class ProxyAction extends RoomActionObject {

	@Override
	public String getMessage() {

		return "Dammit, Jim, he's dead!";
	}

	@Override
	public String getExplanation() {

		return "Something went wrong, so you are getting this response instead of the one " + 
				"you hoped for.";
	}

}
