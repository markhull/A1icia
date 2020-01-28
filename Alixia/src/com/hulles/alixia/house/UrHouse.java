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

import static com.hulles.alixia.house.Translator.translateRequest;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractService;
import com.hulles.alixia.api.dialog.Dialog;
import com.hulles.alixia.api.dialog.DialogRequest;
import com.hulles.alixia.api.dialog.DialogResponse;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.remote.Station;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SessionType;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.media.Language;
import com.hulles.alixia.tools.ExternalAperture;

/**
 * UrHouse is the superclass for all of the Alixia houses that listen on the street bus.
 * 
 * @author hulles
 *
 */
public abstract class UrHouse extends AbstractService {
	private final static Logger LOGGER = LoggerFactory.getLogger(UrHouse.class);
	private EventBus street;
	private final ConcurrentMap<AlixianID, Session> sessions;
	private final Station station;
    private PrompterManager prompterManager;
	private Boolean noPrompt = false;
    
	public UrHouse() {
		
		station = Station.getInstance();
		station.ensureStationExists();
		this.sessions = new ConcurrentHashMap<>();
	}
    public UrHouse(Boolean noPrompt) {
        this();
        
        SharedUtils.checkNotNull(noPrompt);
        this.noPrompt = noPrompt;
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
		if (prompterManager != null) {
			prompterManager.close();
		}
        notifyStopped();
	}
	
    public final void setStreet(EventBus streetBus) {
    
        this.street = streetBus;
        prompterManager = new PrompterManager(streetBus, noPrompt);
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
		LOGGER.debug("UrHouse: dialogArrival for {}", thisHouseName);
		toAlixianID = dialog.getToAlixianID();
		if (!isOurSession(toAlixianID)) {
			LOGGER.debug("UrHouse: not our session: {}", toAlixianID);
			return;
		}
		if (dialog instanceof DialogRequest) {
			LOGGER.debug("UrHouse: dialog request");
			request = (DialogRequest) dialog;
			newDialogRequest(request);
		} else if (dialog instanceof DialogResponse) {
			LOGGER.debug("UrHouse: dialog response");
			response = (DialogResponse) dialog;
			newDialogResponse(response);
		}
	}
			
	/**
	 * Receive a DialogRequest which we post onto the street bus. Note that we also
	 * convert audio in the request to text and translate (!) the request into 
     * American English prior to posting it.
	 * 
	 * @param dialogRequest The request
	 */
	protected void receiveRequestFromClient(DialogRequest dialogRequest) {
		AlixianID fromAlixianID;
		Session session;
		SerialSememe sememe;
		Set<SerialSememe> sememesCopy;
		
		SharedUtils.checkNotNull(dialogRequest);
		fromAlixianID = dialogRequest.getFromAlixianID();
		LOGGER.debug("UrHouse: dialog request from {}", fromAlixianID);
		sememesCopy = new HashSet<>(dialogRequest.getRequestActions());
		sememe = SerialSememe.consume("client_startup", sememesCopy);
		LOGGER.debug("UrHouse: consumed startup , sememe = {}", sememe);
		if (sememe != null) {
			// it's a new session
			LOGGER.debug("UrHouse: starting new session for {}", fromAlixianID);
			session = Session.getSession(fromAlixianID);
			session.setPersonUUID(dialogRequest.getPersonUUID());
			session.setStationUUID(dialogRequest.getStationUUID());
			session.setLanguage(dialogRequest.getLanguage());
            session.setSessionType(SessionType.SERIALIZED);
            session.setIsQuiet(dialogRequest.isQuiet());
			setSession(session);
			return; // we don't need to pass this along, at least for now
		} else if (isOurSession(fromAlixianID)) {
			session = getSession(fromAlixianID);
			sememe = SerialSememe.consume("client_shutdown", sememesCopy);
			if (sememe != null) {
				// close the session
				LOGGER.debug("UrHouse: closing session for {}", fromAlixianID);
				removeSession(session);
//				return; // we don't need to pass this on, at least for now
			} else {
				// update the session
				LOGGER.debug("UrHouse: updating session for {}", fromAlixianID);
				session.update();
                // is it really true that these might have changed since the session was created? TODO
//				session.setPersonUUID(dialogRequest.getPersonUUID());
//				session.setLanguage(dialogRequest.getLanguage());
			}
		} else {
			// not startup (no startup sememe), but session doesn't exist in our map, 
            //    so station was up prior to our starting (we presume)
			LOGGER.debug("UrHouse: starting (pre-existing) new session for {}", fromAlixianID);
			session = Session.getSession(fromAlixianID);
			session.setPersonUUID(dialogRequest.getPersonUUID());
			session.setStationUUID(dialogRequest.getStationUUID());
			session.setLanguage(dialogRequest.getLanguage());
            session.setIsQuiet(dialogRequest.isQuiet());
            session.setSessionType(SessionType.SERIALIZED);
			LOGGER.debug("UrHouse: before setSession for {}", fromAlixianID);
			setSession(session);
			LOGGER.debug("UrHouse: after setSession for {}", fromAlixianID);
		}
		dialogRequest.setRequestActions(sememesCopy);
		LOGGER.debug("UrHouse: made it past session checks for {}", fromAlixianID);
        LOGGER.debug("UrHouse: prompter update for {}", fromAlixianID);
        prompterManager.resetPrompter(fromAlixianID, session.getSessionType(), 
                session.getLanguage(), session.isQuiet());
		speechToText(dialogRequest, session.getLanguage());
        translateRequest(dialogRequest, session.getLanguage());
        if (!dialogRequest.isValid()) {
            LOGGER.error("DialogRequest: {}", dialogRequest.toString());
            throw new AlixiaException("UrHouse: created invalid DialogRequest");
        }
		LOGGER.debug("UrHouse: posting dialog request for {}", fromAlixianID);
        getStreet().post(dialogRequest);
	}
	
	/**
	 * Convert an audio file included in the DialogRequest to text.
	 * 
	 * @param request The request containing the audio file
	 * @param lang The language in which the speech is recorded
	 */
	private static void speechToText(DialogRequest request, Language lang) {
		byte[] audioBytes;
		String audioText;
		
		SharedUtils.checkNotNull(request);
		SharedUtils.checkNotNull(lang);
		audioBytes = request.getRequestAudio();
		if (audioBytes != null) {
			try {
				audioText = ExternalAperture.queryDeepSpeech(audioBytes);
			} catch (Exception ex) {
				LOGGER.error("UrHouse: unable to transcribe audio", ex);
				return;
			}
	        LOGGER.debug("UrHouse: audioText is \"{}\"", audioText);
			if (audioText.length() > 0) {
				// note that this overwrites any message text that was also sent in the DialogRequest...
				request.setRequestMessage(audioText);
			}
		}
	}
	
    /**
     * Set up a prompter for this Alixian.
     * 
     * @param alixianID
     * @param sessionType
     * @param language
     * @param isQuiet 
     */
    protected void promptFor(AlixianID alixianID, SessionType sessionType, Language language, Boolean isQuiet) {
        
        prompterManager.resetPrompter(alixianID, sessionType, language, isQuiet);
    }
    
    /**
     * Turn prompts on and off.
     * 
     * @param noPrompt 
     */
    protected void setNoPrompts(Boolean noPrompt) {
    
        SharedUtils.checkNotNull(noPrompt);
        this.noPrompt = noPrompt;
        if (prompterManager != null) {
            prompterManager.setNoPrompts(noPrompt);
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
