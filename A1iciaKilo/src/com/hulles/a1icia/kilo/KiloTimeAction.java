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
package com.hulles.a1icia.kilo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.room.document.RoomActionObject;

public class KiloTimeAction extends RoomActionObject {
	private LocalDateTime ldt;
	private DateTimeFormatter formatter;
	private String location;
	
	@Override
	public String getMessage() {
		String dateStr;
		StringBuilder sb;
		
		formatter = DateTimeFormatter.ofPattern("MMMM dd yyyy, hh:mm:ss a");
		dateStr = ldt.format(formatter);
		if (location == null) {
			return dateStr;
		}
		sb = new StringBuilder();
		sb.append("The current date and time in ");
		sb.append(location);
		sb.append(" is ");
		sb.append(dateStr);
		return sb.toString();
	}

	public LocalDateTime getLocalDateTime() {
		
		return ldt;
	}

	void setLocalDateTime(LocalDateTime ldt) {
		
		SharedUtils.checkNotNull(ldt);
		this.ldt = ldt;
	}

	@Override
	public String getExplanation() {

		return getMessage();
	}

	public String getLocation() {
		
		return location;
	}

	public void setLocation(String location) {
		
		SharedUtils.nullsOkay(location);
		this.location = location;
	}
	
}
