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
package com.hulles.alixia.tools;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.ApplicationKeys;
import com.hulles.alixia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.media.Language;

/**
 * ExternalAperture is where ALL external access in Alixia occurs, except for AlixiaGoogleTranslate,
 * which we're going to fix at some point. 'External access' means access
 * to the Internet, primarily. File IO on the same server as the one upon which Alixia is running
 * (see ApplicationKeys) is not considered external access, nor is database access (Redis or MySQL)
 * regardless of server.
 * 
 * @author hulles
 *
 */
final public class ExternalAperture {
	private final static Logger LOGGER = LoggerFactory.getLogger(ExternalAperture.class);
    
	private ExternalAperture() {
	}
	
	/**
	 * Returns true if a link is accessible. A response code < 400 is considered "accessible".
	 * 
	 * @param link The string URL to check
	 * @return True if we can connect to the URL
	 */
	public static Boolean linkIsOK(String link) {
		URL url;
		HttpURLConnection conn;
		int responseCode;
		
		try {
			url = new URL(link);
		} catch (MalformedURLException e) {
			LOGGER.warn("Bad URL in linkIsOK", e);
			return false;
		}
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.connect();
			responseCode = conn.getResponseCode();
			if (responseCode < 400) {
				return true;
			}
			return false;
		} catch (IOException e) {
			return false;
		}
	}

    public static String getGoogleTranslation(Language from, Language to, String textToTranslate, String format, String key) {
//        String urlTemplate = "https://translation.googleapis.com/language/translate/v2?source=%s&target=%s&q=%s&format=%s&key=%s";
        String urlTemplate;
        String urlString;
        String encodedText;
        ApplicationKeys appKeys;
        
        SharedUtils.checkNotNull(from);
        SharedUtils.checkNotNull(to);
        SharedUtils.checkNotNull(textToTranslate);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.GOOGLEXLATE);       
//        xkey = "AIzaSyCsD4HkuRu7Gb1lvVUrQOYQM5Ejw4GggLk";
        try {
            encodedText = URLEncoder.encode(textToTranslate, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("ExternalAperture: can't encode url = {}", textToTranslate, ex);
            return null;
        }
        urlString = String.format(urlTemplate, from.getGoogleName(), to.getGoogleName(), 
                encodedText, format, key);
		return getURLStringResult(urlString);
    }
    
    
	/**
	 * Get the current temperature and humidity from an ESP8266 mini-server as a string.
	 * 
	 * @param which Either "temp" or "humidity" depending on the value you want
	 * @return The temperature or humidity string, e.g. "77.00"
	 */
	public static String getCurrentTempAndHumidity(String which) {
		String urlString;
		String urlTemplate;
		ApplicationKeys appKeys;
		
		SharedUtils.checkNotNull(which);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.TEMPHUMIDITY);
		urlString = String.format(urlTemplate, which);
		return getURLStringResult(urlString);
	}

	/**
	 * Get the current weather from Open Weather Map as a string.
	 * 
	 * @param cityID The city code
	 * @param appID The OWM API ID
	 * @return The weather string
	 */
	public static String getCurrentWeatherOWM(Integer cityID, String appID) {
		String urlString;
		String urlTemplate;
		ApplicationKeys appKeys;
		
		SharedUtils.checkNotNull(cityID);
		SharedUtils.checkNotNull(appID);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.OWMCURRENT);
		urlString = String.format(urlTemplate, cityID, appID);
		return getURLStringResult(urlString);
	}

	/**
	 * Get the weather forecast from Open Weather Map as a string.
	 * 
	 * @param cityID The city code
	 * @param appID The OWM API ID
	 * @return The weather forecast string
	 */
	public static String getForecastOWM(Integer cityID, String appID) {
		String urlString;
		String urlTemplate;
		ApplicationKeys appKeys;
		
		SharedUtils.checkNotNull(cityID);
		SharedUtils.checkNotNull(appID);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.OWMFORECAST);
		urlString = String.format(urlTemplate, cityID, appID);
		return getURLStringResult(urlString);
	}
	
	/**
	 * Search WikiData for a string.
	 * 
	 * @param target The string for which to search
	 * @return The search results
	 */
	public static String searchWikiData(String target) {
		String urlString;
		String urlTemplate;
		ApplicationKeys appKeys;
	    
		SharedUtils.checkNotNull(target);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.WIKIDATASEARCH);
		urlString = String.format(urlTemplate, target);
		return getURLStringResult(urlString);
	}

	/**
	 * Get WikiData results for a given title.
	 * 
	 * @param title The WikiData title, which should already be known to exist in WikiData
	 * @return The results
	 */
	public static String getWikiDataByTitle(String title) {
		String urlString;
		String urlTemplate;
		ApplicationKeys appKeys;
	    
		SharedUtils.checkNotNull(title);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.WIKIDATATITLE);
		urlString = String.format(urlTemplate, title);
		return getURLStringResult(urlString);
	}


	/**
	 * Get WikiData results for a given ID.
	 * 
	 * @param qID The WikiData ID, which should already be known to exist in WikiData
	 * @return The results
	 */
	public static String getWikiDataByID(String qID) {
		String urlString;
		String urlTemplate;
		ApplicationKeys appKeys;
	    
		SharedUtils.checkNotNull(qID);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.WIKIDATAID);
		urlString = String.format(urlTemplate, qID);
		return getURLStringResult(urlString);
	}

	/**
	 * Attempt to determine the location of the IP address of the caller.
	 * 
	 * @param ipinfoToken The ipInfo API token
	 * @return The location string
	 */
	public static String getCurrentLocation(String ipinfoToken) {
	    String urlString;
		String urlTemplate;
		ApplicationKeys appKeys;
	    
	    SharedUtils.checkNotNull(ipinfoToken);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.LOCATIONURL);
		urlString = String.format(urlTemplate, ipinfoToken);
		return getURLStringResult(urlString);
	}

	/**
	 * This validates the structure of a query. It does not detect if the query is
	 * suitable for answering by Wolfram|Alpha; for that, see getWolframFastQuery.
	 * FIXME This is currently "broken", in that W|A apparently doesn't support this
	 * type of query from a free account, a fact which is not documented anywhere, $!*&$@.
	 * 
	 * @param query The query to check
	 * @param wolframID The W|A ID
	 * @return The result
	 */
	public static String getWolframValidateQuery(String query, String wolframID) {
	    String urlString;
		String urlTemplate;
		ApplicationKeys appKeys;
	    
	    SharedUtils.checkNotNull(query);
	    SharedUtils.checkNotNull(wolframID);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.WOLFRAMVALIDATE);
		urlString = String.format(urlTemplate, query, wolframID);
		return getURLStringResult(urlString);
	}

	/**
	 * This returns a short answer to the query, in a form suitable for a speech answer. 
	 * "How far is Los Angeles from New York?" returns "The answer is 2464 miles". 
	 * The answer is in plaintext, vs. XML or JSON
	 * 
	 * @param query The URL-encoded query
	 * @param wolframID The AppID
	 * @return The speech answer
	 */
	public static String getWolframSpokenQuery(String query, String wolframID) {
	    String urlString;
		String urlTemplate;
		ApplicationKeys appKeys;
	    
	    SharedUtils.checkNotNull(query);
	    SharedUtils.checkNotNull(wolframID);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.WOLFRAMSPOKEN);
		urlString = String.format(urlTemplate, query, wolframID);
		LOGGER.debug("WolframSpokenQuery url = {}", urlString);
		return getURLStringResult(urlString);
	}

	/**
	 * This returns an image for a simplified Wolfram|Alpha response to the
	 * query. There is only one pod.
	 * 
	 * @param query The URL-encoded query
	 * @param wolframID The AppID
	 * @return The actual image of the answer, e.g. a GIF
	 */
	public static BufferedImage getWolframSimpleQuery(String query, String wolframID) {
	    String urlString;
		String urlTemplate;
		ApplicationKeys appKeys;
	    
	    SharedUtils.checkNotNull(query);
	    SharedUtils.checkNotNull(wolframID);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.WOLFRAMSIMPLE);
		urlString = String.format(urlTemplate, query, wolframID);
		LOGGER.debug("WolframSimpleQuery url = {}", urlString);
		return getURLImageResult(urlString);
	}

	/**
	 * This returns a short answer to the query; "How far is Los Angeles from New York?"
	 * returns "2464 miles". The answer is in plaintext, vs. XML or JSON
	 * 
	 * @param query The URL-encoded query to look up
	 * @param wolframID The AppID
	 * @return The answer
	 */
	public static String getWolframShortQuery(String query, String wolframID) {
	    String urlString;
		String urlTemplate;
		ApplicationKeys appKeys;
	    
	    SharedUtils.checkNotNull(query);
	    SharedUtils.checkNotNull(wolframID);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.WOLFRAMSHORT);
		urlString = String.format(urlTemplate, query, wolframID);
		LOGGER.debug("WolframShortQuery url = {}", urlString);
		return getURLStringResult(urlString);
	}

	/**
	 * This returns the full XML Wolfram|Alpha response to the query.
	 * 
	 * @param query The URL-encoded query
	 * @param wolframID The AppID
	 * @return The XML results
	 */
	public static String getWolframQuery(String query, String wolframID) {
	    String urlString;
		String urlTemplate;
		ApplicationKeys appKeys;
	    
	    SharedUtils.checkNotNull(query);
	    SharedUtils.checkNotNull(wolframID);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.WOLFRAMQUERY);
		urlString = String.format(urlTemplate, query, wolframID);  // pi, 3.141592653589793
		LOGGER.debug("WolframQuery url = {}", urlString);
		return getURLStringResult(urlString);
	}

	/**
	 * Query Mozilla DeepSpeech server.
	 * 
	 * @param audio The audio byte array
	 * @return
	 */
	public static String queryDeepSpeech(byte[] audio) {
		String urlString;
		ApplicationKeys appKeys;
		
		SharedUtils.checkNotNull(audio);
		appKeys = ApplicationKeys.getInstance();
		urlString = appKeys.getKey(ApplicationKey.DEEPSPEECH);
		return postURLStringResult(urlString, audio, "application/octet-stream");
	}

	/**
	 * Query Tika server for file metadata.
	 * 
	 * @param filePath The path of the file about which we want information
	 * @return The information
	 */
	public static String queryTika(Path filePath) {
		String urlString;
        String urlTemplate;
		ApplicationKeys appKeys;
		
		SharedUtils.checkNotNull(filePath);
		appKeys = ApplicationKeys.getInstance();
		urlTemplate = appKeys.getKey(ApplicationKey.TIKA);
        urlString = String.format(urlTemplate, "meta");
		return putURLStringResult(urlString, filePath);
	}

	/**
	 * This is a test query for Alixia Node. TODO move to JUnit.
	 * 
	 * @param query The test query
	 * @return
	 */
	public static String postTestQueryToAlixiaNode(String query) {
        String result;
        
		SharedUtils.checkNotNull(query);
		result = postURLStringResult("http://localhost:1337/alixia/text", query, "text/plain");
        return result;
	}
	
	/**
	 * This is a helper function that returns a String result from an HTTP POST.
	 * 
	 * @param urlString The string URL
	 * @param query The query to post
	 * @param contentType The content type, for the POST parameter; "plain/text" e.g.
	 * @return The result
	 */
	private static String postURLStringResult(String urlString, String query, String contentType) {
		String charset = "UTF-8";
		URL url;
		URLConnection conn;
		StringBuilder sb;
		String line;
	    
		SharedUtils.checkNotNull(urlString);
	    SharedUtils.checkNotNull(query);
	    SharedUtils.checkNotNull(contentType);
		sb = new StringBuilder();
		try {
			url = new URL(urlString);
		} catch (MalformedURLException ex) {
			LOGGER.error("Bad URL in postURLStringResult\nURL string: {}, query: {}, content type: {}" + urlString, query, contentType, ex);
			return null;
		}
		try {
			conn = url.openConnection();			
		} catch (IOException ex) {
			LOGGER.error("I/O Exception opening connection in postURLStringResult\nURL string: {}, query: {}, content type: {}", urlString, query, contentType, ex);
            ex.printStackTrace();
			return null;
		}
		
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		conn.setRequestProperty("Accept-Charset", charset);
		conn.setRequestProperty("Content-Type", contentType + ";charset=" + charset);
		try {
			conn.connect();
		} catch (IOException ex) {
            LOGGER.error("I/O Exception connecting connection in postURLStringResult\nURL string: {}, query: {}, content type: {}", urlString, query, contentType, ex);
			return null;
		}
		
		try (OutputStream outStream = conn.getOutputStream()) {
			try (OutputStreamWriter out = new OutputStreamWriter(outStream)) {
				out.write(query);
				out.flush();
			}
		} catch (IOException ex) {
            LOGGER.error("I/O Exception writing output stream in postURLStringResult\nURL string: {}, query: {}, content type: {}", urlString, query, contentType, ex);
			return null;
		}
			  
		try (InputStream inStream = conn.getInputStream()) { 
			try (BufferedReader in = new BufferedReader(new InputStreamReader(inStream))) {
				while (true) {
					try {
						line = in.readLine();
						if (line == null) {
							break;
						}
						sb.append(line);
					} catch (IOException ex) {
                        LOGGER.error("I/O Exception reading input stream in postURLStringResult\nURL string: {}, query: {}, content type: {}", urlString, query, contentType, ex);
					}
				}
			}
		} catch (IOException ex) {
            LOGGER.error("I/O Exception creating input stream in postURLStringResult\nURL string: {}, query: {}, content type: {}", urlString, query, contentType, ex);
			return null;
		}
		return sb.toString();
	}
	
	/**
	 * This is a helper function that returns a String result from an HTTP POST.
	 * It accepts a byte array (audio e.g.) as input.
	 * @see postURLStringResult(String, String, String)
	 * 
	 * @param urlString The string URL
	 * @param query The query to post
	 * @param contentType The content type, for the POST parameter; "plain/text" e.g.
	 * @return The result
	 */
	private static String postURLStringResult(String urlString, byte[] bytes, String contentType) {
		URL url;
		URLConnection conn;
		StringBuilder sb;
		String line;
	    
		SharedUtils.checkNotNull(urlString);
	    SharedUtils.checkNotNull(bytes);
	    SharedUtils.checkNotNull(contentType);
		sb = new StringBuilder();
		try {
			url = new URL(urlString);
		} catch (MalformedURLException ex) {
            LOGGER.error("Bad URL in postURLStringResult\nURL string: {}, content type: {}", urlString, contentType, ex);
			return null;
		}
		try {
			conn = url.openConnection();			
		} catch (IOException ex) {
            LOGGER.error("I/O Exception opening connection in postURLStringResult\nURL string: {}, content type: {}", urlString, contentType, ex);
			return null;
		}
		
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Transfer-Encoding", "chunked");
		conn.setRequestProperty("Content-Type", contentType);
		try {
			conn.connect();
		} catch (IOException ex) {
            LOGGER.error("I/O Exception connecting connection in postURLStringResult\nURL string: {}, content type: {}", urlString, contentType, ex);
			return null;
		}
		
		try (OutputStream outStream = conn.getOutputStream()) {
			outStream.write(bytes);
			outStream.flush();
		} catch (IOException ex) {
            LOGGER.error("I/O Exception writing output stream in postURLStringResult\nURL string: {}, content type: {}", urlString, contentType, ex);
			return null;
		}
			  
		try (InputStream inStream = conn.getInputStream()) { 
			try (BufferedReader in = new BufferedReader(new InputStreamReader(inStream))) {
				while (true) {
					try {
						line = in.readLine();
						if (line == null) {
							break;
						}
						sb.append(line);
					} catch (IOException ex) {
			            LOGGER.error("I/O Exception reading input stream in postURLStringResult\nURL string: {}, content type: {}", urlString, contentType, ex);
					}
				}
			}
		} catch (IOException ex) {
            LOGGER.error("I/O Exception creating input stream in postURLStringResult\nURL string: {}, content type: {}", urlString, contentType, ex);
			return null;
		}
		return sb.toString();
	}
	
	/**
	 * This is a helper function that returns a String result from an HTTP POST.
	 * It accepts a byte array (audio e.g.) as input.
	 * @see postURLStringResult(String, String, String)
	 * 
	 * @param urlString The string URL
	 * @param query The query to post
	 * @param contentType The content type, for the POST parameter; "plain/text" e.g.
	 * @return The result
	 */
	private static String putURLStringResult(String urlString, Path path) {
		URL url;
		HttpURLConnection conn;
		StringBuilder sb;
		String line;
	    
		SharedUtils.checkNotNull(urlString);
	    SharedUtils.checkNotNull(path);
		sb = new StringBuilder();
		try {
			url = new URL(urlString);
		} catch (MalformedURLException ex) {
			LOGGER.error("Bad URL in putURLStringResult\nURL string: {}", urlString, ex);
			return null;
		}
		try {
			conn = (HttpURLConnection) url.openConnection();			
		} catch (IOException ex) {
			LOGGER.error("I/O Exception opening connection in putURLStringResult\nURL string: {}", urlString, ex);
			return null;
		}
		
		conn.setDoInput(true);
		conn.setDoOutput(true);
 		conn.setRequestProperty("Accept", "application/json");
        try {
            conn.setRequestMethod("PUT");
        } catch (ProtocolException ex) {
			LOGGER.error("Protocol Exception with connection in putURLStringResult\nURL string: {}", urlString, ex);
			return null;
        }
		try {
			conn.connect();
		} catch (IOException ex) {
			LOGGER.error("I/O Exception connecting connection in putURLStringResult\nURL string: {}", urlString, ex);
			return null;
		}
		
		try (OutputStream outStream = conn.getOutputStream()) {
            Files.copy(path, outStream);
			outStream.flush();
		} catch (IOException ex) {
			LOGGER.error("I/O Exception writing output stream in putURLStringResult\nURL string: {}", urlString, ex);
			return null;
		}
			  
		try (InputStream inStream = conn.getInputStream()) { 
			try (BufferedReader in = new BufferedReader(new InputStreamReader(inStream))) {
				while (true) {
					try {
						line = in.readLine();
						if (line == null) {
							break;
						}
						sb.append(line);
                        sb.append("\n");
					} catch (IOException ex) {
						LOGGER.error("I/O Exception reading input stream in putURLStringResult\nURL string: {}", urlString, ex);
					}
				}
			}
		} catch (IOException ex) {
			LOGGER.error("I/O Exception creating input stream in putURLStringResult\nURL string: {}", urlString, ex);
			return null;
		}
		return sb.toString();
	}

	/**
	 * This is a helper function that returns a String result from an HTTP GET call. It uses java.net.URL
	 * to do the GET.
	 * 
	 * @param query The URL (including the query) as a string
	 * @return The result as a String
	 */
	private static String getURLStringResult(String query) {
		URL url;
		StringBuilder sb;
		String line;
	    
	    SharedUtils.checkNotNull(query);
		sb = new StringBuilder();
		try {
			url = new URL(query);
		} catch (MalformedURLException ex) {
			LOGGER.error("Bad URL in getURLStringResult\nquery: {}", query, ex);
			return null;
		}
		try (InputStream inStream = url.openStream()) { 
			try (BufferedReader in = new BufferedReader(new InputStreamReader(inStream))) {
				while (true) {
					try {
						line = in.readLine();
						if (line == null) {
							break;
						}
						sb.append(line);
					} catch (IOException ex) {
						LOGGER.error("I/O Exception reading input stream in getURLStringResult\nquery: {}", query, ex);
					}
				}
			}
		} catch (IOException ex) {
			LOGGER.error("I/O Exception creating input stream in getURLStringResult\nquery: {}", query, ex);
			return null;
		}
		return sb.toString();
	}
	

	/**
	 * This is a helper function that returns a BufferedImage result to an HTTP GET call. 
	 * It uses javax.imageio.ImageIO to read the input stream from the URL.
	 * 
	 * @param query The URL (including the query) as a string
	 * @return The result as a String
	 */
	private static BufferedImage getURLImageResult(String query) {
		URL url;
	    BufferedImage image;
	    
	    SharedUtils.checkNotNull(query);
		try {
			url = new URL(query);
		} catch (MalformedURLException ex) {
			LOGGER.error("Bad URL in getURLImageResult\nquery: {}", query, ex);
			return null;
		}
		try {
			image = ImageIO.read(url);
		} catch (IOException ex) {
			LOGGER.error("I/O Exception reading input stream in getURLImageResult\nquery: {}", query, ex);
			return null;
		}
		return image;
	}
}
