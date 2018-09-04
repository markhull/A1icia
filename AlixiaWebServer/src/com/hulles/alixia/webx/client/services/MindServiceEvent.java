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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.prong.shared.SerialProng;
import com.hulles.alixia.webx.client.services.ServiceHandler.MindServices;
import com.hulles.alixia.webx.shared.SharedUtils;

final public class MindServiceEvent<T> {
	private final MindServices function;
	private AsyncCallback<T> callback;
	private static SerialProng prong;
	private final List<Object> params;

	public MindServiceEvent(MindServices function) {
		
		SharedUtils.checkNotNull(function);
		this.function = function;
		this.params = new ArrayList<>();
	}

	public static SerialProng getProng() {
		
		if (prong == null) {
			AlixiaUtils.error("No prong value in MindServiceEvent");
			return null;
		}
		return prong;
	}
	
	public static void setProng(SerialProng newProng) {
		
		SharedUtils.checkNotNull(newProng);
		prong = newProng;
	}
	
	public void setCallback(AsyncCallback<T> callback) {
		
		SharedUtils.checkNotNull(callback);
		this.callback = callback;
	}
	
	public AsyncCallback<T> getCallback() {
		
		return callback;
	}

	public List<Object> getParams() {
		
		return params;
	}

	public void setParams(List<Object> pList) {
		
		SharedUtils.checkNotNull(pList);
		params.clear();
		params.addAll(pList);
	}
	
	public void addParam(Object object) {
		
		// a param can have a null value, see getNationReport for an example....
		SharedUtils.nullsOkay(object);
		params.add(object);
	}
	
	public Object getParam(int ix) {
		Object param;
		
		try {
			param = params.get(ix);
		} catch (IndexOutOfBoundsException ex) {
			AlixiaUtils.error("Not enough service params", ex);
			return null;
		}		
		return param;
	}
	
	public MindServices getFunction() {
		
		return function;
	}

}
