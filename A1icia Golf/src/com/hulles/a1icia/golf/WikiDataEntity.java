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

import java.util.ArrayList;
import java.util.List;

import com.hulles.a1icia.tools.A1iciaUtils;

final public class WikiDataEntity {
	private String qID;
	private String label;
	private String description;
	private final List<String> aliases;
	private final List<WikiDataClaim> claims;
	
	public WikiDataEntity() {
		
		aliases = new ArrayList<>();
		claims = new ArrayList<>();
	}

	public String getqID() {
		
		return qID;
	}

	public void setqID(String qID) {
		
		A1iciaUtils.checkNotNull(qID);
		this.qID = qID;
	}

	public String getLabel() {
		
		return label;
	}

	public void setLabel(String label) {
		
		A1iciaUtils.checkNotNull(label);
		this.label = label;
	}

	public String getDescription() {
		
		return description;
	}

	public void setDescription(String description) {
		
		A1iciaUtils.checkNotNull(description);
		this.description = description;
	}

	public List<String> getAliases() {
		
		return aliases;
	}

	public void setAliases(List<String> newAliases) {
	
		A1iciaUtils.checkNotNull(newAliases);
		aliases.clear();
		aliases.addAll(newAliases);
	}
	
	public void addAlias(String alias) {
		
		A1iciaUtils.checkNotNull(alias);
		aliases.add(alias);
	}
	
	public List<WikiDataClaim> getClaims() {
		
		return claims;
	}
	
	public void setClaims(List<WikiDataClaim> newClaims) {
		
		A1iciaUtils.checkNotNull(newClaims);
		claims.clear();
		claims.addAll(newClaims);
	}
	
	public void addClaim(WikiDataClaim claim) {
		
		A1iciaUtils.checkNotNull(claim);
		claims.add(claim);
	}
	
	public static String getCommonsMediaImageURL(String fnm) {
		String fileName;
		
		fileName = fnm.replace(' ', '_');
		return "https://commons.wikimedia.org/wiki/File:" + fileName;
	}

}
