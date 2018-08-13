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
package com.hulles.a1icia.prong.shared;

import java.io.Serializable;

public class SerialProng implements Serializable {
	private static final long serialVersionUID = 8026211050845784261L;
	private String prongString;
	
	public SerialProng() {
		
		// we need a no-arg constructor for serialization
		prongString = null;
	}
	public SerialProng(String prongString) {
		
		SharedUtils.checkNotNull(prongString);
		this.prongString = prongString;
	}
	
	public String getProngString() {
		
		return prongString;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((prongString == null) ? 0 : prongString.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SerialProng)) {
			return false;
		}
		SerialProng other = (SerialProng) obj;
		if (prongString == null) {
			if (other.prongString != null) {
				return false;
			}
		} else if (!prongString.equals(other.prongString)) {
			return false;
		}
		return true;
	}
}
