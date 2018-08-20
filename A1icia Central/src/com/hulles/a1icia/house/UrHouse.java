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
package com.hulles.a1icia.house;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.hulles.a1icia.api.dialog.Dialog;
import com.hulles.a1icia.api.dialog.DialogRequest;
import com.hulles.a1icia.api.dialog.DialogResponse;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.remote.Station;
import com.hulles.a1icia.api.shared.SharedUtils;

/**
 * UrHouse is the superclass for all of the A1icia houses that listen on the street bus.
 * 
 * @author hulles
 *
 */
public abstract class UrHouse extends AbstractExecutionThreadService {
	private final static Logger LOGGER = Logger.getLogger("A1icia.UrHouse");
	private final static Level LOGLEVEL = LOGGER.getParent().getLevel();
	private final EventBus street;
	private final ConcurrentMap<A1icianID, Session> sessions;
	private final Station station;
	
	public UrHouse(EventBus street) {
		
		SharedUtils.checkNotNull(street);
		station = Station.getInstance();
		station.ensureStationExists();
		this.street = street;
		this.sessions = new ConcurrentHashMap<>();
	}
	
	@Override
	protected final void startUp() {
		
		street.register(this);
		houseStartup();
	}
	
	@Override
	protected final void shutDown() {

		houseShutdown();
		street.unregister(this);
	}
	
	protected EventBus getStreet() {
	
		return street;
	}
	
	/**
	 * There is a new Dialog on the street, so we examine it to see if it's to one of 
	 * our A1icians, and if so, we call newDialogRequest or newDialogResponse, depending.
	 * 
	 * @param dialog
	 */
	@Subscribe void dialogArrival(Dialog dialog) {
		DialogRequest request;
		DialogResponse response;
		A1icianID toA1icianID;
		
		SharedUtils.checkNotNull(dialog);
		LOGGER.log(LOGLEVEL, "UrHouse: dialogArrival for " + getThisHouse());
		toA1icianID = dialog.getToA1icianID();
		if (!isOurSession(toA1icianID)) {
			LOGGER.log(LOGLEVEL, "UrHouse: not our session: " + toA1icianID);
			return;
		}
		if (dialog instanceof DialogRequest) {
			LOGGER.log(LOGLEVEL, "UrHouse: dialog request");
			request = (DialogRequest) dialog;
			newDialogRequest(request);
		} else if (dialog instanceof DialogResponse) {
			LOGGER.log(LOGLEVEL, "UrHouse: dialog response");
			response = (DialogResponse) dialog;
			newDialogResponse(response);
		}
	}
	
	/**
	 * Return true if the A1icianID is in our sessions map, i.e. it's to one of
	 * our A1icians.
	 * 
	 * @param a1icianID
	 * @return True if we have a session for the A1ician, false otherwise
	 */
	protected boolean isOurSession(A1icianID a1icianID) {
	
		SharedUtils.checkNotNull(a1icianID);
		return sessions.containsKey(a1icianID);
	}
	
	/**
	 * Get a session for this A1ician if it exists in the map.
	 * 
	 * @param a1icianID The ID of the A1ician
	 * @return The session, or null if there isn't one
	 */
	protected Session getSession(A1icianID a1icianID) {
		Session session;
		
		SharedUtils.checkNotNull(a1icianID);
		session = sessions.get(a1icianID);
		return session;
	}
	
	/**
	 * Record a session in the sessions map.
	 * 
	 * @param session
	 */
	protected void setSession(Session session) {
		
		SharedUtils.checkNotNull(session);
		sessions.put(session.getA1icianID(), session);
	}
	
	/**
	 * Remove a session from the sessions map.
	 * 
	 * @param session
	 */
	protected void removeSession(Session session) {
		
		SharedUtils.checkNotNull(session);
		sessions.remove(session.getA1icianID());
	}
	
	protected abstract House getThisHouse();
	
	protected abstract void newDialogRequest(DialogRequest request);
	
	protected abstract void newDialogResponse(DialogResponse response);
	
	protected abstract void houseStartup();
	
	protected abstract void houseShutdown();
}
