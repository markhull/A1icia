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
package com.hulles.alixia.webx.shared;

import java.io.Serializable;

public class SerialConsoleIn implements Serializable {
	private static final long serialVersionUID = 1421962129704897695L;
	private java.util.Date datestamp;
	private String dateString;
	private String text;
	
	public SerialConsoleIn() {
		// need no-arg constructor
	}

	public java.util.Date getDatestamp() {
		
		return datestamp;
	}

	public void setDatestamp(java.util.Date datestamp) {
		
		SharedUtils.checkNotNull(datestamp);
		this.datestamp = datestamp;
	}

	public String getDateString() {
		
		return dateString;
	}

	public void setDateString(String dateString) {
		
		SharedUtils.checkNotNull(dateString);
		this.dateString = dateString;
	}

	public String getText() {
		
		return text;
	}

	public void setText(String text) {
		
		SharedUtils.checkNotNull(text);
		this.text = text;
	}

}
