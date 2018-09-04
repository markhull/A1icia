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
package com.hulles.alixia.romeo;

import com.hulles.alixia.api.shared.AlixiaException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.MessageAction;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;

/**
 * Romeo Room is the game room. Sound clips to the contrary, "Global Thermonuclear War" is not
 * one of them.
 * 
 * It is currently just a stub copied from Alpha. We can proceed on it once we nail down conversations.
 * 
 * @author hulles
 *
 */
public final class RomeoRoom extends UrRoom {
	
	public RomeoRoom() {
		super();
	}

	/**
	 * Here we create an ActionPackage from Romeo, either an analysis or an action, depending 
	 * on the sememe that we receive, and return it to UrRoom.
	 * 
     * @param sememePkg The sememe
     * @param request The incoming request
     * @return The appropriate ActionPackage
	 */
	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "name_that_tune":
				return createNTTActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
		}
	}

	/**
	 * Create an action package for the "Name That Tune" game.
	 * 
	 * @param sememePkg The name_that_tune sememe
	 * @param request The incoming room request
	 * @return
	 */
	private static ActionPackage createNTTActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		MessageAction action;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		action = new MessageAction();
		action.setMessage("Okay, get ready.");
		action.setExplanation("You have to guess the song coming up.");
		pkg.setActionObject(action);
		return pkg;
	}
	
	/**
	 * Return this room name.
	 * 
     * @return This room
	 */
	@Override
	public Room getThisRoom() {

		return Room.ROMEO;
	}

	/**
	 * We don't get responses for anything, so if there is one it's an error.
	 * 
     * @param request The room request we (didn't) sent
     * @param responses The responses we (didn't) got
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
     * @return The set of sememes that we support
	 */
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("name_that_tune"));
		return sememes;
	}

	/**
	 * We don't do anything with RoomAnnouncements but it is legitimate to receive them here,
	 * so no error.
	 * 
     * @param announcement The announcement
     * 
	 */
	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
