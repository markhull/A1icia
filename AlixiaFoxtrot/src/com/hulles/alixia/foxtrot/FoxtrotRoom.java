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
package com.hulles.alixia.foxtrot;

import com.hulles.alixia.api.shared.AlixiaException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.crypto.PurdahKeys;
import com.hulles.alixia.crypto.PurdahKeys.PurdahKey;
import com.hulles.alixia.foxtrot.monitor.FoxtrotPhysicalState;
import com.hulles.alixia.foxtrot.monitor.LinuxMonitor;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;

/**
 * Foxtrot Room is where Alixia can query herself about her own status and health. 
 * 
 * @author hulles
 *
 */
public final class FoxtrotRoom extends UrRoom {
	private LinuxMonitor sysMonitor;
	
	public FoxtrotRoom() {
		super();
		
	}

	/*
	 * Create the action package for Foxtrot-handled sparks.
	 * (non-Javadoc)
	 * @see com.hulles.alixia.room.UrRoom#createActionPackage(com.hulles.alixia.ticket.SememePackage, com.hulles.alixia.room.document.RoomRequest)
	 */
	@Override
	protected ActionPackage createActionPackage(SememePackage sparkPkg, RoomRequest request) {

		switch (sparkPkg.getName()) {
			case "check_warnings":
			case "how_are_you":
			case "inquire_status":
				return createStatusActionPackage(sparkPkg);
			default:
				throw new AlixiaException("Received unknown spark in " + getThisRoom());
		}
	}

	/**
	 * Create an action package for status-related sparks
	 * 
	 * @param sparkPkg The initiating spark package
	 * @return The action package
	 */
	private ActionPackage createStatusActionPackage(SememePackage sparkPkg) {
		ActionPackage pkg;
		FoxtrotAction action;
		FoxtrotPhysicalState state;
		
		pkg = new ActionPackage(sparkPkg);
		action = new FoxtrotAction();
		state = sysMonitor.getFoxtrotPhysicalState();
		action.setState(state);
		pkg.setActionObject(action);
		return pkg;
	}

	@Override
	public Room getThisRoom() {

		return Room.FOXTROT;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new AlixiaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	/*
	 * Start up the Foxtrot room and initialize the system monitor. We currently only support
	 * the Linux system monitor.
	 * (non-Javadoc)
	 * @see com.hulles.alixia.room.UrRoom#roomStartup()
	 */
	@Override
	protected void roomStartup() {
		String osName;
		PurdahKeys purdahKeys;
		String portStr;
		
		purdahKeys = PurdahKeys.getInstance();
		osName = System.getProperty("os.name");
		switch (osName) {
			case "Linux":
				sysMonitor = new LinuxMonitor();
				sysMonitor.setDatabaseUser(purdahKeys.getPurdahKey(PurdahKey.DATABASEUSER));
				sysMonitor.setDatabasePassword(purdahKeys.getPurdahKey(PurdahKey.DATABASEPW));
				sysMonitor.setDatabaseServer(purdahKeys.getPurdahKey(PurdahKey.DATABASESERVER));
				portStr = purdahKeys.getPurdahKey(PurdahKey.DATABASEPORT);
				sysMonitor.setDatabasePort(Integer.parseInt(portStr));
				break;
			default:
				throw new UnsupportedOperationException("This operating system is not supported");
		}
	}

	@Override
	protected void roomShutdown() {
		
	}
	
	/*
	 * Load the sparks we handle.
	 * (non-Javadoc)
	 * @see com.hulles.alixia.room.UrRoom#loadSememes()
	 */
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sparks;
		
		sparks = new HashSet<>();
		sparks.add(SerialSememe.find("check_warnings"));
		sparks.add(SerialSememe.find("how_are_you"));
		sparks.add(SerialSememe.find("inquire_status"));
		return sparks;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
