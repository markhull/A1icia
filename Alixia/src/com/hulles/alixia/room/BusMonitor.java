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
package com.hulles.alixia.room;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.room.document.RoomActionObject;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomDocument;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;

/**
 * BusMonitor just lurks around logging whatever is on the hall bus.
 * 
 * @author hulles
 *
 */
public final class BusMonitor extends UrRoom {
	private final static Logger LOGGER = LoggerFactory.getLogger(BusMonitor.class);
	private final static boolean VERBOSE = false;
	private final static boolean SHOWWHATSPARKS = false;
	
	public BusMonitor(EventBus hall) {
		super(hall);
	}

	/**
	 * Log a room document. This is the method that functions with the Guava event bus.
	 * 
	 * @param document
	 */
	@Subscribe public static void logRoomDocument(RoomDocument document) {
		String msg;
		RoomAnnouncement announcement;
		RoomRequest request;
		RoomResponse response;
		List<ActionPackage> pkgs;
		StringBuffer sb;
		RoomActionObject actionObj;
		SerialSememe sememe;
        String docID;
        String room;
		
		SharedUtils.checkNotNull(document);
		if (document instanceof RoomAnnouncement) {
			announcement = (RoomAnnouncement) document;
			msg = announcement.getDocumentType().name();
            if (msg == null) {
                msg = "(no msg)";
            }
			LOGGER.debug("Hall Bus ANNOUNCEMENT: {}", msg);
		} else	if (document instanceof RoomRequest) {
			request = (RoomRequest) document;
			sb = new StringBuffer();
			sb.append("\nSememe Packages: ");
			for (SememePackage sp : request.getSememePackages()) {
				sb.append(sp.getName());
				sb.append(" ");
			}
			msg = sb.toString();
            docID = request.getDocumentID().toString();
            room = request.getFromRoom().getDisplayName();
			LOGGER.debug("Hall Bus REQUEST {}: {} {}", docID, room, msg);
		} else if (document instanceof RoomResponse) {
			response = (RoomResponse) document;
			if (!VERBOSE) {
				if (response.hasNoResponse()) {
					return;
				}
			}
			if (VERBOSE) {
				pkgs = response.getActionPackages();
				sb = new StringBuffer();
				for (ActionPackage pkg : pkgs) {
					sememe = pkg.getSememe();
					if (sememe.is("what_sememes") && !SHOWWHATSPARKS) {
						continue;
					}
					sb.append("\nAction Package for ");
					sb.append(pkg.getSememe().getName());
					sb.append("\n\tMessage: ");
					actionObj = pkg.getActionObject();
					sb.append(actionObj.getMessage());
					sb.append("\n\tExplanation:\n");
					sb.append(actionObj.getExplanation());
				}
				msg = sb.toString();
                room = response.getFromRoom().getDisplayName();
				LOGGER.debug("Hall Bus RESPONSE: {} {}", room, msg);
			}
		} else {
            if (document == null) {
                LOGGER.debug("Hall Bus NULL DOCUMENT");
            } else {
                LOGGER.debug("Hall Bus UNKNOWN DOCUMENT: {}", document.getClass().getName());
            }
		}
	}

	/**
	 * Return this room.
	 * 
     * @return The name of our room
     * 
	 */
	@Override
	public Room getThisRoom() {

		return Room.BUSMONITOR;
	}

	/**
	 * BusMonitor does not handle responses, and shouldn't receive any here.
	 * 
     * @param request
     * @param responses
     * 
	 */
	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new AlixiaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {
	}

	@Override
	protected void roomShutdown() {
	}

	/**
	 * BusMonitor does not react to sememes, although it could someday.
	 * 
     * @param pkg The sememe package
     * @param request The room request
     * @return The ActionPackage
	 */
	@Override
	public ActionPackage createActionPackage(SememePackage pkg, RoomRequest request) {
		throw new AlixiaException("Request not implemented in " + 
				getThisRoom().getDisplayName());
	}

	/**
	 * Return the set of sememes which we handle here, i.e. none (empty set).
     * 
     * @return The sememes we handle, i.e. the empty set
     * 
	 */
	@Override
	protected Set<SerialSememe> loadSememes() {
		
		return Collections.emptySet();
	}

	/**
	 * BusMonitor does not handle room announcements, at least as called by UrRoom.
     * 
     * @param announcement The room announcement
	 * 
	 */
	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
