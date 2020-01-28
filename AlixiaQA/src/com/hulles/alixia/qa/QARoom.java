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
package com.hulles.alixia.qa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.cayenne.Sememe;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.MessageAction;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.alixia.ticket.Ticket;

/**
 * QA Room is where we do testing and analysis.
 * 
 * @author hulles
 *
 */
public final class QARoom extends UrRoom {
	final static Logger LOGGER = LoggerFactory.getLogger(QARoom.class);
    List<SerialSememe> allSememes;
    int sememeIx;
    ScheduledExecutorService scheduler;
    ScheduledFuture<?> schedulerHandle;
    final Requester qaRequester;
    
	public QARoom() {
		super();
        
		qaRequester = new Requester();
	}

	/**
	 * "pilates" is the sememe that triggers our exercising of Alixia.
     * 
     * @param sememePkg The sememe package
     * @param request The room request
     * @return The ActionPackage we've created
	 */
	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "pilates":
				return createPilatesActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
		}
	}

	/**
	 * This starts the "pilates" action, which exercises all of the sememes.
	 * 
	 * @param sememePkg
	 * @param request
	 * @return
	 */
	private ActionPackage createPilatesActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		MessageAction action;
		Thread thread;
        
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		thread = new Thread(new RunSememes());
		thread.run();
		pkg = new ActionPackage(sememePkg);
		action = new MessageAction();
		action.setMessage("Starting pilates");
		pkg.setActionObject(action);
		return pkg;
	}
	
	/**
	 * Return this room name.
	 * 
     * @return The Room enum for this room.
	 */
	@Override
	public Room getThisRoom() {

		return Room.QA;
	}


	/**
     * This method is executed when we receive a list of responses from our notification request.
	 * 
     * @param request The room request
     * @param responses The responses to the request
	 */
	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		List<ActionPackage> pkgs;
		Ticket ticket;
		StringBuilder sb;
		
		SharedUtils.checkNotNull(request);
		SharedUtils.checkNotNull(responses);
		sb = new StringBuilder("Received responses for requested sememe(s): ");
		for (SememePackage sPkg : request.getSememePackages()) {
			sb.append("\n\t");
			sb.append(sPkg.getSememe().getName());
		}
		for (RoomResponse rr : responses) {
			if (!rr.hasNoResponse()) {
				pkgs = rr.getActionPackages();
				sb.append("\n\t\t");
				sb.append("From room " + rr.getFromRoom());
				for (ActionPackage pkg : pkgs) {
					sb.append(", action is " + pkg.getName());
				}
			}
		}
		LOGGER.debug(sb.toString());
		ticket = request.getTicket();
		ticket.close();
	}

	@Override
	protected void roomStartup() {

        allSememes = new ArrayList<>(Sememe.getAllSememes());
        scheduler = Executors.newScheduledThreadPool(1);
    }

	@Override
	protected void roomShutdown() {
        
	}

	/**
	 * Advertise which sememes we handle.
	 * 
     * @return The list of sememes we process
	 */
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("pilates"));
		return sememes;
	}

	/**
	 * We don't do anything with RoomAnnouncements but it is legitimate to receive them here,
	 * so no error.
	 * 
     * @param announcement
     * 
	 */
	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}
	
	private class RunSememe implements Runnable {
				
		RunSememe() {
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void run() {
			SerialSememe sememe;
			RoomRequest request;
			Ticket ticket;
			Set<Room> rooms;
			
			if (sememeIx >= allSememes.size()) {
				schedulerHandle.cancel(false);
				return;
			}
			sememe = allSememes.get(sememeIx++);
			if (sememe.is("pilates")) {
				// skip our own sememe to avoid the loop
				return;
			}
			LOGGER.debug("Creating request for sememe {}", sememe.getName());
			rooms = UrRoom.getRoomsForSememe(sememe);
			
			// create a request and put it on the bus
	        ticket = Ticket.createNewTicket(getHall(), getThisRoom());
	        ticket.setFromAlixianID(AlixiaConstants.getAlixiaAlixianID());
	        request = new RoomRequest(ticket);
	        request.setFromRoom(getThisRoom());
	        request.setMessage("Pilates");
	        qaRequester.updateRoomRequest(request, sememe, rooms);
	        if (!request.documentIsReady()) {
	        	throw new AlixiaException("QARoom: constructed bad request");
	        }
	        sendRoomRequest(request);
		}
		
	}
	
	private class RunSememes implements Runnable {
		
		RunSememes() {
		}

		@Override
		public void run() {
			RunSememe runner;
			
			sememeIx = 0;
			runner = new RunSememe();
			schedulerHandle = scheduler.scheduleAtFixedRate(runner, 1, 1, TimeUnit.SECONDS);
		}
		
	}
}
