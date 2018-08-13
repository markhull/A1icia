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
package com.hulles.a1icia.media.audio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.media.A1iciaMediaException;
import com.hulles.a1icia.media.MediaUtils;

/**
 * Record audio using aplayer external command. Note: to use Google Speech, the file type needs to
 * be "raw"; to use Sphinx4, the file type needs to be "wav".
 * 
 * @author hulles
 *
 */
public class AudioRecorder {
	private final static Logger LOGGER = Logger.getLogger("A1iciaPi.AudioRecorder");
	private final static Level LOGLEVEL = Level.FINE;
	private final static String RECORDEXEC = "arecord";
	private final static boolean DELETEFILE = false;
//	private final static float CHUNKFACTOR = 0.1f;
	private final static OutputType OUTPUT_TYPE = OutputType.WAVE;
	
    public static byte[] recordBytes() { 
	   return recordBytes(5);
	}
	public static byte[] recordBytes(int duration) {
        Path tempFile = null;
        byte[] bytes = null;
        String device = "default";
        String fileType;
        boolean deleteFile = DELETEFILE;
        int channels = 1;
        int sampleRateHz = 16000;
        int bytesPerSample = 2;
        // duration is in seconds
        
//        int chunkBytes = (int) ((CHUNKFACTOR * sampleRateHz) * channels * bytesPerSample);
        
        MediaUtils.checkNotNull(duration);
        switch (OUTPUT_TYPE) {
	        case WAVE:
	        	fileType = "wav";
	        	break;
	        case RAW:
	        	fileType = "raw";
	        	break;
        	default:
        		throw new UnsupportedOperationException("Unsupported file type");
        }
        try {
 	        String[] args = {RECORDEXEC, 
 	        		"-q", 
 	        		"-t", fileType,
 	        		"-d", String.valueOf(duration),
 	        		"-D", device, 
 	        		"-c", String.valueOf(channels),
 	        		"-f", sampleWidthToString(bytesPerSample), 
 	        		"-r", String.valueOf(sampleRateHz)
    		};
            tempFile = statCommand(args);
            bytes = MediaUtils.pathToByteArray(tempFile);
        }
        catch (Exception ex) {
			throw new A1iciaMediaException("AudioRecorder: can't run " + RECORDEXEC, ex);
        } finally {
	        try {
	            if (deleteFile && tempFile != null) {
	                Files.delete(tempFile);
	            }
	        } catch (Exception ex) {
				throw new A1iciaMediaException("AudioRecorder: can't delete temp file", ex);
	        }
	    }
        
        return bytes;
    }
	
    public static File recordFile() { 
	   return recordFile(5);
	}
	public static File recordFile(int duration) {
        Path tempFile = null;
        String device = "default";
        String fileType;
        int channels = 1;
        int sampleRateHz = 16000;
        int bytesPerSample = 2;
        // duration is in seconds
        
//        int chunkBytes = (int) ((CHUNKFACTOR * sampleRateHz) * channels * bytesPerSample);
        
        MediaUtils.checkNotNull(duration);
        switch (OUTPUT_TYPE) {
	        case WAVE:
	        	fileType = "wav";
	        	break;
	        case RAW:
	        	fileType = "raw";
	        	break;
	    	default:
	    		throw new UnsupportedOperationException("Unsupported file type");
	    }
        try {
 	        String[] args = {RECORDEXEC, 
 	        		"-q", 
 	        		"-t", fileType,
 	        		"-d", String.valueOf(duration),
 	        		"-D", device, 
 	        		"-c", String.valueOf(channels),
 	        		"-f", sampleWidthToString(bytesPerSample), 
 	        		"-r", String.valueOf(sampleRateHz)
    		};
            tempFile = statCommand(args);
        }
        catch (Exception ex) {
			throw new A1iciaMediaException("AudioRecorder: can't run " + RECORDEXEC, ex);
	    }
        
        return tempFile.toFile();
    }
	
    private static String sampleWidthToString(int width) {
    
    	switch(width) {
	    	case 1:
	    		return "s8";
	    	case 2:
	    		return "s16";
	    	case 4:
	    		return "s32";
    		default:
				throw new A1iciaMediaException("AudioRecorder: bad sample width = " + width);
    	}
    }
	
	private static Path statCommand(String[] cmd) {
		ProcessBuilder builder;
		Process proc;
		int retVal;
//		StringBuilder stdOut;
		StringBuilder stdErr;
		char[] charBuffer;
		int bufLen;
		Path tempFile;
		
        MediaUtils.checkNotNull(cmd);
    	try {
			tempFile = Files.createTempFile(null, ".wav");
		} catch (IOException e1) {
			throw new A1iciaMediaException("AudioRecorder: can't create temp file", e1);
		}
		builder = new ProcessBuilder(cmd);
		builder.redirectOutput(tempFile.toFile());
		builder.redirectErrorStream(false); // don't merge stderr w/ stdout
		try {
			proc = builder.start();
		} catch (IOException e) {
			throw new A1iciaMediaException("AudioRecorder: can't start statCommand process", e);
		}
		
/*		stdOut = new StringBuilder();
		charBuffer = new char[1024];
		try (BufferedReader inStream = new BufferedReader(
				new InputStreamReader(proc.getInputStream()))){
			while ((bufLen = inStream.read(charBuffer)) > 0) {
				stdOut.append(charBuffer, 0, bufLen);
			}
		} catch (IOException e) {
			throw new A1iciaMediaException("AudioRecorder: can't read runCommand stream", e);
		}
		LOGGER.log(LOGLEVEL, "statCommand: " + stdOut);
*/
		
		stdErr = new StringBuilder();
		charBuffer = new char[1024];
		try (BufferedReader errStream = new BufferedReader(
				new InputStreamReader(proc.getErrorStream()))){
			while ((bufLen = errStream.read(charBuffer)) > 0) {
				stdErr.append(charBuffer, 0, bufLen);
			}
		} catch (IOException e) {
			throw new A1iciaMediaException("Can't read stderr", e);
		}
		if (stdErr.length() > 0) {
			LOGGER.log(LOGLEVEL, "statCommand Error: " + stdErr.toString());
			throw new A1iciaMediaException("AudioRecorder: standard error not empty: " + stdErr.toString());
		}

		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			throw new A1iciaMediaException("AudioRecorder: statCommand interrupted", e);
		}
		
		try {
			retVal = proc.exitValue();
		} catch (IllegalThreadStateException e) {
			throw new A1iciaMediaException("AudioRecorder: statCommand not terminated when queried", e);
		}
		
		if (retVal != 0) {
			throw new A1iciaMediaException("AudioRecorder: statCommand terminated with non-zero ret val = " + retVal);
		}
		
		return tempFile;
	}

	private enum OutputType {
		WAVE,
		RAW
	}
}
