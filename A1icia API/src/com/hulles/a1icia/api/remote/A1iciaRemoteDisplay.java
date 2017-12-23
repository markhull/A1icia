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
package com.hulles.a1icia.api.remote;

import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.shared.SerialSpark;

/**
 * A1iciaRemoteDisplay handles output from the exchange between a remote station and
 * A1icia Central.
 * 
 * @author hulles
 *
 */
public interface A1iciaRemoteDisplay {

	/**
	 * Add text to the remote display.
	 * 
	 * @param text
	 */
	void receiveText(String text);

	/**
	 * Add text as a request for action to the remote display.
	 * 
	 * @param text
	 */
	void receiveRequest(String text);

	/**
	 * Add an explanation to the remote display, if it's non-null and not the
	 * same as the message text.
	 * 
	 * @param text
	 */
	void receiveExplanation(String text);
	
	/**
	 * Receive a command from A1icia Central. This should return true
	 * if the remote display handles the command and doesn't need more processing,
	 * false otherwise.
	 * 
	 * @param spark
	 */
	boolean receiveCommand(SerialSpark spark);
	
	/**
	 * Receive a media object from A1icia Central. This should return true
	 * if the remote display handles the object and doesn't need more processing,
	 * false otherwise.
	 * 
	 * @param spark
	 */
	boolean receiveObject(A1iciaClientObject object);
}
