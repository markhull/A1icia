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
package com.hulles.a1icia.api.remote;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.dialog.Dialog;
import com.hulles.a1icia.api.dialog.DialogHeader;
import com.hulles.a1icia.api.dialog.DialogRequest;
import com.hulles.a1icia.api.dialog.DialogResponse;
import com.hulles.a1icia.api.dialog.DialogSerialization;
import com.hulles.a1icia.api.jebus.JebusApiBible;
import com.hulles.a1icia.api.jebus.JebusApiHub;
import com.hulles.a1icia.api.jebus.JebusPool;
import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.object.A1iciaClientObject.ClientObjectType;
import com.hulles.a1icia.api.object.AudioObject;
import com.hulles.a1icia.api.object.ChangeLanguageObject;
import com.hulles.a1icia.api.object.LoginObject;
import com.hulles.a1icia.api.object.LoginResponseObject;
import com.hulles.a1icia.api.object.MediaObject;
import com.hulles.a1icia.api.shared.A1iciaAPIException;
import com.hulles.a1icia.api.shared.SerialPerson;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SerialUUID;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.media.Language;
import com.hulles.a1icia.media.MediaFormat;
import com.hulles.a1icia.media.MediaUtils;
import com.hulles.a1icia.media.audio.AudioBytePlayer;
import com.hulles.a1icia.media.audio.SerialAudioFormat;
import com.hulles.a1icia.media.audio.TTSPico;
import com.hulles.a1icia.media.image.SwingImageDisplayer;
import com.hulles.a1icia.media.text.SwingTextDisplayer;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

/**
 * A1iciaRemote implements a full-duplex console scheme with transmitter and receiver operating
 * asynchronously.
 * 
 * @author hulles
 *
 */
public final class A1iciaRemote extends AbstractExecutionThreadService {
	final static Logger LOGGER = Logger.getLogger("A1iciaApi.A1iciaRemote");
	final static Level LOGLEVEL = LOGGER.getParent().getLevel();
//	final static Level LOGLEVEL = Level.INFO;
	private final static String A1ICIA_PREFIX = "A1ICIA: ";
	JebusListener listener = null;
	ExecutorService executor;
	final JebusPool jebusCentral;
	final JebusPool jebusLocal;
	private final A1iciaRemoteDisplay display;
	private A1icianID a1icianID;
	private boolean useTTS = false;
	private boolean playAudio = false;
	private boolean showImage = false;
	private boolean playVideo = false;
	private boolean showText = false;
	private final String jebusHost;
	private final Integer jebusPort;
	private final Station station;
	private SwingTextDisplayer textDisplayer = null;
	private SwingImageDisplayer imageDisplayer = null;
	private SimpleAttributeSet meAttrs;
	private SimpleAttributeSet a1iciaAttrs;
	private SerialUUID<SerialPerson> personUUID = null;
	private Language language = null;
	private String userName;
	private volatile boolean serverUp;
	
	public A1iciaRemote(A1iciaRemoteDisplay display) {
		this(null, null, display);
	}
    public A1iciaRemote(String host, Integer port, A1iciaRemoteDisplay display) {
    	
    	SharedUtils.checkNotNull(display);
    	SharedUtils.nullsOkay(host);
    	SharedUtils.nullsOkay(port);
		LOGGER.log(LOGLEVEL, "A1iciaRemote: Starting up");
		station = Station.getInstance();
		station.ensureStationExists();
		if (host == null) {
			jebusHost = station.getCentralHost();
		} else {
			jebusHost = host;
		}
		if (port == null) {
			jebusPort = station.getCentralPort();
		} else {
			jebusPort = port;
		}
		language = station.getDefaultLanguage();
		this.display = display;
		executor = Executors.newCachedThreadPool();
	    jebusCentral = JebusApiHub.getJebusCentral(jebusHost, jebusPort);
		if (!reachableHost(jebusCentral)) {
			throw new A1iciaAPIException("Can't reach JebusCentral host = " + jebusHost + ", port = " + jebusPort);
		}
		jebusLocal = JebusApiHub.getJebusLocal();
		if (!reachableHost(jebusLocal)) {
			throw new A1iciaAPIException("Can't reach JebusLocal host");
		}
    }
	
	@Override
	protected void startUp() {
    	SerialSememe sememe;
    	
		a1icianID = A1icianID.createA1icianID();
		LOGGER.log(LOGLEVEL, "A1iciaRemote: Started");
		serverUp = true;
		sememe = new SerialSememe();
		sememe.setName("client_startup");
		sendCommand(sememe, null);
    }
	
	private void startTextDisplayer() {
    	AttributeSet defaultAttrs;
    	
		textDisplayer = new SwingTextDisplayer(null, "Console Log");
		defaultAttrs = textDisplayer.getDefaultAttributeSet();
		meAttrs = new SimpleAttributeSet(defaultAttrs);
		StyleConstants.setForeground(meAttrs, Color.BLUE);
		StyleConstants.setBold(meAttrs, true);
		a1iciaAttrs = new SimpleAttributeSet(defaultAttrs);
		StyleConstants.setForeground(a1iciaAttrs, Color.RED);
		StyleConstants.setBold(a1iciaAttrs, true);
		textDisplayer.startup();
	}
	
	private void startImageDisplayer() {
		
		imageDisplayer = new SwingImageDisplayer();
	}
	
	@Override
	protected void shutDown() {
		SerialSememe sememe;
		
		sememe = new SerialSememe();
		sememe.setName("client_shutdown");
		sendCommand(sememe, null);
		if (executor != null) {
			try {
				LOGGER.log(LOGLEVEL, "attempting to shutdown executor");
			    executor.shutdown();
			    executor.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
				LOGGER.log(LOGLEVEL, "tasks interrupted");
			}
			finally {
			    if (!executor.isTerminated()) {
			    	LOGGER.log(LOGLEVEL, "cancelling non-finished tasks");
			    }
			    executor.shutdownNow();
			    LOGGER.log(LOGLEVEL, "shutdown finished");
			}
		}
		executor = null;
	}
    
	public static boolean reachableHost(JebusPool pool) {
    	String result;
    	
    	SharedUtils.checkNotNull(pool);
		try (Jedis jebus = pool.getResource()) {
			result = jebus.ping();
		} catch (Exception ex) {
			return false;
		}
    	return result.equals("PONG");
    }
    
    public boolean reachableHost() {
    	
    	return reachableHost(jebusCentral);
    }
    
    public boolean serverUp() {
    	
    	return serverUp;
    }
    
    public boolean useTTS() {
    	
    	return useTTS;
    }
    
    public void setUseTTS(boolean useTTS) {
    	
    	SharedUtils.checkNotNull(useTTS);
    	this.useTTS = useTTS;
    }	
	
	public boolean playAudio() {
		
		return playAudio;
	}

	public void setPlayAudio(boolean playAudio) {
		
		SharedUtils.checkNotNull(playAudio);
		this.playAudio = playAudio;
	}
	
	public boolean playVideo() {
		
		return playVideo;
	}

	public void setPlayVideo(boolean playVideo) {
		
		SharedUtils.checkNotNull(playVideo);
		this.playVideo = playVideo;
	}

	public boolean showImage() {
		
		return showImage;
	}
	
	public void setShowImage(boolean displayImage) {
		
		SharedUtils.checkNotNull(displayImage);
		this.showImage = displayImage;
		if (displayImage) {
			if (imageDisplayer == null) {
				startImageDisplayer();
			} 
		} else {
			if (imageDisplayer != null) {
				imageDisplayer = null;
			}
		}
	}
	
	public boolean showText() {
		
		return showText;
	}
	
	public void setShowText(boolean displayText) {
		
		SharedUtils.checkNotNull(displayText);
		this.showText = displayText;
		if (showText) {
			if (textDisplayer == null) {
				startTextDisplayer();
			} 
		} else {
			if (textDisplayer != null) {
				textDisplayer.shutdown();
				textDisplayer = null;
			}
		}
	}
	
	public A1icianID getA1icianID() {
		
		return a1icianID;
	}
	
	public String getUserName() {
	
		return userName;
	}
	
	protected Station getStation() {
	
		return station;
	}
	
	public Language getCurrentLanguage() {
	
		return language;
	}
	
	@Override
	protected void run() {
		
		try (Jedis jebus = jebusCentral.getResource()) {
			listener = new JebusListener();
			// the following line blocks while waiting for responses...
			jebus.subscribe(listener, JebusApiBible.getA1iciaFromChannelBytes(jebusCentral));
		}
	}
	
	@Override
	protected void triggerShutdown() {
				
		if (listener == null) {
			return;
		}
		listener.unsubscribe();
		listener = null;
	}
    
	public boolean sendText(String message) {
		DialogRequest request;
		Set<SerialSememe> sememes;
		
		SharedUtils.checkNotNull(message);
		if (!serverUp) {
			return false;
		}
		request = buildRequest();
		sememes = Collections.emptySet();
		request.setRequestActions(sememes);
		request.setRequestMessage(message);
		if (showText) {
			if (userName == null) {
				textDisplayer.appendText("ME: ", meAttrs);
			} else {
				textDisplayer.appendText("ME (", meAttrs);
				textDisplayer.appendText(userName, meAttrs);
				textDisplayer.appendText("): ", meAttrs);
			}
			textDisplayer.appendText(message + "\n");
		}
		return sendRequest(request);
	}
    
	public boolean sendAudio(byte[] audioBytes) {
		DialogRequest request;
		Set<SerialSememe> sememes;
		
		SharedUtils.checkNotNull(audioBytes);
		if (!serverUp) {
			return false;
		}
		request = buildRequest();
		sememes = Collections.emptySet();
		request.setRequestActions(sememes);
		request.setRequestAudio(audioBytes);
		request.setRequestMessage("AUDIO FILE");
		return sendRequest(request);
	}
    
	/** Send a command and an optional message to the host. We currently 
	 * support just one action per request.
	 * 
	 * @param sememe The sememe to send
	 */
	public boolean sendCommand(SerialSememe sememe, String message) {
		DialogRequest request;
		Set<SerialSememe> sememes;
		
		SharedUtils.checkNotNull(sememe);
		SharedUtils.nullsOkay(message);
		if (!serverUp) {
			return false;
		}
		request = buildRequest();
		sememes = Collections.singleton(sememe);
		LOGGER.log(LOGLEVEL, "A1iciaRemote: Sending command");
		request.setRequestActions(sememes);
		request.setRequestMessage(message);
		return sendRequest(request);
	}
    
	public boolean sendLogin(LoginObject object) {
		DialogRequest request;
		Set<SerialSememe> sememes;
		SerialSememe sememe;
		
		SharedUtils.checkNotNull(object);
		if (!serverUp) {
			return false;
		}
		request = buildRequest();
		sememe = new SerialSememe();
		sememe.setName("login");
		sememes = Collections.singleton(sememe);
		LOGGER.log(LOGLEVEL, "A1iciaRemote: Sending login");
		request.setRequestActions(sememes);
		request.setClientObject(object);
		return sendRequest(request);
	}
	
	private DialogRequest buildRequest() {
		DialogRequest request;
		
		request = new DialogRequest();
		request.setFromA1icianID(a1icianID);
		request.setToA1icianID(A1iciaConstants.getA1iciaA1icianID());
		request.setPersonUUID(personUUID);
		request.setLanguage(language);
		request.setStationUUID(station.getStationUUID());
		return request;
	}
	
	private boolean sendRequest(DialogRequest request) {
		DialogHeader header;
		byte[] dialogBytes = null;
		
		SharedUtils.checkNotNull(request);
		if (!serverUp) {
			return false;
		}
		header = new DialogHeader();
		header.setToA1icianID(A1iciaConstants.getA1iciaA1icianID());
		LOGGER.log(LOGLEVEL, "A1iciaRemote: serializing request");
		dialogBytes = DialogSerialization.serialize(header, request);
		if (dialogBytes == null) {
			throw new A1iciaAPIException("Couldn't create dialog to send");
		}
		LOGGER.log(LOGLEVEL, "A1iciaRemote: sending request");
		try (Jedis jebus = jebusCentral.getResource()) {
			jebus.publish(JebusApiBible.getA1iciaToChannelBytes(jebusCentral), dialogBytes);
		}
		return true;
	}
	
	void receiveBytes(byte[] responseBytes) {
		String text;
		String expl;
		Dialog dialog = null;
		DialogResponse dialogResponse;
		DialogRequest dialogRequest;
		SerialSememe sememe;
		Set<SerialSememe> sememes;
		A1iciaClientObject clientObject;
		Language lang;
		
		SharedUtils.checkNotNull(responseBytes);
		LOGGER.log(LOGLEVEL, "A1iciaRemote: in receiveBytes");
		try { // TODO make me better :)
			dialog = DialogSerialization.deSerialize(a1icianID, responseBytes);
		} catch (Exception e1) {
            throw new A1iciaAPIException("Can't deserialize response", e1);
		}
		if (dialog == null) {
			// dialog not sent to us...
			LOGGER.log(LOGLEVEL, "A1iciaRemote: got input, but not sent to us");
			return;
		}
		if (dialog instanceof DialogResponse) {
			dialogResponse = (DialogResponse) dialog;
			
			text = dialogResponse.getMessage();
			lang = dialogResponse.getLanguage();
			processInputMessage(text, lang);
			
			expl = dialogResponse.getExplanation();
			processInputExplanation(expl, text);
			
			sememe = dialogResponse.getResponseAction();
			processInputCommand(sememe);
			
			clientObject = dialogResponse.getClientObject();
			processInputClientObject(clientObject);
		} else {
			dialogRequest = (DialogRequest) dialog;
			
			text = dialogRequest.getRequestMessage();
			lang = dialogRequest.getLanguage();
			processInputRequest(text, lang);
			
			sememes = dialogRequest.getRequestActions();
			for (SerialSememe ss : sememes) {
				processInputCommand(ss);
			}
			
			clientObject = dialogRequest.getClientObject();
			processInputClientObject(clientObject);
		}
	}
	
	private void processInputMessage(String text, Language lang) {
		
		SharedUtils.checkNotNull(text);
		SharedUtils.checkNotNull(lang);
		if (text != null) {
			LOGGER.log(LOGLEVEL, "A1iciaRemote: message text is {0}", text);
			receiveText(text, lang);
			if (showText) {
				if (text.isEmpty()) {
					text = "...";
				}
				textDisplayer.appendText(A1ICIA_PREFIX, a1iciaAttrs);
				textDisplayer.appendText(text + "\n");
			}
		}
	}
	
	private void processInputRequest(String text, Language lang) {
		
		SharedUtils.checkNotNull(text);
		SharedUtils.checkNotNull(lang);
		if (text != null) {
			LOGGER.log(LOGLEVEL, "A1iciaRemote: request text is {0}", text);
			receiveRequest(text, lang);
			if (showText) {
				if (text.isEmpty()) {
					text = "...";
				}
				textDisplayer.appendText(A1ICIA_PREFIX, a1iciaAttrs);
				textDisplayer.appendText(text + "\n");
			}
		}
	}
	
	private void processInputExplanation(String expl, String text) {
		
		SharedUtils.nullsOkay(expl);
		SharedUtils.checkNotNull(text);
		if (expl != null && !expl.isEmpty() && !expl.equals(text)) {
			LOGGER.log(LOGLEVEL, "A1iciaRemote: explanation is {0}", expl);
			if (showText) {
				textDisplayer.appendText(A1ICIA_PREFIX, a1iciaAttrs);
				textDisplayer.appendText(expl + "\n");
			}
			display.receiveExplanation(expl);
		}
	}
	
	private void processInputCommand(SerialSememe sememe) {
		
		SharedUtils.nullsOkay(sememe);
		if (sememe != null) {
			LOGGER.log(LOGLEVEL, "A1iciaRemote: sememe is {0}", sememe.getName());
			receiveCommand(sememe);
		}
	}
	
	private void processInputClientObject(A1iciaClientObject clientObject) {
		AudioObject audioObject;
		MediaObject mediaObject;
		LoginResponseObject loginObject;
		ClientObjectType objectType;
		ChangeLanguageObject changeLanguage;
		
		SharedUtils.nullsOkay(clientObject);
		if (clientObject == null) {
			return;
		}
		if (display.receiveObject(clientObject)) {
			// A1iciaRemoteDisplay handles the object, we don't need to deal with it further;
			// use case is web server, we don't need to play audio on the server itself
			return;
		}		
		// if A1iciaRemoteDisplay doesn't handle it, well, we can, let's roll up our sleeves...
		objectType = clientObject.getClientObjectType();
		LOGGER.log(LOGLEVEL, "A1iciaRemote: processing object");
		switch (objectType) {
			case LOGIN_RESPONSE:
				LOGGER.log(LOGLEVEL, "A1iciaRemote: LOGIN_RESPONSE");
				loginObject = (LoginResponseObject) clientObject;
				this.personUUID = loginObject.getPersonUUID();
				this.userName = loginObject.getUserName();
				break;
			case CHANGE_LANGUAGE:
				LOGGER.log(LOGLEVEL, "A1iciaRemote: CHANGE_LANGUAGE");
				changeLanguage = (ChangeLanguageObject) clientObject;
				this.language = changeLanguage.getNewLanguage();
				// we change the default language for the station based on the most recent change
				station.setDefaultLanguage(language);
				System.out.println("Changed language to " + language.getDisplayName());
				break;
			case IMAGEBYTES:
				LOGGER.log(LOGLEVEL, "A1iciaRemote: IMAGEBYTES");
				if (showImage) {
					LOGGER.log(LOGLEVEL, "A1iciaRemote:receiveBytes: object is ImageObject");
					mediaObject = (MediaObject) clientObject;
					showImage(mediaObject);
				}
				break;
			case AUDIOBYTES:
				LOGGER.log(LOGLEVEL, "A1iciaRemote: AUDIOBYTES");
				if (playAudio) {
					LOGGER.log(LOGLEVEL, "A1iciaRemote:receiveBytes: object is AudioObject");
					audioObject = (AudioObject) clientObject;
					playAudio(audioObject);
				}
				break;
			case VIDEOBYTES:
				LOGGER.log(LOGLEVEL, "A1iciaRemote: VIDEOBYTES");
				if (playVideo) {
					LOGGER.log(LOGLEVEL, "A1iciaRemote:receiveBytes: object is VideoObject");
					mediaObject = (MediaObject) clientObject;
					playVideo(mediaObject);
				}
				break;
			default:
				System.err.println("Unknown client object type = " + objectType);
				break;
		}
	}
	
	private void showImage(MediaObject imageObject) {
		byte[][] imageBytes;
		BufferedImage image;
		String imageTitle;
		
		SharedUtils.checkNotNull(imageObject);
		imageBytes = imageObject.getMediaBytes();
		imageTitle = imageObject.getMediaTitle();
		for (byte[] imageArray : imageBytes) {
			try {
				image = MediaUtils.byteArrayToImage(imageArray);
				imageDisplayer.displayImage(image, imageTitle);
			} catch (IOException e) {
	            throw new A1iciaAPIException("Can't restore image from bytes", e);
			}
		}
	}
	
	private void sendQuietCommand() {
		SerialSememe sememe;
		
		sememe = new SerialSememe();
		sememe.setName("pulse_yellow_LED");
		receiveCommand(sememe);
	}
	
	private void playAudio(AudioObject audioObject) {
		AudioFormat audioFormat;
		SerialAudioFormat serialFormat;
		byte[][] audioBytes;
		
		SharedUtils.checkNotNull(audioObject);
		if (station.isQuiet()) {
			sendQuietCommand();
			return;
		}
		audioBytes = audioObject.getMediaBytes();
		serialFormat = audioObject.getAudioFormat();
		if (audioBytes == null) {
            throw new A1iciaAPIException("Can't play audio from file");
		}
		for (byte[] audioArray : audioBytes) {
			try {
				LOGGER.log(LOGLEVEL, "A1iciaRemote:playAudio: audio from byte array");
				if (serialFormat == null) {
					AudioBytePlayer.playAudioFromByteArray(audioArray, 
							audioObject.getLengthSeconds());
				} else {
					audioFormat = MediaUtils.serialToAudioFormat(serialFormat);
					AudioBytePlayer.playAudioFromByteArray(audioFormat, audioArray, 
							audioObject.getLengthSeconds());
				}
			} catch (Exception e) {
			    throw new A1iciaAPIException("Can't play audio from byte array", e);
			}
		}
	}
	
	private void playVideo(MediaObject object) {
		byte[][] videoBytes;
		MediaFormat format;
		
		SharedUtils.checkNotNull(object);
		if (station.isQuiet()) {
			sendQuietCommand();
			return;
		}
		videoBytes = object.getMediaBytes();
		format = object.getMediaFormat();
		if (videoBytes == null || format == null) {
            throw new A1iciaAPIException("A1iciaRemote: bad argument(s) in playVideo");
		}
		LOGGER.log(LOGLEVEL, "A1iciaRemote:playVideo: video from byte array");
		MediaUtils.playMediaBytes(videoBytes, format);
	}
	
	void receiveText(String text, Language lang) {

		SharedUtils.checkNotNull(text);
		LOGGER.log(LOGLEVEL, "A1iciaRemote:receiveText: text is {0}", text);
		display.receiveText(text);
		if (useTTS) {
			processTTS(text, lang);
		}
	}
	
	void receiveRequest(String text, Language lang) {

		SharedUtils.checkNotNull(text);
		LOGGER.log(LOGLEVEL, "A1iciaRemote:receiveRequest: text is {0}", text);
		display.receiveRequest(text);
		if (useTTS) {
			processTTS(text, lang);
		}
	}
	
	void receiveCommand(SerialSememe sememe) {
		String cmd;
		
		SharedUtils.checkNotNull(sememe);
		cmd = sememe.getName();
		LOGGER.log(LOGLEVEL, "A1iciaRemote:receiveCommand: command is {0}", cmd);
		switch (sememe.getName()) {
			case "central_startup":
				serverUp = true;
				break;
			case "central_shutdown":
				serverUp = false;
				break;
			default:
				break;
		}
		display.receiveCommand(sememe);
	}
	
	private void processTTS(String text, Language lang) {
		String speech;
		int colonPos;
		
		SharedUtils.checkNotNull(text);
		SharedUtils.checkNotNull(lang);
		if (station.isQuiet()) {
			sendQuietCommand();
			return;
		}
		LOGGER.log(LOGLEVEL, "A1iciaRemote:processTTS: text is {0}", text);
		if (text.startsWith("ME")) {
			colonPos = text.indexOf(':');
			speech = text.substring(colonPos + 1);
		} else if (text.startsWith("A1ICIA:")) {
			speech = text.substring(8);
		} else {
			speech = text;
		}
		if (speech == null || speech.isEmpty() || speech.equals("null")) {
			return;
		}
		TTSPico.ttsToAudio(lang, speech);
	}
	
	@SuppressWarnings("unused")
	private static String getMyIPAddress() {
		InetAddress inetAddress;
		
		try {
			inetAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
            throw new A1iciaAPIException("Can't get my IP address", e);
		}
		return inetAddress.getHostAddress();
	}
	
	private class JebusListener extends BinaryJedisPubSub {
		
		JebusListener() {
			
		}
		
        @Override
		public void onMessage(byte[] channel, byte[] msgBytes) {
        	
    		LOGGER.log(LOGLEVEL, "A1iciaRemote:listener: got message");
    		executor.submit(new Runnable() {
    			@Override
    			public void run() {
    	        	receiveBytes(msgBytes);
    			}
    		});
        }

        @Override
		public void onSubscribe(byte[] channel, int subscribedChannels) {

        	LOGGER.log(LOGLEVEL, "Subscribed to A1icia's 'from' channel");
        }

        @Override
		public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        	
        	LOGGER.log(LOGLEVEL, "Unsubscribed to A1icia's 'from' channel");
        }
	}
}
