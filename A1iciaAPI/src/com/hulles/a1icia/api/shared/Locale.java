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
package com.hulles.a1icia.api.shared;


/**
 *
 * @author hulles
 */
public enum Locale {
    ENGLISH("en"),
    SPANISH("es");
    private final String storeName;

    private Locale(String storeName) {
    	
        this.storeName = storeName;
     }

    public String getStoreName() {
    	
        return storeName;
    }
    
    public static Locale findLocale(String name) {
    	
		SharedUtils.checkNotNull(name);
        if (ENGLISH.storeName.equals(name)) {
            return ENGLISH;
        } else if (SPANISH.storeName.equals(name)) {
            return SPANISH;
        } else {
        	throw new IllegalArgumentException("Bad locale = " + name);
        }
    }
}
