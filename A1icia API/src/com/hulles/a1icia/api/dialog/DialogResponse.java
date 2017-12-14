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
package com.hulles.a1icia.api.dialog;

import java.io.Serializable;

import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.SerialSpark;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.media.Language;

public class DialogResponse implements Serializable, Dialog {
	private static final long serialVersionUID = 2837414184795790854L;
	private String response;
	private SerialSpark spark;
	private String explanation;
	private A1iciaClientObject responseObject;
	private A1icianID fromA1icianID;
	private A1icianID toA1icianID;
	private Language language;
	
	public DialogResponse() {
		
	}
	
	public Language getLanguage() {
		
		return language;
	}

	public void setLanguage(Language language) {
		
		SharedUtils.checkNotNull(language);
		this.language = language;
	}

	@Override
	public A1icianID getFromA1icianID() {
		
		return fromA1icianID;
	}

	public void setFromA1icianID(A1icianID a1icianID) {
		
		SharedUtils.checkNotNull(a1icianID);
		this.fromA1icianID = a1icianID;
	}
	
	public SerialSpark getResponseAction() {
		
		return spark;
	}
	
	@Override
	public A1icianID getToA1icianID() {
	
		return toA1icianID;
	}
	
	public void setToA1icianID(A1icianID a1icianID) {
	
		SharedUtils.checkNotNull(a1icianID);
		this.toA1icianID = a1icianID;
	}
	
	public void setResponseAction(SerialSpark spark) {
		
		SharedUtils.nullsOkay(spark);
		this.spark = spark;
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
	public A1iciaClientObject getClientObject() {
		
		return responseObject;
	}
	
	public void setClientObject(A1iciaClientObject obj) {
		
		SharedUtils.nullsOkay(obj);
		this.responseObject = obj;
	}
	
	
	public boolean isValid() {
		A1iciaClientObject clientObject;
		
		if (getLanguage() == null) {
			return false;
		}
		if (getFromA1icianID() == null) {
			return false;
		}
		if (getToA1icianID() == null) {
			return false;
		}
		clientObject = getClientObject();
		if (clientObject != null) {
			if (!clientObject.isValid()) {
				return false;
			}
		}
		if (getMessage() == null && 
				clientObject == null &&
				getResponseAction() == null) {
			// nothing to do
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb;
			
		sb = new StringBuilder("DIALOG RESPONSE\n");
		sb.append("Spark: ");
		if (spark == null) {
			sb.append("NULL");
		} else {
			sb.append(spark.getName());
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
		sb.append("\nFrom A1ician ID: ");
		if (fromA1icianID == null) {
			sb.append("NULL");
		} else {
			sb.append(fromA1icianID);
		}
		sb.append("\nTo A1ician ID: ");
		if (toA1icianID == null) {
			sb.append("NULL");
		} else {
			sb.append(toA1icianID);
		}
		sb.append("\n");
		return sb.toString();
	}
	
}
