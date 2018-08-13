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
package com.hulles.a1icia.media;

/**
 * The A1icia API version of the RuntimeException, for possible expanded use later.
 * 
 * @author hulles
 *
 */
public final class A1iciaMediaException extends RuntimeException {
	private static final long serialVersionUID = -4744550566143714488L;
	private static final String BEES = "500 The Bees They're In My Eyes";
	
	public A1iciaMediaException() {
		super(BEES);
		
		System.err.println(BEES);
	}
	public A1iciaMediaException(String desc) {
		super(desc);
		
		System.err.println(desc);
	}
    public A1iciaMediaException(String desc, Throwable ex) {
        super(desc, ex);
        
        System.err.println(desc);
        ex.printStackTrace();
    }
}
