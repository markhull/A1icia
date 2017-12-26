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
package com.hulles.a1icia.base;

/**
 * The A1icia version of the RuntimeException, for possible expanded use later.
 * 
 * @author hulles
 *
 */
public final class A1iciaException extends RuntimeException {
	private static final long serialVersionUID = 1375561019112558941L;
	
	public A1iciaException() {
		super("500 The Bees They're In My Eyes");
	}
	public A1iciaException(String desc) {
		super(desc);
	}
    public A1iciaException(String desc, Throwable ex) {
        super(desc, ex);
        
        ex.printStackTrace();
    }
}
