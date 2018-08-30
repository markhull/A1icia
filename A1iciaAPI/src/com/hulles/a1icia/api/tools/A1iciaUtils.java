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
package com.hulles.a1icia.api.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.google.common.base.CharMatcher;
import com.google.common.io.CharStreams;
import com.hulles.a1icia.api.shared.A1iciaException;
import com.hulles.a1icia.api.shared.SharedUtils;

/**
 * An olio class for various useful functions in A1icia. These methods are <b>NOT</b> GWT-safe.
 * 
 * @see com.hulles.a1icia.api.SharedUtils
 * 
 * @author hulles
 *
 */
public final class A1iciaUtils {
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
		
		return A1iciaUtils.formatElapsedMillis(seconds * 1000);
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
    	
    /**
     * Create a warning message from argument strings.
     * 
     * @param warnmsgs A series of warning message strings separated by commas. Each
     * string appears on a separate line in the output dialog.
     */
    public static void warning(String... warnmsgs) {
        StringBuilder dialogMsg;
        A1iciaError jebusError;

        dialogMsg = new StringBuilder();
        // warnmsgs can never be null
        for (String msg : warnmsgs) {
            dialogMsg.append(msg);
            dialogMsg.append("\n");
        }
        System.err.println();
        System.err.println(dialogMsg.toString());
        jebusError = new A1iciaError();
        jebusError.setMessage(dialogMsg.toString());       
    }
	
    /**
     * Create an error message from argument strings.
     * 
     * @param errmsgs A series of error message strings separated by commas. Each
     * string appears on a separate line in the output dialog.
     */
    public static void error(String... errmsgs) {
        StringBuilder dialogMsg;
        A1iciaError jebusError;

        dialogMsg = new StringBuilder();
        // errmsgs can never be null
        for (String msg : errmsgs) {
            dialogMsg.append(msg);
            dialogMsg.append("\n");
        }
        System.err.println();
        System.err.println(dialogMsg.toString());
        jebusError = new A1iciaError();
        jebusError.setMessage(dialogMsg.toString());       
   }

    /**
     * Create an error message from its argument string and the exception.
     * 
     * @param errmsg The error message to log.
     * @param ex The exception to log.
     */
    public static void error(String errmsg, Throwable ex) {
        A1iciaError jebusError;

        SharedUtils.checkNotNull(errmsg);
        System.err.println();
        System.err.println(errmsg);
        jebusError = new A1iciaError();
        jebusError.setMessage(errmsg);       
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
		    	throw new A1iciaException("A1iciaUtils: resource input stream is null");
			}
			try (InputStreamReader inr = new InputStreamReader(in)) {
				text = CharStreams.toString(inr);
			}
		} catch (IOException e) {
	    	throw new A1iciaException("A1iciaUtils: can't read resource");
		}
        return text;
	}

}
