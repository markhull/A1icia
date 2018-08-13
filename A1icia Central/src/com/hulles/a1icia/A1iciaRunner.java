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

package com.hulles.a1icia;

import java.io.IOException;

import com.hulles.a1icia.base.A1iciaException;

/**
 * A1iciaRunner is a simple class with a main method to run A1icia Central.
 * 
 * @author hulles
 */
public class A1iciaRunner {   
	
	private static void waitForKey() {
		
		System.out.println("Hit a key ");
		try {
			System.in.read();
		} catch (IOException e) {
			throw new A1iciaException("A1iciaRunner: IO error reading key", e);
		}
	}

	public static void main(String[] args) {
		boolean noprompt = false;
//		DialogRequest input;
		
		if (args.length > 0) {
			if (args[0].equals("--noprompt")) {
				noprompt = true;
			}
		}
//		input = new DialogRequest();
//		input.setRequestMessage("Hi!");
		try (A1icia a1icia = new A1icia(noprompt)) {
			waitForKey();
		}
	}
    
}
