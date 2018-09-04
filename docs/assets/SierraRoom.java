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
package com.hulles.alixia.sierra;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.base.AlixiaException;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.MessageAction;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.room.document.SememeAnalysis;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SentencePackage;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.alixia.ticket.Ticket;
import com.hulles.alixia.ticket.TicketJournal;
import com.hulles.alixia.tools.AlixiaUtils;

/**
 * Sierra Room is where we interact with the so-called Internet of Things, and let Alixia set 
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
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
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
		
		AlixiaUtils.checkNotNull(sememePkg);
		AlixiaUtils.checkNotNull(request);
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
				throw new AlixiaException("SierraRoom: created invalid sememe package");
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
		
		AlixiaUtils.checkNotNull(sememePkg);
		AlixiaUtils.checkNotNull(request);
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
