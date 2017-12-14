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
package com.hulles.a1icia.webx.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.hulles.a1icia.prong.shared.ProngException;
import com.hulles.a1icia.webx.client.content.ConsoleContent;
import com.hulles.a1icia.webx.client.services.MindServiceEvent;
import com.hulles.a1icia.webx.client.services.ServiceHandler;
import com.hulles.a1icia.webx.client.services.ServiceHandler.MindServices;
import com.hulles.a1icia.webx.shared.SerialSystemInfo;
import com.hulles.a1icia.webx.shared.SharedUtils;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class A1iciaWeb implements EntryPoint {
	static String version = null;
	
	static void startUp() {
		MindServiceEvent<SerialSystemInfo> event;
		ConsoleContent content;
		
		AsyncCallback<SerialSystemInfo> callback = new AsyncCallback<SerialSystemInfo>() {
			
			@Override
			public void onSuccess(SerialSystemInfo info) {

				SharedUtils.checkNotNull(info);
				version = info.getVersion();
				MindServiceEvent.setProng(info.getProngValue());
				System.out.println(version);
			}

			@Override
			public void onFailure(Throwable caught) {
				
		        if (caught instanceof ProngException) {
		        	A1iciaClientUtils.prongError(caught);
		        } else {
		        	A1iciaClientUtils.commError(caught);
		        }
			}
		};
		event = new MindServiceEvent<>(MindServices.CHECKSYSTEMS);
		event.setCallback(callback);
		ServiceHandler.handleMindServiceEvent(event);

		content = new ConsoleContent();
		RootPanel.get().add(content.createWidget());
	}
	
	public static String getVersion() {
		
		return version;
	}

	@Override
	public void onModuleLoad() {
		Scheduler scheduler;
		
	    // we defer the rest of the commands so exception handler can handle them
		scheduler = Scheduler.get();
		scheduler.scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
			    startUp();
			}
		});
	}
}
