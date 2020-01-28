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
package com.hulles.alixia.stationserver;

import static com.hulles.alixia.house.Translator.translateResponse;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.hulles.alixia.api.shared.SessionType;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.house.House;
import com.hulles.alixia.house.Session;
import com.hulles.alixia.house.UrHouse;
import com.hulles.alixia.media.Language;

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
	final static Logger LOGGER = LoggerFactory.getLogger(StationServer.class);
    JebusListener listener = null;
    JebusTextListener textListener = null;
	ExecutorService executor;
	final JebusPool jebusPool;
	private final AlixianID alixiaAlixianID;
	private final AlixianID broadcastID;
	private static boolean alreadyRunning = false;
	
    public StationServer() {
        super();
        
        // we only do the alreadyRunning check for a couple key houses and rooms for a reality check
        if (alreadyRunning) {
        	throw new AlixiaException("StationServer is already running");
        }
        alreadyRunning = true;
        
		jebusPool = JebusHub.getJebusCentral(true);
		LOGGER.info("Station Server Jebus is {}", JebusHub.getCentralServerName());
		alixiaAlixianID = AlixiaConstants.getAlixiaAlixianID();
		broadcastID = AlixiaConstants.getBroadcastAlixianID();
    }
	public StationServer(EventBus street) {
        this();
        
		SharedUtils.checkNotNull(street);
        setStreet(street);
	}
    public StationServer(Boolean noPrompt) {
        this();
        
        SharedUtils.checkNotNull(noPrompt);
        super.setNoPrompts(noPrompt);
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
        LOGGER.debug("StationServer: got response from Alixia");
        alixianID = response.getToAlixianID();
        session = Session.getSession(alixianID);
        if (session == null) {
            throw new AlixiaException("Can't get session");
        }
        if (alixianID.equals(broadcastID)) {
            stationBroadcast(response.getMessage(), response.getResponseAction());
            return;
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
			    LOGGER.debug("StationServer: attempting to shutdown executor");
			    executor.shutdown();
			    executor.awaitTermination(3, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
			    LOGGER.error("StationServer: executor shutdown interrupted");
			} finally {
			    if (!executor.isTerminated()) {
			        LOGGER.error("StationServer: cancelling non-finished tasks");
			    }
			    executor.shutdownNow();
			    LOGGER.debug("StationServer: shutdown finished");
			}
		}
		executor = null;
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
		
		SharedUtils.checkNotNull(requestBytes);
		LOGGER.debug("StationServer: got station input...");
		try { // TODO make me better :)
			dialog = DialogSerialization.deSerialize(alixiaAlixianID, requestBytes);
		} catch (Exception e) {
			LOGGER.error("StationServer: can't deserialize bytes", e);
			return;
		}
		if (dialog == null) {
			// dialog not sent to us for some reason... what the heck? This is OUR channel...
			LOGGER.error("StationServer: evil not-to-us traffic on our channel!");
			return;
		}
		if (dialog instanceof DialogRequest) {
			dialogRequest = (DialogRequest) dialog;
		} else {
			LOGGER.error("StationServer: cannot yet receive DialogResponses");
			return;
		}
        receiveRequestFromClient(dialogRequest);
	}
		
	/**
	 * Receive a text request as a String from an Alixian via Jebus which we then 
	 * post onto the street bus.
	 * 
	 * @param text The request
	 */
	void stationReceiveText(String text) {
		AlixianID fromAlixianID;
        String[] messageParts;
        String message;
        SerialUUID<SerialStation> stationUUID;
		DialogRequest request;
       
		SharedUtils.checkNotNull(text);
		LOGGER.debug("StationServer: got station text input...");
        messageParts = text.split("::", 2); // alixianID::text
        if (messageParts.length < 2) { // with specified limit of 2, above, array s/b at most 2 parts
			// message not sent to us for some reason... what the heck? This is OUR channel...
			LOGGER.error("StationServer: evil not-to-us traffic on our channel!");
			return;
		}
		fromAlixianID = new AlixianID(messageParts[0]);
        message = messageParts[1];
        
		request = new DialogRequest();
		request.setFromAlixianID(fromAlixianID);
		request.setToAlixianID(alixiaAlixianID);
		request.setLanguage(Language.AMERICAN_ENGLISH);
        stationUUID = new SerialUUID<>(); // maybe send this along as well from the text message? TODO
        request.setStationUUID(stationUUID);
        request.setSessionType(SessionType.TEXT);
        request.setIsQuiet(false); // we don't get that information from text consoles...
        request.setRequestActions(Collections.emptySet());
        request.setRequestMessage(message);
        if (!request.isValid()) {
            throw new AlixiaException("StationServer: created invalid DialogRequest");
        }
        receiveRequestFromClient(request);
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
		if (!response.isValid()) {
			throw new AlixiaException("Constructed invalid DialogResponse in stationBroadcast");
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
	@SuppressWarnings("unused")
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
		if (!response.isValid()) {
			throw new AlixiaException("Constructed invalid DialogResponse in stationSend");
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
		if (!response.isValid()) {
			throw new AlixiaException("received invalid DialogResponse in stationSend");
		}
		// we don't translate broadcasts, for obvious reasons
		if (!alixianID.equals(broadcastID)) {
			session = getSession(alixianID);
			if (session != null) {
				translateResponse(response, session.getLanguage());
			}
		}
		header = new DialogHeader();
		header.setToAlixianID(alixianID);
        LOGGER.debug("StationServer: in stationSend");
		responseBytes = DialogSerialization.serialize(header, response);
        if (responseBytes != null) {
            LOGGER.debug("StationServer:stationSend: bytes not null, going to jebus them");
			try (Jedis jebus = jebusPool.getResource()) {
				key = JebusBible.getBytesKey(JebusKey.FROMCHANNEL, jebusPool);
				jebus.publish(key, responseBytes);
	            LOGGER.debug("StationServer:stationSend: bytes were jebussed");
			}        	
        }
        LOGGER.debug("StationServer:stationSend: done");
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
        LOGGER.debug("StationServer: in stationSendText");
			try (Jedis jebus = jebusPool.getResource()) {
				key = JebusBible.getStringKey(JebusKey.FROMTEXTCHANNEL, jebusPool);
				jebus.publish(key, text);
	            LOGGER.debug("StationServer:stationSendText: string was jebussed");
			}        	
        LOGGER.debug("StationServer:stationSendText: done");
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
        	
        	LOGGER.debug("Subscribed to {}", channelName(channel));
        }

		@Override
		public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        	
        	LOGGER.debug("Unsubscribed to {}", channelName(channel));
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
        	
        	LOGGER.debug("Subscribed to text channel {}", channel);
        }

		@Override
		public void onUnsubscribe(String channel, int subscribedChannels) {
        	
        	LOGGER.debug("Unsubscribed to text channel {}", channel);
        }
	}

}
