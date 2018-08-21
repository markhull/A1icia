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
package com.hulles.a1icia.node;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.remote.Station;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.node.ng.TinyPages;
import com.hulles.a1icia.prong.shared.SerialProng;

public final class HtmlResponder implements JavaCallback {
//	private final static Logger logger = Logger.getLogger("A1iciaWebServer.HtmlResponder");
//	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	final A1icianID a1icianID;
	private final Station station;
	
	public HtmlResponder() {
		
		station = Station.getInstance();
		station.ensureStationExists();
		a1icianID = A1icianID.createA1icianID();
	}

	@SuppressWarnings("resource")
	@Override
	public Object invoke(V8Object receiver, V8Array parameters) {
		String prongStr;
		String query;
		@SuppressWarnings("unused")
		String response;
		MiniConsole miniConsole;
		SerialProng prong;
		
		SharedUtils.checkNotNull(parameters);
		prongStr = parameters.getString(0);
		if (parameters.length() < 2) {
			System.err.println("HtmlResponder: paramenters error");
			return "Parameters error";
		}
		prong = new SerialProng(prongStr);
		miniConsole = Registrar.getInstance().getConsole(prong);
		query = parameters.getString(1);
		// TODO sanitize the query
		miniConsole.sendText(query);
		response = miniConsole.getMessages();
        return TinyPages.getWebPage();
	}
	
}
