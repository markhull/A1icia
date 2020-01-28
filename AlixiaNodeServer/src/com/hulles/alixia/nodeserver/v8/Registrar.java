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
package com.hulles.alixia.nodeserver.v8;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.remote.Prongz;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialProng;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.nodeserver.NodeConsole;
import com.hulles.alixia.nodeserver.pages.NodeWebServer;

public final class Registrar implements JavaCallback {
	private final static Logger LOGGER = LoggerFactory.getLogger(Registrar.class);
	private final ConcurrentMap<String, NodeConsole> consoleMap;
	private final Prongz prongz;
    private final NodeWebServer nodeServer;
    
	public Registrar(NodeWebServer server) {
	
        SharedUtils.checkNotNull(server);
        nodeServer = server;
		consoleMap = new ConcurrentHashMap<>();
		prongz = Prongz.getInstance();
	}
	
	@Override
	public Object invoke(V8Object receiver, V8Array parameters) {
		String prongStr;
		SerialProng prong;
        
		SharedUtils.checkNotNull(parameters);
        prong = prongz.getNewProng();
        LOGGER.debug("Registrar invoke: prong = {}", prong);
        try (NodeConsole miniConsole = new NodeConsole(nodeServer)) {
            prongStr = prong.getProngString();
            consoleMap.put(prongStr, miniConsole);
    		return prongStr;
        }
	}

	public NodeConsole getConsole(SerialProng prong) {
		
		SharedUtils.checkNotNull(prong);
		try {
			prongz.matchProng(prong);
		} catch (AlixiaException e) {
			// this can possibly be an attack...
			LOGGER.error("Registrar: prong not found");
		}
		clean();
        LOGGER.debug("getConsole: prong = {}", prong);
		return consoleMap.get(prong.getProngString());
	}
	
    // Alician IDs s/b unique, so this should work even if same
    //  person is signed on to two devices
	public NodeConsole getConsole(AlixianID alixianID) {
        Collection<NodeConsole> consoles;
        
		SharedUtils.checkNotNull(alixianID);
        LOGGER.debug("getConsole: alixianID = {}", alixianID);
		clean();
        consoles = consoleMap.values();
        for (NodeConsole console : consoles) {
            if (console.getAlixianID().equals(alixianID)) {
                return console;
            }
        }
        LOGGER.error("Registrar: couldn't find console for AlixianID = " + alixianID);
		return null;
	}
	
	public void clean() {
		Set<String> prongStrs;
		String prongStr;
		NodeConsole console;
		
        LOGGER.debug("cleaning consoleMap");
		prongStrs = prongz.getCurrentProngKeys();
		for (Iterator<String> iter = consoleMap.keySet().iterator(); iter.hasNext(); ) {
			prongStr = iter.next();
			if (!prongStrs.contains(prongStr)) {
				LOGGER.debug("cleaning map and closing console for {}", prongStr);
				console = consoleMap.get(prongStr);
				console.close();
				iter.remove();
			}
		}		
	}
	
	
}
