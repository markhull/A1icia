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
package com.hulles.alixia.graphviz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hulles.alixia.api.shared.SharedUtils;

public abstract class GraphObject {
    private final String id;
    private final List<Attribute> attrList;

    public GraphObject(String id) {
    	
    	SharedUtils.checkNotNull(id);
        this.id = id;
        attrList = Collections.synchronizedList(new ArrayList<>());
    }

    public void addAttribute(Attribute attr){
    	
    	SharedUtils.checkNotNull(attr);
        this.attrList.add(attr);
    }

/*    public void removeAttribute(String attrName){
    	Attribute attr;
    	
    	SharedUtils.checkNotNull(attrName);
    	for (Iterator<Attribute> iter = attrList.iterator(); iter.hasNext(); ) {
    		attr = iter.next();
    		if (attr.getName().equals(attrName)) {
    			iter.remove();
    		}
    	}
    }
*/
    public String getId(){
    	
        return this.id;
    }

    protected String genAttributeDotString(){
        StringBuilder sb;

        sb = new StringBuilder();
        for(Attribute attr : this.attrList){
            sb.append(attr.getName());
            sb.append("=");
            sb.append(attr.getValue());
            sb.append(";\n");
        }
        return sb.toString();
    }

    abstract public GraphObjectType getGraphObjectType();
    
    abstract public String genDotString();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (!(obj instanceof GraphObject)) {
			return false;
		}
		GraphObject other = (GraphObject) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String objString;
		
		objString = getGraphObjectType().name() + ": " + id;
		return objString;
	}

}
