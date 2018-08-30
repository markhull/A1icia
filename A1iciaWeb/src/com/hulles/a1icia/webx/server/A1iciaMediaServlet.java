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
package com.hulles.a1icia.webx.server;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.hulles.a1icia.api.jebus.JebusBible;
import com.hulles.a1icia.api.jebus.JebusBible.JebusKey;
import com.hulles.a1icia.api.jebus.JebusHub;
import com.hulles.a1icia.api.jebus.JebusPool;
import com.hulles.a1icia.media.MediaFormat;
import com.hulles.a1icia.webx.shared.SharedUtils;

import redis.clients.jedis.Jedis;

final public class A1iciaMediaServlet extends HttpServlet {
	private final static Logger LOGGER = Logger.getLogger("A1iciaWeb.A1iciaMediaServlet");
	private final static Level LOGLEVEL = Level.FINE;
	private static final long serialVersionUID = 1052244723683932569L;
	private static final int MEDIA_CACHE_TTL = 60 * 60 * 1; // 1 hr in seconds
	private int imageHits = 0;
	private int mediaHits = 0;
	
	// if this doesn't work, make sure that including the servlet in web.xml instantiates automatically
	
	public A1iciaMediaServlet()  {

        LOGGER.log(LOGLEVEL, "A1iciaMediaServlet: instantiated");
	}
		
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException	  {
	    String qStr;
	    String imgParam;
	    String mmdParam;
	    BufferedImage image;
	    byte[] mediaBytes;
	    String mediaKey;
	    MediaFormat format;
	    
	    qStr = req.getQueryString();
	    imgParam = req.getParameter("img");
	    if (imgParam != null) {
            imageHits++;
			LOGGER.log(LOGLEVEL, "A1iciaMediaServlet: image query = {0}, hits = {1}", 
                    new Object[]{qStr, imageHits});
			image = parseQuery(imgParam);
			if (image == null) {
		    	resp.setStatus(204); // no content available
				return;
		    }
		    if (writeImage(resp, image)) {
			    resp.setStatus(200); // content created, request fulfilled
		    } else {
		    	resp.setStatus(422); // unprocessable entry
		    }
		    return;
	    }
	    mmdParam = req.getParameter("mmd");
	    if (mmdParam != null) {
            mediaHits++;
			LOGGER.log(LOGLEVEL, "A1iciaMediaServlet: multimedia query = {0}, hits = {1}", 
                    new Object[]{qStr, mediaHits});
		    mediaKey = SafeHtmlUtils.fromString(mmdParam).asString();
	    	mediaBytes = getAudioBytes(mediaKey);
	    	format = getMediaFormat(mediaKey);
	    	if (mediaBytes == null || format == null) {
				resp.setStatus(204); // no content available
				return;
	    	}
	    	if (writeAudio(resp, format, mediaBytes)) {
			    resp.setStatus(200); // content created, request fulfilled
		    } else {
		    	resp.setStatus(422); // unprocessable entry
	    	}
	    	return;
	    }
	    // unknown request
	    resp.setStatus(415); // unsupported media type
	  }

	private static boolean writeAudio(HttpServletResponse response, MediaFormat format, byte[] audioBytes) {
		
		LOGGER.log(LOGLEVEL, "A1iciaMediaServlet: preparing audio for response");
		// possibly set max-age=30, or some value like that
		response.setHeader("Cache-Control", "public");
		switch (format) {
			case MP3:
				response.setContentType("audio/mpeg");
				break;
			case WAV:
				response.setContentType("audio/wav");
				break;
			default:
				SharedUtils.error("MediaServlet: bad audio format");
				return false;
		}
		try {
		    return writeAudio(response.getOutputStream(), audioBytes);
		} catch (IOException e) {
		    SharedUtils.error("MediaServlet: error getting audio output stream", e);
		    return false;
		}
	}

	private static boolean writeAudio(OutputStream stream, byte[] audioBytes) {
		
		LOGGER.log(LOGLEVEL, "A1iciaMediaServlet: writing audio into response, bytes len = {0}", audioBytes.length);
	    try {
	        stream.write(audioBytes);
	        stream.close();
	    } catch (IOException e) {
	        SharedUtils.error("MediaServlet: error writing audio output stream", e);
	        return false;
	    }
	    return true;
	}

	private static boolean writeImage(HttpServletResponse response, BufferedImage image) {
		
		// possibly set max-age=30, or some value like that
		response.setHeader("Cache-Control", "public");
		response.setContentType("image/png");
		try {
		    return writeImage(response.getOutputStream(), image);
		} catch (IOException e) {
		    SharedUtils.error("MediaServlet: error getting image output stream", e);
		    return false;
		}
	}

	private static boolean writeImage(OutputStream stream, BufferedImage image) {
		
	    try {
	        ImageIO.write(image, "png", stream);
	        stream.close();
	    } catch (IOException e) {
	        SharedUtils.error("MediaServlet: error writing image output stream", e);
	        return false;
	    }
	    return true;
	}

	private static BufferedImage parseQuery(String query) {
//	    BufferedImage image;
//	    char prefix;
//	    String uuidStr = null;
//	    Person dbPerson;
//	    SerialUUID<SerialPerson> personUUID;
	    
//	    image = null;

//	    prefix = query.charAt(0);
//	    if (query.length() > 1) {
//		    uuidStr = SafeHtmlUtils.fromString(query.substring(1)).asString();
//	    }
//	    switch (prefix) {
//		    case 't':
//		    	// let the browser optimize the repeated calls to this URL
//		    	image = trashCan;
//		    	break;
//		    case 'p': // person
//			    try {
//			    	personUUID = new SerialUUID<>(uuidStr);
//			    } catch (UUIDException e) {
//			    	SharedUtils.error("MediaServlet: bad person UUID received in CambioImageServlet", e);
//			    	return null;
//			    }
//			    dbPerson = Person.findPerson(personUUID);
//			    image = dbPerson.getAvatar();
//				logger.log(LOGLEVEL, "A1iciaMediaServlet serving image for = " + 
//						personUUID.getUUIDString() + ", has image = " + (image != null));
//			    if (image == null) {
//			    	image = noPersonImageAvailable;
//			    }
//			    break;
//		    default:
//		    	SharedUtils.error("MediaServlet: bad prefix in query = " + prefix);
//		    	return null;
//	    }
//	    if (image == null) {
//	    	SharedUtils.error("MediaServlet: null image");
//	    	return null;
//	    }
//	    return image;
	    return null;
	}
	
	@SuppressWarnings("resource")
	public static Long saveAudioBytes(MediaFormat format, byte[] audioBytes) {
		JebusPool jebusPool;
		String counterKey;
		Long val;
		byte[] keyBytes;
		String hashKey;
		String mediaBytesKey;
		String mediaFormatKey;
		
		SharedUtils.checkNotNull(audioBytes);
		LOGGER.log(LOGLEVEL, "A1iciaMediaServlet: in saveAudioBytes, bytes len = {0}", audioBytes.length);
		jebusPool = JebusHub.getJebusLocal();
		try (Jedis jebus = jebusPool.getResource()) {
			counterKey = JebusBible.getStringKey(JebusKey.ALICIAMEDIACACHECOUNTERKEY, jebusPool);
			val = jebus.incr(counterKey);
			hashKey = JebusBible.getA1iciaMediaCacheHashKey(val, jebusPool);
			keyBytes = hashKey.getBytes();
			mediaBytesKey = JebusBible.getStringKey(JebusKey.MEDIABYTESFIELD, jebusPool);
			mediaFormatKey = JebusBible.getStringKey(JebusKey.MEDIAFORMATFIELD, jebusPool);
			jebus.hset(keyBytes, mediaBytesKey.getBytes(), audioBytes);
			jebus.hset(hashKey, mediaFormatKey, format.name());
			jebus.expire(hashKey, MEDIA_CACHE_TTL);
		}
		return val;
	}
	
	@SuppressWarnings("resource")
	private static byte[] getAudioBytes(String key) {
		JebusPool jebusPool;
		Long val;
		String hashKey;
		byte[] audioBytes;
		byte[] keyBytes;
		String mediaBytesKey;
		
		SharedUtils.checkNotNull(key);
		LOGGER.log(LOGLEVEL, "A1iciaMediaServlet: in getAudioBytes");
		jebusPool = JebusHub.getJebusLocal();
		val = Long.parseLong(key);
		try (Jedis jebus = jebusPool.getResource()) {
			hashKey = JebusBible.getA1iciaMediaCacheHashKey(val, jebusPool);
			keyBytes = hashKey.getBytes();
			mediaBytesKey = JebusBible.getStringKey(JebusKey.MEDIABYTESFIELD, jebusPool);
			audioBytes = jebus.hget(keyBytes, mediaBytesKey.getBytes());
		}
		LOGGER.log(LOGLEVEL, "A1iciaMediaServlet: leaving getAudioBytes, bytes len = {0}", audioBytes.length);
		return audioBytes;
	}
	
	@SuppressWarnings("resource")
	private static MediaFormat getMediaFormat(String key) {
		JebusPool jebusPool;
		Long val;
		String hashKey;
		String formatStr;
		String mediaFormatKey;
		
		SharedUtils.checkNotNull(key);
		jebusPool = JebusHub.getJebusLocal();
		val = Long.parseLong(key);
		try (Jedis jebus = jebusPool.getResource()) {
			hashKey = JebusBible.getA1iciaMediaCacheHashKey(val, jebusPool);
			mediaFormatKey = JebusBible.getStringKey(JebusKey.MEDIAFORMATFIELD, jebusPool);
			formatStr = jebus.hget(hashKey, mediaFormatKey);
		}
		return MediaFormat.valueOf(formatStr);
	}
	
//	private BufferedImage imageFromFile(String fileName) {
//		BufferedImage bImage;
//		File img;
//		
//		img = new File(fileName);
//		try {
//			bImage = ImageIO.read(img);
//		} catch (IOException e) {
//    		CambioServerUtils.error("Crappy image file from " + fileName);
//    		return null;
//		}
//		return bImage;
//	}
}
