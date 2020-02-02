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

import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;

/**
 * This represents an action for a receiving Alixian, for example turning on a red LED. This isn't at
 * the Station level, because we can have multiple "red LEDs" in software, for different users on
 * the same web server e.g. But it could also be implemented in hardware, as for the Alixia Pi Mirror.
 * 
 * @author hulles
 *
 */
public class AlixianAction extends RoomActionObject {
	private String message;
	private String explanation;
	private SerialSememe clientAction;
	private AlixianID toAlixianID;
	
	public AlixianID getToAlixianID() {
		
		return toAlixianID;
	}

	public void setToAlixianID(AlixianID toAlixianID) {
		
		SharedUtils.nullsOkay(toAlixianID);
		this.toAlixianID = toAlixianID;
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
