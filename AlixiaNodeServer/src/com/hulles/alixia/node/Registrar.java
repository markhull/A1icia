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

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.prong.server.Prongz;
import com.hulles.alixia.prong.shared.ProngException;
import com.hulles.alixia.prong.shared.SerialProng;

public final class Registrar implements JavaCallback {
	private final ConcurrentMap<String, MiniConsole> consoleMap;
	private static Registrar instance;
	private final Prongz prongz;
	public Registrar() {
		
		consoleMap = new ConcurrentHashMap<>();
		instance = this;
		prongz = Prongz.getInstance();
	}
	
	public static Registrar getInstance() {
	
		return instance;
	}
	
	@SuppressWarnings("resource")
	@Override
	public Object invoke(V8Object receiver, V8Array parameters) {
		String prongStr;
		MiniConsole miniConsole;
		SerialProng prong;
		
		SharedUtils.checkNotNull(parameters);
		prong = prongz.getNewProng();
		miniConsole = new MiniConsole();
		prongStr = prong.getProngString();
		consoleMap.put(prongStr, miniConsole);
		return prongStr;
	}

	public MiniConsole getConsole(SerialProng prong) {
		
		SharedUtils.checkNotNull(prong);
		try {
			prongz.matchProng(prong);
		} catch (ProngException e) {
			// this can possibly be an attack...
			AlixiaUtils.error("Registrar: prong not found");
		}
		clean();
		return consoleMap.get(prong.getProngString());
	}
	
	public void clean() {
		Set<String> prongStrs;
		String prongStr;
		MiniConsole console;
		
		prongStrs = prongz.getCurrentProngKeys();
		for (Iterator<String> iter = consoleMap.keySet().iterator(); iter.hasNext(); ) {
			prongStr = iter.next();
			if (!prongStrs.contains(prongStr)) {
				console = consoleMap.get(prongStr);
				console.close();
				iter.remove();
			}
		}		
	}
	
	
}
