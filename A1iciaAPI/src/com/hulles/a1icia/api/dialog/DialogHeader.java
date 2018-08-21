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

import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.SharedUtils;

/**
 * The concept here is that the DialogHeader contains only enough publicly-available 
 * information to determine if the rest of the dialog is intended for the recipient
 * or not. To this end we can deserialize only the header, not the entire package, unless
 * it is meant for us (broadcast or individually addressed).
 *  
 * @author hulles
 *
 */
public class DialogHeader implements Serializable {
	private static final long serialVersionUID = 1127382284278136055L;
	private A1icianID toA1icianID;
	
	/**
	 * Get the ID of the A1ician for whom the dialog is intended
	 * 
	 * @return The A1ician's ID
	 */
	public A1icianID getToA1icianID() {
		
		return toA1icianID;
	}

	/**
	 * Set the ID of the A1ician to whom the dialog is intended
	 * 
	 * @param a1icianID The ID of the A1ician
	 */
	public void setToA1icianID(A1icianID a1icianID) {
		
		SharedUtils.checkNotNull(a1icianID);
		this.toA1icianID = a1icianID;
	}

}
