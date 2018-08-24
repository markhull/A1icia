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
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.dialog.Dialog;
import com.hulles.a1icia.api.dialog.DialogHeader;
import com.hulles.a1icia.api.dialog.DialogRequest;
import com.hulles.a1icia.api.dialog.DialogResponse;
import com.hulles.a1icia.api.dialog.DialogSerialization;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.jebus.JebusBible;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.jebus.JebusPool;
import com.hulles.a1icia.media.Language;
import com.hulles.a1icia.tools.A1iciaUtils;
import com.hulles.a1icia.tools.ExternalAperture;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

/**
 * A1iciaStationServer is responsible for communication with the outside world, notably with
 * A1iciaStations.
 * <p>
 * ALICIA PUBLISHES ON "a1icia:channel:from" and SUBSCRIBES TO "a1icia:channel:to".
 * 
 * @author hulles
 *
 */
public final class A1iciaStationServer extends UrHouse {
	final static Logger LOGGER = Logger.getLogger("A1icia.A1iciaStationServer");
	final static Level LOGLEVEL1 = A1iciaConstants.getA1iciaLogLevel();
	JebusListener listener = null;
	ExecutorService executor;
	Timer promptTimer;
	private List<Prompter> prompters;
	private final static int PROMPTDELAY = 45 * 1000;
	private final static int NAGDELAY = 60 * 1000;
	final JebusPool jebusPool;
	private final A1icianID a1iciaA1icianID;
	private final A1icianID broadcastID;
	private final Boolean noPrompts;
	
	public A1iciaStationServer(Boolean noPrompts) {
		super();
	
		SharedUtils.checkNotNull(noPrompts);
		this.noPrompts = noPrompts;
		jebusPool = JebusHub.getJebusCentral(true);
		System.out.println("Station Server Jebus is " + JebusHub.getCentralServerName());
		a1iciaA1icianID = A1iciaConstants.getA1iciaA1icianID();
		broadcastID = A1iciaConstants.getBroadcastA1icianID();
	}

	/**
	 * Return which house we are.
	 * 
	 *
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
	 */
	@Override
	protected void newDialogRequest(DialogRequest request) {
		throw new A1iciaException("Request not implemented in " + getThisHouse());
	}

	/**
	 * We got a response from A1icia (presumably) so we send it along to its ultimate
	 * destination A1ician.
	 * 
	 */
	@Override
	protected void newDialogResponse(DialogResponse response) {
		A1icianID a1icianID;
		
		SharedUtils.checkNotNull(response);
        LOGGER.log(LOGLEVEL1, "StationServer: got response from A1icia");
        a1icianID = response.getToA1icianID();
        stationSend(a1icianID, response);
	}

	/**
	 * We use houseStartup to create our executor pool and send a startup broadcast announcement.
	 * 
	 */
	@Override
	protected void houseStartup() {
		SerialSememe openSememe;
		
		executor = Executors.newCachedThreadPool();
		if (!noPrompts) {
			promptTimer = new Timer();
			prompters = new ArrayList<>();
		}
		openSememe = SerialSememe.find("central_startup");
		stationBroadcast("Alicia Central starting up....", openSememe);
	}

	/**
	 * Send a shutdown announcement and close down the executor pool.
	 * 
	 */
	@Override
	protected void houseShutdown() {
		SerialSememe closeSememe;
		
		closeSememe = SerialSememe.find("central_shutdown");
		stationBroadcast("Alicia Central shutting down....", closeSememe);
		if (executor != null) {
			try {
			    System.out.println("StationServer: attempting to shutdown executor");
			    executor.shutdown();
			    executor.awaitTermination(3, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
			    System.err.println("StationServer: executor shutdown interrupted");
			} finally {
			    if (!executor.isTerminated()) {
			        System.err.println("StationServer: cancelling non-finished tasks");
			    }
			    executor.shutdownNow();
			    System.out.println("StationServer: shutdown finished");
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
	 * Receive a raw request as a byte array from an A1ician via Jebus which we then 
	 * deserialize into a DialogRequest and post onto the street bus. Note that we also
	 * translate (!) the request into American English prior to posting it.
	 * 
	 * @param requestBytes The request
	 */
	void stationReceive(byte[] requestBytes) {
		Dialog dialog = null;
		DialogRequest dialogRequest;
		Prompter prompter;
		A1icianID fromA1icianID;
		Session session;
		SerialSememe sememe;
		SerialSememe serverLight;
		Set<SerialSememe> sememesCopy;
		
		SharedUtils.checkNotNull(requestBytes);
		LOGGER.log(LOGLEVEL1, "StationServer: got station input...");
		try { // TODO make me better :)
			dialog = DialogSerialization.deSerialize(a1iciaA1icianID, requestBytes);
		} catch (Exception e) {
			A1iciaUtils.error("StationServer: can't deserialize bytes", e);
			return;
		}
		if (dialog == null) {
			// dialog not sent to us for some reason... what the heck? This is OUR channel...
			A1iciaUtils.error("StationServer: evil not-to-us traffic on our channel!");
			return;
		}
		if (dialog instanceof DialogRequest) {
			dialogRequest = (DialogRequest) dialog;
		} else {
			A1iciaUtils.error("StationServer: cannot yet receive DialogResponses");
			return;
		}
		fromA1icianID = dialogRequest.getFromA1icianID();
		LOGGER.log(LOGLEVEL1, "StationServer: dialog request from " + fromA1icianID);
		sememesCopy = new HashSet<>(dialogRequest.getRequestActions());
		sememe = SerialSememe.consume("client_startup", sememesCopy);
		LOGGER.log(LOGLEVEL1, "StationServer: consumed startup , sememe = " + sememe);
		if (sememe != null) {
			// it's a new session
			LOGGER.log(LOGLEVEL1, "StationServer: starting new session for " + fromA1icianID);
			session = Session.getSession(fromA1icianID);
			session.setPersonUUID(dialogRequest.getPersonUUID());
			session.setStationUUID(dialogRequest.getStationUUID());
			session.setLanguage(dialogRequest.getLanguage());
			setSession(session);
			return; // we don't need to pass this along, at least for now
		} else if (isOurSession(fromA1icianID)) {
			session = getSession(fromA1icianID);
			sememe = SerialSememe.consume("client_shutdown", sememesCopy);
			if (sememe != null) {
				// close the session
				LOGGER.log(LOGLEVEL1, "StationServer: closing session for " + fromA1icianID);
				removeSession(session);
//				return; // we don't need to pass this on, at least for now
			} else {
				// update the session
				LOGGER.log(LOGLEVEL1, "StationServer: updating session for " + fromA1icianID);
				session.update();
				session.setPersonUUID(dialogRequest.getPersonUUID());
				session.setStationUUID(dialogRequest.getStationUUID());
				session.setLanguage(dialogRequest.getLanguage());
			}
		} else {
			// not startup, but session doesn't exist in our map, so station was up
			//    prior to our starting (we presume)
			LOGGER.log(LOGLEVEL1, "StationServer: starting (pre-existing) new session for " + 
					fromA1icianID);
			session = Session.getSession(fromA1icianID);
			session.setPersonUUID(dialogRequest.getPersonUUID());
			session.setStationUUID(dialogRequest.getStationUUID());
			session.setLanguage(dialogRequest.getLanguage());
			LOGGER.log(LOGLEVEL1, "StationServer: before setSession for " + fromA1icianID);
			setSession(session);
			LOGGER.log(LOGLEVEL1, "StationServer: after setSession for " + fromA1icianID);
			// be nice and send them a green server LED
			serverLight = SerialSememe.find("set_green_LED_on");
			stationSend(fromA1icianID, "Connecting to running server....", serverLight);
		}
		dialogRequest.setRequestActions(sememesCopy);
		LOGGER.log(LOGLEVEL1, "StationServer: made it past session checks for " + fromA1icianID);
		
		if (!noPrompts) {
			// cancel existing prompter for this station, if any...
			prompter = null;
			for (Iterator<Prompter> iter = prompters.iterator(); iter.hasNext(); ) {
				prompter = iter.next();
				if (prompter.getA1icianID().equals(fromA1icianID)) {
					prompter.cancel();
					iter.remove();
					break;
				}
			}
			// ...and start a new one
			prompter = new Prompter(fromA1icianID, session.getLanguage(), getStreet());
	        promptTimer.schedule(prompter, PROMPTDELAY, NAGDELAY);
	        prompters.add(prompter);
		}
		speechToText(dialogRequest, session.getLanguage());
        translateRequest(dialogRequest, session.getLanguage());
		LOGGER.log(LOGLEVEL1, "StationServer: posting dialog request for " + fromA1icianID);
        getStreet().post(dialogRequest);
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
		response.setFromA1icianID(a1iciaA1icianID);
		response.setToA1icianID(broadcastID);
		if (command != null) {
			response.setResponseAction(command);
		}
		stationSend(broadcastID, response);
	}
	
	/**
	 * Send a message and/or a command to a station. We take the raw info and create a Dialog
	 * Response to send to the eponymous overloaded method.
	 * 
	 * @param a1icianID The A1icianID of the intended recipient
	 * @param message The message to send
	 * @param command The command to send
	 */
	private void stationSend(A1icianID a1icianID, String message, SerialSememe command) {
		DialogResponse response;
		
		SharedUtils.checkNotNull(a1icianID);
		SharedUtils.nullsOkay(message);
		SharedUtils.nullsOkay(command);
		response = new DialogResponse();
		response.setMessage(message);
		response.setLanguage(Language.AMERICAN_ENGLISH);
		response.setFromA1icianID(a1iciaA1icianID);
		response.setToA1icianID(a1icianID);
		if (command != null) {
			response.setResponseAction(command);
		}
		stationSend(a1icianID, response);
	}
	/**
	 * Send a DialogResponse to a station, probably but not necessarily in response to an
	 * earlier request from the station.
	 * 
	 * @param a1icianID The A1icianID of the intended recipient
	 * @param response The DialogResponse to send
	 */
	private void stationSend(A1icianID a1icianID, DialogResponse response) {
		DialogHeader header;
		byte[] responseBytes = null;
		Session session;
		
		SharedUtils.checkNotNull(a1icianID);
		SharedUtils.checkNotNull(response);
		// we don't translate broadcasts, for obvious reasons
		if (!a1icianID.equals(broadcastID)) {
			session = getSession(a1icianID);
			if (session != null) {
				translateResponse(response, session.getLanguage());
			}
		}
		header = new DialogHeader();
		header.setToA1icianID(a1icianID);
        LOGGER.log(LOGLEVEL1, "StationServer: in stationSend");
		responseBytes = DialogSerialization.serialize(header, response);
        if (responseBytes != null) {
            LOGGER.log(LOGLEVEL1, "StationServer:stationSend: bytes not null, going to jebus them");
			try (Jedis jebus = jebusPool.getResource()) {
				jebus.publish(JebusBible.getA1iciaFromChannelBytes(jebusPool), responseBytes);
	            LOGGER.log(LOGLEVEL1, "StationServer:stationSend: bytes were jebussed");
			}        	
        }
        LOGGER.log(LOGLEVEL1, "StationServer:stationSend: done");
	}
	
	/**
	 * Convert an audio file included in the DialogRequest to text.
	 * 
	 * @param request The request containing the audio file
	 * @param lang The language in which the speech is recorded
	 */
	private static void speechToText(DialogRequest request, Language lang) {
		byte[] audioBytes;
		String audioText = null;
		
		SharedUtils.checkNotNull(request);
		SharedUtils.checkNotNull(lang);
		audioBytes = request.getRequestAudio();
		if (audioBytes != null) {
			try {
				audioText = ExternalAperture.queryDeepSpeech(audioBytes);
			} catch (Exception ex) {
				A1iciaUtils.error("A1iciaStationServer: unable to transcribe audio", ex);
				return;
			}
	        LOGGER.log(Level.INFO, "StationServer: audioText is \"" + audioText + "\"");
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
	 * NOTE: getting rid of A1iciaGoogleTranslate for the time being, jar issues
	 * 
	 * @param request The request to translate
	 * @param lang The language from which to translate
	 */
	private static void translateRequest(DialogRequest request, Language lang) {
//		String translation;
		
		SharedUtils.checkNotNull(request);
		SharedUtils.checkNotNull(lang);
		if ((lang != Language.AMERICAN_ENGLISH) && (lang != Language.BRITISH_ENGLISH)) {
			LOGGER.log(Level.WARNING, "StationServer: translating request from " + lang.getDisplayName() + 
					" to American English");
//			translation = A1iciaGoogleTranslator.translate(lang, Language.AMERICAN_ENGLISH, 
//					request.getRequestMessage());
//			request.setRequestMessage(translation);
		}
	}

	/**
	 * Translate a DialogResponse into another language from its original language as denoted in the
	 * DialogResponse.
	 * 
	 * NOTE: getting rid of A1iciaGoogleTranslate for the time being, jar issues
	 * 
	 * @param response The response to translate
	 * @param lang The language into which to translate
	 */
	private static void translateResponse(DialogResponse response, Language lang) {
//		String messageTranslation;
//		String explanationTranslation;
//		String expl;
		Language langIn;
		
		SharedUtils.checkNotNull(lang);
		langIn = response.getLanguage();
		if (langIn == null) {
			throw new A1iciaException("StationServer: translateResponse: null language in response");
		}
		response.setLanguage(lang);
		if (langIn != lang) {
			LOGGER.log(Level.WARNING, "StationServer: translating response from " + langIn.getDisplayName() + 
					" to " + lang.getDisplayName());
			// also translates American to British and vice versa... TODO change it maybe
//			messageTranslation = A1iciaGoogleTranslator.translate(langIn, lang, 
//					response.getMessage());
//			response.setMessage(messageTranslation);
//			expl = response.getExplanation();
//			if (expl != null && !expl.isEmpty()) {
//				explanationTranslation = A1iciaGoogleTranslator.translate(langIn, lang, 
//						expl);
//				response.setExplanation(explanationTranslation);
//			}
		}
	}
	
	/**
	 * Start our Jebus pub/sub listener.
	 * 
	 */
	@Override
	protected void run() {
		
		try (Jedis jebus = jebusPool.getResource()) {
			listener = new JebusListener();
			// the following line blocks while waiting for responses...
			jebus.subscribe(listener, JebusBible.getA1iciaToChannelBytes(jebusPool));
		}
	}
	
	/**
	 * Initiate a shutdown of the service.
	 * 
	 */
	@Override
	protected void triggerShutdown() {
				
		if (listener == null) {
			return;
		}
		listener.unsubscribe();
		listener = null;
	}
	
	/**
	 * Listen to A1icia's Jebus pub/sub channel.
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
        	
        	LOGGER.log(LOGLEVEL1, "Subscribed to " + channelName(channel));
        }

		@Override
		public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        	
        	LOGGER.log(LOGLEVEL1, "Unsubscribed to " + channelName(channel));
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
			throw new A1iciaException("StationServer: UnsupportedEncodingException", e);
		}
	}

}
