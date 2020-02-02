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
package com.hulles.alixia.api.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.google.common.base.CharMatcher;
import com.google.common.io.CharStreams;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;

/**
 * An olio class for various useful functions in Alixia. These methods are <b>NOT</b> GWT-safe.
 * 
 * @see com.hulles.alixia.api.SharedUtils
 * 
 * @author hulles
 *
 */
public final class AlixiaUtils {
//	private final static Logger LOGGER = LoggerFactory.getLogger(AlixiaUtils.class);
	private final static ZoneId DEFAULTZONE = ZoneId.systemDefault();
	private static final long MILLIS_IN_SECOND = 1000;
	private static final long MILLIS_IN_MINUTE = 60 * MILLIS_IN_SECOND;
	private static final long MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;
	private static final long MILLIS_IN_DAY = MILLIS_IN_HOUR * 24;
	private static final String DAYFORMAT = "%,d day(s), %d hour(s), %d minute(s)";
	private static final String HOURFORMAT = "%d hour(s), %d minute(s)";
	private static final String MINUTESECONDFORMAT = "%d minute(s), %f second(s)";
	private static final String SECONDFORMAT = "%.3f seconds";
	private static final String MILLIFORMAT = "%d ms";
	private static final int KB = 1024;
	private static final int MB = KB * KB;
	private static final int MINUTES_IN_HOUR = 60;
	private static final int MINUTES_IN_DAY = MINUTES_IN_HOUR * 24;
	private static final String MINUTEFORMAT = "%d minute(s)";
	private static final String GIGABYTEFORMAT = "%.2fGB";
	private static final String MEGABYTEFORMAT = "%.2fMB";
	private static final String KILOBYTEFORMAT = "%dKB";

	/**
	 * Format a long amount of elapsed milliseconds into human-friendly form.
	 * 
	 * @param milliseconds
	 * @return The formatted string
	 */
    public static String formatElapsedMillis(long milliseconds) {
    	long days = 0;
    	long hours = 0;
    	long minutes = 0;
    	float seconds; 	
    	long millis;
    	
    	millis = milliseconds;
    	if (millis > MILLIS_IN_DAY) {
    		days = millis / MILLIS_IN_DAY;
    		millis -= (days * MILLIS_IN_DAY);
    	}
    	if (millis > MILLIS_IN_HOUR) {
    		hours = millis / MILLIS_IN_HOUR;
    		millis -= (hours * MILLIS_IN_HOUR);
    	}
    	if (millis > MILLIS_IN_MINUTE) {
    		minutes = millis / MILLIS_IN_MINUTE;
    		millis -= (minutes * MILLIS_IN_MINUTE);
    	}
    	if (millis > MILLIS_IN_SECOND) {
    		seconds = (float)millis / (float)MILLIS_IN_SECOND;
    	} else {
    		seconds = 0;
    	}
    	
    	if (days > 0) {
    		return String.format(DAYFORMAT, days, hours, minutes);
    	}
    	if (hours > 0) {
    		return String.format(HOURFORMAT, hours, minutes);
    	}
    	if (minutes > 0) {
    		return String.format(MINUTESECONDFORMAT, minutes, seconds);
    	}
    	if (seconds > 0) {
    		return String.format(SECONDFORMAT, seconds);
    	}
		return String.format(MILLIFORMAT, millis);
    }
    
	/**
	 * Format a long amount of elapsed minutes into human-friendly form.
	 * 
	 * @param timeInMinutes
	 * @return The formatted string
	 */
	public static String formatElapsedMinutes(long timeInMinutes) {
		long days = 0;
		long hours = 0;
		long minutes;
		long time;
		
		SharedUtils.checkNotNull(timeInMinutes);
		time = timeInMinutes;
		if (time > MINUTES_IN_DAY) {
			days = Math.round(time / MINUTES_IN_DAY);
			time -= (days * MINUTES_IN_DAY);
		}
		if (time > MINUTES_IN_HOUR) {
			hours = Math.round(time / MINUTES_IN_HOUR);
			time -= (hours * MINUTES_IN_HOUR);
		}
		minutes = time;
		
		if (days > 0) {
			return String.format(DAYFORMAT, days, hours, minutes);
		}
		if (hours > 0) {
			return String.format(HOURFORMAT, hours, minutes);
		}
		return String.format(MINUTEFORMAT, minutes);
	}
	
	/**
	 * Format a long amount of elapsed seconds into human-friendly form.
	 * 
	 * @param seconds
	 * @return The formatted string
	 */
	public static String formatElapsedSeconds(long seconds) {
		
		return AlixiaUtils.formatElapsedMillis(seconds * 1000);
	}
	
	/**
	 * Format a long amount of kilobytes into human-friendly form.
	 * 
	 * @param kb Kilobytes
	 * @return The formatted string
	 */
	public static String formatKb(Long kb) {
		float amount;
		float kbf;
		
		SharedUtils.checkNotNull(kb);
		kbf = kb;
		if (kb > MB) {
			amount = kbf / MB;
			return String.format(GIGABYTEFORMAT, amount);
		}
		if (kb > KB) {
			amount = kbf / KB;
			return String.format(MEGABYTEFORMAT, amount);
		}
		return String.format(KILOBYTEFORMAT, kb);
	}

	/**
	 * Return true if the character is a primary vowel (not including y). For
	 * a more interesting approach given an entire string, see PorterStemmer cons function.
	 * 
	 * @param c The character to test
	 * @return True if the character is a primary vowel (aeiou)
	 */
	public static boolean isVowel(Character c) {
		CharMatcher matcher;
		
		SharedUtils.checkNotNull(c);
		matcher = CharMatcher.anyOf("AEIOUaeiou");
		return matcher.matches(c);
	}
	
	/**
	 * Capitalize the first letter of a string.
	 * 
	 * @param s The string to capitalize
	 * @return The capitalized string
	 */
	public static String capitalize(String s) {
		char[] chars;
		
		SharedUtils.checkNotNull(s);
		chars = s.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}
//    	
//    /**
//     * Create an informational message from argument strings. We don't create AlixiaError instances for info messages.
//     * 
//     * @param infomsgs A series of message strings separated by commas. Each
//     * string appears on a separate line in the output dialog.
//     * 
//     */
//    public static void info(String... infomsgs) {
//        StringBuilder dialogMsg;
//
//        dialogMsg = new StringBuilder();
//        // infomsgs can never be null
//        for (String msg : infomsgs) {
//            dialogMsg.append(msg);
//            dialogMsg.append("\n");
//        }
//        LOGGER.info(dialogMsg.toString());
//    }
//    
//    /**
//     * Create an informational message from a template plus argument strings. 
//     * We don't create AlixiaError instances for info messages.
//     * <br />
//     * Example: infoTemplate("This {} is {}!", "method", "awesome") yields "This method is awesome!"
//     * 
//     * @param template The template to pass as a pattern to MessageFormat, e.g. "This {} is {}."
//     * @param infomsgs A series of message strings separated by commas (as varargs)
//     * 
//     */
//    public static void infoTemplate(String template, String... infomsgs) {
//        String message;
//  
//        SharedUtils.checkNotNull(template);
//        message = MessageFormat.format(template, (Object[])infomsgs);
//        LOGGER.info(message);
//    }
//    
//    /**
//     * Create a warning message from argument strings.
//     * 
//     * @param warnmsgs A series of warning message strings separated by commas. Each
//     * string appears on a separate line in the output dialog.
//     */
//    public static void warning(String... warnmsgs) {
//        StringBuilder dialogMsg;
//        AlixiaError jebusError;
//
//        dialogMsg = new StringBuilder();
//        // warnmsgs can never be null
//        for (String msg : warnmsgs) {
//            dialogMsg.append(msg);
//            dialogMsg.append("\n");
//        }
//        LOGGER.warn(dialogMsg.toString());
//        jebusError = new AlixiaError();
//        jebusError.setMessage(dialogMsg.toString());       
//    }
//    
//    /**
//     * Create a warning message from a template plus argument strings.
//     * <br />
//     * Example: warningTemplate("This {} is {}!", "method", "awesome") yields "This method is awesome!"
//     * 
//     * @param template The template to pass as a pattern to MessageFormat, e.g. "This {} is {}."
//     * @param warnmsgs A series of warning message strings separated by commas (as varargs)
//     */
//    public static void warningTemplate(String template, String... warnmsgs) {
//        String message;
//        AlixiaError jebusError;
//        
//        SharedUtils.checkNotNull(template);
//        message = MessageFormat.format(template, (Object[])warnmsgs);
//        LOGGER.warn(message);
//        jebusError = new AlixiaError();
//        jebusError.setMessage(message);       
//    }
//	
//    /**
//     * Create an error message from argument strings.
//     * 
//     * @param errmsgs A series of error message strings separated by commas. Each
//     * string appears on a separate line in the output dialog.
//     */
//    public static void error(String... errmsgs) {
//        StringBuilder dialogMsg;
//        AlixiaError jebusError;
//
//        dialogMsg = new StringBuilder();
//        // errmsgs can never be null
//        for (String msg : errmsgs) {
//            dialogMsg.append(msg);
//            dialogMsg.append("\n");
//        }
//        LOGGER.error(dialogMsg.toString());
//        jebusError = new AlixiaError();
//        jebusError.setMessage(dialogMsg.toString());       
//   }
//
//    
//    /**
//     * Create an error message from a template plus argument strings.
//     * <br />
//     * Example: errorTemplate("This {} is {}!", "method", "awesome") yields "This method is awesome!"
//     * 
//     * @param template The template to pass as a pattern to MessageFormat, e.g. "This {} is {}."
//     * @param errmsgs A series of error message strings separated by commas (as varargs)
//     */
//    public static void errorTemplate(String template, String... errmsgs) {
//        String message;
//        AlixiaError jebusError;
//        
//        SharedUtils.checkNotNull(template);
//        message = MessageFormat.format(template, (Object[])errmsgs);
//        LOGGER.error(message);
//        jebusError = new AlixiaError();
//        jebusError.setMessage(message);       
//    }
//
//    /**
//     * Create an error message from its argument string and the exception.
//     * 
//     * @param errmsg The error message to log.
//     * @param ex The exception to log.
//     */
//    public static void error(String errmsg, Throwable ex) {
//        AlixiaError jebusError;
//
//        SharedUtils.checkNotNull(errmsg);
//        LOGGER.error(errmsg);
//        jebusError = new AlixiaError();
//        jebusError.setMessage(errmsg);       
//    }
	
    /**
     * Given a java.time.Instant, convert it to a java.time.LocalDateTime.
     * 
     * @param instant The date to convert
     * @return The equivalent LocalDateTime 
     */
	public static LocalDateTime ldtFromInstant(Instant instant) {
		LocalDateTime ldt;
		
		SharedUtils.checkNotNull(instant);
		ldt = LocalDateTime.ofInstant(instant, DEFAULTZONE);
		return ldt;
	}
	
    /**
     * Given a java.util.Date, convert it to a java.time.LocalDateTime.
     * 
     * @param utilDate The date to convert
     * @return The equivalent LocalDateTime 
     */
	public static LocalDateTime ldtFromUtilDate(Date utilDate) {
		LocalDateTime ldt;
		
		SharedUtils.checkNotNull(utilDate);
		ldt = LocalDateTime.ofInstant(utilDate.toInstant(), DEFAULTZONE);
		return ldt;
	}
	
    /**
     * Given a java.util.Date, convert it to a java.time.LocalDate.
     * 
     * @param utilDate The date to convert
     * @return The equivalent LocalDate 
     */
	public static LocalDate ldFromUtilDate(Date utilDate) {
		LocalDateTime ldt;
		
		SharedUtils.checkNotNull(utilDate);
		ldt = ldtFromUtilDate(utilDate);
		return ldt.toLocalDate();
	}

	/**
     * Given a java.time.LocalDateTime, convert it to a java.util.Date.
     * 
     * @param ldt The LocalDateTime to convert
     * @return The equivalent java.util.Date 
     */
	public static Date utilDateFromLDT(LocalDateTime ldt) {
		Date utilDate;
		
		SharedUtils.checkNotNull(ldt);
		utilDate = Date.from(ldt.atZone(DEFAULTZONE).toInstant());
		return utilDate;
	}	

	/**
     * Given a java.time.LocalDate, convert it to a java.util.Date.
     * 
     * @param ld The LocalDate to convert
     * @return The equivalent java.util.Date 
     */
	public static Date utilDateFromLD(LocalDate ld) {
		Date utilDate;
		LocalDateTime ldt;
		
		SharedUtils.checkNotNull(ld);
		ldt = ld.atStartOfDay();
		utilDate = utilDateFromLDT(ldt);
		return utilDate;
	}
	
	/**
	 * Transform a Resource (accessible by the ClassLoader) into a String.
	 * 
	 * @param cl The ClassLoader
	 * @param resourcePath The path to the resource
	 * @return The resource as a String
	 */
	public static String getResourceAsString(ClassLoader cl, String resourcePath) {
		String text = null;

		SharedUtils.checkNotNull(cl);
		SharedUtils.checkNotNull(resourcePath);
		try (InputStream in = cl.getResourceAsStream(resourcePath)) {
			if (in == null) {
		    	throw new AlixiaException("AlixiaUtils: resource input stream is null");
			}
			try (InputStreamReader inr = new InputStreamReader(in)) {
				text = CharStreams.toString(inr);
			}
		} catch (IOException e) {
	    	throw new AlixiaException("AlixiaUtils: can't read resource");
		}
        return text;
	}
    
    /**
     * Return the value of the System property for the O/S architecture.
     * 
     * @return The O/S architecture as a String
     */
    public static String getOsArchitecture() {
        
        return System.getProperty("os.arch");
    }
    
    /**
     * Return the value of the System property for the O/S name.
     * 
     * @return The O/S name as a String
     */
    public static String getOsName() {
        
        return System.getProperty("os.name");
    }
    
    /**
     * Return the value of the System property for the O/S version.
     * 
     * @return The O/S version as a String
     */
    public static String getOsVersion() {
        
        return System.getProperty("os.version");
    }
    
    /**
     * Return the value of the System property for the Java vendor.
     * 
     * @return The name of the Java vendor as a String
     */
    public static String getJavaVendor() {
        
        return System.getProperty("java.vendor");
    }
    
    /**
     * Return the value of the System property for the Java version.
     * 
     * @return The Java version as a String
     */
    public static String getJavaVersion() {
        
        return System.getProperty("java.version");
    }
    
    /**
     * Return the value of the System property for the value of environment variable JAVA_HOME.
     * 
     * @return The value of JAVA_HOME as a String
     */
    public static String getJavaHome() {
        
        return System.getProperty("java.home");
    }
    
    /**
     * Return the value of the System property for the user's home directory.
     * 
     * @return The folder name of the user's home directory as a String
     */
    public static String getUserHome() {
        
        return System.getProperty("user.home");
    }
    
    /**
     * Return the value of the System property for the user's name.
     * 
     * @return The name of the user as a String
     */
    public static String getUserName() {
        
        return System.getProperty("user.name");
    }
	
	

}
