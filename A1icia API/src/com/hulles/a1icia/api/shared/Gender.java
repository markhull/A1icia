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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hulles.a1icia.api.shared;


/**
 *
 * @author hulles
 */
public enum Gender {
    FEMALE('F', "Female"),
    MALE('M', "Male");
    private final Character storeName;
    private final String displayName;

    private Gender(Character storeName, String displayName) {
    	
        this.storeName = storeName;
        this.displayName = displayName;
    }

    public Character getStoreName() {
    	
        return storeName;
    }

    public String getDisplayName() {
    	
        return displayName;
    }

    public static Gender findGender(Character code) {
    	
		SharedUtils.checkNotNull(code);
        if (MALE.storeName.equals(code)) {
            return MALE;
        } else if (FEMALE.storeName.equals(code)) {
            return FEMALE;
        } else {
        	throw new IllegalArgumentException("Bad gender code = " + code.toString());
        }
    }
}
