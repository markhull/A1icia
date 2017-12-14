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
package com.hulles.a1icia.golf;

import com.hulles.a1icia.tools.A1iciaUtils;

public class WikiDataClaim {
	private final String propID;
	private Object value;
	private String label;
	private String secondaryLabel = null; // this label is looked up from e.g. wikibase-item
	private String dataType;
	private String type;
	
	WikiDataClaim(String propID) {
		
		A1iciaUtils.checkNotNull(propID);
		this.propID = propID;
	}

	public String getPropID() {
		
		return propID;
	}

	public String getDataType() {
		
		return dataType;
	}

	public void setDataType(String dataType) {
		
		A1iciaUtils.checkNotNull(dataType);
		this.dataType = dataType;
	}

	public String getType() {
		
		return type;
	}

	public void setType(String type) {
		
		this.type = type;
	}

	public Object getValue() {
		
		return value;
	}

	public void setValue(Object value) {
		
		A1iciaUtils.checkNotNull(value);
		this.value = value;
	}

	public String getLabel() {
		
		return label;
	}

	public void setLabel(String label) {
		
		this.label = label;
	}

	public String getSecondaryLabel() {
		
		return secondaryLabel;
	}

	public void setSecondaryLabel(String secondaryLabel) {
		
		A1iciaUtils.checkNotNull(secondaryLabel);
		this.secondaryLabel = secondaryLabel;
	}

}
