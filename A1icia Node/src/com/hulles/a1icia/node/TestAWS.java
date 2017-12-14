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
package com.hulles.a1icia.node;

import java.io.IOException;

import com.hulles.a1icia.api.shared.A1iciaAPIException;

public class TestAWS {
	static A1iciaWebServer aws;
	
	@SuppressWarnings("unused")
	private static void waitForKey() {
		
		System.out.println("Hit a key ");
		try {
			System.in.read();
		} catch (IOException e) {
			throw new A1iciaAPIException("TestAWS: IO error reading key", e);
		}
	}
	
	private static void runServer() {
		Thread serverThread;
		
		aws = new A1iciaWebServer();
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				aws.serve();
			}
		};
		serverThread = new Thread(runner);
		serverThread.start();
	}
	
	public static void main(String[] args) {
//		String result;
		
		runServer();
//		waitForKey();
//		result = ExternalAperture.postTestQueryToA1iciaNode("who are you");
//		System.out.println("WEB RESULT (who are you) ==> " + result);
//		waitForKey();
//		result = ExternalAperture.postTestQueryToA1iciaNode("what time is it");
//		System.out.println("WEB RESULT (what time is it) ==> " + result);
//		waitForKey();
//		result = ExternalAperture.postTestQueryToA1iciaNode("dead sara");
//		System.out.println("WEB RESULT (dead sara) ==> " + result);
//		waitForKey();
//		System.out.println("Server shut down");
	}
}
