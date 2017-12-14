/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
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
package com.hulles.a1icia.media.audio;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import com.hulles.a1icia.media.A1iciaMediaException;
import com.hulles.a1icia.media.Language;
import com.hulles.a1icia.media.MediaUtils;

public class TTSPico {
	private static final String PICOEXEC = "pico2wave";
	private final static Language DEFAULT_LANG = Language.AMERICAN_ENGLISH;
//	private final static Logger logger = Logger.getLogger("A1iciaMedia.TTSPico");
//	private final static Level LOGLEVEL = Level.FINE;
/*
	Valid languages:
		en-US
		en-GB
		de-DE
		es-ES
		fr-FR
		it-IT
*/	
	public static File ttsToFile(String text) {
		return ttsToFile(DEFAULT_LANG, text);
	}
    public static File ttsToFile(Language lang, String text) {
        Path tempFile = null;
        String tempFileName;
        
        MediaUtils.checkNotNull(lang);
        MediaUtils.checkNotNull(text);
        try {
        	tempFile = Files.createTempFile(null, ".wav");
 	        tempFileName = tempFile.toString();
            String[] args = {PICOEXEC, "-l", lang.getPicoName(), "-w", tempFileName, text};
            MediaUtils.statCommand(args);
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        }
        if (tempFile == null) {
        	return null;
        }
        return tempFile.toFile();
    }
	
    public static byte[] ttsToBytes(String text) {
    	return ttsToBytes(DEFAULT_LANG, text);
    }
    public static byte[] ttsToBytes(Language lang, String text) {
        Path tempFile = null;
        String tempFileName;
        byte[] ttsBytes;
        
        MediaUtils.checkNotNull(lang);
        MediaUtils.checkNotNull(text);
        try {
        	tempFile = Files.createTempFile(null, ".wav");
 	        tempFileName = tempFile.toString();
            String[] args = {PICOEXEC, "-l", lang.getPicoName(), "-w", tempFileName, text};
            MediaUtils.statCommand(args);
            ttsBytes = MediaUtils.fileToByteArray(tempFileName);
            Files.delete(tempFile);
            tempFile = null;
            return ttsBytes;
        }
        catch (Exception ex) {
			throw new A1iciaMediaException("TTSPico: can't run pico2wave", ex);
        } finally {
	        try {
	            if (tempFile != null) {
	                Files.delete(tempFile);
	            }
	        }catch (Exception ex) {
				throw new A1iciaMediaException("TTSPico: can't delete temp file", ex);
	        }
	    }
    }
	
    public static void ttsToAudio(String text) {
    	ttsToAudio(DEFAULT_LANG, text);
    }
    public static void ttsToAudio(Language lang, String text) {
        byte[] ttsBytes;
        
        MediaUtils.checkNotNull(lang);
        MediaUtils.checkNotNull(text);
        ttsBytes = ttsToBytes(lang, text);
        try {
			AudioBytePlayer.playAudioFromByteArray(ttsBytes);
		} catch (Exception ex) {
			throw new A1iciaMediaException("TTSPico: can't play audio bytes", ex);
		}
    }
}
