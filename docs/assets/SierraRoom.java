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
package com.hulles.a1icia.sierra;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.room.document.SememeAnalysis;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SentencePackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.ticket.TicketJournal;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * Sierra Room is where we interact with the so-called Internet of Things, and let A1icia set 
 * our stereo to turn on and blast Rick Astley at concert volume while we're on vacation.
 * 
 * @author hulles
 *
 */
public final class SierraRoom extends UrRoom {
	
	public SierraRoom(EventBus bus) {
		super(bus);
	}

	/**
	 * Here we create an ActionPackage from Sierra, either an analysis or an action, depending 
	 * on the sememe that we receive, and return it to UrRoom.
	 * 
	 */
	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "sememe_analysis":
				return createAnalysisActionPackage(sememePkg, request);
			case "bullfrog":
				return createBullfrogActionPackage(sememePkg, request);
			default:
				throw new A1iciaException("Received unknown sememe in " + getThisRoom());
		}
	}
	
	/**
	 * We stick "bullfrog" in as the best sememe package for each sentence.
	 * Because we can.
	 * 
	 * @param sememePkg
	 * @param request
	 * @return A SememeAnalysis package
	 */
	private static ActionPackage createAnalysisActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage actionPkg;
		SememeAnalysis analysis;
		Ticket ticket;
		TicketJournal journal;
		List<SememePackage> sememePackages;
		List<SentencePackage> sentencePackages;
		SememePackage bullPkg;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		ticket = request.getTicket();
		journal = ticket.getJournal();
		actionPkg = new ActionPackage(sememePkg);
		sememePackages = new ArrayList<>();
		sentencePackages = journal.getSentencePackages();
		analysis = new SememeAnalysis();
		for (SentencePackage sentencePackage : sentencePackages) {
			bullPkg = SememePackage.getDefaultPackage("bullfrog");
			bullPkg.setSentencePackage(sentencePackage);
			bullPkg.setConfidence(10);
			if (!bullPkg.isValid()) {
				throw new A1iciaException("SierraRoom: created invalid sememe package");
			}
			sememePackages.add(bullPkg);
		}
		analysis.setSememePackages(sememePackages);
		actionPkg.setActionObject(analysis);
		return actionPkg;
	}

	/**
	 * Create an action package for the "bullfrog" sememe, which consists of saying "bullfrog"
	 * 
	 * @param sememePkg
	 * @param request
	 * @return
	 */
	private static ActionPackage createBullfrogActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		MessageAction action;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		action = new MessageAction();
		action.setMessage("Bullfrog.");
		action.setExplanation("I said, \"Bullfrog!\"");
		pkg.setActionObject(action);
		return pkg;
	}
	
	/**
	 * Return this room name.
	 * 
	 */
	@Override
	public Room getThisRoom() {

		return Room.SIERRA;
	}

	/**
	 * We don't get responses for anything, so if there is one it's an error.
	 * 
	 */
	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
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
	 * Advertise which sememes we handle.
	 * 
	 */
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("sememe_analysis"));
		sememes.add(SerialSememe.find("bullfrog"));
		return sememes;
	}

	/**
	 * We don't do anything with RoomAnnouncements but it is legitimate to receive them here,
	 * so no error.
	 * 
	 */
	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
