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
package com.hulles.alixia.node;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.remote.Station;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.prong.shared.SerialProng;

public final class PlainResponder implements JavaCallback {
//	private final static Logger logger = Logger.getLogger("AlixiaWebServer.PlainResponder");
//	private final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
	final AlixianID alixianID;
	private final Station station;
	
	public PlainResponder() {
		
		station = Station.getInstance();
		station.ensureStationExists();
		alixianID = AlixianID.createAlixianID();
	}

	@SuppressWarnings("resource")
	@Override
	public Object invoke(V8Object receiver, V8Array parameters) {
		String prongStr;
		String query;
		String response;
		MiniConsole miniConsole;
		SerialProng prong;
		
		SharedUtils.checkNotNull(parameters);
		prongStr = parameters.getString(0);
		if (parameters.length() < 2) {
			AlixiaUtils.error("PlainResponder: paramenters error");
			return "Parameters error";
		}
		prong = new SerialProng(prongStr);
		miniConsole = Registrar.getInstance().getConsole(prong);
		query = parameters.getString(1);
		if (query.equals("What a nice body!")) {
			return "Hello from Alixia! And you're right, it is a nice body. Pig.";
		}
		// TODO sanitize the query
		miniConsole.sendText(query);
		response = miniConsole.getMessages();
        return response;
	}

}
