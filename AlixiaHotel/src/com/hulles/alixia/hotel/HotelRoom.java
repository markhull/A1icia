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
package com.hulles.alixia.hotel;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.cayenne.NamedTimer;
import com.hulles.alixia.cayenne.Task;
import com.hulles.alixia.hotel.task.LoadTasks;
import com.hulles.alixia.house.ClientDialogResponse;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.ClientObjectWrapper;
import com.hulles.alixia.room.document.MessageAction;
import com.hulles.alixia.room.document.RoomActionObject;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.alixia.ticket.Ticket;

/**
 * Hotel Room has risen from the ashes and become our calendar room.
 * 
 * @author hulles
 *
 */
public final class HotelRoom extends UrRoom {
	private final static Logger LOGGER = Logger.getLogger("AlixiaHotel.AlixiaHotel");
	private final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
	private TimerHandler timerHandler;
	
	public HotelRoom() {
		super();
		
	}

	@Override
	public Room getThisRoom() {

		return Room.HOTEL;
	}

	/**
     * This method is executed when we receive a list of responses from our timer request. We
     * don't really care about the responses -- we just send an uber-request to
     * Alixia to forward the timer alert, and this is what she sends back.
	 * 
     * @param request The room request
     * @param responses The responses to the request
	 */
	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		List<ActionPackage> pkgs;
		RoomActionObject obj;
		MessageAction msgAction;
		Ticket ticket;
		ClientObjectWrapper cow;
		
		SharedUtils.checkNotNull(request);
		SharedUtils.checkNotNull(responses);
		// note that here we're ignoring the fact that we might get more than one response, 
		// particularly for the media request -- FIXME
		for (RoomResponse rr : responses) {
			if (!rr.hasNoResponse()) {
				// see if we can learn anything....
				pkgs = rr.getActionPackages();
				for (ActionPackage pkg : pkgs) {
					obj = pkg.getActionObject();
					if (obj instanceof MessageAction) {
						msgAction = (MessageAction) obj;
							LOGGER.log(LOGLEVEL, "We got some learning => {0} : {1}", 
                                    new String[]{msgAction.getMessage(), msgAction.getExplanation()});
					} else if (obj instanceof ClientObjectWrapper) {
						cow = (ClientObjectWrapper) obj;
						timerHandler.setMediaFile(cow);
					}
				}
			}
		}
		ticket = request.getTicket();
		ticket.close();
	}

	@Override
	protected void roomStartup() {
		List<Task> tasks;
		
		timerHandler = new TimerHandler(this);
		tasks = Task.getAllTasks();
		if (tasks.isEmpty()) {
			LoadTasks.loadTasks();
		}
	}

	@Override
	protected void roomShutdown() {
		
		timerHandler.close();
	}
	
	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "named_timer":
			case "duration_timer":
				return createTimerActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
		}
	}

	private ActionPackage createTimerActionPackage(SememePackage sememePkg, RoomRequest request) {
		String result;
		MessageAction response;
		ActionPackage pkg;
		NamedTimer dbTimer;
		SememePackage namedTimerPkg;
		String timerName;
		AlixianID alixianID;
		Ticket ticket;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		if (sememePkg.is("duration_timer")) {
			AlixiaUtils.error("HotelRoom: got duration timer sememe, but we can't handle that yet");
			return null;
		}
		// named_timer
		namedTimerPkg = sememePkg;
		timerName = namedTimerPkg.getSememeObject();
		dbTimer = NamedTimer.findNamedTimer(timerName);
		if (dbTimer == null) {
			AlixiaUtils.error("HotelRoom: can't find timer named " + timerName + " in database");
			return null;
		}
		ticket = request.getTicket();
		alixianID = ticket.getFromAlixianID();
		timerHandler.setNewTimer(alixianID, timerName, dbTimer.getDurationMs());
		pkg = new ActionPackage(sememePkg);
		response = new MessageAction();
		result = timerName + " timer is set.";
		response.setMessage(result);
		pkg.setActionObject(response);
		return pkg;
	}

    void postRequest(ClientDialogResponse response) {
    
        SharedUtils.checkNotNull(response);
        super.postPushRequest(response);
    }
    
	public void getMedia(Long timerID, String notificationTitle) {
		RoomRequest mediaRequest;
		Ticket ticket;
		SememePackage sememePkg;
		
		SharedUtils.checkNotNull(timerID);
		SharedUtils.checkNotNull(notificationTitle);
		// now we pop off a request to Mike or whoever to get us some media bytes
		ticket = Ticket.createNewTicket(getHall(), getThisRoom());
		ticket.setFromAlixianID(AlixiaConstants.getAlixiaAlixianID());
		mediaRequest = new RoomRequest(ticket);
		mediaRequest.setFromRoom(getThisRoom());
		sememePkg = SememePackage.getDefaultPackage("notification_medium");
		sememePkg.setSememeObject(notificationTitle);
		mediaRequest.setSememePackages(Collections.singletonList(sememePkg));
		mediaRequest.setMessage(timerID.toString()); // sort of a kluge, but...
//		mediaRequest.setMindObject(response);
		sendRoomRequest(mediaRequest);
	}
	
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("named_timer"));
		sememes.add(SerialSememe.find("duration_timer"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
