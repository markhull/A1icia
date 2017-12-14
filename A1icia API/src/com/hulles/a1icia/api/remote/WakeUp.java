package com.hulles.a1icia.api.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.A1iciaAPIException;
import com.hulles.a1icia.media.MediaUtils;

public class WakeUp {
	private final static Logger LOGGER = Logger.getLogger("A1iciaAPI.WakeUp");
	final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	
	public static void wakeUpLinux() {
		
        String[] wakeupArgs = {"xset", "s", "reset"};
        statCommand(wakeupArgs);
        String[] defaultArgs = {"xset", "s", "default"};
        statCommand(defaultArgs);
	}
	
	private static int statCommand(String[] cmd) {
		ProcessBuilder builder;
		Process proc;
		int retVal;
		StringBuilder stdOut;
		StringBuilder stdErr;
		char[] charBuffer;
		int bufLen;
		
	    MediaUtils.checkNotNull(cmd);
		builder = new ProcessBuilder(cmd);
		builder.redirectErrorStream(false); // don't merge stderr w/ stdout
		try {
			proc = builder.start();
		} catch (IOException e) {
			throw new A1iciaAPIException("WakeUp: can't start statCommand process", e);
		}
		
		stdOut = new StringBuilder();
		charBuffer = new char[1024];
		try (BufferedReader inStream = new BufferedReader(
				new InputStreamReader(proc.getInputStream()))){
			while ((bufLen = inStream.read(charBuffer)) > 0) {
				stdOut.append(charBuffer, 0, bufLen);
			}
		} catch (IOException e) {
			throw new A1iciaAPIException("WakeUp: can't read runCommand stream", e);
		}
		LOGGER.log(LOGLEVEL, "statCommand: " + stdOut);
		
		stdErr = new StringBuilder();
		charBuffer = new char[1024];
		try (BufferedReader errStream = new BufferedReader(
				new InputStreamReader(proc.getErrorStream()))){
			while ((bufLen = errStream.read(charBuffer)) > 0) {
				stdErr.append(charBuffer, 0, bufLen);
			}
		} catch (IOException e) {
			throw new A1iciaAPIException("Can't read stderr", e);
		}
		if (stdErr.length() > 0) {
			LOGGER.log(LOGLEVEL, "statCommand Error: " + stdErr.toString());
			System.err.println("statCommand Error: " + stdErr.toString());
			throw new A1iciaAPIException("WakeUp: standard error not empty");
		}
	
		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			throw new A1iciaAPIException("WakeUp: statCommand interrupted", e);
		}
		
		try {
			retVal = proc.exitValue();
		} catch (IllegalThreadStateException e) {
			throw new A1iciaAPIException("WakeUp: statCommand not terminated when queried", e);
		}
		return retVal;
	}

}
