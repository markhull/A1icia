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
package com.hulles.alixia.api.remote;

import com.hulles.alixia.api.object.AlixiaClientObject;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.media.text.TextDisplayer;

/**
 * AlixiaRemoteDisplay handles output from the exchange between a remote station and
 * Alixia Central.
 * 
 * @author hulles
 *
 */
public interface AlixiaRemoteDisplay extends TextDisplayer {

	/**
	 * Add text to the remote display.
	 * 
	 * @param text The text to display
	 */
	void receiveText(String text);

	/**
	 * Add text as a request for action to the remote display.
	 * 
	 * @param text The text request
	 */
	void receiveRequest(String text);

	/**
	 * Add an explanation to the remote display, if it's non-null and not the
	 * same as the message text.
	 * 
	 * @param text The text explanation
	 */
	void receiveExplanation(String text);
	
	/**
	 * Receive a command from Alixia Central. This should return true
	 * if the remote display handles the command and doesn't need more processing,
	 * false otherwise.
	 * 
	 * @param sememe The command sememe
     * @return True if the implementer consumes the sememe 
	 */
	boolean receiveCommand(SerialSememe sememe);
	
	/**
	 * Receive a media object from Alixia Central. This should return true
	 * if the remote display handles the object and doesn't need more processing,
	 * false otherwise.
	 * 
	 * @param object The media object
     * @return True if the implementer can process the media object
	 */
	boolean receiveObject(AlixiaClientObject object);
	
	/**
	 * This allows us to receive a notification of a text window closing, so
	 * we can act appropriately.
	 * 
	 */
    @Override
	void textWindowIsClosing();

}
