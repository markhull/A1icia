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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hulles.alixia.webx.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.hulles.alixia.api.object.AlixiaClientObject;
import com.hulles.alixia.api.object.AlixiaClientObject.ClientObjectType;
import com.hulles.alixia.api.object.AudioObject;
import com.hulles.alixia.media.MediaFormat;
import com.hulles.alixia.media.audio.TTSPico;
import com.hulles.alixia.webx.client.services.MindService;
import com.hulles.alixia.webx.shared.ProngException;
import com.hulles.alixia.webx.shared.SerialConsoleIn;
import com.hulles.alixia.webx.shared.SerialConsoleOut;
import com.hulles.alixia.webx.shared.SerialProng;
import com.hulles.alixia.webx.shared.SerialSystemInfo;
import com.hulles.alixia.webx.shared.SharedUtils;

/**
 * 
 * @author hulles
 */
final public class MindServiceImpl extends RemoteServiceServlet implements MindService {
	private static final long serialVersionUID = 3646580413754240731L;
	private static final String BUNDLE_NAME = "com.hulles.alixia.webx.Version";
	private static final String MEDIA_URL = "alixiaweb/services/media?mmd=";
	private static final boolean USE_TTS = true;
//	private final static Logger logger = Logger.getLogger("AlixiaWeb.MindServiceImpl");
//	private final static Level LOGLEVEL = Level.INFO;
	private final Prongz prongz;
	private final ConcurrentMap<String, MiniConsole> consoleMap;
	
	public MindServiceImpl() {
		
		prongz = Prongz.getInstance();
		consoleMap = new ConcurrentHashMap<>();
	}

	@SuppressWarnings("resource")
	@Override
	public void sendConsole(SerialProng prong, SerialConsoleIn input) throws ProngException {
		MiniConsole console;
		String prongKey;
		Set<String> prongKeys;
		
		prongz.matchProng(prong);
		SharedUtils.checkNotNull(input);
		prongKey = prong.getProngString();
		console = consoleMap.get(prongKey);
		if (console == null) {
			console = new MiniConsole();
			consoleMap.put(prongKey, console);
		}
		console.sendText(input.getText());
		// now we trim the console map, weeding out any deaders
		prongKeys = prongz.getCurrentProngKeys();
		for (Iterator<String> iter = consoleMap.keySet().iterator(); iter.hasNext(); ) {
			prongKey = iter.next();
			if (!prongKeys.contains(prongKey)) {
				console = consoleMap.get(prongKey);
				console.close();
				iter.remove();
			}
		}
	}

	@SuppressWarnings("resource")
	@Override
	public SerialConsoleOut receiveConsole(SerialProng prong) throws ProngException {
		SerialConsoleOut returnResult;
		String response;
		MiniConsole console;
		String prongKey;
		String expl;
		List<AlixiaClientObject> clientObjects;
		AudioObject audioObject;
		Long key;
		String url;
		MediaFormat format;
		List<String> urls;
		List<MediaFormat> formats;
		List<String> serialFormats;
		List<Integer> lengths;
		Integer length;
		
		prongz.matchProng(prong);
		prongKey = prong.getProngString();
		console = consoleMap.get(prongKey);
		if (console == null) {
			// this happens if we start polling before we send a message, which occurs at startup
			return null;
		}
		response = console.getMessages();
		expl = console.getExplanations();
		clientObjects = console.getClientObjects();
		urls = new ArrayList<>();
		formats = new ArrayList<>();
		lengths = new ArrayList<>();
		for (AlixiaClientObject obj : clientObjects) {
			if (obj.getClientObjectType() == ClientObjectType.AUDIOBYTES) {
				audioObject = (AudioObject) obj;
				format = audioObject.getMediaFormat();
				length = audioObject.getLengthSeconds();
				for (byte[] bytes : audioObject.getMediaBytes()) {
					key = AlixiaMediaServlet.saveAudioBytes(format, bytes);
					url = MEDIA_URL + key;
					urls.add(url);
					formats.add(format);
					lengths.add(length);
				}
			}
		}
		if ((response == null || response.isEmpty()) &&
				(expl == null || expl.isEmpty()) &&
				(urls.isEmpty())){
			return null;
		}
		if (USE_TTS) {
			processTTS(response, urls, formats);
		}
		returnResult = new SerialConsoleOut();
		returnResult.setText(response);
		returnResult.setExplain(expl);
		returnResult.setDatestamp(new Date());
		returnResult.setUrls(urls);
		returnResult.setLengths(lengths);
		serialFormats = new ArrayList<>(formats.size());
		for (MediaFormat fmt : formats) {
			switch (fmt) {
				case MP3:
					serialFormats.add("audio/mpeg");
					break;
				case WAV:
					serialFormats.add("audio/wav");
					break;
				default:
					SharedUtils.error("MindServiceImpl: bad audio format = " + fmt.toString());
			}
		}
		returnResult.setFormats(serialFormats);
		return returnResult;
	}
	
	@Override
	public String queryHealth(SerialProng prong) throws ProngException {
		
		prongz.matchProng(prong);
		return null;
	}
	
	@Override
	public void pingProng(SerialProng prong)  throws ProngException {
		
		prongz.matchProng(prong);
	}

	@Override
	public void clearProng(SerialProng prong) {
		
		prongz.removeProng(prong);
	}

	@Override
	public SerialSystemInfo checkSystems() {
		SerialSystemInfo sysInfo;
		
		sysInfo = new SerialSystemInfo();
		sysInfo.setVersion(getVersionString());
		sysInfo.setProngValue(prongz.getNewProng());
		return sysInfo;
	}
	
	private static String getVersionString() {
		ResourceBundle bundle;
		StringBuilder sb;
		String value;
		
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
		sb = new StringBuilder();
		value = bundle.getString("Name");
		sb.append(value);
		sb.append(" \"");
		value = bundle.getString("Build-Title");
		sb.append(value);
		sb.append("\", Version ");
		value = bundle.getString("Build-Version");
		sb.append(value);
		sb.append(", Build #");
		value = bundle.getString("Build-Number");
		sb.append(value);
		sb.append(" on ");
		value = bundle.getString("Build-Date");
		sb.append(value);
		return sb.toString();
	}
	
	private static void processTTS(String text, List<String> urls, List<MediaFormat> formats) {
		String speak;
//		int colonPos;
		byte[] audioBytes;
		Long key;
		String url;
		MediaFormat format;
		
		SharedUtils.checkNotNull(text);
//		if (text.startsWith("ME")) {
//			colonPos = text.indexOf(':');
//			speak = text.substring(colonPos + 1);
//		} else if (text.startsWith("ALIXIA:")) {
//			speak = text.substring(8);
//		} else {
			speak = text;
//		}
		if (speak == null || speak.isEmpty() || speak.equals("null")) {
			return;
		}
		audioBytes = TTSPico.ttsToBytes(speak);
		format = MediaFormat.WAV;
		key = AlixiaMediaServlet.saveAudioBytes(format, audioBytes);
		url = MEDIA_URL + key;
		urls.add(url);
		formats.add(format);
	}
	
}
