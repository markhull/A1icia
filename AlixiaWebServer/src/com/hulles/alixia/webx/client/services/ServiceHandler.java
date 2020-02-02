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
package com.hulles.alixia.webx.client.services;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hulles.alixia.webx.shared.SerialConsoleIn;
import com.hulles.alixia.webx.shared.SerialConsoleOut;
import com.hulles.alixia.webx.shared.SerialSystemInfo;
import com.hulles.alixia.webx.shared.SharedUtils;

public class ServiceHandler  {
	private static MindServiceAsync mindClass = null;

	/*******************************************************************************/
	/***** PROBLEMS? MAKE SURE event.getParam(n) starts with event.getParam(0) *****/
	/*******************************************************************************/
	
	@SuppressWarnings("unchecked")
	public static void handleMindServiceEvent(MindServiceEvent<?> event) {
		MindServices service;
		
		SharedUtils.checkNotNull(event);
		if (mindClass == null) {
			mindClass = (MindServiceAsync) GWT.create(MindService.class);
		}
		service = event.getFunction();
		try {
			switch (service) {
				case SENDCONSOLE:
					mindClass.sendConsole(MindServiceEvent.getProng(), 
							(SerialConsoleIn)event.getParam(0),
							(AsyncCallback<Void>) event.getCallback());
					break;
				case RECEIVECONSOLE:
					mindClass.receiveConsole(MindServiceEvent.getProng(), 
							(AsyncCallback<SerialConsoleOut>) event.getCallback());
					break;
				case QUERYHEALTH:
					mindClass.queryHealth(MindServiceEvent.getProng(),
							(AsyncCallback<String>) event.getCallback());
					break;
				case CHECKPRONG:
					mindClass.pingProng(MindServiceEvent.getProng(), 
							(AsyncCallback<Void>) event.getCallback());
					break;
				case CLEARPRONG:
					mindClass.clearProng(MindServiceEvent.getProng(), 
							(AsyncCallback<Void>) event.getCallback());
					break;
				case CHECKSYSTEMS:
					mindClass.checkSystems((AsyncCallback<SerialSystemInfo>) event.getCallback());
					break;
				default:
					SharedUtils.error("Unhandled service event");
			}
		} catch (ClassCastException ex) {
			SharedUtils.error("Class cast exception");
		}
	}

	public enum MindServices {
		CHECKPRONG,
		CHECKSYSTEMS,
		CLEARPRONG,
		QUERYHEALTH,
		SENDCONSOLE,
		RECEIVECONSOLE
	}
}
