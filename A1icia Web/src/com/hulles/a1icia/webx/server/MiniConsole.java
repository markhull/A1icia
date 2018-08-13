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
package com.hulles.a1icia.webx.server;

import java.io.Closeable;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.collect.ImmutableList;
import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.remote.A1iciaRemote;
import com.hulles.a1icia.api.remote.A1iciaRemoteDisplay;
import com.hulles.a1icia.api.shared.SerialSememe;

final class MiniConsole implements A1iciaRemoteDisplay, Closeable {
	private final A1iciaRemote console;
	private final StringBuffer textSb;
	private final StringBuffer explSb;
	private final Queue<A1iciaClientObject> clientObjects;
	
	MiniConsole() {
		
		console = new A1iciaRemote(this);
		console.startAsync();
		console.awaitRunning();
		
		console.setUseTTS(false);
		console.setPlayAudio(false);
		console.setShowImage(false);
		console.setShowText(false);
		console.setPlayVideo(false);
		textSb = new StringBuffer();
		explSb = new StringBuffer();
		clientObjects = new ConcurrentLinkedQueue<>();
	}
	
	void sendText(String text) {
		
		if (!console.sendText(text)) {
			receiveText("Unable to communicate with server at this time");
		}
	}
	
	@Override
	public void receiveText(String text) {

		textSb.append(text);
		textSb.append("\n");
	}

	String getMessages() {
		String text;
		
		text = textSb.toString();
		textSb.setLength(0);
		return text;
	}

	String getExplanations() {
		String text;
		
		text = explSb.toString();
		explSb.setLength(0);
		return text;
	}

	List<A1iciaClientObject> getClientObjects() {
		ImmutableList<A1iciaClientObject> listCopy;
		
		listCopy = ImmutableList.copyOf(clientObjects);
		clientObjects.clear();
		return listCopy;
	}
	
	@Override
	public boolean receiveCommand(SerialSememe sememe) {
		
		return false;
	}

	@Override
	public void receiveExplanation(String text) {

		explSb.append(text);
		explSb.append("\n");
	}

	@Override
	public boolean receiveObject(A1iciaClientObject object) {
		
		clientObjects.add(object);
		return true;
	}

	@Override
	public void close() {
		
		console.stopAsync();
		console.awaitTerminated();
	}

	@Override
	public void receiveRequest(String text) {

		receiveText(text);
	}
}
