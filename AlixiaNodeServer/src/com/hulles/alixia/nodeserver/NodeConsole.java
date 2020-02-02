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
package com.hulles.alixia.nodeserver;

import java.io.Closeable;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.dialog.DialogRequest;
import com.hulles.alixia.api.object.AlixiaClientObject;
import com.hulles.alixia.api.object.MediaObject;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.remote.MediaServer;
import com.hulles.alixia.api.remote.Station;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialPerson;
import com.hulles.alixia.api.shared.SerialProng;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SerialUUID;
import com.hulles.alixia.api.shared.SessionType;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.media.Language;
import com.hulles.alixia.media.MediaFormat;
import com.hulles.alixia.media.audio.TTSPico;
import com.hulles.alixia.nodeserver.pages.NodeWebServer;

/**
 * To use NodeConsoles, a NodeConsole should exist for every client session.
 * It just sits around accumulating messages, explanations and objects addressed 
 * to its client until it is polled and drained by the client. In other words, 
 * it acts as a sink for NodeWebServer and a source for its client.
 * 
 * @author hulles
 */
public final class NodeConsole implements Closeable {
	private final static Logger LOGGER = LoggerFactory.getLogger(NodeConsole.class);
	private final Queue<String> messages;
	private final Queue<String> explanations;
	private final Queue<AlixiaClientObject> clientObjects;
	private final AlixianID alixianID;
    private final static boolean USETTS = true;
	private static final String MEDIA_URL = "/alixia/media?mmd=";
    private final Base64.Encoder encoder;
    private final NodeWebServer webServer;
    private final Station station;
    private final Language language;
	private final SerialUUID<SerialPerson> personUUID = null;
    
	public NodeConsole(NodeWebServer nodeServer) {
		
        SharedUtils.checkNotNull(nodeServer);
        webServer = nodeServer;
        alixianID = AlixianID.createAlixianID();
        LOGGER.debug("Starting new NodeConsole for Alixian {}", alixianID);
        messages = new ConcurrentLinkedQueue<>();
        explanations = new ConcurrentLinkedQueue<>();
		clientObjects = new ConcurrentLinkedQueue<>();
        encoder = Base64.getMimeEncoder();
		station = Station.getInstance();
		station.ensureStationExists();
		language = station.getDefaultLanguage();
	}
	
    public AlixianID getAlixianID() {
    
        return alixianID;
    }
    
	public void sendText(String text) {
		DialogRequest request;
		Set<SerialSememe> sememes;
		
        SharedUtils.checkNotNull(text);
        LOGGER.debug("NodeConsole " + alixianID + ": sending text: \"" + text + "\"");
		request = new DialogRequest();
		request.setFromAlixianID(alixianID);
		request.setToAlixianID(AlixiaConstants.getAlixiaAlixianID());
		request.setPersonUUID(personUUID);
		request.setLanguage(language);
		request.setStationUUID(station.getStationUUID());
        request.setIsQuiet(station.isQuiet());
        request.setSessionType(SessionType.SERIALIZED);
		sememes = Collections.emptySet();
		request.setRequestActions(sememes);
		request.setRequestMessage(text);
        LOGGER.debug("NodeConsole " + alixianID + ": ready to validate request");
        if (!request.isValid()) {
            LOGGER.debug("NodeConsole " + alixianID + ": invalid request");
            throw new AlixiaException("NodeConsole: created invalid DialogRequest in sendText");
        }
        LOGGER.debug("NodeConsole " + alixianID + ": sending valid request");
		webServer.receiveRequestFromConsole(request);
    }

    /**
     * 
     * @param prong
     * @return {messages:[], tts:[], explanations:[], media:[], errors:boolean, sessionid:string}
     */
    public JsonObject getJSON(SerialProng prong) {
        JsonArrayBuilder messageBuilder;
        JsonArrayBuilder explanationBuilder;
        JsonArrayBuilder ttsBuilder;
        JsonArrayBuilder mediaBuilder;
        JsonObjectBuilder mediaObjectBuilder;
        JsonArrayBuilder mediaArrayBuilder;
        JsonObjectBuilder resultBuilder;
        byte[] tts;
        String ttsString;
		Long key;
		String url;
		MediaFormat format;
        Integer length;
		MediaObject mediaObject;
        String mediaType;
        
        SharedUtils.checkNotNull(prong);
        LOGGER.debug("NodeConsole " + alixianID + ": getJSON");
        resultBuilder = Json.createObjectBuilder();        
        messageBuilder = Json.createArrayBuilder();
        if (USETTS) {
            ttsBuilder = Json.createArrayBuilder();
        }
        for (String msg : messages) {
            messageBuilder.add(msg);
            if (USETTS) {
                if ((msg != null) && !msg.isEmpty()) {
                    tts = TTSPico.ttsToBytes(msg);
                    ttsString = encoder.encodeToString(tts);
                    ttsBuilder.add(ttsString);
                }
            }
        }
        messages.clear();
        resultBuilder.add("messages", messageBuilder);
        LOGGER.debug("NodeConsole " + alixianID + ": loaded messages");
        if (USETTS) {
            LOGGER.debug("NodeConsole " + alixianID + ": loaded tts");
            resultBuilder.add("tts", ttsBuilder);
        }
        
        explanationBuilder = Json.createArrayBuilder();
        for (String expl : explanations) {
            explanationBuilder.add(expl);
        }
        explanations.clear();        
        resultBuilder.add("explanations", explanationBuilder);
        LOGGER.debug("NodeConsole " + alixianID + ": loaded explanations");
        
        // media object = {mediatype:string, format:string, [length:integer,] urls:[]}
        // assumes that format and length apply to all media filStarting new es included in this
        //    media object
        mediaBuilder = Json.createArrayBuilder();
        for (AlixiaClientObject obj : clientObjects) {
            LOGGER.debug("NodeConsole " + alixianID + ": attempting media load");
            mediaObjectBuilder = Json.createObjectBuilder();
            switch (obj.getClientObjectType()) {
                case AUDIOBYTES:
                case VIDEOBYTES:
                    mediaObject = (MediaObject) obj;
                    format = mediaObject.getMediaFormat();
                    mediaType = "";
                    if (format.isAudio()) {
                        LOGGER.debug("NodeConsole " + alixianID + ": loaded explanations");
                        mediaType = "audio";
                    } else if (format.isVideo()) {
                        mediaType = "video";
                    }
                    mediaObjectBuilder.add("mediatype", mediaType);
                    mediaObjectBuilder.add("format", format.getMimeType());
                    length = mediaObject.getLengthSeconds();
                    if (length != null) {
                        mediaObjectBuilder.add("length", length);
                    }
                    mediaArrayBuilder = Json.createArrayBuilder();
                    for (byte[] bytes : mediaObject.getMediaBytes()) {
                        key = MediaServer.saveMediaBytes(format, bytes);
                        url = MEDIA_URL + key;
                        mediaArrayBuilder.add(url);
                    }
                    mediaObjectBuilder.add("urls", mediaArrayBuilder);
                    mediaBuilder.add(mediaObjectBuilder);
                    break;
                case IMAGEBYTES:
                    LOGGER.error("NodeConsole MediaServer can't support images yet");
                    break;
                default:
                    throw new AlixiaException("Bad ClientObjectType enum: " + obj.getClientObjectType());
            }
        }
        LOGGER.debug("NodeConsole " + alixianID + ": presumably loaded " + clientObjects.size() + " client objects");
        clientObjects.clear();        
        resultBuilder.add("media", mediaBuilder);
        
        resultBuilder.add("errors", false);
        resultBuilder.add("sessionid", prong.getProngString());
        return resultBuilder.build();
    }
    
	public List<String> getMessages() {
        ImmutableList<String> msgList;
        
        msgList = ImmutableList.copyOf(messages);
        messages.clear();
        return msgList;
	}

	public List<String> getExplanations() {
        ImmutableList<String> explList;
        
        explList = ImmutableList.copyOf(explanations);
        explanations.clear();
        return explList;
	}

	public List<AlixiaClientObject> getClientObjects() {
		ImmutableList<AlixiaClientObject> listCopy;
		
		listCopy = ImmutableList.copyOf(clientObjects);
		clientObjects.clear();
		return listCopy;
	}
	
	public void receiveText(String text) {

        SharedUtils.checkNotNull(text);
        if (text != null && !text.isEmpty()) {
            messages.add(text);
        }
	}
	
//	public boolean receiveCommand(SerialSememe sememe) {
//		
//        SharedUtils.checkNotNull(sememe);
//		return false;
//	}

	public void receiveExplanation(String text) {

        SharedUtils.nullsOkay(text);
        if (text != null && !text.isEmpty()) {
            explanations.add(text);
        }
	}

	public boolean receiveObject(AlixiaClientObject object) {
		
        SharedUtils.nullsOkay(object);
        if (object != null) {
            clientObjects.add(object);
        }
		return true;
	}

	@Override
	public void close() {
	}

//	public void receiveRequest(String text) {
//
//        SharedUtils.checkNotNull(text);
//		receiveText(text);
//	}

}
