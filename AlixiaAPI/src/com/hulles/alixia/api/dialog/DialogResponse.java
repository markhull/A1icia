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
package com.hulles.alixia.api.dialog;

import com.hulles.alixia.api.object.AlixiaClientObject;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.media.Language;

public class DialogResponse extends Dialog {
	private static final long serialVersionUID = 2837414184795790854L;
	private final Long responseToRequestID;
	private String response;
	private SerialSememe sememe;
	private String explanation;
	private AlixiaClientObject responseObject;
	private AlixianID fromAlixianID;
	private AlixianID toAlixianID;
	private Language language;
	
	public DialogResponse() {
		super();
		
		responseToRequestID = null;
	}
	public DialogResponse(Long requestID) {
		super();
		
		SharedUtils.checkNotNull(requestID);
		responseToRequestID = requestID;
	}
	
	public Long getResponseToRequestID() {
		
		return responseToRequestID;
	}
	
	public Language getLanguage() {
		
		return language;
	}

	public void setLanguage(Language language) {
		
		SharedUtils.checkNotNull(language);
		this.language = language;
	}

	@Override
	public AlixianID getFromAlixianID() {
		
		return fromAlixianID;
	}

	public void setFromAlixianID(AlixianID alixianID) {
		
		SharedUtils.checkNotNull(alixianID);
		this.fromAlixianID = alixianID;
	}
	
	public SerialSememe getResponseAction() {
		
		return sememe;
	}
	
	@Override
	public AlixianID getToAlixianID() {
	
		return toAlixianID;
	}
	
	public void setToAlixianID(AlixianID alixianID) {
	
		SharedUtils.checkNotNull(alixianID);
		this.toAlixianID = alixianID;
	}
	
	public void setResponseAction(SerialSememe sememe) {
		
		SharedUtils.nullsOkay(sememe);
		this.sememe = sememe;
	}
	
	
	public String getMessage() {
		
		return response;
	}
	
	public void setMessage(String msg) {
		
		SharedUtils.nullsOkay(msg);
		this.response = msg;
	}
	
	public String getExplanation() {
		
		return explanation;
	}

	public void setExplanation(String expl) {
		
		SharedUtils.nullsOkay(expl);
		this.explanation = expl;
	}

	@Override
	public AlixiaClientObject getClientObject() {
		
		return responseObject;
	}
	
	public void setClientObject(AlixiaClientObject obj) {
		
		SharedUtils.nullsOkay(obj);
		this.responseObject = obj;
	}
	
	
	public boolean isValid() {
		AlixiaClientObject clientObject;
		
		if (getLanguage() == null) {
			return false;
		}
		if (getFromAlixianID() == null) {
			return false;
		}
		if (getToAlixianID() == null) {
			return false;
		}
		clientObject = getClientObject();
		if (clientObject != null) {
			if (!clientObject.isValid()) {
				return false;
			}
		}
		if (getResponseAction() == null && 
                getMessage() == null) {
			// nothing to do
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb;
			
		sb = new StringBuilder("DIALOG RESPONSE\n");
		sb.append("Sememe: ");
		if (sememe == null) {
			sb.append("NULL");
		} else {
			sb.append(sememe.getName());
		}
		sb.append("\nLanguage: ");
		if (language == null) {
			sb.append("NULL");
		} else {
			sb.append(language.getDisplayName());
		}
		sb.append("\nResponse: ");
		if (response == null) {
			sb.append("NULL");
		} else {
			sb.append(response);
		}
		sb.append("\nExplanation: ");
		if (explanation == null) {
			sb.append("NULL");
		} else {
			sb.append(explanation);
		}
		sb.append("\nObject: ");
		if (responseObject == null) {
			sb.append("NULL");
		} else {
			sb.append(responseObject.getClientObjectType().toString());
		}
		sb.append("\nFrom Alixian ID: ");
		if (fromAlixianID == null) {
			sb.append("NULL");
		} else {
			sb.append(fromAlixianID);
		}
		sb.append("\nTo Alixian ID: ");
		if (toAlixianID == null) {
			sb.append("NULL");
		} else {
			sb.append(toAlixianID);
		}
		sb.append("\n");
		return sb.toString();
	}
	
}
