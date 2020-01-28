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
package com.hulles.alixia.nodeserver.pages;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.dialog.DialogRequest;
import com.hulles.alixia.api.dialog.DialogResponse;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.house.House;
import com.hulles.alixia.house.UrHouse;
import com.hulles.alixia.nodeserver.NodeConsole;
import com.hulles.alixia.nodeserver.NodeServlet;
import com.hulles.alixia.nodeserver.v8.Registrar;

/**
 * This is an implementation of UrHouse that runs the Node.js / Express web server.
 * It differs from MiniConsole in that it does not extend AlixiaRemoteDisplay and
 * interacts directly with the NodeWebServer house, bypassing Jebus pub / sub.
 * 
 * @see AlixiaExpressServer.js
 * @see com.hulles.alixia.stationserver.StationServer
 * 
 * @author hulles
 */
public final class NodeWebServer extends UrHouse {
	private final static Logger LOGGER = LoggerFactory.getLogger(NodeWebServer.class);
	private ExecutorService executor;
	final NodeServlet servlet;
    private final Registrar registrar;
    
    public NodeWebServer() {
        super();
        
		servlet = new NodeServlet(this);
        registrar = servlet.getRegistrar();
    }
    public NodeWebServer(Boolean noPrompt) {
        this();
        
        SharedUtils.checkNotNull(noPrompt);
        super.setNoPrompts(noPrompt);
   }
    
	/**
	 * Return which house we are.
	 * 
	 *
     * @return Our house
	 */
    @Override
    public House getThisHouse() {
        
        return House.NODESERVER;
    }

	/**
	 * We don't handle incoming dialog requests, so if one is addressed to us it's an error.
	 * 
	 * @see UrHouse
     * 
     * @param request The incoming DialogRequest
	 * 
	 */
    @Override
    protected void newDialogRequest(DialogRequest request) {
		throw new AlixiaException("Request not implemented in " + getThisHouse());
    }

	/**
	 * We got a response from Alixia (presumably) so we send it along to its ultimate
	 * destination Alixian.
	 * 
     * @param response The incoming DialogResponse to be routed
	 */
    @Override
    protected void newDialogResponse(DialogResponse response) {
		AlixianID alixianID;
        
		SharedUtils.checkNotNull(response);
        LOGGER.debug("NodeWebServer: got response from Alixia");
        alixianID = response.getToAlixianID();
        try (NodeConsole console = registrar.getConsole(alixianID)) {
            if (console == null) {
                LOGGER.error("NodeWebServer: can't find console for Alixian ID = " + alixianID);
                return;
            }
            console.receiveText(response.getMessage());
            console.receiveExplanation(response.getExplanation());
            console.receiveObject(response.getClientObject());
        }
    }
		
    
	/**
	 * Receive a DialogRequest from an Alixian which we then 
	 * post onto the street bus.
	 * 
	 * @param request The request
	 */
	public void receiveRequestFromConsole(DialogRequest request) {
        
        SharedUtils.checkNotNull(request);
        LOGGER.debug("NodeWebServer: got request from station");
        super.receiveRequestFromClient(request);
	}

    @Override
    protected void houseStartup() {
        
		executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                servlet.serve();
            }
        });
    }

    @Override
    protected void houseShutdown() {
        
        servlet.stopServing();
		if (executor != null) {
			try {
			    LOGGER.info("NodeWebServer: attempting to shutdown executor");
			    executor.shutdown();
			    executor.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
			    LOGGER.error("NodeWebServer: executor shutdown interrupted");
			} finally {
			    if (!executor.isTerminated()) {
			        LOGGER.error("NodeWebServer: cancelling non-finished tasks");
			    }
			    executor.shutdownNow();
			    LOGGER.info("NodeWebServer: shutdown finished");
			}
		}
		executor = null;
    }
	
}
