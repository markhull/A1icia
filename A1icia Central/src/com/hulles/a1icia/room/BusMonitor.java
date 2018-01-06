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
package com.hulles.a1icia.room;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.SerialSpark;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.room.document.RoomActionObject;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomDocument;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * BusMonitor just lurks around logging whatever is on the hall bus.
 * 
 * @author hulles
 *
 */
public final class BusMonitor extends UrRoom {
	private final static Logger logger = Logger.getLogger("A1icia.BusMonitor");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final static boolean VERBOSE = false;
	private final static boolean SHOWWHATSPARKS = false;
	
	public BusMonitor(EventBus bus) {
		super(bus);
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
		SerialSpark spark;
		
		A1iciaUtils.checkNotNull(document);
		if (document instanceof RoomAnnouncement) {
			announcement = (RoomAnnouncement) document;
			msg = announcement.getDocumentType().name();
			logger.log(LOGLEVEL, "Mind Bus ANNOUNCEMENT: " + (msg == null ? "(no msg)" : msg));
		} else	if (document instanceof RoomRequest) {
			request = (RoomRequest) document;
			sb = new StringBuffer();
			sb.append("\nSpark Packages: ");
			for (SparkPackage sp : request.getSparkPackages()) {
				sb.append(sp.getName());
				sb.append(" ");
			}
			msg = sb.toString();
			logger.log(LOGLEVEL, "Mind Bus REQUEST " + request.getDocumentID() + ": " + request.getFromRoom().getDisplayName() + 
					" " + msg);
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
					spark = pkg.getSpark();
					if (spark.is("what_sparks") && !SHOWWHATSPARKS) {
						continue;
					}
					sb.append("\nAction Package for ");
					sb.append(pkg.getSpark().getName());
					sb.append("\n\tMessage: ");
					actionObj = pkg.getActionObject();
					sb.append(actionObj.getMessage());
					sb.append("\n\tExplanation:\n");
					sb.append(actionObj.getExplanation());
				}
				msg = sb.toString();
				logger.log(LOGLEVEL, "Mind Bus RESPONSE: " + response.getFromRoom().getDisplayName() +
						" " + msg);
			}
		} else {
			logger.log(LOGLEVEL, "Mind Bus UNKNOWN DOCUMENT: " + document.getClass().getName());
		}
	}

	/**
	 * Return this room.
	 * 
	 */
	@Override
	public Room getThisRoom() {

		return Room.BUSMONITOR;
	}

	/**
	 * BusMonitor does not handle responses, and shouldn't receive any here.
	 * 
	 */
	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> response) {
		throw new A1iciaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {
	}

	@Override
	protected void roomShutdown() {
	}

	/**
	 * BusMonitor does not react to sparks, although it could someday.
	 * 
	 */
	@Override
	public ActionPackage createActionPackage(SparkPackage pkg, RoomRequest request) {
		throw new A1iciaException("Request not implemented in " + 
				getThisRoom().getDisplayName());
	}

	/**
	 * Return the set of sparks which we handle here, i.e. none (empty set).
	 */
	@Override
	protected Set<SerialSpark> loadSparks() {
		
		return Collections.emptySet();
	}

	/**
	 * BusMonitor does not handle room announcements, at least as called by UrRoom.
	 * 
	 */
	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
