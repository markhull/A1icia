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
package com.hulles.alixia.house;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractService;
import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.dialog.Dialog;
import com.hulles.alixia.api.dialog.DialogRequest;
import com.hulles.alixia.api.dialog.DialogResponse;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.remote.Station;
import com.hulles.alixia.api.shared.SharedUtils;

/**
 * UrHouse is the superclass for all of the Alixia houses that listen on the street bus.
 * 
 * @author hulles
 *
 */
public abstract class UrHouse extends AbstractService {
	private final static Logger LOGGER = Logger.getLogger("Alixia.UrHouse");
	private final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
	private EventBus street;
	private final ConcurrentMap<AlixianID, Session> sessions;
	private final Station station;
	
	public UrHouse() {
		
		station = Station.getInstance();
		station.ensureStationExists();
		this.sessions = new ConcurrentHashMap<>();
	}
    public UrHouse(EventBus streetBus) {
        this();
        
        SharedUtils.checkNotNull(streetBus);
        setStreet(streetBus);
    }
	
	@Override
	protected final void doStart() {
		
		street.register(this);
		houseStartup();
        notifyStarted();
	}
	
	@Override
	protected final void doStop() {

		houseShutdown();
		street.unregister(this);
        notifyStopped();
	}
	
    public final void setStreet(EventBus streetBus) {
    
        this.street = streetBus;
    }
    
	protected EventBus getStreet() {
	
		return street;
	}
	
	/**
	 * There is a new Dialog on the street, so we examine it to see if it's to one of 
	 * our Alixians, and if so, we call newDialogRequest or newDialogResponse, depending.
	 * 
	 * @param dialog
	 */
	@Subscribe public void dialogArrival(Dialog dialog) {
		DialogRequest request;
		DialogResponse response;
		AlixianID toAlixianID;
		String thisHouseName;
        
		SharedUtils.checkNotNull(dialog);
        thisHouseName = this.getThisHouse().getDisplayName();
		LOGGER.log(LOGLEVEL, "UrHouse: dialogArrival for {0}", thisHouseName);
		toAlixianID = dialog.getToAlixianID();
		if (!isOurSession(toAlixianID)) {
			LOGGER.log(LOGLEVEL, "UrHouse: not our session: {0}", toAlixianID);
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
	 * Return true if the AlixianID is in our sessions map, i.e. it's to one of
	 * our Alixians.
	 * 
	 * @param alixianID
	 * @return True if we have a session for the Alixian, false otherwise
	 */
	public boolean isOurSession(AlixianID alixianID) {
	
		SharedUtils.checkNotNull(alixianID);
		return sessions.containsKey(alixianID);
	}
	
	/**
	 * Get a session for this Alixian if it exists in the map.
	 * 
	 * @param alixianID The ID of the Alixian
	 * @return The session, or null if there isn't one
	 */
	public Session getSession(AlixianID alixianID) {
		Session session;
		
		SharedUtils.checkNotNull(alixianID);
		session = sessions.get(alixianID);
		return session;
	}
	
	/**
	 * Record a session in the sessions map.
	 * 
	 * @param session
	 */
	protected void setSession(Session session) {
		
		SharedUtils.checkNotNull(session);
		sessions.put(session.getAlixianID(), session);
	}
	
	/**
	 * Remove a session from the sessions map.
	 * 
	 * @param session
	 */
	protected void removeSession(Session session) {
		
		SharedUtils.checkNotNull(session);
		sessions.remove(session.getAlixianID());
	}
	
	public abstract House getThisHouse();
	
	protected abstract void newDialogRequest(DialogRequest request);
	
	protected abstract void newDialogResponse(DialogResponse response);
	
	protected abstract void houseStartup();
	
	protected abstract void houseShutdown();
}
