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
package com.hulles.alixia.api.remote;

import java.io.Closeable;
import java.util.Base64;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.google.common.collect.ImmutableList;
import com.hulles.alixia.api.object.AlixiaClientObject;
import com.hulles.alixia.api.object.MediaObject;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialProng;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.media.MediaFormat;
import com.hulles.alixia.media.audio.TTSPico;

/**
 * To use MiniConsoles, a MiniConsole should exist for every client session.
 * It just sits around accumulating messages, explanations and objects addressed 
 * to its client until it is polled and drained by the client. In other words, 
 * it acts as a sink for AlixiaRemote and a source for its client.
 * 
 * @author hulles
 */
public final class MiniConsole implements AlixiaRemoteDisplay, Closeable {
//	private final static Logger LOGGER = Logger.getLogger("AlixiaNodeServer.MiniConsole");
//	private final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
	private final AlixiaRemote console;
	private final Queue<String> messages;
	private final Queue<String> explanations;
	private final Queue<AlixiaClientObject> clientObjects;
	private final AlixianID alixianID;
    private final static boolean USETTS = true;
	private static final String MEDIA_URL = "/alixia/media?mmd=";
    private final Base64.Encoder encoder;
    
	public MiniConsole() {
		
		console = new AlixiaRemote(this);
		console.startAsync();
		console.awaitRunning();
		
        // setting these to true means they should play in the MiniConsole, vs.
        //    the web client, so leave them false here
		console.setUseTTS(false);
		console.setPlayAudio(false);
		console.setShowImage(false);
		console.setShowText(false);
		console.setPlayVideo(false);
        alixianID = AlixianID.createAlixianID();
        messages = new ConcurrentLinkedQueue<>();
        explanations = new ConcurrentLinkedQueue<>();
		clientObjects = new ConcurrentLinkedQueue<>();
        encoder = Base64.getMimeEncoder();
	}
	
    AlixianID getAlixianID() {
    
        return alixianID;
    }
    
	void sendText(String text) {
		
		if (!console.sendText(text)) {
			receiveText("Unable to communicate with Alixia Central at this time");
		}
	}

    /**
     * 
     * @param prong
     * @return {messages:[], tts:[], explanations:[], media:[], errors:boolean, sessionid:string}
     */
    JsonObject getJSON(SerialProng prong) {
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
        if (USETTS) {
            resultBuilder.add("tts", ttsBuilder);
        }
        
        explanationBuilder = Json.createArrayBuilder();
        for (String expl : explanations) {
            explanationBuilder.add(expl);
        }
        explanations.clear();        
        resultBuilder.add("explanations", explanationBuilder);
        
        // media object = {mediatype:string, format:string, [length:integer,] urls:[]}
        // assumes that format and length apply to all media files included in this
        //    media object
        mediaBuilder = Json.createArrayBuilder();
        for (AlixiaClientObject obj : clientObjects) {
            mediaObjectBuilder = Json.createObjectBuilder();
            switch (obj.getClientObjectType()) {
                case AUDIOBYTES:
                case VIDEOBYTES:
                    mediaObject = (MediaObject) obj;
                    format = mediaObject.getMediaFormat();
                    mediaType = "";
                    if (format.isAudio()) {
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
                    throw new AlixiaException("NodeMediaServer can't support images yet");
                default:
                    throw new AlixiaException("Bad ClientObjectType enum: " + obj.getClientObjectType());
            }
        }
        clientObjects.clear();        
        resultBuilder.add("media", mediaBuilder);
        
        resultBuilder.add("errors", false);
        resultBuilder.add("sessionid", prong.getProngString());
        return resultBuilder.build();
    }
    
	List<String> getMessages() {
        ImmutableList<String> msgList;
        
        msgList = ImmutableList.copyOf(messages);
        messages.clear();
        return msgList;
	}

	List<String> getExplanations() {
        ImmutableList<String> explList;
        
        explList = ImmutableList.copyOf(explanations);
        explanations.clear();
        return explList;
	}

	List<AlixiaClientObject> getClientObjects() {
		ImmutableList<AlixiaClientObject> listCopy;
		
		listCopy = ImmutableList.copyOf(clientObjects);
		clientObjects.clear();
		return listCopy;
	}
	
	@Override
	public void receiveText(String text) {

        messages.add(text);
	}
	
	@Override
	public boolean receiveCommand(SerialSememe sememe) {
		
		return false;
	}

	@Override
	public void receiveExplanation(String text) {

        explanations.add(text);
	}

	@Override
	public boolean receiveObject(AlixiaClientObject object) {
		
		clientObjects.add(object);
		return true;
	}

	@Override
	public void close() {
		
		console.stopAsync();
		console.awaitTerminated();
	}

	@Override
	public void receiveRequest(String text) {

		receiveText(text);
	}

    @Override
    public void textWindowIsClosing() {
        
        throw new UnsupportedOperationException("textWindowIsClosing not supported.");
    }
}
