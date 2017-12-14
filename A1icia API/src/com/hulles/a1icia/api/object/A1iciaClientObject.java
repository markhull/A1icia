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
package com.hulles.a1icia.api.object;

import java.io.Serializable;

public interface A1iciaClientObject extends Serializable {

	public ClientObjectType getClientObjectType();
	
	public boolean isValid();
	
	public enum ClientObjectType {
		LOGIN,
		LOGIN_RESPONSE,
		AUDIOBYTES,
		VIDEOBYTES,
		IMAGEBYTES,
		CHANGE_LANGUAGE
	}
}
