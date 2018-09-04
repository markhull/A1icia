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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.dialog.Dialog;
import com.hulles.alixia.api.dialog.DialogHeader;
import com.hulles.alixia.api.dialog.DialogRequest;
import com.hulles.alixia.api.dialog.DialogResponse;
import com.hulles.alixia.api.dialog.DialogSerialization;
import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusBible.JebusKey;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SerialStation;
import com.hulles.alixia.api.shared.SerialUUID;
import com.hulles.alixia.api.shared.SerialUUID.UUIDException;
import com.hulles.alixia.api.shared.SessionType;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.crypto.PurdahKeys;
import com.hulles.alixia.media.Language;
import com.hulles.alixia.tools.ExternalAperture;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * StationServer is responsible for communication with the outside world, notably with
 * AlixiaStations.
 * <p>
 * ALIXIA PUBLISHES ON "alixia:channel:from" and SUBSCRIBES TO "alixia:channel:to".
 * 
 * @author hulles
 *
 */
public final class StationServer extends UrHouse {
	final static Logger LOGGER = Logger.getLogger("Alixia.StationServer");
	final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
//	final static Level LOGLEVEL = Level.INFO;
    JebusListener listener = null;
    JebusTextListener textListener = null;
	ExecutorService executor;
	Timer promptTimer;
	private List<Prompter> prompters;
	private final static int PROMPTDELAY = 45 * 1000;
	private final static int NAGDELAY = 60 * 1000;
	final JebusPool jebusPool;
	private final AlixianID alixiaAlixianID;
	private final AlixianID broadcastID;
	private final Boolean noPrompts;
	
	public StationServer(EventBus street, Boolean noPrompts) {
		super(street);
	
		SharedUtils.checkNotNull(noPrompts);
		this.noPrompts = noPrompts;
		jebusPool = JebusHub.getJebusCentral(true);
		LOGGER.log(Level.INFO, "Station Server Jebus is {0}", JebusHub.getCentralServerName());
		alixiaAlixianID = AlixiaConstants.getAlixiaAlixianID();
		broadcastID = AlixiaConstants.getBroadcastAlixianID();
	}

	/**
	 * Return which house we are.
	 * 
	 *
     * @return Our house
	 */
	@Override
	public House getThisHouse() {
		return House.STATIONSERVER;
	}

	/**
	 * We don't handle incoming dialog requests, so if one is addressed to us it's an error.
	 * 
	 * @see UrHouse
     * 
     * @param request The incoming DialogRequest
	 * 
	 */
	@Override
	protected void newDialogRequest(DialogRequest request) {
		throw new AlixiaException("Request not implemented in " + getThisHouse());
	}

	/**
	 * We got a response from Alixia (presumably) so we send it along to its ultimate
	 * destination Alixian.
	 * 
     * @param response The incoming DialogResponse to be routed
	 */
	@Override
	protected void newDialogResponse(DialogResponse response) {
		AlixianID alixianID;
		Session session;
        
		SharedUtils.checkNotNull(response);
        LOGGER.log(LOGLEVEL, "StationServer: got response from Alixia");
        alixianID = response.getToAlixianID();
        session = Session.getSession(alixianID);
        if (session == null) {
            throw new AlixiaException("Can't get session");
        }
        switch (session.getSessionType()) {
            case SERIALIZED:
                stationSend(alixianID, response);
                break;
            case TEXT:
                stationSendText(alixianID, response.getMessage());
                break;
            default:
                throw new AlixiaException("Bad choice in get session type = " + session.getSessionType());
                
        }
	}

	/**
	 * We use houseStartup to create our executor pool and send a startup broadcast announcement.
	 * 
	 */
	@Override
	protected void houseStartup() {
		SerialSememe openSememe;
		byte[] channel;
        String channelStr;
		
		executor = Executors.newCachedThreadPool();
		if (!noPrompts) {
			promptTimer = new Timer();
			prompters = new ArrayList<>();
		}
        
        listener = new JebusListener();
        channel = JebusBible.getBytesKey(JebusKey.TOCHANNEL, jebusPool);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try (Jedis jebus = jebusPool.getResource()) {
                // the following line blocks while waiting for responses...
                    jebus.subscribe(listener, channel);
                }
            }
        });
            
        textListener = new JebusTextListener();
        channelStr = JebusBible.getStringKey(JebusKey.TOTEXTCHANNEL, jebusPool);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try (Jedis jebus = jebusPool.getResource()) {
                    // the following line blocks while waiting for responses...
                    jebus.subscribe(textListener, channelStr);
                }
            }
        });
        
		openSememe = SerialSememe.find("central_startup");
		stationBroadcast("Alixia Central starting up....", openSememe);
        
//        String translation = translate(Language.AMERICAN_ENGLISH, Language.FRENCH, "Where is my aunt's pen?");
//        System.out.println("TRANSLATION: " + translation);
	}

	/**
	 * Send a shutdown announcement and close down the executor pool.
	 * 
	 */
	@Override
	protected void houseShutdown() {
		SerialSememe closeSememe;
		
		closeSememe = SerialSememe.find("central_shutdown");
		stationBroadcast("Alixia Central shutting down....", closeSememe);
		if (listener != null) {
            listener.unsubscribe();
            listener = null;
		}
		if (textListener != null) {
            textListener.unsubscribe();
            textListener = null;
		}
		if (executor != null) {
			try {
			    LOGGER.log(LOGLEVEL, "StationServer: attempting to shutdown executor");
			    executor.shutdown();
			    executor.awaitTermination(3, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
			    AlixiaUtils.error("StationServer: executor shutdown interrupted");
			} finally {
			    if (!executor.isTerminated()) {
			        AlixiaUtils.error("StationServer: cancelling non-finished tasks");
			    }
			    executor.shutdownNow();
			    LOGGER.log(LOGLEVEL, "StationServer: shutdown finished");
			}
		}
		executor = null;
		if (!noPrompts) {
			for (Prompter prompter : prompters) {
				prompter.cancel();
			}
			promptTimer.cancel();
		}
	}
		
	/**
	 * Receive a raw request as a byte array from an Alixian via Jebus which we then 
	 * deserialize into a DialogRequest and post onto the street bus. Note that we also
	 * translate (!) the request into American English prior to posting it.
	 * 
	 * @param requestBytes The request
	 */
	void stationReceive(byte[] requestBytes) {
		Dialog dialog;
		DialogRequest dialogRequest;
		Prompter prompter;
		AlixianID fromAlixianID;
		Session session;
		SerialSememe sememe;
		SerialSememe serverLight;
		Set<SerialSememe> sememesCopy;
		
		SharedUtils.checkNotNull(requestBytes);
		LOGGER.log(LOGLEVEL, "StationServer: got station input...");
		try { // TODO make me better :)
			dialog = DialogSerialization.deSerialize(alixiaAlixianID, requestBytes);
		} catch (Exception e) {
			AlixiaUtils.error("StationServer: can't deserialize bytes", e);
			return;
		}
		if (dialog == null) {
			// dialog not sent to us for some reason... what the heck? This is OUR channel...
			AlixiaUtils.error("StationServer: evil not-to-us traffic on our channel!");
			return;
		}
		if (dialog instanceof DialogRequest) {
			dialogRequest = (DialogRequest) dialog;
		} else {
			AlixiaUtils.error("StationServer: cannot yet receive DialogResponses");
			return;
		}
		fromAlixianID = dialogRequest.getFromAlixianID();
		LOGGER.log(LOGLEVEL, "StationServer: dialog request from {0}", fromAlixianID);
		sememesCopy = new HashSet<>(dialogRequest.getRequestActions());
		sememe = SerialSememe.consume("client_startup", sememesCopy);
		LOGGER.log(LOGLEVEL, "StationServer: consumed startup , sememe = {0}", sememe);
		if (sememe != null) {
			// it's a new session
			LOGGER.log(LOGLEVEL, "StationServer: starting new session for {0}", fromAlixianID);
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
				LOGGER.log(LOGLEVEL, "StationServer: closing session for {0}", fromAlixianID);
				removeSession(session);
//				return; // we don't need to pass this on, at least for now
			} else {
				// update the session
				LOGGER.log(LOGLEVEL, "StationServer: updating session for {0}", fromAlixianID);
				session.update();
                // is it really true that these might have changed since the session was created? TODO
//				session.setPersonUUID(dialogRequest.getPersonUUID());
//				session.setLanguage(dialogRequest.getLanguage());
			}
		} else {
			// not startup (no startup sememe), but session doesn't exist in our map, 
            //    so station was up prior to our starting (we presume)
			LOGGER.log(LOGLEVEL, "StationServer: starting (pre-existing) new session for {0}", fromAlixianID);
			session = Session.getSession(fromAlixianID);
			session.setPersonUUID(dialogRequest.getPersonUUID());
			session.setStationUUID(dialogRequest.getStationUUID());
			session.setLanguage(dialogRequest.getLanguage());
            session.setIsQuiet(dialogRequest.isQuiet());
            session.setSessionType(SessionType.SERIALIZED);
			LOGGER.log(LOGLEVEL, "StationServer: before setSession for {0}", fromAlixianID);
			setSession(session);
			LOGGER.log(LOGLEVEL, "StationServer: after setSession for {0}", fromAlixianID);
			// be nice and send them a green server LED
			serverLight = SerialSememe.find("set_green_LED_on");
			stationSend(fromAlixianID, "Connecting to running server....", serverLight);
		}
		dialogRequest.setRequestActions(sememesCopy);
		LOGGER.log(LOGLEVEL, "StationServer: made it past session checks for {0}", fromAlixianID);
		
		if (!noPrompts) {
			// cancel existing prompter for this station, if any...
			for (Iterator<Prompter> iter = prompters.iterator(); iter.hasNext(); ) {
				prompter = iter.next();
				if (prompter.getAlixianID().equals(fromAlixianID)) {
					prompter.cancel();
					iter.remove();
					break;
				}
			}
			// ...and start a new one
			prompter = new Prompter(fromAlixianID, session.getSessionType(), 
                    session.getLanguage(), session.isQuiet(), getStreet());
	        promptTimer.schedule(prompter, PROMPTDELAY, NAGDELAY);
	        prompters.add(prompter);
		}
		speechToText(dialogRequest, session.getLanguage());
        translateRequest(dialogRequest, session.getLanguage());
		LOGGER.log(LOGLEVEL, "StationServer: posting dialog request for {0}", fromAlixianID);
        getStreet().post(dialogRequest);
	}
		
	/**
	 * Receive a text request as a String from an Alixian via Jebus which we then 
	 * post onto the street bus.
	 * 
	 * @param text The request
	 */
	void stationReceiveText(String text) {
		Prompter prompter;
		AlixianID fromAlixianID;
		Session session;
        String[] messageParts;
        DialogRequest dialogRequest;
        String message;
        SerialUUID<SerialStation> stationUUID;
        
		SharedUtils.checkNotNull(text);
		LOGGER.log(LOGLEVEL, "StationServer: got station text input...");
        messageParts = text.split("::", 2); // alixianID::text
        if (messageParts.length < 2) { // with specified limit of 2, above, array s/b at most 2 parts
			// message not sent to us for some reason... what the heck? This is OUR channel...
			AlixiaUtils.error("StationServer: evil not-to-us traffic on our channel!");
			return;
		}
		fromAlixianID = new AlixianID(messageParts[0]);
        message = messageParts[1];
		LOGGER.log(LOGLEVEL, "StationServer: text request from {0}", fromAlixianID);
		if (isOurSession(fromAlixianID)) {
			session = getSession(fromAlixianID);
            // update the session
            LOGGER.log(LOGLEVEL, "StationServer: updating session for {0}", fromAlixianID);
            session.update();
//            session.setLanguage(Language.AMERICAN_ENGLISH);
//            session.setSessionType(SessionType.TEXT);
		} else {
            // We don't have the Alixian in our map, so add it. The message should be the station ID for
            //    the first message.
            //
            // TODO we should tighten this up a bit, particularly if we ever have multiple 
            //    houses listening on the street, such that each should only respond to its own Alixians
			LOGGER.log(LOGLEVEL, "StationServer: starting new session for {0}", fromAlixianID);
            try {
                stationUUID = new SerialUUID<>(message);
            } catch (UUIDException ex) {
                stationSendText(fromAlixianID, "*** Invalid session, try restarting the application");
                return;
            }
			session = Session.getSession(fromAlixianID);
			session.setLanguage(Language.AMERICAN_ENGLISH);
            session.setSessionType(SessionType.TEXT);
            session.setIsQuiet(false);
            session.setStationUUID(stationUUID);
			LOGGER.log(LOGLEVEL, "StationServer: before setSession for {0}", fromAlixianID);
			setSession(session);
			LOGGER.log(LOGLEVEL, "StationServer: after setSession for {0}", fromAlixianID);
			// be nice and send them a green server LED
			stationSendText(fromAlixianID, "Connecting to running server....");
            return;
		}
		LOGGER.log(LOGLEVEL, "StationServer: made it past session checks for {0}", fromAlixianID);
		
		if (!noPrompts) {
    		LOGGER.log(LOGLEVEL, "StationServer: prompter update for {0}", fromAlixianID);
			// cancel existing prompter for this station, if any...
			for (Iterator<Prompter> iter = prompters.iterator(); iter.hasNext(); ) {
				prompter = iter.next();
				if (prompter.getAlixianID().equals(fromAlixianID)) {
					prompter.cancel();
					iter.remove();
					break;
				}
			}
			// ...and start a new one
    		LOGGER.log(LOGLEVEL, "StationServer: creating new prompter for {0}", fromAlixianID);
			prompter = new Prompter(fromAlixianID, session.getSessionType(), 
                    session.getLanguage(), session.isQuiet(), getStreet());
	        promptTimer.schedule(prompter, PROMPTDELAY, NAGDELAY);
	        prompters.add(prompter);
    		LOGGER.log(LOGLEVEL, "StationServer: done with prompter update for {0}", fromAlixianID);
		}
        LOGGER.log(LOGLEVEL, "StationServer: preparing dialogRequest for {0}", fromAlixianID);
        dialogRequest = buildRequest(session);
        dialogRequest.setRequestMessage(message);
		LOGGER.log(LOGLEVEL, "StationServer: posting dialog request for {0}", fromAlixianID);
        if (!dialogRequest.isValid()) {
            throw new AlixiaException("StationServer: created invalid DialogRequest");
        }
        getStreet().post(dialogRequest);
	}
	
    /**
     * Because we just get text from our text console, we have to construct our own
     * DialogRequest to put on the street bus.
     * 
     * @param session The current session
     * @return The (mostly) completed DialogRequest
     */
	private DialogRequest buildRequest(Session session) {
		DialogRequest request;
		
        SharedUtils.checkNotNull(session);
		request = new DialogRequest();
		request.setFromAlixianID(session.getAlixianID());
		request.setToAlixianID(alixiaAlixianID);
		request.setLanguage(session.getLanguage());
        request.setStationUUID(session.getStationUUID());
        request.setSessionType(session.getSessionType());
        request.setIsQuiet(false); // we don't get that information from text consoles...
        request.setRequestActions(Collections.emptySet());
		return request;
	}
	
	/**
	 * Broadcast a message to all stations. Broadcasts are in American English, so any translation
	 * will need to happen at the station level.
	 * 
	 * @param message
	 * @param command
	 */
	private void stationBroadcast(String message, SerialSememe command) {
		DialogResponse response;
		
		SharedUtils.checkNotNull(message);
		SharedUtils.nullsOkay(command);
		response = new DialogResponse();
		response.setMessage(message);
		response.setLanguage(Language.AMERICAN_ENGLISH);
		response.setFromAlixianID(alixiaAlixianID);
		response.setToAlixianID(broadcastID);
		if (command != null) {
			response.setResponseAction(command);
		}
		stationSend(broadcastID, response);
        stationSendText(broadcastID, message);
	}
	
	/**
	 * Send a message and/or a command to a station. We take the raw info and create a Dialog
	 * Response to send to the eponymous overloaded method.
	 * 
	 * @param alixianID The AlixianID of the intended recipient
	 * @param message The message to send
	 * @param command The command to send
	 */
	private void stationSend(AlixianID alixianID, String message, SerialSememe command) {
		DialogResponse response;
		
		SharedUtils.checkNotNull(alixianID);
		SharedUtils.nullsOkay(message);
		SharedUtils.nullsOkay(command);
		response = new DialogResponse();
		response.setMessage(message);
		response.setLanguage(Language.AMERICAN_ENGLISH);
		response.setFromAlixianID(alixiaAlixianID);
		response.setToAlixianID(alixianID);
		if (command != null) {
			response.setResponseAction(command);
		}
		stationSend(alixianID, response);
	}
	/**
	 * Send a DialogResponse to a station, probably but not necessarily in response to an
	 * earlier request from the station.
	 * 
	 * @param alixianID The AlixianID of the intended recipient
	 * @param response The DialogResponse to send
	 */
	private void stationSend(AlixianID alixianID, DialogResponse response) {
		DialogHeader header;
		byte[] responseBytes;
		Session session;
		byte[] key;
        
		SharedUtils.checkNotNull(alixianID);
		SharedUtils.checkNotNull(response);
		// we don't translate broadcasts, for obvious reasons
		if (!alixianID.equals(broadcastID)) {
			session = getSession(alixianID);
			if (session != null) {
				translateResponse(response, session.getLanguage());
			}
		}
		header = new DialogHeader();
		header.setToAlixianID(alixianID);
        LOGGER.log(LOGLEVEL, "StationServer: in stationSend");
		responseBytes = DialogSerialization.serialize(header, response);
        if (responseBytes != null) {
            LOGGER.log(LOGLEVEL, "StationServer:stationSend: bytes not null, going to jebus them");
			try (Jedis jebus = jebusPool.getResource()) {
				key = JebusBible.getBytesKey(JebusKey.FROMCHANNEL, jebusPool);
				jebus.publish(key, responseBytes);
	            LOGGER.log(LOGLEVEL, "StationServer:stationSend: bytes were jebussed");
			}        	
        }
        LOGGER.log(LOGLEVEL, "StationServer:stationSend: done");
	}
    
	/**
	 * Send a message to a text-only station, probably but not necessarily in response to an
	 * earlier request from the station.
	 * 
	 * @param alixianID The AlixianID of the intended recipient
	 * @param message The message to send
	 */
	private void stationSendText(AlixianID alixianID, String message) {
		String key;
        String text;
        
		SharedUtils.checkNotNull(alixianID);
		SharedUtils.checkNotNull(message);
        text = String.format("%s::%s", alixianID, message);
        LOGGER.log(LOGLEVEL, "StationServer: in stationSendText");
			try (Jedis jebus = jebusPool.getResource()) {
				key = JebusBible.getStringKey(JebusKey.FROMTEXTCHANNEL, jebusPool);
				jebus.publish(key, text);
	            LOGGER.log(LOGLEVEL, "StationServer:stationSendText: string was jebussed");
			}        	
        LOGGER.log(LOGLEVEL, "StationServer:stationSendText: done");
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
				AlixiaUtils.error("AlixiaStationServer: unable to transcribe audio", ex);
				return;
			}
	        LOGGER.log(LOGLEVEL, "StationServer: audioText is \"{0}\"", audioText);
			if (audioText.length() > 0) {
				// note that this overwrites any message text that was also sent in the DialogRequest...
				request.setRequestMessage(audioText);
			}
		}
	}
	
	/**
	 * Translate a DialogRequest from another language into American English. We currently provide
	 * a console warning because this is an expensive operation, relatively speaking.
	 * 
	 * NOTE: getting rid of AlixiaGoogleTranslate for the time being, jar issues
	 * 
	 * @param request The request to translate
	 * @param lang The language from which to translate
	 */
	private static void translateRequest(DialogRequest request, Language lang) {
		String translation;
		
		SharedUtils.checkNotNull(request);
		SharedUtils.checkNotNull(lang);
		if ((lang != Language.AMERICAN_ENGLISH) && (lang != Language.BRITISH_ENGLISH)) {
			LOGGER.log(Level.WARNING, "StationServer: translating request from {0} to American English", 
                    lang.getDisplayName());
            translation = translate(lang, Language.AMERICAN_ENGLISH, request.getRequestMessage());
			request.setRequestMessage(translation);
		}
	}

	/**
	 * Translate a DialogResponse into another language from its original language as denoted in the
	 * DialogResponse.
	 * 
	 * NOTE: getting rid of AlixiaGoogleTranslate for the time being, jar issues
	 * 
	 * @param response The response to translate
	 * @param lang The language into which to translate
	 */
	private static void translateResponse(DialogResponse response, Language lang) {
		String messageTranslation;
		String explanationTranslation;
		String expl;
		Language langIn;
		
		SharedUtils.checkNotNull(lang);
		langIn = response.getLanguage();
		if (langIn == null) {
			throw new AlixiaException("StationServer: translateResponse: null language in response");
		}
		response.setLanguage(lang);
		if (langIn != lang) {
			LOGGER.log(Level.WARNING, "StationServer: translating response from {0} to {1}", 
                    new String[]{langIn.getDisplayName(), lang.getDisplayName()});
			// also translates American to British and vice versa... TODO change it maybe
            // TODO change this to make only one translate call with 2 texts if explantion exists;
            //    supposedly you can repeat the q parameter in the POST to translate multiple texts
            messageTranslation = translate(langIn, lang, response.getMessage());
			response.setMessage(messageTranslation);
			expl = response.getExplanation();
			if (expl != null && !expl.isEmpty()) {
				explanationTranslation = translate(langIn, lang, expl);
				response.setExplanation(explanationTranslation);
			}
		}
	}
	
    private static String translate(Language from, Language to, String textToTranslate) {
        String result;
        PurdahKeys purdah;
        String key;
        JsonObject resultData;
        JsonObject data;
        JsonArray translations;
        JsonObject translationObject;
        String translation;
        
        SharedUtils.checkNotNull(from);
        SharedUtils.checkNotNull(to);
        SharedUtils.checkNotNull(textToTranslate);
        purdah = PurdahKeys.getInstance();
        key = purdah.getPurdahKey(PurdahKeys.PurdahKey.GOOGLEXLATEKEY);
        result = ExternalAperture.getGoogleTranslation(from, to, textToTranslate, "text", key);
        LOGGER.log(LOGLEVEL, "Translate result: {0}", result);        
		try (BufferedReader reader = new BufferedReader(new StringReader(result))) {
			try (JsonReader jsonReader = Json.createReader(reader)) {
                resultData = jsonReader.readObject();
                LOGGER.log(LOGLEVEL, "ResultData: {0}", resultData);
                data = resultData.getJsonObject("data");
                LOGGER.log(LOGLEVEL, "Data: {0}", data);
                translations = data.getJsonArray("translations");
                LOGGER.log(LOGLEVEL, "Translations: {0}", translations);
                if (translations.size() != 1) {
                    AlixiaUtils.error("Invalid translations size = " + translations.size());
                    translation = null;
                } else {
                    translationObject = translations.getJsonObject(0);
                    translation = translationObject.getString("translatedText");
                }
            }
		} catch (IOException e) {
            throw new AlixiaException("IO exception in StringReader for some reason", e);
		}
        return translation;
    }
	
	/**
	 * Listen to Alixia's Jebus serialization pub/sub channel.
	 * 
	 * @author hulles
	 *
	 */
	private class JebusListener extends BinaryJedisPubSub {
		
		JebusListener() {
		}
		
        @Override
		public void onMessage(byte[] channel, byte[] msgBytes) {
    		executor.submit(new Runnable() {
    			@Override
    			public void run() {
    	        	stationReceive(msgBytes);
    			}
    		});
        }

		@Override
		public void onSubscribe(byte[] channel, int subscribedChannels) {
        	
        	LOGGER.log(LOGLEVEL, "Subscribed to {0}", channelName(channel));
        }

		@Override
		public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        	
        	LOGGER.log(LOGLEVEL, "Unsubscribed to {0}", channelName(channel));
        }
	}
	
	/**
	 * Create a string from the byte array channel name.
	 * 
	 * @param bytes
	 * @return
	 */
	static String channelName(byte[] bytes) {

		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AlixiaException("StationServer: UnsupportedEncodingException", e);
		}
	}
	
	/**
	 * Listen to Alixia's Jebus text-only pub/sub channel.
	 * 
	 * @author hulles
	 *
	 */
	private class JebusTextListener extends JedisPubSub {
		
		JebusTextListener() {
		}
		
        @Override
		public void onMessage(String channel, String msg) {
    		executor.submit(new Runnable() {
    			@Override
    			public void run() {
    	        	stationReceiveText(msg);
    			}
    		});
        }

		@Override
		public void onSubscribe(String channel, int subscribedChannels) {
        	
        	LOGGER.log(LOGLEVEL, "Subscribed to text channel {0}", channel);
        }

		@Override
		public void onUnsubscribe(String channel, int subscribedChannels) {
        	
        	LOGGER.log(LOGLEVEL, "Unsubscribed to text channel {0}", channel);
        }
	}

}
