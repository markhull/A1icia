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
 *******************************************************************************/
package com.hulles.a1icia.foxtrot.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.MoreFiles;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.tools.A1iciaUtils;

public final class FoxtrotUtils {
	private static final int RUNCOMMANDLEN = 50 * 1024;
	
	static String getStringFromFile(String fileName) {
		Path filePath;
		String out = null;
		
		A1iciaUtils.checkNotNull(fileName);
		filePath = Paths.get(fileName);
		if (!Files.exists(filePath)) {
			return null;
		}
		try {
			out = MoreFiles.asCharSource(filePath, StandardCharsets.UTF_8).read();
		} catch (IOException e) {
			e.printStackTrace();
			throw new A1iciaException("IOException reading file " + fileName);
		}
		return out;
	}
	
	static List<String> getLinesFromFile(String fileName) {
		List<String> results = null;
		Path filePath;
		
		A1iciaUtils.checkNotNull(fileName);
		filePath = Paths.get(fileName);
		if (!Files.exists(filePath)) {
			return null;
		}
		try {
//			results = Files.readAllLines(filePath);
			results = MoreFiles.asCharSource(filePath, StandardCharsets.UTF_8).readLines();
		} catch (IOException e) {
			e.printStackTrace();
			throw new A1iciaException("IOException reading file " + fileName);
		}
		return results;
	}
	
	public static String matchPatternInFile(Pattern pattern, String fileName) {
		String result;
		Matcher matcher;
		String firstMatch;
		
		A1iciaUtils.checkNotNull(pattern);
		A1iciaUtils.checkNotNull(fileName);
		result = getStringFromFile(fileName);
		if (result == null) {
			return null;
//			throw new A1iciaException("No input from file " + fileName);
		}
        matcher = pattern.matcher(result);
        matcher.find();
        firstMatch = matcher.group(1);
        return firstMatch;
	}
	
	static int statCommand(String[] cmd) {
		ProcessBuilder builder;
		Process proc;
		int retVal;
		
		A1iciaUtils.checkNotNull(cmd);
		builder = new ProcessBuilder(cmd);
		try {
			proc = builder.start();
		} catch (IOException e) {
			e.printStackTrace();
			throw new A1iciaException("Can't start statCommand process");
		}
		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new A1iciaException("statCommand interrupted");
		}
		try {
			retVal = proc.exitValue();
		} catch (IllegalThreadStateException e) {
			e.printStackTrace();
			throw new A1iciaException("statCommand not terminated when queried");
		}
		return retVal;
	}
	static int statCommand(String cmd) {
		
		A1iciaUtils.checkNotNull(cmd);
		return statCommand(cmd.split(" "));
	}
	
	static StringBuilder runCommand(String[] cmd) {
		ProcessBuilder builder;
		Process proc;
		StringBuilder stdOut;
		StringBuilder stdErr;
		char[] charBuffer;
		int bufLen;
		
		A1iciaUtils.checkNotNull(cmd);
		builder = new ProcessBuilder(cmd);
		builder.redirectErrorStream(false); // don't merge stderr w/ stdout
		try {
			proc = builder.start();
		} catch (IOException e) {
			e.printStackTrace();
			throw new A1iciaException("Can't start runCommand process: lm-sensors installed?");
		}
		try (BufferedReader inStream = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
			stdOut = new StringBuilder(RUNCOMMANDLEN);
			charBuffer = new char[1024];
			try {
				while ((bufLen = inStream.read(charBuffer)) > 0) {
					stdOut.append(charBuffer, 0, bufLen);
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new A1iciaException("Can't read runCommand stream");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new A1iciaException("Can't close runCommand stdout stream");
		}
		
		try (BufferedReader errStream = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
			stdErr = new StringBuilder();
			charBuffer = new char[1024];
			try {
				while ((bufLen = errStream.read(charBuffer)) > 0) {
					stdErr.append(charBuffer, 0, bufLen);
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new A1iciaException("Can't read stderr");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new A1iciaException("Can't close runCommand stderr stream");
		}
		if (stdErr.length() > 0) {
			A1iciaUtils.error("runCommand produced stderr stream, as follows:", stdErr.toString());
		}
		
		proc.destroy();
		return stdOut;
	}
	static StringBuilder runCommand(String cmd) {
		return runCommand(cmd.split(" "));
	}

    public static String execReadToString(String execCommand) {
    	Process proc;
    	String result;
    	
    	try {
	        proc = Runtime.getRuntime().exec(execCommand);
	        try (InputStream stream = proc.getInputStream()) {
	        	try (Scanner scanner = new Scanner(stream)) {
//					scanner.useDelimiter("\\A"); 
		            result = scanner.hasNext() ? scanner.next() : "";
	        	}
	        }
		} catch (IOException e) {
			e.printStackTrace();
			throw new A1iciaException("IO error in execReadToString");
    	}
        return result;
    }	
}
